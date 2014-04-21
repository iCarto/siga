/*
 * Created on 02/04/2007
 *
 * gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
/* CVS MESSAGES:
*
* $Id$
* $Log$
* Revision 1.1  2007-04-04 17:48:53  azabala
* first version in cvs after libDwg<->libFMap disacopling
*
*
*/
package com.iver.cit.gvsig.drivers.dwg.fmapconverters;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.iver.cit.gvsig.fmap.core.FPolyline2D;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.jdwglib.dwg.DwgObject;
import com.iver.cit.jdwglib.dwg.objects.DwgEllipse;
import com.iver.cit.jdwglib.util.GisModelCurveCalculator;

public class DwgEllipse2FMapConverter implements IDwg2FMap {

	/* (non-Javadoc)
	 * @see com.iver.cit.jdwglib.dwg.IDwg2FMap#toFMapGeometry(boolean)
	 */
	public IGeometry toFMapGeometry(DwgObject entity, boolean is3DFile) {
		DwgEllipse ellipse = (DwgEllipse)entity;
		FPolyline2D arcc;
		double[] c = ellipse.getCenter();
		Point2D center = new Point2D.Double(c[0], c[1]);
		double[] majorAxisVector = ellipse.getMajorAxisVector();
		Point2D mav = new Point2D.Double(majorAxisVector[0],
				majorAxisVector[1]);
		double axisRatio = ellipse.getAxisRatio();
		double initAngle = Math.toDegrees(ellipse.getInitAngle());
		double endAngle = Math.toDegrees(ellipse.getEndAngle());
		List arc = GisModelCurveCalculator
				.calculateGisModelEllipse(center, mav, axisRatio,
						initAngle, endAngle);
		if (is3DFile) {
			List arc3D = new ArrayList();
			for (int j = 0; j < arc.size(); j++) {
				double[] pt = (double[]) arc.get(j);
				double[] newPt = new double[]{ pt[0], pt[1], c[2]} ;
				arc3D.add(newPt);
			}
			arcc = FMapUtil.points3DToFPolyline3D(arc3D);
		} else {
			arcc = FMapUtil.points2DToFPolyline2D(arc);
		}
		return ShapeFactory.createGeometry(arcc);
	}
	
	/* (non-Javadoc)
	 * @see com.iver.cit.jdwglib.dwg.IDwg2FMap#toFMapString(boolean)
	 */
	public String toFMapString(boolean is3DFile) {
		if(is3DFile)
			return "FPolyline3D";
		else
			return "FPolyline2D";
	}

}

