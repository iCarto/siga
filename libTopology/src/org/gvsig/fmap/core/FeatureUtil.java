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
package org.gvsig.fmap.core;

import java.io.IOException;
import java.sql.Types;
import java.util.Date;

import org.apache.log4j.Logger;
import org.gvsig.jts.JtsUtil;

import com.hardcode.gdbms.engine.values.BooleanValue;
import com.hardcode.gdbms.engine.values.DateValue;
import com.hardcode.gdbms.engine.values.DoubleValue;
import com.hardcode.gdbms.engine.values.FloatValue;
import com.hardcode.gdbms.engine.values.IntValue;
import com.hardcode.gdbms.engine.values.LongValue;
import com.hardcode.gdbms.engine.values.StringValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.WKBParser2;
import com.iver.utiles.XMLEntity;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

/**
 * Class with utility methods to work with FMap features.
 * 
 * @author Alvaro Zabala
 * 
 */
public class FeatureUtil {
	
	private static Logger logger = Logger.getLogger(FeatureUtil.class);
	
	private static WKBParser2 wkbParser = new WKBParser2();
	
	public static XMLEntity getValueAsXMLEntity(Object value){
		XMLEntity solution = new XMLEntity();
		solution.putProperty("value", value);
		return solution;
	}
	
	public static Object getValueFromXMLEntity(XMLEntity xml){
		if(xml.contains("value"))
			return xml.getObjectProperty("value");
		else
			return null;
	}
	
	
	public static Value getValue(Object object, int fieldType) {
		if(object == null)
			return ValueFactory.createNullValue();
		Value solution = null;
		switch (fieldType) {
		case Types.VARCHAR:
			String str = (String) object;
			solution = ValueFactory.createValue(str);
			break;
		case Types.FLOAT:
			float fval = ((Float) object).floatValue();
			solution = ValueFactory.createValue(fval);
			break;
		case Types.DOUBLE:
			double dval = ((Double) object).doubleValue();
			solution = ValueFactory.createValue(dval);
			break;
		case Types.INTEGER:
			int ival = ((Integer) object).intValue();
			solution = ValueFactory.createValue(ival);
			break;
		case Types.BIGINT:
			long lval = ((Long) object).longValue();
			solution = ValueFactory.createValue(lval);
			break;
		case Types.BIT:
			boolean bval = ((Boolean) object).booleanValue();
			solution = ValueFactory.createValue(bval);
			break;
		case Types.DATE:
			Date dtval = (Date) object;
			solution = ValueFactory.createValue(dtval);
			break;
		default:
			solution = ValueFactory.createNullValue();
		}
		return solution;
	}

	public static Object getJavaTypeFromValue(Value value, int fieldType) {
		Object solution = null;
		switch (fieldType) {
		case Types.VARCHAR:
			StringValue str = (StringValue) value;
			solution = str.getValue();
			break;
		case Types.FLOAT:
			FloatValue fval = (FloatValue) value;
			solution = new Float(fval.floatValue());
			break;
		case Types.DOUBLE:
			DoubleValue dval = (DoubleValue) value;
			solution = new Double(dval.doubleValue());
			break;
		case Types.INTEGER:
			IntValue intval = (IntValue) value;
			solution = new Integer(intval.intValue());
			break;
		case Types.BIGINT:
			LongValue lval = (LongValue) value;
			solution = new Long(lval.longValue());
			break;
		case Types.BIT:
			BooleanValue bval = (BooleanValue) value;
			solution = new Boolean(bval.getValue());
			break;
		case Types.DATE:
			DateValue dtval = (DateValue) value;
			solution = dtval.getValue();
			break;
		}
		return solution;
	}
	
	private static IGeometry geometryFromXmlByteArray(String xmlByteArray){
		String[] bytes = xmlByteArray.split(",");
		byte[] wkbErrorGeometry = new byte[bytes.length];
		for(int i = 0; i < bytes.length; i++){
			wkbErrorGeometry[i] = new Byte(bytes[i].trim()).byteValue();
		}
		return wkbParser.parse(wkbErrorGeometry);
	}
	
