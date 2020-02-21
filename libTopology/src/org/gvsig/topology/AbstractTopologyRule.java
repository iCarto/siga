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

import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.CancellableProgressTask;

/**
 * Default implementation of ITopologyRule
 * 
 * @author azabala
 * 
 */
public abstract class AbstractTopologyRule implements IOneLyrRule  {
	
	/**
	 * We need a reference to topology to get a layer from its name
	 * in setXML method.
	 */
	protected Topology topology;
	
 
	protected FLyrVect originLyr;
	
	/**
	 * Container for the topology errors detected.
	 */
	protected ITopologyErrorContainer errorContainer;
	
	
	/**
	 * Container for all those error fixes that dont need user
	 * interaction (so they could be invoked in a batch process).
	 * The first fix of the list is the default fix
	 */
	protected static List<ITopologyErrorFix> automaticErrorFixes = new ArrayList<ITopologyErrorFix>();
	
	/**
	 * Unique identifier of the rule to distinct it of the rest of rules
	 * of a topology
	 */
	private int ruleId;
	
	/**
	 * Constructor.
	 * 
	 *  @param originLyr
	 *            layer which features we are checking
	 * 
	 * @param topology reference to the topology that owns this rule.
	 */
	public AbstractTopologyRule(Topology topology, FLyrVect originLyr){
		this.topology = topology;
		this.originLyr = originLyr;
	}
	
	/**
	 * Constructor without topology param
	 * 
	 * @param originLyr
	 */
	public AbstractTopologyRule(FLyrVect originLyr){
		this.originLyr = originLyr;
	}
	
	public AbstractTopologyRule(){
		//default constructor. Needed for persistence
	}
	 
	public void setOriginLyr(FLyrVect originLyr) {
		this.originLyr = originLyr;
	}
	 
	public FLyrVect getOriginLyr() {
		return originLyr;
	}
	 
	
	public void setTopologyErrorContainer(ITopologyErrorContainer errorContainer){
		this.errorContainer = errorContainer;
	}
	
	public ITopologyErrorContainer getTopologyErrorContainer(){
		return errorContainer;
	}

	public abstract String getName();
	
	
	public  URL  getDescription(){
		Locale locale = Locale.getDefault();
		String localeStr = locale.getLanguage();
		String urlStr = "docs/"+
						getClass().getName() +
						"/description_%lang%.html";
		String localizedUrl = urlStr.replaceAll("%lang%", localeStr);
		
		URL url = AbstractTopologyRule.class.getResource(localizedUrl);
		if(url == null){
			// for languages used in Spain, fallback to Spanish if their translation is not available
			if (localeStr.equals("ca")||localeStr.equals("gl")||localeStr.equals("eu")||localeStr.equals("va")) {
				localeStr = "es";
				localizedUrl = urlStr.replaceAll("%lang", localeStr);
				url = AbstractTopologyRule.class.getResource(localizedUrl);
				if(url != null)
					return url;
			}
			// as a last resort, fallback to English
			localeStr = "en";
			localizedUrl = urlStr.replaceAll("%lang", localeStr);
			url = AbstractTopologyRule.class.getResource(localizedUrl);	
		}			
		return url;
     }
		
	/**
	 * Checks if the rule's parameters (sourceLyr, destinationLyr) verify rule
	 * preconditions (geometry type, etc.)
	 */
	public abstract void checkPreconditions() throws TopologyRuleDefinitionException ;
	 
	
	public void checkRule(){
		this.checkRule((CancellableProgressTask)null);
	}
	
	public void checkRule(Rectangle2D rect){
		checkRule(null, rect);
	}
	
	
	public void checkRule(CancellableProgressTask progressMonitor){
		try {
			// when we dont pass field names iterator only iterates over
			// geometries
			IFeatureIterator featureIterator = originLyr.getSource().getFeatureIterator();
			while(featureIterator.hasNext()){
				IFeature feature = featureIterator.next();
				if(progressMonitor != null){
					if(progressMonitor.isCanceled()/*
													 * ||
													 * progressMonitor.isFinished()
													 */){
						// TODO Maybe we could show progress info of rule
						// checking.
						// example: feature 1 of N...etc
						return;
					}
   				}
				validateFeature(feature);
			}
			this.ruleChecked();
		} catch (ExpansionFileReadException e) {
			e.printStackTrace();
		} catch (ReadDriverException e) {
			e.printStackTrace();
		}	
	}
	 
