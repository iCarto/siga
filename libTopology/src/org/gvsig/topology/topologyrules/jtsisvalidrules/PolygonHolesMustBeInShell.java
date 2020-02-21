/*
 * Created on 10-abr-2006
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
 * $Id: 
 * $Log: 
 */
package org.gvsig.topology.topologyrules.jtsisvalidrules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.topologyrules.JtsValidRule;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.XMLEntity;
import com.vividsolutions.jts.algorithm.PointInRing;
import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.EdgeIntersectionList;
import com.vividsolutions.jts.geomgraph.GeometryGraph;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

/**
 * This rule checks that all polygon holes must be contained in the exterior shell.
 * 
 * @author Alvaro Zabala
 *
 */
public class PolygonHolesMustBeInShell extends AbstractTopologyRule {
	
    private static List<ITopologyErrorFix> automaticErrorFixes = new ArrayList<ITopologyErrorFix>();
	
    private static String RULE_NAME = Messages.getText("POLYGON_HOLES_MUST_BE_IN_SHELL");
	
	private static final Color DEFAULT_ERROR_COLOR = Color.BLACK;

	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = 
		(MultiShapeSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.MULTI, 
																DEFAULT_ERROR_COLOR);
	static {
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
	}



	private MultiShapeSymbol errorSymbol;
	
	private double snapTolerance;
	
	private JtsValidRule parentRule;
	
	/**
	 * Implementation of PointInRing that applies snap tolerance
	 * 
	 * TODO Move to an external class 
	 */
	private class SnapPointInRing implements PointInRing {

		LinearRing ring;
		double snapTolerance;
		
		
		
		SnapPointInRing(LinearRing ring, double snapTolerance){
			this.ring = ring;
			this.snapTolerance = snapTolerance;
		}
		
		public boolean isInside(Coordinate pt) {
			return SnapCGAlgorithms.isPointInRing(pt, ring.getCoordinates(), snapTolerance);
		}
		
	}
	
	public PolygonHolesMustBeInShell(Topology topology, FLyrVect originLyr, double snapTolerance){
		super(topology, originLyr);
		this.snapTolerance = snapTolerance;
	}
	
	public PolygonHolesMustBeInShell(FLyrVect originLyr, double snapTolerance){
		this(null, originLyr, snapTolerance);
	}
	
	public PolygonHolesMustBeInShell(){}
	
	public String getName() {
		return RULE_NAME;
	}

	public void checkPreconditions() throws TopologyRuleDefinitionException {
		try {
			int shapeType = this.originLyr.getShapeType();
			if(shapeType == FShape.POLYGON || shapeType == FShape.MULTI)
				throw new TopologyRuleDefinitionException();
		} catch (ReadDriverException e) {
			e.printStackTrace();
			throw new TopologyRuleDefinitionException("Error leyendo el tipo de geometria del driver",e);
		}	
	
	
	}

	public void validateFeature(IFeature feature) {
		Geometry jtsGeo = feature.getGeometry().toJTSGeometry();
		if (jtsGeo instanceof Polygon) {
			checkHolesInShell((Polygon)jtsGeo, new GeometryGraph(0, jtsGeo), feature);
		} else if (jtsGeo instanceof MultiPolygon) {
			MultiPolygon multiPoly = (MultiPolygon)jtsGeo;
			for(int i = 0; i < multiPoly.getNumGeometries(); i++){
				Polygon polygon = (Polygon) multiPoly.getGeometryN(i);
				checkHolesInShell( polygon, new GeometryGraph(0, polygon), feature);
			}
		}else if(jtsGeo instanceof GeometryCollection){
			MultiPolygon multiPoly = JtsUtil.convertToMultiPolygon((GeometryCollection)jtsGeo);
			for(int i = 0; i < multiPoly.getNumGeometries(); i++){
				Polygon polygon = (Polygon) multiPoly.getGeometryN(i);
				checkHolesInShell( polygon, new GeometryGraph(0, polygon), feature);
			}
		}
	}