	public static void setXMLEntity(IFeature feature, XMLEntity xml){
		if(xml.contains("fid")){
			feature.setID(xml.getStringProperty("fid"));
		}
		
		if(xml.contains("geometry")){
			String xmlByteArray = (String) xml.getObjectProperty("geometry");
			feature.setGeometry( geometryFromXmlByteArray(xmlByteArray) );
		}
		
		if(xml.contains("numberOfAttributes")){
			int numberOfAttributes = xml.getIntProperty("numberOfAttributes");
			if(numberOfAttributes > 0){
				Value[] values = new Value[numberOfAttributes];
				for(int i = 0; i < numberOfAttributes; i++){
					int type = xml.getIntProperty("sqlType"+i);
					Object obj = xml.getObjectProperty("value"+i);
					Value value = getValue(obj, type);
					values[i] = value;
				}//for
				feature.setAttributes(values);
			}//if
		}//if numberOfAttrs
	}
	
	public static IFeature getFeatureFromXmlEntity(XMLEntity xml){
		DefaultFeature solution = null;
		String fid = null;
		IGeometry geometry = null;
		Value[] values = null;
		
		if(xml.contains("fid")){
			fid = xml.getStringProperty("fid");
		}
		
		if(xml.contains("geometry")){
			String xmlByteArray = (String) xml.getObjectProperty("geometry");
			geometry = geometryFromXmlByteArray(xmlByteArray);
		}
		
		if(xml.contains("numberOfAttributes")){
			int numberOfAttributes = xml.getIntProperty("numberOfAttributes");
			if(numberOfAttributes > 0){
				values = new Value[numberOfAttributes];
				for(int i = 0; i < numberOfAttributes; i++){
					int type = xml.getIntProperty("sqlType"+i);
					Object obj = xml.getObjectProperty("value"+i);
					Value value = getValue(obj, type);
					values[i] = value;
				}//for
			}//if
		}
		
		solution = new DefaultFeature(geometry, values, fid);
		return solution;
	}
	
	
	public static XMLEntity getAsXmlEntity(IFeature feature){
		XMLEntity solution = new XMLEntity();
		String fid = feature.getID();
		solution.putProperty("fid", fid);
		
		IGeometry errorGeometry = feature.getGeometry();
		try {
			byte[] wkbErrorGeometry = errorGeometry.toWKB();
			solution.putProperty("geometry", wkbErrorGeometry);
		} catch (IOException e) {
			logger.error("Error trying to serialize feature to xml ");
			return null;//FIXME Revisar si puede haber problemas devolviendo NULL
		}
		
		Value[] attributes = feature.getAttributes();
		if(attributes != null){
			solution.putProperty("numberOfAttributes", attributes.length);
			for(int i = 0; i < attributes.length; i++){
				Value value = attributes[i];
				int type = value.getSQLType();
				Object objectValue = FeatureUtil.getJavaTypeFromValue(value, type);
				solution.putProperty("sqlType"+i, type);
				solution.putProperty("value"+i, objectValue);
				
			}
		}
		return solution;
	}

	public static IFeature removeOverlappingArea(IFeature featureToEdit, 
											 Geometry originalGeo, 
											 Geometry errorGeo){
		Geometry[] first = null;
		if(originalGeo instanceof GeometryCollection){
			first = JtsUtil.extractGeometries((GeometryCollection) originalGeo);
		}else
		{
			first = new Geometry[]{originalGeo};
		}
		
		Geometry[] second = null;
		if(errorGeo instanceof GeometryCollection){
			second = JtsUtil.extractGeometries((GeometryCollection) errorGeo);
		}else
		{
			second = new Geometry[]{errorGeo};
		}
		
		for (int i = 0; i < first.length; i++) {
			Geometry geom = first[i];
			Geometry partialSolution = null;
			for (int j = 0; j < second.length; j++) {
				Geometry aux = EnhancedPrecisionOp.difference(geom, second[j]);
				if(partialSolution == null)
					partialSolution = aux;
				else
					partialSolution = EnhancedPrecisionOp.union(partialSolution, aux);
			}//for
			first[i] = partialSolution;
		}//for i
		GeometryCollection geomCol = JtsUtil.GEOMETRY_FACTORY.createGeometryCollection(first);
		IGeometry newFGeo = NewFConverter.toFMap(geomCol);
		featureToEdit.setGeometry(newFGeo);
		return featureToEdit;
	}
}
