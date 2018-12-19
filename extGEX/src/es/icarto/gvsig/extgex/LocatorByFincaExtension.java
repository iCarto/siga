package es.icarto.gvsig.extgex;

import com.iver.cit.gvsig.fmap.MapControl;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgex.forms.expropiations.FormExpropiations;
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
        return DBSession.isActive() && isViewActive() && areLayersLoaded();
    }


    @Override
    public void initialize() {
	// nothing to do here
    }
    
    private boolean areLayersLoaded() {
        TOCLayerManager toc = new TOCLayerManager();
        if ((toc.getLayerByName(FormExpropiations.TOCNAME) != null)
            && (toc.getLayerByName(FormExpropiations.TOCNAME_AMPLIACION) != null)) {
            return true;
        }
        return false;
        }
}
