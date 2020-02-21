/*
 * Created on 25/08/2007
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
 * $Log: JtsSnapTest.java,v $
 * Revision 1.3  2007/09/14 17:35:30  azabala
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

import junit.framework.TestCase;

import org.gvsig.jts.GeometryCracker;
import org.gvsig.jts.GeometrySnapper;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.overlay.OverlayOp;
import com.vividsolutions.jts.operation.overlay.SnappingOverlayOperation;
import com.vividsolutions.jts.operation.overlay.snap.SnapIfNeededOverlayOp;
import com.vividsolutions.jts.operation.overlay.snap.SnapOverlayOp;

// import com.vividsolutions.jts.operation.overlay.snap.SnapIfNeededOverlayOp;
// import com.vividsolutions.jts.operation.overlay.snap.SnapOverlayOp;

/**
 * 
 * In these tests we are trying to probe the use of snapping and precision
 * reduction in JTS operations, to avoid undesired effects (for example spureus
 * lines in dissolve) and to allow clustering tolerance in topologies.
 * 
 * @author azabala
 * 
 */

/*
 * ¡Ojo! Tenemos dos alternativas para aplicar Snap con JTS que son excluyentes.
 * 
 * a) Usar  clase SnapOverlayOperation de JTS 1.8. En este caso no se maneja el
 * concepto de tolerancia de snap, sino de "rejilla". El tamaño del snap vendrá
 * dada por la resolución del PrecisionModel empleado (simplemente hay que usar
 * un PrecisionModel de tipo FIXED)
 * 
 * b) Usar las clases que hemos desarrollado en gvSIG dentro de
 * libFMap. En este caso si que se usa la tolerancia de snap. Estas clases han
 * sido testadas y funciona perfectamente para INTERSECCION entre lineas y
 * lineas.
 * 
 * Hay que testar resto de operaciones (UNION, DIFERENCIA, etc.) 
 * 
 */
public class JtsSnapTest extends TestCase {

	static String wktLS1 = "LINESTRING (260 120, 300 180, 420 180, 480 120)";

	static String wktLS2 = "LINESTRING (479.999 120, 459 114, 437 128, 426 130, 458 141)";

	// ESTA HABIENDO PROBLEMAS DE COMPILACION CON LA LIBRERIA JTS 1.8

	public void testLine0() throws ParseException {

		String wktLS1 = "LINESTRING (260 120, 300 180, 420 180, 480 120)";
		String wktLS2 = "LINESTRING (479.999 120, 459 114, 437 128, 426 130, 458 141)";
		GeometryFactory factory = new GeometryFactory(new PrecisionModel(1000));
		WKTReader wktReader = new WKTReader(factory);
		Geometry ls1 = wktReader.read(wktLS1);
		Geometry ls2 = wktReader.read(wktLS2);
		Geometry intersection = SnapOverlayOp.intersection(ls1, ls2);
		System.out.println(intersection.toText());
	}

	public void testLineLineJTS18DEFAULT() throws ParseException {
		GeometryFactory factory = new GeometryFactory();
		WKTReader wktReader = new WKTReader(factory);
		Geometry ls1 = wktReader.read(wktLS1);
		Geometry ls2 = wktReader.read(wktLS2);

		Geometry intersection = SnapIfNeededOverlayOp.intersection(ls1, ls2);
		System.out.println(intersection.toText());
		assertTrue(intersection instanceof GeometryCollection);
		GeometryCollection sol = (GeometryCollection) intersection;
		assertTrue(sol.getNumGeometries() == 0);

	}

	// SnapIfNeeded solo actua si se producen errores de precision en el calculo
	// de overlays
	// si no hay ningun error, pasa del snap
	public void testLineLineJTS18PMFIXED() throws ParseException {
		GeometryFactory factory = new GeometryFactory(new PrecisionModel(1));
		WKTReader wktReader = new WKTReader(factory);
		Geometry ls1 = wktReader.read(wktLS1);
		Geometry ls2 = wktReader.read(wktLS2);
		Geometry intersection = SnapOverlayOp.intersection(ls1, ls2);
		System.out.println(intersection.toText());
		// assertTrue(!(intersection instanceof GeometryCollection));
		GeometryCollection sol = (GeometryCollection) intersection;
		assertTrue(sol.getNumGeometries() != 0);
	}

