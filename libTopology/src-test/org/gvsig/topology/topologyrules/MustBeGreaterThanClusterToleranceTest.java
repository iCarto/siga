/*
 * Created on 26/09/2007
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
package org.gvsig.topology.topologyrules;

import junit.framework.TestCase;

import org.gvsig.jts.GeometryCollapsedException;
import org.gvsig.jts.GeometrySnapper;
import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.util.LayerFactory;
import org.gvsig.topology.util.TestTopologyErrorContainer;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class MustBeGreaterThanClusterToleranceTest extends TestCase {


	public void testLineLayerWithCollapsedCoords() throws ParseException{
		
		FLyrVect lyr = LayerFactory.getLineLayerWithCollapsedCoords();
		MustBeLargerThanClusterTolerance rule = new MustBeLargerThanClusterTolerance(lyr, 11d);
		TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
		rule.setTopologyErrorContainer(errorContainer);
		try {
			rule.checkPreconditions();
			rule.checkRule();
			assertTrue(errorContainer.getNumberOfErrors() == 1);
		} catch (TopologyRuleDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		GeometrySnapper snapper = new GeometrySnapper(11d);
		WKTReader reader = new WKTReader(JtsUtil.GEOMETRY_FACTORY);
		Geometry jtsGeo = reader.read("POLYGON((10 10, 15 10, 15 15, 10 15, 10 10))");
		try {
			Geometry newGeo = snapper.snap(jtsGeo);
		} catch (GeometryCollapsedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		snapper = new GeometrySnapper(4.5d);
		try {
			snapper.snap(jtsGeo);
		} catch (GeometryCollapsedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

