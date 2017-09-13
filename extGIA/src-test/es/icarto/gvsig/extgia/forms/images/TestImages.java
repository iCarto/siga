package es.icarto.gvsig.extgia.forms.images;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Test;

import es.icarto.gvsig.commons.utils.ImageUtils;

public class TestImages {

    private static final String PKVALUE = "C-002N";
    private static final String PKFIELD = "id_talud";
    private static final String TABLENAME = "taludes_imagenes";
    private static final String SCHEMA = "audasa_extgia";

    Connection connection;
    ImagesDAO dao;

    @Before
    public void doSetupImage() {
        String url = "jdbc:postgresql://localhost:5434/audasa_test";
        String user = "postgres";
        String passwd = "postgres";
        // postgresql-9.1-903.jdbc3.jar needs to be in the classpasth before th
        // other gvSIG jars related to pgsql.
        // Configure that in your classpath tab if you use eclipse
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, passwd);
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        dao = new ImagesDAO(connection, SCHEMA, TABLENAME, PKFIELD);
    }

    @Test
    public void testingInsertAndReadImage() throws Exception {

        try {
            File fileImage = new File("data-test/test.jpg");
            BufferedImage image = ImageIO.read(fileImage);
            String query = "DELETE FROM audasa_extgia.taludes_imagenes";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
            connection.commit();

            dao.insertImageIntoDb(PKVALUE, image, "foo", "foo");
            byte[] imageDbBytes = dao.readImageFromDb(PKVALUE);

            byte[] imageMockBytes = ImageUtils.convertImageToBytea(image);

            assertTrue(Arrays.equals(imageDbBytes, imageMockBytes));
        } finally {
            connection.rollback();
        }
    }

    @Test
    public void testingUpdateAndReadImage() throws Exception {

        try {
            File fileImage = new File("data-test/test.jpg");
            BufferedImage image = ImageIO.read(fileImage);
            String query = "DELETE FROM audasa_extgia.taludes_imagenes";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
            connection.commit();

            dao.insertImageIntoDb(PKVALUE, image, "foo", "foo");
            File fileImageToUpdate = new File("data-test/test2.jpg");
            BufferedImage imageToUpdate = ImageIO.read(fileImageToUpdate);
            dao.updateImageIntoDb(PKVALUE, imageToUpdate, "bar", "bar");

            byte[] imageDbBytes = dao.readImageFromDb(PKVALUE);

            byte[] imageMockBytes = ImageUtils.convertImageToBytea(imageToUpdate);

            assertTrue(Arrays.equals(imageDbBytes, imageMockBytes));
        } finally {
            connection.rollback();
        }
    }

    @Test
    public void testingDeleteImage() throws Exception {
        try {
            dao.deleteImageFromDb(PKVALUE);
            byte[] image = dao.readImageFromDb(PKVALUE);
            assertNull(image);
        } finally {
            connection.rollback();
        }
    }

}
