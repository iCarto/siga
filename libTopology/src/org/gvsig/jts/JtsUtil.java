/*
 * Created on 18-sep-2007
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
 * $Log: JtsUtil.java,v $
 * Revision 1.1  2007/09/19 09:01:20  azabala
 * first version in cvs
 *
 *
 */
package org.gvsig.jts;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.Triangle;
import com.vividsolutions.jts.geom.util.GeometryEditor;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import com.vividsolutions.jts.geom.util.PointExtracter;
import com.vividsolutions.jts.geom.util.PolygonExtracter;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.EdgeIntersectionList;
import com.vividsolutions.jts.geomgraph.GeometryGraph;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import com.vividsolutions.jump.util2.CoordinateArrays;

import es.axios.udig.ui.editingtools.internal.geometryoperations.split.SplitStrategy;

/**
 * Utility methods for JTS library use.
 * 
 * @author azabala
 * 
 */
public class JtsUtil {
	
	/**
	 * Generalization factor to compute line generalization
	 */
	public static Double GENERALIZATION_FACTOR = new Double(10d);
	
	public static  PrecisionModel GVSIG_PRECISION_MODEL = new PrecisionModel(10000);

	public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(
			GVSIG_PRECISION_MODEL);

	public static final GeometryEditor GEOMETRY_EDITOR = new GeometryEditor(
			GEOMETRY_FACTORY);

	public static Geometry createGeometry(Coordinate[] coords,
			String geometryType) {
		if (geometryType.equalsIgnoreCase("POINT")) {
			return GEOMETRY_FACTORY.createPoint(coords[0]);
		} else if (geometryType.equalsIgnoreCase("LINESTRING")) {
			return GEOMETRY_FACTORY.createLineString(coords);
		} else if (geometryType.equalsIgnoreCase("LINEARRING")) {
			return GEOMETRY_FACTORY.createLinearRing(coords);
		}
		// else if (geometryType.equalsIgnoreCase("POLYGON")) {
		// // LinearRing exterior = geomFactory.createLinearRing(coords);
		// // return geomFactory.createPolygon(exterior, null);
		// }
		else if (geometryType.equalsIgnoreCase("MULTIPOINT")) {
			return GEOMETRY_FACTORY.createMultiPoint(coords);
		}
		return null;
	}

	public static Polygon createPolygon(Coordinate[] coords, int[] indexOfParts) {
		int numberOfHoles = indexOfParts.length - 1;
		int firstPointOfShell = 0;
		int lastPointOfShell;
		if (numberOfHoles == 0) {
			lastPointOfShell = coords.length - 1;
		} else {
			lastPointOfShell = indexOfParts[1] - 1;
		}
		Coordinate[] shellCoords = new Coordinate[lastPointOfShell
				- firstPointOfShell + 1];
		System.arraycopy(coords, firstPointOfShell, shellCoords, 0,
				shellCoords.length);
		LinearRing shell = GEOMETRY_FACTORY.createLinearRing(shellCoords);

		LinearRing[] holes = null;
		if (numberOfHoles > 0) {
			holes = new LinearRing[numberOfHoles];
			int firstPointOfHole, lastPointOfHole;
			Coordinate[] holeCoords;
			for (int i = 1; i < indexOfParts.length - 1; i++) {
				firstPointOfHole = indexOfParts[i];
				lastPointOfHole = indexOfParts[i + 1] - 1;
				holeCoords = new Coordinate[lastPointOfHole - firstPointOfHole
						+ 1];
				System.arraycopy(coords, firstPointOfHole, holeCoords, 0,
						holeCoords.length);
				holes[i - 1] = GEOMETRY_FACTORY.createLinearRing(holeCoords);
			}
			firstPointOfHole = indexOfParts[indexOfParts.length - 1];
			lastPointOfHole = coords.length - 1;
			holeCoords = new Coordinate[lastPointOfHole - firstPointOfHole + 1];
			System.arraycopy(coords, firstPointOfHole, holeCoords, 0,
					holeCoords.length);
			holes[holes.length - 1] = GEOMETRY_FACTORY
					.createLinearRing(holeCoords);
		}
		return GEOMETRY_FACTORY.createPolygon(shell, holes);
	}

