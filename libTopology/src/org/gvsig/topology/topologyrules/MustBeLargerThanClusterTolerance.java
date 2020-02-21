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
import org.gvsig.jts.GeometryCollapsedException;
import org.gvsig.jts.GeometrySnapper;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.IRuleWithClusterTolerance;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.errorfixes.DeleteTopologyErrorFix;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.XMLEntity;
import com.vividsolutions.jts.geom.Geometry;


/**
 * For lines o polygons. The lenght of a line or the perimeter
 * of a polygon must be larger than the cluster tolerance.
 * 
 * Two consecutives points at a distance lower than cluster tolerance must be
 * deleted.
 *
 *
 * @author azabala
 */
public class MustBeLargerThanClusterTolerance extends AbstractTopologyRule implements  IRuleWithClusterTolerance {
 
	private static  final String RULE_NAME = Messages.getText("must_be_larger_than_cluster_tolerance");
	
	/**
	 * Predefinex automatic fixes for those topology errors caused by this 
	 * rule violation.
	 */
	private static List<ITopologyErrorFix> errorFixes = 
		new ArrayList<ITopologyErrorFix>();
	
	static{
		errorFixes.add(new DeleteTopologyErrorFix());
	}
	
	private static final Color DEFAULT_ERROR_COLOR = Color.ORANGE;
	
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
	  * Cluster tolerance to check.
	  */
	private double clusterTolerance;
	
	/**
	 * Snapper to check coords proximity of a given geometry
	 */
	private GeometrySnapper geometrySnapper;
	
	/**
	 * Symbol for topology errors caused by a violation of this rule.
	 */
	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
	
	
	
	public MustBeLargerThanClusterTolerance(Topology topology, FLyrVect originLyr, double clusterTolerance){
		super(topology, originLyr);
		setClusterTolerance(clusterTolerance);
	}
	
	public MustBeLargerThanClusterTolerance(FLyrVect originLyr, double clusterTolerance){
		this(null, originLyr, clusterTolerance);
	}
	
	public MustBeLargerThanClusterTolerance(){}
	
	
	public String getName() {
		return RULE_NAME;
	}
	
	
	public void checkPreconditions() throws TopologyRuleDefinitionException {
		//This rule doesnt apply to Point or MultiPoint vectorial layers.
		try {
			int shapeType = this.originLyr.getShapeType();
			if( (shapeType == FShape.POINT) || (shapeType == FShape.MULTIPOINT) || (shapeType == FShape.TEXT))
				throw new TopologyRuleDefinitionException();
		} catch (ReadDriverException e) {
			e.printStackTrace();
			throw new TopologyRuleDefinitionException("Error leyendo el tipo de geometria del driver",e);
		}	
	}

	/**
	 * Validates this rule with a feature of the origin layer
	 * @param feature feature of the origin layer this rule is checking
	 */
	public void validateFeature(IFeature feature) {
		IGeometry geometry = feature.getGeometry();
		Geometry jtsGeometry = NewFConverter.toJtsGeometry(geometry);
		try {
			if(FGeometryUtil.getDimensions(originLyr.getShapeType()) == 2){
				//for polygons we dont try to collapse and use isValid
				//because self intersecting polygons will return false
				double area = jtsGeometry.getArea();
				double minArea = clusterTolerance * clusterTolerance;
				if(area < minArea){
					TopologyError topologyError = new TopologyError(geometry, this, feature, topology);
					topologyError.setID(errorContainer.getErrorFid());
					addTopologyError(topologyError);
				}
			}else{
				geometrySnapper.snap(jtsGeometry);
				
			}
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(GeometryCollapsedException e){
			//use an ErrorFactory
			TopologyError topologyError = new TopologyError(geometry, this, feature, topology);
			topologyError.setID(errorContainer.getErrorFid());
			addTopologyError(topologyError);
		}
		
	}

	public double getClusterTolerance() {
		return clusterTolerance;
	}

	public void setClusterTolerance(double clusterTolerance) {
		this.clusterTolerance = clusterTolerance;
		this.geometrySnapper = new GeometrySnapper(clusterTolerance);
	} 
	
	public XMLEntity getXMLEntity(){
		XMLEntity xml = super.getXMLEntity();
		xml.putProperty("className", getClassName());
		xml.putProperty("clusterTolerance", this.clusterTolerance);
		return xml;
	}
	    
	public void setXMLEntity(XMLEntity xml){
		super.setXMLEntity(xml);
		if(xml.contains("clusterTolerance")){
			setClusterTolerance(xml.getDoubleProperty("clusterTolerance"));
		}
	}

	public boolean acceptsOriginLyr(FLyrVect lyr) {
		try {
			return (FGeometryUtil.getDimensions(lyr.getShapeType()) > 0);
		} catch (ReadDriverException e) {
			return false;
		}
	}

	public List<ITopologyErrorFix> getAutomaticErrorFixes() {
		return MustBeLargerThanClusterTolerance.errorFixes;
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
		return errorFixes.get(0);
	}
}
 
