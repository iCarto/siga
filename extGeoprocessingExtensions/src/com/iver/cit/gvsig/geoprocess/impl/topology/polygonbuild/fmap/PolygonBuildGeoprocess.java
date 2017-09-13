/*
 * Created on 15-dic-2006
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
 * $Id: PolygonBuildGeoprocess.java 21235 2008-06-05 14:08:38Z azabala $
 * $Log$
 * Revision 1.6  2007-08-13 11:51:09  jmvivo
 * *** empty log message ***
 *
 * Revision 1.5  2007/05/28 15:36:15  jaume
 * *** empty log message ***
 *
 * Revision 1.4  2007/05/15 07:23:25  cesar
 * Add the finished method for execution from Event Dispatch Thread
 *
 * Revision 1.3  2007/03/06 16:48:14  caballero
 * Exceptions
 *
 * Revision 1.2  2006/12/21 17:43:33  azabala
 * added filtering of dangling lines by dangle tolerance
 *
 * Revision 1.1  2006/12/21 17:23:27  azabala
 * *** empty log message ***
 *
 * Revision 1.2  2006/12/19 19:29:50  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2006/12/15 19:06:29  azabala
 * scheleton of polygon build
 *
 *
 */
package com.iver.cit.gvsig.geoprocess.impl.topology.polygonbuild.fmap;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.driver.exceptions.SchemaEditionException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.visitors.StopVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.ILineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.edition.ShpSchemaManager;
import com.iver.cit.gvsig.fmap.edition.writers.shp.ShpWriter;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.rendering.SingleSymbolLegend;
import com.iver.cit.gvsig.geoprocess.core.fmap.AbstractGeoprocess;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeaturePersisterProcessor2;
import com.iver.cit.gvsig.geoprocess.core.fmap.GeoprocessException;
import com.iver.cit.gvsig.geoprocess.core.fmap.XTypes;
import com.iver.cit.gvsig.topology.NodeError;
import com.iver.utiles.swing.threads.AbstractMonitorableTask;
import com.iver.utiles.swing.threads.IMonitorableTask;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.planargraph.NodeMap;

public class PolygonBuildGeoprocess extends AbstractGeoprocess {

	private static final SimpleMarkerSymbol symNodeError =(SimpleMarkerSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.POINT, Color.RED);
	private static final ILineSymbol symDangle = (ILineSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.LINE, Color.RED);
	private static final ILineSymbol symCutEdge = (ILineSymbol)SymbologyFactory.createDefaultSymbolByShapeType(FShape.LINE, Color.BLUE);
	private static final ILineSymbol symInvalidRing = (ILineSymbol)SymbologyFactory.createDefaultSymbolByShapeType(FShape.LINE, Color.YELLOW);
	static{
		symNodeError.setSize(10);
		symNodeError.setStyle(SimpleMarkerSymbol.SQUARE_STYLE);
		symNodeError.setOutlined(true);
		symNodeError.setOutlineColor(Color.RED);
	}
	/**
	 * Static counter to add like a suffix to the error's layer names
	 * */
	private static int numOcurrences = 0;

	// USER PARAMS
	private boolean onlyFirstLayerSelection = false;

	private boolean computeCleanBefore = false;

	private boolean applySnapTolerance = false;
	private double snapTolerance;

	private boolean applyDangleTolerance = false;
	private double dangleTolerance;

	private boolean addGroupOfLyrs = false;

	private GeometryFactory fact = new GeometryFactory();

	/**
	 * Relates an IWriter with the symbol of the layer
	 * (for error layers)
	 * */
	private HashMap writer2sym = new HashMap();

	/**
	 * Processes features (writing them in a persistent data store)
	 */
	FeaturePersisterProcessor2 processor;

	/**
	 * It saves all temporal writers (to save error geometries)
	 * */
	ArrayList tempWriters = new ArrayList();

	/**
	 * Schema of the result layer
	 */
	ILayerDefinition resultLayerDefinition;


	public PolygonBuildGeoprocess(FLyrVect inputLayer) {
		this.firstLayer = inputLayer;
	}

