package es.icarto.gvsig.extgex;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgex.wms.LoadWMS;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class WMSLoadDimensionesExtension extends AbstractExtension {
    @Override
    public void execute(String actionCommand) {
        LoadWMS loadWMS = new LoadWMS("Planos_Estado_Dimensiones_WMS");
        loadWMS.Load(true);
    }

    @Override
    public boolean isEnabled() {
        return DBSession.isActive() && isViewActive();
    }

}
