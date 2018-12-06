package es.icarto.gvsig.extgex;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.commons.SingleLayerAbstractExtension;
import es.icarto.gvsig.commons.gui.TOCLayerManager;
import es.icarto.gvsig.extgex.locators.LocatorByPK;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class LocatorByPKExtension extends SingleLayerAbstractExtension {

    @Override
    public void execute(String actionCommand) {
        TOCLayerManager toc = new TOCLayerManager(getView().getMapControl());
        FLyrVect pkLayer = toc.getVectorialLayerByName(getLayerName());
        LocatorByPK pkLocator = new LocatorByPK(pkLayer);
        pkLocator.openDialog();
    }

    @Override
    public boolean isEnabled() {
        return DBSession.isActive() && isViewActive() && isLayerLoaded();
    }

    @Override
    protected String getLayerName() {
        return "pks";
    }

}
