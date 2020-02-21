/*
 * Created on 24-sep-2007
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
*
*/
package org.gvsig.topology;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.cresques.cts.IProjection;
import org.gvsig.topology.topologyrules.MustBeLargerThanClusterTolerance;
import org.gvsig.topology.topologyrules.MustNotHaveRepeatedPoints;
import org.gvsig.topology.topologyrules.jtsisvalidrules.GeometryMustHaveValidCoordinates;
import org.gvsig.topology.util.LayerFactory;

import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.crs.CRSFactory;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

/**
 * Unit tests for Topology class.
 * 
 * @author azabala
 *
 */
public class TopologyTest extends TestCase {
	File baseDataPath;
	
	File baseDriversPath;
	
	IProjection PROJECTION_DEFAULT;
	
	ViewPort VIEWPORT;
	
	FLayers ROOT;
	
	SimpleTopologyErrorContainer errorContainer;
	
	Topology topology = null;
	
	Topology topology2 = null;
	
	FLyrVect multiPointLayer;
	
	FLyrVect lineLyrWithCollapsedCoords;
	
	FLyrVect shapeBasedLinearLyr;
	
	MapContext mapContext;
	
	
	public void setUp() throws Exception{
		super.setUp();
		URL url = TopologyTest.class.getResource("testdata");
		if (url == null)
			throw new Exception("No se encuentra el directorio con datos de prueba");

		baseDataPath = new File(url.getFile());
		if (!baseDataPath.exists())
			throw new Exception("No se encuentra el directorio con datos de prueba");

		baseDriversPath = new File("../_fwAndami/gvSIG/extensiones/com.iver.cit.gvsig/drivers");
		if (!baseDriversPath.exists())
			throw new Exception("Can't find drivers path " );

		com.iver.cit.gvsig.fmap.layers.LayerFactory.setDriversPath(baseDriversPath.getAbsolutePath());
		
		
		PROJECTION_DEFAULT = CRSFactory.getCRS("EPSG:23030");
		VIEWPORT = new ViewPort(PROJECTION_DEFAULT);
		mapContext = new MapContext(VIEWPORT);
		ROOT = mapContext.getLayers();
		errorContainer = new SimpleTopologyErrorContainer();
		
		topology = new Topology(mapContext, ROOT, 0.01, 1000, errorContainer );
		topology2 = new Topology(mapContext, ROOT, 0.01, 1000, errorContainer);
		
		
		multiPointLayer = LayerFactory.getLyrWithRepeatedCoords();
		lineLyrWithCollapsedCoords = LayerFactory.getLineLayerWithCollapsedCoords();
		shapeBasedLinearLyr = (FLyrVect) newLayer("vc1-1500.shp", "gvSIG shp driver");
	}
	
	public void tearDown() throws Exception{
		super.tearDown();
		topology = null;
		topology2 = null;
		multiPointLayer = null;
		lineLyrWithCollapsedCoords = null;
	}
	
	public  FLayer newLayer(String fileName,
			   String driverName)
		throws LoadLayerException {
		FLayer solution = null;	
		File file = new File(baseDataPath, fileName);
		solution = com.iver.cit.gvsig.fmap.layers.LayerFactory.createLayer(fileName,
								driverName,
								file, PROJECTION_DEFAULT);
		solution.setAvailable(true);
		return solution;
			
}
	
