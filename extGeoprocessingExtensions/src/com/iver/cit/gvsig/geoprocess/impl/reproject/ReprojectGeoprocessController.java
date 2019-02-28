/*
 * Created on 03-jul-2006
 *
 * gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
/* CVS MESSAGES:
*
* $Id$
* $Log$
* Revision 1.6  2007-09-19 16:09:14  jaume
* removed unnecessary imports
*
* Revision 1.5  2006/11/29 13:11:23  jmvivo
* Se ha añadido mas información al mensaje de error para los GeoprocessException: e.getMessage()
*
* Revision 1.4  2006/10/23 15:16:12  jmvivo
* implementados el getWidth y el getHeight
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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import org.cresques.cts.IProjection;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.edition.ShpSchemaManager;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.geoprocess.core.fmap.AbstractGeoprocessController;
import com.iver.cit.gvsig.geoprocess.core.fmap.GeoprocessException;
import com.iver.cit.gvsig.geoprocess.core.fmap.IGeoprocess;
import com.iver.cit.gvsig.geoprocess.core.gui.AddResultLayerTask;
import com.iver.cit.gvsig.geoprocess.core.gui.IGeoprocessUserEntries;
import com.iver.cit.gvsig.geoprocess.impl.reproject.fmap.ReprojectGeoprocess;
import com.iver.cit.gvsig.geoprocess.impl.reproject.gui.GeoprocessingReprojectPanel;
import com.iver.utiles.swing.threads.IMonitorableTask;
import com.iver.utiles.swing.threads.MonitorableDecoratorMainFirst;

public class ReprojectGeoprocessController extends AbstractGeoprocessController {

	private GeoprocessingReprojectPanel panel;
	private ReprojectGeoprocess reproject;

	public void setView(IGeoprocessUserEntries viewPanel) {
		this.panel = (GeoprocessingReprojectPanel) viewPanel;
	}

	public IGeoprocess getGeoprocess() {
		return new ReprojectGeoprocess();
	}

	public boolean launchGeoprocess() {
		FLyrVect inputLayer = panel.getInputLayer();
		FLayers layers = panel.getFLayers();
		File outputFile = null;
		try {
			outputFile = panel.getOutputFile();
		} catch (FileNotFoundException e3) {
			String error = PluginServices.getText(this, "Error_entrada_datos");
			String errorDescription = PluginServices.getText(this, "Error_seleccionar_resultado");
			panel.error(errorDescription, error);
			return false;
		}
		if (outputFile == null || (outputFile.getAbsolutePath().length() == 0)) {
			String error = PluginServices.getText(this, "Error_entrada_datos");
			String errorDescription = PluginServices.getText(this, "Error_seleccionar_resultado");
			panel.error(errorDescription, error);
			return false;
		}
		if (outputFile.exists()) {
			if (!panel.askForOverwriteOutputFile(outputFile)) {
				return false;
			}
		}
		reproject = (ReprojectGeoprocess) getGeoprocess();
		reproject.setFirstLayer(inputLayer);
		SHPLayerDefinition definition =
			(SHPLayerDefinition) reproject.createLayerDefinition();
        IProjection projection = panel.getTargetProjection();
        definition.setProjection(projection);
		definition.setFile(outputFile);
		ShpSchemaManager schemaManager = new ShpSchemaManager(outputFile.getAbsolutePath());
		IWriter writer = null;
		try {
			writer = getShpWriter(definition);
		} catch (Exception e1) {
			String error = PluginServices.getText(this, "Error_escritura_resultados");
			String errorDescription = PluginServices.getText(this, "Error_preparar_escritura_resultados");
			panel.error(errorDescription, error);
			return false;
		}
		reproject.setResultLayerProperties(writer, schemaManager);
		HashMap params = new HashMap();
		boolean onlySelected = panel.isFirstOnlySelected();
		params.put("firstlayerselection", new Boolean(onlySelected));
		IProjection previousProj = inputLayer.getProjection();
		if(previousProj.equals(projection)){
			String error = PluginServices.getText(this, "Error_entrada_datos");
			String errorDesc = PluginServices.getText(this, "Error_proyecciones_iguales");
			panel.error(errorDesc, error);
			return false;
		}
		params.put("targetProjection", projection);

		try {
			reproject.setParameters(params);
			reproject.checkPreconditions();
			IMonitorableTask task1 = reproject.createTask();
			if(task1 == null){
				return false;

			}
			AddResultLayerTask task2 = new AddResultLayerTask(reproject);
			task2.setLayers(layers);
			MonitorableDecoratorMainFirst globalTask = new MonitorableDecoratorMainFirst(task1,
					task2);
			if (globalTask.preprocess())
				PluginServices.cancelableBackgroundExecution(globalTask);

		} catch (GeoprocessException e) {
			String error = PluginServices.getText(this, "Error_ejecucion");
			String errorDescription = PluginServices.getText(this, "Error_fallo_geoproceso");
			errorDescription = "<html>" + errorDescription + ":<br>" + e.getMessage()+ "</html>";
			panel.error(errorDescription, error);
			return false;
		}
		return true;
	}

	public int getWidth() {
		return 750;
	}

	public int getHeight() {
		return 220;
	}

}

