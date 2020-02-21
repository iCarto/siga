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
import org.gvsig.topology.SimpleTopologyErrorContainer;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.topologyrules.PolygonMustNotOverlap;
import org.gvsig.topology.util.LayerFactory;

import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.io.ParseException;

public class PolygonsMustNotOverlapsFixesTest extends TestCase {
	
	public void testPolygon() throws ParseException, BaseException{
		
//POLYGON ((260 330, 210 180, 440 70, 650 210, 560 360, 380 330, 350 390, 260 330))
//POLYGON ((580 50, 450 180, 500 290, 710 360, 930 260, 930 130, 810 240, 610 140, 580 50))		
		GeneralPathX gpx = new GeneralPathX();
		gpx.moveTo(260, 330);
		gpx.lineTo(210,180);
		gpx.lineTo(440, 70);
		gpx.lineTo(650, 210);
		gpx.lineTo(560, 360);
		gpx.lineTo(380,330);
		gpx.lineTo(350, 390);
		gpx.closePath();
		FGeometry geometry = (FGeometry) ShapeFactory.createPolygon2D(gpx);
		
		
	    GeneralPathX gpx2 = new GeneralPathX();
	    gpx2.moveTo(580, 50);
	    gpx2.lineTo(450, 180);
	    gpx2.lineTo(500, 290); 
	    gpx2.lineTo(710, 360); 
	    gpx2.lineTo(930, 260); 
	    gpx2.lineTo(930, 130);  
	    gpx2.lineTo(810, 240); 
	    gpx2.lineTo(610, 140); 
	    gpx2.closePath();
	    FGeometry geometry2 = (FGeometry) ShapeFactory.createPolygon2D(gpx2);
	    
	    List<IGeometry> geoms = new ArrayList<IGeometry>();
		geoms.add(geometry);
		geoms.add(geometry2);
		
		FLyrVect lyr = LayerFactory.createLayerFor(geoms, FShape.POLYGON);
		
		
		Topology topo = new Topology(null, null, 0.2d, 0, new SimpleTopologyErrorContainer());
		PolygonMustNotOverlap violatedRule = new PolygonMustNotOverlap(topo, lyr, 0.1d);
		violatedRule.setTopologyErrorContainer(topo);
		violatedRule.checkRule();
		
		TopologyError error = topo.getTopologyError(0);
		
		
		new SubstractOverlapPolygonFix().fix(error);
		topo.getErrorContainer().addTopologyError(error);
		MergeOverlapPolygonFix fix = new MergeOverlapPolygonFix();
		fix.initialize(error);
		fix.setParameterValue("featureToPreserve",lyr.getSource().getFeature(0));
		topo.getErrorContainer().addTopologyError(error);
		new CreateFeatureOverlapPolygonFix().fix(error);
		
		assertTrue(topo.getNumberOfErrors() == 0);
		
		
				
	}
}
