/*
 * Created on 07-sep-2007
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
 *
 */
package org.gvsig.topology.topologyrules;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.jts.SnapLineStringSelfIntersectionChecker;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.IRuleWithClusterTolerance;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.errorfixes.ExtendDangleToNearestBoundaryPointFix;
import org.gvsig.topology.errorfixes.ExtendDangleToNearestVertexFix;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;


/**
 * All end points of a line must touch at least another line.
 *( if only touch one line, its a pseudonode, not a dangle)
 *  This rule is checked for the two ends of a line.
 *
 */
public class LineMustNotHaveDangles extends AbstractTopologyRule implements IRuleWithClusterTolerance {

	static final String RULE_NAME = Messages.getText("must_not_have_dangles");
	
	/**
	 * Symbol for topology errors caused by a violation of this rule.
	 */
	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
	
	
	private static List<ITopologyErrorFix> automaticErrorFixes =
		new ArrayList<ITopologyErrorFix>();
	static{
		automaticErrorFixes.add(new ExtendDangleToNearestBoundaryPointFix());
		automaticErrorFixes.add(new ExtendDangleToNearestVertexFix());
	}
	
	
	private static final Color DEFAULT_ERROR_COLOR = Color.RED;
	
	
	
	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = 
		(MultiShapeSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.MULTI, 
																DEFAULT_ERROR_COLOR);
	static{
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
		DEFAULT_ERROR_SYMBOL.setSize(4);
	}
	
	/**
	 * cluster tolerance 
	 */
	double clusterTol;
	/**
	 * Default constructor
	 *
	 */
	public LineMustNotHaveDangles(Topology topology, FLyrVect originLyr, double clusterTol){
		super(topology, originLyr);
		setClusterTolerance(clusterTol);
	}
	
	public LineMustNotHaveDangles(FLyrVect originLyr){
		super(originLyr);
	}
	
	public LineMustNotHaveDangles(){}
	
	public String getName() {
		return RULE_NAME;
	}

	
	public void checkPreconditions() throws TopologyRuleDefinitionException {
		//This rule doesnt apply to Point vectorial layers.
		try {
			int shapeType = this.originLyr.getShapeType();
			if(FGeometryUtil.getDimensions(shapeType) != 1 && shapeType != FShape.MULTI)
				throw new TopologyRuleDefinitionException("LineMustNotHaveDangles requires a lineal geometry type");
		} catch (ReadDriverException e) {
			e.printStackTrace();
			throw new TopologyRuleDefinitionException("Error leyendo el tipo de geometria del driver",e);
		}	
	}

	/**
	 * Validates this rule with a feature of the origin layer
	 * @param feature feature of the origin layer this rule is checking
	 */
	public void validateFeature(IFeature feature) {
		
		IGeometry geom = feature.getGeometry();
		int shapeType = geom.getGeometryType();
		if(shapeType != FShape.LINE && 
		   shapeType != FShape.ARC && 
		   shapeType != FShape.LINE + FShape.Z)
			return;
		
		Geometry jtsGeom = NewFConverter.toJtsGeometry(geom);
		
		process(jtsGeom, feature);
	} 
	
	/**
	 * Utility class to check if a given linestring is a dangle
	 * (it has unconected extremes)
	 * 
	 * @author Alvaro Zabala
	 *
	 */
	class DangleChecker{
		Point end1;
		boolean ends1 = false;
		
