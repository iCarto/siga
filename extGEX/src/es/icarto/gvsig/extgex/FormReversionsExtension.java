package es.icarto.gvsig.extgex;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.extgex.forms.reversions.FormReversions;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class FormReversionsExtension extends Extension {

    private FLyrVect layer = null;
    private FormReversions dialog = null;

    @Override
    public void execute(String actionCommand) {
	DBSession.getCurrentSession().setSchema(DBNames.EXPROPIATIONS_SCHEMA);
	// if (AlphanumericTableLoader.loadTables()) {
	layer = getLayer();
	dialog = new FormReversions(layer, null);
	if (dialog.init()) {
	    PluginServices.getMDIManager().addWindow(dialog);
	}
	// } else {
	// JOptionPane.showMessageDialog(null, PluginServices.getText(this,
	// "alphanumeric_table_no_loaded"));
	// }
    }

    private FLyrVect getLayer() {
	String layerName = FormReversions.TOCNAME;
	TOCLayerManager toc = new TOCLayerManager();
	return toc.getLayerByName(layerName);
    }

    protected void registerIcons() {
	PluginServices.getIconTheme().registerDefault(
		"extgex-reversions",
		this.getClass().getClassLoader()
			.getResource("images/extgpeB.png"));
    }

    @Override
    public void initialize() {
	registerIcons();
    }

    @Override
    public boolean isEnabled() {
	if ((DBSession.getCurrentSession() != null) && hasView()
		&& isLayerLoaded(FormReversions.TOCNAME)) {
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    public boolean isVisible() {
	return true;
    }

    private boolean isLayerLoaded(String layerName) {
	TOCLayerManager toc = new TOCLayerManager();
	FLyrVect layer = toc.getLayerByName(layerName);
	if (layer == null) {
	    return false;
	}
	return true;
    }

    private boolean hasView() {
	IWindow window = PluginServices.getMDIManager().getActiveWindow();
	if (window instanceof View) {
	    return true;
	}
	return false;
    }

}
