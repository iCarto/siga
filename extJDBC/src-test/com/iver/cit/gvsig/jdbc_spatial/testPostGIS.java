package com.iver.cit.gvsig.jdbc_spatial;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.NumericValue;
import com.iver.cit.gvsig.fmap.drivers.ConnectionFactory;
import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.IConnection;
import com.iver.cit.gvsig.fmap.drivers.jdbc.postgis.PostGisDriver;
import com.iver.andami.TestConstants;

public class testPostGIS extends TestCase {

	PostGisDriver driver = new PostGisDriver();
	ResultSet rsGood = null;

	protected void setUp() throws Exception {
		super.setUp();
		String dbtable = "provin"; // OJO CON LAS MAYUSCULAS!!!!!!!

		IConnection conn = null;
		System.out.println("Creating JDBC connection...");
		Class.forName("org.postgresql.Driver");
		conn = ConnectionFactory.createConnection(TestConstants.dburl, TestConstants.dbuser, TestConstants.dbpass);

		((ConnectionJDBC)conn).getConnection().setAutoCommit(false);

		DBLayerDefinition lyrDef = new DBLayerDefinition();
		lyrDef.setName(dbtable);
		lyrDef.setTableName(dbtable);
		lyrDef.setWhereClause("");
		String[] fields = {"nom_provin", "gid"};
		lyrDef.setFieldNames(fields);
		lyrDef.setFieldGeometry("the_geom");
		lyrDef.setFieldID("gid");

		Statement st = ((ConnectionJDBC)conn).getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		rsGood = st.executeQuery("SELECT NOM_PROVIN, GID FROM " + dbtable + " ORDER BY GID");
		driver.setData(conn, lyrDef);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		driver.close();
	}

	/*
	 * Test method for
	 * 'com.iver.cit.gvsig.fmap.drivers.jdbc.postgis.PostGisDriver.getFieldValue(long,
	 * int)'
	 */
	public void testGetFieldValue() throws SQLException, ReadDriverException {
		int pos1 = 1;
		int pos2 = 30;
		int pos3 = 15;


		for (int i=0; i < 40; i++)
		{
			rsGood.absolute(i+1);
			System.out.println("GOOD:" + rsGood.getInt(2) + " " + rsGood.getString(1));

			String aux = driver.getFieldValue(i, 0).toString();
			int id = ((NumericValue) driver.getFieldValue(i, 1)).intValue();
			System.out.println(id + " " + aux);
		}
		rsGood.absolute(pos1);
		String str2 = driver.getFieldValue(pos1-1, 0).toString();
		String str1 = rsGood.getString(1);

		System.out.println(str1 + " - " + str2);

		assertEquals(str1, str2);


		rsGood.absolute(pos2);
		str2 = driver.getFieldValue(pos2-1, 0).toString();
		str1 = rsGood.getString(1);


		int id1 = rsGood.getInt(2);
		int id2 = ((NumericValue) driver.getFieldValue(pos2-1, 1)).intValue();
		System.out.println(id1 + " " + str1 + " - " + id2 + " " + str2);
		assertEquals(id1, id2);


		rsGood.absolute(pos3);
		str2 = driver.getFieldValue(pos3-1, 0).toString();
		str1 = rsGood.getString(1);

		System.out.println(str1 + " - " + str2);

		assertEquals(str1, str2);
	}
}
