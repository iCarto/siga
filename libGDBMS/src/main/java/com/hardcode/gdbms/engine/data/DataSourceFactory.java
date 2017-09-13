package com.hardcode.gdbms.engine.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.rmi.server.UID;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import com.hardcode.driverManager.Driver;
import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.driverManager.DriverManager;
import com.hardcode.driverManager.WriterManager;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.driver.exceptions.WriteDriverException;
import com.hardcode.gdbms.engine.data.db.DBDataSource;
import com.hardcode.gdbms.engine.data.db.DBDataSourceFactory;
import com.hardcode.gdbms.engine.data.db.DBQuerySourceInfo;
import com.hardcode.gdbms.engine.data.db.DBSourceInfo;
import com.hardcode.gdbms.engine.data.db.DBTableSourceInfo;
import com.hardcode.gdbms.engine.data.db.SpatialDBTableSourceInfo;
import com.hardcode.gdbms.engine.data.driver.AlphanumericDBDriver;
import com.hardcode.gdbms.engine.data.driver.DBDriver;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.data.driver.FileDriver;
import com.hardcode.gdbms.engine.data.driver.GDBMSDriver;
import com.hardcode.gdbms.engine.data.driver.ObjectDriver;
import com.hardcode.gdbms.engine.data.file.FileCreationSourceInfo;
import com.hardcode.gdbms.engine.data.file.FileDataSource;
import com.hardcode.gdbms.engine.data.file.FileDataSourceFactory;
import com.hardcode.gdbms.engine.data.file.FileSourceInfo;
import com.hardcode.gdbms.engine.data.object.ObjectDataSource;
import com.hardcode.gdbms.engine.data.object.ObjectDataSourceFactory;
import com.hardcode.gdbms.engine.data.object.ObjectSourceInfo;
import com.hardcode.gdbms.engine.data.persistence.DataSourceLayerMemento;
import com.hardcode.gdbms.engine.data.persistence.Memento;
import com.hardcode.gdbms.engine.data.persistence.OperationLayerMemento;
import com.hardcode.gdbms.engine.instruction.Adapter;
import com.hardcode.gdbms.engine.instruction.ColRefAdapter;
import com.hardcode.gdbms.engine.instruction.CustomAdapter;
import com.hardcode.gdbms.engine.instruction.EvaluationException;
import com.hardcode.gdbms.engine.instruction.SelectAdapter;
import com.hardcode.gdbms.engine.instruction.SemanticException;
import com.hardcode.gdbms.engine.instruction.TableRefAdapter;
import com.hardcode.gdbms.engine.instruction.UnionAdapter;
import com.hardcode.gdbms.engine.instruction.Utilities;
import com.hardcode.gdbms.engine.strategies.OperationDataSource;
import com.hardcode.gdbms.engine.strategies.Strategy;
import com.hardcode.gdbms.engine.strategies.StrategyManager;
import com.hardcode.gdbms.parser.Node;
import com.hardcode.gdbms.parser.ParseException;
import com.hardcode.gdbms.parser.SQLEngine;
import com.hardcode.gdbms.parser.SimpleNode;

/**
 * Clase factoría de DataSources. Contiene métodos para registrar las fuentes de
 * datos (addXXXDataSource) y para obtener los DataSource's asociados a estas
 * createRandomDataSource. Además proporciona un método para ejecutar consultas
 * SQL a partir de la instrucción como cadena o a partir de la instrucción como
 * árbol de adaptadores
 *
 * @author Fernando González Cortés
 */
public class DataSourceFactory {
	public final static int MANUAL_OPENING = 0;

	public final static int AUTOMATIC_OPENING = 1;

	public final static int DATA_WARE_DIRECT_MODE = 0;

	public final static int DATA_WARE_COHERENT_ROW_ORDER = 1;

	final static int DEFAULT_DELAY = 5000;

	/**
	 * Asocia los nombres de las tablas con la información del origen de datos
	 */
	private HashMap tableSource = new HashMap();

	/** Associates a name with the operation layer DataSource with that name */
	private HashMap nameOperationDataSource = new HashMap();

	/**
	 * Asocia los nombres de los orígenes de datos de base de datos con el
	 * nombre de la tabla en el sistema de gestión original
	 */
	private HashMap nameTable = new HashMap();

	private HashMap sourceInfoServerViewInfo = new HashMap();

	private DriverManager dm;

	private ModuleSupport ms = new ModuleSupport();

	private long delay = DEFAULT_DELAY;

	private boolean delegating = false;

	private File tempDir = new File(".");

	private WriterManager wm;

	private Hashtable driversNamesAliases = new Hashtable();

	/**
	 * Get's a unique id in the tableSource and nameOperationDataSource key sets
	 *
	 * @return unique id
	 */
	private String getUID() {
		UID uid = new UID();

		String name = "gdbms" + uid.toString().replace(':','_').replace('-','_');
		return name;
	}

	/**
	 * Removes all associations between names and data sources of any layer.
	 */
	public void removeAllDataSources() {
		tableSource.clear();
		nameOperationDataSource.clear();
	}

	/**
	 * Removes the association between the name and the data sources
	 *
	 * @param ds
	 *            Name of the data source to remove
	 * @throws WriteDriverException TODO
	 * @throws RuntimeException
	 *             If there is no data source registered with that name
	 */
	public void remove(DataSource ds) throws WriteDriverException {
		String name = ds.getName();

		if (tableSource.remove(name) == null) {
			if (nameOperationDataSource.remove(name) == null) {
				throw new RuntimeException(
						"No datasource with the name. Data source name changed since the DataSource instance was retrieved?");
			}
		}
	}

	/**
	 * Removes de View of the data source 'ds'
	 *
	 * @param ds
	 *            DataSource whose view will be deleted
	 * @throws ReadDriverException TODO
	 */
	private void clearView(DBDataSource ds) throws ReadDriverException {
		DBTableSourceInfo dbInfo = (DBTableSourceInfo) ds.getSourceInfo();
		String sql = "DROP VIEW " + dbInfo.tableName;
		ds.execute(sql);
	}

	/**
	 * Removes the views created at query delegation
	 * @throws ReadDriverException TODO
	 */
	public void clearViews() throws ReadDriverException {
		Iterator i = sourceInfoServerViewInfo.values().iterator();

		while (i.hasNext()) {
			ServerViewInfo svi = (ServerViewInfo) i.next();
			clearView(svi.adapter);
		}

		sourceInfoServerViewInfo.clear();
	}

	/**
	 * Añade una fuente de datos de objeto. Dado un objeto que implemente la
	 * interfaz del driver, se toma como fuente de datos y se le asocia un
	 * nombre
	 *
	 * @param rd
	 *            objeto con la información
	 * @param name
	 *            Nombre de la fuente de datos
	 */
	public void addDataSource(ObjectDriver rd, String name) {
		ObjectSourceInfo info = new ObjectSourceInfo();
		info.driver = rd;
		tableSource.put(name, info);
	}

