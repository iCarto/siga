/*
 * Created on 10-abr-2006
 *
 * gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
/* CVS MESSAGES:
*
* $Id: 
* $Log: 
*/
package org.gvsig.topology;

import java.awt.geom.Point2D;
import java.util.List;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.ShapePointExtractor;

import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;

import junit.framework.TestCase;

public class FGeometryUtilTest extends TestCase {
	public void testInsertVertex(){
		GeneralPathX gpx = new GeneralPathX();
		gpx.moveTo(0d, 0d);
		gpx.lineTo(100d, 0d);
		gpx.lineTo(300d, 150d);
		
		IGeometry geometry = ShapeFactory.createPolyline2D(gpx);
		Point2D newVertex = new Point2D.Double(50d, 0.02d);
		IGeometry newGeometry = FGeometryUtil.insertVertex(geometry, newVertex, 0.1);
		List<Point2D[]> pointList = ShapePointExtractor.extractPoints(newGeometry);
		Point2D[] points = pointList.get(0);
		assertTrue(points.length == 4);
	}
}
