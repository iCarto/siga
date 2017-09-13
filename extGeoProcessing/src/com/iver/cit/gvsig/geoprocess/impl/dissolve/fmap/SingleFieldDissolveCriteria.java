/*
 * Created on 12-may-2006
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
* Revision 1.3  2007-03-06 16:47:58  caballero
* Exceptions
*
* Revision 1.2  2006/08/11 16:27:46  azabala
* *** empty log message ***
*
* Revision 1.1  2006/06/20 18:20:45  azabala
* first version in cvs
*
* Revision 1.1  2006/05/24 21:11:14  azabala
* primera version en cvs despues de refactoring orientado a crear un framework extensible de geoprocessing
*
*
*/
package com.iver.cit.gvsig.geoprocess.impl.dissolve.fmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.geoprocess.core.fmap.DefinitionUtils;
import com.iver.cit.gvsig.geoprocess.core.fmap.SummarizationFunction;
import com.iver.cit.gvsig.geoprocess.core.fmap.XTypes;
/**
 * Decides if dissolve two features based in an alphanumeric criteria:
 * two features will be dissolved if they have the same value for
 * a given (and single) field.
 *
 *
 * @author azabala
 *
 */
public class SingleFieldDissolveCriteria implements IDissolveCriteria{
	/**
	 * Name of the field whose values we are going to compare to
	 * decide if dissolve two features
	 */
	protected String dissolveField;

	protected FLyrVect layer;
	/**
	 * Dissolve field value for the 'seed' feature
	 */
	protected Value cachedDissolveValue;

	/**
	 * Builds features resulting of dissolving with
	 * this IDissolveCriteria
	 */
	protected SingleFieldFeatureBuilder builder;

	private int dissolveFieldIndex;


	public SingleFieldDissolveCriteria(String dissolveField,
			FLyrVect layer) throws DriverException{
		this.layer = layer;
		this.dissolveField = dissolveField;
		try {
		    dissolveFieldIndex = layer.getRecordset().getFieldIndexByName(dissolveField);
		} catch (ReadDriverException e) {
		    e.printStackTrace();
		    throw new DriverException(e);
		}
		builder = new SingleFieldFeatureBuilder();

	}

	public IDissolvedFeatureBuilder getFeatureBuilder() {
		return builder;
	}

	class SingleFieldFeatureBuilder implements IDissolvedFeatureBuilder{

		public IFeature createFeature(IGeometry g, int index, int fid) {
			Value[] values = new Value[2];
			fetchDissolveValue(index);
			values[0] = ValueFactory.createValue(fid);
			values[1] = cachedDissolveValue;
			return new DefaultFeature(g, values);
		}

		public IFeature createFeature(IGeometry newGeometry,
				List sumarizedValues,
				int newFid,
				int index) {
			int numNumericFields = sumarizedValues.size();
			Value[] values = new Value[numNumericFields + 2];
			fetchDissolveValue(index);
			values[0] = ValueFactory.createValue(newFid);
			int idx = 1;
			Iterator valIt = sumarizedValues.iterator();
			while(valIt.hasNext()){
				Value val = (Value) valIt.next();
				values[idx] = val;
				idx++;
			}
			values[idx] = cachedDissolveValue;
			return new DefaultFeature(newGeometry, values);
		}

	}

	private void fetchDissolveValue(int index){
		if(cachedDissolveValue == null){
			try {

				cachedDissolveValue = layer.getRecordset().
								getFieldValue(index,
										dissolveFieldIndex);
			} catch (ReadDriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public boolean verifyIfDissolve(int featureIndex1, int featureIndex2) {
		try {
			fetchDissolveValue(featureIndex1);
			Value value2 = layer.getRecordset().
				getFieldValue(featureIndex2, dissolveFieldIndex);
			return value2.doEquals(cachedDissolveValue);
		} catch (ReadDriverException e) {
			//Ver que hacer con la excepcion
			e.printStackTrace();
			return false;
		}

	}

	public void clear() {
		cachedDissolveValue = null;
	}

	public ILayerDefinition createLayerDefinition(Map numFields_SumFunc) {
		SHPLayerDefinition resultLayerDefinition = new SHPLayerDefinition();
		resultLayerDefinition.setShapeType(XTypes.POLYGON);
		ArrayList<FieldDescription> fields = new ArrayList<FieldDescription>();
		//first of all: FID
		FieldDescription fidFd = new FieldDescription();
		fidFd.setFieldLength(10);
		fidFd.setFieldName("fid");
		fidFd.setFieldType(XTypes.INTEGER);
		fidFd.setFieldDecimalCount(0);
		fields.add(fidFd);

		if(numFields_SumFunc != null){
			//sumarization of numeric attributes
			Iterator fieldsIt = numFields_SumFunc.keySet().iterator();
			while(fieldsIt.hasNext()){
				String field = (String) fieldsIt.next();
				SummarizationFunction[] functions =
					(SummarizationFunction[]) numFields_SumFunc.get(field);
				for(int i = 0; i < functions.length; i++){
					FieldDescription description =
						new FieldDescription();
					description.setFieldLength(10);
					description.setFieldDecimalCount(4);
					//to avoid truncation of field names (f.example shp)
					//we only catch five first letters
					String shortName = null;
					if(field.length() > 6)
						shortName = field.substring(0,5);
					else
						shortName = field;
					description.setFieldName(
							shortName + "_" + functions[i].toString());
					description.setFieldType(XTypes.DOUBLE);
					fields.add(description);
				}//for
			}//while
		}//if

	 try {
	     FieldDescription description = layer.getRecordset().getFieldsDescription()[dissolveFieldIndex];
	     fields.add(description);
	  } catch (ReadDriverException e) {
	      e.printStackTrace();
	  }

		FieldDescription[] fieldsDesc = null;
		if(fields.size() == 0){
			fieldsDesc = new FieldDescription[0];
		}else{
			fieldsDesc = new FieldDescription[fields.size()];
			fields.toArray(fieldsDesc);
		}
		resultLayerDefinition.setFieldsDesc(fieldsDesc);
		return resultLayerDefinition;

	}










}

