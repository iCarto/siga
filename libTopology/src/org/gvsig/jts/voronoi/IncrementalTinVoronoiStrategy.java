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
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.gvsig.exceptions.BaseException;
import org.gvsig.jts.voronoi.dt.IncrementalDT;
import org.gvsig.jts.voronoi.dt.TriangleDT;
import org.gvsig.topology.Messages;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.utiles.swing.threads.CancellableProgressTask;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Uses org.geotools.delaunay implementation. This implementation uses an
 * incremental delaunay triangulation algorithm, based in compute a triangle
 * which contains all mess points, and subdivide it in smaller triangles.
 * 
 * @author Alvaro Zabala
 * 
 */
public class IncrementalTinVoronoiStrategy extends AbstractVoronoiStrategy {

	public interface TriangleDTVisitor {
		public void visit(TriangleDT triangleDT);
	}

	private void iterateTriangles(IncrementalDT triangulation,
			TriangleDTVisitor visitor) {
		LinkedList triangles = triangulation.triangles.getLinkedList();
		Iterator trianglesIterator = triangles.iterator();
		while (trianglesIterator.hasNext()) {
			TriangleDT t = (TriangleDT) trianglesIterator.next();
			visitor.visit(t);
		}
	}

	private IncrementalDT createIncrementalDT(VoronoiAndTinInputLyr inputLyr,
			boolean onlySelection) throws BaseException {
		IncrementalDT triangulator = new IncrementalDT();
		int numberOfPoints = inputLyr.getSource().getShapeCount();
		for (int i = 0; i < numberOfPoints; i++) {
			if (onlySelection) {
				if (!inputLyr.getRecordset().getSelection().get(i))
					continue;
			}
			Point2D point = inputLyr.getPoint(i);
			Coordinate coord = new Coordinate(point.getX(), point.getY());
			triangulator.insertPoint(coord);
		}
		return triangulator;
	}

	public List<TriangleFeature> createTin(VoronoiAndTinInputLyr inputLyr,
			boolean onlySelection, CancellableProgressTask progressMonitor)
			throws BaseException {

		final List<TriangleFeature> solution = new ArrayList<TriangleFeature>();
		IncrementalDT triangulator = createIncrementalDT(inputLyr,
				onlySelection);
		iterateTriangles(triangulator, new TriangleDTVisitor() {
			int idx = 0;
			public void visit(TriangleDT t) {
				Point2D a = new Point2D.Double(t.A.x, t.A.y);
				Point2D b = new Point2D.Double(t.B.x, t.B.y);
				Point2D c = new Point2D.Double(t.C.x, t.C.y);
				
				FTriangle triangle = new FTriangle(a, b, c);
				Value fid = ValueFactory.createValue(idx);
				Value associatedVertex = ValueFactory.createValue(idx);
				Value[] values = new Value[] { fid, associatedVertex };
				TriangleFeature feature = new TriangleFeature(triangle, values, new UID().toString());
				solution.add(feature);
				idx++;
			}
		});
		return solution;
	}

	public String getName() {
		return Messages.getText("GT2_BOUNDING_TRIANGLE");
	}
}
