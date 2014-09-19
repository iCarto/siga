package es.icarto.gvsig.extgia.consultas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;
import com.toedter.calendar.JDateChooser;

import es.icarto.gvsig.audasacommons.PreferencesPage;
import es.icarto.gvsig.commons.queries.Component;
import es.icarto.gvsig.commons.queries.CustomiceDialog;
import es.icarto.gvsig.commons.queries.Field;
import es.icarto.gvsig.commons.queries.Utils;
import es.icarto.gvsig.extgia.utils.SqlUtils;
import es.icarto.gvsig.navtableforms.ormlite.ORMLite;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.DomainValues;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.icarto.gvsig.navtableforms.utils.AbeilleParser;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class ConsultasPanel extends JPanel implements IWindow, ActionListener {

    public static String ABEILLE_FILENAME = "forms/consultas_inventario.jfrm";

    private static final KeyValue ALL_ITEMS = new KeyValue("todos", "-TODOS-");
    private static final KeyValue EMPTY_ITEM = new KeyValue(" ", " ");

    private final FormPanel form;
    private final ORMLite ormLite;

    protected WindowInfo viewInfo = null;
    private String title;
    private final int width = 430;
    private final int height = 330;

    private JComboBox elemento;
    private JComboBox areaMantenimiento;
    private JComboBox baseContratista;
    private JComboBox tramo;
    private final JDateChooser fechaInicio;
    private final JDateChooser fechaFin;
    private JRadioButton pdfRadioButton;
    private JRadioButton xlsRadioButton;
    private JButton launchButton;

    private ConsultasFilters<Field> consultasFilters;

    private JButton customButton;

    private final ReportTypeListener reportTypeListener;
    private QueriesWidgetCombo queriesWidget;

    public ConsultasPanel() {

	InputStream stream = getClass().getClassLoader().getResourceAsStream(
		ABEILLE_FILENAME);
	FormPanel result = null;

	try {
	    result = new FormPanel(stream);
	} catch (FormException e) {
	    e.printStackTrace();
	}
	this.add(result);

	Calendar calendar = Calendar.getInstance();
	// Setting name of JTextFieldDateEditors since NTForms gets an error if
	// it is null
	fechaInicio = (JDateChooser) result.getComponentByName("fecha_inicio");
	fechaInicio.getDateEditor().getUiComponent().setName("fecha_inicio_TF");
	fechaFin = (JDateChooser) result.getComponentByName("fecha_fin");
	fechaFin.getDateEditor().getUiComponent().setName("fecha_fin_TF");
	fechaInicio.setDate(calendar.getTime());
	fechaFin.setDate(calendar.getTime());

	this.form = result;
	ormLite = new ORMLite(getClass().getClassLoader()
		.getResource("rules/consultas_metadata.xml").getPath());

	reportTypeListener = new ReportTypeListener();

	initWidgets();
    }

    private void initWidgets() {
	ImageComponent image = (ImageComponent) form
		.getComponentByName("image");
	ImageIcon icon = new ImageIcon(PreferencesPage.AUDASA_ICON);
	image.setIcon(icon);

	HashMap<String, JComponent> widgetsVector = AbeilleParser
		.getWidgetsFromContainer(form);

	elemento = (JComboBox) widgetsVector.get("elemento");
	elemento.addItem(EMPTY_ITEM);
	elemento.addItem(ALL_ITEMS);
	setComboBoxValues(elemento);
	elemento.addActionListener(reportTypeListener);
	queriesWidget = new QueriesWidgetCombo(form, "tipo_consulta", ormLite);

	areaMantenimiento = (JComboBox) widgetsVector.get("area_mantenimiento");
	setComboBoxValues(areaMantenimiento);
	baseContratista = (JComboBox) widgetsVector.get("base_contratista");
	setComboBoxValues(baseContratista);
	tramo = (JComboBox) widgetsVector.get("tramo");
	setComboBoxValues(tramo);

	queriesWidget.addActionListener(new TipoConsultaListener());
	areaMantenimiento
		.addActionListener(new UpdateBaseContratistaListener());
	baseContratista.addActionListener(new UpdateTramoListener());

	pdfRadioButton = (JRadioButton) form.getComponentByName("pdf");
	pdfRadioButton.setSelected(true);

	xlsRadioButton = (JRadioButton) form.getComponentByName("excel");

	ButtonGroup group = new ButtonGroup();
	group.add(pdfRadioButton);
	group.add(xlsRadioButton);

	launchButton = (JButton) form.getComponentByName("launch_button");
	launchButton.addActionListener(this);

	customButton = (JButton) form.getComponentByName("custom_button");
	customButton.addActionListener(this);
	customButton.setEnabled(false);
    }

    private void setComboBoxValues(JComboBox cb) {
	String comboBoxName = cb.getName();
	DomainValues dv = ormLite.getAppDomain().getDomainValuesForComponent(
		comboBoxName);

	for (KeyValue kv : dv.getValues()) {
	    cb.addItem(kv);
	}
    }

    @Override
    public WindowInfo getWindowInfo() {
	viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG);
	viewInfo.setTitle(title);
	viewInfo.setWidth(width);
	viewInfo.setHeight(height);
	return viewInfo;
    }

    @Override
    public Object getWindowProfile() {
	return WindowInfo.DIALOG_PROFILE;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	consultasFilters = new ConsultasFilters<Field>(getFilterAreaValue(),
		getFilterBaseContratistaValue(), getFilterTramoValue(),
		fechaInicio.getDate(), fechaFin.getDate());

	KeyValue selElement = (KeyValue) elemento.getSelectedItem();
	KeyValue selTipoConsulta = queriesWidget.getQuery();

	if (!isCheckingOK(selElement, selTipoConsulta)) {
	    JOptionPane.showMessageDialog(null, PluginServices.getText(this,
		    "elementAndTypeUnselected_msg"));
	    return;
	}

	if (!isCheckingTrabajosAgregadosOK(selElement, selTipoConsulta)) {
	    JOptionPane.showMessageDialog(null,
		    PluginServices.getText(this, "unavailableQuery_msg"));
	    return;
	}

	if (selElement.equals(ALL_ITEMS)
		&& !selTipoConsulta.toString().equals("CaracterÝsticas")) {
	    JOptionPane.showMessageDialog(null,
		    PluginServices.getText(this, "unavailableQuery_msg"));
	    return;
	}

	if (notAvaliableType(selElement, selTipoConsulta)) {
	    JOptionPane.showMessageDialog(null,
		    PluginServices.getText(this, "unavailableQuery_msg"));
	    return;
	}

	if (e.getSource().equals(customButton)) {
	    CustomiceDialog<Field> customiceDialog = new CustomiceDialog<Field>();

	    URL resource = getClass().getClassLoader().getResource(
		    "columns.properties");

	    List<Field> columns = Utils.getFields(resource.getPath(),
		    "audasa_extgia", selElement.getKey().toLowerCase());
	    for (Field f : columns) {
		f.setKey("el." + f.getKey());
	    }

	    if (selElement.getKey().equalsIgnoreCase("senhalizacion_vertical")) {
		List<Field> columns2 = Utils.getFields(resource.getPath(),
			"audasa_extgia", selElement.getKey().toLowerCase()
				+ "_senhales");
		for (Field f : columns2) {
		    f.setKey("se." + f.getKey());
		}
		popToDestination(columns2, "se.id_senhal_vertical",
			customiceDialog);
		columns.addAll(columns2);
	    } else {
		String elementId = ConsultasFieldNames.getElementId(selElement
			.getKey());
		popToDestination(columns, "el." + elementId, customiceDialog);
	    }

	    customiceDialog.addSourceElements(columns);

	    if (selTipoConsulta.equals("Trabajos")) {
		List<Field> columns2 = Utils.getFields(resource.getPath(),
			"audasa_extgia", selElement.getKey().toLowerCase()
				+ "_trabajos");
		setAsFirstItem(columns2, "id_trabajo");

		customiceDialog.clearDestinationListModel();
		customiceDialog.addDestinationElements(columns2);
	    }
	    if (selTipoConsulta.equals("Inspecciones")) {
		List<Field> columns2 = Utils.getFields(resource.getPath(),
			"audasa_extgia", selElement.getKey().toLowerCase()
				+ "_reconocimientos");

		setAsFirstItem(columns2, "n_inspeccion");
		customiceDialog.clearDestinationListModel();
		customiceDialog.addDestinationElements(columns2);
	    }

	    int status = customiceDialog.open();
	    if (status == CustomiceDialog.CANCEL) {
		return;
	    }
	    consultasFilters.setQueryType("CUSTOM");
	    consultasFilters.setFields(customiceDialog.getFields());
	    consultasFilters.setOrderBy(customiceDialog.getOrderBy());
	}

	Component todos = null;
	if (elemento.getSelectedItem().toString().equals("-TODOS-")) {
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

	fields.remove(0);
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

    private boolean notAvaliableType(KeyValue selElement,
	    KeyValue selTipoConsulta) {
	return !SqlUtils.elementHasType(selElement.getKey(),
		selTipoConsulta.toString());
    }

    private boolean isCheckingOK(KeyValue selElemento, KeyValue selTipoConsulta) {
	if (!selElemento.equals(EMPTY_ITEM)
		&& !selTipoConsulta.equals(EMPTY_ITEM)) {
	    return true;
	} else {
	    return false;
	}
    }

    private boolean isCheckingTrabajosAgregadosOK(KeyValue selElemento,
	    KeyValue selTipoConsulta) {
	if (selTipoConsulta.toString().equals("Trabajos Agrupados")) {
	    if (selElemento.toString().equals("Taludes")
		    || selElemento.toString().equals("Isletas")) {
		return true;
	    } else {
		return false;
	    }
	} else {
	    return true;
	}
    }

    private ArrayList<String[]> getElements(String tipoConsulta) {
	ArrayList<String[]> elements = new ArrayList<String[]>();
	PreparedStatement statement;
	String query;
	if (tipoConsulta.equals("CaracterÝsticas")) {
	    query = "SELECT id, item FROM audasa_extgia_dominios.elemento "
		    + "WHERE id_fieldname <> ' '";
	} else {
	    query = "SELECT id, item FROM audasa_extgia_dominios.elemento "
		    + "WHERE id <> 'todos' AND id <> ' ' AND " + tipoConsulta
		    + " = " + "true;";
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

    private KeyValue getFilterAreaValue() {
	KeyValue areaValue = null;
	if (!areaMantenimiento.getSelectedItem().toString().equals(" ")) {
	    areaValue = ((KeyValue) areaMantenimiento.getSelectedItem());
	}
	return areaValue;
    }

    private KeyValue getFilterBaseContratistaValue() {
	KeyValue baseValue = null;
	if (!baseContratista.getSelectedItem().toString().equals(" ")) {
	    baseValue = ((KeyValue) baseContratista.getSelectedItem());
	}
	return baseValue;
    }

    private KeyValue getFilterTramoValue() {
	KeyValue tramoValue = null;
	if (!tramo.getSelectedItem().toString().equals(" ")) {
	    tramoValue = ((KeyValue) tramo.getSelectedItem());
	}
	return tramoValue;
    }

    public class TipoConsultaListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    reportTypeListener.actionPerformed(null);
	    if (queriesWidget.getQueryId().equals(
		    QueriesWidgetCombo.CARACTERISTICAS)) {
		fechaInicio.setEnabled(false);
		fechaFin.setEnabled(false);
	    } else {
		fechaInicio.setEnabled(true);
		fechaFin.setEnabled(true);
	    }
	}
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
	}
    }

    public class UpdateBaseContratistaListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    String id = ((KeyValue) areaMantenimiento.getSelectedItem())
		    .getKey();
	    String getBaseContratistaQuery = "SELECT id, item FROM audasa_extgia_dominios.base_contratista"
		    + " WHERE id_am = " + id + ";";
	    baseContratista.removeAllItems();
	    baseContratista.addItem(new KeyValue(" ", " "));
	    if (!id.isEmpty()) {
		for (KeyValue value : SqlUtils
			.getKeyValueListFromSql(getBaseContratistaQuery)) {
		    baseContratista.addItem(value);
		}
	    }
	}
    }

    public class UpdateTramoListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    if (baseContratista.getSelectedItem() != null
		    && !baseContratista.getSelectedItem().toString()
			    .equals(" ")) {
		String id = ((KeyValue) baseContratista.getSelectedItem())
			.getKey();
		String getTramoQuery = "SELECT id, item FROM audasa_extgia_dominios.tramo"
			+ " WHERE id_bc = " + id + ";";
		tramo.removeAllItems();
		tramo.addItem(new KeyValue(" ", " "));
		if (!id.isEmpty()) {
		    for (KeyValue value : SqlUtils
			    .getKeyValueListFromSql(getTramoQuery)) {
			tramo.addItem(value);
		    }
		}
	    }
	}
    }
}