	/**
	 * Añade una fuente de datos de objeto. Dado un objeto que implemente la
	 * interfaz del driver, se toma como fuente de datos y se le asocia un
	 * nombre
	 *
	 * @param rd
	 *            objeto con la información
	 *
	 * @return the name of the data source
	 */
	public String addDataSource(ObjectDriver rd) {
		String ret = getUID();
		addDataSource(rd, ret);

		return ret;
	}

	/**
	 * Adds a new data source to the system. If the file doesn't exists it is
	 * created when necessary
	 *
	 * @param driverName
	 *            Nombre del driver asociado a la fuente de datos
	 * @param name
	 *            Nombre de la tabla con el que se hará referencia en las
	 *            instrucciones
	 * @param file
	 *            Fichero con los datos
	 */
	public void createFileDataSource(String driverName, String name,
			String file, String[] fieldNames, int[] fieldTypes) {
		FileCreationSourceInfo info = (FileCreationSourceInfo) getFileSourceInfo(
				new FileCreationSourceInfo(), driverName, name, file, false);
		info.fieldNames = fieldNames;
		info.fieldTypes = fieldTypes;
		tableSource.put(name, info);
	}

	/**
	 * Añade un origen de datos de fichero al sistema. Cuando se cree un
	 * DataSource mediante la invocación createRandomDataSource(String) se
	 * creará una instancia del driver cuyo nombre es driverName
	 *
	 * @param driverName
	 *            Nombre del driver asociado a la fuente de datos
	 * @param name
	 *            Nombre de la tabla con el que se hará referencia en las
	 *            instrucciones
	 * @param file
	 *            Fichero con los datos
	 */
	public void addFileDataSource(String driverName, String name, String file) {
		FileSourceInfo info = getFileSourceInfo(new FileSourceInfo(),
				driverName, name, file, false);
		tableSource.put(name, info);
	}

	/**
	 * Gets a FileSourceInfo with the values passed in the parameters
	 *
	 * @param driverName
	 * @param name
	 * @param file
	 *
	 * @return FileSourceInfo
	 */
	private FileSourceInfo getFileSourceInfo(FileSourceInfo info,
			String driverName, String name, String file, boolean spatial) {
		info.name = name;
		info.file = file;
		info.driverName = driverName;
		info.spatial = spatial;

		return info;
	}

	/**
	 * Adds a spatial file data source to the system.
	 *
	 * @param driverName
	 *            driver used to obtain the data
	 * @param name
	 *            name of the data source
	 * @param file
	 *            file with the data
	 */
	public void addSpatialFileDataSource(String driverName, String name,
			String file) {
		FileSourceInfo info = getFileSourceInfo(new FileSourceInfo(),
				driverName, name, file, true);
		tableSource.put(name, info);
	}

	/**
	 * Adds a spatial file data source to the system.
	 *
	 * @param driverName
	 *            driver used to obtain the data
	 * @param file
	 *            file with the data
	 *
	 * @return String Generated name of the added data source
	 */
	public String addSpatialFileDataSource(String driverName, String file) {
		String ret = getUID();
		addSpatialFileDataSource(driverName, ret, file);

		return ret;
	}

	/**
	 * Añade un origen de datos de fichero al sistema. Cuando se cree un
	 * DataSource mediante la invocación createRandomDataSource(String) se
	 * creará una instancia del driver cuyo nombre es driverName
	 *
	 * @param driverName
	 *            Nombre del driver asociado a la fuente de datos
	 * @param file
	 *            Fichero con los datos
	 *
	 * @return Nombre único que se asocia a la tabla
	 */
	public String addFileDataSource(String driverName, String file) {
		String ret = getUID();
		addFileDataSource(driverName, ret, file);

		return ret;
	}

	/**
	 * Obtiene la información de la fuente de datos cuyo nombre se pasa como
	 * parámetro
	 *
	 * @param dataSourceName
	 *            Nombre de la base de datos
	 *
	 * @return Debido a las distintas formas en las que se puede registrar un
	 *         datasource, se devuelve un Object, que podrá ser una instancia de
	 *         DataSourceFactory.FileDriverInfo, DataSourceFactory.DBDriverInfo
	 *         o ReadDriver
	 */
	public SourceInfo getDriverInfo(String dataSourceName) {
		return (SourceInfo) tableSource.get(dataSourceName);
	}

	/**
	 * Gets the information of all data sources registered in the system
	 *
	 * @return DriverInfo[]
	 */
	public SourceInfo[] getDriverInfos() {
		ArrayList ret = new ArrayList();
		Iterator it = tableSource.values().iterator();

		while (it.hasNext()) {
			SourceInfo di = (SourceInfo) it.next();
			ret.add(di);
		}

		return (SourceInfo[]) ret.toArray(new SourceInfo[0]);
	}

	/**
	 * Añade un origen de datos de base de datos al sistema
	 *
	 * @param name
	 *            Nombre de la tabla con el que se hará referencia en las
	 *            instrucciones
	 * @param host
	 *            Cadena de conexión para conectar con el sgbd donde se
	 *            encuentra la tabla
	 * @param port
	 *            Nombre del sgbd capaz de ejecutar SQL en el que reside la
	 *            tabla. Generalmente será la parte de la cadena de conexión
	 *            correspondiente a host, puerto y nombre de la base de datos
	 * @param user
	 *            Nombre de usuario. Null para acceso sin usuario
	 * @param password
	 *            Si el usuario es null se ignora el password
	 * @param dbName
	 *            Nombre de la base de datos a la que se accede
	 * @param tableName
	 *            Nombre de la tabla en la base de datos
	 * @param driverInfo
	 *            Información para saber qué driver debe acceder a la
	 *            información. Se escogerá el driver cuyo valor de retorno del
	 *            método getType coincida con este valor
	 */
	public void addDBDataSourceByTable(String name, String host, int port,
			String user, String password, String dbName, String tableName,
			String driverInfo) {
		DBTableSourceInfo info = new DBTableSourceInfo();
		fillDBTableSourceInfo(info, name, host, port, user, password, dbName,
				tableName, driverInfo);
		tableSource.put(name, info);
		nameTable.put(name, tableName);
	}


	/**
	 * Añade un origen de datos de base de datos al sistema
	 *
	 * @param name
	 *            Nombre de la tabla con el que se hará referencia en las
	 *            instrucciones
	 * @param Connection
	 *            Conexion JDBC a la Base de datos ya abierta (el DataSource
	 *            la usara, pero no la abrira/cerrara)
	 * @param tableName
	 *            Nombre de la tabla en la base de datos
	 * @param driverInfo
	 *            Información para saber qué driver debe acceder a la
	 *            información. Se escogerá el driver cuyo valor de retorno del
	 *            método getType coincida con este valor
	 */
	public void addDBDataSourceByTable(String name, Connection connection, String tableName,
			String driverInfo) {
		DBTableSourceInfo info = new DBTableSourceInfo();
		fillDBTableSourceInfo(info, name, connection,
				tableName, driverInfo);
		tableSource.put(name, info);
		nameTable.put(name, tableName);
	}




