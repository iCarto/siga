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
package org.gvsig.topology.topologyrules.jtsisvalidrules;

import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.ITopologyRule;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;

import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;

/**
 * A kind of TopologyError caused by the violation of a JtsValid rule.
 * 
 * If offers access to the secondary rule which caused the violation of the
 * JTS specification.
 * 
 * 
 * @author Alvaro Zabala
 *
 */
public class JtsValidTopologyError extends TopologyError {

	
	private ITopologyRule secondaryRule;
	
	
	public JtsValidTopologyError(IGeometry geometry, 
									String errorFid,
									AbstractTopologyRule violatedRule, 
									IFeature feature1,
									IFeature feature2, 
									Topology topology) {
		
		super(geometry, errorFid, violatedRule, feature1, feature2, topology);

	}

	public JtsValidTopologyError(IGeometry geometry,
						AbstractTopologyRule violatedRule, 
						IFeature feature1,
						Topology topology) {
		super(geometry, violatedRule, feature1, topology);
	}

	public JtsValidTopologyError(Topology parentTopology) {
		super(parentTopology);
	}

	public ITopologyRule getSecondaryRule() {
		return secondaryRule;
	}

	public void setSecondaryRule(ITopologyRule secondaryRule) {
		this.secondaryRule = secondaryRule;
	}
}
