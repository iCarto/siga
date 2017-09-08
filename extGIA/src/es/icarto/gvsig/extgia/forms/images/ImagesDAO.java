package es.icarto.gvsig.extgia.forms.images;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import es.icarto.gvsig.commons.utils.ImageUtils;


public class ImagesDAO {

    private static final String IMAGE_FIELDNAME = "image";

    public void insertImageIntoDb(Connection connection, String schema, String tablename,
	    String pkField, String pkValue, BufferedImage image, boolean update) throws SQLException, IOException {
	if (schema == null || tablename == null || pkField == null || pkValue == null
		|| pkValue.isEmpty() || image == null) {
	    return;
	}
	byte[] imageBytes = ImageUtils.convertImageToBytea(image);
	PreparedStatement statement;
	if (update) {
	    statement = connection.prepareStatement("UPDATE "
		    + schema + "."
		    + tablename + " SET " + IMAGE_FIELDNAME
		    + " = " + "? WHERE " + pkField
		    + " = ?");
	    statement.setBytes(1, imageBytes);
	    if (getPKFieldType(connection, schema, tablename, pkField) ==  4)  {
		statement.setInt(2, Integer.parseInt(pkValue));
	    }else {
		statement.setString(2, pkValue);
	    }
	} else {
	    statement = connection.prepareStatement("INSERT INTO "
		    + schema + "."
		    + tablename + " VALUES (?, ?)");
	    if (getPKFieldType(connection, schema, tablename, pkField) ==  4)  {
		statement.setInt(1, Integer.parseInt(pkValue));
	    }else {
		statement.setString(1, pkValue);
	    }
	    statement.setBytes(2, imageBytes);
	}
	statement.executeUpdate();
	if (!connection.getAutoCommit()) {
	    connection.commit();
	}
	statement.close();
    }


    public byte[] readImageFromDb(Connection connection, String schema, String tablename,
	    String pkField, String pkValue) throws SQLException {
	PreparedStatement statement = null;
	if (schema == null || tablename == null || pkField == null || pkValue == null
		|| pkValue.isEmpty()) {
	    return null;
	}
	try {
	    statement = connection.prepareStatement("SELECT "
		    + IMAGE_FIELDNAME + " FROM "
		    + schema + "." + tablename
		    + " WHERE " + pkField + " = ?");
	    if (getPKFieldType(connection, schema, tablename, pkField) ==  4)  {
		statement.setInt(1, Integer.parseInt(pkValue));
	    }else {
		statement.setString(1, pkValue);
	    }
	    ResultSet rs = statement.executeQuery();
	    if (rs.next()) {
		return rs.getBytes(1);
	    } else {
		return null;
	    }
	} finally {
	    if (statement != null) {
		statement.close();
	    }
	}
    }

    public void deleteImageFromDb(Connection connection, String schema, String tablename,
	    String pkField, String pkValue) throws SQLException {
	PreparedStatement statement = null;
	if (schema == null || tablename == null || pkField == null || pkValue == null
		|| pkValue.isEmpty()) {
	    return;
	}
	try {
	    statement = connection.prepareStatement("DELETE FROM "
		    + schema + "." + tablename
		    + " WHERE " + pkField + " = ?");
	    if (getPKFieldType(connection, schema, tablename, pkField) ==  4)  {
		statement.setInt(1, Integer.parseInt(pkValue));
	    }else {
		statement.setString(1, pkValue);
	    }
	    statement.execute();
	} finally {
	    if (statement != null) {
		statement.close();
	    }
	}
    }

    private int getPKFieldType(Connection connection, String schema, String tablename,
	    String pkField) {
	PreparedStatement statement;
	try {
	    statement = connection.prepareStatement("SELECT "
		    + pkField + " FROM "
		    + schema + "." + tablename);
	    ResultSet rs = statement.executeQuery();
	    ResultSetMetaData metadata = rs.getMetaData();
	    return metadata.getColumnType(1);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return -1;
    }
}