	/**
	 * Fills the info struct with the values passed in the parameters
	 *
	 * @param info
	 *            struct to populate
	 * @param name
	 *            Name of the data source
	 * @param host
	 *            host where the data is
	 * @param port
	 *            port number
	 * @param user
	 *            user name or null.
	 * @param password
	 *            if user name is null is ignored
	 * @param dbName
	 *            database name
	 * @param tableName
	 *            table name
	 * @param driverInfo
	 *            name of the driver used to access the data
	 */
	private void fillDBTableSourceInfo(DBTableSourceInfo info, String name,
			String host, int port, String user, String password, String dbName,
			String tableName, String driverInfo) {
		info.name = name;
		info.host = host;
		info.port = port;
		info.user = user;
		info.password = password;
		info.dbName = dbName;
		info.dbms = host + ":" + port + "/" + dbName + "," + user + ","
				+ password;
		info.tableName = tableName;
		info.driverName = driverInfo;
	}

	/**
	 * Fills the info struct with the values passed in the parameters
	 *
	 * @param info
	 *            struct to populate
	 * @param name
	 *            Name of the data source
	 * @param Connection
	 *            JDBC opened data base Connection
	 * @param port
	 *            port number
	 * @param user
	 *            user name or null.
	 * @param password
	 *            if user name is null is ignored
	 * @param dbName
	 *            database name
	 * @param tableName
	 *            table name
	 * @param driverInfo
	 *            name of the driver used to access the data
	 */
	private void fillDBTableSourceInfo(DBTableSourceInfo info, String name,
			Connection conection, String tableName, String driverInfo) {
		info.name = name;
		info.host = "";
		info.port = -1;
		info.user = "";
		info.password = "";
		info.dbName = "";
		info.dbms = "";
		info.connection= conection;
		info.tableName = tableName;
		info.driverName = driverInfo;
	}








	/**
	 * Adds a spatial database data source
	 *
	 * @param name
	 *            Name of the data source
	 * @param host
	 *            host where the data is
	 * @param port
	 *            port number
	 * @param user
	 *            user name or null.
	 * @param password
	 *            if user name is null is ignored
	 * @param dbName
	 *            database name
	 * @param tableName
	 *            table name
	 * @param geometryFieldName
	 *            name of the field that has the geometry
	 * @param driverInfo
	 *            name of the driver used to access the data
	 */
	public void addSpatialDBDataSource(String name, String host, int port,
			String user, String password, String dbName, String tableName,
			String geometryFieldName, String driverInfo) {
		SpatialDBTableSourceInfo info = new SpatialDBTableSourceInfo();
		fillDBTableSourceInfo(info, name, host, port, user, password, dbName,
				tableName, driverInfo);
		info.geometryField = geometryFieldName;
		tableSource.put(name, info);
		nameTable.put(name, tableName);
	}


	/**
	 * Adds a spatial database data source
	 *
	 * @param connection
	 *
	 * @param tableName
	 *            table name
	 * @param geometryFieldName
	 *            name of the field that has the geometry
	 * @param driverInfo
	 *            name of the driver used to access the data
	 */
	public void addSpatialDBDataSource(String name, Connection connection, String tableName,
			String geometryFieldName, String driverInfo) {
		SpatialDBTableSourceInfo info = new SpatialDBTableSourceInfo();
		fillDBTableSourceInfo(info, name, connection,
				tableName, driverInfo);
		info.geometryField = geometryFieldName;
		tableSource.put(name, info);
		nameTable.put(name, tableName);
	}








	/**
	 * Adds a spatial database data source
	 *
	 * @param host
	 *            host where the data is
	 * @param port
	 *            port number
	 * @param user
	 *            user name or null.
	 * @param password
	 *            if user name is null is ignored
	 * @param dbName
	 *            database name
	 * @param tableName
	 *            table name
	 * @param geometryFieldName
	 *            name of the field that has the geometry
	 * @param driverInfo
	 *            name of the driver used to access the data
	 *
	 * @return generated name of the added data source
	 */
	public String addSpatialDBDataSource(String host, int port, String user,
			String password, String dbName, String tableName,
			String geometryFieldName, String driverInfo) {
		String ret = getUID();
		addSpatialDBDataSource(ret, host, port, user, password, dbName,
				tableName, geometryFieldName, driverInfo);

		return ret;
	}


	/**
	 * Adds a spatial database data source
	 *
	 * @param connection
	 *
	 * @param tableName
	 *            table name
	 * @param geometryFieldName
	 *            name of the field that has the geometry
	 * @param driverInfo
	 *            name of the driver used to access the data
	 *
	 * @return generated name of the added data source
	 */
	public String addSpatialDBDataSource(Connection connection, String tableName,
			String geometryFieldName, String driverInfo) {
		String ret = getUID();
		addSpatialDBDataSource(ret, connection,
				tableName, geometryFieldName, driverInfo);

		return ret;
	}





	/**
	 * Añade un origen de datos de base de datos al sistema
	 *
	 * @param host
	 *            Cadena de conexión para conectar con el sgbd donde se
	 *            encuentra la tabla
	 * @param port
	 *            Nombre del sgbd capaz de ejecutar SQL en el que reside la
	 *            tabla. Generalmente será la parte de la cadena de conexión
	 *            correspondiente a host, puerto y nombre de la base de datos
	 * @param user
	 *            Nombre de usuario. Null para acceso sin usuario
	 * @param password
	 *            Si el usuario es null se ignora el password
	 * @param dbName
	 *            Nombre de la base de datos a la que se accede
	 * @param tableName
	 *            Nombre de la tabla en la base de datos
	 * @param driverInfo
	 *            Información para saber qué driver debe acceder a la
	 *            información. Se escogerá el driver cuyo valor de retorno del
	 *            método getType coincida con este valor
	 *
	 * @return Nombre de la tabla con el que se hará referencia en las
	 *         instrucciones
	 */
	public String addDBDataSourceByTable(String host, int port, String user,
			String password, String dbName, String tableName, String driverInfo) {
		String name = getUID();
		addDBDataSourceByTable(name, host, port, user, password, dbName,
				tableName, driverInfo);

		return name;
	}



	/**
	 * Añade un origen de datos de base de datos al sistema
	 *
	 * @param connection
	 *            Conexion JDBC abierta a la base de datos(el DataSource
	 *            usara la conexion, pero no la abrira/cerrara)
	 * @param tableName
	 *            Nombre de la tabla en la base de datos
	 * @param driverInfo
	 *            Información para saber qué driver debe acceder a la
	 *            información. Se escogerá el driver cuyo valor de retorno del
	 *            método getType coincida con este valor
	 *
	 * @return Nombre de la tabla con el que se hará referencia en las
	 *         instrucciones
	 */
	public String addDBDataSourceByTable(Connection connection, String tableName, String driverInfo) {
		String name = getUID();
		addDBDataSourceByTable(name, connection,
				tableName, driverInfo);

		return name;
	}





