	public void testLineLineJTS18PMFIXED2() throws ParseException {
		GeometryFactory factory = new GeometryFactory(new PrecisionModel(1000));
		WKTReader wktReader = new WKTReader(factory);
		Geometry ls1 = wktReader.read(wktLS1);
		Geometry ls2 = wktReader.read(wktLS2);
		Geometry intersection = SnapOverlayOp.intersection(ls1, ls2);
		System.out.println(intersection.toText());
		assertTrue(!(intersection instanceof GeometryCollection));
	}

	/*
	 * El resultado de estos tres primeros tests es el siguiente:
	 * GEOMETRYCOLLECTION EMPTY MULTIPOINT (458 141, 480 120) LINESTRING
	 * (479.999 120, 480 120)
	 * 
	 * El snap de JTS viene determinado por el PrecisionModel
	 * 
	 * En el primer caso, el PrecisionModel es el por defecto (maxima
	 * precisión), luego no se intersectan.
	 * 
	 * En el segundo caso, el PrecisionModel tiene una resolución de 0.1, luego
	 * considera que el primer y ultimo punto de la segunda linea intersectan.
	 * AHORA BIEN. Como no hay desplazamiento, considera que (480, 120) y
	 * (479.999, 120) son el mismo punto. Deberían desplazarse??
	 * 
	 * 
	 * 
	 * En el tercer caso ya da resultados no deseados. Los puntos (479.999 120)
	 * y (480, 120) si aplicamos una tolerancia de snap grande deben ser
	 * considerados el mismo punto, pero si la tolerancia de snap es pequeña
	 * (1000 = 0.001 de resolucion) no debería dar un LineString de solucion
	 * REPORTAR A JTS-LIST
	 */

	// TEST DE PRUEBA CON LAS CLASES DE JTS 1.7
	// OJO!!!!!!
	// EL SNAP DE GVSIG SOLO FUNCIONA CON JTS 1.7. CON JTS 1.8 NO COMPILA
	/**
	 * 
	 * Intersects two lines with the GvSig snap overlay api
	 * (com.vividsolutions.jts.overlay.SnappingOverlayOperation) with many snap
	 * tolerances.
	 * 
	 * The PrecisionModel of the geometries doesnt change.
	 * 
	 */
	public void testIntersectionLineLineGvSIG() throws ParseException {
		GeometryFactory factory = new GeometryFactory();
		WKTReader wktReader = new WKTReader(factory);
		Geometry ls1 = wktReader.read(wktLS1);
		Geometry ls2 = wktReader.read(wktLS2);

		// snapDist = 0.1
		Geometry intersection = SnappingOverlayOperation.overlayOp(ls1, ls2,
				OverlayOp.INTERSECTION, 0.1d);
		System.out.println(intersection.toText());

		// snapDist = 0.01
		intersection = SnappingOverlayOperation.overlayOp(ls1, ls2,
				OverlayOp.INTERSECTION, 0.01d);
		System.out.println(intersection.toText());

		// snapDist = 0.001
		intersection = SnappingOverlayOperation.overlayOp(ls1, ls2,
				OverlayOp.INTERSECTION, 0.001d);
		System.out.println(intersection.toText());

		// snapDist = 0.0001
		intersection = SnappingOverlayOperation.overlayOp(ls1, ls2,
				OverlayOp.INTERSECTION, 0.0001d);
		System.out.println(intersection.toText());

		// snapDist = 2
		intersection = SnappingOverlayOperation.overlayOp(ls1, ls2,
				OverlayOp.INTERSECTION, 2d);
		System.out.println(intersection.toText());
	}