	/**
	 * Returns an array of lenght equals to the number of parts of a polygon.
	 * Each member of the array has a 'pointer' to the first point of each part
	 * in the coordinates array of the polygon.
	 * 
	 * 
	 * @param polygon
	 * @see createPolygon(Coordinate[] coords, int[] indexOfParts)
	 * @return
	 */
	public static int[] getIndexOfParts(Polygon polygon) {
		int numParts = polygon.getNumInteriorRing() + 1;
		int[] indexOfParts = new int[numParts];
		indexOfParts[0] = 0;
		if (numParts > 1) {
			indexOfParts[1] = polygon.getExteriorRing().getNumPoints();
		}
		if (numParts > 2) {
			for (int i = 1; i < polygon.getNumInteriorRing(); i++) {
				indexOfParts[i + 1] = indexOfParts[i]
						+ polygon.getInteriorRingN(i).getNumPoints() + 1;
			}
		}
		return indexOfParts;
	}

	public static MultiLineString convertToMultiLineString(
			GeometryCollection geomCol) {
		List<LineString> lines = new ArrayList<LineString>();
		int numGeometries = geomCol.getNumGeometries();
		for (int i = 0; i < numGeometries; i++) {
			Geometry geom = geomCol.getGeometryN(i);
			if (geom instanceof LineString)
				lines.add((LineString) geom);
			else if (geom instanceof MultiLineString) {
				MultiLineString multiLine = (MultiLineString) geom;
				int numLines = multiLine.getNumGeometries();
				for (int j = 0; j < numLines; j++) {
					lines.add((LineString) multiLine.getGeometryN(j));
				}// j
			}// else
			else if (geom instanceof GeometryCollection) {
				MultiLineString multiLine = convertToMultiLineString((GeometryCollection) geom);
				int numLines = multiLine.getNumGeometries();
				for (int j = 0; j < numLines; j++) {
					lines.add((LineString) multiLine.getGeometryN(j));
				}// j
			}// else
		}// for i
		LineString[] lineArray = new LineString[lines.size()];
		lines.toArray(lineArray);
		return GEOMETRY_FACTORY.createMultiLineString(lineArray);
	}

	public static MultiPolygon convertToMultiPolygon(GeometryCollection geomCol) {
		List<Polygon> polygons = new ArrayList<Polygon>();
		int numGeometries = geomCol.getNumGeometries();
		for (int i = 0; i < numGeometries; i++) {
			Geometry geom = geomCol.getGeometryN(i);
			if (geom instanceof Polygon)
				polygons.add((Polygon) geom);
			else if (geom instanceof MultiPolygon) {
				MultiPolygon multiPol = (MultiPolygon) geom;
				int numPols = multiPol.getNumGeometries();
				for (int j = 0; j < numPols; j++) {
					polygons.add((Polygon) multiPol.getGeometryN(j));
				}// j
			}// else
			else if (geom instanceof GeometryCollection) {
				MultiPolygon multiPol = convertToMultiPolygon((GeometryCollection) geom);
				int numPols = multiPol.getNumGeometries();
				for (int j = 0; j < numPols; j++) {
					polygons.add((Polygon) multiPol.getGeometryN(j));
				}// j
			}// else
		}// for i
		Polygon[] polyArray = new Polygon[polygons.size()];
		polygons.toArray(polyArray);
		return GEOMETRY_FACTORY.createMultiPolygon(polyArray);
	}

	public static boolean isClosed(Geometry geom) {
		return isClosed(geom, 0d);
	}

