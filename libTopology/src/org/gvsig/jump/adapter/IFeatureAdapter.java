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
package org.gvsig.jump.adapter;

import java.rmi.server.UID;
import java.util.Date;

import org.gvsig.fmap.core.NewFConverter;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;

/**
 * Adapts a JUMP's feature to IFeature interface.
 * @author Alvaro Zabala
 *
 */
public class IFeatureAdapter implements IFeature {

	
	Feature feature;
	String uid;
	
	
	public IFeatureAdapter(Feature feature){
		this.feature = feature;
		this.uid = new UID().toString();
	}
	
	public IGeometry getGeometry() {
			
		Geometry geometry = feature.getGeometry();
		if(geometry != null)
			return NewFConverter.toFMap(geometry);
		else
			return null;
		
	}

	public void setGeometry(IGeometry geom) {
		Geometry geometry = NewFConverter.toJtsGeometry(geom);
		feature.setGeometry(geometry);
	}

	public IRow cloneRow() {
		return new IFeatureAdapter(this.feature.clone(true));
	}

	public Value getAttribute(int fieldId) {
		
		Object attr = feature.getAttribute(fieldId);
		AttributeType type = feature.getSchema().getAttributeType(fieldId);
		Value solution = ValueFactory.createNullValue();
		if(type == AttributeType.STRING){
			solution = ValueFactory.createValue((String) attr);
		}else if(type == AttributeType.DOUBLE){
			solution = ValueFactory.createValue(((Double)attr).doubleValue());
		}else if(type == AttributeType.INTEGER){
			solution = ValueFactory.createValue(((Integer)attr).intValue());
		}else if(type == AttributeType.DATE){
			solution = ValueFactory.createValue(((Date)attr));
		}
		return solution;
	}

	public Value[] getAttributes() {
		int numFields = feature.getSchema().getAttributeCount();
		Value[] solution = new Value[numFields];
		for(int i = 0; i < numFields; i++){
			solution[i] = getAttribute(i);
		}
		return solution;
	}

	public String getID() {
		return this.uid;
	}

	public void setAttributes(Value[] att) {
		int numValues = att.length;
		for(int i = 0; i < numValues; i++){
			feature.setAttribute(i, att[i]);
		}
	}

	public void setID(String ID) {
		this.uid = ID;
	}

}