	public void setParameters(Map params) throws GeoprocessException {
		Boolean firstLayerSelection = (Boolean) params
				.get("firstlayerselection");
		if (firstLayerSelection != null)
			this.onlyFirstLayerSelection = firstLayerSelection.booleanValue();

		Boolean applySnap = (Boolean) params.get("applysnaptol");
		if (applySnap != null)
			this.applySnapTolerance = applySnap.booleanValue();
		Double snapTol = (Double) params.get("snaptolerance");
		if (snapTol != null)
			this.snapTolerance = snapTol.doubleValue();

		Boolean applyDange = (Boolean) params.get("applydangletol");
		if (applyDange != null)
			this.applyDangleTolerance = applyDange.booleanValue();

		Double dangleTol = (Double) params.get("dangletolerance");
		if (dangleTol != null)
			this.dangleTolerance = dangleTol.doubleValue();

		Boolean cleanBefore = (Boolean) params.get("computeclean");
		if (cleanBefore != null)
			this.computeCleanBefore = cleanBefore.booleanValue();

		Boolean groupOfLyrs = (Boolean) params.get("addgroupoflyrs");
		if (groupOfLyrs != null)
			this.addGroupOfLyrs = groupOfLyrs.booleanValue();

	}

	public void checkPreconditions() throws GeoprocessException {
		if (firstLayer == null)
			throw new GeoprocessException(
					"POLIGON BUILD: capa de entrada a null");
		if (this.writer == null || this.schemaManager == null) {
			throw new GeoprocessException(
					"Operacion de BUILD de polígonos sin especificar capa de resultados");
		}
		try {
			if (firstLayer.getSource().getShapeCount() == 0) {
				throw new GeoprocessException("Capa de entrada vacia");
			}
		} catch (ReadDriverException e) {
			throw new GeoprocessException(
					"Error al verificar si la capa está vacía");
		}
	}

	// TODO Move this stuff to AbstractGeoprocess
	public void process() throws GeoprocessException {
		try {
			createTask().run();
		} catch (Exception e) {
			throw new GeoprocessException("Error al ejecutar el geoproceso");
		}
	}

	/**
	 *
	 * Creates the schema (ILayerDefinition) of the result layer which will have
	 * all the polygons resulting from the BUILD
	 */
	public ILayerDefinition createLayerDefinition() {
		if (resultLayerDefinition == null) {
			resultLayerDefinition = getLayerDefinition();
		}
		return resultLayerDefinition;
	}

	/**
	 * Utility method to create schemas for all kind of layers this
	 * geoprocess could create (polygons, and error layers -dangles, etc-)
	 *
	 * */
	private ILayerDefinition getLayerDefinition(){
		SHPLayerDefinition definition = new SHPLayerDefinition();
		definition.setShapeType(XTypes.POLYGON);
		FieldDescription[] fields = new FieldDescription[1];
		fields[0] = new FieldDescription();
		fields[0].setFieldLength(10);
		fields[0].setFieldName("FID");
		fields[0].setFieldType(XTypes.BIGINT);
		definition.setFieldsDesc(fields);
	    return definition;
	}

	public IMonitorableTask createTask() {
		return new PolygonBuildTask();
	}


	class PolygonBuildTask extends AbstractMonitorableTask {

