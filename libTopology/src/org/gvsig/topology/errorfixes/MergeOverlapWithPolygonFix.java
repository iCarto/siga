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
import org.gvsig.topology.Messages;
import org.gvsig.topology.TopologyError;

import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.vividsolutions.jts.geom.Geometry;

/**
 * This fix is similar to MergeOverlap, but it works with two layers
 * intead one only layer.
 * 
 * @author Alvaro Zabala
 *
 */
public class MergeOverlapWithPolygonFix extends MergeOverlapPolygonFix {
	
	public IFeature featureToPreserve;
	
	public MergeOverlapWithPolygonFix() {
		super();
	}
	
	public List<IFeature>[] fixAlgorithm(TopologyError error) throws BaseException {
		
		List<IFeature>[] solution = null;
		
		IGeometry errorGeometry = error.getGeometry();
		Geometry errorGeoJts = NewFConverter.toJtsGeometry(errorGeometry);
		
		IFeature firstFeature = error.getFeature1();
		Geometry firstJts =NewFConverter.toJtsGeometry(firstFeature.getGeometry());
		
		IFeature secondFeature = error.getFeature2();
		Geometry secondJts = NewFConverter.toJtsGeometry(secondFeature.getGeometry());
		
		List<IFeature> firstLyrEditedFeatures = new ArrayList<IFeature>();
		List<IFeature> secondLyrEditedFeatures = new ArrayList<IFeature>();
		
		if(featureToPreserve.getID().equals(firstFeature.getID())){
			//we remove the overlapping area of first feature
			firstFeature = FeatureUtil.removeOverlappingArea(firstFeature, 
													firstJts, 
													errorGeoJts);
			//and adds to second feature
			secondJts = secondJts.union(errorGeoJts);
			secondFeature.setGeometry(NewFConverter.toFMap(secondJts));
		}else{
			secondFeature = FeatureUtil.removeOverlappingArea(secondFeature, 
					secondJts, 
					errorGeoJts);
			
			firstJts = firstJts.union(errorGeoJts);
			firstFeature.setGeometry(NewFConverter.toFMap(firstJts));
		}
		firstLyrEditedFeatures.add(firstFeature);
		secondLyrEditedFeatures.add(secondFeature);
		
		solution = (List<IFeature>[]) new List[]{firstLyrEditedFeatures, secondLyrEditedFeatures};
		return solution;
	}
	
	public String getEditionDescription() {
		return Messages.getText("MERGE_OVERLAP_AREA_WITH_FIX");
	}
}