	public static Geometry getGeometryToClose(Geometry geom,
			double snapTolerance) {
		if ((geom instanceof Point) || (geom instanceof MultiPoint))
			return null;
		else if (geom instanceof GeometryCollection) {
			GeometryCollection solution = null;
			List<Geometry> solutionGeoms = new ArrayList<Geometry>();
			GeometryCollection geomCol = (GeometryCollection) geom;
			for (int i = 0; i < geomCol.getNumGeometries(); i++) {
				Geometry tempGeom = geomCol.getGeometryN(i);
				Geometry geo2close = getGeometryToClose(tempGeom, snapTolerance);
				solutionGeoms.add(geo2close);
			}

			Geometry[] solutionG = GeometryFactory
					.toGeometryArray(solutionGeoms);
			solution = GEOMETRY_FACTORY.createGeometryCollection(solutionG);
			return solution;
		} else if (geom instanceof LineString) {
			LineString solution = null;
			LineString tempGeom = (LineString) geom;
			Coordinate start = tempGeom.getCoordinateN(0);
			Coordinate end = tempGeom
					.getCoordinateN(tempGeom.getNumPoints() - 1);
			if (!SnapCGAlgorithms.snapEquals2D(start, end, snapTolerance)) {
				Coordinate[] coordinates = { start, end };
				solution = GEOMETRY_FACTORY.createLineString(coordinates);
			}
			return solution;

		} else if (geom instanceof Polygon) {
			GeometryCollection solution = null;
			List<Geometry> solutionGeoms = new ArrayList<Geometry>();
			Polygon polygon = (Polygon) geom;
			LineString ring = polygon.getExteriorRing();
			Geometry ring2close = getGeometryToClose(ring, snapTolerance);
			if (ring2close != null)
				solutionGeoms.add(ring2close);
			// TODO Must we check all polygon rings, or only exterior ring?
			int numHoles = polygon.getNumInteriorRing();
			for (int i = 0; i < numHoles; i++) {
				LineString hole = polygon.getInteriorRingN(i);
				Geometry hole2close = getGeometryToClose(hole, snapTolerance);
				if (hole != null)
					solutionGeoms.add(hole2close);
			}
			Geometry[] solutionArray = GeometryFactory
					.toGeometryArray(solutionGeoms);
			if (solutionArray.length > 0)
				solution = GEOMETRY_FACTORY
						.createGeometryCollection(solutionArray);
			return solution;
		}// else

		return null;
	}

	public static boolean isClosed(Geometry geom, double snapTolerance) {
		if ((geom instanceof Point) || (geom instanceof MultiPoint))
			return false;
		else if (geom instanceof GeometryCollection) {
			GeometryCollection geomCol = (GeometryCollection) geom;
			for (int i = 0; i < geomCol.getNumGeometries(); i++) {
				Geometry tempGeom = geomCol.getGeometryN(i);
				if (!isClosed(tempGeom, snapTolerance))
					return false;
			}
			return true;
		} else if (geom instanceof LineString) {
			LineString tempGeom = (LineString) geom;
			return SnapCGAlgorithms.snapEquals2D(tempGeom.getCoordinateN(0),
					tempGeom.getCoordinateN(tempGeom.getNumPoints() - 1),
					snapTolerance);
		} else if (geom instanceof Polygon) {
			Polygon polygon = (Polygon) geom;
			LineString ring = polygon.getExteriorRing();
			if (!isClosed(ring, snapTolerance))
				return false;
			// TODO Must we check all polygon rings, or only exterior ring?
			int numHoles = polygon.getNumInteriorRing();
			for (int i = 0; i < numHoles; i++) {
				LineString hole = polygon.getInteriorRingN(i);
				if (!isClosed(hole, snapTolerance))
					return false;
			}
			return true;
		}// else
		return false;
	}

	public static Coordinate[] getPoint2DAsCoordinates(Point2D[] point2d) {
		Coordinate[] solution = new Coordinate[point2d.length];
		for (int i = 0; i < point2d.length; i++) {
			solution[i] = new Coordinate(point2d[i].getX(), point2d[i].getY());
		}
		return solution;
	}

	// TODO Quitar esto de FConverter, llevarlo a JtsUtil, y PROPONER LA
	// CREACION DE UN
	// PAQUETE JTS EN FMAP
	public static boolean pointInList(Coordinate testPoint,
			Coordinate[] pointList) {
		return pointInList(testPoint, pointList, 0d);
	}

	public static boolean pointInList(Coordinate testPoint,
			Coordinate[] pointList, double snapTolerance) {
		int t;
		int numpoints;
		Coordinate p;
		numpoints = Array.getLength(pointList);
		for (t = 0; t < numpoints; t++) {
			p = pointList[t];
			if (SnapCGAlgorithms.snapEquals2D(p, testPoint, snapTolerance))
				return true;
		}

		return false;
	}

