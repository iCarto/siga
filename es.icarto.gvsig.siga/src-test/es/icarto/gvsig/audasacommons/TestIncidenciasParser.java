package es.icarto.gvsig.audasacommons;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.audasacommons.incidencias.IncidenciasParser;
import es.icarto.gvsig.commons.testutils.Drivers;
import es.icarto.gvsig.commons.testutils.TestProperties;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class TestIncidenciasParser {

    private static final String filePath = null;

    public static void main(String[] args) {
	Drivers.initgvSIGDrivers(TestProperties.driversPath);

	try {
	    DBSession.createConnection("localhost", 5434, "audasa_test", null,
		    "postgres", "postgres");
	} catch (DBException e) {
	    e.printStackTrace();
	}
	File file = new File(filePath);

	IncidenciasParser parser = null;
	try {
	    parser = new IncidenciasParser(file);
	} catch (IOException e) {
	    e.printStackTrace();
	    NotificationManager
		    .showMessageError(
			    "Error abriendo el fichero. Compruebe que la ruta sea correcta.",
			    e);
	    return;
	} catch (InvalidFormatException e) {
	    e.printStackTrace();
	}

	try {
	    parser.parse();
	} catch (RuntimeException e) {
	    String msg = e.getMessage();
	    if (msg.startsWith("Cabecera")) {
		// Alg�n campo no est� en la cabecera. Abrir fichero.
	    } else {
		NotificationManager.showMessageError(
			"Error desconocido procesando el fichero", e);
	    }
	}
	for (String w : parser.getWarnings()) {
	    System.out.println(w);
	}
	FLyrVect layer = parser.toFLyrVect();
	if (layer == null) {
	    System.out.println("Error escribiendo el shape");
	}
	if (!parser.toKml()) {
	    System.out.println("Error escribiendo el kml");
	}

	System.out.println("Finish!");
    }

}
