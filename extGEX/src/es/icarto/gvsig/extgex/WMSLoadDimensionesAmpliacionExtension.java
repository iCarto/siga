package es.icarto.gvsig.extgex;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgex.wms.LoadWMS;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class WMSLoadDimensionesAmpliacionExtension extends AbstractExtension {

    @Override
    public void execute(String actionCommand) {
        LoadWMS loadWMS = new LoadWMS("planos_dimensiones_ampliacion");
        loadWMS.Load();
    }

    @Override
    public boolean isEnabled() {
        return DBSession.isActive() && isViewActive();
    }

}