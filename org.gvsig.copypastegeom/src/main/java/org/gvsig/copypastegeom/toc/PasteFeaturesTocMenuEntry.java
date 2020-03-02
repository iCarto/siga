package org.gvsig.copypastegeom.toc;

import org.gvsig.copypastegeom.PasteFeaturesExtension;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction;
import com.iver.cit.gvsig.project.documents.view.toc.ITocItem;

public class PasteFeaturesTocMenuEntry extends AbstractTocContextMenuAction {

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
        return 20;
    }

    @Override
    public void execute(ITocItem item, FLayer[] selectedItems) {
        try {
            ((PasteFeaturesExtension) PluginServices.getExtension(PasteFeaturesExtension.class)).pasteFeatures();
        } catch (Exception e) {
            NotificationManager.addError(PluginServices.getText(this, "error_ejecutando_la_herramienta"), e);
        }
    }

    @Override
    public boolean isEnabled(ITocItem item, FLayer[] selectedItems) {
        return ((PasteFeaturesExtension) PluginServices.getExtension(PasteFeaturesExtension.class)).isEnabled();
    }

    @Override
    public boolean isVisible(ITocItem item, FLayer[] selectedItems) {
        return ((PasteFeaturesExtension) PluginServices.getExtension(PasteFeaturesExtension.class)).isVisible();
    }

    public String getText() {
        return PluginServices.getText(this, "paste_features");
    }

}
