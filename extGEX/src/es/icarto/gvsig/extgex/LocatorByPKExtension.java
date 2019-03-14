package es.icarto.gvsig.extgex;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
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
        IWindow view = PluginServices.getMDIManager().getActiveWindow();
        pkLocator.openDialog();
        WindowInfo viewInfo = PluginServices.getMDIManager()
                .getWindowInfo(view);
        WindowInfo pkLocatorInfo = pkLocator.getWindowInfo();
        int x = viewInfo.getX() + viewInfo.getWidth()
                - pkLocatorInfo.getWidth();
        int y = viewInfo.getY();
        pkLocatorInfo.setX(x > 0 ? x : 0);
        pkLocatorInfo.setY(y > 0 ? y : 0);
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
