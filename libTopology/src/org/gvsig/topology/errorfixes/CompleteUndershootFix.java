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

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.topology.Messages;
import org.gvsig.topology.TopologyError;

import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;

public class CompleteUndershootFix extends AbstractTopologyErrorFix {

//	@SuppressWarnings("unchecked")
	public List<IFeature>[] fixAlgorithm(TopologyError topologyError) {
		//TODO Probablemente TopologyError no necesite una referencia a los Feature
		//que lo causaron, bastando el indice y el identificador de la capa
		IFeature causingFeature = topologyError.getFeature1();
		IGeometry geom = causingFeature.getGeometry();
		double clusterTolerance = topologyError.getTopology().getClusterTolerance();
		IGeometry correctedGeom = FGeometryUtil.closeGeometry(geom, clusterTolerance);
		DefaultFeature df = new DefaultFeature(correctedGeom, 
								causingFeature.getAttributes(),
									causingFeature.getID());
		List<IFeature> featureList = new ArrayList<IFeature>();
		featureList.add(df);
		List<IFeature>[] features = new List[]{featureList};
		return features;
	}

	public String getEditionDescription() {
		return Messages.getText("COMPLETE_UNDERSHOOT_FIX");
	}

}
