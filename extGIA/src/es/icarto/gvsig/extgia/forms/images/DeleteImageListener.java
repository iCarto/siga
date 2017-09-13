package es.icarto.gvsig.extgia.forms.images;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.jeta.forms.components.image.ImageComponent;

public class DeleteImageListener implements ActionListener {

    private static final Logger logger = Logger.getLogger(DeleteImageListener.class);

    private final JButton addImageButton;
    private final JButton saveImageButton;
    private final ImagesDAO dao;
    private final ImageComponent imageComponent;

    private String pkValue;

    public DeleteImageListener(ImageComponent imageComponent, JButton addImageButton, ImagesDAO dao,
            JButton saveImageButton) {
        this.imageComponent = imageComponent;
        this.addImageButton = addImageButton;
        this.saveImageButton = saveImageButton;
        this.dao = dao;
    }

    public void setPkValue(String pkValue) {
        this.pkValue = pkValue;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Object[] options = { PluginServices.getText(this, "delete"), PluginServices.getText(this, "cancel") };
            int response = JOptionPane.showOptionDialog(null, PluginServices.getText(this, "img_delete_warning"),
                    PluginServices.getText(this, "delete"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            if (response == JOptionPane.YES_OPTION) {
                try {
                    File oldFile = dao.getFileOnDisk(pkValue);
                    if (oldFile != null) {
                        oldFile.delete();
                    }
                } catch (SQLException e1) {
                    logger.error(e1.getStackTrace(), e1);
                }
                dao.deleteImageFromDb(pkValue);

                ShowImageAction showImage = new ShowImageAction(imageComponent, addImageButton, dao, pkValue);
                showImage.resetEnability(saveImageButton, (JButton) e.getSource());

            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

}
