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
package com.iver.cit.gvsig.fmap.drivers;

import java.awt.geom.Rectangle2D;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;


/**
 * Interfaz a implementar por los drivers que puedan satisfacer de manera
 * r�pida una llamada a getShapeBounds. Esto har� que el procesado de la capa
 * sea un poco m�s r�pido
 */
public interface BoundedShapes {
	/**
	 * Obtiene el Rect�ngulo de la geometr�a i�sima
	 *
	 * @param index �ndice
	 *
	 * @return Rect�ngulo.
	 * @throws ReadDriverException TODO
	 * @throws ExpansionFileReadException
	 */
	Rectangle2D getShapeBounds(int index) throws ReadDriverException, ExpansionFileReadException;

	/**
	 * Devuelve el tipo de la geometr�a. Con .shp est� claro. Con PostGIS,
	 * existe una funci�n:  GeometryType(geometry) Returns the type of the
	 * geometry as a string. Eg: 'LINESTRING', 'POLYGON', 'MULTIPOINT', etc.
	 * OGC SPEC s2.1.1.1 - Returns the name of the instantiable subtype of
	 * Geometry of which this Geometry instance is a member. The name of the
	 * instantiable subtype of Geometry is returned as a string. NOTA: CREO
	 * QUE ESTO NO TIENE SENTIDO CON BASES DE DATOS. LA ESTRATEGIA CON BASES
	 * DE DATOS DEBE SER PEDIR LAS GEOMETRIES CONTENIDAS EN UN RECTANGULO, Y
	 * PREGUNTARLE A CADA GEOMETRY SU BOUNDINGBOX (Y GUARDARLO CON UN
	 * SHAPEINFO). TENGO LA SENSACI�N QUE ESTO LO VAMOS A USAR SOLO CON SHAPES
	 *
	 * @param index
	 *
	 * @return
	 * @throws ReadDriverException
	 */
	int getShapeType(int index) throws ReadDriverException;
}