	public static Coordinate findPtNotNode(Coordinate[] testCoords,
			LinearRing searchRing, GeometryGraph graph) {
		// find edge corresponding to searchRing.
		Edge searchEdge = graph.findEdge(searchRing);
		// find a point in the testCoords which is not a node of the searchRing
		EdgeIntersectionList eiList = searchEdge.getEdgeIntersectionList();
		// somewhat inefficient - is there a better way? (Use a node map, for
		// instance?)
		for (int i = 0; i < testCoords.length; i++) {
			Coordinate pt = testCoords[i];
			if (!eiList.isIntersection(pt))
				return pt;
		}
		return null;
	}

	/**
	 * This method is needed because call to LinearRing.reverse returns a
	 * LineString,
	 * 
	 * @param ring
	 * @return
	 */
//	public static LinearRing reverse(LinearRing ring) {
//		CoordinateSequence seq = ring.getCoordinateSequence();
//		CoordinateSequences.reverse(seq);
//		LinearRing solution = GEOMETRY_FACTORY.createLinearRing(seq);
//		return solution;
//	}
//	
//	
	public static LineString reverse(LineString lineString){
		LineString solution = (LineString) lineString.reverse();
		if(lineString instanceof LinearRing)
			solution = toLinearRing(lineString);
		return solution;
		
	}
	
	public static Geometry douglasPeuckerSimplify(Geometry geometry, double distTolerance){
		return DouglasPeuckerSimplifier.simplify(geometry, distTolerance);
	}
	
	public static Geometry topologyPreservingSimplify(Geometry geometry, double distTolerance){
		return TopologyPreservingSimplifier.simplify(geometry, distTolerance);
	}

	public static Envelope toSnapRectangle(Coordinate coord,
			double snapTolerance) {
		Envelope solution = null;

		double xmin = coord.x - snapTolerance;
		double xmax = coord.x + snapTolerance;
		double ymin = coord.y - snapTolerance;
		double ymax = coord.y + snapTolerance;
		solution = new Envelope(xmin, xmax, ymin, ymax);
		return solution;
	}
	
	public static Envelope rectangle2dToEnvelope(Rectangle2D rect){
		return new Envelope(rect.getMinX(),
							rect.getMaxX(),
							rect.getMinY(),
							rect.getMaxY());
	}

	public static Geometry[] extractGeometries(GeometryCollection geomCol) {
		Geometry[] solution;
		ArrayList<Geometry> geometries = new ArrayList<Geometry>();
		Stack<Geometry> stack = new Stack<Geometry>();
		stack.add(geomCol);
		while (stack.size() > 0) {
			Geometry geometry = (Geometry) stack.pop();
			if (geometry instanceof GeometryCollection) {
				GeometryCollection collection = (GeometryCollection) geometry;
				for (int i = 0; i < collection.getNumGeometries(); i++) {
					stack.add(collection.getGeometryN(i));
				}// for
			} else {
				geometries.add(geometry);
			}
		}// while
		solution = new Geometry[geometries.size()];
		return geometries.toArray(solution);
	}