	/*
	 * To test in a Topology:
	 * 
	 * 1. If we change cluster tolerance, status must be reset:
	 *  -NON_VALIDATED.
	 *  -Dirty zones clear.
	 *  -Topology error clear.
	 *  -Cluster tolerance rules updated with new cluster tolerance
	 *  
	 *  
	 *  
	 *  2. If number of errors is greater than max number of errors during
	 *  a topology validation, process will be interrupted.
	 *  
	 *  3. We cant add rules with layers not referenced by the topology.
	 *  
	 *  4. deleted.
	 *  
	 *  5. If we add a new layer to a topology with status VALIDATED, new status
	 *  will be VALIDATED_WITH_DIRTY_ZONES, and a new dirty zone for the layer
	 *  full extent will be created. If layer shape type is POINT or MULTIPOINT,
	 *  status will be VALIDATED, because this kind of layer dont have MustBeGreaterThanClusterTolerance
	 *  rule associated.
	 *  
	 
	 TEST 6 NOT IMPLEMENTED YET
	 *  6. If a feature's geometry of a topology's layer is edited, a new dirty
	 *  zone will be create for its bounds:
	 *     -with the exception if topology status would be NON_VALIDATED.
	
	
	 *  7. If we validate a topology with status NON_VALIDATED, and it finds errors,
	 *  new status will be VALIDATED_WITH_ERRORS. 
	 *  If it doesnt find errors, status wll be VALIDATED.
	 *  
	 *  8. If two errors are in the same dirty zone, they dont add new dirty
	 *  zones.
	   
	 
	 *  9. If a topology error is added to a topology, dirty zones will be
	 *  updated (with the exception if topology error'geometry bounds is in an existent dirty zone)
	 *  
	 *  10. If we mark a topology exception as an exception, it will be removed
	 *  from dirty zones. If the number of exceptions is equal to the number of
	 *  errors, new status will be VALIDATED.
	 *  
	 *  11. If we demote a error from exception to error, it will be added to
	 *  dirty zones. If number of errors is superior to number of exceptions,
	 *  new status will be VALIDATED_WITH_ERRORS.
	 *  
	 *  TEST 12 NOT IMPLEMENTED YET
	 *  12. We must filter topology errors by rule, layer, geometry type, etc.
	 *  
	 * */
	public void testStatusAndCoherence() throws Exception{
		
		//first test: cluster tolerance 0.1d, not collapsed geometries
		topology.addLayer(lineLyrWithCollapsedCoords);
		topology.validate();
		int status = topology.getStatus();
		assertTrue(status == Topology.VALIDATED);
		int numberOfErrors = topology.getNumberOfErrors();
		assertTrue(numberOfErrors == 0);
		
		//second test: cluster tolerance 12d, one topology error
		topology.setClusterTolerance(12d);
		topology.validate();
		status = topology.getStatus();
		assertTrue(status == Topology.VALIDATED_WITH_ERRORS);
		numberOfErrors = topology.getNumberOfErrors();
		assertTrue(numberOfErrors == 1);
		TopologyError error = topology.getTopologyError(0);
		assertTrue(error.getViolatedRule().getClass().equals(MustBeLargerThanClusterTolerance.class));
		
		
		//third test: we add a point layer with repeated points
		topology.addLayer(multiPointLayer);
		MustNotHaveRepeatedPoints rule1 = 
			new MustNotHaveRepeatedPoints(multiPointLayer);
		topology.addRule(rule1);
		status = topology.getStatus();
		assertTrue(status == Topology.NOT_VALIDATED);
		int numberOfDirtyZones = topology.getNumberOfDirtyZones();
		assertTrue(numberOfDirtyZones == 0);
		
		topology.validate();
		status = topology.getStatus();
		assertTrue(status == Topology.VALIDATED_WITH_ERRORS);
		numberOfErrors = topology.getNumberOfErrors();
		assertTrue(numberOfErrors == 2);
		
		topology.resetStatus();
		topology.setMaxNumberOfErrors(1);
		topology.validate();
		assertTrue(topology.getNumberOfErrors() == 1);
		topology.resetStatus();
		topology.setMaxNumberOfErrors(1000);
		topology.validate();
		assertTrue(topology.getNumberOfErrors() == 2);
		
		//mark as exception
		for(int i = 0; i < numberOfErrors; i++){
			error = topology.getTopologyError(i);
			topology.markAsTopologyException(error);
		}
		assertTrue(topology.getStatus() == Topology.VALIDATED);

	}
	
	
	public void testAddRuleWithLayersNotReferenced(){
		MustNotHaveRepeatedPoints rule = 
			new MustNotHaveRepeatedPoints(multiPointLayer);
		boolean ok = false;
		try {
			topology2.addRule(rule);
			ok = true;
		} catch (RuleNotAllowedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TopologyRuleDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(!ok);
	}
	
	public void testDemoteExceptionsToErrors() throws RuleNotAllowedException, TopologyRuleDefinitionException{
		topology2.setClusterTolerance(12d);
		topology2.addLayer(multiPointLayer);
		topology2.addLayer(this.lineLyrWithCollapsedCoords);
		topology2.addRule(new MustNotHaveRepeatedPoints(multiPointLayer));
		
		topology2.validate();
		int numberOfErrors = topology2.getNumberOfErrors();
		assertTrue(numberOfErrors == 2);
		for(int i = 0; i < numberOfErrors; i++){
			TopologyError error = topology2.getTopologyError(i);
			topology2.markAsTopologyException(error);
		}
		assertTrue(topology2.getStatus() == Topology.VALIDATED);
		for(int i = 0; i < numberOfErrors; i++){
			TopologyError error = topology2.getTopologyError(i);
			topology2.demoteToError(error);
		}
		assertTrue(topology2.getStatus() == Topology.VALIDATED_WITH_ERRORS);
		
		//At this point, topology have two topologyerrors (MustNotHaveRepeatedPoints and MustBeGreaterThanClusterTolerance
		//shape types are LINE (FPolyline2D) and Point (FMultiPoint2D).
		List<TopologyError> errorList = topology2.getTopologyErrorsByLyr(lineLyrWithCollapsedCoords, null, true);
		assertTrue(errorList.size() == 1);
		
		errorList = topology.getTopologyErrorsByShapeType(FShape.LINE, null, true);
		assertTrue(errorList.size() == 1);
		
		
	}
	
	public void testTopologyPersistence() throws RuleNotAllowedException, TopologyRuleDefinitionException{
		topology2 = new Topology(mapContext, ROOT, 0.01, 1000, errorContainer);
		topology2.addLayer(this.shapeBasedLinearLyr);
		MustNotHaveRepeatedPoints ruleA = new MustNotHaveRepeatedPoints(topology2, shapeBasedLinearLyr);
		topology2.addRule(ruleA);
		GeometryMustHaveValidCoordinates ruleB = new 
			GeometryMustHaveValidCoordinates(topology2, shapeBasedLinearLyr);
		topology2.addRule(ruleB);
		
		String fileToSave1 = "/testTopology.xml";
		Map<String, Object> storageParams = new HashMap<String, Object>();
		storageParams.put(TopologyPersister.FILE_PARAM_NAME, fileToSave1);
		TopologyPersister.persist(topology2, storageParams);
		Topology topologyA = TopologyPersister.load(mapContext, storageParams);
		
		assertTrue(topology2.getRuleCount() == topologyA.getRuleCount());
		assertTrue(topology2.getLayerCount() == topologyA.getLayerCount());
	}
	
}