	public void testSegmentIntersectionOfTwoLines() throws ParseException {
		/*
		 * Two polylines with two parallel segments in a distance of 20 uds.
		 */
		String line1wkt = "LINESTRING (300 140, 380 140, 440 140)";
		String line2wkt = "LINESTRING (280 120, 340 120, 420 120, 480 120, 500 140, 480 200, 240 140)";
		GeometryFactory factory = new GeometryFactory();
		WKTReader wktReader = new WKTReader(factory);
		Geometry ls1 = wktReader.read(line1wkt);
		Geometry ls2 = wktReader.read(line2wkt);
		//		
		Geometry intersection = SnappingOverlayOperation.overlayOp(ls1, ls2,
				OverlayOp.INTERSECTION, 19.99);
		System.out.println(intersection.toText());

		intersection = SnappingOverlayOperation.overlayOp(ls1, ls2,
				OverlayOp.INTERSECTION, 20.01);

		System.out.println(intersection.toText());

		// In this test the results are chaotic. A 20 ud snap tolerance is too
		// high,

		/*
		 * Another approach: compute a snap and a crack before compute a overlay
		 * (with JTS normal classes)
		 */

		GeometrySnapper snap = new GeometrySnapper(30.01);

		Geometry[] snappedGeometries = snap.snap(new Geometry[] { ls1, ls2 });
		for (int i = 0; i < snappedGeometries.length; i++) {
			System.out.println(snappedGeometries[i].toText());
		}
		intersection = OverlayOp.overlayOp(snappedGeometries[0],
				snappedGeometries[1], OverlayOp.INTERSECTION);
		System.out.println(intersection.toText());
	}

	public void testNormalizeClosedLinesWithSnap() throws ParseException {
		GeometryFactory factory = new GeometryFactory();
		WKTReader wktReader = new WKTReader(factory);
		String wkt = "LINESTRING (100 100, 200 100, 200 200, 100 200, 100 100.01)";
		Geometry geom = wktReader.read(wkt);
		GeometrySnapper snapper = new GeometrySnapper(0.1);
		Geometry newGeom = snapper.normalizeExtremes(geom);
		assertTrue(!newGeom.equals(geom));
	}

	public void testIntersectionPointLineSnap() throws ParseException {

		// a) point snaps in a linestring vertex with FIXED precision model
		// result must be -13220 2180
		PrecisionModel pm2 = new PrecisionModel(10);
		GeometryFactory factory2 = new GeometryFactory(pm2);
		WKTReader wktReader2 = new WKTReader(factory2);
		String ptWkt = "POINT (-13220 2180)";
		Point pt = (Point) wktReader2.read(ptWkt);
		String lnWkt = "LINESTRING (-13260 2180, -13240 2160, -13220.01 2180, -13180 2180, -13180 2200, -13240 2200)";
		LineString ln = (LineString) wktReader2.read(lnWkt);
		Geometry intersection = SnapOverlayOp.intersection(ln, pt);
		System.out.println(intersection.toText());
		assertTrue(pt.equals(intersection));
		intersection = SnappingOverlayOperation.overlayOp(ln, pt,
				SnappingOverlayOperation.INTERSECTION, 0.01);
		System.out.println(intersection.toText());
		assertTrue(pt.equals(intersection));

		// b) precision model not fixed
		pm2 = new PrecisionModel();
		factory2 = new GeometryFactory(pm2);
		wktReader2 = new WKTReader(factory2);
		pt = (Point) wktReader2.read(ptWkt);
		lnWkt = "LINESTRING (-13260 2180, -13240 2160, -13220.01 2180, -13180 2180, -13180 2200, -13240 2200)";
		ln = (LineString) wktReader2.read(lnWkt);
		intersection = SnapOverlayOp.intersection(ln, pt);
		System.out.println(intersection.toText());
		// the snap of OverlayOp doesnt allow to choose a snap tolerance.
		// it computes the snap tolerance from the geometries' dimensions.

		// c) Snapped intersection is not a vertex of the line
		// Possibly problem: the solution is not part of the line...
		// we could solve this if we had applied a crack and snap process
		ptWkt = "POINT(9.99 10.01)";
		lnWkt = "LINESTRING(0 0, 100 100)";
		pt = (Point) wktReader2.read(ptWkt);
		ln = (LineString) wktReader2.read(lnWkt);

		double snapTolerance = Math.sqrt(2 * (0.01 * 0.01));
		intersection = SnappingOverlayOperation.overlayOp(ln, pt,
				SnappingOverlayOperation.INTERSECTION, snapTolerance);
		System.out.println(intersection.toText());
		assertTrue(pt.equals(intersection));
		intersection = SnappingOverlayOperation.overlayOp(ln, pt,
				SnappingOverlayOperation.INTERSECTION, 0.000001);
		System.out.println(intersection.toText());
		assertTrue(intersection.toText().equalsIgnoreCase(
				"GEOMETRYCOLLECTION EMPTY"));

		// d) we compute a crack and snap to the input geometries
		Geometry[] geoms = { pt, ln };
		GeometrySnapper snapper = new GeometrySnapper(0.05);
		GeometryCracker cracker = new GeometryCracker(0.05);
		geoms = snapper.snap(geoms);// snap must do nothing
		geoms = cracker.crackGeometries(geoms);
		geoms = snapper.snap(geoms);// FIXME ALWAYS MAKE THE CRACK BEFORE THE
									// SNAP. CRACK AND SNAP ONLY WORKS AT
									// VERTICES OF THE TWO GEOMETRIES
		intersection = SnappingOverlayOperation.overlayOp(geoms[1], geoms[0],
				SnappingOverlayOperation.INTERSECTION, 0.01);
		System.out.println(intersection);
		intersection = OverlayOp.overlayOp(geoms[1], geoms[0],
				OverlayOp.INTERSECTION);
		System.out.println(intersection);
	}

