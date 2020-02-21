/*
 * Created on 07-sep-2007
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
 *
 */
package org.gvsig.topology.topologyrules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.SnapLineStringSelfIntersectionChecker;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.IRuleWithClusterTolerance;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.errorfixes.SplitSelfIntersectingLineFix;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;


/**
 *Lines of a layer must not have self intersections.
 *JTS allows this. This is one of the restrictions that must
 *not have pseudonodos checks.
 *
 */
public class LineMustNotSelfIntersect extends AbstractTopologyRule 
	implements IRuleWithClusterTolerance {

	final static String RULE_NAME = Messages.getText("line_must_not_self_intersect");
	
	private static List<ITopologyErrorFix> automaticErrorFixes =
		new ArrayList<ITopologyErrorFix>();
	static{
		automaticErrorFixes.add(new SplitSelfIntersectingLineFix());
	}
	
	private static final Color DEFAULT_ERROR_COLOR = Color.YELLOW;
	
	
	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = 
		(MultiShapeSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.MULTI, 
											DEFAULT_ERROR_COLOR);
	static{
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
		DEFAULT_ERROR_SYMBOL.setSize(3);
	}
	private double clusterTolerance;
	
	/**
	 * Symbol for topology errors caused by a violation of this rule.
	 */
	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
	
	
	public LineMustNotSelfIntersect(Topology topology, 
									FLyrVect originLyr,
									double clusterTolerance) {
		super(topology, originLyr);
		setClusterTolerance(clusterTolerance);
	}
	
	public LineMustNotSelfIntersect(){}

	
	public String getName() {
		return RULE_NAME;
	}

	public void checkPreconditions() throws TopologyRuleDefinitionException {
		try {
			if( FGeometryUtil.getDimensions(this.originLyr.getShapeType()) == 0){
				throw new TopologyRuleDefinitionException("Capa con tipo de geometria incorrecto para la regla "+this.getClassName());
			}
		} catch (ReadDriverException e) {
			throw new TopologyRuleDefinitionException("Error de driver al verificar el tipo de geometria de la capa "+originLyr.getName());
		}
	}


	public void validateFeature(IFeature feature) {
		IGeometry geometry = feature.getGeometry();
		Geometry jtsGeometry = NewFConverter.toJtsGeometry(geometry);
		process(jtsGeometry, feature);
	}
	
	private void process(Geometry geometry, IFeature feature){
		if( ! (geometry instanceof LineString) && ! (geometry instanceof GeometryCollection))
			return;
		
		if(geometry instanceof GeometryCollection){
			GeometryCollection geomCol = (GeometryCollection) geometry;
			for(int i = 0; i < geomCol.getNumGeometries(); i++){
				Geometry geom = geomCol.getGeometryN(i);
				process(geom, feature);
			}
		}else{
			LineString lineString = (LineString) geometry;
			SnapLineStringSelfIntersectionChecker checker =
				new SnapLineStringSelfIntersectionChecker(lineString, clusterTolerance);
			if(checker.hasSelfIntersections()){
				Coordinate[] coords = checker.getSelfIntersections();
				double[] x = new double[coords.length];
				double[] y = new double[coords.length];
				for(int i = 0; i < coords.length; i++){
					x[i] = coords[i].x;
					y[i] = coords[i].y;
				}
				IGeometry errorGeometry = ShapeFactory.createMultipoint2D(x, y);
				TopologyError topologyError = new TopologyError(errorGeometry, this,
						feature, topology);
				topologyError.setID(errorContainer.getErrorFid());
				addTopologyError(topologyError);
			}//if selfintersections
		}	
	}

	public double getClusterTolerance() {
		return clusterTolerance;
	}

	public void setClusterTolerance(double clusterTolerance) {
		this.clusterTolerance = clusterTolerance;
	}

	public boolean acceptsOriginLyr(FLyrVect lyr) {
		try {
			return (FGeometryUtil.getDimensions(lyr.getShapeType()) != 0);
		} catch (ReadDriverException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public List<ITopologyErrorFix> getAutomaticErrorFixes() {
		return automaticErrorFixes;
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
	
	public ITopologyErrorFix getDefaultFixFor(TopologyError topologyError){
		return automaticErrorFixes.get(0);
	}
}
 
