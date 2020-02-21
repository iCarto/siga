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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.iver.cit.gvsig.util.SnappingCoordinateMap;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.EdgeIntersection;
import com.vividsolutions.jts.geomgraph.EdgeIntersectionList;
import com.vividsolutions.jts.geomgraph.GeometryGraph;
import com.vividsolutions.jts.geomgraph.index.EdgeSetIntersector;
import com.vividsolutions.jts.geomgraph.index.SegmentIntersector;
import com.vividsolutions.jts.geomgraph.index.SimpleMCSweepLineIntersector;

/**
 * 
 * Cheks if a given linestring has selfintersections, and gives methods to split
 * a line with self intersections in many lines.
 * 
 * Doesnt consideer snap tolerance.
 * 
 * @author azabala
 * 
 */
public class LineStringSelfIntersectionChecker {

	public final static double DEFAULT_SNAP_TOLERANCE = 0d;

	protected LineString lineString;

	protected GeometryGraph graph;


	protected boolean hasInitialized = false;

	protected Coordinate[] selfIntersections;

	protected double snapTolerance;

	
	
	public LineStringSelfIntersectionChecker(LineString lineString,
			double snapTolerance) {
		this.lineString = lineString;
		this.snapTolerance = snapTolerance;
		this.graph = new GeometryGraph(0, lineString);
	}

	
	public LineStringSelfIntersectionChecker(LineString lineString) {
		this(lineString, DEFAULT_SNAP_TOLERANCE);
	}

	public boolean hasSelfIntersections() {
		if (!hasInitialized) {
			initialize();
			hasInitialized = true;
		}// if
		if (selfIntersections != null && selfIntersections.length > 0)
			return true;
		else
			return false;
	}

	/**
	 * Receives a coordinate of the geometry, and returns if it could be
	 * consideered part of the graph (proper).
	 * 
	 * In linestrings, end points are proper (must not be consideered self
	 * intersection)
	 * 
	 * In linearrings, anything is boundary
	 * 
	 * @param coordinate
	 * @return
	 */
	public boolean isProper(Coordinate coordinate) {
		if (lineString instanceof LinearRing){
//			return coordinate.equals2D(lineString.getCoordinateN(0));
			//in a LinearRing first and last point must be equals
			return SnapCGAlgorithms.snapEquals2D(coordinate, lineString
					.getCoordinateN(0), snapTolerance);
			
		}
		
		
		
		
		
		
		
		
		else {
			boolean solution = false;
//			boolean solution = graph.isBoundaryNode(0, coordinate);
//			if (!solution) {
				// one more try: see the coordinates of the extremes
				// this is needed becase the labeling process for closed
				// linestring
				// dont gives us the desired results
				if (SnapCGAlgorithms.snapEquals2D(coordinate, lineString.getCoordinateN(0), snapTolerance) || 
					SnapCGAlgorithms.snapEquals2D(coordinate, lineString.getCoordinateN(lineString.getNumPoints() - 1), snapTolerance)) 
				{
					//the coordinate is equal to one of the nodes of a lineString
					//if the lineString is not closed, it could not be proper
					if(JtsUtil.isClosed(lineString, snapTolerance))
						solution = true;
				}// if
//			}// if
			return solution;
		}// else
	}

	// TODO Add snap tolerance concept
	// trick extracted from JTS examples
	// TODO REVISAR CUAL ES MEJOR, initialize o initialize2
//	private void initialize2() {
//		List endPtList = new ArrayList();
//		endPtList.add(lineString.getCoordinateN(0));
//		endPtList.add(lineString.getCoordinateN(lineString.getNumPoints() - 1));
//		Coordinate[] endPts = CoordinateArrays.toCoordinateArray(endPtList);
//		Geometry lineStringNodes = new GeometryFactory()
//				.createMultiPoint(endPts);
//		Geometry nodedLine = lineString.union(lineStringNodes);
//		List endPtList2 = new ArrayList();
//		endPtList2.add(((LineString) nodedLine).getCoordinateN(0));
//		endPtList2.add(((LineString) nodedLine).getCoordinateN(lineString
//				.getNumPoints() - 1));
//		Coordinate[] endPts2 = CoordinateArrays.toCoordinateArray(endPtList2);
//		Geometry lineStringNodes2 = new GeometryFactory()
//				.createMultiPoint(endPts2);
//		Geometry selfs = lineStringNodes.difference(lineStringNodes2);
//		selfIntersections = selfs.getCoordinates();
//	}