	public  void checkRule(CancellableProgressTask progressMonitor, Rectangle2D rect){
		try {
			IFeatureIterator iterator = originLyr.getSource().getFeatureIterator(rect, null, null, true);
			while(iterator.hasNext()){
				IFeature feature = iterator.next();
				if(progressMonitor != null){
					if(progressMonitor.isCanceled()/*
													 * ||
													 * progressMonitor.isFinished()
													 */){
						// TODO Maybe we could show progress info of rule
						// checking.
						// example: feature 1 of N...etc
						return;
					}
   				}
				validateFeature(feature);
			}//while
			ruleChecked();
		} catch (ExpansionFileReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	/**
	 * This method must be overwrited by all of those subclases which uses temporal results,
	 * cachés, etc. 
	 */
	public void ruleChecked(){};
	
	public abstract void validateFeature(IFeature feature);
	
	
	public void addTopologyError(TopologyError topologyError){
		this.errorContainer.addTopologyError(topologyError);
	}
	
	/*
	 * Implementation of IPersistence
	 */
	public String getClassName(){
		return this.getClass().getName();
	}
	   
	public XMLEntity getXMLEntity(){
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", getClassName());
		xml.putProperty("originLayerName", this.originLyr.getName());
		xml.putProperty("ruleId", this.ruleId);
		if(this instanceof IRuleWithClusterTolerance){
			double clusterTolerance = ((IRuleWithClusterTolerance)this).getClusterTolerance();
			xml.putProperty("clusterTolerance", clusterTolerance);
		}
		return xml;
	}
	    
	public void setXMLEntity(XMLEntity xml){
		String originLayerName = "";
		if (xml.contains("originLayerName")) {
			originLayerName = xml.getStringProperty("originLayerName");
			this.originLyr = (FLyrVect) topology.getLayer(originLayerName);
		}//if
		
		if(xml.contains("ruleId")){
			ruleId = xml.getIntProperty("ruleId");
		}
		
		if(xml.contains("clusterTolerance")){
			double clusterTolerance = xml.getDoubleProperty("clusterTolerance");
			if(this instanceof IRuleWithClusterTolerance)
				((IRuleWithClusterTolerance)this).setClusterTolerance(clusterTolerance);
		}
	}
	
    public void setId(int ruleId){
    	this.ruleId = ruleId;
    }
	
	public int getId(){
		return ruleId;
	}

	public Topology getTopology() {
		return topology;
	}

	public void setTopology(Topology topology) {
		this.topology = topology;
	}
	
	public boolean equals(Object o){
		if(!o.getClass().equals(this.getClass()))
			return false;
		AbstractTopologyRule oRule = (AbstractTopologyRule)o;
		if(!oRule.originLyr.equals(this.originLyr))
			return false;
		if(this instanceof ITwoLyrRule){
			if(! (o instanceof ITwoLyrRule))
				return false;
			
			ITwoLyrRule thisRule = (ITwoLyrRule)this;
			ITwoLyrRule oTwoRule = (ITwoLyrRule)oRule;
			if(! thisRule.getDestinationLyr().equals(oTwoRule.getDestinationLyr()))
				return false;
		}
		return true;
	}
	
	public int hashCode(){
		return 1;
	}
	
	
	
	public ITopologyErrorFix getDefaultFixFor(TopologyError topologyError){
		ITopologyErrorFix solution = null;
		if(automaticErrorFixes.size() > 0)
			solution = automaticErrorFixes.get(0);
		return solution;
	}

	public  List<ITopologyErrorFix> getAutomaticErrorFixes() {
		return automaticErrorFixes;
	}

}
 
