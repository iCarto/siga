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
 * Improvements to JTS (robustness problems)
 *
 *
 */
package org.gvsig.jts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

/**
 * 
 * From a given linestring and a set of "cut" points, split the linestring
 * resulting a new linestring between two consecutives (from the start) points.
 * 
 * Also remove self-intersections (when the linestring has cicles)
 * 
 * 
 * 
 * @author azabala
 *
 */
public class LineStringSplitter {
	
	
	/*
	 * This inner class is repeated in LineCleanVisitor of
	 * GeoprocessingExtensions and here.
	 * 
	 * TODO Remove one (for that, move this class to libFMap)
	 * 
	 */

	static class LineIntersection {
		Coordinate coordinate;

		double lenght;
	}

	private static final double DEFAULT_SNAP_TOLERANCE = 0d;

	
	
	private static LineString[] splitClosedLineString(LineString lineString,
			Coordinate[] splitPoints){
		return splitClosedLineString(lineString, splitPoints,DEFAULT_SNAP_TOLERANCE);
	}
	
	
	
	private static LineString[] splitClosedLineString(LineString lineString,
			Coordinate[] splitPoints, double snapTolerance) {

		Polygonizer polygonizer = new Polygonizer();
		LineString[] lineStrings = splitSimple(lineString, splitPoints); 
		
		ArrayList<LineString> closedLineStrings = new ArrayList<LineString>();
		ArrayList<LineString> unclosedLineStrings = new ArrayList<LineString>();
		
		for(int i = 0; i < lineStrings.length; i++){
			LineString geom = lineStrings[i];
			if(JtsUtil.isClosed(geom, snapTolerance)){
				closedLineStrings.add(geom);
			}else{
				unclosedLineStrings.add(geom);
			}
		}
		
		//JTS Polygonizer requires a exact coincident in coordinates.
		//We try to apply a previous snap
		//MUST WE APPLY A COORDINATE CRACKING??
		
		LineString[] unclosedLS = new LineString[unclosedLineStrings.size()];
		unclosedLineStrings.toArray(unclosedLS);
		GeometrySnapper snapper = new GeometrySnapper(snapTolerance);
		Geometry[] snappedLS = snapper.snap(unclosedLS);
		List<Geometry> snappedList = Arrays.asList(snappedLS);
		
		
		polygonizer.add(snappedList);
		Collection polygons = polygonizer.getPolygons();
		
		Iterator polyIt = polygons.iterator();
		ArrayList outerRingsList = new ArrayList();
		while (polyIt.hasNext()) {
			outerRingsList.add(((Polygon) polyIt.next()).getExteriorRing());
		}
		
		Iterator closedLineStringIt =closedLineStrings.iterator();
		while(closedLineStringIt.hasNext()){
			outerRingsList.add(closedLineStringIt.next());
		}
		
		LineString[] solution = new LineString[outerRingsList.size()];
		outerRingsList.toArray(solution);
		return solution;
	}
	
	
	//FIXME Introduce snap tolerance concept
	
	private static LineString[] splitUnclosedLineString(LineString lineString,
			Coordinate[] splitPoints, double snapTolerance) {
		
		ArrayList lineStringList = new ArrayList();
//		RobustLengthIndexedLine lengthLine = new RobustLengthIndexedLine(lineString);
		LengthIndexedLine lengthLine = new LengthIndexedLine(lineString);
		ArrayList<LineIntersection> nodeIntersections = new ArrayList<LineIntersection>();
		double linearReferencingIndex = 0d;
		for (int i = 0; i < splitPoints.length; i++) {
			Coordinate coord = splitPoints[i];
			double lengthOfNode = lengthLine.indexOfAfter(coord,
					linearReferencingIndex);
			linearReferencingIndex = lengthOfNode;

			LineIntersection inters = new LineIntersection();
			inters.coordinate = coord;
			inters.lenght = lengthOfNode;
			nodeIntersections.add(inters);
		}// for

		// We sort the intersections by distance along the line
		// (dynamic
		// segmentation)
		Collections.sort(nodeIntersections, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				LineIntersection l1 = (LineIntersection) arg0;
				LineIntersection l2 = (LineIntersection) arg1;
				if (l1.lenght > l2.lenght)
					return 1;
				else if (l1.lenght < l2.lenght)
					return -1;
				else
					return 0;
			}
		});

		LinearLocation lastLocation = null;
		LineIntersection lastIntersection = null;
		RobustLocationIndexedLine indexedLine = new RobustLocationIndexedLine(lineString);
		for (int i = 0; i < nodeIntersections.size(); i++) {
			Geometry geometry = null;
			LineIntersection li = (LineIntersection) nodeIntersections.get(i);
			// LinearLocation location = indexedLine.indexOf(li.coordinate);
			LinearLocation location = indexedLine.indexOfAfter(indexedLine,
					li.coordinate, lastLocation);

			if (lastLocation == null) {
				LinearLocation from = new LinearLocation(0, 0d);
				/*
				 * we build a linestring with all points between the first point
				 * until the first selfintersection
				 */
				geometry = indexedLine.extractLine(from, location);
				lastLocation = location;
				lastIntersection = li;
			} else {
				/*
				 * Its not the first selfintersection. We build a linestring
				 * with all points between two self intersections
				 */
				LinearLocation locationFrom = lastLocation;
				geometry = indexedLine.extractLine(locationFrom, location);
				lastLocation = location;
				lastIntersection = li;

			}
			//If a linestring self intersects with its first point, the first location
			//is equal to the intersection and must ignore this fragment
			if(geometry.getLength() > snapTolerance)
				lineStringList.add(geometry);
		}// for
		LinearLocation endLocation = new LinearLocation();
		endLocation.setToEnd(lineString);
		
		//when the intersection point is equal to the last point we avoid this
		Geometry lastLine = indexedLine.extractLine(lastLocation, endLocation);
		if(lastLine.getLength() > snapTolerance)
			lineStringList.add(lastLine);

		LineString[] solution = new LineString[lineStringList.size()];
		lineStringList.toArray(solution);
		return solution;
	}

	
