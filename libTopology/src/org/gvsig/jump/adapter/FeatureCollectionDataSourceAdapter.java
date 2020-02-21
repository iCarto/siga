/* OrbisCAD. The Community cartography editor
 *
 * Copyright (C) 2005, 2006 OrbisCAD development team
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
 *  OrbisCAD development team
 *   elgallego@users.sourceforge.net
 */
package org.gvsig.jump.adapter;

import java.awt.geom.Rectangle2D;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.NewFConverter;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.driver.exceptions.ReloadDriverException;
import com.hardcode.gdbms.driver.exceptions.WriteDriverException;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.data.edition.DataWare;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.FeatureCollectionMemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;


/**
 * Adapter to work with OpenJUMP's FeatureCollection as a gvSIG driver.
 * 
 * It allows to work with all those OpenJUMP's functions which return FeatureCollection
 * as a result in gvSIG.
 * 
 * 
 * @author Alvaro Zabala
 *
 */
public class FeatureCollectionDataSourceAdapter extends FeatureCollectionMemoryDriver{

    
    private FeatureCollection fc;
    private List features;
    private FeatureSchemaLayerDefinitionAdapter schema;
    
	/**
	 * Constructor 
	 * 
	 * @param name descriptive name of the data source
	 * @param fc OpenJUMP's feature collection
	 */
	public FeatureCollectionDataSourceAdapter(String name,
										FeatureCollection fc,
										FeatureSchemaLayerDefinitionAdapter schema){
		
		super(name, new ArrayList<IFeature>(), null);
		this.fc = fc;
		this.features = fc.getFeatures();
		this.schema = schema;
	}
	

	public int getShapeType() {
		return schema.getShapeType();
	}

	public String getName() {
		return super.getName();
	}


	public int getShapeCount() throws ReadDriverException {
		return fc.size();
	}


	public DriverAttributes getDriverAttributes() {
		return super.getDriverAttributes();
	}


	public Rectangle2D getFullExtent() throws ReadDriverException, ExpansionFileReadException {
		return FGeometryUtil.envelopeToRectangle2D(fc.getEnvelope());
	}


	public IGeometry getShape(int index) throws ReadDriverException {
		if(index <  fc.size()){
			Feature feature = (Feature) features.get(index);
			Geometry geometry = feature.getGeometry();
			return NewFConverter.toFMap(geometry);
		}else
			return null;
	}


	public void reload() throws ReloadDriverException {
	}


	public boolean isWritable() {
		return false;
	}


	public int[] getPrimaryKeys() throws ReadDriverException {
		return null;
	}


	public void write(DataWare dataWare) throws WriteDriverException, ReadDriverException {
	}


	public void setDataSourceFactory(DataSourceFactory dsf) {
	}


	public Value getFieldValue(long index, int fieldId) throws ReadDriverException {
		if(index <  fc.size()){
			Feature feature = (Feature) features.get((int) index);
			Object attr = feature.getAttribute(fieldId);
			AttributeType type = fc.getFeatureSchema().getAttributeType(fieldId);
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
		}else
			return null;
		
	}


	

	public int getFieldCount() throws ReadDriverException {
		//la geometria no cuenta
		return fc.getFeatureSchema().getAttributeCount() - 1;
	}


	public String getFieldName(int fieldId) throws ReadDriverException {
		return fc.getFeatureSchema().getAttributeName(fieldId);
	}


	public long getRowCount() throws ReadDriverException {
		return fc.size();
	}


	public int getFieldType(int i) throws ReadDriverException {
		 FeatureSchema fSchema = fc.getFeatureSchema();
		 if (fSchema instanceof FeatureSchemaAdapter) {
           return ((FeatureSchemaAdapter) fSchema).getDs().getRecordset().getFieldType(i);
       } else {
           AttributeType at = fSchema.getAttributeType(i);
           if (at == AttributeType.DATE) {
               return Types.DATE;
           } else if (at == AttributeType.DOUBLE) {
               return Types.DOUBLE;
           } else if (at == AttributeType.INTEGER) {
               return Types.INTEGER;
           } else if (at == AttributeType.STRING) {
               return Types.VARCHAR;
           } 
           return -1;
       }
	}


	public int getFieldWidth(int i) throws ReadDriverException {
		AttributeType attrType = fc.getFeatureSchema().getAttributeType(i);
		FieldDescription desc = new FieldDescription();
		FeatureSchemaLayerDefinitionAdapter.convert(desc, attrType);
		return desc.getFieldLength();
	}


	public Rectangle2D getShapeBounds(int index) throws ReadDriverException, ExpansionFileReadException {
		return getShape(index).getBounds2D();
	}


	public int getShapeType(int index) throws ReadDriverException {
		return getShape(index).getGeometryType();
	}
}
