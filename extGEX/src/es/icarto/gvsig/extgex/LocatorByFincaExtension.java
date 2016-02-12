package es.icarto.gvsig.extgex;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgex.locators.LocatorByFinca;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class LocatorByFincaExtension extends AbstractExtension {

    @Override
    public void execute(String actionCommand) {
	LocatorByFinca fincaLocator = new LocatorByFinca();
	fincaLocator.openDialog();
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
	if (toc.getLayerByName(DBNames.LAYER_FINCAS) != null) {
	    return true;
	}
	return false;
    }

    @Override
    public void initialize() {
	// nothing to do here
    }
}
