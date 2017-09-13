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
package com.iver.cit.gvsig.fmap.core;

/**
 * Punto 3D.
 *
 * @author Vicente Caballero Navarro
 */
public class FPoint3D extends FPoint2D implements FShape3D {
	private static final String NAME = "POINT";
	
	double z;

	/**
	 * Crea un nuevo FPoint3D.
	 *
	 * @param x Coordenada x.
	 * @param y Coordenada y.
	 * @param z Coordenada z.
	 */
	public FPoint3D(double x, double y, double z) {
		super(x, y);
		this.z = z;
	}

	/**
	 * Devuelve un Array con los valores de todas las Zs, en este caso con un
	 * único vaor de z.
	 *
	 * @return Array de Zs.
	 */
	public double[] getZs() {
		return new double[] { z };
	}
	
	public double getZ() {
		return z;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.core.FShape#getShapeType()
	 */
	public int getShapeType() {
		return FShape.POINT | FShape.Z;
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.core.FShape#cloneFShape()
	 */
	public FShape cloneFShape() {
		return new FPoint3D(p.getX(), p.getY(), z);
	}

	public String toText() {
		StringBuffer str = new StringBuffer();
		str.append(NAME);
		str.append(" (");
		str.append(getX() + " " + getY() + " " + z);
		str.append(")");
		return str.toString();
	}
}
