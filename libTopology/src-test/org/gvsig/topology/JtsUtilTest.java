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

import org.gvsig.jts.JtsUtil;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import junit.framework.TestCase;

public class JtsUtilTest extends TestCase {
	
	private PrecisionModel pm;
	private GeometryFactory factory;
	private WKTReader wktReader;

	protected void setUp() throws Exception {
		super.setUp();
		pm = new PrecisionModel(10000);
		factory = new GeometryFactory(pm);
		wktReader = new WKTReader(factory);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		pm = null;
		factory = null;
		wktReader = null;
	}
	
	public void testCreatePolygon() throws ParseException{
		String wktPoly = "POLYGON ((460 340, 160 240, 480 20, 860 160, 720 360, 560 380, 460 340),"+ 
				  		 "(420 300, 340 160, 520 100, 580 300, 420 300),"+ 
				  		 "(640 240, 620 160, 680 300, 640 240))";
		
		Polygon polygon = (Polygon) wktReader.read(wktPoly);
		int numParts = polygon.getNumInteriorRing() + 1;
		int[] indexOfParts = new int[numParts];
		indexOfParts[0] = 0;
		if(numParts > 1){
			indexOfParts[1] = polygon.getExteriorRing().getNumPoints();
		}
		if(numParts > 2){
			for(int i = 1; i < polygon.getNumInteriorRing(); i++){
				indexOfParts[i + 1] = indexOfParts[i] + polygon.getInteriorRingN(i).getNumPoints() + 1;
			}
		}
		Coordinate[] coordinates = polygon.getCoordinates();
		
		Polygon polygon2 = JtsUtil.createPolygon(coordinates, indexOfParts);
		
		assertTrue(polygon.equalsExact(polygon2));
	}
	
	
	public void testSimplifyGeometry() throws ParseException{
		String wktPoly = "POLYGON ((460 340, 160 240, 480 20, 860 160, 720 360, 560 380, 460 340),"+ 
 		 "(420 300, 340 160, 520 100, 580 300, 420 300),"+ 
 		 "(640 240, 620 160, 680 300, 640 240))";

		Polygon polygon = (Polygon) wktReader.read(wktPoly);
		
		String wkt = "LINESTRING (-23.90282 93.41693, -14.96865 81.97492, 1.64577 95.14107, 5.721 50.62696, 22.02194 52.66458, 26.09718 84.48276, 57.44514 52.03762, 61.05016 76.17555, 87.22571 82.75862)";
		LineString lineString = (LineString) wktReader.read(wkt);
		
		Geometry generalizedLine = JtsUtil.
			douglasPeuckerSimplify(lineString, 10d);
		
		Geometry generalizedPolygon = JtsUtil.
			douglasPeuckerSimplify(polygon, 10d);
		
		assertTrue(! lineString.equals(generalizedLine));
		assertTrue(! polygon.equals(generalizedPolygon));
		
		
		
		
	}
}
