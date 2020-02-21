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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.gvsig.exceptions.BaseException;
import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.SimpleTopologyErrorContainer;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.topologyrules.LineMustNotHaveDangles;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.drivers.FeatureCollectionMemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.LayerDefinition;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class ExtendDangleFixTest extends TestCase {
	
	public void testExtendSegment(){
		
		Coordinate c0 = new Coordinate(0, 0);
		Coordinate c1 = new Coordinate(1, 1);
		
		LineSegment segment = new LineSegment(c0, c1);
		
		Coordinate newCoord = JtsUtil.extentLineSegment(segment, 50d);
		
		double length = c0.distance(newCoord);
		
//		assertTrue(length == 51);
		
		Coordinate c2 = new Coordinate(-1, -1);
		segment = new LineSegment(c0, c2);
		newCoord = JtsUtil.extentLineSegment(segment, 50d);
		
		length = c0.distance(newCoord);
		
		assertTrue(length >= 51);
		
		
	}
	
	public void testExtendDangleFix() throws BaseException{
		
		GeneralPathX gpx = new GeneralPathX();
		gpx.moveTo(190, 200);
		gpx.lineTo(260, 130);
		gpx.lineTo(360, 140);
		gpx.lineTo(420, 180);
		gpx.lineTo(470, 220);
		gpx.lineTo(530, 240);
		FGeometry geometry = (FGeometry) ShapeFactory.createPolyline2D(gpx);
		
		
		GeneralPathX gpx2 = new GeneralPathX();
		gpx2.moveTo(860, 70);
		gpx2.lineTo(970, 120);
		gpx2.lineTo(1000, 220);
		gpx2.lineTo(920, 300);
		//gpx2.lineTo(470, 220); Cuando varios rectangulos contienen un mismo punto, 
		//el operador NNearest solo devuelve el primero que se insertara en el indice
		gpx2.lineTo( 1000, 370);
		FGeometry geometry2 = (FGeometry) ShapeFactory.createPolyline2D(gpx2);
		
		double searchRadius = 400d;
		
		Value[] values1 = new Value[1];
		values1[0] = ValueFactory.createValue(5d);
		
		Value[] values2 = new Value[1];
		values2[0] = ValueFactory.createValue(199d);
		
		DefaultFeature f1 = new DefaultFeature(geometry, values1, "0");
		DefaultFeature f2 = new DefaultFeature(geometry2, values2, "1");
		
		List<IFeature> features = new ArrayList<IFeature>();
		features.add(f1);
		features.add(f2);
		LayerDefinition def = new LayerDefinition(){
			public int getShapeType(){
				return FShape.LINE;
			}	
			
		};
		def.setFieldsDesc(new FieldDescription[]{});
		FeatureCollectionMemoryDriver driver = new FeatureCollectionMemoryDriver("", features, def);
		FLyrVect lyr = (FLyrVect) com.iver.cit.gvsig.fmap.layers.LayerFactory.
											createLayer("",
													driver, 
													null);
		
		Topology topo = new Topology(null, null, 0.2d, 0, new SimpleTopologyErrorContainer());
		
		LineMustNotHaveDangles violatedRule = new LineMustNotHaveDangles(topo, lyr, 0.1d);
		
		FGeometry dangleGeometry  =  (FGeometry) ShapeFactory.createPoint2D(530, 240);
		TopologyError error = new TopologyError(dangleGeometry, violatedRule, f1, topo);
		topo.addTopologyError(error);
		
		new ExtendDangleToNearestVertexFix(searchRadius).fix(error);
		
		assertTrue(topo.getNumberOfErrors() == 0);
		
		f1.setGeometry(geometry);
		topo.addTopologyError(error);
		
		new ExtendDangleToNearestBoundaryPointFix(searchRadius).fix(error);
		
		assertTrue(topo.getNumberOfErrors() == 0);
		
		
		
		
		
	}
}
