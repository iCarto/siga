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

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.jcs.JCSUtil;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.IRuleWithClusterTolerance;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.TopologyRuleDefinitionException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.swing.threads.CancellableProgressTask;

/**
 * All polygons of a vectorial layer must form a continuous surface, 
 * without voids or gaps.
 * 
 * @author Alvaro Zabala
 *
 */
public class PolygonMustNotHaveGapsWithJCS extends AbstractTopologyRule 
									implements IRuleWithClusterTolerance {
	
	private double clusterTolerance;

	static final String RULE_NAME = Messages.getText("POLYGONS_MUST_NOT_HAVE_GAPS");

	private static final Color DEFAULT_ERROR_COLOR = Color.RED;

	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = (MultiShapeSymbol) SymbologyFactory
			.createDefaultSymbolByShapeType(FShape.MULTI, DEFAULT_ERROR_COLOR);
	static {
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
	}

	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
	
	
	public PolygonMustNotHaveGapsWithJCS(Topology topology, FLyrVect originLyr,
			double clusterTolerance) {
		super(topology, originLyr);
		setClusterTolerance(clusterTolerance);
	}

	@Override
	public void checkPreconditions() throws TopologyRuleDefinitionException {
		if(!acceptsOriginLyr(originLyr))
			throw new TopologyRuleDefinitionException(
					"PolygonMustNotOverlap requires a polygonal geometry type");
	}
	
	@Override
	public String getName() {
		return RULE_NAME;
	}
	
	
	public void checkRule(CancellableProgressTask progressMonitor){
		FLyrVect gapsLyr = JCSUtil.getGaps(originLyr, clusterTolerance);
		IFeatureIterator iterator;
		try {
			iterator = gapsLyr.getSource().getFeatureIterator();
			while(iterator.hasNext()){
				IFeature feature = iterator.next();
				TopologyError topologyError = new TopologyError(feature.getGeometry(),
						this, null, getTopology());
				addTopologyError(topologyError);
			}
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	 
	public  void checkRule(CancellableProgressTask progressMonitor, Rectangle2D rect){
		checkRule(progressMonitor);
	}
	
	public double getClusterTolerance() {
		return clusterTolerance;
	}
	
	public void setClusterTolerance(double clusterTolerance) {
		this.clusterTolerance = clusterTolerance;
	}
	
	
	public boolean acceptsOriginLyr(FLyrVect originLyr) {
		try {
			int shapeType = originLyr.getShapeType();
			return (FGeometryUtil.getDimensions(shapeType) == 2);
		} catch (ReadDriverException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public List<ITopologyErrorFix> getAutomaticErrorFixes() {
		//TODO IMPLEMENTAR
		return new ArrayList<ITopologyErrorFix>();
	}
	
	public MultiShapeSymbol getDefaultErrorSymbol() {
		return DEFAULT_ERROR_SYMBOL;
	}
	
	public MultiShapeSymbol getErrorSymbol() {
		return errorSymbol;
	}

	
	public void validateFeature(IFeature feature) {
		throw new NotImplementedException();
	}
}
