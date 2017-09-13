package es.icarto.gvsig.extgia.forms.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.ImageIcon;

import es.icarto.gvsig.commons.utils.ImageUtils;

public class ImagesDAO {

    private static final String IMAGE_FIELDNAME = "image";
    private final Connection connection;
    private final String schema;
    private final String tablename;
    private final String pkField;

    public ImagesDAO(Connection connection, String schema, String tablename, String pkField) {
        this.connection = connection;
        this.schema = schema;
        this.tablename = tablename;
        this.pkField = pkField;
    }

    private void executeInsertOrUpdate(String pkValue, BufferedImage image, String query, String orgName,
            String filepath) throws IOException, SQLException {
        PreparedStatement statement = null;
        try {
            byte[] imageBytes = ImageUtils.convertImageToBytea(image);
            statement = connection.prepareStatement(query);
            statement.setBytes(1, imageBytes);
            if (getPKFieldType() == 4) {
                statement.setInt(2, Integer.parseInt(pkValue));
            } else {
                statement.setString(2, pkValue);
            }
            statement.executeUpdate();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    public void insertImageIntoDb(String pkValue, BufferedImage image, String orgName, String filepath)
            throws SQLException, IOException {
        String query_tmpl = "INSERT INTO %s.%s (%s, %s, org_name, filepath) VALUES (?, ?, '%s', '%s')";
        String query = String.format(query_tmpl, schema, tablename, IMAGE_FIELDNAME, pkField, orgName, filepath);
        executeInsertOrUpdate(pkValue, image, query, orgName, filepath);
    }

    public void updateImageIntoDb(String pkValue, BufferedImage image, String orgName, String filepath)
            throws SQLException, IOException {
        String query_tmpl = "UPDATE %s.%s SET %s = ?, org_name = '%s', filepath = '%s' WHERE %s = ?";
        String query = String.format(query_tmpl, schema, tablename, IMAGE_FIELDNAME, orgName, filepath, pkField);
        executeInsertOrUpdate(pkValue, image, query, orgName, filepath);
    }

    public byte[] readImageFromDb(String pkValue) throws SQLException {
        PreparedStatement statement = null;
        if (schema == null || tablename == null || pkField == null || pkValue == null || pkValue.isEmpty()) {
            return null;
        }
        try {
            statement = connection.prepareStatement("SELECT " + IMAGE_FIELDNAME + " FROM " + schema + "." + tablename
                    + " WHERE " + pkField + " = ?");
            if (getPKFieldType() == 4) {
                statement.setInt(1, Integer.parseInt(pkValue));
            } else {
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

    /*
     * returns true if the image was successfully saved
     */
    public void saveDbImageOnDisk(String pkValue, File path) throws SQLException, IOException {
        byte[] bytes;

        bytes = readImageFromDb(pkValue);
        ImageUtils.byteaToFile(bytes, path);
    }

    public ImageIcon getImageIconFromDb(String pkValue) throws SQLException {
        byte[] elementImageBytes = readImageFromDb(pkValue);
        if (elementImageBytes == null) {
            return null;
        }
        BufferedImage elementImage = ImageUtils.convertByteaToImage(elementImageBytes);
        ImageIcon elementIcon = new ImageIcon(elementImage);
        return elementIcon;
    }

    public void deleteImageFromDb(String pkValue) throws SQLException {
        PreparedStatement statement = null;
        if (schema == null || tablename == null || pkField == null || pkValue == null || pkValue.isEmpty()) {
            return;
        }
        try {
            statement = connection.prepareStatement("DELETE FROM " + schema + "." + tablename + " WHERE " + pkField
                    + " = ?");
            if (getPKFieldType() == 4) {
                statement.setInt(1, Integer.parseInt(pkValue));
            } else {
                statement.setString(1, pkValue);
            }
            statement.execute();
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    private int getPKFieldType() {
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement("SELECT " + pkField + " FROM " + schema + "." + tablename);
            ResultSet rs = statement.executeQuery();
            ResultSetMetaData metadata = rs.getMetaData();
            return metadata.getColumnType(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public File getFileOnDisk(String pkValue) throws SQLException {
        PreparedStatement statement = null;
        if (schema == null || tablename == null || pkField == null || pkValue == null || pkValue.isEmpty()) {
            return null;
        }
        try {
            String query_tmpl = "SELECT filepath FROM %s.%s WHERE %s = ?";
            String query = String.format(query_tmpl, schema, tablename, pkField);
            statement = connection.prepareStatement(query);
            if (getPKFieldType() == 4) {
                statement.setInt(1, Integer.parseInt(pkValue));
            } else {
                statement.setString(1, pkValue);
            }
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String filepath = rs.getString(1);
                if (filepath != null) {
                    return new File(filepath);
                }

            } else {
                return null;
            }
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return null;
    }

}
