package es.icarto.gvsig.siga.models;

import es.icarto.gvsig.commons.db.ConnectionWrapper;
import es.icarto.gvsig.commons.gui.tables.NotEditableTableModel;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class CurrentUser {

    private final NotEditableTableModel data;
    private static String TABLE = "audasa_expedientes.usuarios";

    public CurrentUser() {
	data = new NotEditableTableModel();
	if (DBSession.isActive()) {
	    initFromDB();
	}
    }

    private void initFromDB() {
	final DBSession dbs = DBSession.getCurrentSession();
	String query = String.format(
		"SELECT COALESCE(area,'') FROM %s WHERE name = '%s';", TABLE,
		dbs.getUserName());
	ConnectionWrapper con = new ConnectionWrapper(dbs.getJavaConnection());
	con.execute(query, data);
    }

    public String getArea() {
	return data.getValueAt(0, 0).toString();
    }
}
