package com.hardcode.gdbms.engine.data.driver;

import java.sql.Connection;
import java.sql.SQLException;

import com.hardcode.gdbms.driver.exceptions.OpenDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;

/**
 *
 */
public interface AlphanumericDBDriver extends DBDriver{

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
     * @throws OpenDriverException
     */
    public void open(Connection con, String sql)
        throws SQLException, OpenDriverException;

    /**
     * Devuelve el "esquema" del origen de datos subyacente al driver.
     * (azabala).
     * Es necesario porque determinados drivers de escritura
     * (los de bbdd que no soportan resultsets updatables) necesitan conocer
     * el esquema de la tabla subyacente (nombre, p. ej) para poder construir
     * al vuelo la consulta SQL final.
     * @throws ReadDriverException TODO
     * */
    public ITableDefinition getTableDefinition() throws ReadDriverException;
}
