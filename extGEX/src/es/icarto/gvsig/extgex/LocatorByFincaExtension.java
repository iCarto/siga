package es.icarto.gvsig.extgex;

import es.icarto.gvsig.commons.SingleLayerAbstractExtension;
import es.icarto.gvsig.extgex.locators.LocatorByFinca;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class LocatorByFincaExtension extends SingleLayerAbstractExtension {

    @Override
    public void execute(String actionCommand) {
	LocatorByFinca fincaLocator = new LocatorByFinca();
	fincaLocator.openDialog();
    }

    @Override
    public boolean isEnabled() {
        return DBSession.isActive() && isViewActive() && isLayerLoaded();
    }


    @Override
    public void initialize() {
	// nothing to do here
    }

    @Override
    protected String getLayerName() {
        return DBNames.LAYER_FINCAS;
    }
}
