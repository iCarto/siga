package org.gvsig.topology.util;

/*
 * Created on 18-sep-2007
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
import java.util.ArrayList;
import java.util.List;

import org.cresques.cts.IProjection;
import org.gvsig.topology.ITopologyErrorContainer;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.utiles.XMLEntity;

public class TestTopologyErrorContainer implements ITopologyErrorContainer{

		List<TopologyError> topologyErrors = new ArrayList<TopologyError>();
		
		public void addTopologyError(TopologyError topologyError) {
			topologyError.setID(topologyErrors.size()+"");
			topologyErrors.add(topologyError);
		}
		
		public Object clone(){
			return null;
		}

		public List<TopologyError> getTopologyErrors(String ruleName, int shapeType, FLyrVect sourceLayer, IProjection desiredProjection, boolean includeExceptions) {
			return null;
		}
		public List<TopologyError> getTopologyErrorsByRule(String ruleName, IProjection desiredProjection, boolean includeExceptions) {
			return null;
		}

		public List<TopologyError> getTopologyErrorsByShapeType(int shapeType, IProjection desiredProjection, boolean includeExceptions) {
			return null;
		}

		public List<TopologyError> getTopologyErrorsByLyr(FLyrVect layer, IProjection desiredProjection, boolean includeExceptions) {
			return null;
		}

		public void markAsTopologyException(TopologyError topologyError) {
			topologyError.setException(true);	
		}

		public void demoteToError(TopologyError topologyError) {
			topologyError.setException(false);
		}

		public String getErrorFid() {
			return topologyErrors.size() + "";
		}

		public int getNumberOfErrors() {
			return topologyErrors.size();
		}

		public TopologyError getTopologyError(int index) {
			return topologyErrors.get(index);
		}

		public void clear() {
			// TODO Auto-generated method stub
			
		}

		public int getNumberOfExceptions() {
			// TODO Auto-generated method stub
			return 0;
		}

		public XMLEntity getXMLEntity() throws XMLException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setXMLEntity(XMLEntity xml) throws XMLException {
			// TODO Auto-generated method stub
			
		}

		public void removeErrorsByLayer(FLyrVect layer) {
			// TODO Auto-generated method stub
			
		}

		public void removeErrorsByRule(String ruleName) {
			// TODO Auto-generated method stub
			
		}

		public FLyrVect getAsFMapLayer() {
			// TODO Auto-generated method stub
			return null;
		}

		public FLyrVect getAsFMapLayer(String name, IProjection projection) {
			// TODO Auto-generated method stub
			return null;
		}

		public Topology getTopology() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setTopology(Topology topology) {
			// TODO Auto-generated method stub
			
		}

		public void removeError(TopologyError topologyError) {
			// TODO Auto-generated method stub
			
		}	
	}