/*
 * Created on 26-ago-2007
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
 * $Log: LineStringSelfIntersectionTest.java,v $
 * Revision 1.4  2007/09/14 17:35:30  azabala
 * *** empty log message ***
 *
 * Revision 1.3  2007/09/10 14:42:13  azabala
 * *** empty log message ***
 *
 * Revision 1.2  2007/09/06 16:32:02  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2007/08/27 18:05:59  azabala
 * first version in cvs
 *
 *
 */
package org.gvsig.topology;

import java.util.HashMap;

import junit.framework.TestCase;

import org.gvsig.jts.GeometryCracker;
import org.gvsig.jts.GeometrySnapper;
import org.gvsig.jts.JtsUtil;
import org.gvsig.jts.LineStringSelfIntersectionChecker;
import org.gvsig.jts.RobustLengthIndexedLine;
import org.gvsig.jts.SnapLineStringSelfIntersectionChecker;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * To see visually the geometries used for testing, use JTS Test editor and
 * paste WKT code.
 * 
 * TODO Maybe edition in gvSIG would have a wkt editor, or a tool to select a
 * geometry and see it in WKT.
 * 
 * TODO To avoid robustness problems, is strongly encourage to create 
 * JTS geometries in gvSIG with a FIXED precision model (limit the number of significant decimals used in computations).
 * 
 * For example:
 * <ol>
 *  <li>new PrecisioModel(10000) consideer 4 decimals.</li>
 *  <li>new PrecisionModel(10) consideer 1 decimal.</li>
 * </ol>             
 * 
 * 
 * 
 */

public class LineStringSelfIntersectionTest extends TestCase {

	PrecisionModel pm = new PrecisionModel(10000);
	GeometryFactory factory = new GeometryFactory(pm);
	WKTReader wktReader = new WKTReader(factory);
	
	
	
	public void testRobustLenghtIndexedLine() throws ParseException{
		String wkt = "LINEARRING (-13900 -920, -8300 120, -5020 3460, -9480 5660, -15320 2620, -19420 4960, -19300 7600, -14620 8500, -13180 6840, -16380 5460, -22020 6280, -22340 3060, -18860 2120, -14820 4880, -10200 4460, -8680 2640, -8240 1780, -4420 -420, -5140 -2560, -9480 -3640, -12940 -2740, -13900 -920)";
		LineString lineString = (LineString) wktReader.read(wkt);
		LineStringSelfIntersectionChecker checker = new LineStringSelfIntersectionChecker(
				lineString, 0.01d);
		
		RobustLengthIndexedLine indexedLine = new RobustLengthIndexedLine(lineString);
		HashMap coordsIndex = new HashMap();
		// Con esta casuistica cubrimos el Outer Ring
		if (checker.hasSelfIntersections()) {
			Coordinate[] selfIntersection = checker.getSelfIntersections();
			for(int i = 0; i < selfIntersection.length; i++){
				Coordinate coord = selfIntersection[i];
				Double index = (Double) coordsIndex.get(coord);
				double indexD = 0d;
				if(index != null)
					indexD = index.doubleValue();
				double computedIndex = indexedLine.indexOfAfter(coord, indexD);
				computedIndex = indexedLine.indexOf(coord);
				coordsIndex.put(coord, new Double(computedIndex));
				System.out.println(coord.toString()+" - index = " + computedIndex);
			}	
		}
		//if in this point JTS doesnt launch an exception, RobustLenghtIndexedLine
		//solves all LenghtIndexedLine robustness problems
		assertTrue(true);
	}

