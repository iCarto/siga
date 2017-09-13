/*
 * Created on 05-mar-2006
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
* $Id$
* $Log$
* Revision 1.2  2007-03-06 16:47:58  caballero
* Exceptions
*
* Revision 1.1  2006/06/20 18:20:45  azabala
* first version in cvs
*
* Revision 1.2  2006/06/02 18:21:28  azabala
* *** empty log message ***
*
* Revision 1.1  2006/05/24 21:10:15  azabala
* primera version en cvs despues de refactoring orientado a crear un framework extensible de geoprocessing
*
* Revision 1.5  2006/03/23 21:05:11  azabala
* *** empty log message ***
*
* Revision 1.4  2006/03/17 19:53:34  azabala
* *** empty log message ***
*
* Revision 1.3  2006/03/14 18:32:46  fjp
* Cambio con LayerDefinition para que sea compatible con la definición de tablas también.
*
* Revision 1.2  2006/03/07 21:01:33  azabala
* *** empty log message ***
*
* Revision 1.1  2006/03/05 19:59:16  azabala
* *** empty log message ***
*
*
*/
package com.iver.cit.gvsig.geoprocess.impl.merge.fmap;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.exceptions.visitors.ProcessVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StartVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.fmap.layers.layerOperations.VectorialData;
import com.iver.cit.gvsig.fmap.operations.strategies.FeatureVisitor;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeatureFactory;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeatureProcessor;

/**
 * Visits all features of a layer, in context of a
 * merge geoprocess, and creates a new feature with the
 * attributes equals to ITableDefinition specified
 *
 * @author azabala
 *
 */
public class MergeVisitor implements FeatureVisitor {

	SelectableDataSource recordset;
	ITableDefinition definition;
	FeatureProcessor featureProcessor;

	public MergeVisitor(ITableDefinition schema,
			FeatureProcessor processor){
		this.definition = schema;
		this.featureProcessor = processor;
	}

	public void visit(IGeometry g, int index) throws VisitorException, ProcessVisitorException {
		if(g == null)
			return;
		FieldDescription[] fields =
			definition.getFieldsDesc();
		Value[] values = new Value[fields.length];
		for(int i = 0; i < fields.length; i++){
			String fieldName = fields[i].getFieldName();
			try {
				int fieldIndex = recordset.getFieldIndexByName(fieldName);
				if(fieldIndex == -1)
					values[i] = ValueFactory.createNullValue();
				else{
					//we must to verify data type, not only field name
					int fieldType = fields[i].getFieldType();
					int recordsetType = recordset.getFieldType(fieldIndex);
					if(fieldType != recordsetType){
						values[i] = ValueFactory.createNullValue();
					}else{
						values[i] = recordset.getFieldValue(index, fieldIndex);
					}
					// afalciano: ...but also field length and precision
					int fieldLength = fields[i].getFieldLength();
					int fieldDecimalCount = fields[i].getFieldDecimalCount();
					FieldDescription[] recordsetFieldsDescription = recordset.getFieldsDescription();
					int recordsetLength = recordsetFieldsDescription[fieldIndex].getFieldLength();
					int recordsetDecimalCount = recordsetFieldsDescription[fieldIndex].getFieldDecimalCount();
					if(fieldLength != recordsetLength){
						fields[i].setFieldLength(recordsetLength);
					}
					if(fieldDecimalCount != recordsetDecimalCount){
						fields[i].setFieldDecimalCount(recordsetDecimalCount);
					}
					
				}
			} catch (ReadDriverException e) {
				throw new ProcessVisitorException(recordset.getName(),e,"Error en merge al tratar de leer el atributo de un feature de una de las capas");
			}
		}//for
		IFeature feature = FeatureFactory.createFeature(values, g);
		featureProcessor.processFeature(feature);
	}

	public void stop(FLayer layer) throws VisitorException {
		featureProcessor.finish();
	}

	public boolean start(FLayer layer) throws StartVisitorException {
		if(layer instanceof VectorialData && layer instanceof AlphanumericData){
			try {
				this.recordset = ((AlphanumericData)layer).getRecordset();
				this.featureProcessor.start();
			} catch (ReadDriverException e) {
				return false;
			}
			return true;
		}
		return false;
	}

	public String getProcessDescription() {
		return "Merging many layers";
	}

}

