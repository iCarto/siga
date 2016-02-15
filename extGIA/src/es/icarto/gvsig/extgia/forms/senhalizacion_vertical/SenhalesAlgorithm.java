package es.icarto.gvsig.extgia.forms.senhalizacion_vertical;

import java.awt.Dimension;
import java.io.File;

import javax.swing.ImageIcon;

import es.icarto.gvsig.commons.utils.ImageUtils;

public class SenhalesAlgorithm {

    private static final int PICTURE_SIZE = 20;
    private static final int CARTEL_PICTURE_SIZE = 50;

    private final Dimension BOUNDARY;
    private final String folderPath;
    private final ImageIcon emptyImage;
    private final String extension = ".png";
    private final String emptyImagePath;

    public SenhalesAlgorithm(String folderPath, Dimension boundary) {
	this.BOUNDARY = boundary;
	this.folderPath = folderPath.endsWith(File.separator) ? folderPath
		: folderPath + File.separator;
	emptyImagePath = folderPath + "0_placa.png";
	if (!folderPath.isEmpty()) {
	    // Workaround. Para el mapa con simbología de señales la ruta
	    // devuelta no será real
	    emptyImage = ImageUtils.getScaled(emptyImagePath, BOUNDARY);
	} else {
	    emptyImage = null;
	}
    }

    public ImageIcon getIcon(String tipo, String codigo, String id) {
	String imgPath = getPath(tipo, codigo, id);
	if (imgPath.equals(emptyImagePath)) {
	    return emptyImage;
	}
	return ImageUtils.getScaled(imgPath, BOUNDARY);
    }

    // id = id_senhal_vertical
    public String getPath(String tipo, String codigo, String id) {
	String path = emptyImagePath;
	if (tipo.equals("Panel direccional")) {
	    path = "Panel_direccional_azul.png";
	} else if (tipo.equals("Contenido fijo")) {
	    if (codigo.isEmpty() || (codigo.equals("Otro"))) {
		path = emptyImagePath;
	    } else {
		path = codigo + extension;
	    }
	} else if (tipo.equals("Cartel")) {
	    path = id + extension;
	    File ifi = new File(folderPath + path);
	    if (!ifi.isFile()) {
		path = "0_cartel.png";
	    }
	} else {
	    path = "0_cartel.png";
	}
	return folderPath + path;
    }

    public int getSize(String tipo, String codigo) {
	if (tipo.equals("Cartel")) {
	    return CARTEL_PICTURE_SIZE;
	}
	return PICTURE_SIZE;
    }

}
