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

import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.topology.IRuleWithClusterTolerance;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.errorfixes.DeleteTopologyErrorFix;

import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Geometry;

/**
 * All geometries of origin layer must have at least one geometry of 
 * destination layer with the same geometry.
 * @author Alvaro Zabala
 *
 */
public class LyrMustBeEqual extends AbstractSpatialPredicateTwoLyrRule implements IRuleWithClusterTolerance{

	static final String RULE_NAME = Messages.getText("must_be_equals");
	
	static {
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
		automaticErrorFixes.add(new DeleteTopologyErrorFix());
	}
	
	private double clusterTolerance;
	
	public LyrMustBeEqual(Topology topology, 
			 FLyrVect originLyr,
			 FLyrVect destinationLyr){
		super(topology, originLyr, destinationLyr);
	}


	public LyrMustBeEqual(){
		super();
	}

	@Override
	protected boolean acceptsDestinationGeometryType(int shapeType) {
		return true;
	}

	@Override
	protected boolean acceptsOriginGeometryType(int shapeType) {
		return true;
	}
	
	@Override
	protected void checkWithNeighbourhood(IFeature feature, Rectangle2D extendedBounds, IFeatureIterator neighbourhood) throws BaseException{
		Geometry firstGeometry = NewFConverter.toJtsGeometry(feature.getGeometry());
		while (neighbourhood.hasNext()) {
			IFeature neighbourFeature = neighbourhood.next();
			IGeometry geom2 = neighbourFeature.getGeometry();
			if(acceptsDestinationGeometryType(geom2.getGeometryType())){
				Rectangle2D rect2 = geom2.getBounds2D();
				if (extendedBounds.intersects(rect2)) {
					Geometry jtsGeom2 = NewFConverter.toJtsGeometry(geom2);
					if(checkSpatialPredicate(feature, firstGeometry, 
														neighbourFeature, jtsGeom2 )){
						return;
					}
				}//if
			}//if
		}//while
		//at this point, feature doesnt cover any feature of destination layer
		addTopologyError(createTopologyError(firstGeometry, feature, null));
	}
	

	@Override
	protected boolean checkSpatialPredicate(IFeature feature,
			Geometry firstGeometry, IFeature neighbourFeature, Geometry jtsGeom2) {
		return firstGeometry.equalsExact(jtsGeom2, clusterTolerance);
	}

	@Override
	public void checkPreconditions() throws TopologyRuleDefinitionException {
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}

	public boolean acceptsDestinationLyr(FLyrVect originLyr) {
		return true;
	}

	public boolean acceptsOriginLyr(FLyrVect originLyr) {
		return true;
	}


	public double getClusterTolerance() {
		return clusterTolerance;
	}


	public void setClusterTolerance(double clusterTolerance) {
		this.clusterTolerance = clusterTolerance;
	}
	
	public ITopologyErrorFix getDefaultFixFor(TopologyError topologyError){
		return automaticErrorFixes.get(0);
	}

}