	// TODO REESCRIBIR ESTO CON GENERICS
	// /**
	// * Adapts a Geometry <code>geom</code> to another type of geometry given
	// the desired geometry
	// * class.
	// * <p>
	// * Currently implemented adaptations:
	// * <ul>
	// * <li>Point -> MultiPoint. Wraps the Point on a single part MultiPoint.
	// * <li>Polygon -> MultiPolygon. Wraps the Polygon on a single part
	// MultiPolygon.
	// * <li>LineString -> MultiLineString. Wraps the LineString on a single
	// part MultiLineString.
	// * <li>MultiLineString -> String. Succeeds if merging the parts result in
	// a single LineString,
	// * fails otherwise.
	// * <li>MultiPolygon -> Polygon. Succeeds if merging the parts result in a
	// single Polygon, fails
	// * otherwise.
	// * <li>* -> GeometryCollection
	// * </ul>
	// * </p>
	// * TODO: add more adaptations on an as needed basis
	// *
	// * @param inputGeom
	// * @param adaptTo
	// * @return a new Geometry adapted
	// * @throws IllegalArgumentException if <code>geom</code> cannot be adapted
	// as
	// * <code>adapTo</code>
	// */
	// public static Geometry adapt( final Geometry inputGeom, final Class< ?
	// extends Geometry> adaptTo ) {
	//
	// assert inputGeom != null : "inputGeom can't be null";
	// assert adaptTo != null : "adaptTo can't be null";;
	//
	// final Class<?> geomClass = inputGeom.getClass();
	//
	// if (Geometry.class.equals(adaptTo)) {
	// return inputGeom;
	// }
	//
	// final GeometryFactory gf = inputGeom.getFactory();
	//       
	// if (MultiPoint.class.equals(adaptTo) && Point.class.equals(geomClass)) {
	// return gf.createMultiPoint(new Point[]{(Point) inputGeom});
	// }
	//
	// if (Polygon.class.equals(adaptTo)) {
	// if (adaptTo.equals(geomClass)) {
	// return inputGeom;
	// }
	// Polygonizer polygonnizer = new Polygonizer();
	// polygonnizer.add(inputGeom);
	// Collection polys = polygonnizer.getPolygons();
	// Polygon[] polygons = new ArrayList<Polygon>(polys).toArray(new
	// Polygon[polys.size()]);
	//
	// if (polygons.length == 1) {
	// return polygons[0];
	// }
	// }
	//
	// if (MultiPolygon.class.equals(adaptTo)) {
	// if (adaptTo.equals(geomClass)) {
	// return inputGeom;
	// }
	// if (Polygon.class.equals(geomClass)) {
	// return gf.createMultiPolygon(new Polygon[]{(Polygon) inputGeom});
	// }
	// /*
	// * Polygonizer polygonnizer = new Polygonizer();
	// polygonnizer.add(inputGeom); Collection
	// * polys = polygonnizer.getPolygons(); Polygon[] polygons = new
	// ArrayList<Polygon>(polys).toArray(new
	// * Polygon[polys.size()]); if (MultiPolygon.class.equals(adaptTo)) {
	// return
	// * gf.createMultiPolygon(polygons); } if (polygons.length == 1) { return
	// polygons[0]; }
	// */
	// }
	//
	// if (GeometryCollection.class.equals(adaptTo)) {
	// return gf.createGeometryCollection(new Geometry[]{inputGeom});
	// }
	//
	// if (MultiLineString.class.equals(adaptTo) ||
	// LineString.class.equals(adaptTo)) {
	// LineMerger merger = new LineMerger();
	// merger.add(inputGeom);
	// Collection mergedLineStrings = merger.getMergedLineStrings();
	// ArrayList<LineString> lineList = new
	// ArrayList<LineString>(mergedLineStrings);
	// LineString[] lineStrings = lineList.toArray(new
	// LineString[mergedLineStrings.size()]);
	//
	// if (MultiLineString.class.equals(adaptTo)) {
	// MultiLineString line = gf.createMultiLineString(lineStrings);
	// return line;
	// }
	// if (lineStrings.length == 1) {
	// Geometry mergedResult = (Geometry) lineStrings[0];
	// return mergedResult;
	// }
	// }
	// if(Polygon.class.equals(adaptTo) &&
	// (MultiPolygon.class.equals(geomClass))){
	// // adapts multipolygon to polygon
	//            
	// assert inputGeom.getNumGeometries() == 1 : "the collection must have 1
	// element to adapt to Polygon";
	// return inputGeom.getGeometryN(1);
	//        
	// } else if(LineString.class.equals(adaptTo) &&
	// (MultiLineString.class.equals(geomClass))){
	// // adapts MultiLinestring to Linestring
	//            
	// assert inputGeom.getNumGeometries() == 1 : "the collection must have 1
	// element to adapt to Polygon";
	// return inputGeom.getGeometryN(1);
	// }
	//
	// final String msg =
	// MessageFormat.format(Messages.GeometryUtil_DonotKnowHowAdapt,
	// geomClass.getSimpleName(), adaptTo.getSimpleName());
	//
	// throw new IllegalArgumentException(msg);
	// }
	public static LineString[] extractLineStrings(Geometry g) {
		LineString[] solution = null;
		List<LineString> solutionList = new ArrayList<LineString>();
		if (g instanceof LineString)
			solutionList.add((LineString) g);
		else if (g instanceof GeometryCollection) {
			GeometryCollection geomCol = (GeometryCollection) g;
			List lines = LinearComponentExtracter.getLines(geomCol);
			solutionList.addAll(lines);
		}
		solution = new LineString[solutionList.size()];
		solutionList.toArray(solution);
		return solution;
	}

