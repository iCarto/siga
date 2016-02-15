package es.icarto.gvsig.extgia.forms.senhalizacion_vertical;

import java.awt.Dimension;
import java.io.File;

import javax.swing.ImageIcon;

import com.iver.cit.gvsig.fmap.core.SymbologyFactory;

import es.icarto.gvsig.commons.utils.ImageUtils;

public class SenhalesAlgorithm {

    private static final int PICTURE_SIZE = 20;
    private static final int CARTEL_PICTURE_SIZE = 50;

    private final Dimension boundary;
    private final String folderPath;
    private final ImageIcon emptyImage;
    private final String extension = ".png";
    private final String emptyImagePath;
    private final String emptyImageFile;

    public SenhalesAlgorithm(Dimension boundary) {
	this.boundary = boundary;
	this.folderPath = SymbologyFactory.SymbolLibraryPath + File.separator
		+ "senhales" + File.separator;
	emptyImageFile = "0_placa.png";
	emptyImagePath = folderPath + "0_placa.png";
	if (boundary != null) {
	    // Workaround. Para el mapa con simbología de señales la ruta
	    // devuelta no será real
	    emptyImage = ImageUtils.getScaled(emptyImagePath, boundary);
	} else {
	    emptyImage = null;
	}
    }

    public ImageIcon getIcon(String tipo, String codigo, String id) {
	String imgPath = getAbsolutePathPath(tipo, codigo, id);
	if (imgPath.equals(emptyImagePath)) {
	    return emptyImage;
	}
	return ImageUtils.getScaled(imgPath, boundary);
    }

    // id = id_senhal_vertical
    public String getAbsolutePathPath(String tipo, String codigo,
	    String idSenhal) {
	return folderPath + getFilename(tipo, codigo, idSenhal);
    }

    public int getSize(String tipo, String codigo) {
	if (tipo.equals("Cartel")) {
	    return CARTEL_PICTURE_SIZE;
	}
	return PICTURE_SIZE;
    }

    public String getFilename(String tipo, String codigo, String idSenhal) {
	String path = emptyImageFile;
	if (tipo.equals("Panel direccional")) {
	    path = "Panel_direccional_azul.png";
	} else if (tipo.equals("Contenido fijo")) {
	    if (codigo.isEmpty() || (codigo.equals("Otro"))) {
		path = emptyImageFile;
	    } else {
		path = codigo + extension;
	    }
	} else if (tipo.equals("Cartel")) {
	    path = idSenhal + extension;
	    File ifi = new File(folderPath + path);
	    if (!ifi.isFile()) {
		path = "0_cartel.png";
	    }
	} else {
	    path = "0_cartel.png";
	}
	return path;
    }

}
