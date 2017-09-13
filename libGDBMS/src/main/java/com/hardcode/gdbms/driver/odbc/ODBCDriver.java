/*
 * Created on 16-oct-2004
 */
package com.hardcode.gdbms.driver.odbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Properties;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.data.db.JDBCSupport;
import com.hardcode.gdbms.engine.data.driver.AlphanumericDBDriver;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueWriter;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.drivers.TableDefinition;
import com.iver.cit.gvsig.fmap.edition.IWriteable;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.edition.writers.JdbcWriter;
import com.iver.utiles.NumberUtilities;


/**
 * MySQL driver
 *
 * @author Fernando González Cortés
 */
public class ODBCDriver implements AlphanumericDBDriver, IWriteable {
    private static Exception driverException;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat timeFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    private static ValueWriter vWriter = ValueWriter.internalValueWriter;

    static {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
        } catch (Exception ex) {
            driverException = ex;
        }
    }

    private JDBCSupport jdbcSupport;
    private JdbcWriter jdbcWriter = new JdbcWriter();

    /**
     * DOCUMENT ME!
     *
     * @param host DOCUMENT ME!
     * @param port DOCUMENT ME!
     * @param dbName DOCUMENT ME!
     * @param user DOCUMENT ME!
     * @param password DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException
     * @throws RuntimeException DOCUMENT ME!
     *
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#connect(java.lang.String)
     */
    public Connection getConnection(String host, int port, String dbName,
        String user, String password) throws SQLException {
        if (driverException != null) {
            throw new RuntimeException(driverException);
        }

        String connectionString = "jdbc:odbc:" + dbName;

        if ((user != null) && (!user.equalsIgnoreCase(""))) {
            connectionString += (";UID=" + user + ";PWD=" + password);
        }
        Properties prop = new Properties();
        // TODO: Pasar como parámetro el charset... aunque lo he probado y no va bien con cirílico
//        prop.put("charSet", "ISO-8859-5");
        if (user != null)
        	prop.put("user", user);
        if (password != null)
        	prop.put("password", password);        
        return DriverManager.getConnection(connectionString, prop);
    }

    /**
     * @see com.hardcode.driverManager.Driver#getName()
     */
    public String getName() {
        return "ODBC";
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#executeSQL(java.sql.Connection,
     *      java.lang.String)
     */
    public void open(Connection con, String sql) throws SQLException {
        jdbcSupport = JDBCSupport.newJDBCSupport(con, sql);

        jdbcWriter.setCreateTable(false);
        jdbcWriter.setWriteAll(false);
        Statement st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        ResultSet res = st.executeQuery(sql);

        jdbcWriter.initialize(con, res);

    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getFieldCount() throws ReadDriverException {
        return jdbcSupport.getFieldCount();
    }

    /**
     * DOCUMENT ME!
     *
     * @param fieldId DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getFieldName(int fieldId) throws ReadDriverException {
        return jdbcSupport.getFieldName(fieldId);
    }

    /**
     * DOCUMENT ME!
     *
     * @param i DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getFieldType(int i) throws ReadDriverException {
        return jdbcSupport.getFieldType(i);
    }

    /**
     * DOCUMENT ME!
     *
     * @param rowIndex DOCUMENT ME!
     * @param fieldId DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Value getFieldValue(long rowIndex, int fieldId)
        throws ReadDriverException {
        return jdbcSupport.getFieldValue(rowIndex, fieldId);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getRowCount() throws ReadDriverException {
        return jdbcSupport.getRowCount();
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#close()
     */
    public void close() throws SQLException {
        jdbcSupport.close();
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DriverCommons#getDriverProperties()
     */
    public HashMap getDriverProperties() {
        return null;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DriverCommons#setDataSourceFactory(com.hardcode.gdbms.engine.data.DataSourceFactory)
     */
    public void setDataSourceFactory(DataSourceFactory dsf) {
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#execute(java.sql.Connection,
     *      java.lang.String, com.hardcode.gdbms.engine.data.HasProperties)
     */
    public void execute(Connection con, String sql) throws SQLException {
        JDBCSupport.execute(con, sql);
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getInternalTableName(java.lang.String)
     */
    public String getInternalTableName(String tableName) {
        return tableName;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(long)
     */
    public String getStatementString(long i) {
        return vWriter.getStatementString(i);
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(int,
     *      int)
     */
    public String getStatementString(int i, int sqlType) {
        return vWriter.getStatementString(i, sqlType);
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(double,
     *      int)
     */
    public String getStatementString(double d, int sqlType) {
        return vWriter.getStatementString(d, sqlType);
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(java.lang.String,
     *      int)
     */
    public String getStatementString(String str, int sqlType) {
        return vWriter.getStatementString(str, sqlType);
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(java.sql.Date)
     */
    public String getStatementString(Date d) {
        return dateFormat.format(d);
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(java.sql.Time)
     */
    public String getStatementString(Time t) {
        return timeFormat.format(t);
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(java.sql.Timestamp)
     */
    public String getStatementString(Timestamp ts) {
        return timeFormat.format(ts);
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(byte[])
     */
    public String getStatementString(byte[] binary) {
        return "x" + vWriter.getStatementString(binary);
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(boolean)
     */
    public String getStatementString(boolean b) {
        return vWriter.getStatementString(b);
    }

    /**
     * @see com.hardcode.gdbms.engine.values.ValueWriter#getNullStatementString()
     */
    public String getNullStatementString() {
        return "null";
    }
	public int getFieldWidth(int i) throws ReadDriverException {
		return jdbcSupport.getFieldWidth(i);
	}

	public IWriter getWriter() {
		return jdbcWriter;
	}

	public ITableDefinition getTableDefinition() throws ReadDriverException {
		TableDefinition tableDef = new TableDefinition();
		tableDef.setFieldsDesc(getFieldsDescription());
		return tableDef;
	}

	/*azabala
	TODO Codigo repetido entre ODBCDriver y AbstractJDBCDriver.
	Igual hay que ponerlo en una clase auxiliar comun
	*
	*/
	private FieldDescription[] getFieldsDescription() throws ReadDriverException{
		int numFields = getFieldCount();
		FieldDescription[] fieldsDescrip = new FieldDescription[numFields];
		for (int i = 0; i < numFields; i++) {
			fieldsDescrip[i] = new FieldDescription();
			int type = getFieldType(i);
			fieldsDescrip[i].setFieldType(type);
			fieldsDescrip[i].setFieldName(getFieldName(i));
			fieldsDescrip[i].setFieldLength(getFieldWidth(i));
			if (NumberUtilities.isNumeric(type))
			{
				if (!NumberUtilities.isNumericInteger(type))
					// TODO: If there is a lost in precision, this should be changed.
					fieldsDescrip[i].setFieldDecimalCount(6);
			}
			else
				fieldsDescrip[i].setFieldDecimalCount(0);
			// TODO: ¿DEFAULTVALUE?
			// fieldsDescrip[i].setDefaultValue(get)
		}
		return fieldsDescrip;
	}
	
	public String getDefaultPort() {
		return "";
	}

}

// [eiel-gestion-conexiones]
