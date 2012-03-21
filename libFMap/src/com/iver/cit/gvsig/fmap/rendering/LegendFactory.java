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

import java.awt.Color;
import java.util.Random;

import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.utiles.XMLEntity;


/**
 * Clase factoria de las diferentes leyendas.
 *
 * @author Fernando Gonz�lez Cort�s
 */
public class LegendFactory {
	/**
	 * Crea un objeto renderer de s�mbolo �nico con las caracter�sticas que se
	 * pasan como par�metro
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
	 * Crea un objeto VectorialUniqueValueLegend vac�o, dispuesto para cargar
	 * s�mbolos
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
	 * Crea un objeto VectorialIntervalLegend vac�o, dispuesto para cargar
	 * s�mbolos
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
	 * Crea un renderer con la informaci�n contenida en el objeto XMLEntity
	 *
	 * @param xml XMLEntity.
	 *
	 * @return VectorialLegend
	 *
	 * @throws XMLException
	 */
	public static IVectorLegend createFromXML03(XMLEntity xml)
		throws XMLException {
		//TODO Implementar bien
		try {
			IVectorLegend vl = null;
			Class clase = Class.forName(xml.getStringProperty("className"));
			vl = (IVectorLegend) clase.newInstance();
			vl.setXMLEntity03(xml);

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
	 * Crea un renderer con la informaci�n contenida en el objeto XMLEntity
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
			Class clase = Class.forName(xml.getStringProperty("className"));
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
