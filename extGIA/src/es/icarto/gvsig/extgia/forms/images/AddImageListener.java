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
import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.FilesLinkDataImp;
import es.icarto.gvsig.siga.models.InfoEmpresa;
import es.icarto.gvsig.siga.models.InfoEmpresaGIA;

public class AddImageListener implements ActionListener {

    private static final Logger logger = Logger.getLogger(AddImageListener.class);

    private final ImagesDAO dao;
    private final ImageComponent imageComponent;
    private final JButton addImageButton;
    private final JButton saveImageButton;
    private final JButton deleteImageButton;
    private final Object tramo;

    private String pkValue;

    private AbstractFormWithLocationWidgets form;

    public void setPkValue(String pkValue) {
        this.pkValue = pkValue;
    }

    public AddImageListener(ImageComponent imageComponent, JButton addImageButton, ImagesDAO dao,
            JButton saveImageButton, JButton deleteImageButton, Object tramo) {
        this.imageComponent = imageComponent;
        this.addImageButton = addImageButton;
        this.saveImageButton = saveImageButton;
        this.deleteImageButton = deleteImageButton;
        this.dao = dao;
        this.tramo = tramo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        WorkFlow wf = new WorkFlow(dao, pkValue, getDestFolder());
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

        if (this.form != null) {
            destFile = getDestFolder() + File.separator + fileImage.getName();
            File destFolderFile = new File(getDestFolder());
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

    public void setForm(AbstractFormWithLocationWidgets form) {
        this.form = form;
    }

    public String getDestFolder() {
        if (form != null) {
        	InfoEmpresaGIA infoEmpresa = new InfoEmpresaGIA();
            FilesLinkDataImp data = new FilesLinkDataImp(form.getElement(), infoEmpresa.getCompany(tramo));
            return data.getFolder(form);
        }
        return null;
    }
}
