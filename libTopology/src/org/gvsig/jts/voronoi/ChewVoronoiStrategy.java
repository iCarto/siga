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
import java.util.List;

import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.jts.voronoi.chew.DTriangulationForJTS;
import org.gvsig.topology.Messages;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.utiles.swing.threads.CancellableProgressTask;
import com.vividsolutions.jts.geom.Coordinate;

public class ChewVoronoiStrategy extends AbstractVoronoiStrategy {

	public List<TriangleFeature> createTin(VoronoiAndTinInputLyr inputLyr,
			boolean onlySelection, CancellableProgressTask progressMonitor)
			throws BaseException {

		DTriangulationForJTS ojVoronoi = new DTriangulationForJTS(inputLyr,
				onlySelection);
		List<Coordinate[]> delaunayEdges = ojVoronoi
				.getTinTriangles(progressMonitor);
		return createTinFeatureCollection(delaunayEdges);
	}

	private List<TriangleFeature> createTinFeatureCollection(
			List<Coordinate[]> geoms) {
		List<TriangleFeature> solution = new ArrayList<TriangleFeature>();
		for (int i = 0; i < geoms.size(); i++) {
			Coordinate[] jtsGeom = geoms.get(i);
			Point2D[] points = FGeometryUtil.getCoordinatesAsPoint2D(jtsGeom);
			FTriangle triangle = new FTriangle(points[0], points[1], points[2]);
			Value fid = ValueFactory.createValue(i);
			Value associatedVertex = ValueFactory.createValue(i);
			Value[] values = new Value[] { fid, associatedVertex };
			TriangleFeature feature = new TriangleFeature(triangle, values,
					new UID().toString());
			solution.add(feature);
		}

		return solution;
	}

	public String getName() {
		return Messages.getText("Chew_Incremental_TIN_based_in_a_bounding_triangle");
	}

}
