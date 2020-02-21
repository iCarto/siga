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
package org.gvsig.topology.topologyrules;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.gvsig.fmap.core.MultipartShapeIterator;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.fmap.core.ShapePointExtractor;
import org.gvsig.topology.topologyrules.jtsisvalidrules.GeometryMustNotHaveFewPoints;
import org.gvsig.topology.topologyrules.jtsisvalidrules.IGeometryMustBeClosed;
import org.gvsig.topology.topologyrules.jtsisvalidrules.PolygonHolesMustBeInShell;
import org.gvsig.topology.topologyrules.jtsisvalidrules.PolygonHolesMustNotBeNested;
import org.gvsig.topology.topologyrules.jtsisvalidrules.PolygonMustNotHaveDuplicatedRings;
import org.gvsig.topology.topologyrules.jtsisvalidrules.PolygonMustNotHaveSelfIntersectedRings;
import org.gvsig.topology.util.LayerFactory;
import org.gvsig.topology.util.TestTopologyErrorContainer;

import com.hardcode.gdbms.engine.values.StringValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;



public class JtsValidRuleTest extends TestCase {
	
	static PrecisionModel pm = new PrecisionModel(10000);
	static GeometryFactory factory = new GeometryFactory(pm);
	static WKTReader wktReader = new WKTReader(factory);
	
	public void testJtsInvalidPointLayer() {
		FLyrVect pointLyr = LayerFactory.createPointLyrWhichDontPasJtsValidRule();
		JtsValidRule rule = new JtsValidRule(pointLyr, 0d);
		TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		rule.checkPreconditions();
		rule.checkRule();
		int numberOfErrors = errorContainer.getNumberOfErrors();
		assertTrue(numberOfErrors == 2);
	}
	
	
	public void testJtsInvalidLineLayer(){
		/*
		 * This line layer has two JTS errors:
		 * A line has invalid coords.
		 * Another line has two repeated points (the same point)
		 * */
		FLyrVect lineLyr = LayerFactory.createLineLyrWhichDontPasJtsValidRule();
		JtsValidRule rule = new JtsValidRule(lineLyr, 0d);
		TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		rule.checkPreconditions();
		rule.checkRule();
		int numberOfErrors = errorContainer.getNumberOfErrors();
		assertTrue(numberOfErrors == 2);
	}
	
	
	public void testPolygonMustNotHaveSelfIntersectedRing() throws ParseException{
		//A polygon with a hole which shell has a self-intersection
		Geometry pol1 = wktReader.read("POLYGON ((80 140, 360 60, 180 340, 200 60, 240 20, 80 140), (220 200, 220 140, 300 100, 260 180, 220 200))");
		StringValue value = ValueFactory.createValue("s0");
		Value[] values = {value};
		IGeometry iln1 = FConverter.jts_to_igeometry(pol1);
		DefaultFeature f1 = new DefaultFeature(iln1, values, "id0");
		PolygonMustNotHaveSelfIntersectedRings rule = new 
			PolygonMustNotHaveSelfIntersectedRings(null, 0d);
		TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		rule.validateFeature(f1);
		assertTrue(errorContainer.getNumberOfErrors() == 1);
		
		//polygon with a self intersecting hole
		Geometry pol2 = wktReader.read("POLYGON ((100 80, 240 20, 460 200, 400 380, 220 400, 60 360, 0 220, 100 80), (100 320, 380 220, 60 180, 320 340, 100 320))");
		StringValue value2 = ValueFactory.createValue("s0");
		Value[] values2 = {value2};
		IGeometry iln2 = FConverter.jts_to_igeometry(pol2);
		DefaultFeature f2 = new DefaultFeature(iln2, values2, "id2");
		errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		rule.validateFeature(f2);
		assertTrue(errorContainer.getNumberOfErrors() == 1);
		
	}
	
	
	public void testMultiPartShapeIterator(){
		GeneralPathX gpx = new GeneralPathX();
		gpx.moveTo(200, 260);
		gpx.lineTo(440, 80);
		gpx.lineTo(500, 360);
		gpx.lineTo(380, 380);
		gpx.moveTo(1000, 1000);
		gpx.lineTo(1200, 1200);
		MultipartShapeIterator it = new MultipartShapeIterator(gpx);
		Iterator<Shape> shapeIt = it.getShapeIterator();
		int i = 0;
		while(shapeIt.hasNext()){
			Shape shape = shapeIt.next();
			List<Point2D[]> coordParts = ShapePointExtractor.extractPoints(shape);
			if(i == 0)
				assertTrue(coordParts.get(0).length == 4);
			else if(i == 1)
				assertTrue(coordParts.get(0).length == 2);
			else
				assertTrue(false);//no deberia llegar aquí
			i++;
		}//while
	}
	
	
	public void testMultiPartShapeIteratorWithCurves(){
		GeneralPathX gpx = new GeneralPathX();
		gpx.moveTo(100, 100);
		gpx.curveTo(250, 250, 300, 100, 500, 500);
		gpx.moveTo(1000, 1000);
		gpx.quadTo(1500, 1000, 2500, 2000);
		MultipartShapeIterator it = new MultipartShapeIterator(gpx);
		Iterator<Shape> shapeIt = it.getShapeIterator();
		int i = 0;
		List<Shape> shapes = new ArrayList<Shape>();
		while(shapeIt.hasNext()){
			Shape shape = shapeIt.next();
			shapes.add(shape);
			i++;
		}
		assertTrue(i == 2);
		
		Shape shape = shapes.get(0);
		List<Point2D[]> coordParts = ShapePointExtractor.extractPoints(shape);
		Point2D[] part1 = coordParts.get(0);
		assertTrue(part1.length != 4);//1 point + 3 control points, but extractPoints returns computed curves
		
		Shape shape2 = shapes.get(1);
		List<Point2D[]> coordParts2 = ShapePointExtractor.extractPoints(shape2);
		Point2D[] part2 = coordParts2.get(0);
		assertTrue(part2.length != 3);//1 point + 3 control points, but extractPoints returns computed curves
		
		
	}
	
	
	public void testLinearRingMustBeClosed() throws ParseException{
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
		IGeometryMustBeClosed rule = new IGeometryMustBeClosed(null, 0d);
		TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		rule.validateFeature(f4);
		assertTrue(errorContainer.getNumberOfErrors() == 1);
		
	}
	
