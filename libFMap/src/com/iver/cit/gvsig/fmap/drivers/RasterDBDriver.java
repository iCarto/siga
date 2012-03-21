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

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.iver.cit.gvsig.fmap.operations.selection.Record;


/**
 * Clase Driver de raster para acceso a bases de datos.
 *
 * @author Vicente Caballero Navarro
 */
public class RasterDBDriver {
	/**
	 * @see com.iver.cit.gvsig.fmap.layers.RasterAdapter#renderTo(java.awt.Graphics2D,
	 * 		java.awt.geom.Rectangle2D)
	 */
	public void renderTo(Graphics2D g, Rectangle2D rectVisible)
		throws DriverIOException {
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.layers.RasterAdapter#queryByPoint(Point2D,
	 * 		double)
	 */
	public Record queryByPoint(Point2D p, double tolerancia)
		throws DriverIOException {
		return null;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.layers.RasterAdapter#selectByPoint()
	 */
	public void selectByPoint() throws DriverIOException {
	}
}