	public void testCleanLineWith1SelfIntersection() throws ParseException {
		// linestring with one self intersection
		String wkt1 = "LINESTRING (-12378 3231, -14205 1381, -8845 2486, -11080 4794, -13820 -663)";

		LineString lineString = (LineString) wktReader.read(wkt1);

		LineStringSelfIntersectionChecker checker = new LineStringSelfIntersectionChecker(
				lineString);

		assertTrue(checker.hasSelfIntersections());

		Coordinate[] selfIntersections = checker.getSelfIntersections();

		assertTrue(selfIntersections.length == 1);

		Geometry[] cleanedLineString = checker.clean();

		assertTrue(cleanedLineString.length == 3);

	}

	public void testCleanLineWithManySelfIntersection() throws ParseException {
		String wkt2 = "LINESTRING (-16152 2534, -15190 5251, -10239 3808, -8316 -807, -11417 -1360, -11344 2246, -6128 5203, -600 2606, -1417 515, -3484 -1023, -5263 2486, -816 6765, 602 5948)";
		LineString lineString = (LineString) wktReader.read(wkt2);
		LineStringSelfIntersectionChecker checker = new LineStringSelfIntersectionChecker(
				lineString);
		checker.hasSelfIntersections();
		Coordinate[] selfIntersections = checker.getSelfIntersections();
		Geometry[] cleanedLineString = checker.clean();
		assertTrue(selfIntersections.length == 2);
		assertTrue(cleanedLineString.length == 5);
	}

	public void testPolygonSelfIntersected() throws ParseException {
		String wkt = "POLYGON ((-7571 299, -4686 5443, -13075 6452, -1441 1525, -7571 299))";
		Polygon polygon = (Polygon) wktReader.read(wkt);

		LineStringSelfIntersectionChecker checker = new LineStringSelfIntersectionChecker(
				polygon.getExteriorRing(), 0.01d);

		// Con esta casuistica cubrimos el Outer Ring
		if (checker.hasSelfIntersections()) {
			Coordinate[] selfIntersection = checker.getSelfIntersections();
			LineString[] splitted = (LineString[]) checker.clean();
			assertTrue(selfIntersection.length > 0);
			assertTrue(splitted.length == 2);
			assertTrue(JtsUtil.isClosed(splitted[0], 0.01d));
			//la solucion contiene un LinearRing (generado por polygonizer) y un 
			//LineString que ya estaba cerrado pero que por definicion nunca podrá ser closed
			assertTrue(JtsUtil.isClosed(splitted[1], 0.01d));
		}

		// Ahora habría que hacer lo mismo para cada anillo interior, y luego
		// asignarselos
		// a algun anillo exterior. Además, habría que verificar reglas que
		// deben cumplir
		// los holes de un poligono

	}

	public void testPolygonWithManySelfIntersections() throws ParseException {
		String wkt = "LINESTRING (-13900 -920, -8300 120, -5020 3460, -9480 5660, -15320 2620, -19420 4960, -19300 7600, -14620 8500, -13180 6840, -16380 5460, -22020 6280, -22340 3060, -18860 2120, -14820 4880, -10200 4460, -8680 2640, -8240 1780, -4420 -420, -5140 -2560, -9480 -3640, -12940 -2740, -13900 -920)";
		LineString lineString = (LineString) wktReader.read(wkt);
		LineStringSelfIntersectionChecker checker = new LineStringSelfIntersectionChecker(
				lineString, 0.01d);

		// Con esta casuistica cubrimos el Outer Ring
		if (checker.hasSelfIntersections()) {
			Coordinate[] selfIntersection = checker.getSelfIntersections();
			LineString[] splitted = (LineString[]) checker.clean();
			assertTrue(selfIntersection.length == 4);
			assertTrue(splitted.length == 5);

			assertTrue(splitted[0].isClosed());
			assertTrue(splitted[1].isClosed());
			assertTrue(splitted[2].isClosed());
			assertTrue(splitted[3].isClosed());
			assertTrue(splitted[4].isClosed());
		}
	}
	
	
	public void testLineStringWithSelfIntersectionIfSnap() throws ParseException{
//		 linestring with one self intersection
		String wkt1 = "LINESTRING (-12378 3231, -14205 1381, -8845 2486, -11080 4794, -13820 -663)";
		LineString selfIntersectedLS = (LineString) wktReader.read(wkt1);
		SnapLineStringSelfIntersectionChecker checker = new SnapLineStringSelfIntersectionChecker(selfIntersectedLS, 0.01d);
		if(checker.hasSelfIntersections()){
			Coordinate[] selfIntersection = checker.getSelfIntersections();
			System.out.println(selfIntersection.length);
		}
		
		
		String wkt = "LINESTRING (100 100, 300 100, 300 300, 250 300, 250 100.01)";
		LineString lineString = (LineString) wktReader.read(wkt);
		checker = new SnapLineStringSelfIntersectionChecker(lineString, 0.1d);
		if(checker.hasSelfIntersections()){
			Coordinate[] selfIntersection = checker.getSelfIntersections();
			System.out.println(selfIntersection.length);
		}
	}
	
