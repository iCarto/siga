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
 */
package org.gvsig.jts;

import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Cracks the geometries of a topology's layers to force the sharing of
 * coordinates.
 * 
 * Crack process consist in insert points in a segment when the distance of this
 * segment with a given coordinate is less than the snap tolerance.
 * 
 * So GeometryCracker doesnt compute intersections between segments 
 * and inserts new coordinates there.
 * 
 * GeometryCracker only insert new coordinates in segments when these coordinates
 * are shared geometry (they are vertices of other geometries) 
 * 
 */
public class GeometryCracker {

	private LineSegment seg = new LineSegment();

	private double snapTolerance;

	public GeometryCracker(double snapTolerance) {
		this.snapTolerance = snapTolerance;
	}

	public Geometry[] crackGeometries(Geometry[] geoms) {
		Geometry[] solution = new Geometry[geoms.length];

		for (int i = 0; i < geoms.length; i++) {
			Geometry geom = geoms[i];
			solution[i] = geom;
			for (int k = 0; k < geoms.length; k++) {
				Geometry geom2 = geoms[k];
				if (i == k)
					continue;
				solution[i] = crackGeometries(solution[i], geom2);
			}// for k
		}// for i
		return solution;
	}

	/**
	 * "Cracks" a geometry a with the points of a given geometry b.
	 * 
	 * @param a
	 * @param b
	 * @param snapTolerance
	 * @return
	 */
	public static Geometry crackGeometries(Geometry a, Geometry b, double snapTolerance) {
		GeometryCracker cracker = new GeometryCracker(snapTolerance);
		return cracker.crackGeometries(a, b);
	}
	
	
	public Geometry crackGeometries(Geometry a, Geometry b){
		Geometry solution = a;
		if(a.getGeometryType().equalsIgnoreCase("Polygon")){
			Polygon polygonA = (Polygon)a;
			if(b.getGeometryType().equalsIgnoreCase("Point") || 
			   b.getGeometryType().equalsIgnoreCase("LineString") || 
			   b.getGeometryType().equalsIgnoreCase("LinearRing")){
				solution = crackPolygonWithPointOrLine(polygonA, b);
			}else if(b.getGeometryType().equalsIgnoreCase("Polygon")){
				Polygon polygonB = (Polygon)b;
				solution = crackPolygonWithPolygon(polygonA, polygonB);
			}else if(b instanceof GeometryCollection){
				GeometryCollection collection = (GeometryCollection)b;
				for(int i = 0; i < collection.getNumGeometries(); i++){
					Geometry geometry = collection.getGeometryN(i);
					polygonA = (Polygon) crackGeometries(polygonA, geometry);
				}//for
				solution = polygonA;
			}//else
			
		}else if(a.getGeometryType().equalsIgnoreCase("LineString") || a.getGeometryType().equalsIgnoreCase("LinearRing")){
			LineString lineStringA = (LineString) a;
			if(b.getGeometryType().equalsIgnoreCase("LineString") || b.getGeometryType().equalsIgnoreCase("LinearRing") || b instanceof Point){
				solution = crackLineStringWithPointOrLine(lineStringA, b);
			}else if(b instanceof Polygon){
				Polygon polygonB = (Polygon) b;
				solution = crackLineStringWithPolygon(lineStringA, polygonB);
			}else if(b instanceof GeometryCollection){
				GeometryCollection collection = (GeometryCollection)b;
				for(int i = 0; i < collection.getNumGeometries(); i++){
					Geometry geometry = collection.getGeometryN(i);
					lineStringA = (LineString) crackGeometries(lineStringA, geometry);
				}//for
				solution = lineStringA;
			}//else
			
		}else if(a.getGeometryType().equalsIgnoreCase("Point")){
				solution = a;
		}else if(a instanceof GeometryCollection){
			GeometryCollection collection = (GeometryCollection)a;
			Geometry[] geomArray = new Geometry[collection.getNumGeometries()];
			for(int i = 0; i < collection.getNumGeometries(); i++){
				Geometry geometry = collection.getGeometryN(i);
				Geometry crackedGeometry = crackGeometries(geometry, b);
				geomArray[i] = crackedGeometry;
			}//for
			solution = JtsUtil.GEOMETRY_FACTORY.createGeometryCollection(geomArray);
		}//GeometryCollection
		return solution;
	}
	
