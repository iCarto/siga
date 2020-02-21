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
package org.gvsig.jts.voronoi;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.referencefork.referencing.operation.builder.TriangulationException;
import org.gvsig.exceptions.BaseException;
import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.Messages;

import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.utiles.swing.threads.CancellableProgressTask;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

/**
 * Computes voronois diagrams from a mess of points.
 * 
 * 
 * @author Alvaro Zabala
 * 
 */
public class Voronoier {
	
	public interface VoronoiStrategy {
		public List<IFeature> createThiessenPolygons(VoronoiAndTinInputLyr inputLyr, boolean onlySelection, CancellableProgressTask progressMonitor) throws BaseException;
		public List<IFeature> createThiessenPolygons(VoronoiAndTinInputLyr inputLyr, boolean onlySelection) throws BaseException;
		public List<TriangleFeature> createTin(VoronoiAndTinInputLyr inputLyr, boolean onlySelection) throws BaseException;
		public List<TriangleFeature> createTin(VoronoiAndTinInputLyr inputLyr, boolean onlySelection, CancellableProgressTask progressMonitor) throws BaseException;
		public String getName();
	}
	
	private static final Map<String, VoronoiStrategy> strategies =
		new HashMap<String, VoronoiStrategy>();
	static{
		ChewVoronoiStrategy chew = new ChewVoronoiStrategy();
		strategies.put(chew.getName(), chew);
//		IncrementalTinVoronoiStrategy triangleDt = new IncrementalTinVoronoiStrategy();
//		strategies.put(triangleDt.getName(), triangleDt );
		IncrementalRectBoundsVoronoiStrategy rectangleDt = new IncrementalRectBoundsVoronoiStrategy();
		strategies.put(rectangleDt.getName(), rectangleDt);
	}
	
	public static void registerVoronoiStrategy(VoronoiStrategy voronoi, String name){
		
		if(! strategies.containsValue(voronoi)){
			strategies.put(name, voronoi);
		}
		
	}
	
	
	public static Collection<VoronoiStrategy> getRegisteredAlgorithms(){
		return strategies.values();
	}
	public static List<IFeature> createThiessen(VoronoiAndTinInputLyr inputLyr, boolean onlySelection, String strategyStr, CancellableProgressTask progressMonitor) throws BaseException{
		VoronoiStrategy strategy = strategies.get(strategyStr);
		return strategy.createThiessenPolygons(inputLyr, onlySelection, progressMonitor);
	}
	
	public static List<IFeature> createThiessen(VoronoiAndTinInputLyr inputLyr, boolean onlySelection, String strategy) throws BaseException{
		return createThiessen(inputLyr, onlySelection, strategy, null);
	}
	
	public static List<TriangleFeature> createTIN(VoronoiAndTinInputLyr inputLyr, boolean onlySelection, String strategy) throws BaseException{
		return createTIN(inputLyr, onlySelection, strategy, null);
	}
	
	public static List<TriangleFeature> createTIN(VoronoiAndTinInputLyr inputLyr, boolean onlySelection, String strategyStr, CancellableProgressTask progressMonitor) throws BaseException{
		VoronoiStrategy strategy = strategies.get(strategyStr);
		return strategy.createTin(inputLyr, onlySelection, progressMonitor);
	}
	
		
	
	//TODO We could optimize this method if we work with a layer, instead a List<TriangleFeature>,
	//so we could use spatial indexes
	public static List<TriangleFeature> getAdjacentTriangles(TriangleFeature feature, List<TriangleFeature> tin){
		List<TriangleFeature> adjacentsTriangles = new ArrayList<TriangleFeature>();
		FTriangle triangle = (FTriangle) feature.getGeometry().getInternalShape();
		Iterator <TriangleFeature> i = tin.iterator();
        while (i.hasNext()) {
        	TriangleFeature candidate = i.next();
        	FTriangle otherTriangle = (FTriangle) candidate.getGeometry().getInternalShape();
            if(otherTriangle.equals(triangle))
            	continue;//the same triangle
        	try {
                if (otherTriangle.isAdjacent(triangle)
                        && ! adjacentsTriangles.contains(candidate)) {
                	adjacentsTriangles.add(candidate);
                }
            } catch (TriangulationException e) {
            	//this exception is throwed when find two equals triangles
            	//musnt reach here because we check this previously
                continue;              
            } 
        }//while

        return adjacentsTriangles;
	}
	
	
	/**
	 * Return all the voronoi edges associated to a TIN 
	 * 
	 * @return Arraylist with LineString geometries.
	 */
	private static List<LineString> getVoronoiEdges(List<TriangleFeature> tin) {
		
		ArrayList<LineString> lines = new ArrayList<LineString>();
		Iterator<TriangleFeature> it = tin.iterator();
		while(it.hasNext()){
			TriangleFeature triangle = it.next();
			FTriangle triangleGeo = (FTriangle) triangle.getGeometry().getInternalShape();
			Point2D circumcenter = triangleGeo.getCircumCenter();
			Coordinate jtsCircum = new Coordinate(circumcenter.getX(), circumcenter.getY());
			List<TriangleFeature> adjacentTriangles = Voronoier.getAdjacentTriangles(triangle, tin);
			Iterator<TriangleFeature> adjacentIterator = adjacentTriangles.iterator();
			while(adjacentIterator.hasNext()){
				TriangleFeature other = adjacentIterator.next();
				FTriangle otherGeo = (FTriangle) other.getGeometry().getInternalShape();
				Point2D otherCircum = otherGeo.getCircumCenter();
				Coordinate jtsOther = new Coordinate(otherCircum.getX(), otherCircum.getY());
				Coordinate[] coords = new Coordinate[]{jtsCircum, jtsOther};
				LineString ls = JtsUtil.GEOMETRY_FACTORY.createLineString(coords);
				lines.add(ls);
			}
		}
		return lines;
	}
	
