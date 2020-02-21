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
import java.util.List;

import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.core.FeatureUtil;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.Messages;
import org.gvsig.topology.TopologyError;

import com.iver.cit.gvsig.fmap.core.IFeature;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

/**
 * Automatic fix to 'Polygon must not overlaps' topology error.
 * 
 * It removes from the two overlappings polygons their common area, leaving
 * a gap or void.
 * 
 * @author Alvaro Zabala
 *
 */
public class SubstractOverlapPolygonFix extends AbstractTopologyErrorFix {

	
	public List<IFeature>[] fixAlgorithm(TopologyError error) throws BaseException {
		Geometry errorJts = NewFConverter.toJtsGeometry(error.getGeometry());
		
		IFeature firstFeature = error.getFeature1();
		Geometry firstJts = NewFConverter.toJtsGeometry(firstFeature.getGeometry());
		
		IFeature secondFeature = error.getFeature2();
		Geometry secondJts = NewFConverter.toJtsGeometry(secondFeature.getGeometry());
	
		
		List<IFeature> editedFeatures = new ArrayList<IFeature>();
		
		//we could find three cases:
		//a) first covers second, we compute first difference second
		//b) they dont cover or are covered. we compute first difference second
		//c) second covers a, we compute second difference first, and modifies second
		if(secondJts.covers(firstJts)){
			
			//we remove the overlapped area from the container geometry
			
			secondFeature = FeatureUtil.removeOverlappingArea(secondFeature, 
															secondJts, 
															errorJts );
			editedFeatures.add(secondFeature);
			
		}else if(secondJts.coveredBy(firstJts))
		{
			//we remove secondJts from firstJts
			firstFeature = FeatureUtil.removeOverlappingArea(firstFeature, 
					firstJts, 
					errorJts );
			editedFeatures.add(firstFeature);
		}else{
			//we remove overlapping area from both features
			firstFeature = FeatureUtil.removeOverlappingArea(firstFeature, 
					firstJts, 
					errorJts );
			secondFeature = FeatureUtil.removeOverlappingArea(secondFeature, 
					secondJts, 
					errorJts );
			editedFeatures.add(secondFeature);
			editedFeatures.add(firstFeature);
		}
		return (List<IFeature>[]) new List[]{editedFeatures};
	}
	

	public String getEditionDescription() {
		return Messages.getText("SUBSTRACT_OVERLAP_AREA_FIX");
	}

}
