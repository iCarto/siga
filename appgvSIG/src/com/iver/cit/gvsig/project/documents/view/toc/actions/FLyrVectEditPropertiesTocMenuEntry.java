package com.iver.cit.gvsig.project.documents.view.toc.actions;

import javax.swing.JDialog;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrAnnotation;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.layerOperations.ClassifiableVectorial;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.documents.view.legend.gui.ThemeManagerWindow;
import com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction;
import com.iver.cit.gvsig.project.documents.view.toc.ITocItem;
/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
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
 *   Av. Blasco Ib??ez, 50
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
 * Revision 1.8  2007-09-19 15:51:33  jaume
 * fixed bug when opening the layer properties dialog for more than one layer (two or more active layers selected)
 *
 * Revision 1.7  2007/07/30 12:56:04  jaume
 * organize imports, java 5 code downgraded to 1.4 and added PictureFillSymbol
 *
 * Revision 1.6  2007/03/09 11:25:00  jaume
 * Advanced symbology (start committing)
 *
 * Revision 1.4.2.3  2007/02/01 12:12:41  jaume
 * theme manager window and all its components are now dynamic
 *
 * Revision 1.4.2.2  2007/01/30 18:10:10  jaume
 * start commiting labeling stuff
 *
 * Revision 1.4.2.1  2007/01/26 13:49:03  jaume
 * *** empty log message ***
 *
 * Revision 1.4  2007/01/23 13:10:45  caballero
 * no mostrar propiedades de una capa de anotaciones
 *
 * Revision 1.3  2007/01/04 07:24:31  caballero
 * isModified
 *
 * Revision 1.2  2006/10/02 13:52:34  jaume
 * organize impots
 *
 * Revision 1.1  2006/09/15 10:41:30  caballero
 * extensibilidad de documentos
 *
 * Revision 1.1  2006/09/12 15:58:14  jorpiell
 * "Sacadas" las opcines del men? de FPopupMenu
 *
 *
 */
/**
 * Muestra el men? de propiedades del tema.
 *
 * @author jmorell
 */


public class FLyrVectEditPropertiesTocMenuEntry extends AbstractTocContextMenuAction {
	public String getGroup() {
		return "group2"; //FIXME
	}

	public int getGroupOrder() {
		return 20;
	}

	public int getOrder() {
		return 0;
	}

	public String getText() {
		return PluginServices.getText(this, "propiedades");
	}

	public boolean isEnabled(ITocItem item, FLayer[] selectedItems) {
		if (isTocItemBranch(item)) {
			FLayer lyr = getNodeLayer(item);
			if (lyr.isAvailable()){
				return selectedItems.length == 1;
			}
		}
		return false;
	}

	public boolean isVisible(ITocItem item, FLayer[] selectedItems) {
		if (isTocItemBranch(item)) {
			FLayer lyr = getNodeLayer(item);
				if ((lyr instanceof ClassifiableVectorial)) {
					if (!((lyr instanceof FLyrVect) &&
							!((FLyrVect)lyr).isPropertiesMenuVisible())){
						if (!(lyr instanceof FLyrAnnotation))
							return true;
					}

				}
		}
		return false;

	}


	public void execute(ITocItem item, FLayer[] selectedItems) {
//		long t1 = System.currentTimeMillis();

		FLayer lyr = getNodeLayer(item);
		ThemeManagerWindow fThemeManagerWindow = null;

		if (lyr.isAvailable()) {
			fThemeManagerWindow = new ThemeManagerWindow(lyr);
		}

		if (PluginServices.getMainFrame() == null) {
			JDialog dlg = new JDialog();
			fThemeManagerWindow.setPreferredSize(fThemeManagerWindow.getSize());
			dlg.getContentPane().add(fThemeManagerWindow);
			dlg.setModal(false);
			dlg.pack();
			dlg.setVisible(true);
//			System.err.println("open properties dialog time: "+(System.currentTimeMillis()-t1));
		} else {
//			System.err.println("open properties dialog time: "+(System.currentTimeMillis()-t1));
			if (fThemeManagerWindow != null){
				PluginServices.getMDIManager().addWindow(fThemeManagerWindow);
			}
		}

		Project project=((ProjectExtension)PluginServices.getExtension(ProjectExtension.class)).getProject();
		project.setModified(true);
	}

}
