/*
 * Created on 28-jun-2006
 *
 * gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
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
* Revision 1.4  2006-12-21 17:23:48  azabala
* *** empty log message ***
*
* Revision 1.3  2006/07/03 20:28:20  azabala
* *** empty log message ***
*
* Revision 1.2  2006/06/29 17:58:31  azabala
* *** empty log message ***
*
* Revision 1.1  2006/06/28 18:17:21  azabala
* first version in cvs
*
*
*/
package com.iver.cit.gvsig.geoprocess.impl;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.geoprocess.impl.reproject.ReprojectGeoprocessPlugin;
import com.iver.cit.gvsig.geoprocess.impl.xyshift.XYShiftGeoprocessPlugin;
import com.iver.utiles.extensionPoints.ExtensionPoints;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;

public class RegisterGeoprocessExtension extends Extension{

	public void initialize() {
		ExtensionPoints extensionPoints = 
			ExtensionPointsSingleton.getInstance();
		extensionPoints.add("GeoprocessManager",
				"XYSHIFT", 
				XYShiftGeoprocessPlugin.class);
		extensionPoints.add("GeoprocessManager",
				"REPROJECT", 
				ReprojectGeoprocessPlugin.class);
			
		registerIcons();
	}
	
	private void registerIcons(){
		PluginServices.getIconTheme().registerDefault(
				"xyshiftdesc-icon",
				XYShiftGeoprocessPlugin.class.getResource("resources/xyshiftdesc.png")
			);
//		PluginServices.getIconTheme().registerDefault(
//				"polygbuilddesc-icon",
//				LineCleanGeoprocessPlugin.class.getResource("resources/polygbuilddesc.png")
//			);
//		PluginServices.getIconTheme().registerDefault(
//				"linecleandesc-icon",
//				LineCleanGeoprocessPlugin.class.getResource("resources/linecleandesc.png")
//			);
	}
	
	public void execute(String actionCommand) {
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		return true;
	}

}

