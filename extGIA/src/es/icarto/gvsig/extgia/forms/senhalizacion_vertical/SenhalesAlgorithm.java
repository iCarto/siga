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
    private final String extension = ".gif";
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

    public ImageIcon getIcon(String tipo, String codigo) {
	String imgPath = getPath(tipo, codigo);
	if (imgPath.equals(emptyImagePath)) {
	    return emptyImage;
	}
	return ImageUtils.getScaled(imgPath, BOUNDARY);
    }

    public String getPath(String tipo, String codigo) {
	String path = emptyImagePath;
	if (tipo.equals("Cartel")) {
	    if (codigo.isEmpty() || (codigo.equals("Otro"))) {
		path = "0_cartel.png";
	    } else {
		path = codigo + extension;
	    }
	} else if (tipo.equals("Placa")) {
	    if (codigo.isEmpty() || (codigo.equals("Otro"))) {
		path = "0_placa.png";
	    } else {
		path = codigo + extension;
	    }
	} else {
	    // This condition is the same as "Placa" but this approach is more
	    // readable
	    if (codigo.isEmpty() || (codigo.equals("Otro"))) {
		path = "0_placa.png";
	    } else {
		path = codigo + extension;
	    }
	}
	return folderPath + path;
    }

    public int getSize(String tipo, String codigo) {
	if (tipo.equals("Cartel")) {
	    if (codigo.isEmpty() || (codigo.equals("Otro"))) {
		return PICTURE_SIZE;
	    } else {
		return CARTEL_PICTURE_SIZE;
	    }
	}
	return PICTURE_SIZE;
    }

}
