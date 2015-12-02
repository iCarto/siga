package es.icarto.gvsig.extgia.forms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class CalculateDBForeignValue implements CalculateForeignValue {

    protected final Map<String, String> foreingKey;
    protected final String componentName;
    protected final String foreignField;
    protected final String tableName;
    protected final String idField;

    public CalculateDBForeignValue(Map<String, String> foreingKey,
	    String componentName, String foreignField, String tableName,
	    String idField) {
	this.foreingKey = foreingKey;
	this.componentName = componentName;
	this.foreignField = foreignField;
	this.tableName = tableName;
	this.idField = idField;
    }

    protected String getSQLSentence() {
	return "SELECT " + foreignField + " FROM " + DBFieldNames.GIA_SCHEMA
		+ "." + tableName + " WHERE " + idField + " = " + "'"
		+ foreingKey.get(idField) + "'";
    }

    private String getValue() {
	try {
	    Connection connection = DBSession.getCurrentSession()
		    .getJavaConnection();
	    PreparedStatement statement;
	    statement = connection.prepareStatement(getSQLSentence());
	    ResultSet rs = statement.executeQuery();
	    if (rs.next()) {
		if (rs.getString(1) != null) {
		    if (rs.getString(1).contains(".")) {
			return rs.getString(1).replace(".", ",");
		    } else {
			return rs.getString(1);
		    }
		}
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Override
    public String getComponentName() {
	return componentName;
    }

    @Override
    public ForeignValue getForeignValue() {
	final String value = getValue() == null ? "" : getValue();
	return new ForeignValue(componentName, value);
    }

}
