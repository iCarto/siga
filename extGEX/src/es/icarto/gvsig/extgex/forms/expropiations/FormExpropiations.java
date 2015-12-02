package es.icarto.gvsig.extgex.forms.expropiations;

import java.awt.event.ActionEvent;
import static es.icarto.gvsig.extgex.forms.expropiations.ImporteTotalPagadoCalculation.FINCAS_IMPORTE_PAGADO_TOTAL_AUTOCALCULADO; 
import static es.icarto.gvsig.extgex.forms.expropiations.ImportePendienteTotalCalculation.FINCAS_IMPORTE_PENDIENTE_TOTAL_AUTOCALCULADO;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgex.navtable.NavTableComponentsFactory;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.extgex.utils.retrievers.LocalizadorFormatter;
import es.icarto.gvsig.navtableforms.BasicAbstractForm;
import es.icarto.gvsig.navtableforms.gui.CustomTableModel;
import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.listeners.DependentComboboxHandler;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.udc.cartolab.gvsig.navtable.format.DateFormatNT;
import es.udc.cartolab.gvsig.navtable.format.DoubleFormatNT;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class FormExpropiations extends BasicAbstractForm implements
	TableModelListener {

    private static final String EXPROPIATIONS_AFECTADO_PM = "afectado_por_policia_margenes";

    public static final String TABLENAME = "exp_finca";
    public static final Object TOCNAME = "Fincas";
    public static final String PKFIELD = "id_finca";

    private static final String WIDGET_REVERSIONES = "tabla_reversiones_afectan";
    private static final String WIDGET_EXPROPIACIONES = "expropiaciones";
    private static final String WIDGET_PM = "tabla_pm_afectan";

    private JComboBox tramo;
    private JComboBox uc;
    private JComboBox ayuntamiento;
    private JComboBox subtramo;
    private JTextField finca;
    private JTextField numFinca;
    private JTextField seccion;

    private JTable expropiaciones;
    private JTable reversiones;
    private JTable pm;

    private JComboBox afectado_pm;

    private AddExpropiationListener addExpropiationListener;
    private DeleteExpropiationListener deleteExpropiationListener;
    private JButton addExpropiationButton;
    private JButton deleteExpropiationButton;

    private DependentComboboxHandler ayuntamientoDomainHandler;
    private DependentComboboxHandler subtramoDomainHandler;
    private UpdateNroFincaHandler updateNroFincaHandler;

    private FormReversionsLauncher formReversionsLauncher;

    private ArrayList<String> oldReversions;

    public FormExpropiations(FLyrVect layer, IGeometry insertedGeom) {
	super(layer);

	addButtonsToActionsToolBar();

	addCalculation(new ImporteTotalPagadoCalculation(this));
	addCalculation(new ImportePendienteTotalCalculation(this));
	addChained(DBNames.FIELD_UC_FINCAS, DBNames.FIELD_TRAMO_FINCAS);
	initTooltips();
    }

    private void initTooltips() {
	getFormPanel().getTextField(FINCAS_IMPORTE_PAGADO_TOTAL_AUTOCALCULADO).setToolTipText("<html>Depósito previo imp. consignado <br> + Depósito previo imp. indemnización <br> + Depósito previo imp. pagado <br> + Mútuo acuerdo importe <br> + Anticipo importe <br> + Mútuo acuerdo parcial importe <br> + Límite acuerdo importe <br> + Imp. pagos varios <br> + Otros pagos imp. indemnización <br> +  Imp. justiprecio <br> - Depósito previo imp. levantamiento</html>");
	getFormPanel().getTextField(FINCAS_IMPORTE_PENDIENTE_TOTAL_AUTOCALCULADO).setToolTipText("Imp. terrenos pendiente + Imp. mejoras pendiente");
    }

    private void addButtonsToActionsToolBar() {
	JPanel actionsToolBar = this.getActionsToolBar();
	NavTableComponentsFactory ntFactory = new NavTableComponentsFactory();
	JButton filesLinkB = ntFactory.getFilesLinkButton(layer, this);
	JButton printReportB = ntFactory.getPrintButton(layer, this);
	if ((filesLinkB != null) && (printReportB != null)) {
	    actionsToolBar.add(filesLinkB);
	    actionsToolBar.add(printReportB);
	}
	actionsToolBar.add(new JButton(new AddFincaAction(layer, this)));
    }

    @Override
    protected String getPrimaryKeyValue() {
	return getFormController().getValue(PKFIELD);
    }

    @Override
    protected void setListeners() {
	super.setListeners();

	// RETRIEVE WIDGETS
	Map<String, JComponent> widgets = getWidgets();

	tramo = (JComboBox) widgets.get(DBNames.FIELD_TRAMO_FINCAS);
	uc = (JComboBox) widgets.get(DBNames.FIELD_UC_FINCAS);
	ayuntamiento = (JComboBox) widgets
		.get(DBNames.FIELD_AYUNTAMIENTO_FINCAS);
	subtramo = (JComboBox) widgets
		.get(DBNames.FIELD_PARROQUIASUBTRAMO_FINCAS);

	numFinca = (JTextField) widgets.get(DBNames.FIELD_NUMEROFINCA_FINCAS);
	seccion = (JTextField) widgets.get(DBNames.FIELD_SECCION_FINCAS);

	finca = (JTextField) widgets.get(DBNames.FIELD_IDFINCA);

	expropiaciones = (JTable) widgets.get(WIDGET_EXPROPIACIONES);
	reversiones = (JTable) widgets.get(WIDGET_REVERSIONES);
	pm = (JTable) widgets.get(WIDGET_PM);

	afectado_pm = (JComboBox) widgets.get(EXPROPIATIONS_AFECTADO_PM);

	addExpropiationListener = new AddExpropiationListener();
	addExpropiationButton = (JButton) formBody
		.getComponentByName("expropiaciones_add_button");
	addExpropiationButton.addActionListener(addExpropiationListener);

	deleteExpropiationListener = new DeleteExpropiationListener();
	deleteExpropiationButton = (JButton) formBody
		.getComponentByName("expropiaciones_delete_button");
	deleteExpropiationButton.addActionListener(deleteExpropiationListener);

	ayuntamientoDomainHandler = new DependentComboboxHandler(this, uc,
		ayuntamiento);
	uc.addActionListener(ayuntamientoDomainHandler);

	ArrayList<JComponent> parentComponents = new ArrayList<JComponent>();
	parentComponents.add(uc);
	parentComponents.add(ayuntamiento);
	subtramoDomainHandler = new DependentComboboxHandler(this,
		parentComponents, subtramo);
	ayuntamiento.addActionListener(subtramoDomainHandler);

	updateNroFincaHandler = new UpdateNroFincaHandler();
	subtramo.addActionListener(updateNroFincaHandler);
	numFinca.addKeyListener(updateNroFincaHandler);
	seccion.addKeyListener(updateNroFincaHandler);

	formReversionsLauncher = new FormReversionsLauncher(this);
	reversiones.addMouseListener(formReversionsLauncher);

    }

    @Override
    protected void removeListeners() {
	super.removeListeners();

	uc.removeActionListener(ayuntamientoDomainHandler);
	ayuntamiento.removeActionListener(subtramoDomainHandler);
	subtramo.removeActionListener(updateNroFincaHandler);
	numFinca.removeKeyListener(updateNroFincaHandler);
	seccion.removeKeyListener(updateNroFincaHandler);

	reversiones.removeMouseListener(formReversionsLauncher);

	addExpropiationButton.removeActionListener(addExpropiationListener);
	deleteExpropiationButton
		.removeActionListener(deleteExpropiationListener);
    }

    public class AddExpropiationListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    SubformExpropiationsAddExpropiation subForm = new SubformExpropiationsAddExpropiation(
		    expropiaciones);
	    PluginServices.getMDIManager().addWindow(subForm);
	}
    }

    public class DeleteExpropiationListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    int[] selectedRows = expropiaciones.getSelectedRows();
	    DefaultTableModel model = (DefaultTableModel) expropiaciones
		    .getModel();
	    for (int i = 0; i < selectedRows.length; i++) {
		int rowIndex = selectedRows[i];
		model.removeRow(rowIndex);
		repaint();
	    }
	}
    }

    private void setIDFinca() {
	if ((tramo.getSelectedItem() instanceof KeyValue)
		&& (uc.getSelectedItem() instanceof KeyValue)
		&& (ayuntamiento.getSelectedItem() instanceof KeyValue)
		&& (subtramo.getSelectedItem() instanceof KeyValue)) {
	    // will update id_finca only when comboboxes have proper values
	    String id_finca = LocalizadorFormatter.getTramo(((KeyValue) tramo
		    .getSelectedItem()).getKey())
		    + LocalizadorFormatter.getUC(((KeyValue) uc
			    .getSelectedItem()).getKey())
		    + LocalizadorFormatter
			    .getAyuntamiento(((KeyValue) ayuntamiento
				    .getSelectedItem()).getKey())
		    + LocalizadorFormatter.getSubtramo(((KeyValue) subtramo
			    .getSelectedItem()).getKey())
		    + getStringNroFincaFormatted()
		    + getStringSeccionFormatted();
	    finca.setText(id_finca);
	    getFormController().setValue(DBNames.FIELD_IDFINCA, id_finca);
	}
    }

    public String getIDFinca() {
	return finca.getText();
    }

    private String getStringNroFincaFormatted() {
	HashMap<String, String> values = getFormController().getValuesChanged();
	try {
	    String formatted = LocalizadorFormatter.getNroFinca(values
		    .get(DBNames.FIELD_NUMEROFINCA_FINCAS));
	    numFinca.setText(formatted);
	    getFormController().setValue(DBNames.FIELD_NUMEROFINCA_FINCAS,
		    formatted);
	    return formatted;
	} catch (NumberFormatException nfe) {
	    numFinca.setText(LocalizadorFormatter.FINCA_DEFAULT_VALUE);
	    getFormController().setValue(DBNames.FIELD_NUMEROFINCA_FINCAS,
		    LocalizadorFormatter.FINCA_DEFAULT_VALUE);
	    return LocalizadorFormatter.FINCA_DEFAULT_VALUE;
	}
    }

    private String getStringSeccionFormatted() {
	HashMap<String, String> values = getFormController().getValuesChanged();
	String formatted = values.get(DBNames.FIELD_SECCION_FINCAS);
	seccion.setText(formatted);
	getFormController().setValue(DBNames.FIELD_SECCION_FINCAS, formatted);
	return formatted;
    }

    public class UpdateNroFincaHandler implements KeyListener, ActionListener {

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	    if (!isFillingValues()) {
		setIDFinca();
	    }
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    if (!isFillingValues()) {
		setIDFinca();
	    }
	}
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();
	ayuntamientoDomainHandler.updateComboBoxValues();
	subtramoDomainHandler.updateComboBoxValues();
	updateJTables();
    }

    private void updateJTables() {
	oldReversions = new ArrayList<String>();

	updateExpropiationsTable();
	updateReversionsTable();
	updatePMTable();

    }

    private void updateExpropiationsTable() {
	ArrayList<String> columnasCultivos = new ArrayList<String>();
	columnasCultivos.add(DBNames.FIELD_SUPERFICIE_EXPROPIACIONES);
	columnasCultivos.add(DBNames.FIELD_IDCULTIVO_EXPROPIACIONES);

	try {
	    DefaultTableModel tableModel;
	    tableModel = new DefaultTableModel();
	    for (String columnName : columnasCultivos) {
		tableModel.addColumn(columnName);
	    }
	    expropiaciones.setModel(tableModel);
	    Value[] expropiationData = new Value[3];
	    PreparedStatement statement;
	    String query = "SELECT " + DBNames.FIELD_SUPERFICIE_EXPROPIACIONES
		    + ", " + DBNames.FIELD_DESCRIPCION_CULTIVOS + " " + "FROM "
		    + DBNames.SCHEMA_DATA + "." + DBNames.TABLE_EXPROPIACIONES
		    + " a, " + DBNames.SCHEMA_DATA + "."
		    + DBNames.TABLE_CULTIVOS + " b " + "WHERE a."
		    + DBNames.FIELD_IDCULTIVO_EXPROPIACIONES + " = " + "b."
		    + DBNames.FIELD_ID_CULTIVO_CULTIVOS + " AND "
		    + DBNames.FIELD_ID_FINCA_EXPROPIACIONES + " = '"
		    + getIDFinca() + "';";
	    statement = DBSession.getCurrentSession().getJavaConnection()
		    .prepareStatement(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    while (rs.next()) {
		if (rs.getObject(1) != null) {
		    NumberFormat doubleFormat = DoubleFormatNT
			    .getDisplayingFormat();
		    Double doubleValue = rs.getDouble(1);
		    String doubleAsString = doubleFormat.format(doubleValue);
		    expropiationData[0] = ValueFactory
			    .createValue(doubleAsString);
		} else {
		    expropiationData[0] = null;
		}
		if (rs.getObject(2) != null) {
		    expropiationData[1] = ValueFactory.createValue(rs
			    .getString(2));
		} else {
		    expropiationData[1] = null;
		}
		tableModel.addRow(expropiationData);
		// Save current Fincas in order to remove them
		// from database when there is some change in the table model
		// oldExpropiations.add(rs.getString(1));
	    }
	    repaint();
	    tableModel.addTableModelListener(this);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    private void updateReversionsTable() {
	DefaultTableModel tableModel = setTableHeader();
	try {
	    reversiones.setModel(tableModel);
	    Value[] reversionData = new Value[tableModel.getColumnCount()];
	    PreparedStatement statement;
	    String query = "SELECT "
		    + DBNames.FIELD_IDREVERSION_FINCA_REVERSION + ", "
		    + DBNames.FIELD_SUPERFICIE_FINCA_REVERSION + ", "
		    + DBNames.FIELD_IMPORTE_FINCA_REVERSION_EUROS + ", "
		    + DBNames.FIELD_IMPORTE_FINCA_REVERSION_PTAS + ", "
		    + DBNames.FIELD_FECHA_FINCA_REVERSION + " " + "FROM "
		    + DBNames.SCHEMA_DATA + "." + DBNames.TABLE_FINCA_REVERSION
		    + " " + "WHERE "
		    + DBNames.FIELD_IDEXPROPIACION_FINCA_REVERSION + " = '"
		    + getIDFinca() + "';";
	    statement = DBSession.getCurrentSession().getJavaConnection()
		    .prepareStatement(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    while (rs.next()) {
		reversionData[0] = ValueFactory.createValue(rs.getString(1));
		if (rs.getObject(2) != null) {
		    reversionData[1] = ValueFactory
			    .createValue(getDoubleFormatted(rs.getDouble(2)));
		} else {
		    reversionData[1] = ValueFactory.createNullValue();
		}
		if (rs.getObject(3) != null) {
		    reversionData[2] = ValueFactory
			    .createValue(getDoubleFormatted(rs.getDouble(3)));
		} else {
		    reversionData[2] = ValueFactory.createNullValue();
		}
		if (rs.getObject(4) != null) {
		    reversionData[3] = ValueFactory.createValue(rs.getInt(4));
		} else {
		    reversionData[3] = ValueFactory.createNullValue();
		}
		if (rs.getObject(5) != null) {
		    reversionData[4] = ValueFactory
			    .createValue(getDateFormatted(rs.getDate(5)));
		} else {
		    reversionData[4] = ValueFactory.createNullValue();
		}
		tableModel.addRow(reversionData);
		// Save current Fincas in order to remove them
		// from database when there is some change in the table model
		oldReversions.add(rs.getString(1));
	    }
	    repaint();
	    tableModel.addTableModelListener(this);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    private DefaultTableModel setTableHeader() {
	CustomTableModel tableModel = new CustomTableModel();
	List<Field> columnasReversiones = new ArrayList<Field>();
	columnasReversiones.add(new Field("exp_id", "<html>Reversión</html>"));
	columnasReversiones.add(new Field("superficie",
		"<html>Superficie (m<sup>2</sup>)</html>"));
	columnasReversiones.add(new Field("importe_euros",
		"<html>Importe (&euro;)</html>"));
	columnasReversiones.add(new Field("importe_ptas",
		"<html>Importe (Ptas)</html>"));
	columnasReversiones.add(new Field("fecha_acta", "<html>Fecha</html>"));

	for (Field columnName : columnasReversiones) {
	    tableModel.addColumn(columnName);
	}

	return tableModel;
    }

    private String getDateFormatted(Date date) {
	SimpleDateFormat dateFormat = DateFormatNT.getDateFormat();
	return dateFormat.format(date);
    }

    private String getDoubleFormatted(Double doubleValue) {
	NumberFormat doubleFormat = DoubleFormatNT.getDisplayingFormat();
	return doubleFormat.format(doubleValue);
    }

    public void updatePMTable() {
	ArrayList<String> columnasPM = new ArrayList<String>();
	columnasPM.add(DBNames.FIELD_NUMPM_FINCAS_PM);

	try {
	    DefaultTableModel tableModel;
	    tableModel = new DefaultTableModel();
	    for (String columnName : columnasPM) {
		tableModel.addColumn(columnName);
	    }
	    pm.setModel(tableModel);
	    pm.setEnabled(false);
	    Value[] pmData = new Value[3];
	    PreparedStatement statement;
	    String query = "SELECT " + DBNames.FIELD_NUMPM_FINCAS_PM + " "
		    + "FROM " + DBNames.PM_SCHEMA + "."
		    + DBNames.TABLE_FINCAS_PM + " " + "WHERE "
		    + DBNames.FIELD_IDFINCA_FINCAS_PM + " = '" + getIDFinca()
		    + "';";
	    statement = DBSession.getCurrentSession().getJavaConnection()
		    .prepareStatement(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    if (rs.next()) {
		if (afectado_pm.getItemCount() > 1) {
		    afectado_pm.setSelectedIndex(1);
		}
	    }
	    rs.beforeFirst();
	    while (rs.next()) {
		pmData[0] = ValueFactory.createValue(rs.getString(1));
		tableModel.addRow(pmData);
	    }
	    repaint();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public boolean saveRecord() throws StopWriterVisitorException {
	saveExpropiationsTable();
	return super.saveRecord();
    }

    private void saveExpropiationsTable() {
	PreparedStatement statement;
	String query = null;
	String superficie;
	String cultivo;

	// First, we remove old Expropiations on this Finca
	try {
	    query = "DELETE FROM " + DBNames.SCHEMA_DATA + "."
		    + DBNames.TABLE_EXPROPIACIONES + " " + "WHERE "
		    + DBNames.FIELD_ID_FINCA_EXPROPIACIONES + " = '"
		    + getIDFinca() + "';";
	    statement = DBSession.getCurrentSession().getJavaConnection()
		    .prepareStatement(query);
	    statement.execute();
	} catch (SQLException e) {
	    e.printStackTrace();
	}

	// Now, we save into database current state of JTable
	for (int i = 0; i < expropiaciones.getRowCount(); i++) {
	    try {
		if (expropiaciones.getModel().getValueAt(i, 0) != null) {
		    superficie = expropiaciones.getModel().getValueAt(i, 0)
			    .toString();
		    if (superficie.contains(",")) {
			superficie = superficie.replace(",", ".");
		    }
		} else {
		    superficie = null;
		}
		if (expropiaciones.getModel().getValueAt(i, 1) != null) {
		    cultivo = expropiaciones.getModel().getValueAt(i, 1)
			    .toString();
		} else {
		    cultivo = null;
		}
		query = "INSERT INTO " + DBNames.SCHEMA_DATA + "."
			+ DBNames.TABLE_EXPROPIACIONES + " " + "VALUES ('"
			+ getIDFinca() + "',";
		if (superficie != null) {
		    query = query + " '" + superficie + "',";
		} else {
		    query = query + " null,";
		}
		if (cultivo != null) {
		    String cultivoID = getIDCultivo(cultivo);

		    query = query + " '" + cultivoID + "');";
		} else {
		    query = query + " null );";
		}
		statement = DBSession.getCurrentSession().getJavaConnection()
			.prepareStatement(query);
		statement.execute();
	    } catch (SQLException e) {
		e.printStackTrace();
		continue;
	    }
	}

    }

    private String getIDCultivo(String cultivo) {
	String cultivoID = null;
	PreparedStatement statement;
	String query = "SELECT " + DBNames.FIELD_ID_CULTIVO_CULTIVOS + " "
		+ "FROM " + DBNames.SCHEMA_DATA + "." + DBNames.TABLE_CULTIVOS
		+ " " + "WHERE " + DBNames.FIELD_DESCRIPCION_CULTIVOS + " = "
		+ "'" + cultivo + "';";
	try {
	    statement = DBSession.getCurrentSession().getJavaConnection()
		    .prepareCall(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    rs.next();
	    cultivoID = rs.getString(1);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return cultivoID;
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
	updateJTables();
    }

    public String getSQLQuery(String queryID) {
	if (queryID.equalsIgnoreCase("EXPROPIACIONES")) {
	    return "select * from " + "'" + DBNames.TABLE_EXPROPIACIONES + "'"
		    + "where " + DBNames.FIELD_IDFINCA + " = " + "'"
		    + getIDFinca() + "'" + ";";
	}
	return null;
    }

    @Override
    public String getBasicName() {
	return TABLENAME;
    }

    @Override
    protected String getSchema() {
	return DBNames.SCHEMA_DATA;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
	super.setChangedValues(true);
	super.saveB.setEnabled(true);
    }

}