		PolygonBuildTask() {
			setInitialStep(0);
			int additionalSteps = 2;
			try {
				if (onlyFirstLayerSelection) {
					int numSelected = firstLayer.getRecordset().getSelection()
							.cardinality();
					setFinalStep(numSelected + additionalSteps);
				} else {
					int numShapes = firstLayer.getSource().getShapeCount();
					setFinalStep(numShapes + additionalSteps);
				}// else
			} catch (ReadDriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setDeterminatedProcess(true);
			setStatusMessage(PluginServices.getText(this,
					"PolygonBuild._Progress_Message"));

		}

		/**
		 * Verifies cancelation events, and return a boolean flag if processes
		 * must be stopped for this cancelations events.
		 *
		 * @param cancel
		 * @param va
		 * @param visitor
		 * @return
		 * @throws DriverIOException
		 */

		// TODO Move all this stuff and remove
		boolean verifyCancelation(ReadableVectorial va) {
			if (isCanceled()) {
				try {
					va.stop();
				} finally {
					return true;
				}
			}
			return false;
		}

		/**
		 * Processes the record "record" of the given vectorial data source
		 * (ReadableVectorial) to extract linear components with "lineFilter".
		 * (Previous step necessary to polygonize a linear layer).
		 *
		 * If user has choosen add to the toc a group of layers (with
		 * topological errors, pseudonodes, dangles, etc) this method computes
		 * pseudonodes also.
		 * @throws StopWriterVisitorException
		 */
		void process(ReadableVectorial va,
				FeaturePersisterProcessor2 processor,
				LinearComponentExtracter lineFilter,
				NodeMap nodeMap,
				int record) throws ReadDriverException, StopVisitorException {

			if (verifyCancelation(va)) {
				processor.finish();
				return;
			}
			reportStep();
			IGeometry g;
			try {
				g = va.getShape(record);
			} catch (ExpansionFileReadException e) {
				throw new ReadDriverException(va.getDriver().getName(),e);
			}
			if(g == null)
				return;
			Geometry jtsGeom = g.toJTSGeometry();
			jtsGeom.apply(lineFilter);

			// In parallel to the line filtering, if nodeMap != null we
			// look for pseudonodes
			if(nodeMap != null){
				Coordinate[] coords = jtsGeom.getCoordinates();
			    if (jtsGeom.isEmpty())
			    	return;
			    Coordinate[] linePts = CoordinateArrays.
			    			removeRepeatedPoints(coords);
			    Coordinate startPt = linePts[0];
			    Coordinate endPt = linePts[linePts.length - 1];

			    NodeError nStart = (NodeError) nodeMap.find(startPt);
			    NodeError nEnd = (NodeError) nodeMap.find(endPt);
			    if (nStart == null)
			    {
			    	nStart = new NodeError(startPt);
			    	nodeMap.add(nStart);
			    }else
			    	nStart.setOccurrences(nStart.getOccurrences()+1);
			    
			    if (nEnd == null)
			    {
			    	nEnd = new NodeError(endPt);
			    	nodeMap.add(nEnd);
			    }
			    else
			    	nEnd.setOccurrences(nEnd.getOccurrences()+1);
			}// if nodeMap
		}



		public void run() throws Exception {
			processor = new FeaturePersisterProcessor2(writer);
			Polygonizer polygonizer = new Polygonizer();
			try {
				processor.start();
				ReadableVectorial va = firstLayer.getSource();
				va.start();

				List linesList = new ArrayList();
				LinearComponentExtracter lineFilter = new LinearComponentExtracter(
						linesList);

				NodeMap nodeMap = null;
				if(addGroupOfLyrs)
					nodeMap = new NodeMap();
				if (onlyFirstLayerSelection) {
					FBitSet selection = firstLayer.getRecordset()
							.getSelection();
					for (int i = selection.nextSetBit(0); i >= 0; i = selection
							.nextSetBit(i + 1)) {
						process(va, processor, lineFilter, nodeMap, i);
					}
				} else {
					for (int i = 0; i < va.getShapeCount(); i++) {// for each
						process(va, processor, lineFilter, nodeMap, i);
					}// for
				}// if selection
				va.stop();

				// here lineList has all the linear elements
				if (computeCleanBefore) {
					linesList = cleanLines(linesList);
				}
				for (Iterator i = linesList.iterator(); i.hasNext(); ) {
				      Geometry g = (Geometry) i.next();
				      polygonizer.add(g);
				}

				reportStep();

				// First we save polygons from build
				List polygons = (List) polygonizer.getPolygons();
				for(int i = 0; i < polygons.size(); i++){
					Geometry geom = (Geometry) polygons.get(i);
					Value[] values = new Value[1];
					values[0] = ValueFactory.createValue(i);
					IGeometry igeom = FConverter.jts_to_igeometry(geom);
					DefaultFeature feature = new
						DefaultFeature(igeom, values, (i+""));
					processor.processFeature(feature);
				}
				processor.finish();

				reportStep();
				if(addGroupOfLyrs){
					Collection cutEdgesLines = (List) polygonizer.getCutEdges();
					Collection danglingLines = polygonizer.getDangles();
					Collection invalidRingLines = (List) polygonizer.getInvalidRingLines();
					List nodeErrors = new ArrayList();
					// Obtain pseudonodes
					// (we look for nodes of valency 1)
					Iterator it = nodeMap.iterator();
					while (it.hasNext()){
						NodeError node = (NodeError) it.next();
						if (node.getOccurrences() == 1){
							FPoint2D p = FConverter.coordinate2FPoint2D(
									node.getCoordinate());
							IGeometry gAux = ShapeFactory.createPoint2D(p);
							nodeErrors.add(gAux.toJTSGeometry());
						}// if
					}// while

					
					String fileName = ((ShpWriter)writer).getShpPath();
					String layerName = null;
					int fileNameStart = fileName.lastIndexOf(File.separator) + 1;
					if(fileNameStart == -1)
						fileNameStart = 0;
					layerName = fileName.substring(fileNameStart, fileName.length());
					if(layerName.endsWith(".shp"))
						layerName = layerName.substring(0, layerName.length() - 4);
					
					if(cutEdgesLines != null){
						if(cutEdgesLines.size() > 0)
							writeCutEdgeLines(cutEdgesLines, layerName);
					}

					if(danglingLines != null){
						//check to filter dangling lines by length
						if(applyDangleTolerance){
							ArrayList filteredDangles = new ArrayList();
							Iterator iterator = danglingLines.iterator();
							while(iterator.hasNext()){
								Geometry geom = (Geometry) iterator.next();
								if(geom.getLength() >= dangleTolerance)
									filteredDangles.add(geom);
							}//while
							danglingLines = filteredDangles;
						}
						if(danglingLines.size() > 0)
							writeDanglingLines(danglingLines, layerName);
					}

					if(invalidRingLines != null){
						if(invalidRingLines.size() > 0)
							writeInvalidRingLines(invalidRingLines, layerName);
					}

					if(nodeErrors != null){
						if(nodeErrors.size() > 0)
							writeNodeErrors(nodeErrors, layerName);
					}
				}// if addGroupOfLayers
			} catch (ReadDriverException e) {
				e.printStackTrace();
			}
		}

		public String getNote() {
			String buildText = PluginServices.getText(this,
					"Generando_topologia_de_poligonos");
			String of = PluginServices.getText(this, "de");
			return buildText + " " + getCurrentStep() + " " + of + " "
					+ getFinishStep();
		}

		public void cancel() {
			setCanceled(true);
			PolygonBuildGeoprocess.this.cancel();
		}

		/* (non-Javadoc)
		 * @see com.iver.utiles.swing.threads.IMonitorableTask#finished()
		 */
		public void finished() {
			// TODO Auto-generated method stub

		}

	}// PolygonBuildTask

