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

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.voronoi.Voronoier.VoronoiStrategy;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.utiles.swing.threads.CancellableProgressTask;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Abstract base class for VoronoiStrategy implementations.
 * 
 * If offers common functionality to VoronoiStrategy subclasses, as for example
 * Voronoi polygons creation from TIN triangles
 * (tin and voronoi polygons are a dual)
 * @author Alvaro Zabala
 *
 */
public abstract class AbstractVoronoiStrategy implements VoronoiStrategy {

	public List<TriangleFeature> createTin(VoronoiAndTinInputLyr inputLyr,
			boolean onlySelection) throws BaseException {
		return createTin(inputLyr, onlySelection, null);
	}
	
	public List<IFeature> createThiessenPolygons(VoronoiAndTinInputLyr inputLyr,
			boolean onlySelection)
			throws BaseException {
		return createThiessenPolygons(inputLyr, onlySelection, null);
	}

	public List<IFeature> createThiessenPolygons(
			VoronoiAndTinInputLyr inputLyr, boolean onlySelection,
			CancellableProgressTask progressMonitor) throws BaseException {

		List<TriangleFeature> tin = createTin(inputLyr, onlySelection);
		Polygon messBoundingBox = (Polygon) Voronoier.getThiessenBoundingBox(
				inputLyr, onlySelection);
		List<Geometry> voronoiPolygons = Voronoier.getThiessenPolys(
				messBoundingBox, tin, progressMonitor);
		return createVoronoiPolygonsFeatureCollection(voronoiPolygons);
	}

	public List<IFeature> createVoronoiPolygonsFeatureCollection(
			List<Geometry> geoms) {
		List<IFeature> solution = new ArrayList<IFeature>();
		for (int i = 0; i < geoms.size(); i++) {
			Geometry jtsGeom = geoms.get(i);
			IGeometry fmapGeometry = NewFConverter.toFMap(jtsGeom);
			Value fid = ValueFactory.createValue(i);
			Value associatedVertex = ValueFactory.createValue(i);
			Value[] values = new Value[] { fid, associatedVertex };
			DefaultFeature feature = new DefaultFeature(fmapGeometry, values,
					new UID().toString());
			solution.add(feature);
		}

		return solution;
	}
	
	public String toString(){
		return getName();
	}

}
