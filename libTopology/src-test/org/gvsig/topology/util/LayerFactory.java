/*
 * Created on 24-sep-2007
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
* $Log$
*
*/
package org.gvsig.topology.util;

import java.util.ArrayList;
import java.util.List;

import org.gvsig.fmap.core.NewFConverter;

import com.hardcode.gdbms.engine.values.StringValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FMultiPoint2D;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.drivers.FeatureCollectionMemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.LayerDefinition;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class LayerFactory {
	static PrecisionModel pm = new PrecisionModel(10000);
	static GeometryFactory factory = new GeometryFactory(pm);
	static WKTReader wktReader = new WKTReader(factory);
	
	
	public static FLyrVect getLineLayerWithCollapsedCoords() throws ParseException{
		Geometry ln1 = wktReader.read("LINESTRING(500 500, 510 500)");
		Geometry ln2 = wktReader.read("LINESTRING(700 100, 720 120, 800 600)");
		
		LayerDefinition definition = createTestLayerDefinition();
		definition.setShapeType(FShape.LINE);
		
		//feature 1
		int index = 0;
		IFeature f1 = createTestFeature(ln1, index);
		index++;
		IFeature f2 = createTestFeature(ln2, index);
		
		ArrayList<IFeature> features = new ArrayList<IFeature>();
		features.add(f1);
		features.add(f2);
		
		FeatureCollectionMemoryDriver driver = 
			new FeatureCollectionMemoryDriver("multipuntos", 
				                                   features,
				                                   definition);
		
		return  (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.createLayer("multipuntos",
																driver, 
																null);
	}
	
	public static FLyrVect getLyrWithRepeatedCoords() throws ParseException{	
		Geometry multiPoint = wktReader.read("MULTIPOINT (520 160, 540 180, 540 180, 540 180, 600 180, 580 140, 640 140, 640 140, 640 160)");
		Geometry multiPoint2 = wktReader.read("MULTIPOINT (520 160, 540 180, 600 180, 580 140, 640 140,  640 160)");
		LayerDefinition definition = createTestLayerDefinition();
		definition.setShapeType(FShape.MULTIPOINT);
		
		Coordinate[] coords = ((MultiPoint)multiPoint).getCoordinates();
		double[] x = new double[coords.length];
		double[] y = new double[coords.length];
		for(int i = 0; i < coords.length; i++){
			x[i] = coords[i].x;
			y[i] = coords[i].y;
		}
		FMultiPoint2D m1 = new FMultiPoint2D(x, y);
		StringValue value = ValueFactory.createValue("s1");
		Value[] values = {value};
		DefaultFeature f1 = new DefaultFeature(m1, values, "id1");
		
		Coordinate[] coords2 = ((MultiPoint)multiPoint2).getCoordinates();
		double[] x2 = new double[coords2.length];
		double[] y2 = new double[coords2.length];
		for(int i = 0; i < coords2.length; i++){
			x2[i] = coords2[i].x;
			y2[i] = coords2[i].y;
		}
		FMultiPoint2D m2 = new FMultiPoint2D(x2, y2);
		StringValue value2 = ValueFactory.createValue("s2");
		Value[] values2 = {value2};
		DefaultFeature f2 = new DefaultFeature(m2, values2, "id2");
		
		ArrayList<IFeature> features = new ArrayList<IFeature>();
		features.add(f1);
		features.add(f2);
		
		
		FeatureCollectionMemoryDriver driver = 
			new FeatureCollectionMemoryDriver("multipuntos", 
				                                   features,
				                                   definition);
		
		return  (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.createLayer("multipuntos",
																driver, 
																null);
	}
	
	public static FLyrVect createPointLyrWhichDontPasJtsValidRule(){
		LayerDefinition definition = createTestLayerDefinition();
		definition.setShapeType(FShape.POINT);
		FeatureCollectionMemoryDriver driver = null;
		
		Coordinate coord1 = new Coordinate(Double.NaN, Double.POSITIVE_INFINITY);
		Point pt1 = factory.createPoint(coord1);
		int index = 0;
		IFeature f1 = createTestFeature(pt1, index);
		index++;
		
		Coordinate coord2 = new Coordinate(100d, 100d);
		Point pt2 = factory.createPoint(coord2);
		IFeature f2 = createTestFeature(pt2, index);
		index++;
		
		Coordinate coord3 = new Coordinate(50d, 75d);
		Point pt3 = factory.createPoint(coord3);
		IFeature f3 = createTestFeature(pt3, index);
		index++;
		
		Coordinate coord4 = new Coordinate(Double.NaN, Double.NEGATIVE_INFINITY);
		Point pt4 = factory.createPoint(coord4);
		IFeature f4 = createTestFeature(pt4, index);
		index++;
		
		List<IFeature> features = new ArrayList<IFeature>();
		features.add(f1);
		features.add(f2);
		features.add(f3);
		features.add(f4);
		
		driver = new FeatureCollectionMemoryDriver("puntos no validos jts",
															features,
															definition);
		
		return  (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.createLayer("puntosJtsInvalidos",
								driver, 
								null);
	}
	
	
	public static FLyrVect createLineLyrWhichDontPasJtsValidRule(){
		LayerDefinition definition = createTestLayerDefinition();
		definition.setShapeType(FShape.LINE);
		FeatureCollectionMemoryDriver driver = null;
		
		Coordinate coord1 = new Coordinate(100d, 230.342d);
		Coordinate[] coords = {coord1, coord1};
		Geometry geom1 = factory.createLineString(coords);
		int index = 0;
		IFeature f1 = createTestFeature(geom1, index);
		
		Coordinate coord2 = new Coordinate(139d, 2021.0d);
		Coordinate[] coords2 = {coord1, coord2};
		Geometry geom2 = factory.createLineString(coords2);
		IFeature f2 = createTestFeature(geom2, index);
		
		Coordinate coord3 = new Coordinate(212d, Double.NaN);
		Coordinate[] coords3 = {coord2, coord3};
		Geometry geom3 = factory.createLineString(coords3);
		IFeature f3 = createTestFeature(geom3, index);
		
		List<IFeature> features = new ArrayList<IFeature>();
		features.add(f1);
		features.add(f2);
		features.add(f3);
		
		driver = new FeatureCollectionMemoryDriver("lineas no validos jts",
				features,
				definition);

		return  (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.
			createLayer("puntosJtsInvalidos",
							driver, 
							null);
	}

	//TODO Create test cases to probe SNAP functions
	public static FLyrVect createPolygonLayerWhichDontPassJtsValidRule() throws ParseException{
		LayerDefinition definition = createTestLayerDefinition();
		definition.setShapeType(FShape.POLYGON);
		FeatureCollectionMemoryDriver driver = null;
		List<IFeature> features = new ArrayList<IFeature>();
		int index = 0;
		
		//A polygon with a hole which shell has a self-intersection->TODO el paso jts->fmap->jts hace cosas raras
		Geometry pol1 = wktReader.read("POLYGON ((80 140, 360 60, 180 340, 200 60, 240 20, 80 140), (220 200, 220 140, 300 100, 260 180, 220 200))");
		IFeature f1 = createTestFeature(pol1, index);
		index++;
		
		
		//Non error polygon
		Geometry pol2 = wktReader.read("POLYGON ((200 260, 440 80, 500 360, 380 380, 200 260))");
		IFeature f2 = createTestFeature(pol2, index);
		index++;
		
		//Polygon with selintersecting hole
		Geometry pol3 = wktReader.read("POLYGON ((100 80, 240 20, 460 200, 400 380, 220 400, 60 360, 0 220, 100 80), (100 320, 380 220, 60 180, 320 340, 100 320))");
		IFeature f3 = createTestFeature(pol3, index);
		index++;
		
		//unclosed polygon Por definicion JTS si comprueba que los linearRing sean cerrados en su construccion
//		Geometry pol4 = wktReader.read("POLYGON ((200 260, 440 80, 500 360, 380 380))");
		GeneralPathX gpx = new GeneralPathX();
		gpx.moveTo(200, 260);
		gpx.lineTo(440, 80);
		gpx.lineTo(500, 360);
		gpx.lineTo(380, 380);
		FPolygon2D polygon = new FPolygon2D(gpx);
		IGeometry pol4 = ShapeFactory.createGeometry(polygon);
		StringValue value = ValueFactory.createValue("s4");
		Value[] values = {value};
		DefaultFeature f4 = new DefaultFeature(pol4, values, "id4");

		index++;
		
		//it isnt a polygon. its a line (three collinear points: collapsed polygon)
//		Geometry pol5 = wktReader.read("POLYGON ((200 260, 440 80, 200 260))");
//		IFeature f5 = createTestFeature(pol5, index);
		GeneralPathX gpx2 = new GeneralPathX();
		gpx2.moveTo(200, 260);
		gpx2.lineTo(440, 80);
		gpx2.lineTo(200, 260);
		FPolygon2D polygon2 = new FPolygon2D(gpx2);
		IGeometry pol5 = ShapeFactory.createGeometry(polygon2);
		StringValue value2 = ValueFactory.createValue("s5");
		Value[] values2 = {value2};
		DefaultFeature f5 = new DefaultFeature(pol5, values2, "id5");
		index++;
		
		//Polygon with shell points in CCW order
		Geometry pol6 = wktReader.read("POLYGON ((100 320, 380 220, 60 180, 320 340, 100 320))");
		IFeature f6 = createTestFeature(pol6, index);
		index++;
		
		//Polygon with hole in CCW order
		Geometry pol7 = wktReader.read("POLYGON ((0 0, 1000 0, 1000 1000, 0 1000, 0 0), (100 80, 240 20, 460 200, 400 380, 220 400, 60 360, 0 220, 100 80))");
		IFeature f7 = createTestFeature(pol7, index);
		index++;
		
		//Polygon with two holes that touch in more than a point
		Geometry pol8 = wktReader.read("POLYGON ((40 380, 40 20, 500 20, 500 380, 40 380),"+
						"(140 320, 160 200, 240 160, 400 220, 300 340, 140 320),"+
						"(300 140, 420 300, 460 100, 300 140))");
		
		IFeature f8 = createTestFeature(pol8, index);
		index++;
		
		//the hole is ccw
		Geometry pol9 = wktReader.read("POLYGON ((160 320, 740 80, 740 340, 400 380, 160 320), (440 260, 600 200, 660 300, 500 300, 440 260))");
		IFeature f9 = createTestFeature(pol9, index);
		index++;
		
		//hole is not full contained by shell
		Geometry pol10 = wktReader.read("POLYGON ((160 320, 740 80, 740 340, 400 380, 160 320), (440 260, 500 300, 660 300, 600 200, 440 260))");
		IFeature f10 = createTestFeature(pol10, index);
		index++;
		
		//shell and hole have the same coordinate sequence
		Geometry pol11 = wktReader.read("POLYGON((440 260, 600 200, 660 300, 500 300, 440 260),(440 260, 500 300, 660 300, 600 200, 440 260))");
		IFeature f11 = createTestFeature(pol11, index);
		index++;
		
		features.add(f1);
		features.add(f2);
		features.add(f3);
		features.add(f4);//jts no permite linearRing no cerrados (esto si lo chequea)
		features.add(f5);//jts no permite linearRings con < 3 puntos diferentes y no colineales
		features.add(f6);
		features.add(f7);
		features.add(f8);
		features.add(f9);
		features.add(f10);
		features.add(f11);
		
		driver = new FeatureCollectionMemoryDriver("poligonos no validos jts",
				features,
				definition);

		return  (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.
			createLayer("poligonos no validos jts",
							driver, 
							null);
			
	}
	
	
	public static FLyrVect createLyrForIGeometryMustBeClosedTest() throws ParseException{
		
		LayerDefinition definition = createTestLayerDefinition();
		definition.setShapeType(FShape.LINE);
		FeatureCollectionMemoryDriver driver = null;
		List<IFeature> features = new ArrayList<IFeature>();
		int index = 0;
		
		
		Geometry unclosedLine = wktReader.read("LINESTRING (280 80, 320 140, 360 220, 520 260, 560 200, 600 140, 500 80, 400 80, 260 60)");
		IFeature f1 = createTestFeature(unclosedLine, index++);
		
		Geometry overShootLine = wktReader.read("LINESTRING (140 220, 420 280, 320 380, 160 380, 80 300, 280 180)");
		IFeature f2 = createTestFeature(overShootLine, index++);
		
		features.add(f1);
		features.add(f2);
		
		driver = new FeatureCollectionMemoryDriver("lineas no cerradas",
				features,
				definition);
		
		return  (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.
		createLayer("lineas no cerradas",
						driver, 
						null);
		
	}
	
	public static FLyrVect createLyrForLineMustNotHavePseudonodesTest() throws ParseException{
		LayerDefinition definition = createTestLayerDefinition();
		definition.setShapeType(FShape.LINE);
		FeatureCollectionMemoryDriver driver = null;
		List<IFeature> features = new ArrayList<IFeature>();
		int index = 0;
		
		Geometry line1 = wktReader.read("LINESTRING (10 10, 100 10)");
		IFeature f1 = createTestFeature(line1, index++);
		
		//pseudonode is point (100 10)
		Geometry line2 = wktReader.read("LINESTRING (100.01 10, 200 10)");
		IFeature f2 = createTestFeature(line2, index++);
		
		Geometry line3 = wktReader.read("LINESTRING (200 10, 300 300)");
		IFeature f3 = createTestFeature(line3, index++);
		
		Geometry line4 = wktReader.read("LINESTRING (200 10, 100 -150)");
		IFeature f4 = createTestFeature(line4, index++);
		
		features.add(f1);
		features.add(f2);
		features.add(f3);
		features.add(f4);
		
		driver = new FeatureCollectionMemoryDriver("lineas con pseudonodo en 100 10",
				features,
				definition);
		
		return  (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.
		createLayer("lineas con pseudonodo en 100 10",
						driver, 
						null);
		
	}
	
	public static FLyrVect createPolygonForNoSelfIntersectTest() throws ParseException{
		LayerDefinition definition = createTestLayerDefinition();
		definition.setShapeType(FShape.POLYGON);
		FeatureCollectionMemoryDriver driver = null;
		List<IFeature> features = new ArrayList<IFeature>();
		int index = 0;
		
		Geometry line1 = wktReader.read("POLYGON ((-13900 -920, -8300 120, -5020 3460, -9480 5660, -15320 2620, -19420 4960, -19300 7600, -14620 8500, -13180 6840, -16380 5460, -22020 6280, -22340 3060, -18860 2120, -14820 4880, -10200 4460, -8680 2640, -8240 1780, -4420 -420, -5140 -2560, -9480 -3640, -12940 -2740, -13900 -920))");
		IFeature f1 = createTestFeature(line1, index++);
		features.add(f1);
		driver = new FeatureCollectionMemoryDriver("poligonos con autointersecciones",
				features,
				definition);
		
		return  (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.
		createLayer("poligonos con autointersecciones",
						driver, 
						null);
	}
	
	public static FLyrVect createPolygonForMustNotOverlapTest() throws ParseException{
		LayerDefinition definition = createTestLayerDefinition();
		definition.setShapeType(FShape.POLYGON);
		FeatureCollectionMemoryDriver driver = null;
		List<IFeature> features = new ArrayList<IFeature>();
		int index = 0;
		
		Geometry poly1 = wktReader.read("POLYGON ((290 340, 180 280, 610 230, 720 330, 530 400, 300 380, 290 340))");
		IFeature f1 = createTestFeature(poly1, index++);
		features.add(f1);
		
		Geometry poly2 = wktReader.read("POLYGON ((180 210, 550 210, 200 390, 180 210))");
		IFeature f2 = createTestFeature(poly2, index++);
		features.add(f2);
		
		Geometry poly3 = wktReader.read("POLYGON ((820 180, 820 30, 980 30, 1000 180, 820 180))");
		IFeature f3 = createTestFeature(poly3, index++);
		features.add(f3);
		
		
		
		driver = new FeatureCollectionMemoryDriver("poligonos que solapan",
				features,
				definition);
		
		return  (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.
		createLayer("poligonos que solapan",
						driver, 
						null);
	}
	
	public static FLyrVect createPolygonForMustNotHaveGapsTest() throws ParseException{
		FLyrVect solution = null;
		
		LayerDefinition definition = createTestLayerDefinition();
		definition.setShapeType(FShape.POLYGON);
		
		FeatureCollectionMemoryDriver driver = null;
		List<IFeature> features = new ArrayList<IFeature>();
		int index = 0;
		
		Geometry poly1 = wktReader.read("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))");
		IFeature f1 = createTestFeature(poly1, index++);
		features.add(f1);
		
		Geometry poly2 = wktReader.read("POLYGON ((1 0, 2 0, 2 1, 1 1, 1 0))");
		IFeature f2 = createTestFeature(poly2, index++);
		features.add(f2);
		
		Geometry poly3 = wktReader.read("POLYGON ((2 0, 3 0, 3 1, 2 1, 2 0))");
		IFeature f3 = createTestFeature(poly3, index++);
		features.add(f3);
		
		
		Geometry poly4 = wktReader.read("POLYGON ((0 1, 1 1, 1 2, 0 2, 0 1))");
		IFeature f4 = createTestFeature(poly4, index++);
		features.add(f4);
		
		
		Geometry poly6 = wktReader.read("POLYGON ((2 1, 3 1, 3 2, 2 2, 2 1))");
		IFeature f6 = createTestFeature(poly6, index++);
		features.add(f6);
		
		Geometry poly7 = wktReader.read("POLYGON ((0 2, 1 2, 1 3, 0 3, 0 2))");
		IFeature f7 = createTestFeature(poly7, index++);
		features.add(f7);
		
		Geometry poly8 = wktReader.read("POLYGON ((1 2, 2 2, 2 3, 1 3, 1 2))");
		IFeature f8 = createTestFeature(poly8, index++);
		features.add(f8);
		
		Geometry poly9 = wktReader.read("POLYGON ((2 2, 3 2, 3 3, 2 3, 2 2))");
		IFeature f9 = createTestFeature(poly9, index++);
		features.add(f9);
		
		driver = new FeatureCollectionMemoryDriver("poligonos 1",
				features,
				definition);
		
		solution =  (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.createLayer("poligonos que solapan",
						driver, 
						null);
		return solution;
	}
	
	
	public static FLyrVect[] createPolygonForMustNotOverlapWithTest() throws ParseException{
		
		FLyrVect[] solution = new FLyrVect[2];
		
		LayerDefinition definition = createTestLayerDefinition();
		definition.setShapeType(FShape.POLYGON);
		
		FeatureCollectionMemoryDriver driver = null;
		List<IFeature> features = new ArrayList<IFeature>();
		int index = 0;
		
		
		Geometry poly1 = wktReader.read("POLYGON ((290 340, 180 280, 610 230, 720 330, 530 400, 300 380, 290 340))");
		IFeature f1 = createTestFeature(poly1, index++);
		features.add(f1);
		
		Geometry poly3 = wktReader.read("POLYGON ((820 180, 820 30, 980 30, 1000 180, 820 180))");
		IFeature f3 = createTestFeature(poly3, index++);
		features.add(f3);
		driver = new FeatureCollectionMemoryDriver("poligonos 1",
				features,
				definition);
		
		solution[0] =  (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.
		createLayer("poligonos que solapan",
						driver, 
						null);
		
		
		
		FeatureCollectionMemoryDriver driver2 = null;
		ArrayList<IFeature> features2 = new ArrayList<IFeature>();
		index = 0;
		Geometry poly11= wktReader.read("POLYGON ((20 210, 30 50, 320 30, 330 180, 20 210))");
		IFeature f11 = createTestFeature(poly11, index++);
		features2.add(f11);
		
		Geometry poly22= wktReader.read("POLYGON ((880 380, 920 230, 1000 210, 840 10, 880 380))");
		IFeature f22 = createTestFeature(poly22, index++);
		features.add(f22);
		driver2 = new FeatureCollectionMemoryDriver("poligonos 1",
				features2,
				definition);
		
		solution[1] =  (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.
		createLayer("poligonos que solapan",
						driver2, 
						null);
		
		return solution;
	}
	
	private static LayerDefinition createTestLayerDefinition() {
		FieldDescription fieldDescription = new FieldDescription();
		fieldDescription.setFieldName("str1");
		fieldDescription.setFieldType(FieldDescription.stringToType("String"));
		fieldDescription.setFieldLength(10);
		FieldDescription[] fields = {fieldDescription};
		LayerDefinition definition = new LayerDefinition();
		definition.setFieldsDesc(fields);
		return definition;
	}
	
	private static IFeature createTestFeature(Geometry geometry, int index){
		StringValue value = ValueFactory.createValue("s"+index);
		Value[] values = {value};
		IGeometry iln1 = NewFConverter.toFMap(geometry);
		DefaultFeature f1 = new DefaultFeature(iln1, values, "id"+index);
		return f1;
	}
	
	
	public static FLyrVect createLayerFor(List<IGeometry> geometries, 
												final int shapeType){
		List<IFeature> features = new ArrayList<IFeature>();
		
		for(int i = 0; i < geometries.size(); i++){
			IGeometry geometry = geometries.get(i);
			Value[] values = new Value[1];
			values[0] = ValueFactory.createValue(0d);
			
			DefaultFeature f = new 
				DefaultFeature(geometry, values, i+"");
			features.add(f);
			
		}
		
		LayerDefinition def = new LayerDefinition(){
			public int getShapeType(){
				return shapeType;
			}	
		};
		def.setFieldsDesc(new FieldDescription[]{});
		FeatureCollectionMemoryDriver driver = 
			new FeatureCollectionMemoryDriver("", features, def);
		FLyrVect lyr = (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.
											createLayer("",
													driver, 
													null);
		
		return lyr;
	}
	

}

