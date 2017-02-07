package es.icarto.gvsig.extgex.queries;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.WindowInfo;

import es.icarto.gvsig.commons.db.ConnectionWrapper;
import es.icarto.gvsig.commons.gui.BasicAbstractWindow;
import es.icarto.gvsig.commons.queries.CustomiceDialog;
import es.icarto.gvsig.commons.queries.FinalActions;
import es.icarto.gvsig.commons.queries.QueriesWidget;
import es.icarto.gvsig.commons.queries.Utils;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgex.forms.expropiations.FormExpropiations;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class QueriesPanel extends BasicAbstractWindow implements ActionListener {

    private static final Logger logger = Logger.getLogger(QueriesPanel.class);

    private static final String DEFAULT_FILTER = "--TODOS--";

    public static final String ID_RUNQUERIES = "runQueriesButton";
    private JButton runQueriesB;

    public static final String ID_CUSTOMQUERIES = "customQueriesButton";
    private JButton customQueriesB;

    private final String ID_TRAMOCB = "tramo";
    private JComboBox tramo;

    private final String ID_UCCB = "uc";
    private JComboBox uc;

    private final String ID_AYUNTAMIENTOCB = "ayuntamiento";
    private JComboBox ayuntamiento;

    private final String ID_PARROQUIA_SUBTRAMOCB = "parroquia_subtramo";
    private JComboBox parroquia_subtramo;

    private final DBSession dbs;

    // Filters
    String tramoSelected = DEFAULT_FILTER;
    String ucSelected = DEFAULT_FILTER;
    String ayuntamientoSelected = DEFAULT_FILTER;
    String parroquiaSelected = null;

    private QueriesWidget queriesWidget;
    private QueriesOuputWidget queriesOuputWidget;

    public QueriesPanel() {
	super();
	setWindowTitle("Consultas");
	setWindowInfoProperties(WindowInfo.MODELESSDIALOG);

	dbs = DBSession.getCurrentSession();
	initWidgets();
	initListeners();
    }

    private void initWidgets() {
	runQueriesB = (JButton) formPanel.getComponentByName(ID_RUNQUERIES);
	runQueriesB.addActionListener(this);
	customQueriesB = (JButton) formPanel
		.getComponentByName(ID_CUSTOMQUERIES);
	customQueriesB.addActionListener(this);
	customQueriesB.setEnabled(false);

	initFilterWidgets();

	queriesOuputWidget = new QueriesOuputWidget(formPanel, "pdf", "excel");
	queriesWidget = new QueriesWidgetCB(formPanel, "tipo_consulta");
    }

    private void initFilterWidgets() {
	tramo = (JComboBox) formPanel.getComponentByName(ID_TRAMOCB);
	tramo.addItem(new String(DEFAULT_FILTER));
	uc = (JComboBox) formPanel.getComponentByName(ID_UCCB);
	uc.addItem(new String(DEFAULT_FILTER));
	ayuntamiento = (JComboBox) formPanel
		.getComponentByName(ID_AYUNTAMIENTOCB);
	ayuntamiento.addItem(new String(DEFAULT_FILTER));
	parroquia_subtramo = (JComboBox) formPanel
		.getComponentByName(ID_PARROQUIA_SUBTRAMOCB);
	parroquia_subtramo.setEnabled(false);

	try {
	    String[] order = new String[1];
	    order[0] = DBNames.FIELD_IDTRAMO;
	    String[][] tramos = dbs.getTable(DBNames.TABLE_TRAMOS,
		    DBNames.SCHEMA_DATA, order, false);
	    for (int i = 0; i < tramos.length; i++) {
		tramo.addItem(tramos[i][1]);
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    private void initListeners() {
	tramo.addActionListener(new TramoListener());
	uc.addActionListener(new UCListener());
	ayuntamiento.addActionListener(new AyuntamientoListener());
	parroquia_subtramo.addActionListener(new ParroquiaListener());
    }

    private final class TramoListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    tramoSelected = (String) tramo.getSelectedItem();
	    updateUCDependingOnTramo();
	}
    }

    private final class UCListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    ucSelected = (String) uc.getSelectedItem();
	    if (ucSelected != null) {
		updateAyuntamientoDependingOnUC();
	    }
	}
    }

    private final class AyuntamientoListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    ayuntamientoSelected = (String) ayuntamiento.getSelectedItem();
	    if (ayuntamientoSelected != null) {
		updateParroquiaDependingOnAyuntamientoAndUC();
	    }
	}
    }

    private final class ParroquiaListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    if (!parroquia_subtramo.isEnabled()) {
		parroquiaSelected = null;
	    } else {
		parroquiaSelected = (String) parroquia_subtramo
			.getSelectedItem();
	    }
	}
    }

    private String getTramoId() throws SQLException {
	String whereSQL = DBNames.FIELD_NOMBRETRAMO_TRAMOS + " = " + "'"
		+ tramoSelected + "'";
	String tramoId = getIDFromCB(DBNames.FIELD_IDTRAMO,
		DBNames.TABLE_TRAMOS, whereSQL);
	return tramoId;
    }

    private String getUcId() throws SQLException {
	String whereSQL = DBNames.FIELD_NOMBREUC_UC + " = " + "'" + ucSelected
		+ "'";
	String uc = getIDFromCB(DBNames.FIELD_IDUC, DBNames.TABLE_UC, whereSQL);
	return uc;
    }

    private String getAyuntamientoId() throws SQLException {
	String whereSQL = DBNames.FIELD_IDUC + " = " + "'" + getUcId() + "'"
		+ " and " + DBNames.FIELD_NOMBREAYUNTAMIENTO_AYUNTAMIENTO
		+ " = " + "'" + ayuntamientoSelected + "'";
	String ayuntamiento = getIDFromCB(DBNames.FIELD_IDAYUNTAMIENTO,
		DBNames.TABLE_AYUNTAMIENTOS, whereSQL);
	return ayuntamiento;
    }

    private String getIDFromCB(String fieldID, String tablename,
	    String whereClause) throws SQLException {
	Connection con = dbs.getJavaConnection();
	String query = "SELECT " + fieldID + " FROM " + DBNames.SCHEMA_DATA
		+ "." + tablename + " WHERE " + whereClause;
	Statement st = con.createStatement();
	ResultSet resultSet = st.executeQuery(query);
	resultSet.first();
	return resultSet.getString(1);
    }

    private void updateUCDependingOnTramo() {

	if (tramoSelected.compareToIgnoreCase(DEFAULT_FILTER) == 0) {
	    uc.removeAllItems();
	    uc.addItem(new String(DEFAULT_FILTER));
	} else {
	    try {
		String[] order = new String[1];
		order[0] = DBNames.FIELD_IDUC;
		String tramoSelectedId = getTramoId();
		String whereClause = DBNames.FIELD_IDTRAMO + " = " + "'"
			+ tramoSelectedId + "'";
		String[][] ucs = dbs.getTable(DBNames.TABLE_UC,
			DBNames.SCHEMA_DATA, whereClause, order, false);
		uc.removeAllItems();
		uc.addItem(new String(DEFAULT_FILTER));
		for (int i = 0; i < ucs.length; i++) {
		    uc.addItem(ucs[i][2]);
		}
	    } catch (SQLException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    private void updateAyuntamientoDependingOnUC() {

	if (ucSelected.compareToIgnoreCase(DEFAULT_FILTER) == 0) {
	    ayuntamiento.removeAllItems();
	    ayuntamiento.addItem(new String(DEFAULT_FILTER));
	} else {
	    try {
		String[] order = new String[1];
		order[0] = DBNames.FIELD_IDAYUNTAMIENTO;
		String ucSelectedId = getUcId();
		String whereClause = DBNames.FIELD_IDUC + " = " + "'"
			+ ucSelectedId + "'";
		String[][] ayuntamientos = dbs.getTable(
			DBNames.TABLE_AYUNTAMIENTOS, DBNames.SCHEMA_DATA,
			whereClause, order, false);
		ayuntamiento.removeAllItems();
		ayuntamiento.addItem(new String(DEFAULT_FILTER));
		for (int i = 0; i < ayuntamientos.length; i++) {
		    ayuntamiento.addItem(ayuntamientos[i][2]);
		}
	    } catch (SQLException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    private void updateParroquiaDependingOnAyuntamientoAndUC() {

	if (ayuntamientoSelected.compareToIgnoreCase(DEFAULT_FILTER) == 0) {
	    parroquia_subtramo.removeAllItems();
	    parroquia_subtramo.setEnabled(false);
	} else {
	    try {
		String[] order = new String[1];
		order[0] = DBNames.FIELD_IDPARROQUIA;
		String ucSelectedId = getUcId();
		String ayuntamientoSelectedId = getAyuntamientoId();
		String whereClause = DBNames.FIELD_IDAYUNTAMIENTO + " = " + "'"
			+ ayuntamientoSelectedId + "'" + " and "
			+ DBNames.FIELD_IDUC + " = " + "'" + ucSelectedId + "'";
		String[][] parroquias = dbs.getTable(
			DBNames.TABLE_PARROQUIASSUBTRAMOS, DBNames.SCHEMA_DATA,
			whereClause, order, false);
		if (parroquias.length <= 0) {
		    parroquia_subtramo.setEnabled(false);
		} else {
		    parroquia_subtramo.setEnabled(true);
		    parroquia_subtramo.removeAllItems();
		    parroquia_subtramo.addItem(new String(DEFAULT_FILTER));
		    for (int i = 0; i < parroquias.length; i++) {
			parroquia_subtramo.addItem(parroquias[i][3]);
		    }
		}
	    } catch (SQLException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	try {
	    PluginServices.getMDIManager().setWaitCursor();
	    if (e.getSource() == runQueriesB) {
		executeValidations(false);
	    } else if (e.getSource() == customQueriesB) {
		executeValidations(true);
	    }
	} catch (SQLException e1) {
	    logger.error(e1.getStackTrace(), e1);
	} finally {
	    PluginServices.getMDIManager().restoreCursor();
	}

    }

    private void executeValidations(boolean customized) throws SQLException {

	QueryFilters queryFilters = new QueryFilters(getFilters());
	String queryCode = queriesWidget.getQueryId();

	String query = null;
	String queryDescription = null;
	String queryTitle = null;
	String querySubtitle = null;

	if (!queryCode.startsWith("custom")) {
	    String[] queryContents = doQuery(queryCode);
	    query = queryContents[0].replace("\n", " ");
	    queryDescription = queryContents[1];
	    queryTitle = queryContents[2];
	    querySubtitle = queryContents[3];
	}

	if (customized) {
	    CustomiceDialog<Field> customiceDialog = new CustomiceDialog<Field>();
	    URL resource = getClass().getClassLoader().getResource(
		    "columns.properties");

	    List<Field> columns = null;
	    if (queryCode.equals("custom-exp_finca")) {
		columns = Utils.getFields(resource.getPath(),
			DBNames.SCHEMA_DATA, FormExpropiations.TABLENAME);
		for (Field f : columns) {
		    if (f.getKey().equals("afectado_por_policia_margenes")) {
			f.setKey("(select count(numero_pm) > 0 from audasa_pm.fincas_pm sub where sub.id_finca = el.id_finca)");
		    } else {
			f.setKey("el." + f.getKey());
		    }
		}
		columns.add(new Field(
			"(select array_to_string(array_agg(id_reversion), ' / ') from audasa_expropiaciones.finca_reversion fr where fr.id_finca = el.id_finca)",
			"Reversiones"));
		columns.add(new Field(
			"(select count(id_reversion) from audasa_expropiaciones.finca_reversion fr where fr.id_finca = el.id_finca)",
			"Reversiones - conteo"));
		columns.add(new Field(
			"(select array_to_string(array_agg(numero_pm), ' / ') from audasa_pm.fincas_pm sub where sub.id_finca = el.id_finca)",
			"Policía de Márgenes"));
		columns.add(new Field(
			"(select count(numero_pm) from audasa_pm.fincas_pm sub where sub.id_finca = el.id_finca)",
			"Policía de Márgenes - conteo"));
		popToDestination(columns, "el.id_finca", customiceDialog);
		query = "SELECT foo FROM "
			+ DBNames.SCHEMA_DATA
			+ "."
			+ FormExpropiations.TABLENAME
			+ " AS el LEFT OUTER JOIN audasa_expropiaciones.tramos tr ON tr.id_tramo = el.tramo LEFT OUTER JOIN audasa_expropiaciones.uc uc ON uc.id_uc = el.unidad_constructiva LEFT OUTER JOIN audasa_expropiaciones.ayuntamientos ay ON (ay.id_ayuntamiento = el.ayuntamiento AND ay.id_uc = el.unidad_constructiva) LEFT OUTER JOIN audasa_expropiaciones.parroquias_subtramos pa ON (pa.id_parroquia = el.parroquia_subtramo AND pa.id_ayuntamiento = el.ayuntamiento AND pa.id_uc = el.unidad_constructiva) "
			+ getWhereClause(false);
		queryDescription = "Expropiaciones";
		queryTitle = "Listado de expropiaciones";
		querySubtitle = "";
	    } else {
		String[] tableColumns = DBSession.getCurrentSession()
			.getColumns(DBNames.SCHEMA_DATA,
				FormExpropiations.TABLENAME);
		columns = parseQuery(query, tableColumns);

	    }

	    customiceDialog.addSourceElements(columns);

	    int status = customiceDialog.open();
	    if (status == CustomiceDialog.CANCEL) {
		return;
	    }

	    queryFilters.setQueryType("CUSTOM");
	    queryFilters.setFields(customiceDialog.getFields());

	    query = buildQuery(query, customiceDialog.getFields(),
		    customiceDialog.getOrderBy());
	}
	ConnectionWrapper con = new ConnectionWrapper(DBSession
		.getCurrentSession().getJavaConnection());

	ResultTableModel result = new ResultTableModel(queryCode,
		queryDescription, queryTitle, querySubtitle, getFilters());
	result.setQueryFilters(queryFilters);
	con.execute(query, result);

	File file = queriesOuputWidget.to(result, getFilters());
	if (file != null) {
	    FinalActions finalActions = new FinalActions(
		    result.getRowCount() == 0, file);
	    finalActions.openReport();
	}

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

    private String[] doQuery(String queryCode) throws SQLException {

	String whereClause = DBNames.FIELD_CODIGO_QUERIES + " = '" + queryCode
		+ "'";
	String[][] tableContent = dbs.getTable(DBNames.TABLE_QUERIES,
		DBNames.SCHEMA_QUERIES, whereClause);

	String[] contents = new String[4];
	String query = tableContent[0][1];
	boolean hasWhere = false;
	if (tableContent[0][5].compareToIgnoreCase("SI") == 0) {
	    hasWhere = true;
	}
	// query
	contents[0] = query.replaceAll("\\[\\[WHERE\\]\\]",
		getWhereClause(hasWhere));
	// description
	contents[1] = tableContent[0][2];
	// title
	contents[2] = tableContent[0][3];
	// subtitle
	contents[3] = tableContent[0][4];
	return contents;
    }

    private String getWhereClause(boolean hasWhere) throws SQLException {
	String whereC;
	if (!hasWhere) {
	    whereC = " WHERE ";
	} else {
	    whereC = " AND ";
	}
	if (tramoSelected.compareToIgnoreCase(DEFAULT_FILTER) != 0) {
	    whereC = whereC + DBNames.FIELD_TRAMO_FINCAS + " = " + "'"
		    + getTramoId() + "'";
	}
	if (ucSelected.compareToIgnoreCase(DEFAULT_FILTER) != 0) {
	    whereC = whereC + " AND " + DBNames.FIELD_UC_FINCAS + " = " + "'"
		    + getUcId() + "'";
	}
	if (ayuntamientoSelected.compareToIgnoreCase(DEFAULT_FILTER) != 0) {
	    whereC = whereC + " AND " + DBNames.FIELD_AYUNTAMIENTO_FINCAS
		    + " = " + "'" + getAyuntamientoId() + "'";
	}
	if (whereC.equalsIgnoreCase(" WHERE ")) {
	    whereC = ""; // has no combobox selected
	}
	if (whereC.equalsIgnoreCase(" AND ")) {
	    whereC = "AND 1=1";
	}
	return whereC;
    }

    private String buildFields(List<Field> fields, String select) {
	for (Field field : fields) {
	    if (field.getKey().equals("el.tramo")) {
		select += "tr.nombre_tramo AS  \"Tramo\", ";
	    } else if (field.getKey().equals("el.unidad_constructiva")) {
		select += "uc.nombre_uc AS  \"Unidad constructiva\", ";
	    } else if (field.getKey().equals("el.ayuntamiento")) {
		select += "ay.nombre_ayuntamiento AS  \"Ayuntamiento\", ";
	    } else if (field.getKey().equals("el.parroquia_subtramo")) {
		select += "pa.nombre_parroquia AS  \"Parroquia / Subtramo\", ";
	    } else {
		select = select
			+ field.getKey()
			+ String.format(" AS \"%s\"", field.getLongName()
				.replace("\"", "'")) + ", ";
	    }
	}
	return select.substring(0, select.length() - 2);
    }

    private String buildQuery(String query, List<Field> fields,
	    List<Field> orderBy) {

	String subquery = query;

	if (fields.size() > 0) {
	    subquery = buildFields(fields, "SELECT   ")
		    + query.substring(query.indexOf(" FROM"), query.length());
	}

	if (!orderBy.isEmpty()) {

	    int indexOf = subquery.indexOf("ORDER BY ");
	    if (indexOf != -1) {
		subquery = subquery.substring(0, indexOf + 9);
	    } else {
		if (subquery.endsWith(";")) {
		    subquery = subquery.substring(0, subquery.length() - 1);
		}

		subquery = subquery + " ORDER BY ";
	    }

	    for (Field kv : orderBy) {
		subquery = subquery + kv.getKey() + ", ";
	    }
	    subquery = subquery.substring(0, subquery.length() - 2);
	}
	return subquery;
    }

    private List<Field> parseQuery(String query, String[] columns) {

	String fieldsStr = query.substring(query.indexOf("SELECT ") + 7,
		query.indexOf(" FROM"));
	String[] fields = fieldsStr.split(",");
	List<Field> fieldList = new ArrayList<Field>();

	// object returned by Arrays.asList does not implement remove operation
	List<String> columnList = new ArrayList<String>(Arrays.asList(columns));
	for (String f : fields) {
	    String[] split = f.split(" as ");
	    Field kv = new Field(split[0].trim(), split[1].trim().replace("\"",
		    ""));
	    fieldList.add(kv);
	    columnList.remove(kv.getKey());

	}
	for (String f : columnList) {
	    Field kv = new Field(f, f);
	    fieldList.add(kv);
	}
	return fieldList;

    }

    private String[] getFilters() {
	String[] filters = new String[4];
	filters[0] = tramoSelected;
	filters[1] = ucSelected;
	filters[2] = ayuntamientoSelected;
	filters[3] = parroquiaSelected;
	return filters;
    }

    @Override
    protected String getBasicName() {
	return "consultas";
    }

    @Override
    protected JButton getDefaultButton() {
	return null;
    }

    @Override
    protected Component getDefaultFocusComponent() {
	return null;
    }

}
