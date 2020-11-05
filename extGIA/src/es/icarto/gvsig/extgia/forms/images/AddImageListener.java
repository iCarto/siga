package es.icarto.gvsig.extgia.forms.images;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.jeta.forms.components.image.ImageComponent;

import es.icarto.gvsig.commons.utils.FileUtils;
import es.icarto.gvsig.commons.utils.ImageUtils;

public class AddImageListener implements ActionListener {

    private static final Logger logger = Logger.getLogger(AddImageListener.class);

    private final ImagesDAO dao;
    private final ImageComponent imageComponent;
    private final JButton addImageButton;
    private final JButton saveImageButton;
    private final JButton deleteImageButton;

    private String pkValue;
    private String destFolder;

    public AddImageListener(ImageComponent imageComponent, JButton addImageButton, ImagesDAO dao,
            JButton saveImageButton, JButton deleteImageButton) {
        this.imageComponent = imageComponent;
        this.addImageButton = addImageButton;
        this.saveImageButton = saveImageButton;
        this.deleteImageButton = deleteImageButton;
        this.dao = dao;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
		WorkFlow wf = new WorkFlow(dao, pkValue, destFolder);
        if (wf.abort()) {
            return;
        }
        File fileImage = wf.getFile();
        String destFile = null;
        BufferedImage image;
        try {
            image = ImageIO.read(fileImage);
            if (image == null) {
                throw new IOException("La imagen no puede ser leída");
            }
        } catch (IOException e1) {
            logger.error(e1.getStackTrace(), e1);
            showWarning(PluginServices.getText(this, "image_msg_error"));
            return;
        }

        if (this.destFolder != null) {
            destFile = destFolder + File.separator + fileImage.getName();
            File destFolderFile = new File(destFolder);
            if (!destFolderFile.exists()) {
                if (!destFolderFile.mkdirs()) {
                    showWarning(PluginServices.getText(this, "image_msg_error"));
                    return;
                }
            }

            try {
                File oldFile = dao.getFileOnDisk(pkValue);
                if (oldFile != null && oldFile.exists()) {
                    oldFile.delete();
                }
            } catch (SQLException e1) {
                logger.error(e1.getStackTrace(), e1);
            }

            FileUtils.copyFile(fileImage, new File(destFile));
        }

        try {
            BufferedImage imageResized = ImageUtils.resizeImage(image, 615, 345, 460);
            if (wf.oldImageInDB) {
                dao.updateImageIntoDb(pkValue, imageResized, fileImage.getName(), destFile);
            } else {
                dao.insertImageIntoDb(pkValue, imageResized, fileImage.getName(), destFile);
            }
        } catch (SQLException e1) {
            logger.error(e1.getStackTrace(), e1);
            showWarning(PluginServices.getText(this, "image_msg_error"));
            return;
        } catch (IOException e1) {
            logger.error(e1.getStackTrace(), e1);
            showWarning(PluginServices.getText(this, "image_msg_error"));
            return;
        }

        // JOptionPane.showMessageDialog(null, PluginServices.getText(this, "image_msg_added"));
        ShowImageAction showImage = new ShowImageAction(imageComponent, addImageButton, dao, pkValue);
        showImage.resetEnability(saveImageButton, deleteImageButton);

    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog((Component) PluginServices.getMainFrame(), msg, "Aviso",
                JOptionPane.WARNING_MESSAGE);
    }
    
    public void setPkValue(String pkValue) {
        this.pkValue = pkValue;
    }
    
    public void setDestFolder(String destFolder) {
    	this.destFolder = destFolder;
    }

}
