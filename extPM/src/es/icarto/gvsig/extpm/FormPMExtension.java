package es.icarto.gvsig.extpm;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extpm.forms.FormPM;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class FormPMExtension extends AbstractExtension {

    @Override
    public void execute(String actionCommand) {
	FLyrVect layer = getPMLayer();
	FormPM dialog = new FormPM(layer);
	if (dialog.init()) {
	    PluginServices.getMDIManager().addWindow(dialog);
	}
    }

    @Override
    public boolean isEnabled() {
	View view = getView();
	if ((DBSession.getCurrentSession() != null) && view != null
		&& isLayerLoaded(FormPM.TOCNAME)) {
	    return true;
	} else {
	    return false;
	}
    }

    private FLyrVect getPMLayer() {
	TOCLayerManager toc = new TOCLayerManager();
	return toc.getLayerByName(FormPM.TOCNAME);
    }

    private boolean isLayerLoaded(String layerName) {
	TOCLayerManager toc = new TOCLayerManager();
	FLyrVect layer = toc.getLayerByName(layerName);
	if (layer == null) {
	    return false;
	}
	return true;
    }
}