	// TODO Add snap tolerance concept
	// this algorithm has been designed by us
	protected void initialize() {
		ArrayList<Coordinate> selfIntersectionsList = new ArrayList<Coordinate>();
		SnappingCoordinateMap selfIntersectionsMap = new SnappingCoordinateMap(
				snapTolerance);
		List edges = computeSelfNodes();
//		List edges = computeSelfNodesWithSnap();
		Iterator i = edges.iterator();
		while (i.hasNext()) {// for each edge graph
			Edge e = (Edge) i.next();
			EdgeIntersectionList eiList = e.getEdgeIntersectionList();
			for (Iterator eiIt = eiList.iterator(); eiIt.hasNext();) {
				EdgeIntersection ei = (EdgeIntersection) eiIt.next();
				if (!isProper(ei.coord)) {
					if (!selfIntersectionsMap.containsKey(ei.coord)) {
						selfIntersectionsList.add(ei.coord);
						selfIntersectionsMap.put(ei.coord, ei.coord);
					}//if
				}//if
			}// for
		}// while
		int numSelfIntersections = selfIntersectionsList.size();
		if (numSelfIntersections > 0) {
			selfIntersections = new Coordinate[numSelfIntersections];
			selfIntersectionsList.toArray(selfIntersections);
		}
	}

	/**
	 * This method is needed, instead graph.computeSelfNodes because when a
	 * geometry is of type linestring, the call to
	 * esi.computeIntersections(edges, si, param) always pass param = false
	 * (self intersections ara allowed for linestrings in OGC SFS geometry
	 * model)
	 * 
	 * We need param=true
	 * 
	 */

	// TODO Add snap tolerance concept
	private List computeSelfNodes() {
		SegmentIntersector si = new SegmentIntersector(
				new RobustLineIntersector(), true, false);
		EdgeSetIntersector esi = new SimpleMCSweepLineIntersector();
		List edges = new ArrayList();
		Iterator edgesIt = graph.getEdgeIterator();
		while (edgesIt.hasNext()) {
			edges.add(edgesIt.next());
		}
		esi.computeIntersections(edges, si, true);
		return edges;
	}
	
	
//	private List computeSelfNodesWithSnap() {
//		LineIntersector li = new SnapLineIntersector(snapTolerance);
//		SegmentIntersector si = new SegmentIntersector(li, true, false);
//		SnapSimpleMCSweepLineIntersector esi = new SnapSimpleMCSweepLineIntersector();
//		List edges = new ArrayList();
//		Iterator edgesIt = graph.getEdgeIterator();
//		while (edgesIt.hasNext()) {
//			edges.add(edgesIt.next());
//		}
//		esi.computeIntersections(edges, si, true);
//		return edges;
//	}
	

	/**
	 * Returns the self intersections of the linestring
	 */
	public Coordinate[] getSelfIntersections() {
		return selfIntersections;
	}

	public Geometry[] clean() {
		if (!hasInitialized) {
			initialize();
			hasInitialized = true;
		}// if
		if (selfIntersections == null && selfIntersections.length < 1)
			return new Geometry[] { lineString };
		else
			return LineStringSplitter.removeSelfIntersections(lineString,
					selfIntersections, snapTolerance);
	}

	public LineString getLineString() {
		return lineString;
	}

	public void setLineString(LineString lineString) {
		this.lineString = lineString;
	}

}
