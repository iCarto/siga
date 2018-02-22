/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
 *   Av. Blasco Ibáñez, 50
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
package com.iver.cit.gvsig.gui.selectionByTheme;

import java.util.ArrayList;
import java.util.List;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayersIterator;
import com.iver.cit.gvsig.project.documents.view.gui.View;



public class DefaultSelectionByThemeModel
	implements SelectionByThemeModel {

	private View view;
	
	public DefaultSelectionByThemeModel() {
		IWindow view = PluginServices.getMDIManager().getActiveWindow();
		if (view instanceof View){
			this.view = (View) view;
		}
		
	}

	/**
	 * @see com.iver.cit.gvsig.gui.selectionByTheme.SelectionByThemeModel#getViews()
	 */
	public FLayers getLayers() {			
		return view.getModel().getMapContext().getLayers();
	}
	
	public List<FLayer> getInnerLayers() {
		List<FLayer> flayers = new ArrayList<FLayer>();

		LayersIterator it = new LayersIterator(view.getModel().getMapContext().getLayers()) {
		    @Override
		    public boolean evaluate(FLayer layer) {
		        return layer instanceof FLyrVect;
		    }
		};

		while (it.hasNext()) {
		    flayers.add(it.nextLayer());
		}
		return (List<FLayer>) flayers;
	}




	
}
