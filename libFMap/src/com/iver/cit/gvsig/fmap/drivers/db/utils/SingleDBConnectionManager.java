package com.iver.cit.gvsig.fmap.drivers.db.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.hardcode.driverManager.Driver;
import com.hardcode.gdbms.engine.data.driver.AlphanumericDBDriver;
import com.iver.cit.gvsig.fmap.drivers.ConnectionFactory;
import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.drivers.IConnection;
import com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;

/**
 * Utility class to handle connections properly. One connection instance per
 * (db, user, gvsig session)
 *
 * @author jldominguez
 *
 */
public class SingleDBConnectionManager {

	private static Logger logger = Logger.getLogger(SingleDBConnectionManager.class.getName());
	private static SingleDBConnectionManager single_instance = null;
	private HashMap connections = new HashMap();

	/**
	 * Non-public to avoid unwanted instances.
	 *
	 */
	protected SingleDBConnectionManager() {

	}

	/**
	 * Singleton model to keep single instances.
	 *
	 * @return single instance
	 */
	public static SingleDBConnectionManager instance() {
		if (single_instance == null) {
			single_instance = new SingleDBConnectionManager();
		}
		return single_instance;
	}

	/**
	 * Utility metho to find a connection with its parameters
	 * given the connection object.
	 *
	 * @param co the connection object
	 * @return
	 */
	public ConnectionWithParams findConnection(IConnection co) {

		Iterator iter = connections.keySet().iterator();
		while (iter.hasNext()) {
			String keyitem = (String) iter.next();
			ConnectionWithParams cwp = (ConnectionWithParams) connections.get(keyitem);
			if (cwp.getConnection() == co) {
				return cwp;
			}
		}
		return null;
	}


	/**
	 * Creates a new connection with its parameters if not created yet.
	 *
	 * @param _drvName driver name
	 * @param _user user name
	 * @param _pw password
	 * @param _name connection name
	 * @param _host host url
	 * @param _port port number as string
	 * @param _db database name
	 * @param _schema schema name
	 * @param _connected whether or not to connect the connection
	 * @return the connection with parameters object
	 * @throws SQLException
	 */
	public ConnectionWithParams getConnection (
			String _drvName,
			String _user,
			String _pw,
			String _name,
			String _host,
			String _port,
			String _db,
			String _schema,
			boolean _connected
			) throws DBException {

		// IVectorialDatabaseDriver drv = 
		Driver _drv = getInstanceFromName(_drvName);
		
		if (_drv==null)return null;
		
		String conn_str = getConnStringForDriver(_drv, _host, _port, _db, _user, _pw);
		String key = getConnectionKey(_drvName, _host, _db, _port, _user);

		if (!connections.containsKey(key)) {

			ConnectionWithParams cwp = null;

			if (_connected) {
				IConnection new_connection;
				
				try {
					new_connection = getConnectionForDriver(_drv, conn_str, _host, _port, _db, _user, _pw);
				} catch (Exception e) {
					throw new DBException(e);
				}

				cwp = new ConnectionWithParams(
						conn_str,
						new_connection,
						_drvName,
						_user,
						_pw,
						_name,
						_host,
						_port,
						_db,
						_schema,
						true);
			} else {

				cwp = new ConnectionWithParams(
						conn_str,
						null,
						_drvName,
						_user,
						null,
						_name,
						_host,
						_port,
						_db,
						_schema,
						false);
			}
			connections.put(key, cwp);
		}

		ConnectionWithParams _cwp = (ConnectionWithParams) connections.get(key);

		if (_cwp.getName().compareTo(_name) != 0) {
			// connections.remove(key);
			_cwp.setName(_name);
			connections.put(key, _cwp);
		}

		if ((!_cwp.isConnected()) && (_connected)) {
			_cwp.connect(_pw);
		}

		return _cwp;
	}

	
	public static IConnection getConnectionForDriver(Driver _drv, String conn_str,
			String _host, String _port, String _db, String _user, String _pw) throws Exception {

		IConnection resp = null;
		
		int intport = 0;
		
		if (_drv instanceof AlphanumericDBDriver) {
			
			try {
				intport = Integer.parseInt(_port);
			} catch (Exception ex) {
				// throw new DBException(ex);
				// With ODBC, we may not choose a port, so, this error shouldn't be thrown.
				System.out.println(_drv.getName() + " port parameter should be an Integer??" + _port);
				intport = 0;
			}
			AlphanumericDBDriver alpha_drv = (AlphanumericDBDriver) _drv;
			Connection _c_ = alpha_drv.getConnection(_host, intport, _db, _user, _pw);
			ConnectionJDBC resp2 = new ConnectionJDBC();
			resp2.setDataConnection(_c_, _user, _pw);
			resp = resp2;
			
		} else {
			
			if (_drv instanceof IVectorialDatabaseDriver) {
				IVectorialDatabaseDriver geo_driver = (IVectorialDatabaseDriver) _drv;
				resp = ConnectionFactory.createConnection(conn_str, _user, _pw);
			} else {
				logger.error("Did not create IConnection, Unexpected driver: " + _drv.getClass().getName());
			}
		}
		return resp;
	}

