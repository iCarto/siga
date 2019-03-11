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
 * Revision 1.5  2007/05/15 07:23:26  cesar
 * Add the finished method for execution from Event Dispatch Thread
 *
 * Revision 1.4  2007/03/06 16:48:14  caballero
 * Exceptions
 *
 * Revision 1.3  2006/08/11 17:20:32  azabala
 * *** empty log message ***
 *
 * Revision 1.2  2006/07/04 16:43:18  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2006/07/03 20:28:38  azabala
 * *** empty log message ***
 *
 *
 */
package com.iver.cit.gvsig.geoprocess.impl.reproject.fmap;

import java.util.Map;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.visitors.ProcessVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StartVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.fmap.layers.layerOperations.VectorialData;
import com.iver.cit.gvsig.fmap.operations.strategies.FeatureVisitor;
import com.iver.cit.gvsig.fmap.operations.strategies.SimpleStrategy;
import com.iver.cit.gvsig.geoprocess.core.fmap.AbstractGeoprocess;
import com.iver.cit.gvsig.geoprocess.core.fmap.DefinitionUtils;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeatureFactory;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeaturePersisterProcessor2;
import com.iver.cit.gvsig.geoprocess.core.fmap.GeoprocessException;
import com.iver.utiles.swing.threads.AbstractMonitorableTask;
import com.iver.utiles.swing.threads.IMonitorableTask;

public class ReprojectGeoprocess extends AbstractGeoprocess {

	/**
	 * Schema of the result layer
	 */

	private ILayerDefinition resultLayerDefinition;

	/**
	 * flag to only clip selection of input layer
	 */
	private boolean onlyInputLayerSelection = false;

	/**
	 * Processes features (writing them)
	 */
	FeaturePersisterProcessor2 processor;

	/**
	 * Target projection for features of the input layer
	 */
	private IProjection targetProjection;

	ReprojectTask task = null;

	public ReprojectGeoprocess() {
		super();
	}

	public void setFirstLayer(FLyrVect inputLayer){
		this.firstLayer = inputLayer;
	}

	public void setParameters(Map params) throws GeoprocessException {
		Boolean firstLayerSelection = (Boolean) params
				.get("firstlayerselection");
		if (firstLayerSelection != null)
			this.onlyInputLayerSelection = firstLayerSelection.booleanValue();
		this.targetProjection = (IProjection) params.get("targetProjection");

	}

	public void checkPreconditions() throws GeoprocessException {
		if (this.writer == null || this.schemaManager == null) {
			throw new GeoprocessException(
					"Operacion de reproyección sin especificar capa de resultados");
		}
		if (this.targetProjection == null)
			throw new GeoprocessException(
					"Geoproceso reproyección sin proyección destino");
	}

	public void process() throws GeoprocessException {
		try {
			createTask().run();
		} catch (Exception e) {
			throw new GeoprocessException(
					"Error durante ejecución del geoproceso", e);
		}

	}

	public ILayerDefinition createLayerDefinition() {
		if (resultLayerDefinition == null) {
			try {
				resultLayerDefinition = DefinitionUtils
						.createLayerDefinition(super.firstLayer);
			} catch (ReadDriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return resultLayerDefinition;
	}

	class ReprojectVisitor implements FeatureVisitor {

		FeaturePersisterProcessor2 processor;

		SelectableDataSource recordset;

		ILayerDefinition layerDefinition;

		FBitSet selection;

		ICoordTrans ct;

		ReprojectVisitor(ILayerDefinition layerDefinition,
				FeaturePersisterProcessor2 processor, FBitSet selection,
				ICoordTrans coordTrans) {

			this.layerDefinition = layerDefinition;
			this.processor = processor;
			this.selection = selection;
			this.ct = coordTrans;
		}

		public void visit(IGeometry g, int index) throws VisitorException, StopWriterVisitorException, ProcessVisitorException {
			if (g == null)
				return;
			if (selection != null) {
				if (!selection.get(index))
					return;
			}// if
			g.reProject(ct);
			IFeature feature = null;
			try {
				feature = createFeature(g, index);
			} catch (ReadDriverException e) {
				throw new ProcessVisitorException(recordset.getName(),e,"Error al crear al reproyectar "+index);
			}
			processor.processFeature(feature);
		}

		public String getProcessDescription() {
			return "";
		}

		public void stop(FLayer layer) throws StopWriterVisitorException, VisitorException {
			processor.finish();
		}

		public boolean start(FLayer layer) throws StartVisitorException {
			if (layer instanceof AlphanumericData
					&& layer instanceof VectorialData) {
				try {
					this.recordset = ((AlphanumericData) layer).getRecordset();
					this.processor.start();
				} catch (ReadDriverException e) {
					return false;
				}
				return true;
			}
			return false;
		}

		private IFeature createFeature(IGeometry geometry, int layerIndex)
				throws ReadDriverException {
			IFeature solution = null;
			FieldDescription[] fields = layerDefinition.getFieldsDesc();
			Value[] featureAttr = new Value[fields.length];
			int numFields = recordset.getFieldCount();
			for (int indexField = 0; indexField < numFields; indexField++) {
				// for each field of firstRs
				String fieldName = recordset.getFieldName(indexField);
				for (int j = 0; j < fields.length; j++) {
					if (fieldName.equalsIgnoreCase(fields[j].getFieldName())) {
						featureAttr[j] = recordset.getFieldValue(layerIndex,
								indexField);
						break;
					}// if
				}// for
			}// for
			// now we put null values
			for (int i = 0; i < featureAttr.length; i++) {
				if (featureAttr[i] == null)
					featureAttr[i] = ValueFactory.createNullValue();
			}
			solution = FeatureFactory.createFeature(featureAttr, geometry);
			return solution;
		}
	}

	class ReprojectTask extends AbstractMonitorableTask {

		private ReprojectTask() {
			setInitialStep(0);
			try {
				if (onlyInputLayerSelection) {
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
					"XYShift._Progress_Message"));

		}

		public void run() throws Exception {
			processor =
				new FeaturePersisterProcessor2(writer);
			FBitSet selection = null;
			if(onlyInputLayerSelection)
				selection = firstLayer.getRecordset().getSelection();
			IProjection from = firstLayer.getProjection();
			IProjection to = targetProjection;

//			ICoordTrans ct = new CoordTrans((CoordSys) to, (CoordSys) from);
			ICoordTrans ct = from.getCT(to);
			ReprojectVisitor visitor = new ReprojectVisitor(resultLayerDefinition,
					processor, selection, ct);
			try {
                new SimpleStrategy(firstLayer).process(visitor, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// TODO INTERNACIONALIZAR LOS MENSAJES
		public String getNote() {
			return "Reproyectando features..." + " " + getCurrentStep() + " "
					+ "de" + " " + getFinishStep();
		}

		public void cancel() {
			setCanceled(true);
			ReprojectGeoprocess.this.cancel();
		}

		/* (non-Javadoc)
		 * @see com.iver.utiles.swing.threads.IMonitorableTask#finished()
		 */
		public void finished() {
			// TODO Auto-generated method stub

		}
	}

	public IMonitorableTask createTask() {
		if(task == null)
			task = new ReprojectTask();
		return task;
	}

}
