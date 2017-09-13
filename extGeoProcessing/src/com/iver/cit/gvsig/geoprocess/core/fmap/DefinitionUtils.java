/*
 * Created on 07-feb-2006
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
* Revision 1.2  2006/06/20 18:19:43  azabala
* refactorización para que todos los nuevos geoprocesos cuelguen del paquete impl
*
* Revision 1.1  2006/05/24 21:12:16  azabala
* primera version en cvs despues de refactoring orientado a crear un framework extensible de geoprocessing
*
* Revision 1.6  2006/05/08 15:38:31  azabala
* converted private constant in public
*
* Revision 1.5  2006/03/26 20:03:31  azabala
* *** empty log message ***
*
* Revision 1.4  2006/03/14 18:32:46  fjp
* Cambio con LayerDefinition para que sea compatible con la definición de tablas también.
*
* Revision 1.3  2006/02/19 20:56:07  azabala
* *** empty log message ***
*
* Revision 1.2  2006/02/17 16:34:00  azabala
* *** empty log message ***
*
* Revision 1.1  2006/02/09 15:59:48  azabala
* First version in CVS
*
*
*/
package com.iver.cit.gvsig.geoprocess.core.fmap;

import java.util.ArrayList;
import java.util.List;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

/**
 * This class has utility methods to work with LayerDefinitions
 * @author azabala
 *
 * TODO By now only works with SHPLayerDefinition. Redesign to work
 * with any layer definition.
 */
public class DefinitionUtils {
	public static int NUM_DECIMALS = 5;
	/**
	 * It builds a LayerDefinition from a LyrVect
	 * @param datasource
	 * @param shapeType
	 * @return
	 * @throws ReadDriverException
	 * @throws DriverException
	 * @throws com.iver.cit.gvsig.fmap.DriverException
	 */
	public static SHPLayerDefinition createLayerDefinition(FLyrVect layer) throws ReadDriverException{
		SHPLayerDefinition solution = new SHPLayerDefinition();
		solution.setName(layer.getName());
		solution.setShapeType(layer.getShapeType());
		SelectableDataSource datasource = layer.getRecordset();
		FieldDescription[] fieldsDescription = datasource.getFieldsDescription();
		solution.setFieldsDesc(fieldsDescription);
		return solution;
	}



	public static int getDataTypeLength(int dataType){
		switch(dataType){
		case XTypes.NUMERIC:
		case XTypes.DOUBLE:
		case XTypes.REAL:
		case XTypes.FLOAT:
		case XTypes.BIGINT:
		case XTypes.INTEGER:
		case XTypes.DECIMAL:
			return 20;
		case XTypes.CHAR:
		case XTypes.VARCHAR:
		case XTypes.LONGVARCHAR:
			return 254;
		case XTypes.DATE:
			return 8;
		case XTypes.BOOLEAN:
		case XTypes.BIT:
			return 1;
		}
		return 0;
	}

	/**
	 * Utility class to overlay geoprocess (that merges
	 * layer definitions of many features)
	 * @param firstLayer
	 * @param secondLayer
	 * @return
	 * @throws ReadDriverException
	 * @throws DriverException
	 * @throws com.iver.cit.gvsig.fmap.DriverException
	 */
	public static SHPLayerDefinition mergeLayerDefinitions(FLyrVect firstLayer,
			FLyrVect secondLayer) throws ReadDriverException{

		SHPLayerDefinition solution = new SHPLayerDefinition();
		solution.setName(firstLayer.getName() + "-" + secondLayer.getName());
		solution.setShapeType(firstLayer.getShapeType());
		SelectableDataSource firstDatasource = firstLayer.getRecordset();
		SelectableDataSource secondDatasource = secondLayer.getRecordset();
		FieldDescription[] fields = 
			new FieldDescription[firstDatasource.getFieldCount() + secondDatasource.getFieldCount()];
		
		int i = 0;
		for (FieldDescription fieldDesc:firstDatasource.getFieldsDescription()) {
		    fields[i++] = fieldDesc;
		}
		for (FieldDescription fieldDesc:secondDatasource.getFieldsDescription()) {
		    fields[i++] = fieldDesc;
		}
		
		solution.setFieldsDesc(fields);
		return solution;
	}
	/**
	 * Tells if a FieldDescription is numeric
	 * @param fieldDesc
	 * @return
	 */
	public static boolean isNumeric(FieldDescription fieldDesc){
		return XTypes.isNumeric(fieldDesc.getFieldType());
	}
	/**
	 * Returns all numeric FieldDescriptions of a LayerDefinition
	 * @param layerDef
	 * @return
	 */
	public static List getNumerics(ITableDefinition layerDef){
		ArrayList solution = new ArrayList();
		FieldDescription[] fields = layerDef.getFieldsDesc();
		for(int i = 0; i < fields.length; i++){
			//java access to arrays is a consumption operation
			FieldDescription field = fields[i];
			if(DefinitionUtils.isNumeric(field)){
				solution.add(field);
			}
		}
		return solution;
	}
}

