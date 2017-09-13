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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.cresques.cts.ICoordTrans;

import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.v02.FLabel;
import com.iver.utiles.swing.threads.Cancellable;
import com.vividsolutions.jts.geom.Geometry;


/**
 * DOCUMENT ME!
 *
 * @author Vicente Caballero Navarro
 */
public class FNullGeometry extends AbstractGeometry {
    
    public final static FNullGeometry INSTANCE = new FNullGeometry();
	/**
	 * @see com.iver.cit.gvsig.fmap.core.IGeometry#draw(java.awt.Graphics2D,
	 * 		ViewPort, ISymbol)
	 */
	public void draw(Graphics2D g, ViewPort vp, ISymbol symbol) {
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.core.IGeometry#toJTSGeometry()
	 */
	public Geometry toJTSGeometry() {
		return null;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.core.IGeometry#createLabels(int, boolean)
	 */
	public FLabel[] createLabels(int position, boolean duplicates) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.core.IGeometry#intersects(java.awt.geom.Rectangle2D)
	 */
	public boolean intersects(Rectangle2D r) {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.core.IGeometry#getBounds2D()
	 */
	public Rectangle2D getBounds2D() {
		return null;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.core.IGeometry#getGeometryType()
	 */
	public int getGeometryType() {
		return FShape.NULL;
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.core.IGeometry#draw(java.awt.Graphics2D, com.iver.cit.gvsig.fmap.ViewPort, com.iver.cit.gvsig.fmap.core.v02.FSymbol)
	 */
	public void draw(Graphics2D g, ViewPort vp, ISymbol symbol, Cancellable cancel) {
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.core.IGeometry#cloneGeometry()
	 */
	public IGeometry cloneGeometry() {
		return this;
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.core.IGeometry#reProject(org.cresques.cts.ICoordTrans)
	 */
	public void reProject(ICoordTrans ct) {
	}


	/**
	 * @see com.iver.cit.gvsig.fmap.core.IGeometry#getPathIterator(AffineTransform)
	 */
	public PathIterator getPathIterator(AffineTransform at) {
		//TODO falta implementar.
		return null;
	}

    /* (non-Javadoc)
     * @see com.iver.cit.gvsig.fmap.core.IGeometry#fastIntersects(double, double, double, double)
     */
    public boolean fastIntersects(double x, double y, double w, double h) {
        return false;
    }

    /**
     * @see com.iver.cit.gvsig.fmap.core.IGeometry#toWKB()
     */
    public byte[] toWKB() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.iver.cit.gvsig.fmap.core.IGeometry#drawInts(java.awt.Graphics2D, com.iver.cit.gvsig.fmap.ViewPort, com.iver.cit.gvsig.fmap.core.v02.FSymbol)
     */
    public void drawInts(Graphics2D g, ViewPort vp, ISymbol symbol, Cancellable cancel) {
        // TODO Auto-generated method stub

    }

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.core.IGeometry#getHandlers(int)
	 */
	public Handler[] getHandlers(int type) {
		// TODO Auto-generated method stub
		return null;
	}


	public void transform(AffineTransform at) {

	}

	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return null;
	}

	public boolean contains(double arg0, double arg1) {
		return false;
	}

	public boolean contains(double arg0, double arg1, double arg2, double arg3) {
		return false;
	}

	public boolean intersects(double arg0, double arg1, double arg2, double arg3) {
		return false;
	}

	public Rectangle getBounds() {
		return null;
	}

	public boolean contains(Point2D arg0) {
		return false;
	}

	public boolean contains(Rectangle2D arg0) {
		return false;
	}

	public Shape getInternalShape() {
		// TODO Auto-generated method stub
		return null;
	}

	public void drawInts(Graphics2D graphics2D, ViewPort viewPort, double dpi,
			CartographicSupport cartographicSymbol, Cancellable cancel) {
		// TODO Auto-generated method stub
		throw new Error("Not yet implemented!");

	}
}
