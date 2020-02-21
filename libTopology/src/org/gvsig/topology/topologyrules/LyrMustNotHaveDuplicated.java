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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jcs.JCSUtil;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.IRuleWithClusterTolerance;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.errorfixes.DeleteTopologyErrorFix;

import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Geometry;

public class LyrMustNotHaveDuplicated extends AbstractTopologyRule implements IRuleWithClusterTolerance{
		 
		private static  final String RULE_NAME = Messages.getText("must_not_have_duplicates");
		
		/**
		 * Predefinex automatic fixes for those topology errors caused by this 
		 * rule violation.
		 */
		private static List<ITopologyErrorFix> errorFixes = 
			new ArrayList<ITopologyErrorFix>();
		
		static{
			errorFixes.add(new DeleteTopologyErrorFix());
		}
		
		private static final Color DEFAULT_ERROR_COLOR = Color.RED;
		
		//this symbol is multishape because this topology rule applies to lines or polygons
		private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = 
			(MultiShapeSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.MULTI, 
												DEFAULT_ERROR_COLOR);
		static{
			DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
			DEFAULT_ERROR_SYMBOL.setSize(1);
			DEFAULT_ERROR_SYMBOL.setLineWidth(1);
			DEFAULT_ERROR_SYMBOL.getOutline().setLineColor(DEFAULT_ERROR_COLOR);
			DEFAULT_ERROR_SYMBOL.setFillColor(DEFAULT_ERROR_COLOR);
		}

		/**
		 * Symbol for topology errors caused by a violation of this rule.
		 */
		private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
		
		private double clusterTolerance;
		
		public LyrMustNotHaveDuplicated(Topology topology, FLyrVect originLyr){
			super(topology, originLyr);
		}
		
		
		public LyrMustNotHaveDuplicated(){}
		
		
		public String getName() {
			return RULE_NAME;
		}
		
		
		public void checkPreconditions() throws TopologyRuleDefinitionException {
		}
		
		
		/**
		 * Validates this rule with a feature of the origin layer
		 * @param feature feature of the origin layer this rule is checking
		 */
		public void validateFeature(IFeature feature) {
			IGeometry geom = feature.getGeometry();
			Geometry jtsGeom = NewFConverter.toJtsGeometry(geom);
			
			Rectangle2D bounds = geom.getBounds2D();
			Point2D extendedLL = new Point2D.Double(bounds.getMinX(), bounds.getMinY());
			Point2D extendedUL = new Point2D.Double(bounds.getMaxX(), bounds.getMaxY());
			bounds.add(extendedLL);
			bounds.add(extendedUL);
				
			try {
				IFeatureIterator neighbours = getOriginLyr().
											  getSource().getFeatureIterator(bounds, null, 
													    					null, false);
				while (neighbours.hasNext()) {
					IFeature neighbourFeature = neighbours.next();
					if(neighbourFeature.getID().equals(feature.getID()))
						continue;
					IGeometry geom2 = neighbourFeature.getGeometry();
					Rectangle2D rect2 = geom2.getBounds2D();
					if (bounds.intersects(rect2)) {
						Geometry second = NewFConverter.toJtsGeometry(geom2);
						if(JCSUtil.checkMatchingCriteria(jtsGeom, second, clusterTolerance))
						{
							TopologyError error = new TopologyError(geom, this, feature, getTopology());
							addTopologyError(error);
						}
					}
					
				}//while	
				
			} catch (BaseException e) {
				e.printStackTrace();
			}
		}

		

		public List<ITopologyErrorFix> getAutomaticErrorFixes() {
			return LyrMustNotHaveDuplicated.errorFixes;
		}

		public MultiShapeSymbol getDefaultErrorSymbol() {
			return DEFAULT_ERROR_SYMBOL;
		}

		public MultiShapeSymbol getErrorSymbol() {
			return errorSymbol;
		}

		public void setErrorSymbol(MultiShapeSymbol errorSymbol) {
			this.errorSymbol = errorSymbol;
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
			return errorFixes.get(0);
		}
	}
