package es.icarto.gvsig.extgex;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgex.forms.expropiations.ExpropiationsLayerResolver;
import es.icarto.gvsig.extgex.locators.LocatorByFinca;
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
        return ExpropiationsLayerResolver.areLayersLoaded();
    }
}
