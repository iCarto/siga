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
package org.gvsig.topology.topologyrules;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.MultipartShapeIterator;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.fmap.core.ShapePointExtractor;
import org.gvsig.jts.JtsUtil;
import org.gvsig.jts.SnapLineStringSelfIntersectionChecker;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.IRuleWithClusterTolerance;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.ITopologyRuleWithExclusiveFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.errorfixes.CompleteUndershootFix;
import org.gvsig.topology.errorfixes.RemoveOvershootFix;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.FGeometryCollection;
import com.iver.cit.gvsig.fmap.core.FMultiPoint2D;
import com.iver.cit.gvsig.fmap.core.FNullGeometry;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPoint3D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * This rule checks that a fmap geometry is closed.
 * 
 * @author Alvaro Zabala
 * 
 */
public class FMapGeometryMustBeClosed extends AbstractTopologyRule 
	implements IRuleWithClusterTolerance, ITopologyRuleWithExclusiveFix {

	private static String RULE_NAME = Messages.getText("polygon_must_be_closed");

	private double clusterTolerance;
	
	/**
	 * Symbol for topology errors caused by a violation of this rule.
	 */
	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
	
	private static List<ITopologyErrorFix> automaticErrorFixes =
		new ArrayList<ITopologyErrorFix>();
	static{
		automaticErrorFixes.add(new RemoveOvershootFix());
		automaticErrorFixes.add(new CompleteUndershootFix());
		
	}
	
	private static final Color DEFAULT_ERROR_COLOR = Color.RED;
	
	//this symbol is multishape because this topology rule applies to lines or polygons
	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = 
		(MultiShapeSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.MULTI, 
											DEFAULT_ERROR_COLOR);
	static{
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
		DEFAULT_ERROR_SYMBOL.setSize(0.5);
		DEFAULT_ERROR_SYMBOL.setLineWidth(0.5);
		DEFAULT_ERROR_SYMBOL.getOutline().setLineColor(DEFAULT_ERROR_COLOR);
		DEFAULT_ERROR_SYMBOL.setFillColor(DEFAULT_ERROR_COLOR);
	}

	/**
	 * Constructor.
	 * 
	 * @param topology
	 * @param originLyr
	 * @param snapTolerance
	 */
	public FMapGeometryMustBeClosed(Topology topology, FLyrVect originLyr,
			double snapTolerance) {
		super(topology, originLyr);
		setClusterTolerance(snapTolerance);
	}
	
	public FMapGeometryMustBeClosed(){}

	public void checkPreconditions() throws TopologyRuleDefinitionException {
		try {
			if (FGeometryUtil.getDimensions(originLyr.getShapeType()) < 1)
				throw new TopologyRuleDefinitionException(
						"La capa no es de lineas o poligonos");
		} catch (ReadDriverException e) {
			throw new TopologyRuleDefinitionException(
					"Error de driver al chequear las precondiciones de la regla");
		}
	}

	public String getName() {
		return RULE_NAME;
	}

	public void validateFeature(IFeature feature) {
		IGeometry geometry = feature.getGeometry();
		process(geometry, feature);

	}

	private void process(IGeometry geometry, IFeature feature) {
		if (geometry instanceof FMultiPoint2D
				|| geometry instanceof FNullGeometry)
			return;
		else if (geometry instanceof FGeometry) {
			FGeometry geom = (FGeometry) geometry;
			FShape shp = (FShape) geom.getInternalShape();
			if (shp instanceof FPoint2D || shp instanceof FPoint3D) {
				return;
			} else {
				List<IGeometry> geometries = new ArrayList<IGeometry>();
				MultipartShapeIterator it = new MultipartShapeIterator(shp);
				Iterator<Shape> shapeIt = it.getShapeIterator();
				while (shapeIt.hasNext()) {
					Shape shape = shapeIt.next();
					List<Point2D[]> partsCoords = ShapePointExtractor
							.extractPoints(shape);
					for (int i = 0; i < partsCoords.size(); i++) {
						Point2D[] part = partsCoords.get(i);
						if (!FGeometryUtil.isClosed(part, clusterTolerance)) {
							//createGeometryNotClosedError(part, feature);
							IGeometry partError = getGeometryNotClosedError(part);
							if(partError != null)
								geometries.add(getGeometryNotClosedError(part));
						}// if isClosed
					}// for i
				}// while
				
				IGeometry[] geoms = new IGeometry[geometries.size()];
				geometries.toArray(geoms);
				FGeometryCollection errorGeom = new FGeometryCollection(geoms);
				TopologyError topologyError = new TopologyError(errorGeom, this,
						feature, topology);
				topologyError.setID(errorContainer.getErrorFid());
				addTopologyError(topologyError);
				
			}
		} else if (geometry instanceof FGeometryCollection) {
			FGeometryCollection geomCol = (FGeometryCollection) geometry;
			IGeometry[] geometries = geomCol.getGeometries();
			for (int i = 0; i < geometries.length; i++) {
				process(geometries[i], feature);
			}// for
		} else {
			System.out.println("Encontrado " + geometry.getClass().toString());
		}
	}
	
	
	
	 
	private IGeometry getGeometryNotClosedError(Point2D[] unclosedPart){
		
		List<IGeometry> geometries = new ArrayList<IGeometry>();
		// we are going to see if shape is an overshoot or an
		// undershoot
		Coordinate[] jtsCoords = JtsUtil.getPoint2DAsCoordinates(unclosedPart);
		LineString jtsGeo = (LineString) JtsUtil.createGeometry(jtsCoords, "LINESTRING");		
		SnapLineStringSelfIntersectionChecker checker = 
			new SnapLineStringSelfIntersectionChecker(jtsGeo, this.clusterTolerance);
		if(checker.hasSelfIntersections()){
			//Over shoot
			Geometry[] geoms = checker.clean();
			for(int i = 0; i < geoms.length; i++){
				LineString line = (LineString) geoms[i];
				if(!line.isClosed()){
					//this rule doesnt checks if a line has self intersections.
					//thats the reason for we only consideer errors at splitted linestrings
					//which are unclosed.
					IGeometry errorGeom = NewFConverter.jts_to_igeometry(line);
					geometries.add(errorGeom);
				}//if
			}//for

		}else{
			//Under Shoot
			GeneralPathX gp = new GeneralPathX();
			Point2D start = unclosedPart[0];
			Point2D end = unclosedPart[unclosedPart.length - 1];
			gp.moveTo(start.getX(), start.getY());
			gp.lineTo(end.getX(), end.getY());
			IGeometry errorGeom = ShapeFactory.createPolyline2D(gp);
			geometries.add(errorGeom);
		}
		int size = geometries.size();
		if(size == 0)
			return null;
		else if(size == 1)
			return geometries.get(0);
		else{
			IGeometry[] geoms = new IGeometry[geometries.size()];
			geometries.toArray(geoms);
			IGeometry solution = new FGeometryCollection(geoms);
			return solution;
		}
		
	}

	private void createGeometryNotClosedError(Point2D[] unclosedPart, IFeature feature) {

		// we are going to see if shape is an overshoot or an
		// undershoot
		Coordinate[] jtsCoords = JtsUtil.getPoint2DAsCoordinates(unclosedPart);
		LineString jtsGeo = (LineString) JtsUtil.createGeometry(jtsCoords, "LINESTRING");		
		SnapLineStringSelfIntersectionChecker checker = 
			new SnapLineStringSelfIntersectionChecker(jtsGeo, this.clusterTolerance);
		if(checker.hasSelfIntersections()){
			//Over shoot
			Geometry[] geoms = checker.clean();
			for(int i = 0; i < geoms.length; i++){
				LineString line = (LineString) geoms[i];
				if(!line.isClosed()){
					//this rule doesnt checks if a line has self intersections.
					//thats the reason for we only consideer errors at splitted linestrings
					//which are unclosed.
					IGeometry errorGeom = NewFConverter.jts_to_igeometry(line);
					TopologyError topologyError = new TopologyError(errorGeom, this,
							feature, topology);
					topologyError.setID(errorContainer.getErrorFid());
					addTopologyError(topologyError);
				}//if
			}//for

		}else{
			//Under Shoot
			GeneralPathX gp = new GeneralPathX();
			Point2D start = unclosedPart[0];
			Point2D end = unclosedPart[unclosedPart.length - 1];
			
			gp.moveTo(start.getX(), start.getY());
			gp.lineTo(end.getX(), end.getY());

			IGeometry errorGeometry = ShapeFactory.createPolyline2D(gp);
			TopologyError topologyError = new TopologyError(errorGeometry, this,
					feature, topology);
			topologyError.setID(errorContainer.getErrorFid());
			addTopologyError(topologyError);
		}
	}

	public double getClusterTolerance() {
		return clusterTolerance;
	}

	public void setClusterTolerance(double clusterTolerance) {
		this.clusterTolerance = clusterTolerance;
	}

	public boolean acceptsOriginLyr(FLyrVect lyr) {
		try {
			return (FGeometryUtil.getDimensions(lyr.getShapeType()) >= 1);
		} catch (ReadDriverException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<ITopologyErrorFix> getAutomaticErrorFixes() {
		return FMapGeometryMustBeClosed.automaticErrorFixes;
	}
	
	

	public MultiShapeSymbol getDefaultErrorSymbol() {
		return DEFAULT_ERROR_SYMBOL;
	}
	
	@Override
	public ITopologyErrorFix getDefaultFixFor(TopologyError topologyError){
		if(topologyError != null)
			return getExclusiveErrorFixes(topologyError).get(0);
		else
			return getAutomaticErrorFixes().get(0);
	}

	public MultiShapeSymbol getErrorSymbol() {
		return errorSymbol;
	}

	public void setErrorSymbol(MultiShapeSymbol errorSymbol) {
		this.errorSymbol = errorSymbol;
	}

	public List<ITopologyErrorFix> getExclusiveErrorFixes(TopologyError error) {
		List<ITopologyErrorFix> solution = new ArrayList<ITopologyErrorFix>();
		IGeometry geom = error.getFeature1().getGeometry();
		if(geom instanceof FGeometry){
			FShape shp = (FShape) geom.getInternalShape();
			MultipartShapeIterator it = new MultipartShapeIterator(shp);
			Iterator<Shape> shapeIt = it.getShapeIterator();
			while (shapeIt.hasNext()) {
				Shape shape = shapeIt.next();
				List<Point2D[]> partsCoords = ShapePointExtractor
						.extractPoints(shape);
				for (int i = 0; i < partsCoords.size(); i++) {
					Point2D[] part = partsCoords.get(i);
					if (!FGeometryUtil.isClosed(part, clusterTolerance)) {
						Coordinate[] jtsCoords = JtsUtil.
							getPoint2DAsCoordinates(part);
						LineString jtsGeo = (LineString) JtsUtil.createGeometry(jtsCoords, "LINESTRING");						
						SnapLineStringSelfIntersectionChecker checker = 
							new SnapLineStringSelfIntersectionChecker(jtsGeo, this.clusterTolerance);
						if(checker.hasSelfIntersections()){
							//Over shoot
							solution.add(new RemoveOvershootFix());
						}else{
							//Under Shoot
							solution.add(new CompleteUndershootFix());
						}
					}// if isClosed
				}// for i
			}// while
		} 
		return solution;
//		else if (geom instanceof FGeometryCollection) {
//			FGeometryCollection geomCol = (FGeometryCollection) geom;
//			IGeometry[] geometries = geomCol.getGeometries();
//			for (int i = 0; i < geometries.length; i++) {
//				process(geometries[i], feature);
//			}// for
//		}
	}

}
