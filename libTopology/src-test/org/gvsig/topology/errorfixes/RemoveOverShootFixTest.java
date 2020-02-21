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
package org.gvsig.topology.errorfixes;

import junit.framework.TestCase;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.jts.JtsUtil;

import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Test for remove overshoot fix.
 * 
 * @author Alvaro Zabala
 *
 */
public class RemoveOverShootFixTest extends TestCase {
	
	GeometryFactory factory = new GeometryFactory(JtsUtil.GVSIG_PRECISION_MODEL);
	WKTReader wktReader = new WKTReader(factory);
	
	
	public void tearDown() throws Exception{
		super.tearDown();
		factory = null;
		wktReader = null;
		System.gc();
	}
	
	public void testLineString() throws ParseException{
		String wkt = "LINESTRING (260 200, 320 280, 460 300, 540 220, 580 120, 520 80, 380 40, 300 120, 280 320)";
		Geometry geom = wktReader.read(wkt);
		Geometry solution = JtsUtil.removeOverShoot(geom, 0d);
		assertTrue(!solution.equals(geom));
	}
	
	public void testLineString2() throws ParseException{
		String wkt = "LINESTRING (35.47077922077922 47.88961038961039, 31.25 56.98051948051948, 34.98376623376623 69.8051948051948, 42.61363636363636 73.7012987012987, 47.97077922077922 67.20779220779221, 40.503246753246756 56.16883116883117, 31.25 62.175324675324674, 25.89285714285714 68.01948051948052, 25.730519480519476 68.01948051948052, 25.730519480519476 68.01948051948052)";
		Geometry geom = wktReader.read(wkt);
		Geometry solution = JtsUtil.removeOverShoot(geom, 0d);
		
	}
	
	public void testPolygon() throws ParseException{
		
//		Polygon polygon = JtsUtil.GEOMETRY_FACTORY.createPolygon((LinearRing) shellG, new LinearRing[]{(LinearRing) holeG});
		GeneralPathX gpx = new GeneralPathX();
		gpx.moveTo(340, 300);
		gpx.lineTo(200, 180);
		gpx.lineTo(380, 40);
		gpx.lineTo(580, 120);
		gpx.lineTo(340, 300);
		gpx.moveTo(300, 200);
		gpx.lineTo(360, 100);
		gpx.lineTo(460, 120);
		gpx.lineTo(460, 180);
		gpx.lineTo(300, 200);
		gpx.lineTo(260, 180);
		
		FGeometry geometry = (FGeometry) ShapeFactory.createPolygon2D(gpx);
		
		FGeometry newGeometry = (FGeometry) FGeometryUtil.removeOvershoot(geometry, 0d);
		
		assertTrue(FGeometryUtil.isClosed(newGeometry));
				
	}
}
