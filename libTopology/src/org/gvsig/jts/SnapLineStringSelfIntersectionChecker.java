/*
 * Created on 12-sep-2007
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
* $Log: SnapLineStringSelfIntersectionChecker.java,v $
* Revision 1.1  2007/09/14 17:35:52  azabala
* first version in cvs
*
*
*/
package org.gvsig.jts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.iver.cit.gvsig.util.SnappingCoordinateMap;
import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.EdgeIntersectionList;
import com.vividsolutions.jts.geomgraph.SnappingGeometryGraph;
import com.vividsolutions.jts.geomgraph.SnapEdgeIntersectionList.SnapEdgeIntersectNode;
import com.vividsolutions.jts.geomgraph.index.SegmentIntersector;
import com.vividsolutions.jts.geomgraph.index.SnapSimpleMCSweepLineIntersector;
import com.vividsolutions.jts.operation.overlay.SnapLineIntersector;

public class SnapLineStringSelfIntersectionChecker extends
		LineStringSelfIntersectionChecker {

	public SnapLineStringSelfIntersectionChecker(LineString lineString,
			double snapTolerance) {
		super(lineString, snapTolerance);
		this.graph = new SnappingGeometryGraph(snapTolerance,0, lineString);
	}

	
	public SnapLineStringSelfIntersectionChecker(LineString lineString) {
		this(lineString, DEFAULT_SNAP_TOLERANCE);
	}

	
	protected void initialize() {
		ArrayList selfIntersectionsList = new ArrayList();
		SnappingCoordinateMap selfIntersectionsMap = new SnappingCoordinateMap(
				snapTolerance);
		List edges = computeSelfNodesWithSnap();
		Iterator i = edges.iterator();
		while (i.hasNext()) {// for each edge graph
			Edge e = (Edge) i.next();
			EdgeIntersectionList eiList = e.getEdgeIntersectionList();
			for (Iterator eiIt = eiList.iterator(); eiIt.hasNext();) {
				Object obj =  eiIt.next();
				SnapEdgeIntersectNode ei = (SnapEdgeIntersectNode) obj;
				if (!isProper(ei.getCoordinate())) {
					if (!selfIntersectionsMap.containsKey(ei.getCoordinate())) {
						selfIntersectionsList.add(ei.getCoordinate());
						selfIntersectionsMap.put(ei.getCoordinate(), ei.getCoordinate());
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


	
	
	private List computeSelfNodesWithSnap() {
		LineIntersector li = new SnapLineIntersector(snapTolerance);
		SegmentIntersector si = new SegmentIntersector(li, true, false);
		SnapSimpleMCSweepLineIntersector esi = new SnapSimpleMCSweepLineIntersector();
		List edges = new ArrayList();
		Iterator edgesIt = graph.getEdgeIterator();
		while (edgesIt.hasNext()) {
			edges.add(edgesIt.next());
		}
		esi.computeIntersections(edges, si, true);
		return edges;
	}
	

	

//	public Geometry[] clean() {
//		if (!hasInitialized) {
//			initialize();
//			hasInitialized = true;
//		}// if
//		if (selfIntersections == null && selfIntersections.length < 1)
//			return new Geometry[] { lineString };
//		else
//			return LineStringSplitter.removeSelfIntersections(lineString,
//					selfIntersections, snapTolerance);
//	}

}