	public static Polygon[] extractPolygons(Geometry g) {
		Polygon[] solution = null;
		List<Polygon> solutionList = new ArrayList<Polygon>();
		if (g instanceof Polygon)
			solutionList.add((Polygon) g);
		else if (g instanceof GeometryCollection) {
			GeometryCollection geomCol = (GeometryCollection) g;
			List polygons = PolygonExtracter.getPolygons(geomCol);
			solutionList.addAll(polygons);
		}
		solution = new Polygon[solutionList.size()];
		solutionList.toArray(solution);
		return solution;
	}
	
	
	public static LineString[] extractRings(Polygon polygon){
		int numHoles = polygon.getNumInteriorRing();
		LineString[] solution = new LineString[numHoles + 1];
		solution[0] = polygon.getExteriorRing();
		for(int i = 0; i < numHoles; i++){
			solution[i+1] = polygon.getInteriorRingN(i);
		}
		return solution;
	}

	public static Point[] extractPoints(Geometry g) {
		Point[] solution = null;
		List<Point> solutionList = new ArrayList<Point>();
		if (g instanceof Point)
			solutionList.add((Point) g);
		else if (g instanceof GeometryCollection) {
			GeometryCollection geomCol = (GeometryCollection) g;
			List points = PointExtracter.getPoints(geomCol);
			solutionList.addAll(points);
		}
		solution = new Point[solutionList.size()];
		solutionList.toArray(solution);
		return solution;
	}

	/**
	 * Returns the segment of the specified geometry closest to the specified
	 * coordinate.
	 * 
	 * This code is extracted from JTS.
	 * 
	 * @param geometry
	 * @param target
	 * @return
	 */
	public static LineSegment segmentInRange(Geometry geometry,
			Coordinate target, double tolerance) {
		LineSegment closest = null;
		List coordArrays = CoordinateArrays.toCoordinateArrays(geometry, false);
		for (Iterator i = coordArrays.iterator(); i.hasNext();) {
			Coordinate[] coordinates = (Coordinate[]) i.next();
			for (int j = 1; j < coordinates.length; j++) {
				LineSegment candidate = new LineSegment(coordinates[j - 1],
						coordinates[j]);
				if (candidate.distance(target) > tolerance) {
					continue;
				}
				if ((closest == null)
						|| (candidate.distance(target) < closest
								.distance(target))) {
					closest = candidate;
				}
			}
		}
		return closest;
	}

	public static LinearRing toLinearRing(LineString lineString) {
		Coordinate[] coords = lineString.getCoordinates();
		return GEOMETRY_FACTORY.createLinearRing(coords);
	}

	private static Geometry removeOverShootFromLine(LineString line,
			double clusterTolerance) {
		SnapLineStringSelfIntersectionChecker checker = new SnapLineStringSelfIntersectionChecker(
				line, clusterTolerance);
		if (checker.hasSelfIntersections()) {
			// Over shoot
			Geometry[] geoms = checker.clean();
			List<Geometry> linesWithoutOvershoot = new ArrayList<Geometry>();
			for (int i = 0; i < geoms.length; i++) {
				LineString brokenLine = (LineString) geoms[i];
				if(JtsUtil.isClosed(brokenLine, clusterTolerance)){
//				if (brokenLine.isClosed()) {
					linesWithoutOvershoot.add(brokenLine);
				}// if
			}// for
			if (linesWithoutOvershoot.size() == 1)
				return linesWithoutOvershoot.get(0);
			else {
				Geometry[] solutionGeoms = new Geometry[linesWithoutOvershoot
						.size()];
				linesWithoutOvershoot.toArray(solutionGeoms);
				return GEOMETRY_FACTORY.createGeometryCollection(solutionGeoms);
			}
		} else {// this line doesnt have overshoots
			return line;
		}
	}

