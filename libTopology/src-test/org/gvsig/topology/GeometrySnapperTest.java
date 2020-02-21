/*
 * Created on 05-sep-2007
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
* $Id$
* $Log: GeometrySnapperTest.java,v $
* Revision 1.2  2007/09/10 14:42:13  azabala
* *** empty log message ***
*
* Revision 1.1  2007/09/06 16:32:02  azabala
* *** empty log message ***
*
*
*/
package org.gvsig.topology;

import java.util.ArrayList;

import org.gvsig.jts.GeometrySnapper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;


import junit.framework.TestCase;

public class GeometrySnapperTest extends TestCase {

	GeometryFactory factory = new GeometryFactory();
	WKTReader wktReader = new WKTReader(factory);
	
	
	public void tearDown() throws Exception{
		super.tearDown();
		factory = null;
		wktReader = null;
		System.gc();
	}
	
	public void testAverageSnap1(){
		Coordinate coord1 = new Coordinate(0, 0);
		Coordinate coord2 = new Coordinate(100, 100);
		Coordinate coord3 = new Coordinate(200, 200);
		ArrayList coords = new ArrayList();
		coords.add(coord1);
		coords.add(coord2);
		coords.add(coord3);
		int[] weights = {1,1,1};//all coordinates with the same weight
		
		GeometrySnapper snapper = new GeometrySnapper(0.1d);
		Coordinate averageCoord = snapper.snapAverage(coords, weights);
		
		assertTrue(averageCoord.x == (0 + 100 + 200)/3);
		
		
		int[] weights2 = {1,2,2, 1};//first coordinate with double weight
		coords.add(new Coordinate(0.5, 1.27));
		averageCoord = snapper.snapAverage(coords, weights2);
		//snapped point is nearest to the coord with higher weight
		assertTrue(coord1.distance(averageCoord) < coord3.distance(averageCoord));
	}
	
	
	public void testSnapManyGeometries() throws ParseException{
		String linestring1 = "LINESTRING (280 80, 380 240, 620 360, 820 220, 600 140, 560 240, 420 180)";
		String linestring2 = "LINESTRING (280.001 79.999, 420.21 179.999)";
		
		LineString geo1 = (LineString) wktReader.read(linestring1);
		LineString geo2 = (LineString) wktReader.read(linestring2);
		
		GeometrySnapper snapper = new GeometrySnapper(0.6);
		LineString[] geometries = {geo1, geo2};
		int[] weights = {1,0};
		Geometry[] snappedGeometries = snapper.snap(geometries, weights);
		for(int i = 0; i < snappedGeometries.length; i++){
			System.out.println(snappedGeometries[i].toText());
		}
		LineString geo2orig = (LineString) wktReader.read(linestring2);
		assertTrue(!geo2orig.equals(geo1));
	}
	
	
	
}

