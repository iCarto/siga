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
package org.gvsig.topology.errorfixes;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.core.FLyrUtil;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.Messages;
import org.gvsig.topology.TopologyError;
import org.gvsig.util.GNumberParameter;
import org.gvsig.util.GParameter;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.drivers.featureiterators.FeatureListIntIterator;
import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.spatialindex.INearestNeighbourFinder;
import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import com.vividsolutions.jts.geom.util.GeometryEditor.CoordinateOperation;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class ExtendDangleToNearestBoundaryPointFix extends
		ExtendDangleToNearestVertexFix {
	
	public double searchRadius;

	public ExtendDangleToNearestBoundaryPointFix(double searchRadius) {
		super(searchRadius);
		this.searchRadius = searchRadius;
	}

	public ExtendDangleToNearestBoundaryPointFix() {
		super();
	}

	protected Coordinate getClosestPoint(Geometry original, Geometry nearestGeometry, DistanceOp distanceOp){
		Geometry boundary = nearestGeometry.getBoundary();
		
		return DistanceOp.closestPoints(original, boundary)[1];
	}
	
	public String getEditionDescription() {
		return Messages.getText("EXTEND_DANGLE_TO_BOUNDARY_FIX");
	}
	
	
	public List<IFeature>[] fixAlgorithm(TopologyError error) throws BaseException {
		FLyrVect originLyr = error.getOriginLayer();
		double clusterTolerance = error.getTopology().getClusterTolerance();
		FGeometry errorGeometry = (FGeometry) error.getGeometry();
		FPoint2D dangle = (FPoint2D) errorGeometry.getInternalShape();
		Coordinate jtsCoord = new Coordinate(dangle.getX(), dangle.getY());
		Point jtsDangle = JtsUtil.GEOMETRY_FACTORY.createPoint(jtsCoord);
		
		IFeature originFeature = error.getFeature1();
		
		IFeature nearestFeature = getNearestFeature(jtsDangle, originLyr, originFeature);
		
		if(nearestFeature != null){
			Geometry nearestJts = NewFConverter.toJtsGeometry(nearestFeature.getGeometry());
			Geometry originalJts = NewFConverter.toJtsGeometry(originFeature.getGeometry());
			
			Coordinate closestPoint = getClosestPoint(jtsDangle, nearestJts);
			double distance = DistanceOp.distance(jtsDangle, nearestJts);
			
			if(distance <= searchRadius){
				Geometry editedGeometry = extendGeometry(originalJts, jtsDangle.getCoordinate(), closestPoint, clusterTolerance);
				if(editedGeometry != null){
					IGeometry newGeom = NewFConverter.toFMap(editedGeometry);
					
					originFeature.setGeometry(newGeom);
					List<IFeature> editedFeatures = new ArrayList<IFeature>();
					editedFeatures.add(originFeature);
					return (List<IFeature>[]) new List[]{editedFeatures};
				}//if editedGeometry
			}//if distance	
			else{
				BaseException exception = new BaseException(){
					public String getFormatString(){
						return Messages.getText("Geometry_not_found_in_radius_search");
					}
					@Override
					protected Map values() {
						// TODO Auto-generated method stub
						return null;
					}};
				throw exception;
			}
		}//if nearest feature
		return null;
	}
	
	
	
	
	private Geometry extendGeometry(Geometry g, Coordinate dangle, final Coordinate closestPoint, double clusterTolerance){
		
		List<LineString> geometries2process = new ArrayList<LineString>();
		if (g instanceof LineString)
			geometries2process.add((LineString) g);
		else if (g instanceof GeometryCollection) {
			GeometryCollection geomCol = (GeometryCollection) g;
			List lines = LinearComponentExtracter.getLines(geomCol);
			geometries2process.addAll(lines);
		}
		
		for(int i = 0; i < geometries2process.size(); i++){
			LineString lineString = geometries2process.get(i);
			if(JtsUtil.isEndPoint(lineString, dangle, clusterTolerance)){
				int numPoints = lineString.getNumPoints();
				Coordinate start = lineString.getCoordinateN(0);
				Coordinate end = lineString.getCoordinateN(numPoints - 1);
				Geometry editedGeometry = null;
				if(SnapCGAlgorithms.snapEquals2D(dangle, start, clusterTolerance)){
					editedGeometry = JtsUtil.GEOMETRY_EDITOR.edit(lineString, new CoordinateOperation(){
						public Coordinate[] edit(Coordinate[] coordinates, Geometry geometry) {
							Coordinate[] newCoordinates = new Coordinate[coordinates.length + 1];
							System.arraycopy(coordinates, 0, newCoordinates, 1, coordinates.length);
							newCoordinates[0] = closestPoint;
							return newCoordinates;
						}});
					
				}else{
					editedGeometry = JtsUtil.GEOMETRY_EDITOR.edit(lineString, new CoordinateOperation(){
						public Coordinate[] edit(Coordinate[] coordinates, Geometry geometry) {
							Coordinate[] newCoordinates = new Coordinate[coordinates.length + 1];
							System.arraycopy(coordinates, 0, newCoordinates, 0, coordinates.length);
							newCoordinates[coordinates.length] = closestPoint;
							return newCoordinates;
						}});
				}
				return editedGeometry;
			}//if isEndPoint
		}//for
		//at this point we havent found a linestring end point for the dangle error
		//TODO launch inconsistent exception?
		return null;
	}

	
	
	private IFeature getNearestFeature(Point jtsDangle, FLyrVect originLyr, IFeature originFeature) throws ReadDriverException{
//		IGeometry originalGeometry = originFeature.getGeometry();
//		Geometry originalJts = NewFConverter.toJtsGeometry(originalGeometry);
		
		INearestNeighbourFinder nnFinder =
			FLyrUtil.getNearestNeighbourFinder(originLyr);
		
		int numberOfNearest = 2;
		Rectangle2D dangleRect = new Rectangle2D.Double(jtsDangle.getX(), jtsDangle.getY(), 1, 1);
//		List nearestIndexes = nnFinder.
//			findNNearest(numberOfNearest, new Point2D.Double(dangle.getX(), dangle.getY()));
		List nearestIndexes = nnFinder.findNNearest(numberOfNearest, dangleRect);
		
		//we look for the nearest neighbour to the dangle, excepting the originFeature
		//if the distance to the point is lesser than the search radius, we snap it
		double nearestDistance = Double.MAX_VALUE;
		
		FeatureListIntIterator it = new FeatureListIntIterator(nearestIndexes, originLyr.getSource() );
		IFeature nearestFeature = null;
		while(it.hasNext()){
			IFeature feature = it.next();
			if(feature.getID().equalsIgnoreCase(originFeature.getID()))
				continue;
			IGeometry igeometry = feature.getGeometry();
			Geometry jtsGeo = igeometry.toJTSGeometry();
			double dist = jtsGeo.distance(jtsDangle);
//			double dist = originalJts.distance(jtsGeo);
			if (dist <= nearestDistance) {
				// by adding <=, we follow the convention that
				// if two features are at the same distance, take
				// the last as nearest neighbour
				nearestDistance = dist;
				nearestFeature = feature;
			}// if
		}//while
		return nearestFeature;
	}
	

	public double getSearchRadius() {
		return searchRadius;
	}

	public void setSearchRadius(double searchRadius) {
		this.searchRadius = searchRadius;
	}

	
	public GParameter[] getParameters() {
		GParameter[] parameters = new GParameter[1];
		parameters[0] = 
			new GNumberParameter("searchRadius", new Double(0d), this, false);
		return parameters;
	}

	public void setParameterValue(String paramName, Object value) {
		if(paramName.equals("searchRadius")){
			if(value.getClass().isAssignableFrom(Number.class)){
				this.searchRadius = ((Number)value).doubleValue();
			}
		}	
	}

}