	public void testIntersectionPolygonPointSnap() throws ParseException {
		
		GeometryFactory factory = new GeometryFactory(new PrecisionModel());
		WKTReader wktReader = new WKTReader(factory);
		
		String polWkt = "POLYGON ((-13220 2200, -13200 2180, -13220 2160, -13260 2180, -13280 2200, -13280 2220, -13220 2200))";
		String ptWkt = "POINT (-13253 2212)";
		String ptWkt2 = "POINT(-13220.01 2200)";

		Geometry pol = wktReader.read(polWkt);
		Geometry pt = wktReader.read(ptWkt);
		Geometry pt2 = wktReader.read(ptWkt2);

		GeometrySnapper snapper = new GeometrySnapper(1);
		GeometryCracker cracker = new GeometryCracker(1);

		Geometry[] geoms = { pol, pt, pt2 };
		geoms = cracker.crackGeometries(geoms);
		geoms = snapper.snap(geoms);

		// Conclusion: la interseccion punto-linea punto-poligono puede ser
		// resuelta satisfactoriamente
		// con el proceso de crack and snap
		//En general, el proceso de crack and snap resuelve todo lo que tenga
		//que ver con vertices
	}

	public void testIntersectionLinePolygonWithSnap() throws ParseException {
		
		GeometryFactory factory = new GeometryFactory(new PrecisionModel(100));
		WKTReader wktReader = new WKTReader(factory);
		
		
		//a) snapped intersection is a linestring vertex
		String polWkt = "POLYGON ((-13240 2220, -13300 2180, -13260 2160, -13200 2180, -13160 2220, -13240 2220))";
		String linWkt = "LINESTRING (-13320 2220, -13300 2200, -13271 2200, -13220 2260)";
		Geometry pol = wktReader.read(polWkt);
		Geometry lin = wktReader.read(linWkt);
		double dist = pol.distance(lin);
		Geometry[] geoms = {pol, lin};
		GeometrySnapper snapper = new GeometrySnapper(dist);
		GeometryCracker cracker = new GeometryCracker(dist);
		
		
		//TODO It has not too much sense that snap and crack points has too mutch precision
		//We could reduce this precision with a PrecisionModel and makePrecision call
		geoms = cracker.crackGeometries(geoms);
		geoms = snapper.snap(geoms);
		
		Geometry intersection = OverlayOp.overlayOp(geoms[0], geoms[1], OverlayOp.INTERSECTION);
		assertTrue(!intersection.toText().equalsIgnoreCase("GEOMETRYCOLLECTION EMPTY"));
		
		intersection = SnappingOverlayOperation.overlayOp(pol, lin, OverlayOp.INTERSECTION, dist);
		assertTrue(!intersection.toText().equalsIgnoreCase("GEOMETRYCOLLECTION EMPTY"));
		
		//El caso Linea - Poligono se resueve bien aplicando crack and snap si el snap no se hace en segmentos
		//paralelos entre poligono y linea
		linWkt = "LINESTRING (-13380 2259.99, -13220 2259.99, -13140 2160, -13200 2200, -13260 2220, -13320 2160, -13400 2200, -13460 2260)";
		polWkt = "POLYGON ((-13380 2260, -13260 2260, -13140 2320, -13260 2340, -13240 2300, -13380 2340, -13400 2340, -13380 2260))";
		
		lin = wktReader.read(linWkt);
		pol = wktReader.read(polWkt);
		
		cracker = new GeometryCracker(0.02);
		snapper = new GeometrySnapper(0.02);
		Geometry[] geoms2 = {lin, pol};
		geoms2 = cracker.crackGeometries(geoms2);
		geoms2 = snapper.snap(geoms2);
		
		//en este caso cracker y snap no funcionan, pues poligono y linea deben snapear en un segmento paralelo
		intersection = SnapOverlayOp.intersection(pol, lin);
		System.out.println(intersection);
		//El snap de JTS 1.8 no da resultados correctos para lineas paralelas si el PrecisionModel = 100
		//probamos con precision model = 10
		factory = new GeometryFactory(new PrecisionModel(10));
		wktReader = new WKTReader(factory);
		lin = wktReader.read(linWkt);
		pol = wktReader.read(polWkt);
		intersection = SnapOverlayOp.intersection(pol, lin);
		System.out.println(intersection);
		//El snap de JTS 1.8 funciona bien, solo que no permite jugar con la distancia de snap
		//que viene dada por el precision model (10 = 0.1 ud, 100 = 0.01 ud. etc.)
		
		
		//la interseccion con snap de linea con poligono funciona bien en nuestras clases
		intersection = SnappingOverlayOperation.overlayOp(pol, lin, OverlayOp.INTERSECTION, 0.02);
		assertTrue(intersection instanceof LineString);
		assertTrue(intersection.getCoordinates().length == 2);
		System.out.println(intersection);
		
		
	}

