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
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.topology.SimpleTopologyErrorContainer;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.topologyrules.LineMustNotHavePseudonodes;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.drivers.FeatureCollectionMemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.LayerDefinition;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Test for remove pseudonode fix.
 * @author Alvaro Zabala
 *
 */
public class RemovePseudoNodeFixTest extends TestCase {
	GeometryFactory factory = new GeometryFactory();
	WKTReader wktReader = new WKTReader(factory);
	
	
	public void tearDown() throws Exception{
		super.tearDown();
		factory = null;
		wktReader = null;
		System.gc();
	}
	
	public void test1() throws ParseException, BaseException{
		String wkt1 = "LINESTRING (200 80, 240 160, 300 240, 400 240)";
		String wkt2 = "LINESTRING (400 240, 440 220, 460 240, 440 280)";
		
		Geometry g1 = wktReader.read(wkt1);
		Geometry g2 = wktReader.read(wkt2);
		
		IGeometry ig1 = NewFConverter.toFMap(g1);
		IGeometry ig2 = NewFConverter.toFMap(g2);
		
		Value[] values1 = new Value[1];
		values1[0] = ValueFactory.createValue(5d);
		
		Value[] values2 = new Value[1];
		values2[0] = ValueFactory.createValue(199d);
		
		DefaultFeature f1 = new DefaultFeature(ig1, values1, "0");
		DefaultFeature f2 = new DefaultFeature(ig2, values2, "1");
		
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
		LineMustNotHavePseudonodes violatedRule = new LineMustNotHavePseudonodes(topo, lyr, 0.1d);
		FGeometry fgeometry =  (FGeometry) ShapeFactory.createPoint2D(400, 240);
		TopologyError error = new TopologyError(fgeometry, violatedRule, f1, topo);
		
		new RemovePseudoNodeFix().fix(error);
		
	}
}
