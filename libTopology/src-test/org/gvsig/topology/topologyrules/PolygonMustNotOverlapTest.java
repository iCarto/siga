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

import java.util.ArrayList;
import java.util.List;

import org.cresques.cts.IProjection;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jcs.JCSUtil;
import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.util.LayerFactory;
import org.gvsig.topology.util.TestTopologyErrorContainer;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.crs.CRSFactory;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

import junit.framework.TestCase;

public class PolygonMustNotOverlapTest extends TestCase {
	
	IProjection PROJECTION_DEFAULT = CRSFactory.getCRS("EPSG:23030");
	public void testDontOverlaps() throws ParseException, TopologyRuleDefinitionException{
		FLyrVect lyrWithOverlaps = 
			LayerFactory.createPolygonForMustNotOverlapTest();
		lyrWithOverlaps.setProjection(PROJECTION_DEFAULT);
		
		PolygonMustNotOverlap rule = 
			new PolygonMustNotOverlap(null, lyrWithOverlaps, 0.1d);
		
		TestTopologyErrorContainer errorContainer = 
			new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		rule.checkPreconditions();
		rule.checkRule();

		int numberOfErrors = errorContainer.getNumberOfErrors();
		assertTrue(numberOfErrors == 1);
		
		errorContainer.clear();
		FLyrVect lyr2 = LayerFactory.createPolygonForMustNotHaveGapsTest();
		lyr2.setProjection(PROJECTION_DEFAULT);
		PolygonMustNotHaveGaps rule2 = new PolygonMustNotHaveGaps(null,lyr2 , 0.1d);
		rule2.setTopologyErrorContainer(errorContainer);
		rule2.checkPreconditions();
		rule2.checkRule();
		
		numberOfErrors = errorContainer.getNumberOfErrors();
		
		JCSUtil.removeGapsFromPolygonCoverage(lyr2, 0.01d, 22.5d);
		
		IFeatureIterator iterator;
		List<Geometry> geometries = new ArrayList<Geometry>();
		Geometry union = null;
		try {
			iterator = lyr2.getSource().getFeatureIterator();
			while(iterator.hasNext()){
				IFeature feature = iterator.next();
				Geometry fGeo = NewFConverter.toJtsGeometry(feature.getGeometry());
				geometries.add(fGeo);
				
				if(union == null)
					union = fGeo;
				else
					union = EnhancedPrecisionOp.union(union, fGeo);
				
			}
			System.out.println(union);
			GeometryCollection geomCol = JtsUtil.GEOMETRY_FACTORY.createGeometryCollection(geometries.toArray(new Geometry[0]));
			System.out.println(geomCol);
			Geometry buffer = geomCol.buffer(0d);
			System.out.println(buffer);
			Geometry gaps = EnhancedPrecisionOp.difference(buffer, union);
			System.out.println(gaps);
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
