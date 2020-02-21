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
package org.gvsig.util;

import java.lang.reflect.Field;
import java.util.List;

import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
/**
 * A parameter of type 'IFeature'.
 * 
 * It receives a list of FLyrVect where we can fetch this feature.
 * @author Alvaro Zabala
 *
 */
public class GFeatureParameter extends GParameter {

	private List<FLyrVect> featureContainers;
	/**
	 * Constructor. It receives a feature container in adition to the rest of GParameter
	 * attributes.
	 * 
	 * @param paramName
	 * @param paramType
	 * @param defaultValue
	 * @param parentReference
	 * @param featureContainer
	 */
	public GFeatureParameter(String paramName, 
							 Class paramType,
							 Object defaultValue, 
							 Object parentReference,
							 List<FLyrVect> featureContainers) {
		super(paramName, FEATURE_PARAM_TYPES, defaultValue, parentReference);
		
		this.featureContainers = featureContainers;
	}

	
	public void setValue(Object obj) {
	
			Field field;
			try {
				field = parentReference.getClass().getDeclaredField(paramName);
				 field.setAccessible(true);
				 if(! (obj instanceof IFeature))
					field.set(parentReference, defaultValue);
				 else
					field.set(parentReference, obj);
			} catch (Exception e) {
				e.printStackTrace();
			} 
	}


	public List<FLyrVect> getFeatureContainer() {
		return featureContainers;
	}


	public void setFeatureContainer(List<FLyrVect> featureContainers) {
		this.featureContainers = featureContainers;
	}

}
