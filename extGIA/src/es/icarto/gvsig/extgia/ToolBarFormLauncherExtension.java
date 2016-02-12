package es.icarto.gvsig.extgia;

import java.util.ArrayList;
import java.util.List;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgia.forms.LaunchGIAForms;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class ToolBarFormLauncherExtension extends AbstractExtension {

    private final static List<String> elementList = new ArrayList<String>();
    static {
	for (Elements e : Elements.values()) {
	    elementList.add(e.name());
	}
    }

    @Override
    public void execute(String actionCommand) {
	TOCLayerManager toc = new TOCLayerManager();
	FLyrVect[] layers = toc.getActiveLayers();
	for (FLyrVect layer : layers) {
	    LaunchGIAForms.callFormDependingOfLayer(layer, false);
	}
    }

    @Override
    public boolean isEnabled() {
	return DBSession.isActive() && activeInventoryLayer();
    }

    private boolean activeInventoryLayer() {
	View view = getView();
	if (view == null) {
	    return false;
	}
	TOCLayerManager toc = new TOCLayerManager(view.getMapControl());
	FLyrVect[] layers = toc.getActiveLayers();
	for (FLyrVect l : layers) {
	    if (elementList.contains(l.getName())) {
		return true;
	    }
	}
	return false;
    }
}
