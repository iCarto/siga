/*
 * Created on 26-jun-2006
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
* Revision 1.8  2007-09-19 16:06:05  jaume
* removed unnecessary imports
*
* Revision 1.7  2007/06/20 10:50:32  jmvivo
* Modificaci�n para estandarizar la busqueda de los html de descripciones.
* Tambi�n se controla que, si no existe la descripci�n en el idioma corriente se usar� el ingl�s.
*
* Revision 1.6  2007/03/06 16:47:58  caballero
* Exceptions
*
* Revision 1.5  2006/09/15 10:42:54  caballero
* extensibilidad de documentos
*
* Revision 1.4  2006/08/29 07:56:30  cesar
* Rename the *View* family of classes to *Window* (ie: SingletonView to SingletonWindow, ViewInfo to WindowInfo, etc)
*
* Revision 1.3  2006/08/29 07:21:09  cesar
* Rename com.iver.cit.gvsig.fmap.Fmap class to com.iver.cit.gvsig.fmap.MapContext
*
* Revision 1.2  2006/08/11 16:20:24  azabala
* *** empty log message ***
*
* Revision 1.1  2006/06/27 16:11:41  azabala
* toString() added to Plugin interface to force textual representation of geoprocess plugins
*
*
*/
package com.iver.cit.gvsig.geoprocess.impl.dissolve;

import java.net.URL;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.geoprocess.core.GeoprocessPluginAbstract;
import com.iver.cit.gvsig.geoprocess.core.IGeoprocessController;
import com.iver.cit.gvsig.geoprocess.core.IGeoprocessPlugin;
import com.iver.cit.gvsig.geoprocess.core.gui.IGeoprocessUserEntries;
import com.iver.cit.gvsig.geoprocess.impl.dissolve.gui.GeoProcessingDissolvePanel2;
import com.iver.cit.gvsig.geoprocess.manager.GeoprocessManager;
import com.iver.cit.gvsig.project.documents.view.gui.View;

public class DissolveGeoprocessPlugin extends GeoprocessPluginAbstract implements IGeoprocessPlugin {
	
	private static String analisisPkg;
	private static String agregationPkg;
	private static String agregationPkgDesc;
	private static String geoprocessName;
	
	static{
		analisisPkg = PluginServices.getText(null, "Analisis");
		agregationPkg = PluginServices.getText(null, "Agregacion");
		geoprocessName = PluginServices.getText(null, "Disolver");
		
		agregationPkgDesc = PluginServices.getText(null, "Agregacion_Desc");
		GeoprocessManager.
		registerPackageDescription(analisisPkg + "/" + agregationPkg,
				agregationPkgDesc);
	}
	
	public IGeoprocessUserEntries getGeoprocessPanel() {
		View vista = (View)PluginServices.getMDIManager().getActiveWindow();
		MapContext mapContext = vista.getModel().getMapContext();
        FLayers layers = mapContext.getLayers();
        GeoProcessingDissolvePanel2 dataSelectionPanel = 
			new GeoProcessingDissolvePanel2(layers);
        return dataSelectionPanel;
	}


	public URL getImgDescription() {
		return PluginServices.getIconTheme().getURL("dissolvedesc-resource");
	}

	public IGeoprocessController getGpController() {
		return new DissolveGeoprocessController();
	}

	public String getNamespace() {
		return analisisPkg + "/" + agregationPkg + "/" + geoprocessName;
	}

	public String toString(){
		return geoprocessName;
	}

}

