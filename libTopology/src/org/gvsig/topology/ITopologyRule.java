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
import java.util.List;

import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.utiles.IPersistence;
import com.iver.utiles.swing.threads.CancellableProgressTask;

/**
 * A topology rule checks that features' geometry of one or more layers
 * verifies a given spatial condition.
 * 
 * 
 * 
 * @author azabala
 *
 */
public interface ITopologyRule extends IPersistence {
	
	/**
	 * Return the name of this topology rule
	 * @return
	 */
	public String getName();
	
	/**
	 * Returns url of a html document with the description of the 
	 * topology rule.
	 * @return
	 */
	public URL getDescription();
	 
	/**
	 *Checks if the rule's parameters 
	 *(sourceLyr, destinationLyr) verify 
	 *rule preconditions (geometry type, etc.)
	 * @throws TopologyRuleDefinitionException 
	 */
	public void checkPreconditions() throws TopologyRuleDefinitionException;
	 
	/**
	 * Checks this rule for all features of origin layer
	 */
	public  void checkRule();
	
	/**
	 * Check the rule for all features and pass log progress messages to
	 * specified progress monitor.
	 * @param progressMonitor
	 */
	public void checkRule(CancellableProgressTask progressMonitor);
	
	public void checkRule(CancellableProgressTask progressMonitor, Rectangle2D rect);
	 
	/**
	 * Checks this rule for all features of the origin layer in the specified
	 * rectangle 2d.
	 * @param rect
	 */
	public  void checkRule(Rectangle2D rect);
	
	/**
	 * Notifies that this rule has finished validation process.
	 * Useful to free resources, empty caches, etc.
	 */
	public void ruleChecked();
	
	public void validateFeature(IFeature feature);
	
	/**
	 * Sets the topology error container for this rule.
	 * @param errorContainer
	 */
	public void setTopologyErrorContainer(ITopologyErrorContainer errorContainer);
	
	/**
	 * Gets the topology error container for this rule
	 * @return
	 */
	public ITopologyErrorContainer getTopologyErrorContainer();
	
	public void addTopologyError(TopologyError topologyError);
	
	/**
	 * Sets rule unique identifier inside a topology
	 * @param ruleId rule unique identifier
	 */
	public void setId(int ruleId);
	
	public int getId();
	
	/**
	 * sets the owner of the topology rule
	 * @param owner
	 */
	public void setTopology(Topology owner);
	
	/**
	 * returns the owner of the topology rule
	 * @return
	 */
	public Topology getTopology();
	
	/**
	 * Returns the default error symbol for the topology errors caused
	 * by the violation of this rule.
	 * @return A MultiShapeSymbol, because error layer is a multigeometry layer
	 */
	public MultiShapeSymbol getDefaultErrorSymbol();
	
	/**
	 * Returns the actual error symbol for topology errors.
	 * @return
	 */
	public MultiShapeSymbol getErrorSymbol();
	
	/**
	 * Returns a list of automatic error fixes for topology errors caused
	 * by the violation of this rule.
	 * @return
	 */
	public List<ITopologyErrorFix> getAutomaticErrorFixes();
	
	public ITopologyErrorFix getDefaultFixFor(TopologyError topologyError);
	
}
 
