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

import org.geotools.referencefork.referencing.operation.builder.MappedPosition;
import org.gvsig.referencing.MappedPositionContainer;

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
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.drivers.BoundedShapes;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;

/**
 * Memory driver to adapt MappedPositionContainer (which contains MappedPosition)
 * to fmap driver interfaces. It only returns geometries (no fields)
 * 
 * @author Alvaro Zabala
 *
 */
public class VectorErrorMemoryDriver implements VectorialDriver, ObjectDriver,
		BoundedShapes {

	public static final Rectangle2D EMPTY_FULL_EXTENT = new Rectangle2D.Double();

	private DriverAttributes attr = new DriverAttributes();

	/**
	 * Name of the data source of this driver
	 */
	String name;
	
	/**
	 * Container of MappedPosition
	 */
	MappedPositionContainer mappedPositionContainer;

	

	/**
	 * Constructor
	 * 
	 * @param name
	 *            descriptive name of the data source
	 * @param features
	 *            collection of features in memory
	 * @param definition
	 *            definition of the layer of these features
	 */
	public VectorErrorMemoryDriver(String name,
			MappedPositionContainer mappedPositionContainer) {
		this.name = name;
		this.mappedPositionContainer = mappedPositionContainer;
		attr.setLoadedInMemory(true);
//		try {
//			computeFullExtent();
//		} catch (ExpansionFileReadException e) {
//			e.printStackTrace();
//		} catch (ReadDriverException e) {
//			e.printStackTrace();
//		}
	}

	public int getShapeType() {
		return FShape.LINE;
	}

	public String getName() {
		return name;
	}

	public int getShapeCount() throws ReadDriverException {
		return mappedPositionContainer.getCount();
	}

	public Rectangle2D getFullExtent() throws ReadDriverException,
			ExpansionFileReadException {
		return computeFullExtent();
	}

	public IGeometry getShape(int index) throws ReadDriverException {
		if (index < mappedPositionContainer.getCount()){
			MappedPosition mappedPosition = mappedPositionContainer.getMappedPosition(index);
			double[] sourceCoords = mappedPosition.getSource().getCoordinates();
			double[] targetCoords = mappedPosition.getTarget().getCoordinates();
			GeneralPathX gpx = new GeneralPathX();
			gpx.moveTo(sourceCoords[0], sourceCoords[1]);
			gpx.lineTo(targetCoords[0], targetCoords[1]);
			return ShapeFactory.createPolyline2D(gpx);
		
	
		}
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

	public void write(DataWare dataWare) throws WriteDriverException,
			ReadDriverException {
	}

	public void setDataSourceFactory(DataSourceFactory dsf) {
	}

	public Value getFieldValue(long rowIndex, int fieldId)
			throws ReadDriverException {
		if(fieldId == 0)
			return ValueFactory.createValue(rowIndex);
		else
			return ValueFactory.createNullValue();
	}

	public int getFieldCount() throws ReadDriverException {
		return 1;
	}

	public String getFieldName(int fieldId) throws ReadDriverException {
		if(fieldId == 0)
			return "fid";
		else
			return "";
	}

	public long getRowCount() throws ReadDriverException {
		return getShapeCount();
	}

	public int getFieldType(int i) throws ReadDriverException {
		if(i == 0)
			return Types.NUMERIC;
		else
			return -1;
	}

	public int getFieldWidth(int i) throws ReadDriverException {
		return 20;
	}

	public Rectangle2D getShapeBounds(int index) throws ReadDriverException,
			ExpansionFileReadException {
		IGeometry geometry = getShape(index);
		return geometry.getBounds2D();
	}

	public int getShapeType(int index) throws ReadDriverException {
		IGeometry geometry = getShape(index);
		return geometry.getGeometryType();
	}

	private Rectangle2D computeFullExtent() throws ExpansionFileReadException, ReadDriverException {
		 Rectangle2D fullExtent = null;
		 for (int i = 0; i < getRowCount(); i++) {
			 Rectangle2D rAux = getShapeBounds(i);
			 if (fullExtent == null)
				 fullExtent = rAux;
			 else
				 fullExtent.add(rAux);
		 }//for
		 return fullExtent;
	}

	public MappedPositionContainer getMappedPositionContainer() {
		return mappedPositionContainer;
	}

}