	/**
	 * Añade un origen de datos de base de datos al sistema
	 *
	 * @param name
	 *            Nombre de la tabla con el que se hará referencia en las
	 *            instrucciones
	 * @param host
	 *            Cadena de conexión para conectar con el sgbd donde se
	 *            encuentra la tabla
	 * @param port
	 *            Nombre del sgbd capaz de ejecutar SQL en el que reside la
	 *            tabla. Generalmente será la parte de la cadena de conexión
	 *            correspondiente a host, puerto y nombre de la base de datos
	 * @param user
	 *            Nombre de usuario. Null para acceso sin usuario
	 * @param password
	 *            Si el usuario es null se ignora el password
	 * @param dbName
	 *            Nombre de la base de datos a la que se accede
	 * @param sql
	 *            Instrucción SQL que define los datos de la tabla
	 * @param driverInfo
	 *            Información para saber qué driver debe acceder a la
	 *            información. Se escogerá el driver cuyo valor de retorno del
	 *            método getType coincida con este valor
	 */
	public void addDBDataSourceBySQL(String name, String host, int port,
			String user, String password, String dbName, String sql,
			String driverInfo) {
		DBQuerySourceInfo info = new DBQuerySourceInfo();
		info.name = name;
		info.host = host;
		info.port = port;
		info.user = user;
		info.password = password;
		info.dbName = dbName;
		info.dbms = host + ":" + port + "/" + dbName + "," + user + ","
				+ password;
		info.sql = sql;
		info.driverName = driverInfo;
		tableSource.put(name, info);
	}


	/**
	 * Añade un origen de datos de base de datos al sistema
	 *
	 * @param name
	 *            Nombre de la tabla con el que se hará referencia en las
	 *            instrucciones
	 * @param connection
	 *            Conexion de JDBC a la base de datos ya abierta (el
	 *            DataSource la usara, pero no la abrira/cerrara)
	 * @param sql
	 *            Instrucción SQL que define los datos de la tabla
	 * @param driverInfo
	 *            Información para saber qué driver debe acceder a la
	 *            información. Se escogerá el driver cuyo valor de retorno del
	 *            método getType coincida con este valor
	 */
	public void addDBDataSourceBySQL(String name, Connection connection, String sql,
			String driverInfo) {
		DBQuerySourceInfo info = new DBQuerySourceInfo();
		info.name = name;
		info.host = "";
		info.port = -1;
		info.user = "";
		info.password = "";
		info.dbName = "";
		info.dbms ="";
		info.connection = connection;
		info.sql = sql;
		info.driverName = driverInfo;
		tableSource.put(name, info);
	}







	/**
	 * Añade un origen de datos de base de datos al sistema
	 *
	 * @param host
	 *            Cadena de conexión para conectar con el sgbd donde se
	 *            encuentra la tabla
	 * @param port
	 *            Nombre del sgbd capaz de ejecutar SQL en el que reside la
	 *            tabla. Generalmente será la parte de la cadena de conexión
	 *            correspondiente a host, puerto y nombre de la base de datos
	 * @param user
	 *            Nombre de usuario. Null para acceso sin usuario
	 * @param password
	 *            Si el usuario es null se ignora el password
	 * @param dbName
	 *            Nombre de la base de datos a la que se accede
	 * @param sql
	 *            Instrucción SQL que define los datos de la tabla
	 * @param driverInfo
	 *            Información para saber qué driver debe acceder a la
	 *            información. Se escogerá el driver cuyo valor de retorno del
	 *            método getType coincida con este valor
	 *
	 * @return Nombre de la tabla con el que se hará referencia en las
	 *         instrucciones
	 */
	public String addDBDataSourceBySQL(String host, int port, String user,
			String password, String dbName, String sql, String driverInfo) {
		String ret = getUID();
		addDBDataSourceBySQL(ret, host, port, user, password, dbName, sql,
				driverInfo);

		return ret;
	}

	/**
	 * Añade un origen de datos de base de datos al sistema
	 *
	 * @param connection
	 *            Conexion de JDBC ya abierta (el DataSource la usara
	 *            pero no la abrira/cerrara)
	 * @param sql
	 *            Instrucción SQL que define los datos de la tabla
	 * @param driverInfo
	 *            Información para saber qué driver debe acceder a la
	 *            información. Se escogerá el driver cuyo valor de retorno del
	 *            método getType coincida con este valor
	 *
	 * @return Nombre de la tabla con el que se hará referencia en las
	 *         instrucciones
	 */
	public String addDBDataSourceBySQL(Connection connection, String sql, String driverInfo) {
		String ret = getUID();
		addDBDataSourceBySQL(ret, connection, sql,
				driverInfo);

		return ret;
	}


	/**
	 * Cambia el nombre de una fuente de datos. Las consultas SQL que se
	 * ejecuten con el nombre anterior fallarán
	 *
	 * @param oldName
	 *            Nombre actual de la fuente de datos que se quiere cambiar
	 * @param newName
	 *            Nombre que se le quiere poner a la fuente de datos
	 *
	 * @throws NoSuchTableException
	 *             Si no hay ninguna fuente de datos de nombre 'oldName'
	 */
	public void changeDataSourceName(String oldName, String newName)
			throws NoSuchTableException {
		SourceInfo di = (SourceInfo) tableSource.remove(oldName);

		if (di == null) {
			// may be a operation layer DataSource
			OperationDataSource ret = (OperationDataSource) nameOperationDataSource
					.remove(oldName);
			if (ret == null){
				throw new NoSuchTableException(oldName);

			}
			nameOperationDataSource.put(newName, ret);


		} else {

			tableSource.put(newName, di);
		}
	}

	/**
	 * Gets the data source passed by adding the AutomaticDataSource decorator
	 * if factory mode is AUTOMATIC.
	 *
	 * @param ds
	 *            DataSource
	 * @param mode
	 *            opening mode
	 *
	 * @return DataSource
	 */
	private DataSource getModedDataSource(DataSource ds, int mode) {
		if (mode == AUTOMATIC_OPENING) {
			return new AutomaticDataSource(ds, delay);
		} else {
			return ds;
		}
	}

	/**
	 * Sets the minimum delay between accesses needed to close the DataSource.
	 * If accesses are delayed more than 'delay' the DataSource MAY be closed.
	 * Only applies when the mode is set to AUTOMATIC_MODE
	 *
	 * @param delay
	 *            time un milliseconds
	 */
	public void setClosingDelay(long delay) {
		this.delay = delay;
	}

	/**
	 * Dado el nombre de una tabla, se busca la fuente de datos asociada a dicha
	 * tabla y se obtiene un datasource adecuado en funcion del tipo de fuente
	 * de datos accediendo al subsistema de drivers
	 *
	 * @param tableName
	 *            Nombre de la fuente de datos
	 *
	 * @return DataSource que accede a dicha fuente
	 * @throws DriverLoadException
	 * @throws NoSuchTableException
	 * @throws ReadDriverException TODO
	 */
	public DataSource createRandomDataSource(String tableName)
			throws DriverLoadException, NoSuchTableException, ReadDriverException {
		return createRandomDataSource(tableName, tableName, MANUAL_OPENING);
	}

