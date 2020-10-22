package es.icarto.gvsig.extgia.consultas;

import static es.icarto.gvsig.extgia.preferences.DBFieldNames.AREA_MANTENIMIENTO;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.BASE_CONTRATISTA;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.TRAMO;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.toedter.calendar.JDateChooser;

import es.icarto.gvsig.commons.queries.Component;
import es.icarto.gvsig.commons.queries.CustomiceDialog;
import es.icarto.gvsig.commons.queries.Utils;
import es.icarto.gvsig.commons.queries.ValidatableForm;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.icarto.gvsig.siga.PreferencesPage;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class ConsultasPanel extends ValidatableForm implements ActionListener, ChangeListener {

    private static final KeyValue ALL_ITEMS = new KeyValue("todos",
	    "00   TODOS   00");
    private static final KeyValue EMPTY_ITEM = new KeyValue(" ", " ");

    private JComboBox elemento;

    private JCheckBox seleccionados;
    private JComboBox area;
    private JComboBox baseContratista;
    private JComboBox tramo;
    private JCheckBox ultimos;
    private final JDateChooser fechaInicio;
    private final JDateChooser fechaFin;
    private JRadioButton pdfRadioButton;
    private JRadioButton xlsRadioButton;
    private JButton launchButton;
    private JButton customButton;

    private ConsultasFilters<Field> consultasFilters;

    private ReportTypeListener reportTypeListener;
    private QueriesWidgetCombo queriesWidget;

    public ConsultasPanel() {

	super();
	setWindowTitle("Consultas Inventario");
	Calendar calendar = Calendar.getInstance();

	fechaInicio = (JDateChooser) formPanel
		.getComponentByName("fecha_inicio");
	fechaFin = (JDateChooser) formPanel.getComponentByName("fecha_fin");
	fechaInicio.setDate(calendar.getTime());
	fechaFin.setDate(calendar.getTime());
    }

    @Override
    protected void initWidgets() {
	super.initWidgets();
	addImageHandler("image", PreferencesPage.SIGA_LOGO);
	addChained(BASE_CONTRATISTA, AREA_MANTENIMIENTO);
	addChained(TRAMO, BASE_CONTRATISTA);
	addChained("elemento", "tipo_consulta");

	elemento = (JComboBox) getWidgets().get("elemento");

	queriesWidget = new QueriesWidgetCombo(formPanel, "tipo_consulta");
	
	seleccionados = (JCheckBox) formPanel.getComponentByName("seleccionados");
	seleccionados.setEnabled(false);
	area = (JComboBox) formPanel.getComponentByName(AREA_MANTENIMIENTO);
	baseContratista = (JComboBox) formPanel.getComponentByName(BASE_CONTRATISTA);
	tramo = (JComboBox) formPanel.getComponentByName(TRAMO);
	
	ultimos = (JCheckBox) formPanel.getComponentByName("ultimos");
	
	//ultimos.setEnabled(false);
	
	pdfRadioButton = (JRadioButton) formPanel.getComponentByName("pdf");
	pdfRadioButton.setSelected(true);

	xlsRadioButton = (JRadioButton) formPanel.getComponentByName("excel");

	ButtonGroup group = new ButtonGroup();
	group.add(pdfRadioButton);
	group.add(xlsRadioButton);

	launchButton = (JButton) formPanel.getComponentByName("launch_button");
	customButton = (JButton) formPanel.getComponentByName("custom_button");

    }

    @Override
    protected void setListeners() {
	super.setListeners();
	reportTypeListener = new ReportTypeListener();
	elemento.addActionListener(reportTypeListener);
	queriesWidget.addActionListener(reportTypeListener);
	seleccionados.addChangeListener(this);
	ultimos.addChangeListener(this);
	launchButton.addActionListener(this);
	customButton.addActionListener(this);
	customButton.setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	consultasFilters = new ConsultasFilters<Field>(
		getFilterValue(AREA_MANTENIMIENTO),
		getFilterValue(BASE_CONTRATISTA), getFilterValue(TRAMO),
		fechaInicio.getDate(), fechaFin.getDate(), seleccionados.isSelected(),
		getSelectedRecordsFromElement(), ultimos.isSelected());

	KeyValue selElement = (KeyValue) elemento.getSelectedItem();
	KeyValue selTipoConsulta = queriesWidget.getQuery();

	if (!isCheckingOK(selElement, selTipoConsulta)) {
	    JOptionPane.showMessageDialog(null, PluginServices.getText(this,
		    "elementAndTypeUnselected_msg"));
	    return;
	}

	if (e.getSource().equals(customButton)) {
	    CustomiceDialog<Field> customiceDialog = customize(selElement.getKey(), selTipoConsulta);
	    int status = customiceDialog.open();
        if (status == CustomiceDialog.CANCEL) {
            return;
        }
        consultasFilters.setQueryType("CUSTOM");
        consultasFilters.setFields(customiceDialog.getFields());
        consultasFilters.setOrderBy(customiceDialog.getOrderBy());
	}

	Component todos = null;
	if (elemento.getSelectedItem().equals(ALL_ITEMS)) {
	    todos = new Composite(consultasFilters, selTipoConsulta,
		    pdfRadioButton.isSelected());
	    ((Composite) todos).add(getElements(selTipoConsulta.toString()));
	} else {
	    String[] element = { selElement.getKey(), selElement.getValue() };
	    todos = new Leaf(element, consultasFilters, selTipoConsulta,
		    pdfRadioButton.isSelected());
	}

	if (!todos.setOutputPath(null)) {
	    return;
	}

	try {
	    PluginServices.getMDIManager().setWaitCursor();
	    todos.generateReportFile();
	} finally {
	    PluginServices.getMDIManager().restoreCursor();
	}
	todos.finalActions();

    }
    
    private CustomiceDialog<Field> customize(String selElementKey, KeyValue selTipoConsulta) {
        CustomiceDialog<Field> customiceDialog = new CustomiceDialog<Field>();
        
        URL resource = getClass().getClassLoader().getResource("columns.properties");

        final List<String> reserved = Elements.valueOf(selElementKey).ignoredColumnsInReports;
        List<Field> columns = Utils.getFields(resource.getPath(), "audasa_extgia", selElementKey.toLowerCase(), reserved);

        for (Field f : columns) {
            f.setKey("el." + f.getKey());
        }

        if (selElementKey.equalsIgnoreCase("senhalizacion_vertical")) {
            final List<String> reservedColumns = Arrays.asList(new String[] { "gid", "the_geom", "geom", "municipio" });
            List<Field> columns2 = Utils.getFields(resource.getPath(), "audasa_extgia", selElementKey.toLowerCase() + "_senhales", reservedColumns);
            for (Field f : columns2) {
                f.setKey("se." + f.getKey());
            }
            popToDestination(columns2, "se.id_senhal_vertical", customiceDialog);
            columns.addAll(columns2);
        } else {
            String elementId = ConsultasFieldNames.getElementId(selElementKey); // Se puede sacar del Enum
            popToDestination(columns, "el." + elementId, customiceDialog);
        }

        customiceDialog.addSourceElements(columns);

        if (selTipoConsulta.equals("Trabajos")) {
        List<Field> columns2 = Utils.getFields(resource.getPath(),
            "audasa_extgia", selElementKey.toLowerCase()
                + "_trabajos");
        setAsFirstItem(columns2, "id_trabajo");

        customiceDialog.clearDestinationListModel();
        customiceDialog.addDestinationElements(columns2);
        }
        if (selTipoConsulta.equals("Inspecciones")) {
        List<Field> columns2 = Utils.getFields(resource.getPath(), "audasa_extgia", 
        selElementKey.toLowerCase() + "_reconocimientos", reserved);
        setAsFirstItem(columns2, "n_inspeccion");
        customiceDialog.clearDestinationListModel();
        customiceDialog.addDestinationElements(columns2);
        }

        return customiceDialog;
    }

    // TODO. This method should be in CustomiceDialog and not here
    private void popToDestination(List<Field> fields, String key,
	    CustomiceDialog<Field> customiceDialog) {
	Iterator<Field> iterator = fields.iterator();
	Field firstItem = null;
	while (iterator.hasNext()) {
	    Field next = iterator.next();
	    if (next.getKey().equals(key)) {
		firstItem = new Field(key, next.getLongName());
		iterator.remove();
		break;
	    }
	}
	if (firstItem == null) {
	    NotificationManager.addWarning("La tabla no tiene el campo:" + key);
	    return;
	}

	ArrayList<Field> destist = new ArrayList<Field>();
	destist.add(firstItem);
	customiceDialog.addDestinationElements(destist);

    }

    private Field setAsFirstItem(List<Field> columns2, String key) {
	Iterator<Field> iterator = columns2.iterator();
	Field firstField = null;
	while (iterator.hasNext()) {
	    Field next = iterator.next();
	    if (next.getKey().equals(key)) {
		firstField = new Field("sub." + key, next.getLongName());
		iterator.remove();
	    } else {
		next.setKey("sub." + next.getKey());
	    }
	}
	if (firstField == null) {
	    NotificationManager
		    .addWarning("La tabla no tiene el campo n_inspeccion");
	    return firstField;
	}
	columns2.add(0, firstField);
	return firstField;
    }

    private boolean isCheckingOK(KeyValue selElemento, KeyValue selTipoConsulta) {
	if (!selElemento.equals(EMPTY_ITEM)
		&& !selTipoConsulta.equals(EMPTY_ITEM)) {
	    return true;
	} else {
	    return false;
	}
    }

    private ArrayList<String[]> getElements(String tipoConsulta) {
	ArrayList<String[]> elements = new ArrayList<String[]>();
	PreparedStatement statement;
	String query;
	if (tipoConsulta.equals("Características")) {
	    query = "SELECT id, item FROM audasa_extgia_dominios.elemento "
		    + "WHERE id_fieldname IS NOT NULL GROUP BY id, item";
	} else {
	    query = "SELECT id, item FROM audasa_extgia_dominios.elemento "
		    + "WHERE  id_fieldname IS NOT NULL AND " + tipoConsulta
		    + " = " + "true GROUP BY id, item";
	}
	try {
	    Connection connection = DBSession.getCurrentSession()
		    .getJavaConnection();
	    statement = connection.prepareStatement(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    while (rs.next()) {
		String[] element = new String[2];
		element[0] = rs.getString(1);
		element[1] = rs.getString(2);
		elements.add(element);
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return elements;
    }

    private KeyValue getFilterValue(String cmpName) {
	KeyValue kv = null;
	JComboBox cmp = (JComboBox) getWidgets().get(cmpName);
	if (!cmp.getSelectedItem().toString().equals(" ")) {
	    kv = ((KeyValue) cmp.getSelectedItem());
	}
	return kv;
    }

    private class ReportTypeListener implements ActionListener {

	private boolean caracSelected() {
	    return !queriesWidget
		    .isQueryIdSelected(QueriesWidgetCombo.TRABAJOS_AGRUPADOS);
	}

	private boolean todosNoSelected() {
	    Object selectedItem = elemento.getSelectedItem();
	    return (selectedItem != null) && (!selectedItem.equals(ALL_ITEMS))
		    && (!selectedItem.equals(EMPTY_ITEM));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    customButton.setEnabled(caracSelected() && todosNoSelected());
	    if (todosNoSelected()) {
	        KeyValue selElement = (KeyValue) elemento.getSelectedItem();
	        if (seleccionadosCheckBoxHasToBeEnabled(selElement.getKey())) {
	        seleccionados.setEnabled(true);    
	        } else {
	        seleccionados.setEnabled(false);
	        }
	    } else {
	        seleccionados.setEnabled(false);
	    }
	}
    }

    @Override
    protected String getBasicName() {
	return "consultas_inventario";
    }
    
    
    private boolean seleccionadosCheckBoxHasToBeEnabled(String element) {
    IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();
    TOCLayerManager toc = new TOCLayerManager();
    FLyrVect layer = toc.getLayerByName(element);
    if (layer == null) {
        return false;
    }
    int recordsSelected = 0;
    try {
        recordsSelected = layer.getRecordset().getSelection().cardinality();
    } catch (ReadDriverException e) {
        e.printStackTrace();
    }
    if (iWindow instanceof View && recordsSelected >= 1)  {
    return true;
    }
    return false;
    }
    
    public ArrayList<String> getSelectedRecordsFromElement() {
    ArrayList<String> selectedElementsID = new ArrayList<String>();
    if (seleccionados.isEnabled()) {
    TOCLayerManager toc = new TOCLayerManager();
    FLyrVect layer = toc.getLayerByName(((KeyValue)elemento.getSelectedItem()).getKey());
    try {
        String idFieldName = Elements.valueOf(layer.getName()).pk;
        SelectableDataSource recordset = layer.getRecordset();
        int idFieldNameIndex = recordset.getFieldIndexByName(idFieldName);
        for (int i = 0; i < recordset.getRowCount(); i++) {
            if (recordset.isSelected(i)) {
            Value value = recordset.getFieldValue(i, idFieldNameIndex);
            selectedElementsID.add(value.toString());
            }
        }   
    } catch (ReadDriverException e1) {
        e1.printStackTrace();
    }
    }
    return selectedElementsID;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
    if (seleccionados.isSelected()) {
    area.setEnabled(false);
    baseContratista.setEnabled(false);
    tramo.setEnabled(false);
    } else {
    area.setEnabled(true);
    baseContratista.setEnabled(true);
    tramo.setEnabled(true);
    }
    
    if (ultimos.isSelected()) {
    fechaInicio.setEnabled(false);
    fechaFin.setEnabled(false);
    }else {
    fechaInicio.setEnabled(true);
    fechaFin.setEnabled(true);
    }
    
    }
}
