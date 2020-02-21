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
import com.iver.cit.gvsig.fmap.edition.EditableAdapter;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;

public class SplitSelfIntersectingLineFix extends AbstractTopologyErrorFix {

	public List<IFeature>[] fixAlgorithm(TopologyError error) throws BaseException {
		
		double clusterTolerance = error.getTopology().getClusterTolerance();
		
		IGeometry selfIntersectingPoints = error.getGeometry();
		FMultiPoint2D shape = (FMultiPoint2D) selfIntersectingPoints.
													getInternalShape();
		
		MultiPoint selfIntersections = (MultiPoint)NewFConverter.toJtsGeometry(shape);
		
		IFeature originFeature = error.getFeature1();
		Value[] attributes = originFeature.getAttributes();
		IGeometry originGeometry = originFeature.getGeometry();
		Geometry jtsGeo = NewFConverter.toJtsGeometry(originGeometry);
		//TODO Maybe we must change array for a List<LineString)
		LineString[] lines = JtsUtil.extractLineStrings(jtsGeo);
		for(int i = 0; i < lines.length; i++){
			LineString line = lines[i];
			boolean  disjoints = true;
//			if(!line.disjoint(selfIntersections)){
				//we comment this original code because we have found an inconsistent test case with JTS 1.9
				/*
				We have:
						LINESTRING (-34.89284 126.02532, -25.70532 122.96281, -21.05876 117.47142, -25.81092 114.30331, -29.82386 120.74514, -27.60619 128.77101, -21.90359 130.24946, -14.40573 127.18696, -10.7096 117.15461, -13.5609 116.09858, -18.20746 122.75161, -6.48546 129.51024, -2.89493 120.95634, 8.40466 126.23653, 0.80119 126.44773, -8.06951 122.5404, -8.06951 122.5404, -8.06951 122.5404)
						MULTIPOINT (-28.914741841726936 124.03284485429363, -13.723360261472408 125.3366439538489, -4.2628609229577705 124.21683965700947)
				JTS sais a.disjoints(b) is true
				 * */
			for(int j = 0; j < selfIntersections.getNumGeometries(); j++){
				if(line.intersects(selfIntersections.getGeometryN(j)))
					disjoints = false;
				    break;
			}
			
			if(! disjoints){
				//this is the self intersecting part
				SnapLineStringSelfIntersectionChecker checker =
					new SnapLineStringSelfIntersectionChecker(line, clusterTolerance);
				Geometry[] splittedGeometries = checker.clean();
				int splittedLength = splittedGeometries.length;
				IFeature[] solution = new IFeature[splittedLength];
				for(int j = 0; j < splittedLength; j++){
					IGeometry newGeometry = NewFConverter.toFMap(splittedGeometries[j]);
					solution[j] =  new DefaultFeature(newGeometry, attributes, originFeature.getID()+j);
				}
				
				List<IFeature> editedFeatures = new ArrayList<IFeature>();
				editedFeatures.addAll(Arrays.asList(solution));
				return (List<IFeature>[]) new List[]{editedFeatures};
			}
		}
		return null;
		
	}

	public void fix(TopologyError error) throws BaseException {

		EditableAdapter[] adapters = prepareEdition(error);
		List<IFeature>[] correctedFeatures = fixAlgorithm(error);
		if(correctedFeatures != null){
			List<IFeature> newFeatures = correctedFeatures[0];
			IFeature originalFeature = error.getFeature1();
			adapters[0].removeRow(Integer.parseInt(originalFeature.getID()), 
													getEditionDescription(), 
													EditionEvent.GRAPHIC);
		
			for(int i = 0; i < newFeatures.size(); i++){
				adapters[0].doAddRow(newFeatures.get(i), 
						EditionEvent.GRAPHIC);
			}
			error.getTopology().removeError(error);
		}

		
	}

	public String getEditionDescription() {
		return Messages.getText("SPLIT_SELFINTERSECTING_FIX");
	}

}
