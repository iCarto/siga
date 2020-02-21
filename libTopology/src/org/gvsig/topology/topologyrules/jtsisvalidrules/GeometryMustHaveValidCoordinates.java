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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.topologyrules.JtsValidRule;

import com.iver.cit.gvsig.fmap.core.Handler;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

/**
 * Checks if geometry coordinates are valid.
 * 
 * @author Alvaro Zabala
 *
 */
public class GeometryMustHaveValidCoordinates extends AbstractTopologyRule{
	
	private JtsValidRule parentRule;
	
	static final List<ITopologyErrorFix> automaticFixes = new ArrayList<ITopologyErrorFix>();
	
	
	/**
	 * Constructor
	 * @param topology
	 * @param originLyr
	 */
	public GeometryMustHaveValidCoordinates(Topology topology,
			FLyrVect originLyr) {
		super(topology, originLyr);
	}
	
	public GeometryMustHaveValidCoordinates(FLyrVect originLyr){
		super(originLyr);
	}
	
	public GeometryMustHaveValidCoordinates(){}
	
	
	public String getName() {
		return Messages.getText("GEOMETRY_MUST_HAVE_VALID_COORDINATES");
	}
	
	public void checkPreconditions() throws TopologyRuleDefinitionException {}
	
	public void validateFeature(IFeature feature) {
		Handler[] handlers = feature.getGeometry().getHandlers(IGeometry.SELECTHANDLER);
		for(int i = 0; i < handlers.length; i++){
			Point2D pt = handlers[i].getPoint();
			if(! isValid(pt)){
				 IGeometry fgeo = feature.getGeometry();
		    	 AbstractTopologyRule violatedRule = null;
		    	 if(this.parentRule != null)
		    		 violatedRule = parentRule;
		    	 else
		    		 violatedRule = this;
				 JtsValidTopologyError topologyError = 
		    		 new JtsValidTopologyError(fgeo, violatedRule, feature, topology);
		    	 topologyError.setSecondaryRule(this);
		    	 errorContainer.addTopologyError(topologyError);
			}
		}//for	
	} 
	
	private boolean isValid(Point2D coord){
	  if (Double.isNaN(coord.getX())) return false;
	  if (Double.isInfinite(coord.getX())) return false;
	  if (Double.isNaN(coord.getY())) return false;
	  if (Double.isInfinite(coord.getY())) return false;
	  return true;
	}

	public JtsValidRule getParentRule() {
		return parentRule;
	}

	public void setParentRule(JtsValidRule parentRule) {
		this.parentRule = parentRule;
	}

	public boolean acceptsOriginLyr(FLyrVect originLyr) {
		return true;
	}

	public List<ITopologyErrorFix> getAutomaticErrorFixes() {
		return automaticFixes;
	}

	public MultiShapeSymbol getDefaultErrorSymbol() {
		return null;
	}

	public MultiShapeSymbol getErrorSymbol() {
		return null;
	}
  }

