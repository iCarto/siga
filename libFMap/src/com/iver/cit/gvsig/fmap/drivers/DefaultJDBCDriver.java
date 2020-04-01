/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package com.iver.cit.gvsig.fmap.drivers;

import java.awt.geom.Rectangle2D;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.hardcode.driverManager.DriverEvent;
import com.hardcode.driverManager.DriverEventListener;
import com.hardcode.gdbms.driver.exceptions.BadFieldDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.driver.exceptions.ReloadDriverException;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.data.driver.ObjectDriver;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.Messages;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.db.utils.ConnectionWithParams;
import com.iver.cit.gvsig.fmap.drivers.db.utils.SingleDBConnectionManager;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.JPasswordDlg;

/**
 * Clase abstracta qu
 */
public abstract class DefaultJDBCDriver implements IVectorialJDBCDriver, ObjectDriver {
    private static Logger logger = Logger.getLogger(SelectableDataSource.class.getName());
    protected static Hashtable poolPassw = new Hashtable();

    protected IConnection conn;
    // protected String tableName;
    // protected String whereClause;
    // protected String fields;
    // protected String sqlOrig;
    protected DBLayerDefinition lyrDef = null;
    protected ResultSet rs;
    protected boolean bCursorActivo = false;
    // protected Statement st;
    protected int numReg = -1;

    protected Rectangle2D fullExtent = null;

    // protected String strFID_FieldName;
    // protected String idFID_FieldName;

    protected Hashtable hashRelate;

    protected ResultSetMetaData metaData = null;
    protected Rectangle2D workingArea;
    protected String driverClass;
    protected String userName;
    protected String dbUrl;
    protected String className;
    protected String catalogName;
    protected String schema;
    protected String tableName;
    protected String[] fields;
    protected String FIDfield;
    protected String geometryField;
    protected String whereClause;
    protected String strSRID;
    // private double flatness;

    protected String host, port, dbName, connName;

