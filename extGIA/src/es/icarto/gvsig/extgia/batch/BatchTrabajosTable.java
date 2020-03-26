package es.icarto.gvsig.extgia.batch;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.edition.IEditableSource;

import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.gui.tables.handler.BaseTableHandler;
import es.icarto.gvsig.navtableforms.gui.tables.model.BaseTableModel;
import es.icarto.gvsig.navtableforms.ormlite.ORMLite;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.DomainValues;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.icarto.gvsig.navtableforms.utils.TOCTableManager;
import es.udc.cartolab.gvsig.navtable.dataacces.TableController;
import es.udc.cartolab.gvsig.navtable.format.DateFormatNT;

@SuppressWarnings("serial")
public class BatchTrabajosTable extends JPanel implements IWindow {

    private WindowInfo viewInfo;
    private JTable table;
    private final String dbTableName;
    private final String[] columnNames;
    private final String[] columnDbNames;
    private final String[][] data;
    private final String[][] originalData;

    private final ORMLite ormLite;

    private JButton cancelButton;
    private JButton saveButton;

    private BatchTrabajosTableCalculation calculation;
    private final BaseTableHandler trabajosTableHandler;

    private static final Logger logger = Logger
	    .getLogger(BatchTrabajosTable.class);

    public static String[] editableColumns = { "Fecha", "Unidad", "Medición",
	"Observaciones", "Ancho", "Longitud" };

    public BatchTrabajosTable(String dbTableName, String[][] data,
	    final String[] columnNames, final String[] columnDbNames,
	    BaseTableHandler trabajosTableHandler) {
	super();
	this.dbTableName = dbTableName;
	this.columnNames = columnNames;
	this.columnDbNames = columnDbNames;
	this.ormLite = getOrmLite();
	this.data = data;

	this.originalData = new String[data.length][data[0].length];
	for (int i = 0; i < data.length; i++) {
	    for (int j = 0; j < data[i].length; j++) {
		originalData[i][j] = data[i][j] + "";
	    }
	}

	this.trabajosTableHandler = trabajosTableHandler;
	initTable();
    }

    public ORMLite getOrmLite() {
	return new ORMLite(getClass().getClassLoader()
		.getResource("rules/" + dbTableName + "_metadata.xml")
		.getPath());
    }

    private void initTable() {
	this.setLayout(new BorderLayout());
	table = new JTable(new BatchTrabajosTableModel());
	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	table.getTableHeader().setReorderingAllowed(false);

	JScrollPane scrollPane = new JScrollPane(table);

	cancelButton = new JButton("Cancelar");
	cancelButton.addActionListener(new ButtonsHandler());
	saveButton = new JButton("Guardar");
	saveButton.addActionListener(new ButtonsHandler());

	JPanel buttonPane = new JPanel();
	buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
	buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	buttonPane.add(Box.createHorizontalGlue());
	buttonPane.add(cancelButton);
	buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
	buttonPane.add(saveButton);

	this.add(scrollPane, BorderLayout.CENTER);
	this.add(buttonPane, BorderLayout.SOUTH);

	setUnidadCellEditorAsComboBox();
	setFechaCellEditorAsJDateChooser();

	calculation = new BatchTrabajosTableCalculation(this, dbTableName,
		columnDbNames[0]);
	table.setDefaultRenderer(Object.class, new ColorColumnRenderer(
		calculation));

	calculation.initAllRows();
	autoFit();
	saveButton.setEnabled(calculation.validate());

	table.getModel().addTableModelListener(calculation);
    }

    private void autoFit() {
	int avaliable = table.getColumnModel().getTotalColumnWidth();

	int[] maxLengths = ((BatchTrabajosTableModel) table.getModel())
		.getMaxLengths();
	double needed = 0.0;
	for (int i = 0; i < table.getColumnCount(); i++) {
	    int m = (table.getColumnName(i).length() > maxLengths[i]) ? table
		    .getColumnName(i).length() : maxLengths[i];
	    needed += m;
	}

	for (int i = 0; i < table.getModel().getColumnCount(); i++) {
	    double preferredWidth = avaliable * (maxLengths[i] / needed);

	    preferredWidth = 150;
	    table.getColumnModel().getColumn(i)
	    .setPreferredWidth((int) preferredWidth);
	}
    }

    @Override
    public WindowInfo getWindowInfo() {
	if (viewInfo == null) {
	    viewInfo = new WindowInfo(WindowInfo.ICONIFIABLE
		    | WindowInfo.MAXIMIZABLE | WindowInfo.RESIZABLE
		    | WindowInfo.PALETTE | WindowInfo.MODELESSDIALOG);
	    viewInfo.setTitle(PluginServices.getText(this, "Añadir Trabajos"));
	    viewInfo.setWidth((int) table.getPreferredSize().getWidth() + 10);
	    viewInfo.setHeight(480);
	}
	return viewInfo;
    }

    @Override
    public Object getWindowProfile() {
	return WindowInfo.EXTERNAL_PROFILE;
    }

    private void closeWindow() {
	PluginServices.getMDIManager().closeWindow(this);
    }

    private void showWarning(String msg) {
	JOptionPane.showMessageDialog(
		(Component) PluginServices.getMainFrame(), msg, "Aviso",
		JOptionPane.WARNING_MESSAGE);
    }

    public class ButtonsHandler implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {

	    if (e.getSource() == saveButton) {
		save();
	    }

