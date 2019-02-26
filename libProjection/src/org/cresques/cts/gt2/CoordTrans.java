/*
 * Cresques Mapping Suite. Graphic Library for constructing mapping applications.
 *
 * Copyright (C) 2004-5.
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
 * cresques@gmail.com
 */
package org.cresques.cts.gt2;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

/**
 * Transforma coordenadas entre dos sistemas
 * @see org.creques.cts.CoordSys
 * @author "Luis W. Sevilla" <sevilla_lui@gva.es>
 */
class CoordTrans implements ICoordTrans {
    private CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
    private CRSFactory csFactory = new CRSFactory();
    private CoordinateTransform trans = null;
    private CoordSys from = null;
    private CoordSys to = null;

    private static final Map<String, Map<String, CoordinateTransform>> TRANS_CACHE = new HashMap<String, Map<String, CoordinateTransform>>();

    private ICoordTrans invertedCT = null;

    public CoordTrans(CoordSys from, CoordSys to) {
        this.from = from;
        this.to = to;

        if (!TRANS_CACHE.containsKey(from.getFullCode())) {
            TRANS_CACHE.put(from.getFullCode(),
                    new HashMap<String, CoordinateTransform>());
        }
        if (!TRANS_CACHE.get(from.getFullCode()).containsKey(to.getFullCode())) {
            TRANS_CACHE.get(from.getFullCode()).put(
                    to.getFullCode(),
                    ctFactory.createTransform(
                            csFactory.createFromName(from.getFullCode()),
                            csFactory.createFromName(to.getFullCode())));
        }

        this.trans = TRANS_CACHE.get(from.getFullCode()).get(to.getFullCode());
    }

    public IProjection getPOrig() {
        return from;
    }

    public IProjection getPDest() {
        return to;
    }

    public ICoordTrans getInverted() {
        if (invertedCT == null)
            invertedCT = new CoordTrans(to, from);
        return invertedCT;
    }

    public Point2D convert(Point2D ptOrig, Point2D ptDest) {
        ProjCoordinate p = new ProjCoordinate();
        ProjCoordinate p2 = new ProjCoordinate();
        p.x = ptOrig.getX();
        p.y = ptOrig.getY();
        trans.transform(p, p2);
        return new Point2D.Double(p2.x, p2.y);
    }

    public String toString() {
        return trans.toString();
    }

    /* (non-Javadoc)
     * @see org.cresques.cts.ICoordTrans#convert(java.awt.geom.Rectangle2D)
     */
    public Rectangle2D convert(Rectangle2D rect) {
        Point2D pt1 = new Point2D.Double(rect.getMinX(), rect.getMinY());
        Point2D pt2 = new Point2D.Double(rect.getMaxX(), rect.getMaxY());
        pt1 = convert(pt1, null);
        pt2 = convert(pt2, null);
        rect = new Rectangle2D.Double();
        rect.setFrameFromDiagonal(pt1, pt2);

        return rect;
    }
}
