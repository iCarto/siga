package es.icarto.gvsig.extgia.forms.images;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.jeta.forms.components.image.ImageComponent;

import es.icarto.gvsig.commons.gui.SaveFileDialog;

public class SaveImageListener implements ActionListener {

    private static final Logger logger = Logger.getLogger(SaveImageListener.class);

    private final ImagesDAO dao;
    private final ImageComponent imageComponent;
    private final JButton addImageButton;
    private String pkValue;

    private static File initFile = new File(System.getProperty("user.home"));

    public SaveImageListener(ImageComponent imageComponent, JButton addImageButton, ImagesDAO dao) {
        this.imageComponent = imageComponent;
        this.addImageButton = addImageButton;
        this.dao = dao;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SaveFileDialog saveFile = new SaveFileDialog("Ficheros PNG", "png");
        saveFile.setCurrentDirectory(initFile);
        saveFile.setAskForOverwrite(true);
        File file = saveFile.showDialog();
        if (file != null) {
            try {
                initFile = file;
                dao.saveDbImageOnDisk(pkValue, file);
            } catch (SQLException e1) {
                logger.error(e1.getStackTrace(), e1);
                showWarning("Error descargando la imagen");
            } catch (IOException e1) {
                logger.error(e1.getStackTrace(), e1);
                showWarning("Error descargando la imagen");
            }
        }

    }

    public void setPkValue(String pkValue) {
        this.pkValue = pkValue;
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog((Component) PluginServices.getMainFrame(), msg, "Aviso",
                JOptionPane.WARNING_MESSAGE);
    }

}
