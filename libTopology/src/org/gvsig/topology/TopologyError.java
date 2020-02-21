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
package org.gvsig.topology;

import org.gvsig.fmap.core.FeatureUtil;

import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FGeometryCollection;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.IPersistence;
import com.iver.utiles.XMLEntity;


/**
 * Error produced when one or many features 
 * dont pass a topology rule.
 */
public class TopologyError extends DefaultFeature implements IPersistence{
	
	/**
	 * Reference to the topology to which this error is associated.
	 */
	private Topology topology;
		
	/**
	 *rule which has been violated
	 */
	private ITopologyRule violatedRule;
	 
	/**
	 First feature that causes the topology error
	 (Some rules' topology error will have only one feature associated
	 */
	private IFeature feature1;
	 
	/**
	 *Second feature that causes the topology error (null if dont exists)
	 */
	private IFeature feature2;
	 
	/**
	 * Flag that marks if this error is allowed
	 * (it has been marked as an exception)
	 */
	private boolean exception;
	
	/**
	 * Constructor
	 * @param geometry geometry of the error
	 * @param errorFid unique identifier for this error in the error container
	 * (topology)
	 * 
	 * @param violatedRule topology rule that this error violates
	 * 
	 * @param sourceLyrFeatures features of the origin layer in the rule
	 * that violates the rule
	 * 
	 * @param destinationLyrFeatures features of the destination layer in the rule
	 * that violates the rule
	 */
	public TopologyError(IGeometry geometry, 
			    		String errorFid,
			    		AbstractTopologyRule violatedRule,
			    		IFeature feature1, 
			    		IFeature feature2,
			    		Topology topology){
		super(geometry, null, errorFid);
		this.exception = false;
		this.violatedRule = violatedRule;
		this.feature1 = feature1;
		this.feature2 = feature2;
		this.topology = topology;
	}
	
	public TopologyError(IGeometry geometry,
						 AbstractTopologyRule violatedRule,
						 IFeature feature1, 
						 Topology topology){
		super(geometry, null, null);
		this.violatedRule = violatedRule;
		this.feature1 = feature1;
		this.topology = topology;
	}
	
	
	public TopologyError(Topology parentTopology){
		super(null, null, null);
		this.topology = parentTopology;
	}
	 
	public void setViolatedRule(AbstractTopologyRule violatedRule) {
		this.violatedRule = violatedRule;
	}
	 
	public ITopologyRule getViolatedRule() {
		return violatedRule;
	}
	 
	public void setFeature1(IFeature feature) {
		this.feature1 = feature;
	}
	 
	public IFeature getFeature1() {
		return feature1;
	}
	 
	public void setFeature2(IFeature feature) {
		this.feature2 = feature;
	}
	 
	public IFeature getFeature2() {
		return feature2;
	}
	 
	/**
	 *Ruturns the type of geometry of the error
	 */
	public int getShapeType() {
		return getGeometry().getGeometryType();
	}
	
	public String getShapeTypeAsText(){
		String solution = "";
		int shapeType = getShapeType();
		switch (shapeType) {
		case FShape.POINT:
		case FShape.TEXT:
			solution = Messages.getText("POINT");
			break;
		case FShape.POLYGON:
			solution = Messages.getText("POLYGON");
			break;
		case FShape.LINE:
		case FShape.ARC:
		case FShape.CIRCLE:
		case FShape.ELLIPSE:
			//needed because FGeometryCollection returns FShape.LINE
			if(getGeometry() instanceof FGeometryCollection)
				solution = Messages.getText("MULTIGEOMETRY");
			else
				solution = Messages.getText("LINE");
			break;
		case FShape.MULTI:
			solution = Messages.getText("MULTI");
			break;
		case FShape.MULTIPOINT:
			solution = Messages.getText("MULTIPOINT");
			break;
		}
		return solution;
	}
	 
	public void setException(boolean exception) {
		this.exception = exception;
	}
	 
	public boolean isException() {
		return exception;
	}

	
	public FLyrVect getOriginLayer(){
		return ((IOneLyrRule)violatedRule).getOriginLyr();
	}
	
	public FLyrVect getDestinationLayer(){
		if(violatedRule instanceof ITwoLyrRule)
			return  ((ITwoLyrRule)violatedRule).getDestinationLyr();
		else
			return null;
	}

	public String getClassName() {
		return this.getClass().getName();
	}
	
	public Topology getTopology(){
		return this.topology;
	}

	public XMLEntity getXMLEntity() {
		XMLEntity solution = FeatureUtil.getAsXmlEntity(this);
		solution.putProperty("exception", this.exception);
		
		solution.addChild(this.violatedRule.getXMLEntity());
		XMLEntity xmlFeature = null;
		boolean hasFeature1 = false;
		if(feature1 != null){
			hasFeature1 = true;
			xmlFeature = FeatureUtil.getAsXmlEntity(feature1);
			solution.addChild(xmlFeature);
		}
		solution.putProperty("hasFeature1", hasFeature1);
		
		boolean hasFeature2 = false;
		if(feature2 != null){
			hasFeature2 = true;
			xmlFeature = FeatureUtil.getAsXmlEntity(feature2);
			solution.addChild(xmlFeature);
		}
		solution.putProperty("hasFeature2", hasFeature2);
		return solution;
	}

	public void setXMLEntity(XMLEntity xml) {
		//first of all information xml of feature
		FeatureUtil.setXMLEntity(this, xml);
		
		if(xml.contains("exception")){
			exception = xml.getBooleanProperty("exception");
		}
		
		this.violatedRule =
			 TopologyRuleFactory.createFromXML(this.topology, xml.getChild(0));
		int childNumber = 1;
		
		if(xml.contains("hasFeature1")){
				boolean hasFeature1 = xml.getBooleanProperty("hasFeature1");
				if(hasFeature1)
				{
					this.feature1 = FeatureUtil.getFeatureFromXmlEntity(xml.getChild(childNumber));
					childNumber++;
				}
		}//if
		
		if(xml.contains("hasFeature2")){
			boolean hasFeature2 = xml.getBooleanProperty("hasFeature2");
			if(hasFeature2)
			{
				this.feature2 = FeatureUtil.getFeatureFromXmlEntity(xml.getChild(childNumber));
			}
		}//if
	}

}
 