	/**
	 * Dado el nombre de una tabla, se busca la fuente de datos asociada a dicha
	 * tabla y se obtiene un datasource adecuado en funcion del tipo de fuente
	 * de datos accediendo al subsistema de drivers
	 *
	 * @param tableName
	 *            Nombre de la fuente de datos
	 * @param mode
	 *            opening mode: AUTOMATIC_OPENING -> the DataSource opens
	 *            automatically and closes after a while. It can be closed
	 *            manually. MANUAL_OPENING -> the DataSource opens and closes
	 *            manually
	 *
	 * @return DataSource que accede a dicha fuente
	 * @throws DriverLoadException
	 * @throws NoSuchTableException
	 * @throws ReadDriverException TODO
	 */
	public DataSource createRandomDataSource(String tableName, int mode)
			throws DriverLoadException, NoSuchTableException, ReadDriverException {
		return createRandomDataSource(tableName, tableName, mode);
	}

	/**
	 * Dado el nombre de una tabla, se busca la fuente de datos asociada a dicha
	 * tabla y se obtiene un datasource adecuado en funcion del tipo de fuente
	 * de datos accediendo al subsistema de drivers. Se utiliza internamente
	 * como nombre del DataSource el alias que se pasa como parámetro
	 *
	 * @param tableName
	 *            Nombre de la fuente de datos
	 * @param tableAlias
	 *            Alias que tiene el DataSource en una instrucción
	 *
	 * @return DataSource que accede a dicha fuente de datos si la fuente de
	 *         datos es alfanumérica o SpatialDataSource si la fuente de datos
	 *         es espacial
	 * @throws NoSuchTableException
	 *             Si no hay una fuente de datos registrada con ese nombre
	 * @throws DriverLoadException
	 *             Si hay algún error con el sistema de carga de drivers
	 * @throws ReadDriverException TODO
	 * @throws RuntimeException
	 *             bug
	 */
	public DataSource createRandomDataSource(String tableName, String tableAlias)
			throws NoSuchTableException, DriverLoadException, ReadDriverException {
		return createRandomDataSource(tableName, tableAlias, MANUAL_OPENING);
	}

	/**
	 * Dado el nombre de una tabla, se busca la fuente de datos asociada a dicha
	 * tabla y se obtiene un datasource adecuado en funcion del tipo de fuente
	 * de datos accediendo al subsistema de drivers. Se utiliza internamente
	 * como nombre del DataSource el alias que se pasa como parámetro
	 *
	 * @param tableName
	 *            Nombre de la fuente de datos
	 * @param tableAlias
	 *            Alias que tiene el DataSource en una instrucción
	 * @param mode
	 *            openning mode
	 *
	 * @return DataSource que accede a dicha fuente de datos si la fuente de
	 *         datos es alfanumérica o SpatialDataSource si la fuente de datos
	 *         es espacial
	 * @throws NoSuchTableException
	 *             Si no hay una fuente de datos registrada con ese nombre
	 * @throws DriverLoadException
	 *             Si hay algún error con el sistema de carga de drivers
	 * @throws ReadDriverException TODO
	 * @throws RuntimeException
	 *             bug
	 */
	public DataSource createRandomDataSource(String tableName,
			String tableAlias, int mode) throws NoSuchTableException,
			DriverLoadException, ReadDriverException {
		Object o = tableSource.get(tableName);

		if (o == null) {
			// may be a operation layer DataSource
			OperationDataSource ret = (OperationDataSource) nameOperationDataSource
					.get(tableName);

			if (ret != null) {
				ret.setName(tableAlias);

				return getModedDataSource(ret, mode);
			}

			// table not found
			throw new NoSuchTableException(tableName);
		}

		SourceInfo info = (SourceInfo) o;
		info.name = tableAlias;

		if (info instanceof FileSourceInfo) {
			FileSourceInfo fileInfo = (FileSourceInfo) info;

			Driver d = this.getDriver(fileInfo.driverName);

			if (info instanceof FileCreationSourceInfo) {
				FileCreationSourceInfo creationInfo = (FileCreationSourceInfo) info;
					if (!new File(creationInfo.file).exists()) {
						((FileDriver) d).createSource(creationInfo.file,
								creationInfo.fieldNames,
								creationInfo.fieldTypes);
					}
			}

			FileDataSource adapter;

			if (fileInfo.spatial) {
				adapter = FileDataSourceFactory.newSpatialInstance();
			} else {
				adapter = FileDataSourceFactory.newInstance();
			}

			((GDBMSDriver) d).setDataSourceFactory(this);
			adapter.setDriver((FileDriver) d);
			adapter.setSourceInfo(fileInfo);
			adapter.setDataSourceFactory(this);

			return getModedDataSource(adapter, mode);
		} else if (info instanceof DBQuerySourceInfo) {
			DBQuerySourceInfo dbInfo = (DBQuerySourceInfo) info;

			String driverInfo = dbInfo.driverName;
			Driver d = this.getDriver(driverInfo);

			((GDBMSDriver) d).setDataSourceFactory(this);
			return getModedDataSource(getDataSourceByQuery(dbInfo.sql,
					(AlphanumericDBDriver) d, dbInfo), mode);
		} else if (info instanceof DBTableSourceInfo) {
			DBTableSourceInfo dbInfo = (DBTableSourceInfo) info;

			String driverInfo = dbInfo.driverName;
			Driver d = this.getDriver(driverInfo);

			DBDataSource adapter;

			if (info instanceof SpatialDBTableSourceInfo) {
				adapter = DBDataSourceFactory.newSpatialDataSourceInstance();
			} else {
				adapter = DBDataSourceFactory.newDataSourceInstance();
			}

			((GDBMSDriver) d).setDataSourceFactory(this);
			adapter.setDriver((DBDriver) d);
			adapter.setSourceInfo(dbInfo);
			adapter.setDataSourceFactory(this);

			return getModedDataSource(adapter, mode);
		} else if (info instanceof ObjectSourceInfo) {
			ObjectSourceInfo driverInfo = (ObjectSourceInfo) o;
			ObjectDataSource adapter = ObjectDataSourceFactory.newInstance();
			driverInfo.driver.setDataSourceFactory(this);
			adapter.setDriver(driverInfo.driver);
			adapter.setSourceInfo((ObjectSourceInfo) driverInfo);
			adapter.setDataSourceFactory(this);

			return getModedDataSource(adapter, mode);
		} else {
			throw new RuntimeException();
		}
	}

	/**
	 * Creates a DataSource from a memento object with the manual opening mode
	 *
	 * @param m
	 *            memento
	 *
	 * @return DataSource
	 *
	 * @throws RuntimeException
	 *             If the DataSource class cannot be instatiated
	 */
	public DataSource createRandomDataSource(Memento m) {
		return createRandomDataSource(m, DataSourceFactory.MANUAL_OPENING);
	}

