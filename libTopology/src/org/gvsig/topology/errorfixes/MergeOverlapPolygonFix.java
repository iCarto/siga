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
import org.gvsig.util.GFeatureParameter;
import org.gvsig.util.GParameter;

import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Automatic fix to 'Polygon must not overlaps' topology error.
 * 
 * It removes from one polygon the common area.
 * 
 * @author Alvaro Zabala
 *
 */

public class MergeOverlapPolygonFix extends AbstractTopologyErrorFix implements ITopologyErrorFixWithParameters {

	/**
	 * One of the topology error's causing features, which preserve
	 * its geometry.
	 */
	public IFeature featureToPreserve;
	
	/**
	 * Parameters needed for the fix
	 */
	GParameter[] parameters = null;
	
	public MergeOverlapPolygonFix() {
	}
	
	public List<IFeature>[] fixAlgorithm(TopologyError error) throws BaseException {
		
		List<IFeature>[] solution = null;
		
		IGeometry errorGeometry = error.getGeometry();
		Geometry errorGeoJts = NewFConverter.toJtsGeometry(errorGeometry);
		
		IFeature firstFeature = error.getFeature1();
		Geometry firstJts =NewFConverter.toJtsGeometry(firstFeature.getGeometry());
		
		IFeature secondFeature = error.getFeature2();
		Geometry secondJts = NewFConverter.toJtsGeometry(secondFeature.getGeometry());
		
		List<IFeature> editedFeatures = new ArrayList<IFeature>();
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
			//FIXME USAR JTSUTIL PARA CONTEMPLAR LA POSIBILIDAD DE QUE LLEGUEN MULTIGEOMETRIAS
			firstJts = firstJts.union(errorGeoJts);
			firstFeature.setGeometry(NewFConverter.toFMap(firstJts));
		}
		editedFeatures.add(firstFeature);
		editedFeatures.add(secondFeature);
		
		solution = (List<IFeature>[]) new List[]{editedFeatures};
		return solution;
	}
	
	
	public String getEditionDescription() {
		return Messages.getText("MERGE_OVERLAP_AREA_FIX");
	}


	public IFeature getFeatureToPreserve() {
		return featureToPreserve;
	}


	public void setFeatureToPreserve(IFeature featureToPreserve) {
		this.featureToPreserve = featureToPreserve;
	}


	public GParameter[] getParameters() {
		return parameters;
	}


	public void setParameterValue(String paramName, Object value) {
		if(paramName.equals("featureToPreserve")){
			if(value.getClass().isAssignableFrom(IFeature.class)){
				this.featureToPreserve = (IFeature) value; 
			}
		}	
		
	}

	public void initialize(TopologyError error) {
		List<FLyrVect> featureContainers = new ArrayList<FLyrVect>();
		featureContainers.add(error.getOriginLayer());
		if(error.getDestinationLayer() != null){
			featureContainers.add(error.getDestinationLayer());
		}
			
		parameters = new GParameter[]{
				new GFeatureParameter("featureToPreserve",  
							GParameter.FEATURE_PARAM_TYPES, 
							 error.getFeature1(), 
							 this,
							 featureContainers)
		};
	}

}
