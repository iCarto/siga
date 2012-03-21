/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
 *   Av. Blasco Ib��ez, 50
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
package com.iver.cit.gvsig.fmap.rendering;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.instruction.FieldNotFoundException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.utiles.XMLEntity;


/**
 * Interface of a vectorial legend.
 *
 * @author Vicente Caballero Navarro
 */
public interface IVectorLegend extends ILegend {
	/**
	 * Inserts the DataSource.
	 *
	 * @param ds DataSource.
	 *
	 * @throws FieldNotFoundException when the field is not found.
	 * @throws DriverException When the driver fails.
	 */
	void setDataSource(DataSource ds)
		throws FieldNotFoundException, ReadDriverException;

	/**
	 * Returns the symbol to be used to represent the feature in the i-th
	 * record in the DataSource
	 * @param i, the record index
	 *
	 * @return ISymbol.
	 *
	 * @throws DriverException
	 */
	ISymbol getSymbol(int i) throws ReadDriverException;
	
	/**
     * Returns a symbol starting from an IFeature.
	 * 
	 * TAKE CARE!! When we are using a feature iterator as a database
	 * the only field that will be filled is the fieldID.
	 * The rest of fields will be null to reduce the time of creation
	 *
	 * @param feat IFeature.
	 *
	 * @return S�mbolo.
	 */
    ISymbol getSymbolByFeature(IFeature feat);

	/**
	 * Returns the type of the shape.
	 *
	 * @return Returns the type of the shapes that the legend is ready to use.
	 * 
	 */
	int getShapeType();

	/**
	 * Defines the type of the shape.
	 *
	 * @param shapeType type of the shape.
	 */
	void setShapeType(int shapeType);

	/**
	 * Establishes the default symbol of a legend. In a SingleSymbolLegend the symbol
	 * is established by calling this method.
	 *
	 * @param s default symbol.
	 * @throws IllegalArgumentException, if the symbol isn't suitable for the
	 * layer's data type.
	 */
	void setDefaultSymbol(ISymbol s) throws IllegalArgumentException;

	/**
	 * Returns the XMLEntity.
	 *
	 * @return XMLEntity.
	 */
	XMLEntity getXMLEntity();

	/**
	 * Inserts the XMLEntity.
	 *
	 * @param xml XMLEntity.
	 */
	void setXMLEntity(XMLEntity xml);

    /**
     * Inserts the XMLEntity.
     *
     * @param xml XMLEntity.
     */
    void setXMLEntity03(XMLEntity xml);

    /**
     * Returns true or false depending on if the rest of values are used.
	 * 
	 * @return  True if the rest of values are used.
	 */
    public boolean isUseDefaultSymbol();

    void useDefaultSymbol(boolean b);

    public ZSort getZSort();

	public void setZSort(ZSort zSort);
	
	public boolean isSuitableForShapeType(int shapeType);
}
