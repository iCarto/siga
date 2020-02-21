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

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;

import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;
/**
 * All geometries of origin layer must be covered by a group
 * of geometries of destination layer
 * (it doesnt matter if are covered by one only geometries or a
 * group of them)
 * 
 * @author Alvaro Zabala
 *
 */
public class LyrMustBeCoveredByLyr extends LyrMustBeCoveredByOneGeometry {

	static final String RULE_NAME = Messages.getText("must_be_covered_by_all_lyr");
	
	static {
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
	}
	
	public LyrMustBeCoveredByLyr(Topology topology, 
			 FLyrVect originLyr,
			 FLyrVect destinationLyr){
		super(topology, originLyr, destinationLyr);
	}


	public LyrMustBeCoveredByLyr(){
		super();
	}
	
	@Override
	protected void checkWithNeighbourhood(IFeature feature, Rectangle2D extendedBounds, IFeatureIterator neighbourhood) throws BaseException{
		Geometry firstGeometry = NewFConverter.toJtsGeometry(feature.getGeometry());
		List<Geometry> neighboursGeo = new ArrayList<Geometry>();
		while (neighbourhood.hasNext()) {
			IFeature neighbourFeature = neighbourhood.next();
			IGeometry geom2 = neighbourFeature.getGeometry();
			if(acceptsDestinationGeometryType(geom2.getGeometryType())){
				Rectangle2D rect2 = geom2.getBounds2D();
				if (extendedBounds.intersects(rect2)) {
					Geometry jtsGeom2 = NewFConverter.toJtsGeometry(geom2);
					if(!checkSpatialPredicate(feature, firstGeometry, 
														neighbourFeature, jtsGeom2 )){
						neighboursGeo.add(jtsGeom2);
					}else
						return;//checks the rule
				}//if
			}//if
		}//while
		//at this point, feature is not covered by one neighbour feature
		//we check for all the neighbourhood
		Geometry[] geometries = neighboursGeo.toArray(new Geometry[0]);
		GeometryCollection geomCol = JtsUtil.GEOMETRY_FACTORY.createGeometryCollection(geometries);
		Geometry buffer = geomCol.buffer(0d);
		if(! firstGeometry.coveredBy(buffer)){
			Geometry errorGeo = EnhancedPrecisionOp.difference(firstGeometry, buffer);
			addTopologyError(createTopologyError(errorGeo, feature, null));
		}
	}

	
	@Override
	public String getName() {
		return RULE_NAME;
	}
}