//	FIXME Introduce snap tolerance concept
	
	/**
	 * Splits the specified lineString with the specified points.
	 * (One point will give two lines, two points three lines, etc
	 * 
	 * @param lineString
	 * @param points
	 * */
	
	public static LineString[] splitSimple(LineString lineString, Coordinate[] points) {
		LineString[] splittedLS = null;
		RobustLengthIndexedLine lengthLine = new RobustLengthIndexedLine(lineString);
		/*
		 * LenghtIndexedLine indexOfAlter method returns the index of a line greater than the specified index.
		 * For this reason, we must save in a cache the last computed index for a given coord, and pass
		 * this precomputed index to the indexOfAlter method
		 * */
		HashMap coordsIndex = new HashMap();
		ArrayList nodeIntersections = new ArrayList();
		for (int i = 0; i < points.length; i++) {
			Coordinate coord = points[i];
			Double index = (Double) coordsIndex.get(coord);
			double indexD = 0d;
			if(index != null)
				indexD = index.doubleValue();
			double computedIndex = lengthLine.indexOfAfter(coord, indexD);
			coordsIndex.put(coord, new Double(computedIndex));
					LineIntersection inters = new LineIntersection();
			inters.coordinate = coord;
			inters.lenght = computedIndex;
			nodeIntersections.add(inters);
		}// for

		// We sort the intersections by distance along the line
		// (dynamic
		// segmentation)
		Collections.sort(nodeIntersections, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				LineIntersection l1 = (LineIntersection) arg0;
				LineIntersection l2 = (LineIntersection) arg1;
				if (l1.lenght > l2.lenght)
					return 1;
				else if (l1.lenght < l2.lenght)
					return -1;
				else
					return 0;
			}
		});

		LinearLocation lastLocation = null;
		LineIntersection lastIntersection = null;
		RobustLocationIndexedLine indexedLine = new RobustLocationIndexedLine(lineString);
		ArrayList splittedLsList = new ArrayList();
		for (int i = 0; i < nodeIntersections.size(); i++) {
			Geometry solution = null;
			LineIntersection li = (LineIntersection) nodeIntersections.get(i);
			LinearLocation location = indexedLine.indexOfAfter(indexedLine,
					li.coordinate, lastLocation);
			if (lastLocation == null) {
				LinearLocation from = new LinearLocation(0, 0d);
				solution = indexedLine.extractLine(from, location);
				lastLocation = location;
				lastIntersection = li;

			} else {
				LinearLocation locationFrom = lastLocation;
				solution = indexedLine.extractLine(locationFrom, location);
				lastLocation = location;
				lastIntersection = li;
			}
			splittedLsList.add(solution);
		}// for
		LinearLocation endLocation = new LinearLocation();
		endLocation.setToEnd(lineString);
		Geometry geo = indexedLine.extractLine(lastLocation, endLocation);
		splittedLsList.add(geo);
		splittedLS = new LineString[splittedLsList.size()];
		splittedLsList.toArray(splittedLS);
		
		return splittedLS;

	}

	/**
	 * to propper work, LineStringSplitter needs coordinates to split a line. in
	 * a self intersected line case, we must to repeat self interesections
	 * coordinates, because self intersections produce cycles
	 */

	public static Coordinate[] duplicateSelfIntersections(
			Coordinate[] selfIntersections) {
		Coordinate[] solution = new Coordinate[selfIntersections.length * 2];
		for (int i = 0; i < selfIntersections.length; i++) {
			solution[2 * i] = selfIntersections[i];
			solution[(2 * i) + 1] = selfIntersections[i];
		}
		return solution;
	}
	
	
//	FIXME Introduce snap tolerance concept
	

	public static LineString[] removeSelfIntersections(LineString lineString,
			Coordinate[] splitPoints, double snapTolerance) {
		if (splitPoints.length < 1) {
			return new LineString[] { lineString };
		}
		
		Coordinate[] duplicatedSelfIntersections = duplicateSelfIntersections(splitPoints);
		//TODO Create a test case for a linestring not closed for precision reasons
		//but closed with a given snap tolerance
		if(JtsUtil.isClosed(lineString, snapTolerance)){
//		if (lineString.isClosed()) {
			return splitClosedLineString(lineString,
					duplicatedSelfIntersections, snapTolerance);
		} else {
			return splitUnclosedLineString(lineString,
					duplicatedSelfIntersections, snapTolerance);
		}

	}
	
	
	public static LineString[] removeSelfIntersections(LineString lineString,
			Coordinate[] splitPoints){
		return removeSelfIntersections(lineString, splitPoints, DEFAULT_SNAP_TOLERANCE);
	}
}