	/**
	 * This method apply a topology clean to the list of lines.
	 *
	 * This clean is made in memory (against LineCleanGeoprocess, that makes
	 * this clean without memory consumption).
	 *
	 */
	// TODO Move this method to a JTS Utility class
	List cleanLines(List lines) {
		Geometry linesGeom = fact.createMultiLineString(GeometryFactory
				.toLineStringArray(lines));
		Geometry empty = fact.createMultiLineString(null);
		Geometry noded = linesGeom.union(empty);
		List nodedList = new ArrayList();
		nodedList.add(noded);
		return nodedList;
	}


	void writeGeometriesInMemory(Collection geometries, int geometryType, ISymbol symbol, String fileName) throws InitializeWriterException, SchemaEditionException, VisitorException {

		ShpWriter writer = new ShpWriter();
		File newFile = new File(fileName);
		writer.setFile(newFile);
		SHPLayerDefinition schema = (SHPLayerDefinition) getLayerDefinition();
		schema.setShapeType(geometryType);
		writer.initialize(schema);
		schema.setFile(newFile);
		ShpSchemaManager schemaManager =
			new ShpSchemaManager(newFile.getAbsolutePath());
		schemaManager.createSchema(schema);
		FeaturePersisterProcessor2 tempProcessor = new
			FeaturePersisterProcessor2(writer);
		tempProcessor = new FeaturePersisterProcessor2(writer);
		tempProcessor.start();
		Iterator it = geometries.iterator();
		int i = 0;
		while(it.hasNext()){
			Geometry geom = (Geometry) it.next();
			Value[] values = new Value[1];
			values[0] = ValueFactory.createValue(i);
			IGeometry igeom = FConverter.jts_to_igeometry(geom);
			DefaultFeature feature = new
				DefaultFeature(igeom, values, (i+""));
			tempProcessor.processFeature(feature);
			i++;
		}
		tempProcessor.finish();
		//We save the information to recover these layers after
		//(to add them to the toc)
		tempWriters.add(writer);

		//saves the symbol specified for this writer, to use it
		//when we will add the layer derived of the writer to the TOC
		writer2sym.put(writer, symbol);
	}