	private String getConnStringForDriver(
			Driver _drv,
			String _host,
			String _port,
			String _db,
			String _user,
			String _pw) {

		
		if (_drv instanceof AlphanumericDBDriver) {
			logger.error("Returned null conn str (OK) for: " + _drv.getClass().getName());
			return null;
		} else {
			if (_drv instanceof IVectorialDatabaseDriver) {
				IVectorialDatabaseDriver geo_driver = (IVectorialDatabaseDriver) _drv;
				return geo_driver.getConnectionString(_host, _port, _db, _user, _pw);
			} else {
				logger.error("Unexpected driver type: " + _drv.getClass().getName());
				return null;
			}
		}
	}

//	/**
//	 * Creates a new connection with its parameters if not created yet.
//	 *
//	 * @param _drvName driver name
//	 * @param _user user name
//	 * @param _pw password
//	 * @param _name connection name
//	 * @param _host host url
//	 * @param _port port number as string
//	 * @param _db database name
//	 * @param _connected whether or not to connect the connection
//	 * @return the connection with parameters object
//	 * @throws SQLException
//	 */
//	public ConnectionWithParams getConnection (
//			String _drvName,
//			String _user,
//			String _pw,
//			String _name,
//			String _host,
//			String _port,
//			String _db,
//			boolean _connected
//			) throws DBException {
//
//		return getConnection(
//				_drvName,
//				_user,
//				_pw,
//				_name,
//				_host,
//				_port,
//				_db,
//				"",
//				_connected);
//	}
	
	/**
	 * Gets available open connections.
	 *
	 * @return array of open connections with parameters
	 */
	public ConnectionWithParams[] getConnectedConnections() {
		Iterator iter = connections.keySet().iterator();
		if (!iter.hasNext()) return null;

		ArrayList aux = new ArrayList();

		while (iter.hasNext()) {
			ConnectionWithParams _cwp =
				(ConnectionWithParams) connections.get(iter.next());
			if (_cwp.isConnected()) {
				aux.add(_cwp);
			}
		}

		ConnectionWithParams[] resp = new ConnectionWithParams[aux.size()];
		for (int i=0; i<aux.size(); i++) {
			resp[i] = (ConnectionWithParams) aux.get(i);
		}
		return resp;
	}

	/**
	 * Gets all available connections.
	 *
	 * @return array of all connections with parameters
	 */
	public ConnectionWithParams[] getAllConnections() {
		Iterator iter = connections.keySet().iterator();
		if (!iter.hasNext()) return null;

		ArrayList aux = new ArrayList();

		while (iter.hasNext()) {
			ConnectionWithParams _cwp =
				(ConnectionWithParams) connections.get(iter.next());
			aux.add(_cwp);
		}

		ConnectionWithParams[] resp = new ConnectionWithParams[aux.size()];
		for (int i=0; i<aux.size(); i++) {
			resp[i] = (ConnectionWithParams) aux.get(i);
		}
		return resp;
	}

	/**
	 * Removes connection with its params.
	 *
	 * @param _cwp connection with params to be removed
	 */
	private void removeConnectionWP(ConnectionWithParams _cwp) {

		ArrayList keysToRemove = new ArrayList();

		Iterator iter = connections.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			ConnectionWithParams cwp = (ConnectionWithParams) connections.get(key);
			if (cwp == _cwp) {
				keysToRemove.add(key);
			}
		}
		for (int i=0; i<keysToRemove.size(); i++) {
			connections.remove(keysToRemove.get(i));
		}
	}


	/**
	 * Closes and removes a connection with params object
	 *
	 * @param _cwp
	 * @return whether the connection was actually closed (false if the
	 * connection was not open at the start)
	 */
	public boolean closeAndRemove(ConnectionWithParams _cwp) {

		boolean it_was_open = true;

		try {
			it_was_open = (_cwp.getConnection() != null) && (!_cwp.getConnection().isClosed());
			if (_cwp.getConnection() != null) _cwp.getConnection().close();
			removeConnectionWP(_cwp);
		} catch (Exception se) {
			logger.error("While closing connection: " + se.getMessage(), se);
			return false;
		}
		logger.info("Connection successfully closed.");
		return it_was_open;
	}

	/**
	 * Called by the extension object when gvsig terminates.
	 *
	 */
	public void closeAllBeforeTerminate() {

		boolean ok = true;
		String key = "";
		ConnectionWithParams cwp = null;
		Iterator iter = connections.keySet().iterator();
		while (iter.hasNext()) {
			key = (String) iter.next();
			cwp = (ConnectionWithParams) connections.get(key);

			if (cwp.getConnection() == null) continue;

			try {
				cwp.getConnection().close();
			} catch (DBException se) {
				ok = false;
				logger.error("While closing connection: " + se.getMessage(), se);
			}
		}

		connections.clear();

		if (ok) {
			logger.info("Successfully closed all connections.");
		} else {
			logger.warn("Problems while closing all connections.");
		}
	}

	/**
	 * Gets the objects key to be used in the inner hashmap
	 * @param _drvName driver name
	 * @param _host host's url
	 * @param _db database name
	 * @param _port port number
	 * @param _user user name
	 * @return
	 */
	private static String getConnectionKey(
			String _drvName,
			String _host,
			String _db,
			String _port, String _user) {

		String resp = "_driver_" + _drvName.toLowerCase();
		resp = resp + "_host_" + _host.toLowerCase();
		resp = resp + "_db_" + _db.toLowerCase(); // ---------------- nueva
		resp = resp + "_port_" + _port;
		resp = resp + "_user_" + _user.toLowerCase();
		return resp;
	}

	/**
	 * Utility method to instantiate a driver given its name.
	 *
	 * @param drvname driver name
	 * @return driver instance
	 */
	public static Driver getInstanceFromName(String drvname) {

		// IVectorialDatabaseDriver _driver = null;
		Driver _driver = null;
        try {
            _driver = LayerFactory.getDM().getDriver(drvname);
        } catch (Exception e) {
        	logger.error("While getting driver (" + drvname + ") instance: " + e.getMessage());
        	return null;
        }
        return _driver;
	}
}

// [eiel-gestion-conexiones]