package es.icarto.gvsig.extgia.consultas;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import es.icarto.gvsig.commons.db.ConnectionWrapper;
import es.icarto.gvsig.commons.queries.CSVReport;
import es.icarto.gvsig.commons.testutils.Drivers;
import es.icarto.gvsig.commons.testutils.TestProperties;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.agregados.TrabajosAgregadosReportQueries;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.extgia.utils.SqlUtils;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class TestConsultas {

    private static ConsultasFilters<Field> mockFilters;

    private static final SimpleDateFormat sdf = new SimpleDateFormat(
	    "dd/MM/yyyy");

    @BeforeClass
    public static void doSetupBeforeClass() {

	try {
	    Date firstDate = sdf.parse("01/01/1980");
	    Date lastDate = sdf.parse("01/07/2013");

	    mockFilters = new ConsultasFilters<Field>(
		    new KeyValue("1", "Norte"), new KeyValue("1", "Norte"),
		    new KeyValue("1", "AP-9"), firstDate, lastDate);

	} catch (ParseException e1) {
	    e1.printStackTrace();
	}

	try {
	    Drivers.initgvSIGDrivers(TestProperties.driversPath);
	    DBSession.createConnection("localhost", 5434, "audasa_test", null,
		    "postgres", "postgres");

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    protected String getSchema() {
	return "audasa_extgia";
    }

    @Test
    public void testCaracteristicasPDFReportsQueries() throws SQLException {

	for (int i = 0; i < Elements.values().length; i++) {
	    if (ConsultasFieldNames
		    .getPDFCaracteristicasFieldNames(Elements
			    .values()[i].toString()) != null) {
		Statement st = DBSession.getCurrentSession()
			.getJavaConnection().createStatement();
		String query = CaracteristicasQueries
			.getPDFCaracteristicasQuery(
				Elements.values()[i].toString(),
				mockFilters);
		ResultSet rs = st.executeQuery(query);
		assertTrue(Elements.values()[i].toString(),
			rs != null);
	    }
	}
    }

    @Test
    public void testCaracteristicasPDFReportsQueriesWithNullValues()
	    throws SQLException {
	Date firstDate;
	try {
	    firstDate = sdf.parse("01/01/1980");
	    Date lastDate = sdf.parse("01/07/2013");

	    mockFilters = new ConsultasFilters<Field>(null, null, null,
		    firstDate, lastDate);

	    testCaracteristicasPDFReportsQueries();
	} catch (ParseException e) {
	    e.printStackTrace();
	}
    }

    @Test
    public void testTrabajosReportsQueries() throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {
	    if (SqlUtils.elementHasType(
		    Elements.values()[i].toString(), "Trabajos")
		    && !Elements.values()[i].toString().equals(
			    "Firme")) {
		Statement st = DBSession.getCurrentSession()
			.getJavaConnection().createStatement();
		String query = "SELECT "
			+ ConsultasFieldNames
			.getTrabajosFieldNames(ConsultasFieldNames
				.getElementId(Elements
					.values()[i].toString()))
					+ " FROM " + getSchema() + "."
					+ Elements.values()[i].toString()
					+ "_trabajos;";
		ResultSet rs = st.executeQuery(query);
		assertTrue(rs != null);
	    }
	}
    }

    @Test
    public void testTrabajosReportsQueriesWithLocationFilters()
	    throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {
	    if (SqlUtils.elementHasType(
		    Elements.values()[i].toString(), "Trabajos")
		    && !Elements.values()[i].toString().equals(
			    "Firme")) {
		Statement st = DBSession.getCurrentSession()
			.getJavaConnection().createStatement();
		String query = "SELECT "
			+ ConsultasFieldNames
			.getTrabajosFieldNames(ConsultasFieldNames
				.getElementId(Elements
					.values()[i].toString()))
					+ " FROM "
					+ getSchema()
					+ "."
					+ Elements.values()[i].toString()
					+ "_trabajos"
					+ " WHERE "
					+ ConsultasFieldNames
					.getElementId(Elements.values()[i]
						.toString())
						+ " IN (SELECT "
						+ ConsultasFieldNames
						.getElementId(Elements.values()[i]
							.toString()) + " FROM " + getSchema()
							+ "." + Elements.values()[i].toString()
							+ mockFilters.getWhereClauseByLocationWidgets() + ");";
		ResultSet rs = st.executeQuery(query);
		assertTrue(rs != null);
	    }
	}
    }

    @Test
    public void testTrabajosAgregadosReportsQueries() throws SQLException {
	String[] elements = { "Taludes", "Isletas" };
	TrabajosAgregadosReportQueries agregadosReportQueries = new TrabajosAgregadosReportQueries(
		elements[0], mockFilters);
	ResultSet rs;
	Statement st = DBSession.getCurrentSession().getJavaConnection()
		.createStatement();

	String query = agregadosReportQueries.getDesbroceManualQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getDesbroceManualSumQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getDesbroceMecanicoQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getDesbroceMecanicoSumQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getDesbroceRetroaranhaQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getDesbroceRetroaranhaSumQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getDesbroceTotalSumQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getHerbicidadQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getHerbicidaSumQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getSiegaMecanicaIsletasQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getSiegaMecanicaIsletasSumQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getSiegaMecanicaMedianaQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getSiegaMecanicaMedianaSumQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getSiegaTotalSumQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getVegetacionQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);

	query = agregadosReportQueries.getVegetacionSumQuery()
		+ mockFilters.getWhereClauseFiltersForAgregados();
	rs = st.executeQuery(query);
	assertTrue(rs != null);
    }

    @Test
    public void testTrabajosAgregadosReportsQueriesWithNullValues()
	    throws SQLException {

	Date firstDate;
	try {
	    firstDate = sdf.parse("01/01/1980");
	    Date lastDate = sdf.parse("01/07/2013");

	    mockFilters = new ConsultasFilters<Field>(null, null, null,
		    firstDate, lastDate);

	    testTrabajosAgregadosReportsQueries();
	} catch (ParseException e) {
	    e.printStackTrace();
	}
    }

    @Test
    public void testReconocimientosReportsQueries() throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {
	    if (SqlUtils.elementHasType(
		    Elements.values()[i].toString(),
		    "Inspecciones")
		    && !Elements.values()[i].toString().equals(
			    "Firme")) {
		Statement st = DBSession.getCurrentSession()
			.getJavaConnection().createStatement();
		String fields = ConsultasFieldNames
			.getReconocimientosFieldNames(Elements
				.values()[i].toString());
		if (!ConsultasFieldNames
			.hasIndiceFieldOnReconocimientos(Elements
				.values()[i].toString())) {
		    fields = ConsultasFieldNames
			    .getReconocimientosFieldNames(Elements
				    .values()[i].toString());
		}
		String query = "SELECT " + fields + " FROM " + getSchema()
			+ "." + Elements.values()[i].toString()
			+ "_reconocimientos;";
		ResultSet rs = st.executeQuery(query);
		assertTrue(rs != null);
	    }
	}
    }

    @Test
    public void testReconocimientosReportsQueriesWithLocationFilters()
	    throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {
	    if (SqlUtils.elementHasType(
		    Elements.values()[i].toString(),
		    "Inspecciones")
		    && !Elements.values()[i].toString().equals(
			    "Firme")) {
		Statement st = DBSession.getCurrentSession()
			.getJavaConnection().createStatement();
		String fields = ConsultasFieldNames
			.getReconocimientosFieldNames(Elements
				.values()[i].toString());
		if (!ConsultasFieldNames
			.hasIndiceFieldOnReconocimientos(Elements
				.values()[i].toString())) {
		    fields = ConsultasFieldNames
			    .getReconocimientosFieldNames(Elements
				    .values()[i].toString());
		}
		String query = "SELECT "
			+ fields
			+ " FROM "
			+ getSchema()
			+ "."
			+ Elements.values()[i].toString()
			+ "_reconocimientos"
			+ " WHERE "
			+ ConsultasFieldNames
			.getElementId(Elements.values()[i]
				.toString())
				+ " IN (SELECT "
				+ ConsultasFieldNames
				.getElementId(Elements.values()[i]
					.toString()) + " FROM " + getSchema()
					+ "." + Elements.values()[i].toString()
					+ mockFilters.getWhereClauseByLocationWidgets() + ");";
		ResultSet rs = st.executeQuery(query);
		assertTrue(rs != null);
	    }
	}
    }

    @Test
    public void testFirmeTrabajosReportQuerie() throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {
	    Statement st = DBSession.getCurrentSession().getJavaConnection()
		    .createStatement();
	    String query = "SELECT "
		    + ConsultasFieldNames
		    .getFirmeTrabajosFieldNames("id_firme") + " FROM "
		    + getSchema() + "." + "firme_trabajos;";
	    ResultSet rs = st.executeQuery(query);
	    assertTrue(rs != null);
	}
    }

    @Test
    public void testFirmeTrabajosReportQuerieWithFilters() throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {
	    Statement st = DBSession.getCurrentSession().getJavaConnection()
		    .createStatement();
	    String query = "SELECT "
		    + ConsultasFieldNames
		    .getFirmeTrabajosFieldNames("id_firme") + " FROM "
		    + getSchema() + "." + "firme_trabajos"
		    + " WHERE id_firme IN (SELECT id_firme FROM " + getSchema()
		    + ".firme" + mockFilters.getWhereClauseByLocationWidgets()
		    + ");";
	    ResultSet rs = st.executeQuery(query);
	    assertTrue(rs != null);
	}
    }

    @Test
    public void testFirmeReconocimientosReportQuerie() throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {
	    Statement st = DBSession.getCurrentSession().getJavaConnection()
		    .createStatement();
	    String query = "SELECT "
		    + ConsultasFieldNames.getReconocimientosFieldNames("Firme")
		    + " FROM " + getSchema() + "." + "firme_reconocimientos;";
	    ResultSet rs = st.executeQuery(query);
	    assertTrue(rs != null);
	}
    }

    @Test
    public void testFirmeReconocimientosReportQuerieWithFilters()
	    throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {
	    Statement st = DBSession.getCurrentSession().getJavaConnection()
		    .createStatement();
	    String query = "SELECT "
		    + ConsultasFieldNames.getReconocimientosFieldNames("Firme")
		    + " FROM " + getSchema() + "." + "firme_reconocimientos"
		    + " WHERE id_firme IN (SELECT id_firme FROM " + getSchema()
		    + ".firme" + mockFilters.getWhereClauseByLocationWidgets()
		    + ");";
	    ResultSet rs = st.executeQuery(query);
	    assertTrue(rs != null);
	}
    }

    @Test
    public void testCSVCaracteristicas() throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {

	    String query = CaracteristicasQueries.getCSVCaracteristicasQuery(
		    Elements.values()[i].toString(), mockFilters);
	    ConnectionWrapper conW = new ConnectionWrapper(DBSession
		    .getCurrentSession().getJavaConnection());
	    String mockFileDir = "/tmp/test_"
		    + Elements.values()[i].toString() + ".csv";
	    new CSVReport(mockFileDir, conW.execute(query), mockFilters);
	    File mockFile = new File(mockFileDir);
	    assertTrue(mockFile.exists());
	    mockFile.delete();
	}
    }

    @Test
    public void testCSVCaracteristicasWithNullValues() throws SQLException {
	Date firstDate;
	try {
	    firstDate = sdf.parse("01/01/1980");
	    Date lastDate = sdf.parse("01/07/2013");

	    mockFilters = new ConsultasFilters<Field>(null, null, null,
		    firstDate, lastDate);

	    testCSVCaracteristicas();
	} catch (ParseException e) {
	    e.printStackTrace();
	}
    }

    @Test
    public void testCSVCaracteristicasFirme() throws SQLException {
	String query = CaracteristicasQueries.getCSVCaracteristicasQuery(
		"Firme", mockFilters);
	String mockFileDir = "/tmp/test_" + "Firme" + ".csv";
	ConnectionWrapper conW = new ConnectionWrapper(DBSession
		.getCurrentSession().getJavaConnection());
	new CSVReport(mockFileDir, conW.execute(query), mockFilters);
	File mockFile = new File(mockFileDir);
	assertTrue(mockFile.exists());
	mockFile.delete();

    }

    @Test
    public void testCSVTrabajos() throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {
	    if (SqlUtils.elementHasType(
		    Elements.values()[i].toString(), "Trabajos")
		    && !Elements.values()[i].toString().equals(
			    "Firme")) {
		String query = "SELECT "
			+ ConsultasFieldNames
			.getTrabajosFieldNames(ConsultasFieldNames
				.getElementId(Elements
					.values()[i].toString()))
					+ " FROM " + getSchema() + "."
					+ Elements.values()[i].toString()
					+ "_trabajos;";
		String mockFileDir = "/tmp/test_"
			+ Elements.values()[i].toString() + ".csv";

		ConnectionWrapper conW = new ConnectionWrapper(DBSession
			.getCurrentSession().getJavaConnection());

		new CSVReport(mockFileDir, conW.execute(query), mockFilters);
		File mockFile = new File(mockFileDir);
		assertTrue(mockFile.exists());
		mockFile.delete();
	    }
	}
    }

    @Test
    public void testCSVReconocimientos() throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {
	    if (SqlUtils.elementHasType(
		    Elements.values()[i].toString(),
		    "Inspecciones")
		    && !Elements.values()[i].toString().equals(
			    "Firme")) {
		String fields = ConsultasFieldNames
			.getReconocimientosFieldNames(ConsultasFieldNames
				.getElementId(Elements.values()[i]
					.toString()));
		if (!ConsultasFieldNames
			.hasIndiceFieldOnReconocimientos(Elements
				.values()[i].toString())) {
		    fields = ConsultasFieldNames
			    .getReconocimientosFieldNames(Elements
				    .values()[i].toString());
		}
		String query = "SELECT " + fields + " FROM " + getSchema()
			+ "." + Elements.values()[i].toString()
			+ "_reconocimientos;";
		String mockFileDir = "/tmp/test_"
			+ Elements.values()[i].toString() + ".csv";
		ConnectionWrapper conW = new ConnectionWrapper(DBSession
			.getCurrentSession().getJavaConnection());
		new CSVReport(mockFileDir, conW.execute(query), mockFilters);
		File mockFile = new File(mockFileDir);
		assertTrue(mockFile.exists());
		mockFile.delete();
	    }
	}
    }

    @Test
    public void testCSVTrabajosFirme() throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {
	    String query = "SELECT "
		    + ConsultasFieldNames
		    .getFirmeTrabajosFieldNames("id_firme") + " FROM "
		    + getSchema() + "." + "firme_trabajos;";
	    String mockFileDir = "/tmp/test_"
		    + Elements.values()[i].toString() + ".csv";
	    ConnectionWrapper conW = new ConnectionWrapper(DBSession
		    .getCurrentSession().getJavaConnection());
	    new CSVReport(mockFileDir, conW.execute(query), mockFilters);
	    File mockFile = new File(mockFileDir);
	    assertTrue(mockFile.exists());
	    mockFile.delete();
	}
    }

    @Test
    public void testCSVReconocimientosFirme() throws SQLException {
	for (int i = 0; i < Elements.values().length; i++) {
	    String query = "SELECT "
		    + ConsultasFieldNames.getReconocimientosFieldNames("Firme")
		    + " FROM " + getSchema() + "." + "firme_reconocimientos;";
	    String mockFileDir = "/tmp/test_"
		    + Elements.values()[i].toString() + ".csv";
	    ConnectionWrapper conW = new ConnectionWrapper(DBSession
		    .getCurrentSession().getJavaConnection());
	    new CSVReport(mockFileDir, conW.execute(query), mockFilters);
	    File mockFile = new File(mockFileDir);
	    assertTrue(mockFile.exists());
	    mockFile.delete();
	}
    }

    @Test
    public void testCaracteristicasPDFReportSenhalizacionVerticalQuery()
	    throws SQLException {
	Statement st = DBSession.getCurrentSession().getJavaConnection()
		.createStatement();
	String query = CaracteristicasQueries.getPDFCaracteristicasQuery(
		Elements.Senhalizacion_Vertical.toString(),
		mockFilters);
	ResultSet rs = st.executeQuery(query);
	assertTrue(rs != null);
    }

    @Test
    public void testCaracteristicasPDFReportSenhalizacionVerticalQueryWithNullValues()
	    throws SQLException {
	Date firstDate;
	try {
	    firstDate = sdf.parse("01/01/1980");
	    Date lastDate = sdf.parse("01/07/2013");

	    mockFilters = new ConsultasFilters<Field>(null, null, null,
		    firstDate, lastDate);

	    testCaracteristicasPDFReportSenhalizacionVerticalQuery();
	} catch (ParseException e) {
	    e.printStackTrace();
	}
    }
}
