package es.icarto.gvsig.siga.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.table.TableModel;

import es.icarto.gvsig.commons.db.ConnectionWrapper;
import es.icarto.gvsig.commons.utils.Field;
import es.udc.cartolab.gvsig.elle.gui.wizard.load.MunicipiosTableModel;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class MunicipioConstantes {

    private static final String MUNICIPIO_CONSTANTS_TABLENAME = "elle.municipio_constantes";
    private final MunicipiosTableModel data;

    private final CurrentUser user;

    public MunicipioConstantes(CurrentUser user) {
	data = new MunicipiosTableModel();

	this.user = user;

	if (DBSession.isActive()) {
	    initFromDB();
	}
    }

    private void initFromDB() {
	ConnectionWrapper con = new ConnectionWrapper(DBSession
		.getCurrentSession().getJavaConnection());
	String area = user.getArea();
	if (area.isEmpty()) {
	    return;
	}
	String where = String.format(" WHERE area IN ('%s', 'Ambas') ", area);
	if (area.equalsIgnoreCase("ambas")) {
	    where = "";
	}
	String query = String
		.format("SELECT id as \"Código INE\", municipio as \"Municipio\", '' as \"Descripción\", '<span style=\"color:blue\">' || ap || '</span>' as \"AP\", '<span style=\"color:green\">' || ag || '</span>' as \"AG\" FROM %s %s ORDER BY orden;",
			MUNICIPIO_CONSTANTS_TABLENAME, where);
	con.execute(query, data);
    }

    public TableModel getAsTableModel() {
	return data;
    }

    public Collection<String> getAsIds() {
	Collection<String> ids = new ArrayList<String>();
	for (int i = 0; i < data.getRowCount(); i++) {
	    ids.add(data.getValueAt(i, 0).toString());
	}
	return ids;
    }

    public Collection<Field> getAsFields() {
	Collection<Field> values = new ArrayList<Field>();
	for (int i = 0; i < data.getRowCount(); i++) {
	    String id = data.getValueAt(i, 0).toString();
	    String name = data.getValueAt(i, 1).toString();
	    values.add(new Field(id, name));

	}
	return values;
    }

    public String getNombreMunicipioById(String id) {
	String query = "SELECT item FROM " + MUNICIPIO_CONSTANTS_TABLENAME
		+ " WHERE id =" + "'" + id + "'" + ";";
	PreparedStatement statement;
	try {
	    DBSession dbs = DBSession.getCurrentSession();
	    statement = dbs.getJavaConnection().prepareStatement(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    rs.first();
	    return rs.getString(1);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return null;
    }

}
