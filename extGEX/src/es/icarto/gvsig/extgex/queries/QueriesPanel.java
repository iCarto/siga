package es.icarto.gvsig.extgex.queries;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
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
import es.icarto.gvsig.commons.queries.CustomiceDialog;
import es.icarto.gvsig.commons.queries.FinalActions;
import es.icarto.gvsig.commons.queries.QueriesWidget;
import es.icarto.gvsig.commons.queries.Utils;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgex.forms.expropiations.FormExpropiations;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.extgia.consultas.ValidatableForm;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.icarto.gvsig.siga.PreferencesPage;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class QueriesPanel extends ValidatableForm implements ActionListener {

    private static final Logger logger = Logger.getLogger(QueriesPanel.class);

    
    public static final String ID_RUNQUERIES = "runQueriesButton";
    private JButton runQueriesB;

    public static final String ID_CUSTOMQUERIES = "customQueriesButton";
    private JButton customQueriesB;


    private QueriesWidget queriesWidget;
    private QueriesOuputWidget queriesOuputWidget;

    public QueriesPanel() {
	super();
	setWindowTitle("Consultas");
	setWindowInfoProperties(WindowInfo.MODELESSDIALOG);
	addImageHandler("image", PreferencesPage.SIGA_LOGO);
    }

    protected void initWidgets() {
        super.initWidgets();
        addChained("uc", "tramo");
        addChained("ayuntamiento", "uc");
        addChained("parroquia_subtramo", "ayuntamiento");
	runQueriesB = (JButton) formPanel.getComponentByName(ID_RUNQUERIES);
	runQueriesB.addActionListener(this);
	customQueriesB = (JButton) formPanel
		.getComponentByName(ID_CUSTOMQUERIES);
	customQueriesB.addActionListener(this);
	customQueriesB.setEnabled(false);

	queriesOuputWidget = new QueriesOuputWidget(formPanel, "pdf", "excel");
	queriesWidget = new QueriesWidgetCB(formPanel, "tipo_consulta");
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

	QueryFilters queryFilters = new QueryFilters(getSelectedKVForWidget("tramo"), getSelectedKVForWidget("uc"), getSelectedKVForWidget("ayuntamiento"), getSelectedKVForWidget("parroquia_subtramo"));
	String queryCode = queriesWidget.getQueryId();

	String query = null;
	String queryDescription = null;
	String queryTitle = null;
	String querySubtitle = null;

	if (!queryCode.startsWith("custom")) {
	    String[] queryContents = doQuery(queryCode, queryFilters);
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
			+ queryFilters.getWhereClauseByLocationWidgets(false);
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
		queryDescription, queryTitle, querySubtitle);
	result.setQueryFilters(queryFilters);
	con.execute(query, result);

	File file = queriesOuputWidget.to(result, queryFilters);
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

    private String[] doQuery(String queryCode, QueryFilters filters) throws SQLException {

	String whereClause = DBNames.FIELD_CODIGO_QUERIES + " = '" + queryCode
		+ "'";
	String[][] tableContent = DBSession.getCurrentSession().getTable(DBNames.TABLE_QUERIES,
		DBNames.SCHEMA_QUERIES, whereClause);

	String[] contents = new String[4];
	String query = tableContent[0][1];
	boolean hasWhere = false;
	if (tableContent[0][5].compareToIgnoreCase("SI") == 0) {
	    hasWhere = true;
	}
	// query
	contents[0] = query.replaceAll("\\[\\[WHERE\\]\\]",
		filters.getWhereClauseByLocationWidgets(hasWhere));
	// description
	contents[1] = tableContent[0][2];
	// title
	contents[2] = tableContent[0][3];
	// subtitle
	contents[3] = tableContent[0][4];
	return contents;
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

    private KeyValue getSelectedKVForWidget(String name) {
        Object widget = ((JComboBox)getWidgets().get(name)).getSelectedItem();
        return (KeyValue)widget;
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