	public static Geometry removeOverShoot(Geometry jtsGeom,
			double clusterTolerance) {
		List<Geometry> jtsProcessed = new ArrayList<Geometry>();

		List<Geometry> geom2process = new ArrayList<Geometry>();

		if (jtsGeom instanceof GeometryCollection) {
			GeometryCollection collection = (GeometryCollection) jtsGeom;
			Geometry[] geometries = JtsUtil.extractGeometries(collection);
			geom2process.addAll(Arrays.asList(geometries));
		} else if ((jtsGeom instanceof Point)) {
			return jtsGeom;// this fix doesnt apply to point geometries. maybe
							// launch an exception?
		} else {
			geom2process.add(jtsGeom);
		}

		boolean allGeometriesPoint = true;
		for (int i = 0; i < geom2process.size(); i++) {
			Geometry geometry = geom2process.get(i);
			if (!(geometry instanceof Point)) {
				allGeometriesPoint = false;
				if (geometry instanceof LineString) {
					LineString line = (LineString) geometry;
					Geometry correctedLine = removeOverShootFromLine(line,
							clusterTolerance);
					jtsProcessed.add(correctedLine);
				} else if (geometry instanceof Polygon) {
					Polygon polygon = (Polygon) geometry;

					Geometry correctedShell = removeOverShoot(polygon
							.getExteriorRing(), clusterTolerance);
					if (!(correctedShell instanceof LinearRing)) {
						correctedShell = JtsUtil
								.toLinearRing((LineString) correctedShell);
					}
					LinearRing[] correctedHoles = new LinearRing[polygon
							.getNumInteriorRing()];
					for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
						LineString correctedHole = (LineString) removeOverShootFromLine(
								polygon.getInteriorRingN(j), clusterTolerance);
						if (!(correctedHole instanceof LinearRing)) {
							correctedHoles[j] = JtsUtil
									.toLinearRing((LineString) correctedHoles[j]);
						} else {
							correctedHoles[j] = (LinearRing) correctedHole;
						}
					}// for j

					Polygon newPolygon = GEOMETRY_FACTORY.createPolygon(
							(LinearRing) correctedShell, correctedHoles);
					jtsProcessed.add(newPolygon);
				}// else
			}
		}// for i