	    if (e.getSource() == cancelButton) {
		closeWindow();
	    }
	}

	private void save() {
	    stopCellEdition();
	    if (!calculation.validate()) {
		showWarning("Hay valores incorrectos en la tabla");
		saveButton.setEnabled(false);
		return;
	    }

	    try {
		TOCTableManager tm = new TOCTableManager();
		IEditableSource source = tm.getTableModelByName(dbTableName);
		for (int i = 0; i < data.length; i++) {
		    saveRow(source, i);
		}

		notifyChangesToEmbedFormTable();

	    } catch (Exception e) {
		logger.error(e.getStackTrace(), e);
		showWarning("Problema desconocido guardando la capa. Los datos no serán salvados");
		return;
	    }

	    showSavedSuccesfullyMsg();

	    closeWindow();
	}

	private void showSavedSuccesfullyMsg() {
	    JOptionPane.showMessageDialog(
		    (Component) PluginServices.getMainFrame(),
		    PluginServices.getText(this, "addedInfo_msg_I")
			    + data.length + " "
			    + PluginServices.getText(this, "addedInfo_msg_II"));

	}

	private void notifyChangesToEmbedFormTable() {
	    if (trabajosTableHandler != null) {
		BaseTableModel m = (BaseTableModel) trabajosTableHandler
			.getJTable().getModel();
		m.dataChanged();
		// workaround. next line should work if createTableModel in
		// GIATableHandler doesnt ofuscate it
		// trabajosTableHandler.getModel().dataChanged();
	    }
	}

	private void saveRow(IEditableSource source, int i) throws Exception {
	    HashMap<String, String> newValues = new HashMap<String, String>();
	    for (int j = 0; j < data[i].length; j++) {
		String v = (data[i][j] == null) ? "" : data[i][j];
		newValues.put(columnDbNames[j], v);
	    }

	    TableController tableController = new TableController(source);
	    tableController.create(newValues);
	}
    }

    private void stopCellEdition() {
	if (table.isEditing()) {
	    if (table.getCellEditor() != null) {
		table.getCellEditor().stopCellEditing();
	    }
	}
    }

    public HashMap<String, String> getTableDataRow(int row, int columns) {
	HashMap<String, String> rowValues = new HashMap<String, String>();
	for (int i = 0; i < columns; i++) {
	    rowValues.put(columnNames[i], table.getValueAt(row, i).toString());
	}
	return rowValues;
    }

    private void setUnidadCellEditorAsComboBox() {
	DomainValues unidadValues = ormLite.getAppDomain()
		.getDomainValuesForComponent(DBFieldNames.UNIDAD);
	JComboBox unidadComboBox = new JComboBox();
	for (KeyValue value : unidadValues.getValues()) {
	    unidadComboBox.addItem(value.toString());
	}
	Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
	while (columns.hasMoreElements()) {
	    TableColumn unidadColumn = columns.nextElement();
	    if (unidadColumn.getHeaderValue().toString()
		    .equalsIgnoreCase(DBFieldNames.UNIDAD)) {
		unidadColumn
			.setCellEditor(new DefaultCellEditor(unidadComboBox));
		break;
	    }
	}
    }

    private void setFechaCellEditorAsJDateChooser() {
	Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
	while (columns.hasMoreElements()) {
	    TableColumn unidadColumn = columns.nextElement();
	    if (unidadColumn.getHeaderValue().toString()
		    .equalsIgnoreCase(DBFieldNames.FECHA)) {
		DateCellEditor dateEditor = new DateCellEditor(
			DateFormatNT.getDateFormat());
		unidadColumn.setCellEditor(dateEditor);
		break;
	    }
	}
    }

    class BatchTrabajosTableModel extends AbstractTableModel {

	@Override
	public int getColumnCount() {
	    return columnNames.length;
	}

	@Override
	public int getRowCount() {
	    return data.length;
	}

	@Override
	public String getColumnName(int col) {
	    return columnNames[col];
	}

	@Override
	public Object getValueAt(int row, int col) {
	    return data[row][col];
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
	    if ((data[row][col] == null) && (value != null)) {
		data[row][col] = (String) value;
		fireTableCellUpdated(row, col);
	    } else if ((data[row][col] == null) && (value == null)) {
		return;
	    } else if ((data[row][col] != null)
		    && (!data[row][col].equals(value))) {
		data[row][col] = (String) value;
		fireTableCellUpdated(row, col);
	    }
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
	    TableColumn column = table.getColumnModel().getColumn(columnIndex);
	    for (String s : editableColumns) {
		if (s.equals(column.getHeaderValue().toString())) {
		    return true;
		}
	    }
	    return false;
	}

	public int[] getMaxLengths() {
	    int[] maxLengths = new int[getColumnCount()];
	    Arrays.fill(maxLengths, 50);

	    for (int i = 0; i < getRowCount(); i++) {
		for (int j = 0; j < getColumnCount(); j++) {
		    final Object o = getValueAt(i, j);
		    int l = (o == null) ? 0 : o.toString().length();
		    if (l > maxLengths[j]) {
			maxLengths[j] = l;
		    }
		}
	    }
	    return maxLengths;
	}
    }

    public JButton getSaveButton() {
	return saveButton;
    }

    public String[] getColumnDbNames() {
	return columnDbNames;
    }

    public JTable getTable() {
	return table;
    }

    public String[][] getOrinalData() {
	return originalData;
    }
}
