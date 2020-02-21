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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.topologyrules.JtsValidRule;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geomgraph.GeometryGraph;

/**
 * This rule checks that every geometry has at least the minimun number of points
 * to correctly define it.
 * For example: 
 * 
 * a) a polyline2d at least must have two points.
 * b) a polygon2d at least must have three non collinear points.
 * 
 * 
 * @author Alvaro Zabala
 *
 */
public class GeometryMustNotHaveFewPoints extends AbstractTopologyRule{
	  	
	private JtsValidRule parentRule;
	
	static final List<ITopologyErrorFix> automaticFixes = new ArrayList<ITopologyErrorFix>();
	
	private static final Color DEFAULT_ERROR_COLOR = Color.RED;
	
	//this symbol is multishape because this topology rule applies to lines or polygons
	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = 
		(MultiShapeSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.MULTI, 
											DEFAULT_ERROR_COLOR);
	static{
		DEFAULT_ERROR_SYMBOL.setSize(1);
		DEFAULT_ERROR_SYMBOL.setLineWidth(1);
		DEFAULT_ERROR_SYMBOL.getOutline().setLineColor(DEFAULT_ERROR_COLOR);
		DEFAULT_ERROR_SYMBOL.setFillColor(DEFAULT_ERROR_COLOR);
	}
	
	/**
	 * Symbol for topology errors caused by a violation of this rule.
	 */
	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
	
	
	public JtsValidRule getParentRule() {
		return parentRule;
	}

	public void setParentRule(JtsValidRule parentRule) {
		this.parentRule = parentRule;
	}

	public GeometryMustNotHaveFewPoints(FLyrVect originLyr) {
		super(originLyr);
	}
	
	public GeometryMustNotHaveFewPoints(){}
		
		public GeometryMustNotHaveFewPoints(Topology topology, FLyrVect originLyr) {
			super(topology, originLyr);
		}
	
	
		public String getName() {
			return Messages.getText("GEOMETRY_MUST_NOT_HAVE_FEW_POINTS");
		}
		
		public void checkPreconditions() throws TopologyRuleDefinitionException {
			int shapeType;
			try {
				shapeType = this.originLyr.getShapeType();
				switch(shapeType){
				case FShape.POINT:
				case FShape.TEXT:
				case FShape.MULTIPOINT:
					throw new TopologyRuleDefinitionException("La regla Few Points no aplica sobre capas de puntos");
			}
			} catch (ReadDriverException e) {
				throw new TopologyRuleDefinitionException("Error al tratar de verificar el tipo de geometria");
			}
		}
		
		public void validateFeature(IFeature feature) {
			IGeometry geometry = feature.getGeometry();
			Geometry jtsGeo = geometry.toJTSGeometry();
			if(jtsGeo.getDimension() == 0)
				return;
			GeometryGraph graph = new GeometryGraph(0, jtsGeo );
		    if (graph.hasTooFewPoints()) {//TODO JTS NO CONTEMPLA CURVAS....VER SI ES MEJOR DEFINIR ESTO CON FMAP
		        AbstractTopologyRule violatedRule = null;
		        if(this.parentRule != null)
		        	violatedRule = parentRule;
		        else
		        	violatedRule = this;
		    	JtsValidTopologyError error = new JtsValidTopologyError(geometry, violatedRule, feature, topology );
		    	error.setSecondaryRule(this);
		      this.errorContainer.addTopologyError(error);
		    }//if 
		}

		public boolean acceptsOriginLyr(FLyrVect lyr) {
			try {
				return FGeometryUtil.getDimensions(lyr.getShapeType()) > 0;
			} catch (ReadDriverException e) {
				e.printStackTrace();
				return false;
			}
		}

		public List<ITopologyErrorFix> getAutomaticErrorFixes() {
			return automaticFixes;
		}

		public MultiShapeSymbol getDefaultErrorSymbol() {
			return DEFAULT_ERROR_SYMBOL;
		}

		public MultiShapeSymbol getErrorSymbol() {
			return errorSymbol;
		}  
  }