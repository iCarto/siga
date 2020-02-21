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
package com.iver.cit.gvsig.drivers;

import java.awt.geom.Rectangle2D;
import java.sql.Types;

import org.gvsig.topology.ITopologyErrorContainer;
import org.gvsig.topology.Messages;
import org.gvsig.topology.TopologyError;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.driver.exceptions.ReloadDriverException;
import com.hardcode.gdbms.driver.exceptions.WriteDriverException;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.data.driver.ObjectDriver;
import com.hardcode.gdbms.engine.data.edition.DataWare;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.BoundedShapes;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

public class TopologyErrorMemoryDriver implements VectorialDriver, 
													ObjectDriver,
													BoundedShapes {

	public static final Rectangle2D EMPTY_FULL_EXTENT = new Rectangle2D.Double();
	
	public static final String LEGEND_FIELD = Messages.getText("Rule_type");
	
	/**
	 * Number of fields to show
	 * */
	private static final int NUMBER_OF_FIELDS = 7;
	
	private static final int DEFAULT_FIELD_LENGTH = 20;
	
	private DriverAttributes attr = new DriverAttributes();
	
	/**
     * Name of the data source of this driver
     */
	String name;
	
	ITopologyErrorContainer errorContainer;
	
	
	
	/**
	 * Full extent of all features
	 */
	Rectangle2D fullExtent;
	
	
	
	/**
	 * Constructor 
	 * @param name descriptive name of the data source
	 * @param features collection of features in memory
	 * @param definition definition of the layer of these features
	 */
	public TopologyErrorMemoryDriver(String name,
								ITopologyErrorContainer errorContainer){
		this.name = name;
		this.errorContainer = errorContainer;
		attr.setLoadedInMemory(true);
		computeFullExtent();
	}
	

	public int getShapeType() {
		return FShape.MULTI;
	}

	public String getName() {
		return name;
	}


	public int getShapeCount() throws ReadDriverException {
		return errorContainer.getNumberOfErrors();
	}



	public Rectangle2D getFullExtent() throws ReadDriverException, ExpansionFileReadException {
		if(fullExtent == null){
			//collection is empty
			return EMPTY_FULL_EXTENT;
		}
		return fullExtent;
	}


	public IGeometry getShape(int index) throws ReadDriverException {
		if(index <  errorContainer.getNumberOfErrors())
			return  errorContainer.getTopologyError(index).getGeometry();
		else
			return null;
	}


	public void reload() throws ReloadDriverException {
	}
	
	public DriverAttributes getDriverAttributes() {
		return attr;
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


	public Value getFieldValue(long rowIndex, int fieldId) throws ReadDriverException {
		TopologyError feature = errorContainer.getTopologyError((int) rowIndex);
		Value solution = null;
		switch(fieldId){
		case 0:
			solution = ValueFactory.createValue(feature.getViolatedRule().getName());
			break;
		case 1:
			solution = ValueFactory.createValue(feature.getOriginLayer().getName());
			break;
		case 2:
			FLyrVect destinationLyr = feature.getDestinationLayer();
			if(destinationLyr != null)
				solution = ValueFactory.createValue(feature.getDestinationLayer().getName());
			else
				solution = ValueFactory.createNullValue();
			break;
		case 3:
			solution = ValueFactory.createValue(feature.getShapeTypeAsText());
			break;
		case 4://TODO Algunos errores tienen el feature1 y el feature2 a null
			IFeature feature1 = feature.getFeature1();
			if(feature1 != null)
				solution = ValueFactory.createValue(feature1.getID());
			else
				solution = ValueFactory.createNullValue();
			break;
		case 5:
			IFeature feature2 = feature.getFeature2();
			if(feature2 != null)
				solution = ValueFactory.createValue(feature2.getID());
			else
				solution = ValueFactory.createNullValue();
			break;
		case 6:
			solution = ValueFactory.createValue(feature.isException());
			break;
		}
		return solution;
	}


	public int getFieldCount() throws ReadDriverException {
		return NUMBER_OF_FIELDS;
	}


	public String getFieldName(int fieldId) throws ReadDriverException {
		String solution = "";
		switch(fieldId){
		case 0:
			solution = Messages.getText("Rule_type");
			break;
		case 1:
			solution = Messages.getText("Layer_1");
			break;
		case 2:
			solution = Messages.getText("Layer_2");
			break;
		case 3:
			solution = Messages.getText("Shape_Type");
			break;
		case 4:
			solution = Messages.getText("Feature_1");
			break;
		case 5:
			solution = Messages.getText("Feature_2");
			break;
		case 6:
			solution = Messages.getText("Exception");
			break;
		}
		return solution;
	}


	public long getRowCount() throws ReadDriverException {
		return getShapeCount();
	}


	public int getFieldType(int i) throws ReadDriverException {
		int fieldType = -1;
		switch(i){
		case 0:
		case 1:
		case 2:
		case 3:
			fieldType = Types.VARCHAR;
		case 4:
		case 5:
			fieldType = Types.NUMERIC;
		case 6:
			fieldType = Types.BOOLEAN;
			
		}
		return fieldType;
	}


	public int getFieldWidth(int i) throws ReadDriverException {
		return DEFAULT_FIELD_LENGTH;
	}


	public Rectangle2D getShapeBounds(int index) throws ReadDriverException, ExpansionFileReadException {
		IGeometry geometry = getShape(index);
		return geometry.getBounds2D();
	}


	public int getShapeType(int index) throws ReadDriverException {
		IGeometry geometry = getShape(index);
		return geometry.getGeometryType();
	}
	
	private void computeFullExtent() {
		
		for(int i = 0; i < errorContainer.getNumberOfErrors(); i++){
			IFeature feature = errorContainer.getTopologyError(i);
			Rectangle2D rAux = feature.getGeometry().getBounds2D();
			if(fullExtent == null)
				fullExtent = rAux;
			else
				fullExtent.add(rAux);
		}
	}

}
