package com.iver.cit.gvsig.project.documents.view.toc.actions;

import java.awt.Component;

import javax.swing.JOptionPane;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction;
import com.iver.cit.gvsig.project.documents.view.toc.ITocItem;
import com.iver.utiles.XMLEntity;

public class CutLayersTocMenuEntry extends AbstractTocContextMenuAction {
	private CopyPasteLayersUtiles utiles = CopyPasteLayersUtiles.getInstance();

	public String getGroup() {
		return "copyPasteLayer";
	}

	public int getGroupOrder() {
		return 60;
	}
	public int getOrder() {
		return 1;
	}

	public String getText() {
		return PluginServices.getText(this, "cortar");
	}

	public boolean isEnabled(ITocItem item, FLayer[] selectedItems) {
		if ( selectedItems.length >= 1 && isTocItemBranch(item)){
			for (int i=0;i< selectedItems.length;i++) {
				if (selectedItems[i].isEditing()){
					return false;
				}
			}
			return true;
		}
		return false;
	}


	public void execute(ITocItem item, FLayer[] selectedItems) {
		XMLEntity xml = this.utiles.generateXMLCopyLayers(selectedItems);
		if (xml == null) {
			JOptionPane.showMessageDialog(
					(Component)PluginServices.getMainFrame(),
					"<html>"+PluginServices.getText(this,"No_ha_sido_posible_realizar_la_operacion")+"</html>",//Mensaje
					PluginServices.getText(this,"cortar"),//titulo
					JOptionPane.ERROR_MESSAGE
					);
			return;
		}

		String data = xml.toString();
		if (data == null) {
			JOptionPane.showMessageDialog(
					(Component)PluginServices.getMainFrame(),
					"<html>"+PluginServices.getText(this,"No_ha_sido_posible_realizar_la_operacion")+"</html>",//Mensaje
					PluginServices.getText(this,"cortar"),//titulo
					JOptionPane.ERROR_MESSAGE
					);
			return;
		}


		PluginServices.putInClipboard(data);


    	int option=JOptionPane.showConfirmDialog((Component)PluginServices.getMainFrame(),PluginServices.getText(this,"desea_borrar_la_capa"));
    	if (option!=JOptionPane.OK_OPTION) {
    		return;
    	}
		getMapContext().beginAtomicEvent();


		boolean isOK =this.utiles.removeLayers(selectedItems);

		getMapContext().endAtomicEvent();

		if (isOK) {
			getMapContext().invalidate();
			Project project=((ProjectExtension)PluginServices.getExtension(ProjectExtension.class)).getProject();
			project.setModified(true);
			if (getMapContext().getLayers().getLayersCount()==0) {
				PluginServices.getMainFrame().enableControls();
			}
		}

	}

}
