package org.gvsig.copypastegeom.toc;

import org.gvsig.copypastegeom.CopyFeaturesExtension;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction;
import com.iver.cit.gvsig.project.documents.view.toc.ITocItem;

public class CopyFeaturesTocMenuEntry extends AbstractTocContextMenuAction {

    @Override
    public String getGroup() {
        return "copypastegeom";
    }

    @Override
    public int getGroupOrder() {
        return 80;
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void execute(ITocItem item, FLayer[] selectedItems) {
        try {
            ((CopyFeaturesExtension) PluginServices.getExtension(CopyFeaturesExtension.class)).copyFeatures();
        } catch (ReadDriverException e) {
            NotificationManager.addError(PluginServices.getText(this, "error_ejecutando_la_herramienta"), e);
        }
    }

    @Override
    public boolean isEnabled(ITocItem item, FLayer[] selectedItems) {
        return ((CopyFeaturesExtension) PluginServices.getExtension(CopyFeaturesExtension.class)).isEnabled();
    }

    @Override
    public boolean isVisible(ITocItem item, FLayer[] selectedItems) {
        return ((CopyFeaturesExtension) PluginServices.getExtension(CopyFeaturesExtension.class)).isVisible();
    }

    public String getText() {
        return PluginServices.getText(this, "copy_features");
    }

}