	void writeCutEdgeLines(Collection cutEdgeLines, String layerName) throws InitializeWriterException, SchemaEditionException, VisitorException{
		String temp = System.getProperty("java.io.tmpdir") 	+ layerName +
		"_cutEdgeLines" +
		(numOcurrences++)+
		".shp";
		writeGeometriesInMemory(cutEdgeLines, XTypes.LINE, symCutEdge, temp);
	}


	void writeDanglingLines(Collection danglingLines, String layerName) throws InitializeWriterException, SchemaEditionException, VisitorException{
		String temp = System.getProperty("java.io.tmpdir")  + layerName +
		"_danglingLines" +
		(numOcurrences++)+
		".shp";
		writeGeometriesInMemory(danglingLines, XTypes.LINE, symDangle, temp);
	}

	void writeInvalidRingLines(Collection invalidRingLines, String layerName) throws InitializeWriterException, SchemaEditionException, VisitorException{
		String temp = System.getProperty("java.io.tmpdir")  + layerName +
		"_invalidRing" +
		(numOcurrences++)+
		".shp";
		writeGeometriesInMemory(invalidRingLines, XTypes.LINE,symInvalidRing, temp);
	}

	void writeNodeErrors(Collection nodeErrors, String layerName) throws InitializeWriterException, SchemaEditionException, VisitorException{
		String temp = System.getProperty("java.io.tmpdir") + layerName +
		"_pseudonodes" +
		(numOcurrences++)+
		".shp";
		writeGeometriesInMemory(nodeErrors, XTypes.POINT,symNodeError, temp);
	}


	public FLayer getResult() throws GeoprocessException {
		if(! addGroupOfLyrs){
			// user choose in GUI not to load errors in TOC
			return super.getResult();
		}else{
			if(tempWriters.size() == 0)
				return super.getResult();
			IWriter[] writers = new IWriter[tempWriters.size()];
			tempWriters.toArray(writers);

	    MapContext map = firstLayer.getMapContext();
			FLayers solution = new FLayers();//(map,map.getLayers());
			solution.setMapContext(map);
			solution.setParentLayer(map.getLayers());
			solution.setName("Build");
			solution.addLayer(super.getResult());
			for(int i = 0; i < writers.length; i++){
				FLyrVect layer = (FLyrVect) createLayerFrom(writers[i]);
				ISymbol symbol = (ISymbol) writer2sym.get(writers[i]);
				try {
					layer.setLegend(new SingleSymbolLegend(symbol));
				} catch (Exception e) {
					throw new GeoprocessException("Error al crear la leyenda de una de las capas resultado", e);
				}
				solution.addLayer(layer);
			}
			return solution;
		}//else
	}
}
