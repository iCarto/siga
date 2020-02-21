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

public final class GNumberParameter extends GParameter{

	private boolean integer = false;
	
	
	public GNumberParameter(String paramName, Number defaultValue, 
							Object paramReference, boolean integer) {
		super(paramName, NUMBER_PARAM_TYPE, defaultValue, paramReference);
		this.integer = integer;
	}
	
	public boolean isInteger(){
		return integer;
	}
	
	public void setInteger(boolean integer){
		this.integer = integer;
	}
	
	public void setValue(Object obj) {
		try {
			Field field;
			if(! (obj instanceof Number)){
//				paramType.getField(paramName).set(parentReference, defaultValue);
				field = parentReference.getClass().getDeclaredField(paramName);
				field.setAccessible(true);
				field.set(parentReference, defaultValue);
			}
			if(isInteger()){
				field = parentReference.getClass().getDeclaredField(paramName);
			    field.setAccessible(true);
			    field.set(parentReference, new Integer(((Number)obj).intValue()));
			}
//				paramType.getField(paramName).set(parentReference, new Integer(((Number)obj).intValue()));
			else{
				field = parentReference.getClass().getDeclaredField(paramName);
			    field.setAccessible(true);
			    field.set(parentReference, new Double(((Number)obj).doubleValue()));
			}
//				paramType.getField(paramName).set(parentReference, new Double(((Number)obj).doubleValue()));
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}