		Point end2;
		boolean ends2 = false;
	}
	
	
	private void process(Geometry geometry, IFeature feature){
		if(geometry instanceof GeometryCollection){
			GeometryCollection geomCol = (GeometryCollection) geometry;
			for(int i = 0; i < geomCol.getNumGeometries(); i++){
				Geometry geomI = geomCol.getGeometryN(i);
				process(geomI, feature);
			}
		}else if(geometry instanceof LineString){
			LineString lineString = (LineString) geometry;
			
			//first of all, we check for a closed linestring
			//first point equals to last point.
			//This would not be a dangle
			if(JtsUtil.isClosed(lineString, clusterTol)){
				return;
			}
			
			
			Point end1 = lineString.getPointN(0);
			Point end2 = lineString.getPointN(lineString.getNumPoints() - 1);
			
			DangleChecker checker = new DangleChecker();
			checker.end1 = end1;
			checker.end2 = end2;
			
			Rectangle2D rect = NewFConverter.
					convertEnvelopeToRectangle2D(lineString.getEnvelopeInternal());
			
			double xmin = rect.getMinX() - 5;
			double ymin = rect.getMinY() - 5;
			double w = rect.getWidth() + 5;
			double h = rect.getHeight() + 5;
			rect = new Rectangle2D.Double(xmin, ymin, w, h);
			
			try {
				IFeatureIterator neighbours = originLyr.
											 getSource().
											 getFeatureIterator(rect, null, null, false);
				while(neighbours.hasNext()){
					IFeature neighbourFeature = neighbours.next();
					if(neighbourFeature.getID().equalsIgnoreCase(feature.getID()))
						continue;
					Geometry geom2 = NewFConverter.toJtsGeometry(neighbourFeature.getGeometry());
					process2(checker, geom2);
				}
				
				SnapLineStringSelfIntersectionChecker selfIntChecker = 
					new SnapLineStringSelfIntersectionChecker(lineString, clusterTol);
				selfIntChecker.setLineString(lineString);
				
				if(! checker.ends1){//dangling node
					//we check if its is a selfintersection
					//(if true, its not a dangling node)
					boolean isSelfIntersection = false;
					if(selfIntChecker.hasSelfIntersections()){
						Coordinate[] selfs = selfIntChecker.getSelfIntersections();
						for(int i = 0; i < selfs.length; i++){
							if(SnapCGAlgorithms.snapEquals2D(checker.end1.getCoordinate(), selfs[i], clusterTol)){
								isSelfIntersection = true;
							}
						}//for
					}
					if(! isSelfIntersection){
						IGeometry errorGeom = 
							ShapeFactory.createPoint2D(checker.end1.getX(),
													   checker.end1.getY());
					    TopologyError topologyError = 
							new TopologyError(errorGeom, 
												this, 
											 feature,
											topology);
					    topologyError.setID(errorContainer.getErrorFid());
					    addTopologyError(topologyError);
					}	
				}
				
				if(! checker.ends2){//dangling node
					boolean isSelfIntersection = false;
					if(selfIntChecker.hasSelfIntersections()){
						Coordinate[] selfs = selfIntChecker.getSelfIntersections();
						for(int i = 0; i < selfs.length; i++){
							if(SnapCGAlgorithms.snapEquals2D(checker.end1.getCoordinate(), selfs[i], clusterTol)){
								isSelfIntersection = true;
							}
						}//for
					}
					if(! isSelfIntersection){
						IGeometry errorGeom = 
							ShapeFactory.createPoint2D(checker.end2.getX(),
													   checker.end2.getY());
					    TopologyError topologyError = 
							new TopologyError(errorGeom, 
												this, 
											 feature,
											topology);
					    topologyError.setID(errorContainer.getErrorFid());
					    addTopologyError(topologyError);
					}	
				}
				
				if(! checker.ends2 && ! checker.ends1){//dangling line
					GeneralPathX gp = new GeneralPathX();
					gp.moveTo(checker.end1.getX(), checker.end1.getY());
					gp.lineTo(checker.end2.getX(), checker.end2.getY());
					IGeometry errorGeom = ShapeFactory.createPolyline2D(gp);
					TopologyError topologyError = 
							new TopologyError(errorGeom, 
												this, 
											 feature,
											topology);
				    topologyError.setID(errorContainer.getErrorFid());
				    addTopologyError(topologyError);
				}
				
			} catch (ReadDriverException e) {
				e.printStackTrace();
				return;
			}
			
		}else{
			System.out.println("Encontrado:"+geometry.toString()+" en regla de dangles");
		}
	}
	
	
	private void process2(DangleChecker checker, Geometry intersectingGeom){
		if(intersectingGeom instanceof LineString){
			LineString lineString = (LineString) intersectingGeom;
			
			Envelope snapEnvelope = JtsUtil.
				toSnapRectangle(checker.end1.getCoordinate(), clusterTol);
			Geometry snapPolygon = null;
//			if(lineString.intersects(checker.end1)){
			if(lineString.getEnvelopeInternal().intersects(snapEnvelope)){
				snapPolygon = JtsUtil.GEOMETRY_FACTORY.toGeometry(snapEnvelope);
				if(lineString.intersects(snapPolygon))
					checker.ends1 = true;
			}
			
			snapEnvelope = JtsUtil.
				toSnapRectangle(checker.end2.getCoordinate(), clusterTol);
//			if(lineString.intersects(checker.end2)){
			if(lineString.getEnvelopeInternal().intersects(snapEnvelope)){
				snapPolygon = JtsUtil.GEOMETRY_FACTORY.toGeometry(snapEnvelope);
				if(lineString.intersects(snapPolygon))
					checker.ends2 = true;
			}
			
		}else if(intersectingGeom instanceof GeometryCollection){
			GeometryCollection geomCol = (GeometryCollection) intersectingGeom;
			for(int i = 0; i < geomCol.getNumGeometries(); i++){
				Geometry geomI = geomCol.getGeometryN(i);
				process2(checker, geomI);
			}
		}else{
			System.out.println("Encontrado:"+intersectingGeom.toString()+" en regla de dangles");
		}
	}

	public double getClusterTolerance() {
		return clusterTol;
	}

	public void setClusterTolerance(double clusterTolerance) {
		this.clusterTol = clusterTolerance;
		
	}

	public boolean acceptsOriginLyr(FLyrVect lyr) {
		try {
			int shapeType = lyr.getShapeType();
			return (FGeometryUtil.getDimensions(shapeType) == 1);
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
	
	public void setErrorSymbol(MultiShapeSymbol errorSymbol) {
		this.errorSymbol = errorSymbol;
	}
	
	public ITopologyErrorFix getDefaultFixFor(TopologyError topologyError){
		return automaticErrorFixes.get(1);
	}
}
 
