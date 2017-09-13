package es.icarto.gvsig.extgia.forms.images;

import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.apache.log4j.Logger;

import com.jeta.forms.components.image.ImageComponent;

import es.icarto.gvsig.siga.PreferencesPage;

public class ShowImageAction {

    private static final Logger logger = Logger.getLogger(ShowImageAction.class);

    private final ImageComponent imageComponent;
    private final JButton addImageButton;
    private final String pkValue;
    private final ImagesDAO dao;
    private final boolean showImage;

    public ShowImageAction(ImageComponent imageComponent, JButton addImageButton, ImagesDAO dao, String pkValue) {
        this.imageComponent = imageComponent;
        this.addImageButton = addImageButton;
        this.dao = dao;
        this.pkValue = pkValue;
        showImage = showImage();
        if (showImage) {
            addImageButton.setText("Actualizar");
        } else {
            addImageButton.setText("Añadir");
        }
        imageComponent.repaint();
    }

    public boolean showImage() {
        try {
            ImageIcon elementIcon = dao.getImageIconFromDb(pkValue);
            if (elementIcon == null) {
                imageComponent.setIcon(getUnavailableImageIcon());
                return false;
            }
            imageComponent.setIcon(elementIcon);
        } catch (SQLException e1) {
            logger.error(e1.getStackTrace(), e1);
            return false;
        }
        return true;
    }

    private ImageIcon getUnavailableImageIcon() {
        return new ImageIcon(PreferencesPage.IMG_UNAVAILABLE);
    }

    public void resetEnability(JButton saveImageButton, JButton deleteImageButton) {

        if (saveImageButton != null) {
            saveImageButton.setEnabled(showImage);
        }
        if (deleteImageButton != null) {
            deleteImageButton.setEnabled(showImage);
        }

    }

}
