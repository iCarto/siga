package com.hardcode.gdbms.engine.data.driver;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 */
public interface SpatialDBDriver extends DBDriver, SpatialDriver{

    /**
     * Ejecuta la instruccion que se pasa como par�metro en el gestor de base
     * de datos en el que est� conectado el driver. Los nombres de las tablas
     * de la instrucci�n est�n preparados para el sistema de gesti�n donde se
     * ejecutar� la instrucci�n
     *
     * @param con Conexi�n con la cual se ha de obtener el ResultSet
     * @param sql Instrucci�n SQL a ejecutar
     * @param props Properties of the overlaying DataSource layer
     *
     * @throws SQLException Si se produce un error ejecutando la instrucci�n
     *         SQL public void openTable(Connection con, String table,
     *         HasProperties props) throws SQLException;
     */
    public void open(Connection con, String sql, String tableName, String geomFieldName)
        throws SQLException;

}
