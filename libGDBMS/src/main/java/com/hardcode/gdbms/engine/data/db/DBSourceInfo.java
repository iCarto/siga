package com.hardcode.gdbms.engine.data.db;

import java.sql.Connection;

import com.hardcode.gdbms.engine.data.SourceInfo;


/**
 * Informaci�n del driver de base de datos
 *
 * @author Fernando Gonz�lez Cort�s
 */
public abstract class DBSourceInfo extends SourceInfo {
    public String host;
    public int port;
    public String dbName;
    public String user;
    public String password;
    public String dbms;
    public Connection connection;
}
