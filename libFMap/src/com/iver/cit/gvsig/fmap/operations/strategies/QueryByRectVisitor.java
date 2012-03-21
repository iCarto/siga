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
package com.iver.cit.gvsig.fmap.operations.strategies;

import java.awt.geom.Rectangle2D;

import org.cresques.cts.ICoordTrans;

import com.iver.cit.gvsig.exceptions.visitors.ProcessVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StartVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;


/**
 * Query by Rectangle Visitor.
 *
 * @author Vicente Caballero Navarro
 */
public class QueryByRectVisitor implements FeatureVisitor {
	private Rectangle2D rect = null;
	private FBitSet bitset = null;
	private ICoordTrans ct = null;

	/**
	 * Inserta el Rect�ngulo para realizar la consulta.
	 *
	 * @param r Rect�ngulo para la consulta.
	 */
	public void setRect(Rectangle2D r) {
		rect = r;
	}

	/**
	 * Devuelve un FBitSet como respuesta a la consulta.
	 *
	 * @return FBitSet con los �ndices de los registros de la consulta.
	 */
	public FBitSet getBitSet() {
		return bitset;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.operations.strategies.FeatureVisitor#visit(com.iver.cit.gvsig.fmap.core.IGeometry,
	 * 		int)
	 */
	public void visit(IGeometry g, int index) throws VisitorException, ProcessVisitorException {
		if (g==null)return;
		// Comentamos esto porque la geometry ya viene reproyectada
		/* if (ct != null) {
			g.reProject(ct);
		} */

		if (g.intersects(rect)) {
			bitset.set(index, true);
		} else {
			bitset.set(index, false);
		}
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.operations.strategies.FeatureVisitor#stop(com.iver.cit.gvsig.fmap.layers.FLayer)
	 */
	public void stop(FLayer layer) throws VisitorException {
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.operations.strategies.FeatureVisitor#start(com.iver.cit.gvsig.fmap.layers.FLayer)
	 */
	public boolean start(FLayer layer) throws StartVisitorException {
		bitset = new FBitSet();
		// ct = layer.getCoordTrans();

		return true;
	}

	public String getProcessDescription() {
		return "Selecting geometries that intersects a rectangle";
	}
}
