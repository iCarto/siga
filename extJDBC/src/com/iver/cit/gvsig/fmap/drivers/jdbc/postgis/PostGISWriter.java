package com.iver.cit.gvsig.fmap.drivers.jdbc.postgis;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.WriteDriverException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.iver.cit.gvsig.exceptions.visitors.ProcessVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.ProcessWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.IConnection;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.edition.IFieldManager;
import com.iver.cit.gvsig.fmap.edition.IRowEdited;
import com.iver.cit.gvsig.fmap.edition.ISpatialWriter;
import com.iver.cit.gvsig.fmap.edition.fieldmanagers.JdbcFieldManager;
import com.iver.cit.gvsig.fmap.edition.writers.AbstractWriter;

public class PostGISWriter extends AbstractWriter implements ISpatialWriter,
IFieldManager {

	private int numRows;

	private DBLayerDefinition lyrDef;

	private IConnection conex;

	private Statement st;

	private boolean bCreateTable;

	private boolean bWriteAll;

	private PostGIS postGisSQL = new PostGIS();
	// private double flatness;

	private JdbcFieldManager fieldManager;

	/**
	 * Useful to create a layer from scratch Call setFile before using this
	 * function
	 * 
	 * @param lyrDef
	 * @throws InitializeWriterException
	 * @throws IOException
	 * @throws DriverException
	 */
	public void initialize(ITableDefinition lyrD)
	throws InitializeWriterException {
		super.initialize(lyrD);
		this.lyrDef = (DBLayerDefinition) lyrD;
		conex = lyrDef.getConnection();

		try {
			st = ((ConnectionJDBC) conex).getConnection().createStatement();

			if (bCreateTable) {
				// try {
				// st.execute("DROP TABLE " + lyrDef.getComposedTableName() +
				// ";");
				// } catch (SQLException e1) {
				// // Si no existe la tabla, no hay que borrarla.
				// }
				dropTableIfExist();

				String sqlCreate = postGisSQL.getSqlCreateSpatialTable(lyrDef,
						lyrDef.getFieldsDesc(), true);
				System.out.println("sqlCreate =" + sqlCreate);
				st.execute(sqlCreate);

				String sqlAlter = postGisSQL.getSqlAlterTable(lyrDef);
				System.out.println("sqlAlter =" + sqlAlter);
				st.execute(sqlAlter);
				// CREATE TABLE PARKS ( PARK_ID int4, PARK_NAME varchar(128),
				// PARK_DATE date, PARK_TYPE varchar(2) );
				// SELECT AddGeometryColumn('parks_db', 'parks', 'park_geom',
				// 128,
				// 'MULTIPOLYGON', 2 );

				/*
				 * BEGIN; INSERT INTO ROADS_GEOM (ID,GEOM,NAME ) VALUES
				 * (1,GeometryFromText('LINESTRING(191232 243118,191108
				 * 243242)',-1),'Jeff Rd'); INSERT INTO ROADS_GEOM (ID,GEOM,NAME )
				 * VALUES (2,GeometryFromText('LINESTRING(189141 244158,189265
				 * 244817)',-1),'Geordie Rd'); COMMIT;
				 */
				((ConnectionJDBC) conex).getConnection().commit();
			}
			((ConnectionJDBC) conex).getConnection().setAutoCommit(false);

			String schema_tablename = lyrDef.getComposedTableName();
			fieldManager = new JdbcFieldManager(((ConnectionJDBC)conex).getConnection(), schema_tablename);



		} catch (SQLException e) {
			throw new InitializeWriterException(getName(), e);
		}

	}

	public void preProcess() throws StartWriterVisitorException {
		numRows = 0;
		// ATENTION: We will transform (in PostGIS class; doubleQuote())
		// to UTF-8 strings. Then, we tell the PostgreSQL server
		// that we will use UTF-8, and it can translate
		// to its charset
		// Note: we have to translate to UTF-8 because
		// the server cannot manage UTF-16

		ResultSet rsAux;
		try {
			((ConnectionJDBC) conex).getConnection().rollback();
			alterTable();

			rsAux = st.executeQuery("SHOW server_encoding;");
			rsAux.next();
			String serverEncoding = rsAux.getString(1);
			System.out.println("Server encoding = " + serverEncoding);
			// st.execute("SET CLIENT_ENCODING TO 'UNICODE';");
			// Intentamos convertir nuestras cadenas a ese encode.
			postGisSQL.setEncoding(serverEncoding);
		} catch (SQLException e) {
			throw new StartWriterVisitorException(getName(), e);
		} catch (WriteDriverException e) {
			throw new StartWriterVisitorException(getName(), e);
		}

	}

	public void process(IRowEdited row) throws ProcessWriterVisitorException {

		String sqlInsert;
		try {
			// System.out.println("Escribiendo numReg=" + numReg + " con
			// STATUS=" + row.getStatus());
			switch (row.getStatus()) {
			case IRowEdited.STATUS_ADDED:
				IFeature feat = (IFeature) row.getLinkedRow();
				sqlInsert = postGisSQL.getSqlInsertFeature(lyrDef, feat);
				System.out.println("sql = " + sqlInsert);
				st.execute(sqlInsert);

				break;
			case IRowEdited.STATUS_MODIFIED:
				IFeature featM = (IFeature) row.getLinkedRow();
				if (bWriteAll) {
					sqlInsert = postGisSQL.getSqlInsertFeature(lyrDef, featM);
					System.out.println("sql = " + sqlInsert);
					st.execute(sqlInsert);
				} else {
					String sqlModify = postGisSQL.getSqlModifyFeature(lyrDef,
							featM);
					System.out.println("sql = " + sqlModify);
					st.execute(sqlModify);
				}
				break;
			case IRowEdited.STATUS_ORIGINAL:
				IFeature featO = (IFeature) row.getLinkedRow();
				if (bWriteAll) {
					sqlInsert = postGisSQL.getSqlInsertFeature(lyrDef, featO);
					st.execute(sqlInsert);
				}
				break;
			case IRowEdited.STATUS_DELETED:
				String sqlDelete = postGisSQL.getSqlDeleteFeature(lyrDef, row);
				System.out.println("sql = " + sqlDelete);
				st.execute(sqlDelete);

				break;
			}

			numRows++;
		} catch (ProcessVisitorException e) {
			Logger.getLogger(this.getClass()).error(getName(), e);
			// JOptionPane.showMessageDialog(null, "incorrect_geometry");
		} catch (SQLException e) {
			throw new ProcessWriterVisitorException(this.getName(), e);
		}

	}

	public void postProcess() throws StopWriterVisitorException {
		try {
			((ConnectionJDBC) conex).getConnection().commit();
		} catch (SQLException e) {
			throw new StopWriterVisitorException(getName(), e);
		}
	}

	public String getName() {
		return "PostGIS Writer";
	}

	public boolean canWriteGeometry(int gvSIGgeometryType) {
		switch (gvSIGgeometryType) {
		case FShape.POINT:
			return true;
		case FShape.LINE:
			return true;
		case FShape.POLYGON:
			return true;
		case FShape.ARC:
			return false;
		case FShape.ELLIPSE:
			return false;
		case FShape.MULTIPOINT:
			return true;
		case FShape.TEXT:
			return false;
		}
		return false;
	}

	public boolean canWriteAttribute(int sqlType) {
		switch (sqlType) {
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.INTEGER:
		case Types.BIGINT:
			return true;
		case Types.DATE:
			return true;
		case Types.BIT:
		case Types.BOOLEAN:
			return true;
		case Types.VARCHAR:
		case Types.CHAR:
		case Types.LONGVARCHAR:
			return true;

		}

		return false;
	}

	/**
	 * @return Returns the bCreateTable.
	 */
	public boolean isCreateTable() {
		return bCreateTable;
	}

	/**
	 * @param createTable
	 *            The bCreateTable to set.
	 */
	public void setCreateTable(boolean createTable) {
		bCreateTable = createTable;
	}

	public FieldDescription[] getOriginalFields() {
		return lyrDef.getFieldsDesc();
	}

	public void addField(FieldDescription fieldDesc) {
		fieldManager.addField(fieldDesc);

	}

	public FieldDescription removeField(String fieldName) {
		return fieldManager.removeField(fieldName);

	}

	public void renameField(String antName, String newName) {
		fieldManager.renameField(antName, newName);

	}

	public boolean alterTable() throws WriteDriverException {
		return fieldManager.alterTable();
	}

	public FieldDescription[] getFields() {
		return fieldManager.getFields();
	}

	public boolean canAlterTable() {
		// CHANGE FROM CARTOLAB
		// return true;
		try {
			boolean can_alter = false;
			st = ((ConnectionJDBC) conex).getConnection().createStatement();
			String sql = "SELECT current_user=(SELECT tableowner from pg_tables WHERE schemaname = '"
				+ lyrDef.getSchema()
				+ "' and tablename='"
				+ lyrDef.getTableName()
				+ "') OR (SELECT usesuper FROM pg_user WHERE usename=current_user) AS can_alter;";
			ResultSet rs = st.executeQuery(sql);
			if (rs.next()) {
				can_alter = rs.getBoolean("can_alter");
			}
			return can_alter;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		// END CHANGE FROM CARTOLAB
	}

	public boolean canSaveEdits() {
		try {
			// return !((ConnectionJDBC) conex).getConnection().isReadOnly();
			boolean can_edit = false;
	
			
			st = ((ConnectionJDBC) conex).getConnection().createStatement();
			String sql2;
			sql2 = "select * from GEOMETRY_COLUMNS WHERE F_TABLE_SCHEMA = '"
						+ lyrDef.getSchema() + "' AND F_TABLE_NAME = '" + lyrDef.getTableName()
						+ "'";

			ResultSet rs2 = st.executeQuery(sql2);
			int dim =0;
			while (rs2.next()) {
				dim = rs2.getInt("coord_dimension");
			}
			rs2.close();
			
			String sql = "SELECT has_table_privilege('"
				+ lyrDef.getComposedTableName() + "', 'insert') "
				+ "AND has_table_privilege('"
				+ lyrDef.getComposedTableName() + "', 'update') "
				+ "AND has_table_privilege('"
				+ lyrDef.getComposedTableName()
				+ "', 'delete') as can_edit;";
			ResultSet rs = st.executeQuery(sql);
			if (rs.next()) {
				can_edit = rs.getBoolean("can_edit");
			}
			rs.close();
			st.close();
			if (dim > 2)
				return false;
			
			
			return can_edit
			&& !((ConnectionJDBC) conex).getConnection().isReadOnly();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private boolean dropTableIfExist() throws SQLException {
		if (!this.existTable(lyrDef.getSchema(), lyrDef.getTableName())) {
			return false;
		}
		st = ((ConnectionJDBC) conex).getConnection().createStatement();
		st.execute("DROP TABLE " + lyrDef.getComposedTableName() + ";");
		st.close();
		return false;
	}

	private boolean existTable(String schema, String tableName)
	throws SQLException {
		boolean exists = false;
		if (schema == null || schema.equals("")) {
			schema = " current_schema()::Varchar ";
		} else {
			schema = "'" + schema + "'";
		}

		String sql = "select relname,nspname "
			+ "from pg_class inner join pg_namespace "
			+ "on relnamespace = pg_namespace.oid where "
			+ " relkind = 'r' and relname = '" + tableName
			+ "' and nspname = " + schema;

		st = ((ConnectionJDBC) conex).getConnection().createStatement();
		ResultSet rs = st.executeQuery(sql);
		if (rs.next()) {
			exists = true;
		}
		rs.close();
		st.close();

		return exists;
	}
}
