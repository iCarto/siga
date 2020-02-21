/*
 * Created on 18-sep-2007
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
* $Id: MustNotHaveRepeatedPointsTest.java,v 1.2 2007/09/19 16:36:56 azabala Exp $
* $Log: MustNotHaveRepeatedPointsTest.java,v $
* Revision 1.2  2007/09/19 16:36:56  azabala
* added snap repeated points test
*
* Revision 1.1  2007/09/19 09:22:34  azabala
* first version in cvs
*
*
*/
package org.gvsig.topology.topologyrules;

import java.util.Collection;

import junit.framework.TestCase;

import org.gvsig.jts.RepeatedPointTester;
import org.gvsig.jts.SnapCoordinateList;
import org.gvsig.jts.SnapRepeatedPointTester;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.util.TestTopologyErrorContainer;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class MustNotHaveRepeatedPointsTest extends TestCase {

	PrecisionModel pm = new PrecisionModel(10000);
	GeometryFactory factory = new GeometryFactory(pm);
	WKTReader wktReader = new WKTReader(factory);
	
	
	Geometry multiPoint;
	Geometry multiPoint2;
	Geometry lineString;
	Geometry polygon;
	
	
	public void setUp() throws Exception{
		super.setUp();
		multiPoint = wktReader.read("MULTIPOINT (520 160, 540 180, 540 180, 540 180, 600 180, 580 140, 640 140, 640 140, 640 160)");
		multiPoint2 = wktReader.read("MULTIPOINT (520 160, 540 180, 600 180, 580 140, 640 140,  640 160)");
		lineString = wktReader.read("LINESTRING (520 120, 560 180, 560 180, 560 180, 620 160, 660 200, 700 140, 700 140, 700 140, 620 120)");
		polygon = wktReader.read("POLYGON ((600 180, 600 180, 600 180, 600 180, 540 200, 480 140, 540 120, 540 120, 680 160, 600 180, 600 180))");
	}
	
	public void tearDown() throws Exception{
		super.tearDown();
		multiPoint = null;
		lineString = null;
		polygon = null;
		pm = null;
		factory = null;
		wktReader = null;
	}
	
	
	public void testRepeatedPointTester() {
		RepeatedPointTester tester = new RepeatedPointTester();
		
		boolean bMultiPoint = tester.hasRepeatedPoint(multiPoint);
		Collection<Coordinate> mp = tester.getRepeatedCoordinates();
		tester.clear();
		
		boolean bLineString = tester.hasRepeatedPoint(lineString);
		Collection<Coordinate> mp2 = tester.getRepeatedCoordinates();
		tester.clear();
		
		
		boolean bPolygon = tester.hasRepeatedPoint(polygon);
		Collection<Coordinate> mp3 = tester.getRepeatedCoordinates();
		
		assertTrue(bMultiPoint);
		assertTrue(bLineString);
		assertTrue(bPolygon);
		
		assertTrue(mp.size() == 2);
		assertTrue(mp2.size() == 2);
		assertTrue(mp3.size() == 2);
	}
	
	public void testRemoveRepeatedPoints(){
		RepeatedPointTester tester = new RepeatedPointTester();
		multiPoint = tester.removeRepeatedPoints(multiPoint);
		
		lineString = tester.removeRepeatedPoints(lineString);
		
		polygon = tester.removeRepeatedPoints(polygon);
	}
	
	public void testMustNotHaveRepeatedPointsRule() throws ParseException{
		
		FLyrVect multiPointLayer = org.gvsig.topology.util.LayerFactory.getLyrWithRepeatedCoords();
		MustNotHaveRepeatedPoints rule = new MustNotHaveRepeatedPoints(multiPointLayer);
		TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		try {
			rule.checkPreconditions();
			rule.checkRule();
			int numberOfErrors = errorContainer.getNumberOfErrors();
			assertTrue(numberOfErrors == 1);
		} catch (TopologyRuleDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void testSnapCoordinateList(){
		SnapCoordinateList coordList = new SnapCoordinateList(0.01);
		
		Coordinate coord1 = new Coordinate(100, 100);
		Coordinate coord2 = new Coordinate(200, 200);
		Coordinate coord3 = new Coordinate(300, 100);
		Coordinate coord4 = new Coordinate(400, 400);
		Coordinate coord5 = new Coordinate(400.001, 400.001);
		
		coordList.add(coord1, false);
		coordList.add(coord2, false);
		coordList.add(coord3, false);
		coordList.add(coord4, false);
		coordList.add(coord5, false);
		
		Coordinate[] coords = coordList.toCoordinateArray();
		assertTrue(coords.length == 4);
		
	}
	
	/**
	 * The same test that testRepeatedPointTester with snapped coordinates
	 * @throws ParseException
	 */
	public void testSnapRepeatedPointTester() throws ParseException{
		
		Geometry multiPoint = wktReader.read("MULTIPOINT (520 160, 540 180, 540.002 179.999, 540.01 179.999, 600 180, 580 140, 640 140, 640 140, 640 160)");
		Geometry lineString = wktReader.read("LINESTRING (520 120, 560 180, 560 180.001, 560.001 180, 620 160, 660 200, 700 140, 700 140, 700 140, 620 120)");
		Geometry polygon = wktReader.read("POLYGON ((600 180, 600 179.999, 600.004 180, 600 180, 540 200, 480 140, 540 120, 540 120, 680 160, 600 180, 600 180))");
		SnapRepeatedPointTester tester = new SnapRepeatedPointTester(0.011);
		
		boolean bMultiPoint = tester.hasRepeatedPoint(multiPoint);
		Collection<Coordinate> mpA = tester.getRepeatedCoordinates();
		tester.clear();
		
		boolean bLineString = tester.hasRepeatedPoint(lineString);
		Collection<Coordinate> mpB = tester.getRepeatedCoordinates();
		tester.clear();
		
		
		boolean bPolygon = tester.hasRepeatedPoint(polygon);
		Collection<Coordinate> mpC = tester.getRepeatedCoordinates();
		
		assertTrue(bMultiPoint);
		assertTrue(bLineString);
		assertTrue(bPolygon);
		
		assertTrue(mpA.size() == 2);
		assertTrue(mpB.size() == 2);
		assertTrue(mpC.size() == 2);
		
		multiPoint = tester.removeRepeatedPoints(multiPoint);
		lineString = tester.removeRepeatedPoints(lineString);
		polygon = tester.removeRepeatedPoints(polygon);
	}

}