	/**
	 * Creates a DataSource from a memento object with the specified opening
	 * mode
	 *
	 * @param m
	 *            memento
	 * @param mode
	 *            opening mode
	 *
	 * @return DataSource
	 *
	 * @throws RuntimeException
	 *             If the DataSource class cannot be instatiated
	 */
	public DataSource createRandomDataSource(Memento m, int mode) {
		if (m instanceof DataSourceLayerMemento) {
			DataSourceLayerMemento mem = (DataSourceLayerMemento) m;

			try {
				return createRandomDataSource(mem.getTableName(), mem
						.getTableAlias(), mode);
			} catch (DriverLoadException e) {
				throw new RuntimeException(
						"La información guardada no es consistente", e);
			} catch (NoSuchTableException e) {
				throw new RuntimeException(
						"La información guardada no es consistente", e);
			} catch (ReadDriverException e) {
				throw new RuntimeException(e);
			}
		} else {
			OperationLayerMemento mem = (OperationLayerMemento) m;

			try {
				return executeSQL(mem.getSql(), mode);
			} catch (DriverLoadException e) {
				throw new RuntimeException(
						"La información guardada no es consistente", e);
			} catch (ParseException e) {
				throw new RuntimeException(
						"La información guardada no es consistente", e);
			} catch (SemanticException e) {
				throw new RuntimeException(
						"La información guardada no es consistente", e);
			} catch (EvaluationException e) {
				throw new RuntimeException(
						"La información guardada no es consistente", e);
			} catch (ReadDriverException e) {
				throw new RuntimeException(
						"La información guardada no es consistente", e);
			}
		}
	}

