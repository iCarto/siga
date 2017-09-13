/*
 * Created on 10-oct-2006
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
* $Id: LineCleanGeoprocess.java 24094 2008-10-19 07:44:04Z azabala $
* $Log$
* Revision 1.4  2007-07-12 11:10:24  azabala
* bug 2617 solved (clean fails with multilinestring geometries)
*
* Revision 1.3  2007/05/15 07:23:26  cesar
* Add the finished method for execution from Event Dispatch Thread
*
* Revision 1.2  2007/03/06 16:48:14  caballero
* Exceptions
*
* Revision 1.1  2006/12/21 17:23:27  azabala
* *** empty log message ***
*
* Revision 1.1  2006/12/04 19:42:23  azabala
* *** empty log message ***
*
* Revision 1.8  2006/11/14 18:34:16  azabala
* *** empty log message ***
*
* Revision 1.7  2006/11/14 18:00:57  azabala
* internationalized texts
*
* Revision 1.6  2006/11/13 20:41:08  azabala
* *** empty log message ***
*
* Revision 1.5  2006/11/10 13:22:57  azabala
* better syncronization of clean and build network (use of pipetask)
*
* Revision 1.4  2006/11/09 21:08:32  azabala
* *** empty log message ***
*
* Revision 1.3  2006/10/19 16:06:48  azabala
* *** empty log message ***
*
* Revision 1.2  2006/10/17 18:25:53  azabala
* *** empty log message ***
*
* Revision 1.1  2006/10/10 18:50:17  azabala
* First version in CVS
*
*
*/
package com.iver.cit.gvsig.geoprocess.impl.topology.lineclean.fmap;

import java.io.File;
import java.util.Map;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.LayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.edition.ShpSchemaManager;
import com.iver.cit.gvsig.fmap.edition.writers.shp.ShpWriter;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.geoprocess.core.fmap.AbstractGeoprocess;
import com.iver.cit.gvsig.geoprocess.core.fmap.DefinitionUtils;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeaturePersisterProcessor2;
import com.iver.cit.gvsig.geoprocess.core.fmap.GeoprocessException;
import com.iver.cit.gvsig.geoprocess.core.fmap.XTypes;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.cit.gvsig.util.SnappingCoordinateMap;
import com.iver.utiles.swing.threads.AbstractMonitorableTask;
import com.iver.utiles.swing.threads.IMonitorableTask;
import com.iver.utiles.swing.threads.IPipedTask;

public class LineCleanGeoprocess extends AbstractGeoprocess {

	/**
	 * Schema of the result layer
	 */
	private ILayerDefinition resultLayerDefinition;


	/**
	 * flag to only clip selection of input layer
	 */
	private boolean onlyFirstLayerSelection = false;
	
	/**
	 * flag to mark if must create a layer with the detected
	 * pseudonodes
	 */
	private boolean createLyrsWithErrorGeometries = false;

	/**
	 * Processes features (writing them)
	 */
	FeaturePersisterProcessor2 processor;

	/**
	 * Writer to save in a temporal layer intersections
	 */
	private IWriter intersectionsWriter;
	/**
	 * Processor that saves created features using the IWriter
	 */
	FeaturePersisterProcessor2 intersectsProcessor;


	public LineCleanGeoprocess(FLyrVect inputLayer){
		this.firstLayer = inputLayer;
	}


	public void setParameters(Map params) throws GeoprocessException {
		Boolean firstLayerSelection = (Boolean) params.get("firstlayerselection");
		if (firstLayerSelection != null)
			this.onlyFirstLayerSelection =
				firstLayerSelection.booleanValue();
		
		Boolean createLyrsWithError = (Boolean) params.get("createlayerswitherrors");
		if (createLyrsWithError != null)
			this.createLyrsWithErrorGeometries =
				createLyrsWithError.booleanValue();

	}

	public void checkPreconditions() throws GeoprocessException {
		if (firstLayer == null)
			throw new GeoprocessException("CLEAN: capa de entrada a null");
		if (this.writer == null || this.schemaManager == null) {
			throw new GeoprocessException(
					"Operacion de CLEAN sin especificar capa de resultados");
		}
		try {
			if(firstLayer.getSource().getShapeCount() == 0){
				throw new GeoprocessException(
				"Capa de entrada vacia");
			}
		} catch (ReadDriverException e) {
			throw new GeoprocessException(
			"Error al verificar si la capa está vacía");
		}
	}

	public void process() throws GeoprocessException {
		try {
			createTask().run();
		} catch (Exception e) {
			throw new GeoprocessException("Error al ejecutar el geoproceso", e);
		}
	}

