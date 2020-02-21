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

import com.iver.cit.gvsig.fmap.core.IFeature;

/**
 * It models a generic parameter for gvSIG application.
 * 
 * Initially its used for ITopologyErrorWithParameters error fixed, althought
 * it has been designed for many contexts.
 * 
 * 
 * @author Alvaro Zabala
 *
 */
public abstract class GParameter {
	
	public static final Class<IFeature> FEATURE_PARAM_TYPES = IFeature.class;
	public static final Class<Number> NUMBER_PARAM_TYPE = Number.class;
	
	protected String paramName;
	protected Class paramType;
	protected Object defaultValue;
	protected Object parentReference;
	
	public Object getParentReference() {
		return parentReference;
	}

	public void setParentReference(Object parentReference) {
		this.parentReference = parentReference;
	}
	
	public abstract void setValue(Object obj);
	

	/**
	 * Constructor.
	 * 
	 * @param paramName
	 * @param paramType
	 */
	public GParameter(String paramName, Class paramType, Object defaultValue, Object parentReference) {
		this.paramName = paramName;
		this.paramType = paramType;
		this.defaultValue = defaultValue;
		this.parentReference = parentReference;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public Class getParamType() {
		return paramType;
	}

	public void setParamType(Class paramType) {
		this.paramType = paramType;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Object getValue() {
			try {
				return parentReference.getClass().getDeclaredField(paramName).get(parentReference);
	//			return paramType.getField(paramName).get(parentReference);
			} catch (Exception e) {
				e.printStackTrace();
				return defaultValue;
			}
		}
}

 




