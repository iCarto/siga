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

import junit.framework.TestCase;

import org.gvsig.topology.util.TestTopologyErrorContainer;

import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;

public class PolygonMustBeClosedTest extends TestCase {
	
	PrecisionModel pm = new PrecisionModel(10000);
	GeometryFactory factory = new GeometryFactory(pm);
	WKTReader wktReader = new WKTReader(factory);
	
	
	IGeometry polygon1;
	
	FMapGeometryMustBeClosed rule;
	
	public void setUp() throws Exception{
		super.setUp();
		
		GeneralPathX gp = new GeneralPathX();
		gp.moveTo(320d,240d);
		gp.lineTo(300d, 160d);
		gp.lineTo(400d,80d);
		gp.lineTo(540d,40d); 
		gp.lineTo(660d,100d); 
		gp.lineTo(660d,160d);
		gp.lineTo(680d,220d);
		gp.lineTo(660d,280d);
		gp.lineTo(580,320d);
		gp.lineTo(500d,320d);
		gp.lineTo(440d,340d);
		gp.lineTo(340d,340d);
		gp.lineTo(280d,280d);
		
		polygon1 = ShapeFactory.createPolygon2D(gp);
		
//		rule = new PolygonMustBeClosed(null, null, 0d);
		
		rule = new FMapGeometryMustBeClosed(null, null, 0d);
		TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		
		
	}
	
	public void tearDown() throws Exception{
		super.tearDown();
	}
	
	public void test1(){
		
		rule.validateFeature(new DefaultFeature(polygon1, null));
	}
}
