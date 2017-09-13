package es.icarto.gvsig.extgia.forms.images;

import java.awt.Component;
import java.io.File;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;

import es.icarto.gvsig.commons.imagefilechooser.ImageFileChooser;

// package protected
class WorkFlow {

    private static final Logger logger = Logger.getLogger(WorkFlow.class);
    private final ImagesDAO dao;
    private final String pkValue;

    public final boolean oldImageInDB;
    public final boolean oldImageOnDisk;
    public final boolean sameFilenameOnDisk;
    private final String destFolder;
    private static File initFile = new File(System.getProperty("user.home"));
    private final File fileImage;

    public WorkFlow(ImagesDAO dao, String pkValue, String destFolder) {
        this.dao = dao;
        this.pkValue = pkValue;
        this.destFolder = destFolder;

        final ImageFileChooser fileChooser = new ImageFileChooser(initFile);

        File file = fileChooser.showDialog();

        if (file == null) {
            fileImage = null;
        } else if (!file.exists() || !fileChooser.accept(file)) {
            showWarning("La imagen seleccionada no es válida");
            fileImage = null;
        } else {
            fileImage = file;
            initFile = fileImage;
        }

        oldImageInDB = oldImageInDB();
        oldImageOnDisk = oldImageOnDisk();
        sameFilenameOnDisk = sameFilenameOnDisk();
    }

    /*
     * This pk already has an associated image in the db
     */
    private boolean oldImageInDB() {

        try {
            ImageIcon elementIcon = dao.getImageIconFromDb(pkValue);
            return elementIcon != null;
        } catch (SQLException e) {
            logger.error(e.getStackTrace(), e);
        }
        return false;
    }

    /*
     * This pk already has an associated image on disk
     */
    private boolean oldImageOnDisk() {
        try {
            return dao.getFileOnDisk(pkValue) != null;
        } catch (SQLException e) {
            logger.error(e.getStackTrace(), e);
        }
        return false;
    }

    /*
     * There is a file with the same name on the disk already
     */
    private boolean sameFilenameOnDisk() {
        if (fileImage == null) {
            return false;
        }
        String name = fileImage.getName();
        File destFile = new File(destFolder + File.separator + name);
        return destFile.isFile();
    }

    private boolean destFolderExists() {
        if (fileImage == null) {
            return false;
        }
        return new File(destFolder).isDirectory();
    }

    private String generateMsg() {

        String msg = null;

        if (oldImageInDB && oldImageOnDisk) {
            msg = "Ya existe una imagen en la base de datos y en el disco para este elemento.\nSi continúa la imagen antigua será elminada de la base de datos y del disco.";
        } else if (oldImageInDB && sameFilenameOnDisk) {
            msg = "Ya existe una imagen en la base de datos para este elemento, y una imagen en disco con el mismo nombre que la seleccionada.\nSi continúa las imagenes antiguas serán eliminadas de la base de datos y del disco.";
        } else if (oldImageInDB) {
            msg = "Ya existe una imagen en la base de datos para este elemento.\nSi continúa la imagen será eliminada.";
        } else if (sameFilenameOnDisk) {
            msg = "Ya existe una imagen en disco con ese nombre.\nSi continúa la imagen antigua será eliminada";
        } else if ((oldImageOnDisk) || (oldImageOnDisk && sameFilenameOnDisk)) {
            logger.warn("Esto no debería pasar");
        }

        if (!destFolderExists()) {
            msg += "\n\nEl directorio:\n  " + destFolder
                    + "\nno existe y será creado para copiar la imagen original a este nuevo directorio";
        }
        return msg;
    }

    private boolean userWantsToOverwrite(String msg) {

        int m = JOptionPane.showConfirmDialog(null, msg, "Aviso", JOptionPane.OK_CANCEL_OPTION);

        if (m == JOptionPane.OK_OPTION) {
            return true;
        }
        return false;
    }

    public boolean abort() {

        if (fileImage == null) {
            return true;
        }

        String msg = generateMsg();

        if (msg != null) {
            return !userWantsToOverwrite(msg);
        }
        return false;
    }

    public File getFile() {
        return fileImage;
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog((Component) PluginServices.getMainFrame(), msg, "Aviso",
                JOptionPane.WARNING_MESSAGE);
    }
}
