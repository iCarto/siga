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

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.geotools.pt.CoordinatePoint;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.DefaultCoordinateOperationFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;


//import org.geotools.pt.MismatchedDimensionException;

/**
 * Transforma coordenadas entre dos sistemas
 * @see org.creques.cts.CoordSys
 * @author "Luis W. Sevilla" <sevilla_lui@gva.es>
 */
class CoordTrans implements ICoordTrans {
    private DefaultCoordinateOperationFactory trFactory = new DefaultCoordinateOperationFactory();
    private CoordinateOperation op = null;
    private CoordSys from = null;
    private CoordSys to = null;

    private ICoordTrans invertedCT = null;

    public CoordTrans(CoordSys from, CoordSys to) {
        this.from = from;
        this.to = to;

        try {
            op = trFactory.createOperation(CRS.decode(from.getAbrev()),
                    CRS.decode(to.getAbrev()));
        } catch (OperationNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAuthorityCodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FactoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        CoordinatePoint pt1 = new CoordinatePoint(ptOrig);
        CoordinatePoint pt2 = new CoordinatePoint(0D, 0D);
        ptDest = null;

        try {
            op.getMathTransform().transform(pt1, pt2);
            ptDest = pt2.toPoint2D();
        } catch (TransformException e) {
            // TODO Bloque catch generado automáticamente
            e.printStackTrace();
        }

        return ptDest;
    }

    public String toString() {
        return op.toString();
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
