/*
 * Created on 03-jul-2006
 *
 * gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
 * Revision 1.7  2007-09-19 16:09:14  jaume
 * removed unnecessary imports
 *
 * Revision 1.6  2007/06/20 10:50:31  jmvivo
 * Modificaci�n para estandarizar la busqueda de los html de descripciones.
 * Tambi�n se controla que, si no existe la descripci�n en el idioma corriente se usar� el ingl�s.
 *
 * Revision 1.5  2006/09/21 18:14:42  azabala
 * changes of appGvSig packages (document extensibility)
 *
 * Revision 1.4  2006/08/29 08:46:36  cesar
 * Rename the remaining method calls (extGeoprocessingExtensions was not in my workspace)
 *
 * Revision 1.3  2006/08/11 17:20:32  azabala
 * *** empty log message ***
 *
 * Revision 1.2  2006/07/04 16:43:18  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2006/07/03 20:28:29  azabala
 * *** empty log message ***
 *
 *
 */
package com.iver.cit.gvsig.geoprocess.impl.reproject;

import java.net.URL;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.geoprocess.core.GeoprocessPluginAbstract;
import com.iver.cit.gvsig.geoprocess.core.IGeoprocessController;
import com.iver.cit.gvsig.geoprocess.core.IGeoprocessPlugin;
import com.iver.cit.gvsig.geoprocess.core.gui.IGeoprocessUserEntries;
import com.iver.cit.gvsig.geoprocess.impl.reproject.gui.GeoprocessingReprojectPanel;
import com.iver.cit.gvsig.project.documents.view.gui.View;

public class ReprojectGeoprocessPlugin extends GeoprocessPluginAbstract  implements IGeoprocessPlugin {

	private static String dataConvertPkg;
	private static String geoprocessName;
	
	static{
		dataConvertPkg = 
			PluginServices.getText(null, "Conversion_de_datos");
		geoprocessName =
			PluginServices.getText(null, "Reproyectar");
	}
	
	
	
	
	public IGeoprocessUserEntries getGeoprocessPanel() {
		com.iver.andami.ui.mdiManager.IWindow view = PluginServices.getMDIManager().getActiveWindow();
		View vista = (View) view;
		FLayers layers = vista.getModel().getMapContext().getLayers();
		return (IGeoprocessUserEntries) new GeoprocessingReprojectPanel(layers);
	}

	public URL getImgDescription() {
		URL url = PluginServices.getIconTheme().getURL("xyshiftdesc-icon");
		return url;
	}

	public IGeoprocessController getGpController() {
		return new ReprojectGeoprocessController();
	}

	
	public String getNamespace() {
		return dataConvertPkg + "/" + geoprocessName;
	}
	
	public String toString(){
		return geoprocessName;
	}

}