    protected ArrayList driverEventListeners = new ArrayList();

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver#getConnection()
     */
    @Override
    public IConnection getConnection() {
        return conn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver#getFields()
     */
    @Override
    public String[] getFields() {
        /*
         * StringTokenizer tokenizer = new StringTokenizer(fields, ",");
         * String[] arrayFields = new String[tokenizer.countTokens()];
         * int i=0;
         * while (tokenizer.hasMoreTokens())
         * {
         * arrayFields[i] = tokenizer.nextToken();
         * i++;
         * }
         * return arrayFields;
         */
        return lyrDef.getFieldNames();

    }

    /**
     * First, the geometry field. After, the rest of fields
     * 
     * @return
     */
    public String getTotalFields() {
        String strAux = getGeometryField(getLyrDef().getFieldGeometry());
        String[] fieldNames = getLyrDef().getFieldNames();
        for (int i = 0; i < fieldNames.length; i++) {
            strAux = strAux + ", " + fieldNames[i];
        }
        return strAux;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.VectorialDatabaseDriver#getWhereClause()
     */
    @Override
    public String getWhereClause() {
        return lyrDef.getWhereClause(); // .toUpperCase();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver#getTableName()
     */
    @Override
    public String getTableName() {
        return lyrDef.getTableName();
    }

    /**
     * @throws DriverIOException
     * @throws DriverException
     * @see com.iver.cit.gvsig.fmap.layers.ReadableVectorial#getShapeCount()
     */
    @Override
    public int getShapeCount() throws ReadDriverException {
        if (numReg == -1) {
            try {
                Statement s = ((ConnectionJDBC) conn).getConnection().createStatement();
                ResultSet r = s.executeQuery("SELECT COUNT(*) AS NUMREG FROM " + lyrDef.getTableName() + " "
                        + getCompleteWhere());
                r.next();
                numReg = r.getInt(1);
                System.err.println("numReg = " + numReg);
            } catch (SQLException e) {
                throw new ReadDriverException(getName(), e);
            }
        }

        return numReg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.VectorialDriver#getFullExtent()
     */
    @Override
    public Rectangle2D getFullExtent() throws ExpansionFileReadException, ReadDriverException {
        // Por defecto recorremos todas las geometrias.
        // Las bases de datos como PostGIS pueden y deben
        // sobreescribir este método.
        if (fullExtent == null) {
            // try
            // {
            IFeatureIterator itGeom = getFeatureIterator("SELECT " + getGeometryField(getLyrDef().getFieldGeometry())
                    + ", " + getLyrDef().getFieldID() + " FROM " + getLyrDef().getComposedTableName() + " "
                    + getCompleteWhere());
            IGeometry geom;
            int cont = 0;
            while (itGeom.hasNext()) {
                geom = itGeom.next().getGeometry();
                if (cont == 0) {
                    fullExtent = geom.getBounds2D();
                } else {
                    fullExtent.add(geom.getBounds2D());
                }
                cont++;
            }
            // }
            // catch (DriverException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }

        }
        return fullExtent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.VectorialDriver#getShapeType()
     */
    @Override
    public int getShapeType() {
        /*
         * IGeometry geom;
         * if (shapeType == -1)
         * {
         * shapeType = FShape.MULTI;
         * try {
         * geom = getShape(0);
         * if (geom != null)
         * shapeType = geom.getGeometryType();
         * } catch (IOException e) {
         * // e.printStackTrace();
         * }
         * }
         * return shapeType;
         */
        return lyrDef.getShapeType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldType(int)
     */
    @Override
    public int getFieldType(int idField) throws ReadDriverException {
        try {
            int i = idField + 2; // idField viene basado en 1, y
            // además nos saltamos el campo de geometry
            int type = metaData.getColumnType(i);
            if (type == Types.VARCHAR) {
                return Types.VARCHAR;
            }
            if (type == Types.FLOAT) {
                return Types.FLOAT;
            }
            if (type == Types.REAL) {
                return Types.FLOAT;
            }
            if (type == Types.DOUBLE) {
                return Types.DOUBLE;
            }
            if (type == Types.INTEGER) {
                return Types.INTEGER;
            }
            if (type == Types.SMALLINT) {
                return Types.SMALLINT;
            }
            if (type == Types.TINYINT) {
                return Types.TINYINT;
            }
            if (type == Types.BIGINT) {
                return Types.BIGINT;
            }
            if (type == Types.BIT) {
                return Types.BIT;
            }
            if (type == Types.DATE) {
                return Types.DATE;
            }
            if (type == Types.DECIMAL) {
                return Types.DOUBLE;
            }
            if (type == Types.NUMERIC) {
                return Types.DOUBLE;
            }
            if (type == Types.DATE) {
                return Types.DATE;
            }
            if (type == Types.TIME) {
                return Types.TIME;
            }
            if (type == Types.TIMESTAMP) {
                return Types.TIMESTAMP;
            }
            if (type == Types.NUMERIC) {
                return Types.DOUBLE;
            }
            if (type == Types.BOOLEAN) {
                return Types.BOOLEAN;
            }
            if (type == Types.CHAR) {
                return Types.CHAR;
            }

        } catch (SQLException e) {
            throw new BadFieldDriverException(getName(), e, String.valueOf(idField));
        }
        return Types.OTHER;
    }

    /**
     * Obtiene el valor que se encuentra en la fila y columna indicada
     * Esta es la implementación por defecto. Si lo del absolute
     * no va bien, como es el caso del PostGis, el driver lo
     * tiene que reimplementar
     *
     * @param rowIndex
     *            fila
     * @param fieldId
     *            columna
     *
     * @return subclase de Value con el valor del origen de datos
     *
     * @throws DriverException
     *             Si se produce un error accediendo al DataSource
     */
    @Override
    public Value getFieldValue(long rowIndex, int idField) throws ReadDriverException {
        int i = (int) (rowIndex + 1);
        int fieldId = idField + 2;
        try {
            rs.absolute(i);
            if (metaData.getColumnType(fieldId) == Types.VARCHAR) {
                String strAux = rs.getString(fieldId);
                if (strAux == null) {
                    strAux = "";
                }
                return ValueFactory.createValue(strAux);
            }
            if (metaData.getColumnType(fieldId) == Types.FLOAT) {
                return ValueFactory.createValue(rs.getFloat(fieldId));
            }
            if (metaData.getColumnType(fieldId) == Types.DOUBLE) {
                return ValueFactory.createValue(rs.getDouble(fieldId));
            }
            if (metaData.getColumnType(fieldId) == Types.INTEGER) {
                return ValueFactory.createValue(rs.getInt(fieldId));
            }
            if (metaData.getColumnType(fieldId) == Types.BIGINT) {
                return ValueFactory.createValue(rs.getLong(fieldId));
            }
            if (metaData.getColumnType(fieldId) == Types.BIT) {
                return ValueFactory.createValue(rs.getBoolean(fieldId));
            }
            if (metaData.getColumnType(fieldId) == Types.DATE) {
                return ValueFactory.createValue(rs.getDate(fieldId));
            }
        } catch (SQLException e) {
            throw new BadFieldDriverException(getName(), e, String.valueOf(fieldId));
        }
        return null;

    }

    /**
     * Obtiene el número de campos del DataSource
     *
     * @return
     *
     * @throws DriverException
     *             Si se produce algún error accediendo al
     *             DataSource
     */
    @Override
    public int getFieldCount() throws ReadDriverException {
        try {
            // Suponemos que el primer campo es el de las geometries, y no lo
            // contamos
            return rs.getMetaData().getColumnCount() - 1;
        } catch (SQLException e) {
            throw new ReadDriverException(getName(), e);
        }

    }

    /**
     * Devuelve el nombre del campo fieldId-ésimo
     *
     * @param fieldId
     *            índice del campo cuyo nombre se quiere obtener
     *
     * @return
     * @throws com.hardcode.gdbms.engine.data.driver.DriverException
     *
     * @throws DriverException
     *             Si se produce algún error accediendo al
     *             DataSource
     */
    @Override
    public String getFieldName(int fieldId) throws ReadDriverException {
        try {
            return rs.getMetaData().getColumnName(fieldId + 2);
        } catch (SQLException e) {
            throw new ReadDriverException(getName(), e);
        }
    }

    /**
     * Obtiene el número de registros del DataSource
     *
     * @return
     * @throws ReadDriverException
     *
     * @throws DriverException
     *             Si se produce algún error accediendo al
     *             DataSource
     */
    @Override
    public long getRowCount() throws ReadDriverException {
        return getShapeCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver#close()
     */
    @Override
    public void close() {
    }

    /**
     * Recorre el recordset creando una tabla Hash que usaremos para
     * relacionar el número de un registro con su identificador único.
     * Debe ser llamado en el setData justo después de crear el recorset
     * principal
     * 
     * @throws SQLException
     */
    protected void doRelateID_FID() throws DBException {
        try {
            hashRelate = new Hashtable();

            String strSQL = "SELECT " + getLyrDef().getFieldID() + " FROM " + getLyrDef().getComposedTableName() + " "
                    + getCompleteWhere() + " ORDER BY " + getLyrDef().getFieldID();
            Statement s = ((ConnectionJDBC) getConnection()).getConnection().createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet r = s.executeQuery(strSQL);
            int id = 0;
            int gid;
            int index = 0;
            while (r.next()) {
                String aux = r.getString(1);
                Value val = ValueFactory.createValue(aux);
                hashRelate.put(val, new Integer(index));
                System.out.println("ASOCIANDO CLAVE " + aux + " CON VALOR " + index);
                index++;
            }
            numReg = index;
            r.close();
            // rs.beforeFirst();
        } catch (SQLException e) {
            throw new DBException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.VectorialDatabaseDriver#getRowIndexByFID(java.lang.Object)
     */
    @Override
    public int getRowIndexByFID(IFeature FID) {
        int resul;
        // Object obj = FID.getAttribute(lyrDef.getIdFieldID());
        String theId = FID.getID();
        Value aux = ValueFactory.createValue(theId);
        // System.err.println("Mirando si existe " + aux.toString());
        if (hashRelate.containsKey(aux)) {
            Integer rowIndex = (Integer) hashRelate.get(aux);
            resul = rowIndex.intValue();
            // System.err.println("Row asociada a " + aux.toString() + ":" + resul);
            return resul;
        } else {
            return -1;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.VectorialDatabaseDriver#setXMLEntity(com.iver.utiles.XMLEntity)
     */
    @Override
    public void setXMLEntity(XMLEntity xml) throws XMLException {

        className = xml.getStringProperty("className");
        // dbUrl = xml.getStringProperty("dbURL");
        catalogName = xml.getStringProperty("catalog");
        userName = xml.getStringProperty("username");
        driverClass = xml.getStringProperty("driverclass");
        tableName = xml.getStringProperty("tablename");
        if (xml.contains("schema")) {
            schema = xml.getStringProperty("schema");
        }
        fields = xml.getStringArrayProperty("fields");
        FIDfield = xml.getStringProperty("FID");
        geometryField = xml.getStringProperty("THE_GEOM");
        whereClause = xml.getStringProperty("whereclause");
        strSRID = xml.getStringProperty("SRID");

        if (xml.contains("host")) {
            host = xml.getStringProperty("host");
            port = xml.getStringProperty("port");
            dbName = xml.getStringProperty("dbName");
            connName = xml.getStringProperty("connName");
        } else {
            // Por compatibilidad con versiones anteriores
            dbUrl = xml.getStringProperty("dbURL");
            extractParamsFromDbUrl(dbUrl);

        }

        if (xml.contains("minXworkArea")) {
            double x = xml.getDoubleProperty("minXworkArea");
            double y = xml.getDoubleProperty("minYworkArea");
            double H = xml.getDoubleProperty("HworkArea");
            double W = xml.getDoubleProperty("WworkArea");
            workingArea = new Rectangle2D.Double(x, y, W, H);
        }

        DBLayerDefinition lyrDef = new DBLayerDefinition();
        lyrDef.setCatalogName(catalogName);
        lyrDef.setSchema(schema);
        lyrDef.setTableName(tableName);
        lyrDef.setFieldGeometry(geometryField);
        lyrDef.setFieldNames(fields);
        lyrDef.setFieldID(FIDfield);
        lyrDef.setWhereClause(whereClause);
        // lyrDef.setClassToInstantiate(driverClass);
        if (workingArea != null) {
            lyrDef.setWorkingArea(workingArea);
        }

        lyrDef.setSRID_EPSG(strSRID);

        setLyrDef(lyrDef);

    }

    private void extractParamsFromDbUrl(String dbUrl2) {
        // jdbc:postgres://localhost:5431/latin1
        int iDbName = dbUrl2.lastIndexOf('/');
        dbName = dbUrl2.substring(iDbName + 1);
        int iLast2points = dbUrl2.lastIndexOf(':');
        port = dbUrl2.substring(iLast2points + 1, iDbName);
        int iHost = dbUrl2.indexOf("//");
        host = dbUrl2.substring(iHost + 2, iLast2points);
        connName = dbUrl2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver#load()
     */
    @Override
    public void load() throws ReadDriverException {
        try {
            if (driverClass != null) {
                Class.forName(driverClass);
            }

            String _drvName = getName();

            String keyPool = _drvName + "_" + host + "_" + port + "_" + dbName + "_" + userName;

            IConnection newConn = null;
            String clave = null;
            ConnectionWithParams cwp = null;

            if (poolPassw.containsKey(keyPool)) {

                clave = (String) poolPassw.get(keyPool);
                cwp = SingleDBConnectionManager.instance().getConnection(_drvName, userName, clave, connName, host,
                        port, dbName, schema, true);

            } else {

                cwp = SingleDBConnectionManager.instance().getConnection(_drvName, userName, null, connName, host,
                        port, dbName, schema, false);

                if (cwp.isConnected()) {

                    poolPassw.put(keyPool, cwp.getPw());

                } else {

                    JPasswordDlg dlg = new JPasswordDlg();
                    String strMessage = Messages.getString("conectar_jdbc");
                    String strPassword = Messages.getString("password");
                    dlg.setMessage(strMessage + " [" + _drvName + ", " + host + ", " + port + ", " + dbName + ", "
                            + userName + "]. " + strPassword + "?");
                    dlg.setVisible(true);
                    clave = dlg.getPassword();
                    if (clave == null) {
                        return;
                    }
                    poolPassw.put(keyPool, clave);

                    cwp.connect(clave);
                }
            }

            newConn = cwp.getConnection();

            DBLayerDefinition lyrDef = new DBLayerDefinition();
            if (getLyrDef() == null) {
                lyrDef.setCatalogName(catalogName);
                lyrDef.setSchema(schema);
                lyrDef.setTableName(tableName);
                lyrDef.setFieldNames(fields);
                lyrDef.setFieldID(FIDfield);
                lyrDef.setFieldGeometry(geometryField);
                lyrDef.setWhereClause(whereClause);
                // lyrDef.setClassToInstantiate(driverClass);
                if (workingArea != null) {
                    lyrDef.setWorkingArea(workingArea);
                }

                lyrDef.setSRID_EPSG(strSRID);
            } else {
                lyrDef = getLyrDef();
            }

            setData(newConn, lyrDef);
        } catch (ClassNotFoundException e) {
            // logger.debug(e);
            // DriverJdbcNotFoundExceptionType type =
            // new DriverJdbcNotFoundExceptionType();
            // type.setDriverJdbcClassName(driverClass);
            // type.setLayerName(this.getTableName());
            throw new ReadDriverException(getName(), e);
        } catch (DBException e) {
            throw new ReadDriverException(getName(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.VectorialDatabaseDriver#getXMLEntity()
     */
    @Override
    public XMLEntity getXMLEntity() {
        XMLEntity xml = new XMLEntity();
        xml.putProperty("className", this.getClass().getName());
        try {
            DatabaseMetaData metadata = ((ConnectionJDBC) getConnection()).getConnection().getMetaData();
            xml.putProperty("catalog", getLyrDef().getCatalogName());

            // TODO: NO DEBEMOS GUARDAR EL NOMBRE DE USUARIO Y CONTRASEÑA
            // AQUI. Hay que utilizar un pool de conexiones
            // y pedir al usuario que conecte a la base de datos
            // en la primera capa. En el resto, usar la conexión
            // creada con anterioridad.
            String userName = metadata.getUserName();
            int aux = userName.indexOf("@");
            if (aux != -1) {
                userName = userName.substring(0, aux);
            }
            xml.putProperty("username", userName);

            Driver drv = DriverManager.getDriver(metadata.getURL());
            // System.out.println(drv.getClass().getName());
            xml.putProperty("driverclass", drv.getClass().getName());
            xml.putProperty("schema", lyrDef.getSchema());
            xml.putProperty("tablename", getTableName());
            xml.putProperty("fields", lyrDef.getFieldNames());
            xml.putProperty("FID", lyrDef.getFieldID());
            xml.putProperty("THE_GEOM", lyrDef.getFieldGeometry());
            xml.putProperty("whereclause", getWhereClause());
            xml.putProperty("SRID", lyrDef.getSRID_EPSG());

            ConnectionWithParams cwp = SingleDBConnectionManager.instance().findConnection(getConnection());

            // FIXME:(Chema) Estos cambios los hago porque da errores as persistencia
            if (cwp != null) {
                xml.putProperty("host", cwp.getHost());
                xml.putProperty("port", cwp.getPort());
                xml.putProperty("dbName", cwp.getDb());
                xml.putProperty("connName", cwp.getName());
            } else {
                xml.putProperty("dbURL", metadata.getURL());
            }
            // Chema

            if (getWorkingArea() != null) {
                xml.putProperty("minXworkArea", getWorkingArea().getMinX());
                xml.putProperty("minYworkArea", getWorkingArea().getMinY());
                xml.putProperty("HworkArea", getWorkingArea().getHeight());
                xml.putProperty("WworkArea", getWorkingArea().getWidth());
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return xml;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver#setWorkingArea(java.awt.geom.Rectangle2D)
     */
    @Override
    public void setWorkingArea(Rectangle2D rect) {
        this.workingArea = rect;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver#getWorkingArea()
     */
    @Override
    public Rectangle2D getWorkingArea() {
        return workingArea;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hardcode.gdbms.engine.data.driver.GDBMSDriver#setDataSourceFactory(com.hardcode.gdbms.engine.data.
     * DataSourceFactory)
     */
    @Override
    public void setDataSourceFactory(DataSourceFactory arg0) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver#getLyrDef()
     */
    @Override
    public DBLayerDefinition getLyrDef() {
        if (this.conn != null) {
            if (lyrDef.getConnection() != this.conn) {
                lyrDef.setConnection(this.conn);
            }
        }
        return lyrDef;
    }

    /**
     * @param lyrDef
     *            The lyrDef to set.
     */
    public void setLyrDef(DBLayerDefinition lyrDef) {
        this.lyrDef = lyrDef;
    }

    abstract public String getSqlTotal();

    /**
     * @return Returns the completeWhere. WITHOUT order by clause!!
     */
    abstract public String getCompleteWhere();

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.VectorialDriver#reLoad()
     */
    @Override
    public void reload() throws ReloadDriverException {
        try {
            if ((conn == null) || (((ConnectionJDBC) conn).getConnection().isClosed())) {
                this.load();
            }

            ((ConnectionJDBC) conn).getConnection().commit();

            setData(conn, lyrDef);
        } catch (SQLException e) {
            throw new ReloadDriverException(getName(), e);
        } catch (ReadDriverException e) {
            throw new ReloadDriverException(getName(), e);
        } catch (DBException e) {
            throw new ReloadDriverException(getName(), e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldWidth(int)
     */
    @Override
    public int getFieldWidth(int fieldId) {
        int i = -1;
        try {
            int aux = fieldId + 2; // idField viene basado en 1, y
            i = rs.getMetaData().getColumnDisplaySize(aux);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // SUN define que getColumnDisplaySize devuelve numeros negativos cuando el campo es de tipo Text o Vartext
        // sin ancho. Nosotros vamos a devolver 255 para que fucione, por lo menos al exportar a DBF.
        // Nota: Si se truncan cadenas, este es el sitio que lo provoca.
        if (i < 0) {
            i = 255;
        }
        return i;
    }

    // public void setFlatness(double flatness) {
    // this.flatness = flatness;
    // }

    // -----------------------------------------------------------
    // ---- EXT JDBC NUEVA ---
    // -----------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver#getConnectionString(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String getConnectionString(String host, String port, String dbname, String user, String pw) {

        String resp = getConnectionStringBeginning() + "//" + host.toLowerCase();

        if (dbname.trim().length() > 0) {
            resp += ":" + port;
        } else {
            resp += ":" + getDefaultPort();
        }

        resp += "/" + dbname;// .toLowerCase();
        return resp;
    }

    /**
     * Gets available table names. Should be overwritten by subclasses if its
     * not compatible or if it can be refined
     *
     * @param conn
     *            connection object
     * @param catalog
     *            catalog name
     * @return array of strings with available table names
     * @throws SQLException
     */
    @Override
    public String[] getTableNames(IConnection conn, String catalog) throws DBException {
        try {
            DatabaseMetaData dbmd;
            dbmd = ((ConnectionJDBC) conn).getConnection().getMetaData();

            String[] types = { "TABLE", "VIEW" };
            ResultSet rs = dbmd.getTables(catalog, null, null, types);
            TreeMap ret = new TreeMap();
            while (rs.next()) {
                // ret.put(rs.getString("TABLE_NAME"), rs.getString("TABLE_NAME"));
                // As suggested by Jorge Agudo, to allow charging tables from other schemas
                ret.put((rs.getString("TABLE_SCHEM") != null ? (rs.getString("TABLE_SCHEM") + ".") : "")
                        + rs.getString("TABLE_NAME"),
                        (rs.getString("TABLE_SCHEM") != null ? (rs.getString("TABLE_SCHEM") + ".") : "")
                                + rs.getString("TABLE_NAME"));
            }
            rs.close();
            return (String[]) ret.keySet().toArray(new String[0]);
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    /**
     * Gets all field names of a given table
     * 
     * @param conn
     *            connection object
     * @param table_name
     *            table name
     * @return all field names of the given table
     * @throws SQLException
     */
    @Override
    public String[] getAllFields(IConnection conn, String table_name) throws DBException {
        try {
            Statement st = ((ConnectionJDBC) conn).getConnection().createStatement();
            ResultSet rs = st.executeQuery("select * from " + table_name + " LIMIT 1");
            ResultSetMetaData rsmd = rs.getMetaData();
            String[] ret = new String[rsmd.getColumnCount()];

            for (int i = 0; i < ret.length; i++) {
                ret[i] = rsmd.getColumnName(i + 1);
            }
            rs.close();
            st.close();
            return ret;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    /**
     * Gets all field type names of a given table
     * 
     * @param conn
     *            connection object
     * @param table_name
     *            table name
     * @return all field type names of the given table
     * @throws SQLException
     */
    @Override
    public String[] getAllFieldTypeNames(IConnection conn, String table_name) throws DBException {
        try {
            Statement st = ((ConnectionJDBC) conn).getConnection().createStatement();
            ResultSet rs = st.executeQuery("select * from " + table_name + " LIMIT 1");
            ResultSetMetaData rsmd = rs.getMetaData();
            String[] ret = new String[rsmd.getColumnCount()];

            for (int i = 0; i < ret.length; i++) {
                ret[i] = rsmd.getColumnTypeName(i + 1);
            }
            rs.close();
            st.close();
            return ret;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    /**
     * Gets the table's possible id fields. By default, all fields can be id.
     * It should be overwritten by subclasses.
     *
     * @param conn
     *            conenction object
     * @param table_name
     *            table name
     * @return the table's possible id fields
     * @throws SQLException
     */
    @Override
    public String[] getIdFieldsCandidates(IConnection conn, String table_name) throws DBException {
        return getAllFields(conn, table_name);
    }

    /**
     * Gets the table's possible geometry fields. By default, all fields can be geometry
     * fields. It should be overwritten by subclasses.
     *
     * @param conn
     *            conenction object
     * @param table_name
     *            table name
     * @return the table's possible geometry fields
     * @throws SQLException
     */
    @Override
    public String[] getGeometryFieldsCandidates(IConnection conn, String table_name) throws DBException {
        return getAllFields(conn, table_name);
    }

    /**
     * Tells if it's an empty table (with no records)
     * 
     * @param conn
     *            conenction object
     * @param tableName
     *            rtable name
     * @return whether it's an empty table (with no records) or not
     */
    public boolean isEmptyTable(IConnection conn, String tableName) {

        boolean res = true;

        try {
            Statement st = ((ConnectionJDBC) conn).getConnection().createStatement();
            ResultSet rs = null;
            rs = st.executeQuery("select * from " + tableName + " LIMIT 1");
            res = !rs.next();
            rs.close();
            st.close();
        } catch (Exception ex) {
            res = true;
        }
        return res;
    }

    /**
     * Utility method to allow the driver to do something with the geometry field.
     * By default does nothing.
     *
     * @param flds
     *            user-selected fields
     * @param geomField
     *            geometry field
     * @return new user-selected fields
     */
    public String[] manageGeometryField(String[] flds, String geomField) {
        return flds;
    }

    /**
     * Empty method called when the layer is going to be removed from the view.
     * Subclasses can overwrite it if needed.
     *
     */
    @Override
    public void remove() {

    }

    public void addDriverEventListener(DriverEventListener listener) {
        if (!driverEventListeners.contains(listener)) {
            driverEventListeners.add(listener);
        }

    }

    public void removeDriverEventListener(DriverEventListener listener) {
        driverEventListeners.remove(listener);

    }

    public void notifyDriverEndLoaded() {
        DriverEvent event = new DriverEvent(DriverEvent.DRIVER_EVENT_LOADING_END);
        for (int i = 0; i < driverEventListeners.size(); i++) {
            DriverEventListener aux = (DriverEventListener) driverEventListeners.get(i);
            aux.driverNotification(event);
        }
    }

    public static String removePrefix(String str) {

        int colon_ind = str.indexOf(":");
        if (colon_ind != -1) {
            return str.substring(colon_ind + 1);
        } else {
            return str;
        }
    }

    @Override
    public boolean canRead(IConnection iconn, String tablename) throws SQLException {
        return true;
    }

    @Override
    public void validateData(IConnection conn, DBLayerDefinition lyrDef) throws DBException {
        // subclasses should implement this to allow early detection of
        // invalid layer settings (gvSIG will allow user to correct them and the
        // notification dialog will not show)
    }
}

// [eiel-gestion-conexiones]