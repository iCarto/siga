package es.icarto.gvsig.extgia.forms.images;

import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.jeta.forms.components.image.ImageComponent;

import es.icarto.gvsig.commons.utils.ImageUtils;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.siga.PreferencesPage;

public class ShowImageAction {

    private final ImageComponent imageComponent;
    private final JButton addImageButton;
    private final Connection connection;
    private final String tablename;
    private final String pkField;
    private final String pkValue;

    public ShowImageAction(ImageComponent imageComponent, JButton addImageButton, String tablename,
	    String pkField, String pkValue) {
	this.imageComponent = imageComponent;
	this.addImageButton = addImageButton;
	this.tablename = tablename;
	this.pkField = pkField;
	this.pkValue = pkValue;
	connection = DBFacade.getConnection();
	if (showImage()) {
	    addImageButton.setText("Actualizar");
	}else {
	    addImageButton.setText("Añadir");
	}
	imageComponent.repaint();
    }

    public boolean showImage() {
	ImagesDAO dao = new ImagesDAO();
	try {
	    byte[] elementImageBytes = dao.readImageFromDb(connection, DBFieldNames.GIA_SCHEMA,
		    tablename, pkField, pkValue);
	    if (elementImageBytes == null) {
		imageComponent.setIcon(getUnavailableImageIcon());
		return false;
	    }
	    BufferedImage elementImage = ImageUtils.convertByteaToImage(elementImageBytes);
	    ImageIcon elementIcon = new ImageIcon(elementImage);
	    imageComponent.setIcon(elementIcon);
	} catch (SQLException e1) {
	    e1.printStackTrace();
	}
	return true;
    }

    private ImageIcon getUnavailableImageIcon() {
	return new ImageIcon (PreferencesPage.IMG_UNAVAILABLE);
    }

}
