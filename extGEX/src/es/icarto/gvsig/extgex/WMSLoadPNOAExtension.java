package es.icarto.gvsig.extgex;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgex.wms.LoadWMS;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class WMSLoadPNOAExtension extends AbstractExtension {

    @Override
    public void execute(String actionCommand) {
        LoadWMS loadWMS = new LoadWMS("Ortofotos_PNOA_WMS");
        loadWMS.Load();
    }

    @Override
    public boolean isEnabled() {
        return DBSession.isActive() && isViewActive();
    }

    @Override
    public void initialize() {
        // Overrided, because there is no toolbar icon for this extension
    }
}
