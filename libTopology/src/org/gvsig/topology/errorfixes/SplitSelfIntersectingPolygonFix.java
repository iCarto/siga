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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.jts.SnapLineStringSelfIntersectionChecker;
import org.gvsig.topology.Messages;
import org.gvsig.topology.TopologyError;

import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FMultiPoint2D;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Polygon;

public class SplitSelfIntersectingPolygonFix extends SplitSelfIntersectingLineFix {

	
	public List<IFeature>[] fixAlgorithm(TopologyError error) throws BaseException {
		double clusterTolerance = error.getTopology().getClusterTolerance();
		
		IGeometry selfIntersectingPoints = error.getGeometry();
		FMultiPoint2D shape = (FMultiPoint2D) selfIntersectingPoints.
													getInternalShape();
		
		MultiPoint selfIntersections = (MultiPoint)NewFConverter.toJtsGeometry(shape);
		
		IFeature originFeature = error.getFeature1();
		Value[] attributes = originFeature.getAttributes();
		IGeometry originGeometry = originFeature.getGeometry();
		Geometry jtsGeo = originGeometry.toJTSGeometry();
		
		Polygon[] polygons = JtsUtil.extractPolygons(jtsGeo);
		for(int i = 0; i < polygons.length; i++){
			Polygon polygon = polygons[i];
			
			if(!polygon.disjoint(selfIntersections)){
				
				List<LinearRing> shells = new ArrayList<LinearRing>();
				List<LinearRing> holes = new ArrayList<LinearRing>();
				
				LinearRing shell = (LinearRing) polygon.getExteriorRing();
				SnapLineStringSelfIntersectionChecker checker =
					new SnapLineStringSelfIntersectionChecker(shell, clusterTolerance);
				Geometry[] splittedGeometries = checker.clean();
				
//				shells.addAll((Collection<? extends LinearRing>) Arrays.asList(splittedGeometries));
				for(int k = 0; k < splittedGeometries.length; k++){ 
					shells.add(JtsUtil.toLinearRing((LineString) splittedGeometries[k]));
                }
				
				
				for(int j = 0; j < polygon.getNumInteriorRing(); j++){
					LinearRing hole = (LinearRing) polygon.getInteriorRingN(j);
					checker = new SnapLineStringSelfIntersectionChecker(hole, 
															clusterTolerance);
					splittedGeometries = checker.clean();
					
//					shells.addAll((Collection<? extends LinearRing>) Arrays.asList(splittedGeometries));
					for(int k = 0; k < splittedGeometries.length; k++){ 
						shells.add(JtsUtil.toLinearRing((LineString) splittedGeometries[k]));
	                }
				}
				
				Geometry editedGeometry = JtsUtil.buildPolygons(shells, holes);
				Polygon[] correctedPolygons = JtsUtil.extractPolygons(editedGeometry);
				IFeature[] newFeatures = new IFeature[correctedPolygons.length];
				
				for(int j = 0; j < correctedPolygons.length; j++){
					Polygon pol = correctedPolygons[j];
					IGeometry igeom = NewFConverter.toFMap(pol);
					newFeatures[j] =  new DefaultFeature(igeom, 
													attributes, 
													originFeature.getID()+j);
				}
				List<IFeature> editedFeatures = new ArrayList<IFeature>();
				editedFeatures.addAll(Arrays.asList(newFeatures));
				return (List<IFeature>[]) new List[]{editedFeatures};
			}//if ! disjoints
		}
		return null;
	}

	
	public String getEditionDescription() {
		return Messages.getText("SPLIT_SELFINTERSECTING_POLYGON_FIX");
	}

}