	public void testMustNotHaveFewPoints(){
		GeneralPathX gpx2 = new GeneralPathX();
		gpx2.moveTo(200, 260);
		gpx2.lineTo(440, 80);
		gpx2.lineTo(200, 260);
		FPolygon2D polygon2 = new FPolygon2D(gpx2);
		IGeometry pol5 = ShapeFactory.createGeometry(polygon2);
		StringValue value2 = ValueFactory.createValue("s5");
		Value[] values2 = {value2};
		DefaultFeature f5 = new DefaultFeature(pol5, values2, "id5");
		GeometryMustNotHaveFewPoints rule = new GeometryMustNotHaveFewPoints(null);
		TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		rule.validateFeature(f5);
		assertTrue(errorContainer.getNumberOfErrors() == 1);
	}
	
	public void testPolygonHolesMustNotBeNested() throws ParseException{
		Geometry pol = wktReader.read("POLYGON ((40 380, 40 20, 500 20, 500 380, 40 380),"+
				                      "(140 320, 300 340, 400 220, 240 160, 160 200, 140 320)," +
				                      "(300 140, 420 300, 460 100, 300 140))");
		IGeometry geometry = FConverter.jts_to_igeometry(pol);
		MultiPolygon multiPolygon = NewFConverter.toJtsPolygon((FShape)geometry.getInternalShape());
		geometry = FConverter.jts_to_igeometry(multiPolygon);
		StringValue value = ValueFactory.createValue("s1");
		Value[] values = {value};
		DefaultFeature f = new DefaultFeature(geometry, values, "id1");
		PolygonHolesMustNotBeNested rule = new PolygonHolesMustNotBeNested(null);
		TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		rule.validateFeature(f);
		assertTrue(errorContainer.getNumberOfErrors() == 1);
	}
	
	public void testPolygonHolesMustBeInShell() throws ParseException{
		Geometry pol = wktReader.read("POLYGON ((300 320, 240 220, 500 80, 660 300, 580 360, 300 320),"+ 
				"(300 260, 340 120, 520 160, 520 300, 300 260))");
		IGeometry geometry = FConverter.jts_to_igeometry(pol);
		StringValue value = ValueFactory.createValue("s1");
		Value[] values = {value};
		DefaultFeature f = new DefaultFeature(geometry, values, "id1");
	
		PolygonHolesMustBeInShell rule = new PolygonHolesMustBeInShell(null, 0.01d);
		TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		rule.validateFeature(f);
		assertTrue(errorContainer.getNumberOfErrors() == 1);
	}
	
	
	public void testPolygonMustNotHaveDuplicatedRings() throws ParseException{
		Geometry pol = wktReader.read("POLYGON((440 260, 600 200, 660 300, 500 300, 440 260),(440 260, 500 300, 660 300, 600 200, 440 260))");
		IGeometry geometry = FConverter.jts_to_igeometry(pol);
		StringValue value = ValueFactory.createValue("s1");
		Value[] values = {value};
		DefaultFeature f = new DefaultFeature(geometry, values, "id1");
		PolygonMustNotHaveDuplicatedRings rule = new PolygonMustNotHaveDuplicatedRings(null, 0.01d);
		TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		rule.validateFeature(f);
		assertTrue(errorContainer.getNumberOfErrors() == 1);
		
	}
	
	public void testJtsInvalidPolygonLayer() throws ParseException{
		FLyrVect polLyr = LayerFactory.createPolygonLayerWhichDontPassJtsValidRule();
		JtsValidRule rule = new JtsValidRule(polLyr, 0d);
		TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		rule.checkPreconditions();
		rule.checkRule();
		int numberOfErrors = errorContainer.getNumberOfErrors();
//		assertTrue(numberOfErrors == 8);
//		TopologyError error0 = errorContainer.getTopologyError(0);
//		assertTrue(error0.getViolatedRule() instanceof PolygonMustNotHaveSelfIntersectedRings);
		
	}
	
	
	
	
	

}
