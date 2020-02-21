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

import org.gvsig.exceptions.BaseException;
import org.gvsig.topology.SimpleTopologyErrorContainer;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.topologyrules.LineMustNotSelfIntersect;
import org.gvsig.topology.topologyrules.PolygonMustNotSelfIntersect;
import org.gvsig.topology.util.LayerFactory;

import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import junit.framework.TestCase;

public class SplitSelfIntersectingPolygonFixTest extends TestCase {
//	 220 260, 260 200)
//	LINESTRING (140 120, 100 140, 100 240, 220 220, 200 140, 60 180, 60 140, 100 100, 180 80, 140 120)
	
public void testSplitSelfFix() throws BaseException{
		
		GeneralPathX gpx = new GeneralPathX();
		gpx.moveTo(260, 200);
		gpx.lineTo(280, 80);
		gpx.lineTo(600, 60);
		gpx.lineTo(680, 180);
		gpx.lineTo(660, 340);
		gpx.lineTo(520, 180);
		gpx.lineTo(380, 20);
		gpx.lineTo(180, 20);
		gpx.lineTo(0, 160);
		gpx.lineTo(60, 260);
		gpx.lineTo(220, 260);
//		gpx.lineTo(260, 200);
		gpx.closePath();
		
		gpx.moveTo(140, 120);
		gpx.lineTo(100, 140);
		gpx.lineTo(100, 240);
		gpx.lineTo(220, 220);
		gpx.lineTo(200, 140);
		gpx.lineTo(60, 180);
		gpx.lineTo(60, 140);
		gpx.lineTo(100, 100);
		gpx.lineTo(180, 80);
//		gpx.lineTo(140, 120);
		gpx.closePath();
		
			
		
		IGeometry geometry = ShapeFactory.createPolygon2D(gpx);
	
		List<IGeometry> geoms = new ArrayList<IGeometry>();
		geoms.add(geometry);
		
		FLyrVect lyr = LayerFactory.createLayerFor(geoms, FShape.POLYGON);
		
		
		Topology topo = new Topology(null, null, 0.2d, 0, new SimpleTopologyErrorContainer());
		PolygonMustNotSelfIntersect violatedRule = new PolygonMustNotSelfIntersect(topo, lyr, 0.1d);
		violatedRule.setTopologyErrorContainer(topo);
		violatedRule.checkRule();
		
		TopologyError error = topo.getTopologyError(0);
		
		
		new SplitSelfIntersectingPolygonFix().fix(error);
		
		assertTrue(topo.getNumberOfErrors() == 0);
	}
}
