package es.icarto.gvsig.extgia.consultas;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import com.iver.cit.gvsig.fmap.layers.LayerFactory;

import es.icarto.gvsig.extgia.consultas.agregados.TrabajosAgregadosReportQueries;
import es.icarto.gvsig.extgia.consultas.caracteristicas.CSVCaracteristicasQueries;
import es.icarto.gvsig.extgia.consultas.caracteristicas.PDFCaracteristicasQueries;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.utils.SqlUtils;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class TestConsultas {

    private static ConsultasFilters mockFilters;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @BeforeClass
    public static void doSetupBeforeClass() {

	try {
	    Date firstDate = sdf.parse("01/01/1980");
	    Date lastDate = sdf.parse("01/07/2013");

	    mockFilters = new ConsultasFilters(
		    new KeyValue("1", "Norte"),
		    new KeyValue("1", "Norte"),
		    new KeyValue("1", "AP-9"),
		    firstDate,
		    lastDate);

	} catch (ParseException e1) {
	    e1.printStackTrace();
	}

	try {
	    initializegvSIGDrivers();
	    DBSession.createConnection("localhost", 5432, "audasa_test", null,
		    "postgres", "postgres");

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private static void initializegvSIGDrivers() throws Exception {
	final String fwAndamiDriverPath = "../_fwAndami/gvSIG/extensiones/com.iver.cit.gvsig/drivers";
	final File baseDriversPath = new File(fwAndamiDriverPath);
	if (!baseDriversPath.exists()) {
	    throw new Exception("Can't find drivers path: "
		    + fwAndamiDriverPath);
	}

	LayerFactory.setDriversPath(baseDriversPath.getAbsolutePath());
	if (LayerFactory.getDM().getDriverNames().length < 1) {
	    throw new Exception("Can't find drivers in path: "
		    + fwAndamiDriverPath);
	}
    }

    protected String getSchema() {
	return "audasa_extgia";
    }

    @Test
    public void testCaracteristicasPDFReportsQueries() throws SQLException {

	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    if (ConsultasFieldNames.getPDFCaracteristicasFieldNames(
		    DBFieldNames.Elements.values()[i].toString())!=null) {
		Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
		String query = PDFCaracteristicasQueries.getPDFCaracteristicasQuery(
			DBFieldNames.Elements.values()[i].toString(), mockFilters);
		ResultSet rs = st.executeQuery(query);
		assertTrue(rs!=null);
	    }
	}
    }

    @Test
    public void testCaracteristicasPDFReportsQueriesWithNullValues() throws SQLException {
	Date firstDate;
	try {
	    firstDate = sdf.parse("01/01/1980");
	    Date lastDate = sdf.parse("01/07/2013");

	    mockFilters = new ConsultasFilters(
		    null,
		    null,
		    null,
		    firstDate,
		    lastDate);

	    testCaracteristicasPDFReportsQueries();
	}catch (ParseException e) {
	    e.printStackTrace();
	}
    }

    @Test
    public void testTrabajosReportsQueries() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    if (SqlUtils.elementHasType(DBFieldNames.Elements.values()[i].toString(), "Trabajos") &&
		    !DBFieldNames.Elements.values()[i].toString().equals("Firme")) {
		Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
		String query = "SELECT " +
			ConsultasFieldNames.getTrabajosFieldNames(
				ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString())) +
				" FROM " + getSchema() + "." + DBFieldNames.Elements.values()[i].toString() +
				"_trabajos;";
		ResultSet rs = st.executeQuery(query);
		assertTrue(rs!=null);
	    }
	}
    }

    @Test
    public void testTrabajosReportsQueriesWithLocationFilters() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    if (SqlUtils.elementHasType(DBFieldNames.Elements.values()[i].toString(), "Trabajos") &&
		    !DBFieldNames.Elements.values()[i].toString().equals("Firme")) {
		Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
		String query = "SELECT " +
			ConsultasFieldNames.getTrabajosFieldNames(
				ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString())) +
				" FROM " + getSchema() + "." + DBFieldNames.Elements.values()[i].toString() +
				"_trabajos" +
				" WHERE " + ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString()) +
				" IN (SELECT " +
				ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString()) +
				" FROM " + getSchema() + "." + DBFieldNames.Elements.values()[i].toString() +
				mockFilters.getWhereClauseByLocationWidgets(false) + ");";
		ResultSet rs = st.executeQuery(query);
		assertTrue(rs!=null);
	    }
	}
    }

    @Test
    public void testTrabajosAgregadosReportsQueries() throws SQLException {
	String[] elements = {"Taludes", "Isletas"};
	TrabajosAgregadosReportQueries agregadosReportQueries = new TrabajosAgregadosReportQueries(elements[0]);
	ResultSet rs;
	Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();

	String query = agregadosReportQueries.getDesbroceManualQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], false);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getDesbroceManualSumQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], true);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getDesbroceMecanicoQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], false);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getDesbroceMecanicoSumQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], true);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getDesbroceRetroaranhaQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], false);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getDesbroceRetroaranhaSumQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], true);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getDesbroceTotalSumQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], true);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getHerbicidadQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], false);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getHerbicidaSumQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], true);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getSiegaMecanicaIsletasQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], false);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getSiegaMecanicaIsletasSumQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], true);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getSiegaMecanicaMedianaQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], false);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getSiegaMecanicaMedianaSumQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], true);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getSiegaTotalSumQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], true);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getVegeracionQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], false);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);

	query = agregadosReportQueries.getVegeracionSumQuery() +
		mockFilters.getWhereClauseFiltersForAgregados(elements[0], true);
	rs = st.executeQuery(query);
	assertTrue(rs!=null);
    }

    @Test
    public void testTrabajosAgregadosReportsQueriesWithNullValues() throws SQLException {

	Date firstDate;
	try {
	    firstDate = sdf.parse("01/01/1980");
	    Date lastDate = sdf.parse("01/07/2013");

	    mockFilters = new ConsultasFilters(
		    null,
		    null,
		    null,
		    firstDate,
		    lastDate);

	    testTrabajosAgregadosReportsQueries();
	} catch (ParseException e) {
	    e.printStackTrace();
	}
    }

    @Test
    public void testReconocimientosReportsQueries() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    if (SqlUtils.elementHasType(DBFieldNames.Elements.values()[i].toString(), "Inspecciones") &&
		    !DBFieldNames.Elements.values()[i].toString().equals("Firme")) {
		Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
		String fields = ConsultasFieldNames.getReconocimientosFieldNames(
			ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString()));
		if (!ConsultasFieldNames.hasIndiceFieldOnReconocimientos(DBFieldNames.Elements.values()[i].toString())) {
		    fields = ConsultasFieldNames.getReconocimientosFieldNamesWithoutIndice(
			    ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString()));
		}
		String query = "SELECT " + fields +
			" FROM " + getSchema() + "." + DBFieldNames.Elements.values()[i].toString() +
			"_reconocimientos;";
		ResultSet rs = st.executeQuery(query);
		assertTrue(rs!=null);
	    }
	}
    }

    @Test
    public void testReconocimientosReportsQueriesWithLocationFilters() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    if (SqlUtils.elementHasType(DBFieldNames.Elements.values()[i].toString(), "Inspecciones") &&
		    !DBFieldNames.Elements.values()[i].toString().equals("Firme")) {
		Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
		String fields = ConsultasFieldNames.getReconocimientosFieldNames(
			ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString()));
		if (!ConsultasFieldNames.hasIndiceFieldOnReconocimientos(DBFieldNames.Elements.values()[i].toString())) {
		    fields = ConsultasFieldNames.getReconocimientosFieldNamesWithoutIndice(
			    ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString()));
		}
		String query = "SELECT " + fields +
			" FROM " + getSchema() + "." + DBFieldNames.Elements.values()[i].toString() +
			"_reconocimientos" +
			" WHERE " + ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString()) +
			" IN (SELECT " +
			ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString()) +
			" FROM " + getSchema() + "." + DBFieldNames.Elements.values()[i].toString() +
			mockFilters.getWhereClauseByLocationWidgets(false) + ");";
		ResultSet rs = st.executeQuery(query);
		assertTrue(rs!=null);
	    }
	}
    }

    @Test
    public void testFirmeTrabajosReportQuerie() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
	    String query = "SELECT " +
		    ConsultasFieldNames.getFirmeTrabajosFieldNames("id_firme") +
		    " FROM " + getSchema() + "." + "firme_trabajos;";
	    ResultSet rs = st.executeQuery(query);
	    assertTrue(rs!=null);
	}
    }

    @Test
    public void testFirmeTrabajosReportQuerieWithFilters() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
	    String query = "SELECT " +
		    ConsultasFieldNames.getFirmeTrabajosFieldNames("id_firme") +
		    " FROM " + getSchema() + "." + "firme_trabajos" +
		    " WHERE id_firme IN (SELECT id_firme FROM " + getSchema() +
		    ".firme" + mockFilters.getWhereClauseByLocationWidgets(false) +
		    ");";
	    ResultSet rs = st.executeQuery(query);
	    assertTrue(rs!=null);
	}
    }

    @Test
    public void testFirmeReconocimientosReportQuerie() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
	    String query = "SELECT " +
		    ConsultasFieldNames.getFirmeReconocimientosFieldNames("id_firme") +
		    " FROM " + getSchema() + "." + "firme_reconocimientos;";
	    ResultSet rs = st.executeQuery(query);
	    assertTrue(rs!=null);
	}
    }

    @Test
    public void testFirmeReconocimientosReportQuerieWithFilters() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
	    String query = "SELECT " +
		    ConsultasFieldNames.getFirmeReconocimientosFieldNames("id_firme") +
		    " FROM " + getSchema() + "." + "firme_reconocimientos" +
		    " WHERE id_firme IN (SELECT id_firme FROM " + getSchema() +
		    ".firme" + mockFilters.getWhereClauseByLocationWidgets(false) +
		    ");";
	    ResultSet rs = st.executeQuery(query);
	    assertTrue(rs!=null);
	}
    }

    @Test
    public void testCSVCaracteristicas() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    if (DBFieldNames.Elements.values()[i].toString().equals("Firme")) {
		return;
	    }else {
		if (ConsultasFieldNames.getCSVCaracteristicasFieldNames(
			DBFieldNames.Elements.values()[i].toString())!=null) {
		    Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
		    String query = CSVCaracteristicasQueries.getCSVCaracteristicasQuery(
			    DBFieldNames.Elements.values()[i].toString(), mockFilters);
		    ResultSet rs = st.executeQuery(query);
		    ResultSetMetaData rsMetaData = rs.getMetaData();
		    String mockFileDir = "/tmp/test_" + DBFieldNames.Elements.values()[i].toString() + ".csv";
		    new CSVReport(mockFileDir, rsMetaData, rs, mockFilters);
		    File mockFile = new File(mockFileDir);
		    assertTrue(mockFile.exists());
		    mockFile.delete();
		}
	    }
	}
    }

    @Test
    public void testCSVCaracteristicasWithNullValues() throws SQLException {
	Date firstDate;
	try {
	    firstDate = sdf.parse("01/01/1980");
	    Date lastDate = sdf.parse("01/07/2013");

	    mockFilters = new ConsultasFilters(
		    null,
		    null,
		    null,
		    firstDate,
		    lastDate);

	    testCSVCaracteristicas();
	}catch (ParseException e) {
	    e.printStackTrace();
	}
    }

    @Test
    public void testCSVCaracteristicasFirme() throws SQLException {

	Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
	String query = CSVCaracteristicasQueries.getCSVCaracteristicasQuery("Firme", mockFilters);
	ResultSet rs = st.executeQuery(query);
	ResultSetMetaData rsMetaData = rs.getMetaData();
	String mockFileDir = "/tmp/test_" + "Firme" + ".csv";
	new CSVReport(mockFileDir, rsMetaData, rs, mockFilters);
	File mockFile = new File(mockFileDir);
	assertTrue(mockFile.exists());
	mockFile.delete();

    }

    @Test
    public void testCSVTrabajos() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    if (SqlUtils.elementHasType(DBFieldNames.Elements.values()[i].toString(), "Trabajos") &&
		    !DBFieldNames.Elements.values()[i].toString().equals("Firme")) {
		Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
		String query = "SELECT " +
			ConsultasFieldNames.getTrabajosFieldNames(
				ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString())) +
				" FROM " + getSchema() + "." + DBFieldNames.Elements.values()[i].toString() +
				"_trabajos;";
		ResultSet rs = st.executeQuery(query);
		ResultSetMetaData rsMetaData = rs.getMetaData();
		String mockFileDir = "/tmp/test_" + DBFieldNames.Elements.values()[i].toString() + ".csv";
		new CSVReport(mockFileDir, rsMetaData, rs, mockFilters);
		File mockFile = new File(mockFileDir);
		assertTrue(mockFile.exists());
		mockFile.delete();
	    }
	}
    }

    @Test
    public void testCSVReconocimientos() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    if (SqlUtils.elementHasType(DBFieldNames.Elements.values()[i].toString(), "Inspecciones") &&
		    !DBFieldNames.Elements.values()[i].toString().equals("Firme")) {
		Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
		String fields = ConsultasFieldNames.getReconocimientosFieldNames(ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString()));
		if (!ConsultasFieldNames.hasIndiceFieldOnReconocimientos(DBFieldNames.Elements.values()[i].toString())){
		    fields = ConsultasFieldNames.getReconocimientosFieldNamesWithoutIndice(ConsultasFieldNames.getElementId(DBFieldNames.Elements.values()[i].toString()));
		}
		String query = "SELECT " + fields +
			" FROM " + getSchema() + "." + DBFieldNames.Elements.values()[i].toString() +
			"_reconocimientos;";
		ResultSet rs = st.executeQuery(query);
		ResultSetMetaData rsMetaData = rs.getMetaData();
		String mockFileDir = "/tmp/test_" + DBFieldNames.Elements.values()[i].toString() + ".csv";
		new CSVReport(mockFileDir, rsMetaData, rs, mockFilters);
		File mockFile = new File(mockFileDir);
		assertTrue(mockFile.exists());
		mockFile.delete();
	    }
	}
    }

    @Test
    public void testCSVTrabajosFirme() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
	    String query = "SELECT " +
		    ConsultasFieldNames.getFirmeTrabajosFieldNames("id_firme") +
		    " FROM " + getSchema() + "." + "firme_trabajos;";
	    ResultSet rs = st.executeQuery(query);
	    ResultSetMetaData rsMetaData = rs.getMetaData();
	    String mockFileDir = "/tmp/test_" + DBFieldNames.Elements.values()[i].toString() + ".csv";
	    new CSVReport(mockFileDir, rsMetaData, rs, mockFilters);
	    File mockFile = new File(mockFileDir);
	    assertTrue(mockFile.exists());
	    mockFile.delete();
	}
    }

    @Test
    public void testCSVReconocimientosFirme() throws SQLException {
	for (int i=0; i<DBFieldNames.Elements.values().length; i++) {
	    Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
	    String query = "SELECT " +
		    ConsultasFieldNames.getFirmeReconocimientosFieldNames("id_firme") +
		    " FROM " + getSchema() + "." + "firme_reconocimientos;";
	    ResultSet rs = st.executeQuery(query);
	    ResultSetMetaData rsMetaData = rs.getMetaData();
	    String mockFileDir = "/tmp/test_" + DBFieldNames.Elements.values()[i].toString() + ".csv";
	    new CSVReport(mockFileDir, rsMetaData, rs, mockFilters);
	    File mockFile = new File(mockFileDir);
	    assertTrue(mockFile.exists());
	    mockFile.delete();
	}
    }

    @Test
    public void testCaracteristicasPDFReportSenhalizacionVerticalQuery() throws SQLException {
	Statement st = DBSession.getCurrentSession().getJavaConnection().createStatement();
	String query = PDFCaracteristicasQueries.getSenhalizacionVerticalQuery(mockFilters);
	ResultSet rs = st.executeQuery(query);
	assertTrue(rs!=null);
    }

    @Test
    public void testCaracteristicasPDFReportSenhalizacionVerticalQueryWithNullValues() throws SQLException {
	Date firstDate;
	try {
	    firstDate = sdf.parse("01/01/1980");
	    Date lastDate = sdf.parse("01/07/2013");

	    mockFilters = new ConsultasFilters(
		    null,
		    null,
		    null,
		    firstDate,
		    lastDate);

	    testCaracteristicasPDFReportSenhalizacionVerticalQuery();
	}catch (ParseException e) {
	    e.printStackTrace();
	}
    }
}
