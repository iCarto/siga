package es.icarto.gvsig.extgex;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgex.forms.FormExpropiationLine;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class FormExpropiationLineExtension extends AbstractExtension {

    @Override
    public void initialize() {
    }

    @Override
    public void execute(String actionCommand) {
	AbstractForm dialog = getForm();
	if (dialog.init()) {
	    PluginServices.getMDIManager().addCentredWindow(dialog);
	}
    }
    
    private AbstractForm getForm() {
        FLyrVect layer = getLayer(); 
        AbstractForm dialog = new FormExpropiationLine(layer, isAmpliacion());
        return dialog;
    }

    
    @Override
    public boolean isEnabled() {
    View view = getView();
    if ((DBSession.getCurrentSession() != null) && view != null
        && isLayerLoaded(getTOCLayerName())) {
        return true;
    } else {
        return false;
    }
    }

    private FLyrVect getLayer() {
	TOCLayerManager toc = new TOCLayerManager();
	return toc.getLayerByName(getTOCLayerName());
    }

    private boolean isLayerLoaded(String layerName) {
	TOCLayerManager toc = new TOCLayerManager();
	FLyrVect layer = toc.getLayerByName(layerName);
	if (layer == null) {
	    return false;
	}
	return true;
    }
    
    private String getTOCLayerName() {
        return isAmpliacion() ? FormExpropiationLine.TOCNAME_AMPLIACION : FormExpropiationLine.TOCNAME;
    }
    
    protected boolean isAmpliacion() {
        return false;
    }
    
}