	public void testLineStringWithOneSnapSelfIntersection() throws ParseException{
		//a) use SnapSelfIntersectionChecker
		String wkt1 = "LINESTRING (100 100, 300 100, 300 300, 250 300, 250 100.01)";
		LineString lin = (LineString) wktReader.read(wkt1);
		SnapLineStringSelfIntersectionChecker checker = new SnapLineStringSelfIntersectionChecker(lin, 0.02);
		if(checker.hasSelfIntersections()){
			Coordinate[] selfIntersection = checker.getSelfIntersections();
			assertTrue(selfIntersection.length == 1);
			assertTrue(selfIntersection[0].equals2D(new Coordinate(250, 100)));
		}
		
		
		
		//b) use previous crack and snap, and normal checker
		GeometryCracker cracker = new GeometryCracker(0.02);
		GeometrySnapper snapper = new GeometrySnapper(0.02);
		//pequeño truco para snapear y cracker una misma linea
		Geometry[] geoms = {lin, lin};
		geoms = cracker.crackGeometries(geoms);
		geoms = snapper.snap(geoms);
		
		LineStringSelfIntersectionChecker checker2 = new LineStringSelfIntersectionChecker((LineString) geoms[0]);
		if(checker2.hasSelfIntersections()){
			Coordinate[] intersections = checker2.getSelfIntersections();
			System.out.println(intersections.length);
		}
		//No funciona. Las lineas a cracker deben ser diferentes
		
		
	}
	
	
	public void testErrorFound1() throws ParseException{
		String wkt = "LINESTRING (232180.23144462315 4174489.509113544, 232121.61266760822 4174405.7675090614, 232124.40506564634 4174416.9332327805, 232123.90644495402 4174416.9085969683, 232115.9631261746 4174404.4976449297, 232067.30475050298 4174399.6315630404, 232052.7077264457 4174385.0335209747, 232034.86549405233 4174393.1435895893, 232015.40238810578 4174389.899602864, 231984.58502321702 4174375.302375205, 231944.03671616025 4174362.3262247005, 231905.10928265666 4174339.6191320284, 231871.0479310423 4174321.777917644, 231827.25563725995 4174290.9605527553, 231786.70610859265 4174256.8992011407, 231752.6457749871 4174221.2159579643, 231725.0726004257 4174192.021706248, 231707.2303680323 4174157.9603546336, 231692.633343975 4174122.2769078556, 231682.9011801965 4174083.3506959626, 231661.81597908627 4174050.9112359104, 231621.2676720295 4174023.338061349, 231592.07240230442 4174015.227992734, 231585.58422525148 4174003.874955402, 231566.12111930494 4174011.9840060086, 231533.68186285428 4174011.9840060086, 231514.21855330598 4174008.740019283, 231494.75442935067 4174007.1179241193, 231489.8883474614 4174015.227992734, 231467.18125478926 4174023.338061349, 231437.98598506418 4174034.6913022823, 231407.1696381842 4174055.7773177996, 231364.998217955 4174075.240627348, 231324.44991089823 4174096.325828458, 231293.6325460095 4174110.924074126, 231270.92545333735 4174138.4972486873, 231243.3520751742 4174170.9354871293, 231222.2658560552 4174204.9968387433, 231202.80275010865 4174240.6802855213, 231186.5836308877 4174277.984402251, 231183.33964416213 4174308.8017671397, 231186.5836308877 4174347.7289970415, 231198.15289328076 4174385.0078671537, 231189.2880729687 4174444.8474402777, 231208.8269187672 4174553.7110578213, 231209.96260934373 4174511.713513489, 231214.1568054491 4174506.680681764, 231214.1568054491 4174488.8384493706, 231217.4007921747 4174466.1313566985, 231219.02309094014 4174443.424060425, 231223.88795121887 4174412.606695536, 231231.99801983373 4174376.9244703683, 231231.99801983373 4174342.8631187542, 231230.37592467008 4174303.9356852504, 231240.1080884486 4174279.6064974144, 231262.81518112074 4174245.546163809, 231274.67213279032 4174232.371297994, 231300.94307054154 4174215.951631047, 231340.02279815616 4174185.246857927, 231390.26743508296 4174148.9595283507, 231406.12210716662 4174149.593340607, 231420.14477067956 4174141.740217404, 231460.69409574507 4174133.631166798, 231489.8883474614 4174144.98420413, 231525.57179423943 4174169.3135955674, 231553.14496880083 4174190.3996110843, 231583.96233368956 4174203.37474358, 231592.07240230442 4174214.729002522, 231598.6167734411 4174231.3412767276, 231624.74518996544 4174246.6582365823, 231656.5117462252 4174272.8992448756, 231712.09644992155 4174300.6916985246, 231739.66962448295 4174325.0219043694, 231763.99901592053 4174349.351295807, 231793.19428564562 4174360.704129537, 231809.41442287533 4174375.302375205, 231815.71711873155 4174387.576099662, 231848.0570175269 4174405.7675090614, 231898.3018580555 4174444.8474402777, 231926.21606555264 4174481.1349734557, 231962.50339512914 4174509.049180953, 232001.58352994727 4174520.213683062, 232037.87085952374 4174536.9633884504, 232085.32513442996 4174550.91947419, 232121.61266760822 4174581.6252653184, 232160.6925988246 4174617.912594895, 232199.7715120322 4174656.992526111, 232207.1351766217 4174659.294651129, 232228.72510647684 4174652.385629253, 232228.66158272998 4174652.31090741, 232213.72881938252 4174634.662300283, 232211.2239070192 4174632.1575915217, 232208.41522084086 4174620.2177739386, 232198.68305706233 4174599.131351218, 232182.46393784136 4174571.5581766567, 232161.10428156852 4174546.639154543, 232160.6925988246 4174545.3365105297, 232180.23144462315 4174489.509113544, 232180.23144462315 4174489.509113544)";
		LineString lin = (LineString) wktReader.read(wkt);
		SnapLineStringSelfIntersectionChecker checker = new SnapLineStringSelfIntersectionChecker(lin, 0.02);
		if(checker.hasSelfIntersections()){
			Coordinate[] selfIntersection = checker.getSelfIntersections();
			assertTrue(selfIntersection.length == 1);
			assertTrue(selfIntersection[0].equals2D(new Coordinate(250, 100)));
		}
		//Con 0.02 no da errores. Subimos a 0.1 la tolerancia de SNAP....
		checker = new SnapLineStringSelfIntersectionChecker(lin, 0.1);
		if(checker.hasSelfIntersections()){
			Coordinate[] selfIntersection = checker.getSelfIntersections();
			assertTrue(selfIntersection.length == 1);
			assertTrue(selfIntersection[0].equals2D(new Coordinate(250, 100)));
		}
	}
	
}