	private void checkHolesInShell(Polygon p, GeometryGraph graph, IFeature feature) {
		LinearRing shell = (LinearRing) p.getExteriorRing();
		Polygon shellAsPoly = null;
//		PointInRing pir = null;
//		if(snapTolerance == 0d)
//			pir = new MCPointInRing(shell);
//		else
//			pir = new SnapPointInRing(shell, snapTolerance);

		for (int i = 0; i < p.getNumInteriorRing(); i++) {
			LinearRing hole = (LinearRing) p.getInteriorRingN(i);
			
			//TODO Probably this wont work if we dont use 
			//libFMap's SnappingGeometryGraph
			List<Coordinate> holePts = findPtNotNode(hole.getCoordinates(), shell,
					graph);
			/*
			 * If no non-node hole vertex can be found, the hole must split the
			 * polygon into disconnected interiors. This will be caught by a
			 * subsequent check.
			 */
			if (holePts == null || holePts.size() == 0)
				return;

			/*
			List<Coordinate> outsideCoordinates = new ArrayList<Coordinate>();
			for(int j = 0; j < holePts.size(); j++){
				Coordinate coordinate = holePts.get(j);
				boolean outside = !pir.isInside(coordinate);
				if (outside) {
					outsideCoordinates.add(coordinate);
				}
			}//for
			
			int outsideCoordsSize = outsideCoordinates.size();
			if( outsideCoordsSize > 0){
				Coordinate start = outsideCoordinates.get(0);
				GeneralPathX gpx = new GeneralPathX();
				gpx.moveTo(start.x, start.y);
				for(int j = 1; j < outsideCoordsSize; j++){
					Coordinate coord = outsideCoordinates.get(j);
					gpx.lineTo(coord.x, coord.y);
				}
				Coordinate end = outsideCoordinates.get(outsideCoordsSize - 1);
				if(! SnapCGAlgorithms.snapEquals2D(start, end, snapTolerance)){
					gpx.closePath();
				}
				IGeometry igeo = ShapeFactory.createPolygon2D(gpx);
				IFeature[] features = {feature};
				TopologyError topologyError = new TopologyError(igeo, this, features);
				addTopologyError(topologyError);
			}//if
			*/
			
			//At this point hole is not contained in shell
			if(shellAsPoly == null){
				shellAsPoly = JtsUtil.GEOMETRY_FACTORY.createPolygon(shell, null);
			}
			
			Polygon holeAsPoly = JtsUtil.GEOMETRY_FACTORY.createPolygon(hole, null);
			Geometry errorGeometry = EnhancedPrecisionOp.difference(shellAsPoly, holeAsPoly);
			IGeometry errorGeo = NewFConverter.toFMap(errorGeometry);
			AbstractTopologyRule violatedRule = null;
			if(this.parentRule != null)
				violatedRule = parentRule;
			else
				violatedRule = this;
			JtsValidTopologyError topologyError = 
				new JtsValidTopologyError(errorGeo, violatedRule, feature, topology);
			topologyError.setSecondaryRule(this);
			addTopologyError(topologyError);
			
		}//for holes
	}

	
	//TODO Move to a util class (repeated code)
	List<Coordinate> findPtNotNode(Coordinate[] testCoords, 
								LinearRing searchRing,
								GeometryGraph graph) {
		
		List<Coordinate> solution = new ArrayList<Coordinate>();
		// find edge corresponding to searchRing.
		Edge searchEdge = graph.findEdge(searchRing);
		// find a point in the testCoords which is not a node of the searchRing
		EdgeIntersectionList eiList = searchEdge.getEdgeIntersectionList();
		// somewhat inefficient - is there a better way? (Use a node map, for
		// instance?)
		for (int i = 0; i < testCoords.length; i++) {
			Coordinate pt = testCoords[i];
			if (!eiList.isIntersection(pt))
				solution.add(pt);
		}
		if(solution.size() == 0)
			return null;
		else 
			return solution;
	}
	
	public XMLEntity getXMLEntity(){
		XMLEntity xml = super.getXMLEntity();
		xml.putProperty("snapTolerance", snapTolerance);
		return xml;
	}
	    
	public void setXMLEntity(XMLEntity xml){
		super.setXMLEntity(xml);
		
		if(xml.contains("snapTolerance")){
			snapTolerance = xml.getDoubleProperty("snapTolerance");
		}
	}

	public JtsValidRule getParentRule() {
		return parentRule;
	}

	public void setParentRule(JtsValidRule parentRule) {
		this.parentRule = parentRule;
	}
	
	public boolean acceptsOriginLyr(FLyrVect lyr) {
		try {
			return FGeometryUtil.getDimensions(lyr.getShapeType()) == 2;
		} catch (ReadDriverException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<ITopologyErrorFix> getAutomaticErrorFixes() {
		return automaticErrorFixes;
	}

	public MultiShapeSymbol getDefaultErrorSymbol() {
		return DEFAULT_ERROR_SYMBOL;
	}

	public MultiShapeSymbol getErrorSymbol() {
		return errorSymbol;
	}
}