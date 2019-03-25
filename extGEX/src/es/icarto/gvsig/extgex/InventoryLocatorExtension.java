package es.icarto.gvsig.extgex;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.WindowInfo;

import es.icarto.gvsig.commons.SingleLayerAbstractExtension;
import es.icarto.gvsig.commons.gui.TOCLayerManager;
import es.icarto.gvsig.extgex.locators.InventoryLocator;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class InventoryLocatorExtension extends SingleLayerAbstractExtension {

    @Override
    public void execute(String actionCommand) {
        InventoryLocator inventoryLocator = new InventoryLocator(
                new TOCLayerManager(getView().getMapControl()));
        inventoryLocator.openDialog();
        WindowInfo viewInfo = PluginServices.getMDIManager().getWindowInfo(
                PluginServices.getMDIManager().getActiveWindow());
        WindowInfo pkLocatorInfo = inventoryLocator.getWindowInfo();
        int x = viewInfo.getX() + viewInfo.getWidth()
                - pkLocatorInfo.getWidth();
        int y = viewInfo.getY();
        pkLocatorInfo.setX(x > 0 ? x : 0);
        pkLocatorInfo.setY(y > 0 ? y : 0);
    }

    @Override
    public boolean isEnabled() {
        return DBSession.isActive()
                && isViewActive()
                && isLayerLoaded()
                && !InventoryLocator.getValidElements(
                        new TOCLayerManager(getView().getMapControl()))
                        .isEmpty();
    }

    @Override
    protected String getLayerName() {
        return "pks";
    }

}
