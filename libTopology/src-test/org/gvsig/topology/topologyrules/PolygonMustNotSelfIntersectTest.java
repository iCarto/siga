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

import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.util.LayerFactory;
import org.gvsig.topology.util.TestTopologyErrorContainer;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.io.ParseException;

import junit.framework.TestCase;

public class PolygonMustNotSelfIntersectTest extends TestCase {
		
		public void testSelfIntersect() throws ParseException, TopologyRuleDefinitionException{
			FLyrVect lyr = LayerFactory.createPolygonForNoSelfIntersectTest();
			PolygonMustNotSelfIntersect rule = new PolygonMustNotSelfIntersect(null, lyr, 0.5d);
			TestTopologyErrorContainer errorContainer = new TestTopologyErrorContainer();
			rule.setTopologyErrorContainer(errorContainer);
			rule.checkPreconditions();
			rule.checkRule();
			int numberOfErrors = errorContainer.getNumberOfErrors();
			assertTrue(numberOfErrors == 1);
			
			
			
		}
}
