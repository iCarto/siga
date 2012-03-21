package com.iver.cit.gvsig.project.documents.view.toc.actions;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction;
import com.iver.cit.gvsig.project.documents.view.toc.ITocItem;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib��ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
/* CVS MESSAGES:
 *
 * $Id$
 * $Log$
 * Revision 1.2  2006-10-02 13:52:34  jaume
 * organize impots
 *
 * Revision 1.1  2006/09/15 10:41:30  caballero
 * extensibilidad de documentos
 *
 * Revision 1.1  2006/09/12 15:58:14  jorpiell
 * "Sacadas" las opcines del men� de FPopupMenu
 *
 *
 */
/**
*
* @author Jose Manuel Viv� (Chema)
*
* Entrada de men� para recargar una capa.
*/
public class ReloadLayerTocMenuEntry extends AbstractTocContextMenuAction{

    private static Logger logger = Logger.getLogger(ReloadLayerTocMenuEntry.class.getName());

    public String getGroup() {
		return "group3"; //FIXME
	}

	public int getGroupOrder() {
		return 30;
	}

	public int getOrder() {
		return 1;
	}

	public String getText() {
		return PluginServices.getText(this, "recargar");
	}

	public boolean isEnabled(ITocItem item, FLayer[] selectedItems) {
		FLayer layer = getNodeLayer(item);
		if(layer instanceof FLyrVect){
			if(((FLyrVect)layer).isEditing()){
				return false;
			}
		}
		return true;
	}

	public boolean isVisible(ITocItem item, FLayer[] selectedItems) {
		if (isTocItemBranch(item)) {
			return true;
		}
		return false;

	}


	public void execute(ITocItem item, FLayer[] selectedItems) {
		try {
			FLayer layer = getNodeLayer(item);
			if(layer instanceof FLyrVect){
				if(((FLyrVect)layer).isEditing()){
					JOptionPane.showMessageDialog(null, PluginServices.getText(this, "_cant_reload_an_editing_layer"));
				}
			}
			layer.reload();
		} catch (Exception ex) {
			logger.warn("_cant_reload_layer", ex);
			JOptionPane.showMessageDialog(null, PluginServices.getText(this, "_cant_reload_layer"));
		}
	}
}