		if (allGeometriesPoint)
			return jtsGeom;
		if (jtsProcessed.size() == 1)
			return jtsProcessed.get(0);
		else {
			Geometry[] geoms = new Geometry[jtsProcessed.size()];
			jtsProcessed.toArray(geoms);
			return GEOMETRY_FACTORY.createGeometryCollection(geoms);
		}
	}

	/**
	 * Returns the coordinate resulting of extend segment the specified
	 * distance.
	 * 
	 * @param segment
	 * @param distance
	 * @return
	 */
	public static Coordinate extentLineSegment(LineSegment segment,
			double distance) {
		Coordinate solution = null;

		double segmentLenght = segment.getLength();
		Coordinate c0 = segment.getCoordinate(0);
		Coordinate c1 = segment.getCoordinate(1);
		double dx = c1.x - c0.x;
		double dy = c1.y - c0.y;

		double dx2 = dx * (segmentLenght + distance) / segmentLenght;
		double dy2 = dy * (segmentLenght + distance) / segmentLenght;

		solution = new Coordinate(c0.x + dx2, c0.y + dy2);
		return solution;
	}

	/**
	 * Checks if the given 'a' linestring is connected to the given 'b'
	 * linestring.
	 * 
	 * Two linestrings are connected if they share and end point.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean isConnected(LineString a, LineString b,
			double clusterTolerance) {
		Coordinate start = a.getCoordinateN(0);
		Coordinate end = a.getCoordinateN(a.getNumPoints());

		if (isEndPoint(b, start, clusterTolerance)
				|| isEndPoint(b, end, clusterTolerance))
			return true;
		else
			return false;
	}

	public static boolean isEndPoint(LineString a, Coordinate coord,
			double clusterTolerance) {

		Coordinate start = a.getCoordinateN(0);
		Coordinate end = a.getCoordinateN(a.getNumPoints() - 1);

		if (SnapCGAlgorithms.snapEquals2D(start, coord, clusterTolerance)
				|| SnapCGAlgorithms.snapEquals2D(end, coord, clusterTolerance))
			return true;
		else
			return false;
	}

	public static Geometry split(Geometry geom, LineString line) {
		return SplitStrategy.splitOp(geom, line);
	}

	/**
	 * Receives a collection of linear rings to form shells and a collection of
	 * linear rings to form holes and returns derived polygons.
	 * 
	 * @param shells
	 * @param holes
	 * @return
	 */
	public static Geometry buildPolygons(List<LinearRing> shells,
			List<LinearRing> holes) {

		ArrayList<List<LinearRing>> holesForShells = new ArrayList<List<LinearRing>>(
				shells.size());

		for (int i = 0; i < shells.size(); i++) {
			holesForShells.add(new ArrayList<LinearRing>());
		}

		for (int i = 0; i < holes.size(); i++) {// foreach hole
			LinearRing testRing = (LinearRing) holes.get(i);
			LinearRing minShell = null;
			Envelope minEnv = null;
			Envelope testEnv = testRing.getEnvelopeInternal();
			Coordinate testPt = testRing.getCoordinateN(0);
			LinearRing tryRing = null;

			// we look for the smallest ring that contains the hole
			for (int j = 0; j < shells.size(); j++) {
				tryRing = (LinearRing) shells.get(j);

				Envelope tryEnv = tryRing.getEnvelopeInternal();

				if (minShell != null) {
					minEnv = minShell.getEnvelopeInternal();
				}

				boolean isContained = false;
				Coordinate[] coordList = tryRing.getCoordinates();

				if (tryEnv.contains(testEnv)
						&& (CGAlgorithms.isPointInRing(testPt, coordList) || (pointInList(
								testPt, coordList)))) {
					isContained = true;
				}

				// check if this new containing ring is smaller than the current
				// minimum ring
				if (isContained) {
					if ((minShell == null) || minEnv.contains(tryEnv)) {
						minShell = tryRing;
					}
				}
			}

			if (minShell == null) {//this hole is not contained by any shell

				//we do the assumption that this hole is really a
				// shell (polygon)
				// whose point werent digitized in the right order
				LinearRing newRing = (LinearRing) JtsUtil.reverse(testRing);
				shells.add(newRing);
				holesForShells.add(new ArrayList<LinearRing>());
			} else {
				holesForShells.get(shells.indexOf(minShell)).add(testRing);
			}
		}// i

		Polygon[] polygons = new Polygon[shells.size()];

		for (int i = 0; i < shells.size(); i++) {
			polygons[i] = GEOMETRY_FACTORY.createPolygon(shells.get(i),
					(LinearRing[]) (holesForShells.get(i)).toArray(new LinearRing[0]));
		}
		
		if(polygons.length == 1)
			return polygons[0];
		else
			return GEOMETRY_FACTORY.createMultiPolygon(polygons);
	}
	
	public static Envelope getBoundingBox(List<Geometry> geometries){
		Envelope  envelope = new Envelope();
	    for (int i = 0; i < geometries.size(); i++) {
	        Geometry geometry = geometries.get(i);
	        envelope.expandToInclude(geometry.getEnvelopeInternal());
	    }
	    return envelope;
	}
	
	public static Coordinate getCircumCenter(Coordinate a, Coordinate b, Coordinate c){
		return Triangle.circumcentre(a, b, c);
	}

	public static Geometry difference(Geometry poly, List<Geometry> lineNeighbours){
		Geometry solution = null;
		List<Geometry> differences = new ArrayList<Geometry>();
		for(int i = 0; i < lineNeighbours.size(); i++){
			Geometry neighbour = lineNeighbours.get(i);
			if(poly.overlaps(neighbour))
				differences.add(EnhancedPrecisionOp.difference(poly, neighbour ));
		}
		solution = GEOMETRY_FACTORY.createGeometryCollection(differences.toArray(new Geometry[0]));
		return solution;
	}
}