	/**
	 * Cracks the passed polygon with the point parameter
	 * @param point
	 * @param polygon
	 * @return
	 */
	private Geometry crackPolygonWithPointOrLine(Polygon polygon, Geometry geometry){
		if(! (geometry instanceof LineString) && ! (geometry instanceof Point))
			throw new IllegalArgumentException("Este metodo solo funciona con puntos y lineas y se recibio "+geometry.getGeometryType());
		LinearRing shell = (LinearRing) polygon.getExteriorRing();
		LinearRing crackedShell = (LinearRing) JtsUtil.createGeometry(crackTo(shell.getCoordinates(), geometry.getCoordinates()), "LINEARRING");
		int numberOfHoles = polygon.getNumInteriorRing();
		LinearRing[] crackedHoles = new LinearRing[numberOfHoles];
		for(int i = 0; i < numberOfHoles; i++){
			LinearRing hole = (LinearRing) polygon.getInteriorRingN(i);
			LinearRing crackedHole = (LinearRing) JtsUtil.createGeometry(crackTo(hole.getCoordinates(), geometry.getCoordinates()), "LINEARRING");
			crackedHoles[i] = crackedHole;
		}
		return JtsUtil.GEOMETRY_FACTORY.createPolygon(crackedShell, crackedHoles);
	}
	
	private Geometry crackLineStringWithPointOrLine(LineString lineA, Geometry geometry){
		if(! (geometry instanceof LineString) && ! (geometry instanceof Point))
			throw new IllegalArgumentException("Este metodo solo funciona con puntos y lineas y se recibio "+geometry.getGeometryType());
		Coordinate[] newCoordsA = crackTo(lineA.getCoordinates(), geometry.getCoordinates());
		//we call to lineA.getGeometryType because it could be a LineString or a LinearRing
		return JtsUtil.createGeometry(newCoordsA,  lineA.getGeometryType());
	}
	
	
	
	private Geometry crackLineStringWithPolygon(LineString line, Polygon polygon){
		LinearRing shell = (LinearRing) polygon.getExteriorRing();
		Coordinate[] lineStringCracked = crackTo(line.getCoordinates(), shell.getCoordinates());
		int numberOfHoles = polygon.getNumInteriorRing();
		for(int i = 0; i < numberOfHoles; i++){
			LinearRing hole = (LinearRing) polygon.getInteriorRingN(i);
			lineStringCracked = crackTo(lineStringCracked, hole.getCoordinates());
		}
		return JtsUtil.createGeometry(lineStringCracked, line.getGeometryType());
	}
	
	
	private Geometry crackPolygonWithPolygon(Polygon polyA, Polygon polyB){
		LinearRing shell = (LinearRing) polyB.getExteriorRing();
		Polygon crackedPolygon = (Polygon) crackPolygonWithPointOrLine(polyA, shell);
		int numberOfHoles = polyB.getNumInteriorRing();
		for(int i = 0; i < numberOfHoles; i++){
			LinearRing hole = (LinearRing) polyB.getInteriorRingN(i);
			crackedPolygon = (Polygon) crackPolygonWithPointOrLine(crackedPolygon, hole);
		}
		return crackedPolygon;
	}
	
	
	
	
	/*
	 * This code is extracted from the class LineStringSnapper of JTS
	 */
	private Coordinate[] crackTo(Coordinate[] srcPts, Coordinate[] snapPts) {
		CoordinateList coordList = new CoordinateList(srcPts);
		crackSegments(coordList, snapPts);
		Coordinate[] newPts = coordList.toCoordinateArray();
		return newPts;
	}

	private void crackSegments(CoordinateList srcCoords, Coordinate[] snapPts) {
		int distinctPtCount = snapPts.length;
		Coordinate firstPoint = snapPts[0];
		Coordinate lastPoint = snapPts[snapPts.length - 1];
		if (SnapCGAlgorithms.snapEquals2D(firstPoint, lastPoint, snapTolerance)){
			if(distinctPtCount > 1)//necessary because with snapPts.length the algorithm doesnt work
				distinctPtCount = snapPts.length - 1;
		}
		for (int i = 0; i < distinctPtCount; i++) {
			Coordinate snapPt = snapPts[i];
			int index = findSegmentIndexToSnap(snapPt, srcCoords);
			if (index >= 0) {
				seg.p0 = srcCoords.getCoordinate(index);
				seg.p1 = srcCoords.getCoordinate(index +1);
				Coordinate newCoordinate = seg.closestPoint(snapPt);
				srcCoords.add(index + 1, newCoordinate, false);
			}// if
		}// for i
	}

	private int findSegmentIndexToSnap(Coordinate snapPt,
			CoordinateList srcCoords) {
		double minDist = Double.MAX_VALUE;
		int snapIndex = -1;
		for (int i = 0; i < srcCoords.size() - 1; i++) {
			seg.p0 = (Coordinate) srcCoords.get(i);
			seg.p1 = (Coordinate) srcCoords.get(i + 1);

			if (SnapCGAlgorithms.snapEquals2D(seg.p0, snapPt, snapTolerance)
					|| SnapCGAlgorithms.snapEquals2D(seg.p1, snapPt,
							snapTolerance))
				return -1;
			double dist = seg.distance(snapPt);
			if (dist <= snapTolerance && dist < minDist) {
				minDist = dist;
				snapIndex = i;
			}
		}//for
		return snapIndex;
	}

}
