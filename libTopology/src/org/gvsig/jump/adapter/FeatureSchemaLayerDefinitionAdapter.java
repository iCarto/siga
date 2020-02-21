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

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.LayerDefinition;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;

/**
 * gvSIG's LayerDefinition implementation based in an openjump's feature schema.
 * 
 * @author Alvaro Zabala
 *
 */
public class FeatureSchemaLayerDefinitionAdapter extends LayerDefinition{
	
	
	FeatureSchema featureSchema;
	
	
	public FeatureSchemaLayerDefinitionAdapter(FeatureSchema featureSchema){
		this.featureSchema = featureSchema;
	}
	
	public FieldDescription[] getFieldsDesc() {
		
		int numFields = featureSchema.getAttributeCount();
		List<FieldDescription> solution = new ArrayList<FieldDescription>(); 
		for(int i = 0; i < numFields; i++){
			String name = featureSchema.getAttributeName(i);
			AttributeType type = featureSchema.getAttributeType(i);
			if(type == AttributeType.GEOMETRY)
				continue;//LayerDefintion doestn consideer geometry types
			
			if(type == AttributeType.OBJECT)
				continue;
			FieldDescription fieldDesc = new FieldDescription();
			fieldDesc.setFieldName(name);
			convert(fieldDesc, type);
			solution.add(fieldDesc);
		}
		FieldDescription[] fieldDescArray = new FieldDescription[solution.size()];
		solution.toArray(fieldDescArray);
		return fieldDescArray;
	}

	
	public static void convert(FieldDescription fieldDescription, AttributeType type) {
		if(type == AttributeType.STRING){
			fieldDescription.setFieldType(Types.VARCHAR);
		}else if(type == AttributeType.DOUBLE){
			fieldDescription.setFieldType(Types.REAL);
			fieldDescription.setFieldLength(14);
			fieldDescription.setFieldDecimalCount(5);
		}else if(type == AttributeType.INTEGER){
			fieldDescription.setFieldType(Types.INTEGER);
		}else if(type == AttributeType.DATE){
			fieldDescription.setFieldType(Types.DATE);
		}
		
	}

	public void setFieldsDesc(FieldDescription[] fieldsDesc) {
		this.fieldsDesc = fieldsDesc;
	}


}