	public ILayerDefinition createLayerDefinition() {
		if (resultLayerDefinition == null) {
			try {
				resultLayerDefinition = DefinitionUtils.
							createLayerDefinition(firstLayer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resultLayerDefinition;
	}


	class LineCleanTask extends AbstractMonitorableTask implements IPipedTask{

		private LineCleanTask() {
			setInitialStep(0);
			try {
				if (onlyFirstLayerSelection) {
					int numSelected = firstLayer.getRecordset().getSelection()
							.cardinality();
					setFinalStep(numSelected);
				} else {
					int numShapes = firstLayer.getSource().getShapeCount();
					setFinalStep(numShapes);
				}// else
			} catch (ReadDriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setDeterminatedProcess(true);
			setStatusMessage(PluginServices.getText(this,
					"LineClean._Progress_Message"));

		}

		/**
		 * Verifies cancelation events, and return a boolean flag if processes must
		 * be stopped for this cancelations events.
		 *
		 * @param cancel
		 * @param va
		 * @param visitor
		 * @return
		 * @throws DriverIOException
		 */
		protected boolean verifyCancelation(ReadableVectorial va) {
			if (isCanceled()) {
				try {
					va.stop();
				} finally {
					return true;
				}
			}
			return false;
		}


		public void run() throws Exception {
			processor =
				new FeaturePersisterProcessor2(writer);

			intersectionsWriter = new ShpWriter();
			String temp = System.getProperty("java.io.tmpdir") +
					"/intersections_" +
					System.currentTimeMillis() +
					".shp";
			File newFile = new File(temp);
			((ShpWriter) intersectionsWriter).setFile(newFile);

			ILayerDefinition intersectDefinition = new SHPLayerDefinition();
			intersectDefinition.setShapeType(XTypes.POINT);
			FieldDescription[] intersectFields = new FieldDescription[2];
			intersectFields[0] = new FieldDescription();
			intersectFields[0].setFieldLength(10);
			intersectFields[0].setFieldDecimalCount(0);
			intersectFields[0].setFieldName("FID1");
			intersectFields[0].setFieldType(XTypes.INTEGER);
			intersectFields[1] = new FieldDescription();
			intersectFields[1].setFieldLength(10);
			intersectFields[1].setFieldDecimalCount(0);
			intersectFields[1].setFieldName("FID2");
			intersectFields[1].setFieldType(XTypes.INTEGER);
			intersectDefinition.setFieldsDesc(intersectFields);

			((ShpWriter) intersectionsWriter).initialize(
					(LayerDefinition) intersectDefinition);
			((SHPLayerDefinition) intersectDefinition).setFile(newFile);

			ShpSchemaManager interSchMg =
				new ShpSchemaManager(newFile.getAbsolutePath());
			interSchMg.createSchema(intersectDefinition);

			intersectsProcessor = new
				FeaturePersisterProcessor2(intersectionsWriter);

			FBitSet selection = null;
			SnappingCoordinateMap coordMap =
				new SnappingCoordinateMap(LineCleanVisitor.DEFAULT_SNAP);
			LineCleanVisitor visitor =
				new LineCleanVisitor(processor,
						             intersectsProcessor,
						onlyFirstLayerSelection,
						resultLayerDefinition,
						intersectDefinition,
						firstLayer,
						firstLayer.getRecordset(), coordMap);

			try {
				processor.start();
				intersectsProcessor.start();

				ReadableVectorial va = firstLayer.getSource();
				va.start();
				for (int i = 0; i < va.getShapeCount(); i++) {// for each geometry
					if (verifyCancelation(va)) {
						intersectsProcessor.finish();
						return;
					}
					if(selection != null){
						if (selection.get(i)) {
								reportStep();
								visitor.visit(va.getShape(i), i);
						}

					}else{
						reportStep();
						visitor.visit(va.getShape(i), i);
					}
				}// for
				va.stop();
				processor.finish();
				intersectsProcessor.finish();


			} catch (ReadDriverException e) {
				e.printStackTrace();
			}
		}

		// TODO INTERNACIONALIZAR LOS MENSAJES
		public String getNote() {
			String cleaningText = PluginServices.getText(this, "Limpiando_lineas");
			String of = PluginServices.getText(this, "de");
			return cleaningText + " " + getCurrentStep() + " "
					+ of + " " + getFinishStep();
		}

		public void cancel() {
			setCanceled(true);
			LineCleanGeoprocess.this.cancel();
		}

		/* (non-Javadoc)
		 * @see com.iver.utiles.swing.threads.IPipedTask#getResult()
		 */
		public Object getResult() {
			try {
				return LineCleanGeoprocess.this.getResult();
			} catch (GeoprocessException e) {
				return null;
			}
		}

		/* (non-Javadoc)
		 * @see com.iver.utiles.swing.threads.IPipedTask#setEntry(java.lang.Object)
		 */
		public void setEntry(Object object) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see com.iver.utiles.swing.threads.IMonitorableTask#finished()
		 */
		public void finished() {
			// TODO Auto-generated method stub

		}
	}


	public IMonitorableTask createTask() {
		return new LineCleanTask();
	}


	public FLayer getResult() throws GeoprocessException {
		FLyrVect cleanedLayer = (FLyrVect) createLayerFrom(this.writer);
		FLyrVect pseudonodes = null;
		
		if(this.createLyrsWithErrorGeometries){
			pseudonodes = (FLyrVect) createLayerFrom(this.intersectionsWriter);
			try {
				if(pseudonodes.getSource().getShapeCount() != 0){
	
					FLayers solution = new FLayers();//(map,null);
					solution.setMapContext(firstLayer.getMapContext());
					solution.setName(this.firstLayer.getName()+"_cleaned");
					solution.addLayer(cleanedLayer);
					solution.addLayer(pseudonodes);
					return solution;
				}
			} catch (ReadDriverException e) {
				throw new GeoprocessException("Error de lectura de datos");
			}
		}//if
		
		return cleanedLayer;
	}

}