	public void testIntersectionPolygonPolygonWithSnap() throws ParseException {
		GeometryFactory factory = new GeometryFactory(new PrecisionModel(100));
		WKTReader wktReader = new WKTReader(factory);
		//a) two polygons snaps with the crack and snap process
		String polwkt1 = "POLYGON ((-13540 2340, -13420 2340, -13351 2380, -13300 2420, -13440 2420, -13560 2400, -13540 2340))";
		String polwkt2 = "POLYGON ((-13419.99 2340, -13280 2420, -12960 2360, -13060 2140, -13400 2140, -13560 2220, -13240 2240, -13240 2300, -13419.99 2340))";
		
		Geometry pol1 = wktReader.read(polwkt1);
		Geometry pol2 = wktReader.read(polwkt2);
	
		Geometry[] geoms = {pol1, pol2};
		GeometryCracker cracker = new GeometryCracker(0.5);
		geoms = cracker.crackGeometries(geoms);
		GeometrySnapper snapper = new GeometrySnapper(0.5);
		geoms = snapper.snap(geoms);
		System.out.println(geoms);
		Geometry intersection = OverlayOp.overlayOp(geoms[0], geoms[1], OverlayOp.INTERSECTION);
		System.out.println(intersection);
		intersection = SnappingOverlayOperation.overlayOp(geoms[0], geoms[1], OverlayOp.INTERSECTION, 0.4);
		System.out.println(intersection);
		//In this case, the crack and snap process solves the snap, so OverlayOp and
		//SnapOverlayOp gives the same result
		
		//b) Snap with parallel segments. Again the crack and snap solves the problem
		polwkt1 = "POLYGON ((-13600 2219.99, -13100 2219.99, -12920 2060, -13200 2020, -13160 2160, -13900 2040, -13600 2219.99))";
		polwkt2 = "POLYGON ((-13460 2220, -13220 2220, -13000 2340, -13180 2420, -13500 2400, -13280 2280, -13480 2340, -13460 2220))";
		pol1 = wktReader.read(polwkt1);
		pol2 = wktReader.read(polwkt2);
		cracker = new GeometryCracker(0.2);
		snapper = new GeometrySnapper(0.2);
		Geometry[] geoms2 = {pol1, pol2};
		geoms2 = cracker.crackGeometries(geoms2);
		geoms2 = snapper.snap(geoms2);
		intersection = SnappingOverlayOperation.overlayOp(pol1, pol2, OverlayOp.INTERSECTION, 0.2);
		System.out.println(intersection);
		
		//c) Two intersection areas: a linestring (parallel segments snapped)
		//and a true intersection
		polwkt1 = "POLYGON ((-13460 2280, -13440 2300, -13280 2300, -13240 2220, -13400 2220, -13440 2240, -13460 2280))";
		polwkt2 = "POLYGON ((-13320 2260, -13220 2340, -13340 2380, -13500 2400, -13600 2360, -13600 2280, -13540 2200, -13440.01 2240, -13560 2280, -13500 2360, -13360 2360, -13320 2260))";
		
		//by snapping, one intersection is POINT(-13440.01 2240)
		//by computing, onother one is a POLYGON
		pol1 = wktReader.read(polwkt1);
		pol2 = wktReader.read(polwkt2);
		cracker = new GeometryCracker(0.2);
		snapper = new GeometrySnapper(0.2);
		Geometry[] geoms3 = {pol1, pol2};
		geoms3 = cracker.crackGeometries(geoms3);
		geoms3 = snapper.snap(geoms3);
		intersection = SnappingOverlayOperation.overlayOp(geoms3[0], geoms3[1], OverlayOp.INTERSECTION, 0.2);
		System.out.println(intersection);
		assertTrue(intersection instanceof GeometryCollection);
		assertTrue(((GeometryCollection)intersection).getNumGeometries() == 2);
	}
	
	
	public void testUnionPointLineWithSnap() throws ParseException{
		String lnWkt = "LINESTRING (120 340, 220 100, 440 320, 600 60, 860 180, 800 300)";
		String ptWkt = "POINT (800 299.99)";
		GeometryFactory factory = new GeometryFactory(new PrecisionModel());
		WKTReader wktReader = new WKTReader(factory);
		Geometry ln = wktReader.read(lnWkt);
		Geometry pt = wktReader.read(ptWkt);
		
		Geometry lnUpt = SnappingOverlayOperation.overlayOp(ln, pt, OverlayOp.UNION, 0.02);
		assertTrue(lnUpt.toText().equalsIgnoreCase("LINESTRING (120 340, 220 100, 440 320, 600 60, 860 180, 800 300)"));
		
		lnWkt = "LINESTRING (320 160, 780 160)";
		ptWkt = "POINT (560 160.01)";
		ln = wktReader.read(lnWkt);
		pt = wktReader.read(ptWkt);
		lnUpt = SnappingOverlayOperation.overlayOp(ln, pt, OverlayOp.UNION, 0.02);
		
		assertTrue(lnUpt.toText().equalsIgnoreCase("LINESTRING (320 160, 780 160)"));
	}
	
	public void testUnionLineWithLineWithSnap() throws ParseException{
		String lnWkt = "LINESTRING (480 160, 520 140, 540 160, 580 160)";
		String ptWkt = "LINESTRING (580 160, 660 180, 680 160)";
		GeometryFactory factory = new GeometryFactory(new PrecisionModel());
		WKTReader wktReader = new WKTReader(factory);
		Geometry ln1 = wktReader.read(lnWkt);
		Geometry ln2 = wktReader.read(ptWkt);
		
		Geometry lnUpt = SnappingOverlayOperation.overlayOp(ln1, ln2, OverlayOp.UNION, 0.02);
		System.out.println(lnUpt);
		//El resultado es MULTILINESTRING ((480 160, 520 140, 540 160, 580 160), (580.01 160, 660 180, 680 160))
		//Es lo mismo que saldría si los puntos final e inicial coincidiesen
		
		//Para fusionarlo están las clases LineStringSewer de JTS
	}
}
