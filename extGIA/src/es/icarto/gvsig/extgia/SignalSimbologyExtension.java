package es.icarto.gvsig.extgia;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.rendering.LegendFactory;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.signalsimbology.SignalLegend;
import es.icarto.gvsig.extgia.signalsimbology.SignalSimbologyDialog;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;

public class SignalSimbologyExtension extends Extension {

    private static final Logger logger = Logger
	    .getLogger(SignalSimbologyExtension.class);

    @Override
    public void initialize() {
	LegendFactory.registerCustomLegend(SignalLegend.class.getName(), SignalLegend.class);
    }

    @Override
    public void execute(String actionCommand) {
	TOCLayerManager tocLayerManager = new TOCLayerManager();
	final FLyrVect postes = tocLayerManager
		.getLayerByName(DBFieldNames.SENHALIZACION_VERTICAL_LAYERNAME);
	final FLyrVect signals = tocLayerManager
		.getLayerByName(DBFieldNames.SENHALIZACION_VERTICAL_SENHALES_LAYERNAME);
	if ((postes == null) || (signals == null)) {
	    showWarning("La capas 'Senhalizacion_Vertical' y 'Senhales' deben estar cargadas en el TOC");
	    return;
	}

	try {
	    // Si se intentan cargar más de 1881 pictogramas se produce un Java
	    // Heap Space
	    long rowCount = signals.getRecordset().getRowCount();
	    if (rowCount > 1000) {
		showWarning("No pueden mostrarse más de 1000 pictogramas de señales");
		return;
	    }
	} catch (ReadDriverException e) {
	    logger.error(e.getStackTrace(), e);
	}

	SignalSimbologyDialog dialog = new SignalSimbologyDialog(postes,
		signals);
	dialog.openDialog();
    }

    private void showWarning(String msg) {
	JOptionPane.showMessageDialog(
		(Component) PluginServices.getMainFrame(), msg, "Aviso",
		JOptionPane.WARNING_MESSAGE);

    }

    @Override
    public boolean isEnabled() {
	IWindow window = PluginServices.getMDIManager().getActiveWindow();
	if (window instanceof View) {
	    return true;
	}
	return false;
    }

    @Override
    public boolean isVisible() {
	return true;
    }

}
