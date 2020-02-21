/*
 * Created on 10-sep-2007
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
 * $Log: SnapAndCrackTest.java,v $
 * Revision 1.1  2007/09/14 17:34:46  azabala
 * first version in cvs
 *
 *
 */
package org.gvsig.topology;

import org.gvsig.jts.GeometryCracker;
import org.gvsig.jts.GeometrySnapper;
import org.gvsig.jts.LineStringSplitter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

import junit.framework.TestCase;

/**
 * Unit tests for GeometryCrack and GeometrySnap classes.
 * 
 * @author azabala
 * 
 */
public class SnapAndCrackTest extends TestCase {

	PrecisionModel pm;

	GeometryFactory factory;

	WKTReader wktReader;

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

	public void testCrackTwoPolygons() throws ParseException {
		String pol1txt = "POLYGON ((320 160, 320 120, 360 120, 360 160, 320 160))";
		String pol2txt = "POLYGON ((360 160, 360 140, 360 120, 400 120, 400 140, 400 160, 360 160))";
		Polygon geo1 = (Polygon) wktReader.read(pol1txt);
		Polygon geo2 = (Polygon) wktReader.read(pol2txt);

		// GeometryCracker cracker = new GeometryCracker(0.1d);

		Polygon cracked1 = (Polygon) GeometryCracker.crackGeometries(geo1, 
																geo2, 
																	0.1d);

		System.out.println(cracked1.toText());
	}
	
	public void testCrackTwoIntersectingLinesWithPseudonodes() throws ParseException{
		
		//a) Two linestrings that intersects in points that are not nodes (pseudonodes)
		String a = "LINESTRING (300 120, 360 140, 480 160, 420 100)";
		String b = "LINESTRING (320 140, 360 100, 420 180, 480 100, 400 120)";
		LineString linA = (LineString) wktReader.read(a);
		LineString linB = (LineString) wktReader.read(b);
		
		GeometryCracker cracker = new GeometryCracker(0.1);
		Geometry[] geoms = {linA, linB};
		Geometry[] cracked = cracker.crackGeometries(geoms);
		
		System.out.println(cracked[0].toText());
		System.out.println(cracked[1].toText());
		
		//Crack and Snap only affect to Vertices (it doesnt compute intersections)
		assertTrue(a.equalsIgnoreCase(cracked[0].toText()));
		assertTrue(b.equalsIgnoreCase(cracked[1].toText()));
		
		
		//a1) We split these two lines in their intersections (MustNotHavePseudonodes rule)
		Geometry intersections = EnhancedPrecisionOp.intersection(linA, linB);
		Coordinate[] intersectionPoints = intersections.getCoordinates();
		LineStringSplitter splitter = new LineStringSplitter();
		LineString[] linASplittedA = splitter.splitSimple(linA, intersectionPoints);
		LineString[] linBSplittedB = splitter.splitSimple(linB, intersectionPoints);
		assertTrue(linASplittedA.length == 6);
		assertTrue(linBSplittedB.length == 6);
		assertTrue(intersectionPoints.length == 5);
		
		//in this point, LineStringSplitter could return splitted linestring with
		//numerical precision problems. To avoid this, we could snap them.
		
		GeometrySnapper snapper = new GeometrySnapper(0.01d);
		Geometry[] aa = snapper.snap(linASplittedA);
		Geometry[] bb = snapper.snap(linBSplittedB);
		
		//FIXME
		//Instead of use Union for the test, use LineStringSewer
		
		Geometry splittedUnion = aa[0];
		for(int i = 1; i < aa.length; i++){
			splittedUnion = splittedUnion.union(aa[i]);
		}
//		assertTrue(splittedUnion.equalsExact(linA));
		
		splittedUnion = bb[0];
		for(int i = 1; i < bb.length; i++){
			splittedUnion = splittedUnion.union(bb[i]);
		}
//		assertTrue(splittedUnion.equals(linB));
		
		
		//b) Two intersecting lines with vertices that must be cracked and snapped
		
		//b1) vertex 320,180.001 must be cracked in first geometry
		// FIXME When were going to crack a vertex...¿why dont apply the line equation y=mx+b
		//to avoid the pick 300 180, 320 180.001 360 180
		
		//b2) In 360,120 both lines intersects (this only will be detected in OverlayOp)
		//FIXME This is a real intersection. In the next test use a snap intersection
		
		// b3) In 380.01 99.99 we must crack first geometry
		
		// b4) 420.01 100.001 must be snapped
		
		//conclusion: to clean a topology we must: a) snap b) crack c) compute intersections
		
		a = "LINESTRING (300 180, 360 180, 360 100, 420 100)";
		b = "LINESTRING (320 180.001, 320 120, 380 120, 380.01 99.99, 400 180, 480 180, 420.01 100.001)";
		snapper = new GeometrySnapper(0.08);
		Geometry aG = wktReader.read(a);
		Geometry bG = wktReader.read(b);
		Geometry[] newgeoms = {aG,bG};
		Geometry[] snapped = snapper.snap(newgeoms);
		
		cracker = new GeometryCracker(0.08);
		snapped = cracker.crackGeometries(snapped);
		
		for(int i = 0; i < snapped.length; i++){
			LineString ls1 = (LineString) snapped[i];
			for(int j = 0; j < snapped.length; j++){
				if(i == j)
					continue;
				LineString ls2 = (LineString) snapped[i];
				//AQUI NO USAMOS SNAP. 
				Coordinate[] inters = EnhancedPrecisionOp.intersection(ls1, ls2).getCoordinates();
				LineString[] result1 = splitter.splitSimple(ls1, inters);
				Geometry multi1 = factory.createGeometryCollection(result1);
				LineString[] result2 = splitter.splitSimple(ls2, inters);
				Geometry multi2 = factory.createGeometryCollection(result2);
				multi2.toText();
				//Esto funciona porque son solo dos. Para hacerlo con muchas lineas, hay que mantener
				//para cada lineString una lista con todas las intersecciones, con independencia
				//de con que geomeetrias sea.. (USAR PARA ESTO EL PREPAREDGEOMETRY DE JTS)
			}//for
		}//for
		
		
		
		
	}

}
