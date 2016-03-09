/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
package com.iver.cit.gvsig.fmap.rendering;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.utiles.XMLEntity;


/**
 * Clase factoria de las diferentes leyendas.
 *
 * @author Fernando González Cortés
 */
public class LegendFactory {

    	private static Map<String, Class<? extends IVectorLegend>> customLegends = new HashMap<String, Class<? extends IVectorLegend>>();

    	public static void registerCustomLegend(String classname, Class<? extends IVectorLegend> clasz) {
    	    customLegends.put(classname, clasz);
    	}

	/**
	 * Crea un objeto renderer de símbolo único con las características que se
	 * pasan como parámetro
	 *
	 * @param shapeType Tipo de shape.
	 *
	 * @return VectorialLegend.
	 */
	public static IVectorLegend createSingleSymbolLegend(int shapeType) {
		Random rand = new Random();

		int numreg = rand.nextInt(255/2);
		double div = (1-rand.nextDouble()*0.66)*0.9;
		Color randomColor = new Color(
				((int) (255*div + (numreg * rand.nextDouble()))) % 255,
				((int) (255*div + (numreg * rand.nextDouble()))) % 255,
				((int) (255*div + (numreg * rand.nextDouble()))) % 255);

		if(!SymbologyFactory.DefaultAleatoryFillColor)
			randomColor = SymbologyFactory.DefaultFillSymbolColor;

		return new SingleSymbolLegend(
				SymbologyFactory.createDefaultSymbolByShapeType(shapeType, randomColor));

	}

	/**
	 * Crea un objeto VectorialUniqueValueLegend vacío, dispuesto para cargar
	 * símbolos
	 *
	 * @param shapeType Tipo de shape.
	 *
	 * @return VectorialUniqueValueLegend.
	 */
	public static VectorialUniqueValueLegend createVectorialUniqueValueLegend(
		int shapeType) {
		return new VectorialUniqueValueLegend(shapeType);
	}

	/**
	 * Crea un objeto VectorialIntervalLegend vacío, dispuesto para cargar
	 * símbolos
	 *
	 * @param shapeType tipo de shape.
	 *
	 * @return VectorialIntervalLegend
	 */
	public static VectorialIntervalLegend createVectorialIntervalLegend(
		int shapeType) {
		return new VectorialIntervalLegend(shapeType);
	}

	/**
	 * Crea un renderer con la información contenida en el objeto XMLEntity
	 *
	 * @param xml XMLEntity.
	 *
	 * @return VectorialLegend
	 *
	 * @throws XMLException
	 */
	public static IVectorLegend createFromXML(XMLEntity xml)
		throws XMLException {
		//TODO Implementar bien
		try {
			IVectorLegend vl = null;
			final String className = xml.getStringProperty("className");
			Class clase = customLegends.get(className);
			if (clase == null) {
			   clase = Class.forName(className);
			}
			vl = (IVectorLegend) clase.newInstance();
			vl.setXMLEntity(xml);

			return vl;
		} catch (ClassNotFoundException e) {
			throw new XMLException(e);
		} catch (InstantiationException e) {
			throw new XMLException(e);
		} catch (IllegalAccessException e) {
			throw new XMLException(e);
		}
	}

	/**
	 * Clona la leyenda.
	 *
	 * @param l VectorialLegend a clonar.
	 *
	 * @return VectorialLegend cloando.
	 *
	 * @throws XMLException
	 * @throws DriverException
	 */
	public static IVectorLegend cloneLegend(IVectorLegend l)
		throws XMLException, DriverException {
		return createFromXML(l.getXMLEntity());
	}
}