	/**
	 * Method returns thiessen polygons within a empirically enlarged bounding
	 * box around the point set. Therefore the voronoi edges are polygonized and
	 * the intersecting voronoi polygons with the bounding box are calculated.
	 * These intersecting thiessen polygons (in the bounding box) are returned.
	 * 
	 * <p>
	 * Note: "thiesen" and "voronoi" is exchangeable.
	 * 
	 * @return
	 */
	public static List<Geometry> getThiessenPolys(Polygon bbox,
										   List<TriangleFeature> tin,
							 			   CancellableProgressTask progressMonitor) {
		
		List<LineString> edges = Voronoier.getVoronoiEdges(tin);
		Geometry lines = null;
		Geometry mls = (Geometry) edges.get(0);
		if (progressMonitor != null) {
			progressMonitor.setInitialStep(0);
			int numOfSteps = edges.size();
			progressMonitor.setFinalStep(numOfSteps);
			progressMonitor.setDeterminatedProcess(true);
			progressMonitor.setNote(Messages
					.getText("topology_clean_of_thiessen_edges"));
			progressMonitor.setStatusMessage(Messages
					.getText("voronoi_diagram_layer_message"));
			progressMonitor.reportStep();
		}

		for (int i = 1; i < edges.size(); i++) {
			Geometry line = (Geometry) edges.get(i);
			mls = mls.union(line);
			if (progressMonitor != null)
				progressMonitor.reportStep();
		}
		lines = mls;

		Polygonizer poly = new Polygonizer();
		poly.add(lines);
		Collection polys = poly.getPolygons();
		
//		// -- get final polygons in bounding box (=intersection polygons with
//		// the bbox)
//		Geometry bbox = Voronoier.getThiessenBoundingBox(i);
		
		if (progressMonitor != null) {
			progressMonitor.setInitialStep(0);
			int numOfSteps = polys.size();
			progressMonitor.setFinalStep(numOfSteps);
			progressMonitor.setDeterminatedProcess(true);
			progressMonitor.setNote(Messages
					.getText("computing_thiessen_polygons"));
			progressMonitor.setStatusMessage(Messages
					.getText("voronoi_diagram_layer_message"));
		}

		ArrayList<Geometry> finalPolys = new ArrayList<Geometry>();
		for (Iterator iter = polys.iterator(); iter.hasNext();) {
			Geometry candPoly = (Geometry) iter.next();
			Geometry intersection = bbox.intersection(candPoly);
			if (progressMonitor != null)
				progressMonitor.reportStep();
			if (intersection != null) {
				if (intersection.getArea() > 0) {
					finalPolys.add(intersection);
				}
			}
		}
		if (progressMonitor != null)
			progressMonitor.reportStep();
		return finalPolys;
	}
	
	/**
	 * the size of the box has been empirically defined to get "undistorted"
	 * outer thiessen polygons
	 * 
	 * @return a bounding box necesseray to create the final thiessenpolygons
	 * @throws BaseException 
	 */
	public static  Geometry getThiessenBoundingBox(VoronoiAndTinInputLyr inputLyr, boolean onlySelection) throws BaseException {
		MessExtentVertexVisitor visitor = new MessExtentVertexVisitor();
		inputLyr.visitVertices(visitor, onlySelection);
		double minX = visitor.getArgminx();
		double minY = visitor.getArgminy();
		double maxX = visitor.getArgmaxx();
		double maxY = visitor.getArgmaxy();
		
		double dx = maxX - minX;
		double dy = maxY - minY;
		
		double semiDy = 0.5 * dy;
		double extendedDy = 2.5 * dy;
		
		//FIXME Check that bounding box is not a point or a line
		
		Coordinate[] coords = new Coordinate[5];
		coords[0] = new Coordinate(minX - dx,
								   minY - semiDy); // lowerleft
		
		coords[1] = new Coordinate(minX + (3 * dx),
								   minY - semiDy); // lowerright
		
		coords[2] = new Coordinate(minX + (3 * dx),
								   minY + extendedDy); // topright
		
		coords[3] = new Coordinate(minX - dx,
								   minY + extendedDy); // topleft
		
		coords[4] =  new Coordinate(minX - dx,
				   minY - semiDy);
		
		LinearRing lr = JtsUtil.GEOMETRY_FACTORY.createLinearRing(coords);
		Geometry bbox = JtsUtil.GEOMETRY_FACTORY.createPolygon(lr, null);
		return bbox;
	}
}