	/**
	 * Devuelve true si todas las tablas provienen del mismo data base
	 * management system
	 *
	 * @param tables
	 *            Array de tablas
	 *
	 * @return boolean
	 */
	private boolean sameDBMS(DataSource[] tables) {
		if (!(tables[0] instanceof DBDataSource)) {
			return false;
		}

		String dbms = ((DBDataSource) tables[0]).getDBMS();

		for (int i = 1; i < tables.length; i++) {
			if (!(tables[i] instanceof DBDataSource)) {
				return false;
			}

			if (!dbms.equals(((DBDataSource) tables[1]).getDBMS())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * A partir de una instrucción select se encarga de obtener el DataSource
	 * resultado de la ejecución de dicha instrucción
	 *
	 * @param instr
	 *            Instrucción select origen del datasource
	 * @param mode
	 *            opening mode
	 *
	 * @return DataSource que accede a los datos resultado de ejecutar la select
	 * @throws DriverLoadException
	 * @throws SemanticException
	 *             Si la instrucción tiene errores semánticos
	 * @throws EvaluationException
	 *             If there's an error evaluating any expression
	 * @throws ReadDriverException TODO
	 */
	public DataSource createRandomDataSource(SelectAdapter instr, int mode)
			throws DriverLoadException, SemanticException,
			EvaluationException, ReadDriverException {
		return getModedDataSource(getDataSource(instr), mode);
	}

	/**
	 * Creates a view in the database management system that hosts the data
	 * source 'dbds'. The view is defined by the sql parameter
	 *
	 * @param dbds
	 *            DataSource used to execute the query
	 * @param sql
	 *            The SQL query defining the view
	 *
	 * @return Name of the view
	 * @throws ReadDriverException TODO
	 * @throws DriverException
	 *             If the view cannot be created
	 */
	private String getView(DBDataSource dbds, String sql)
			throws ReadDriverException {
		ServerViewInfo svi = (ServerViewInfo) sourceInfoServerViewInfo.get(dbds
				.getSourceInfo());

		/*
		 * Return the view name if it's already created or create the view if
		 * it's not created
		 */
		if (svi != null) {
			return svi.viewName;
		} else {
			// create the view
			String viewName = getUID();
			String viewQuery = "CREATE VIEW " + viewName + " AS " + sql;
			dbds.execute(viewQuery);

			// Register the view created
			sourceInfoServerViewInfo.put(dbds.getSourceInfo(),
					new ServerViewInfo(dbds, viewName));

			return viewName;
		}
	}

	/**
	 * Gets a DataSource implementation with the sql instruction as the data
	 * source by creating a view in the underlaying datasource management system
	 *
	 * @param sql
	 *            Instruction definig the data source
	 * @param driver
	 *            Driver used to access the data source
	 * @param dbInfo
	 *            data source info
	 *
	 * @return DataSource
	 * @throws ReadDriverException TODO
	 */
	private DBDataSource getDataSourceByQuery(String sql,
			AlphanumericDBDriver driver, DBTableSourceInfo dbInfo)
			throws ReadDriverException {
		// Create the adapter
		DBDataSource adapter = DBDataSourceFactory.newDataSourceInstance();

		// set the driver
		adapter.setDriver(driver);

		// Create the query
		adapter.setSourceInfo(dbInfo);

		// Gets the view name
		String viewName = getView(adapter, sql);

		// Complete the source info with the view name
		dbInfo.tableName = viewName;

		// Register the name association
		nameTable.put(dbInfo.name, viewName);

		// setup the adapter
		adapter.setSourceInfo(dbInfo);
		adapter.setDataSourceFactory(this);

		return adapter;
	}

	/**
	 * A partir de una instrucción select se encarga de obtener el DataSource
	 * resultado de la ejecución de dicha instrucción
	 *
	 * @param instr
	 *            Instrucción select origen del datasource
	 *
	 * @return DataSource que accede a los datos resultado de ejecutar la select
	 * @throws SemanticException
	 * @throws EvaluationException
	 * @throws ReadDriverException TODO
	 * @throws RuntimeException
	 *             bug
	 */
	private DataSource getDataSource(SelectAdapter instr)
			throws SemanticException, EvaluationException, ReadDriverException {
		DataSource[] tables = instr.getTables();

		// Estrategia de delegación de la instrucción en el sgbd original de la
		// tabla
		if (sameDBMS(tables) && delegating) {
			String sql = translateInstruction(instr, tables);

			DBDataSource table = (DBDataSource) tables[0];

			// Set the driver info
			DBSourceInfo source = (DBSourceInfo) table.getSourceInfo();
			String dataSourceName = addDBDataSourceBySQL(source.host,
					source.port, source.user, source.password, source.dbName,
					sql, source.driverName);

			try {
				return createRandomDataSource(dataSourceName,
						DataSourceFactory.MANUAL_OPENING);
			} catch (NoSuchTableException e) {
				throw new RuntimeException(e);
			} catch (DriverLoadException e) {
				throw new RuntimeException(e);
			}
		}

		// Estrategia normal
		Strategy strategy = StrategyManager.getStrategy(instr);

		OperationDataSource ret = strategy.select(instr);

		ret.setName(getUID());
		nameOperationDataSource.put(ret.getName(), ret);

		return ret;
	}

	/**
	 * Translates the table references by changind the gdbms name with the
	 * underlaying database management system table name
	 *
	 * @param instr
	 *            root of the adapted tree
	 * @param tables
	 *            DataSources involved in the instruction
	 *
	 * @return The translated sql query
	 * @throws SemanticException
	 *             If the instruction is not semantically correct
	 * @throws ReadDriverException TODO
	 */
	private String translateInstruction(Adapter instr, DataSource[] tables)
			throws SemanticException, ReadDriverException {
		HashMap instrNameDBName = new HashMap();

		translateFromTables(instr, instrNameDBName);
		translateColRefs(instr, instrNameDBName, tables);

		return Utilities.getText(instr.getEntity());
	}

	/**
	 * Gets the name of the table where the field is in
	 *
	 * @param fieldName
	 *            field whose table wants to be guessed
	 * @param tables
	 *            tables involved in the search
	 *
	 * @return table name
	 * @throws ReadDriverException TODO
	 * @throws SemanticException
	 */
	private String guessTableName(String fieldName, DataSource[] tables)
			throws ReadDriverException, SemanticException {
		int tableIndex = -1;

		for (int i = 0; i < tables.length; i++) {
			tables[i].start();

			if (tables[i].getFieldIndexByName(fieldName) != -1) {
				if (tableIndex != -1) {
					throw new SemanticException("ambiguous column reference: "
							+ fieldName);
				} else {
					tableIndex = i;
				}
			}

			tables[i].stop();
		}

		if (tableIndex == -1) {
			throw new SemanticException("Field not found: " + fieldName);
		}

		return tables[tableIndex].getName();
	}

	/**
	 * Translates the table references by changind the gdbms name with the
	 * underlaying database management system table name
	 *
	 * @param adapter
	 *            adapter processed
	 * @param instrNameDBName
	 *            hasmap with the gdbms names a s the keys and the database name
	 *            as the values.
	 * @param tables
	 *            tables involved in the instruction
	 * @throws SemanticException
	 *             If the instruction is not semantically correct
	 * @throws ReadDriverException TODO
	 */
	private void translateColRefs(Adapter adapter, HashMap instrNameDBName,
			DataSource[] tables) throws SemanticException, ReadDriverException {
		if (adapter instanceof ColRefAdapter) {
			ColRefAdapter tra = (ColRefAdapter) adapter;
			SimpleNode s = tra.getEntity();

			if (s.first_token != s.last_token) {
				String name = s.first_token.image;
				s.first_token.image = instrNameDBName.get(name).toString();
			} else {
				String tableName = guessTableName(s.first_token.image, tables);
				s.first_token.image = instrNameDBName.get(tableName) + "."
						+ s.first_token.image;
			}
		} else {
			Adapter[] hijos = adapter.getChilds();

			for (int i = 0; i < hijos.length; i++) {
				translateColRefs(hijos[i], instrNameDBName, tables);
			}
		}
	}

	/**
	 * Translates the table references by changind the gdbms name with the
	 * underlaying database management system table name
	 *
	 * @param adapter
	 *            adapter processed
	 * @param instrNameDBName
	 *            hasmap with the gdbms names a s the keys and the database name
	 *            as the values.
	 */
	private void translateFromTables(Adapter adapter, HashMap instrNameDBName) {
		if (adapter instanceof TableRefAdapter) {
			TableRefAdapter tra = (TableRefAdapter) adapter;
			SimpleNode s = tra.getEntity();

			if (s.first_token == s.last_token) {
				String alias = "gdbms" + System.currentTimeMillis();
				String name = s.first_token.image;
				s.first_token.image = nameTable.get(name) + " " + alias;
				instrNameDBName.put(name, alias);
			} else {
				String alias = s.last_token.image;
				String name = s.first_token.image;
				s.first_token.image = nameTable.get(name).toString();
				instrNameDBName.put(alias, alias);
			}
		} else {
			Adapter[] hijos = adapter.getChilds();

			for (int i = 0; i < hijos.length; i++) {
				translateFromTables(hijos[i], instrNameDBName);
			}
		}
	}

	/**
	 * Obtiene el DataSource resultado de ejecutar la instrucción de union
	 *
	 * @param instr
	 *            instrucción de union
	 * @param mode
	 *            opening mode
	 *
	 * @return DataSource
	 * @throws SemanticException
	 *             Si la instrucción tiene errores semánticos
	 * @throws EvaluationException
	 *             If there's any problem during expresion evaluation
	 * @throws ParseException
	 *             If there is a select statement embeeded in the union
	 *             statement and its parse fails
	 * @throws ReadDriverException TODO
	 */
	public DataSource createRandomDataSource(UnionAdapter instr, int mode)
			throws SemanticException,
			EvaluationException, ParseException, ReadDriverException {
		return getModedDataSource(getDataSource(instr), mode);
	}

	/**
	 * Obtiene el DataSource resultado de ejecutar la instrucción de union
	 *
	 * @param instr
	 *            instrucción de union
	 *
	 * @return DataSource
	 * @throws SemanticException
	 *             Si la instrucción tiene errores semánticos
	 * @throws ParseException
	 *             If there is a select statement embeeded in the union
	 *             statement and its parse fails
	 * @throws EvaluationException
	 *             If there's any problem during expresion evaluation
	 * @throws ReadDriverException TODO
	 */
	private DataSource getDataSource(UnionAdapter instr)
			throws SemanticException,
			ParseException, EvaluationException, ReadDriverException {
		try {
			Strategy strategy = StrategyManager.getStrategy(instr);

			OperationDataSource ret;
			ret = strategy.union(instr);

			ret.setName(getUID());
			nameOperationDataSource.put(ret.getName(), ret);

			return ret;
		} catch (DriverLoadException e) {
			throw new ReadDriverException("DataSourceFactory",e);
		}

	}

	/**
	 * Creates a DataSource as a result of a custom query
	 *
	 * @param instr
	 *            Root node of the adapter tree of the custom query instruction
	 * @param mode
	 *            opening mode
	 *
	 * @return DataSource with the custom query result
	 *
	 * @throws SemanticException
	 *             if there is any semantic error in the instruction
	 */
	public DataSource getDataSource(CustomAdapter instr, int mode)
			throws SemanticException {
		return getModedDataSource(getDataSource(instr), mode);
	}

	/**
	 * Creates a DataSource as a result of a custom query
	 *
	 * @param instr
	 *            Root node of the adapter tree of the custom query instruction
	 *
	 * @return DataSource with the custom query result
	 *
	 * @throws SemanticException
	 *             if there is any semantic error in the instruction
	 */
	private DataSource getDataSource(CustomAdapter instr)
			throws SemanticException {
		Strategy strategy = StrategyManager.getStrategy(instr);

		OperationDataSource ret = strategy.custom(instr);

		ret.setName(getUID());
		nameOperationDataSource.put(ret.getName(), ret);

		return ret;
	}

	/**
	 * Ejecuta la instrucción SQL que se pasa como parámetro obteniendo un
	 * DataSource con el resultado de la ejecución
	 *
	 * @param sql
	 *            instrucción sql que se quiere ejecutar
	 * @param mode
	 *            opening mode
	 *
	 * @return DataSource con el resultado
	 * @throws ParseException
	 *             Si se produce un error de parse de la instrucción
	 * @throws DriverLoadException
	 *             Si no se pueden cargar los drivers
	 * @throws SemanticException
	 *             Si la instrucción tiene algún error semántico
	 * @throws EvaluationException
	 *             If there's an error evaluating any expression
	 * @throws ReadDriverException TODO
	 */
	public DataSource executeSQL(String sql, int mode) throws ParseException,
			DriverLoadException, SemanticException,
			EvaluationException, ReadDriverException {
		ByteArrayInputStream bytes = new ByteArrayInputStream(sql.getBytes());
		SQLEngine parser = new SQLEngine(bytes);

		parser.SQLStatement();

		Node root = parser.getRootNode();
		Adapter rootAdapter = Utilities.buildTree(root.jjtGetChild(0), sql,
				this);

		Utilities.simplify(rootAdapter);

		DataSource result = null;

		if (rootAdapter instanceof SelectAdapter) {
			result = getDataSource((SelectAdapter) rootAdapter);
		} else if (rootAdapter instanceof UnionAdapter) {
			result = getDataSource((UnionAdapter) rootAdapter);
		} else if (rootAdapter instanceof CustomAdapter) {
			result = getDataSource((CustomAdapter) rootAdapter);
		}

		// if operation was delegated it isn't a OperationDataSource
		if (result instanceof OperationDataSource) {
			((OperationDataSource) result).setSQL(sql);
		}

		result.setDataSourceFactory(this);

		return getModedDataSource(result, mode);
	}

	/**
	 * Establece el DriverManager que se usará para instanciar DataSource's.
	 * Este metodo debe ser invocado antes que ningún otro
	 *
	 * @param dm
	 *            El manager que se encarga de cargar los drivers
	 */
	public void setDriverManager(DriverManager dm) {
		this.dm = dm;
	}
	/**
	 * Establece el WriterManager que se usará para instanciar DataSource's.
	 * Este metodo debe ser invocado antes que ningún otro
	 *
	 * @param dm
	 *            El manager que se encarga de cargar los drivers
	 */
	public void setWriterManager(WriterManager wm) {
		this.wm = wm;
	}
	/**
	 * Get's the module with the specified name
	 *
	 * @param name
	 *            name of the wanted module
	 *
	 * @return instance of the module
	 */
	public Object getModule(String name) {
		return ms.getModule(name);
	}

	/**
	 * Registers a module in the system with the specified name
	 *
	 * @param name
	 *            name of the module
	 * @param instance
	 *            module instance
	 */
	public void registerModule(String name, Object instance) {
		ms.registerModule(name, instance);
	}

	/**
	 * Gets a driver manager reference
	 *
	 * @return DriverManagers.
	 */
	public DriverManager getDriverManager() {
		return dm;
	}
	/**
	 * Gets a writer manager reference
	 *
	 * @return WriterManagers.
	 */
	public WriterManager getWriterManager() {
		return wm;
	}
	/**
	 * Sets if this factory will check for delegating instructions at the server
	 * (true) or will execute all queries internally (false). By delegating at
	 * the server, lots of queries will be defined in the database management
	 * system where the execution is delegated. Invoke clearViews to remove all
	 * created views.
	 *
	 * @param b
	 */
	public void setDelegating(boolean b) {
		this.delegating = b;
	}

	/**
	 * Creates a new table on the specified database
	 *
	 * @param database
	 *            name of the database where the table will be created
	 * @param pkNames
	 *            Names of the primary key fields
	 * @param names
	 *            names of the fields
	 * @param types
	 *            types of the fields. Must have the same length than names
	 *
	 * @return the table name
	 *
	 * @throws SQLException
	 *             if the creation fails
	 */
	public String createTable(String database, String[] pkNames,
			String[] names, int[] types) throws SQLException {
		// Get a name for the table
		String tableName = getUID();

		// Create the table
		InnerDBUtils.execute(database, InnerDBUtils.getCreateStatementWithPK(
				tableName, pkNames, names, types));

		return tableName;
	}

	/**
	 * Frees all resources used during execution
	 * @throws SQLException
	 *             If cannot free internal resources
	 * @throws ReadDriverException TODO
	 */
	public void finalizeThis()  throws SQLException, ReadDriverException {

		try {
			clearViews();
		} finally {
			Connection c = null;
			try {
				c = java.sql.DriverManager.getConnection(
						"jdbc:hsqldb:file:", "", "");
			} catch (Exception e) {
				return;
			}
			Statement st = c.createStatement();
			st.execute("SHUTDOWN");
			st.close();
			c.close();
		}

	}

	/**
	 * Initializes the system.
	 *
	 * @throws InitializationException
	 *             If the initialization
	 */
	public void initialize() throws InitializationException {
		initialize(".");
	}

	/**
	 * Initializes the system
	 *
	 * @param tempDir
	 *            temporary directory to write data
	 *
	 * @throws InitializationException
	 *             If the initialization fails
	 */
	public void initialize(String tempDir) throws InitializationException {
		try {
			this.tempDir = new File(tempDir);

			if (!this.tempDir.exists()) {
				this.tempDir.mkdirs();
			}

			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
			throw new InitializationException(e);
		}
		fillDriversNamesAliases();
	}

	/**
	 * Gets the URL of a file in the temporary directory. Does not creates any
	 * file
	 *
	 * @return String
	 */
	public String getTempFile() {
		return tempDir.getAbsolutePath() + File.separator + "gdmbs"
				+ System.currentTimeMillis();
	}

	/**
	 * Registra alias de nombres de los drivers
	 * por si ha sido necesario modificar el nombre
	 * de alguno, y se necesita compatibilidad
	 *
	 */
	private void fillDriversNamesAliases() {
		this.driversNamesAliases.put("GDBMS HSQLDB driver", "HSQL");
		this.driversNamesAliases.put("mysql", "MySQL Alphanumeric");
		this.driversNamesAliases.put("odbc", "ODBC");
		this.driversNamesAliases.put("mysql", "MySQL Alphanumeric");
		this.driversNamesAliases.put("oracle", "Oracle Alphanumeric");
		this.driversNamesAliases.put("postgresql", "PostgreSQL Alphanumeric");
	}

	private Driver getDriver(String name) throws DriverLoadException {
		if (this.driversNamesAliases.containsKey(name)) {
			name = (String)this.driversNamesAliases.get(name);
		}
		return this.dm.getDriver(name);

	}

	/**
	 * Information to delete the view on the server: name of the view and the
	 * adapter to remove it
	 */
	private class ServerViewInfo {
		public DBDataSource adapter;

		public String viewName;

		/**
		 * Crea un nuevo ServerViewInfo.
		 *
		 * @param ds
		 *            DOCUMENT ME!
		 * @param name
		 *            DOCUMENT ME!
		 */
		public ServerViewInfo(DBDataSource ds, String name) {
			adapter = ds;
			viewName = name;
		}
	}

}
