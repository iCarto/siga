package es.icarto.gvsig.extgex;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgex.locators.LocatorByPK;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class LocatorByPKExtension extends AbstractExtension {

    private static final String LAYER_PKS = "pks";

    private FLyrVect pkLayer;

    @Override
    public void execute(String actionCommand) {
	LocatorByPK pkLocator = new LocatorByPK(pkLayer);
	pkLocator.openDialog();
    }

    @Override
    public boolean isEnabled() {
	if ((DBSession.getCurrentSession() != null) && (getView() != null)
		&& isLayerLoaded()) {
	    return true;
	}
	return false;
    }

    private boolean isLayerLoaded() {
	TOCLayerManager toc = new TOCLayerManager();
	pkLayer = toc.getLayerByName(LAYER_PKS);
	if (pkLayer != null) {
	    return true;
	}
	return false;
    }

}
