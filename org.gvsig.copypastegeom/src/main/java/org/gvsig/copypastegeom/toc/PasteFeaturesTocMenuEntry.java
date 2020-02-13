package org.gvsig.copypastegeom.toc;

import org.gvsig.copypastegeom.PasteFeaturesExtension;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction;
import com.iver.cit.gvsig.project.documents.view.toc.ITocItem;

public class PasteFeaturesTocMenuEntry extends AbstractTocContextMenuAction {

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.contextMenu.AbstractContextMenuAction#getGroup()
	 */
	public String getGroup() {
		return "copyPasteFeatures";
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.contextMenu.AbstractContextMenuAction#getGroupOrder()
	 */
	public int getGroupOrder() {
		return 80;
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.contextMenu.AbstractContextMenuAction#getOrder()
	 */
	public int getOrder() {
		return 20;
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction#execute(com.iver.cit.gvsig.project.documents.view.toc.ITocItem, com.iver.cit.gvsig.fmap.layers.FLayer[])
	 */
	public void execute(ITocItem item, FLayer[] selectedItems) {
		try {
			((PasteFeaturesExtension)PluginServices.getExtension(PasteFeaturesExtension.class)).pasteFeatures();
		} catch (Exception e) {
			NotificationManager.addError(PluginServices.getText(this, "error_ejecutando_la_herramienta"),e);
		}
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction#isEnabled(com.iver.cit.gvsig.project.documents.view.toc.ITocItem, com.iver.cit.gvsig.fmap.layers.FLayer[])
	 */
	public boolean isEnabled(ITocItem item, FLayer[] selectedItems) {
		return ((PasteFeaturesExtension)PluginServices.getExtension(PasteFeaturesExtension.class)).isEnabled();
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction#isVisible(com.iver.cit.gvsig.project.documents.view.toc.ITocItem, com.iver.cit.gvsig.fmap.layers.FLayer[])
	 */
	public boolean isVisible(ITocItem item, FLayer[] selectedItems) {
		return ((PasteFeaturesExtension)PluginServices.getExtension(PasteFeaturesExtension.class)).isVisible();
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.IContextMenuAction#getText()
	 */
	public String getText() {
		return PluginServices.getText(this, "paste_features");
	}

}
