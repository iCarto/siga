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
package org.cresques.cts;

import org.cresques.geo.ViewPortData;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/**
 *
 * @author "Luis W. Sevilla" (sevilla_lui@gva.es)
 */
public interface IProjection {

    public IDatum getDatum();

    public Point2D createPoint(double x, double y);

    // TODO Quitar si no son necesarias.
    public String getAbrev();
    
    /**
     * Devuelve getAbrev() mas los parametros de transformacion si los hay
     * ej.: (EPSG:23030:proj@+proj...@...)
     * 
     * @return getAbrev() o getAbrev()+parametros
     */
    public String getFullCode();

    public void drawGrid(Graphics2D g, ViewPortData vp);

    public void setGridColor(Color c);

    public Color getGridColor();

    /**
     * Crea un ICoordTrans para transformar coordenadas
     * desde el IProjection actual al dest.
     * @param dest
     * @return
     */

    public ICoordTrans getCT(IProjection dest);

    public Point2D toGeo(Point2D pt);

    public Point2D fromGeo(Point2D gPt, Point2D mPt);

    public boolean isProjected();

    public double getScale(double minX, double maxX, double width, double dpi);
    public Rectangle2D getExtent(Rectangle2D extent,double scale,double wImage,double hImage,double mapUnits,double distanceUnits,double dpi);
}
