package es.icarto.gvsig.audasacommons;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.audasacommons.incidencias.IncidenciasParser;
import es.icarto.gvsig.commons.gui.ExcelFileChooser;
import es.icarto.gvsig.utils.DesktopApi;

public class ImportIncidenciasExtension extends Extension {

    private static final Logger logger = Logger
	    .getLogger(ImportIncidenciasExtension.class);

    public final static String KEY = "import-incidencias";

    // tracks the last folder selected by the user in this session
    private File folder = new File(System.getProperty("user.home"));

    @Override
    public void initialize() {
	registerIcon();
    }

    private void registerIcon() {
	URL iconResource = getClass().getClassLoader().getResource(
		"images/import-incidencias.png");
	PluginServices.getIconTheme().registerDefault(KEY, iconResource);
    }

    @Override
    public void execute(String actionCommand) {
	ExcelFileChooser excelFileChooser = new ExcelFileChooser(folder);
	File file = excelFileChooser.showDialog();
	IncidenciasParser parser = null;

	if (file == null) {
	    return;
	}
	folder = file.getParentFile();
	try {
	    parser = new IncidenciasParser(file);
	} catch (InvalidFormatException e1) {
	    logger.error(e1.getStackTrace(), e1);
	    JOptionPane
		    .showMessageDialog(
			    null,
			    "Error abriendo el fichero.\nCompruebe que la ruta sea correcta y es un fichero Excel v�lido.\nPruebe a abrir y guardar el fichero con otro nombre.",
			    null, JOptionPane.INFORMATION_MESSAGE, null);
	    return;
	} catch (IOException e1) {
	    logger.error(e1.getStackTrace(), e1);
	    JOptionPane
		    .showMessageDialog(
			    null,
			    "Error abriendo el fichero.\nCompruebe que la ruta sea correcta y es un fichero Excel v�lido.\nPruebe a abrir y guardar el fichero con otro nombre.",
			    null, JOptionPane.INFORMATION_MESSAGE, null);
	    return;
	} catch (RuntimeException e) {

	    Object[] options = {
		    PluginServices.getText(this, "optionPane_yes"),
		    PluginServices.getText(this, "optionPane_no") };
	    int m = JOptionPane
		    .showOptionDialog(
			    null,
			    "Error abriendo el fichero.\nPruebe a abrir y cerrar el Excel con otro nombre.\n�Desea abrir el fichero Excel ahora?",
			    null, JOptionPane.YES_NO_CANCEL_OPTION,
			    JOptionPane.INFORMATION_MESSAGE, null, options,
			    options[0]);
	    if (m == JOptionPane.OK_OPTION) {
		DesktopApi.open(file);
	    }

	    return;
	}

	FLyrVect layer = null;
	boolean kmlWritted = false;
	try {
	    PluginServices.getMDIManager().setWaitCursor();
	    parser.parse();
	    layer = parser.toFLyrVect();
	    if (layer != null) {
		IWindow iwindow = PluginServices.getMDIManager()
			.getActiveWindow();
		if (iwindow instanceof View) {
		    View view = (View) iwindow;
		    FLayers layers = view.getMapControl().getMapContext()
			    .getLayers();
		    layers.addLayer(layer);
		}

		kmlWritted = parser.toKml();
	    }

	} catch (RuntimeException e) {
	    PluginServices.getMDIManager().restoreCursor();
	    String msg = e.getMessage();
	    if (msg.startsWith("Cabecera")) {
		Object[] options = {
			PluginServices.getText(this, "optionPane_yes"),
			PluginServices.getText(this, "optionPane_no") };
		int m = JOptionPane.showOptionDialog(null,
			"Error abriendo el fichero.\n" + msg
				+ "\n�Desea abrir el fichero Excel ahora?",
			null, JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.INFORMATION_MESSAGE, null, options,
			options[0]);
		if (m == JOptionPane.OK_OPTION) {
		    DesktopApi.open(file);
		}
	    } else {
		logger.error(e.getStackTrace(), e);
		JOptionPane.showMessageDialog(null,
			"Error desconocido procesando el fichero", null,
			JOptionPane.INFORMATION_MESSAGE, null);
	    }
	    return;
	}

	PluginServices.getMDIManager().restoreCursor();

	String msg = "";

	List<String> warnings = parser.getWarnings();

	if (layer == null) {
	    if (warnings.size() > 0) {
		msg = "No se ha podido crear el shp. Estas pueden ser algunas de las razones:\n\n";
	    } else {
		msg = "No se ha podido crear el shp por alg�n motivo no determinado\nCompruebe que la ruta sea correcta y es un fichero Excel v�lido.\nPruebe a abrir y guardar el fichero con otro nombre.\n";
	    }
	} else if (!kmlWritted) {
	    if (warnings.size() > 0) {
		msg = "El shp ha sido creado pero no se ha podido crear el kml. Estas pueden ser algunas de las razones:\n\n";
	    } else {
		msg = "El shp ha sido creado pero no se ha podido crear el kml por alg�n motivo no determinado\nCompruebe que la ruta sea correcta y es un fichero Excel v�lido.\nPruebe a abrir y guardar el fichero con otro nombre.\n";
	    }
	}

	if (msg.isEmpty()) {
	    msg = "shp y kml creados correctamente.";
	    if (warnings.size() > 0) {
		msg += "\nPero revise los siguientes avisos:\n";
	    }
	}
	for (String w : warnings) {
	    msg += "* " + w + "\n";
	}

	JOptionPane.showMessageDialog(null, msg, null,
		JOptionPane.INFORMATION_MESSAGE, null);

    }

    @Override
    public boolean isEnabled() {
	return PluginServices.getMDIManager().getActiveWindow() instanceof View;
    }

    @Override
    public boolean isVisible() {
	return true;
    }

}
