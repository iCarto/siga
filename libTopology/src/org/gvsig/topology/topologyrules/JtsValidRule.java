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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.IRuleWithClusterTolerance;
import org.gvsig.topology.ITopologyErrorContainer;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.ITopologyRule;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.TopologyRuleFactory;
import org.gvsig.topology.errorfixes.DeleteTopologyErrorFix;
import org.gvsig.topology.topologyrules.jtsisvalidrules.GeometryMustHaveValidCoordinates;
import org.gvsig.topology.topologyrules.jtsisvalidrules.GeometryMustNotHaveFewPoints;
import org.gvsig.topology.topologyrules.jtsisvalidrules.IGeometryMustBeClosed;
import org.gvsig.topology.topologyrules.jtsisvalidrules.MultiPolygonMustNotHaveNestedShells;
import org.gvsig.topology.topologyrules.jtsisvalidrules.PolygonHolesMustBeInShell;
import org.gvsig.topology.topologyrules.jtsisvalidrules.PolygonHolesMustNotBeNested;
import org.gvsig.topology.topologyrules.jtsisvalidrules.PolygonMustHaveConnectedInterior;
import org.gvsig.topology.topologyrules.jtsisvalidrules.PolygonMustNotHaveDuplicatedRings;
import org.gvsig.topology.topologyrules.jtsisvalidrules.PolygonMustNotHaveSelfIntersectedRings;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.XMLEntity;

/**
 * Any validated geometry must be a right JTS geometry.
 * 
 * A JTS geometry to be valid must check:
 * <ul>
 * <li>Must pass IsValidOp.</li>
 * <li>Polygon's shells must not selfintersect.</li>
 * <li>Polygon's holes must not selfintersect.</li>
 * <li>Polygon's rings must be closed.</li>
 * <li>Polygon's with only three collinear points are not allowed
 *  (this is a collapsed polygon to a line)</li>
 * <li>Polygon's shell must have coordinates in CCW</li>
 * <li>Polygon's holes must have coordinates in CCCW</li>
 * <li>Polygon's holes must not touch in more than one point</li>
 * <li>Polygon's holes could not be null</li>
 * <li>Polygon's holes must be contained by polygon shell</li>
 * <li>If a polygon has its exterior ring to null, but it has a hole, probably
   the order of the coordinates must be inverted.</li>
   <li>A polygon must not have repeated holes</li>
   <li>A polyline cant have two equals points.
   </ul>
 *
 */
public class JtsValidRule extends AbstractTopologyRule implements IRuleWithClusterTolerance {

	private static String RULE_NAME = Messages.getText("must_be_jts_valid");
	
	
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
	
	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;

	private static Logger logger = Logger.getLogger(JtsValidRule.class.getName());
	
	List<ITopologyRule> jtsRules = new ArrayList<ITopologyRule>();
	
	
	private double snapTolerance;
	
	
	public JtsValidRule(FLyrVect originLyr, double snapTolerance){
		this(null, originLyr, snapTolerance);
	}
	
	public JtsValidRule(){}
	
	public JtsValidRule(Topology topology, FLyrVect originLyr, double snapTolerance){
		super(topology, originLyr);
		setClusterTolerance(snapTolerance);
	}
	
	public void setOriginLyr(FLyrVect originLyr){
		super.setOriginLyr(originLyr);
		initialize();
	}
	
	public void setTopology(Topology topology) {
		this.topology = topology;
		for(int i = 0; i < jtsRules.size(); i++){
			jtsRules.get(i).setTopology(topology);
		}
	}
	
	public void setTopologyErrorContainer(ITopologyErrorContainer errorContainer){
		super.setTopologyErrorContainer(errorContainer);
		Iterator<ITopologyRule> iterator = jtsRules.iterator();
		while(iterator.hasNext()){
			ITopologyRule rule = iterator.next();
			rule.setTopologyErrorContainer(this.errorContainer);
		}
	}
	
	private void initialize() {
		try {
			int shapeType = this.getOriginLyr().getShapeType();
			
			switch(shapeType){
			case FShape.POINT:
			case FShape.TEXT:
			case FShape.MULTIPOINT:
				jtsRules.add(getValidCoordsRule());
				break;
			
			
			case FShape.ARC:
			case FShape.CIRCLE:
			case FShape.ELLIPSE:
			case FShape.LINE:
				jtsRules.add(getValidCoordsRule());
				jtsRules.add(getFewPointsRule());
				break;
				
			
			case FShape.MULTI://If type is multi, it will have all rules
				jtsRules.add(getValidCoordsRule());
				jtsRules.add(getFewPointsRule());
				jtsRules.add(getClosedRingsRule());
				jtsRules.add(getHolesInShellRule());
				jtsRules.add(getHolesNotNestedRule());
				jtsRules.add(getIntersectingRingsRule());
				jtsRules.add(getNestedShellsRule());
				jtsRules.add(getNotDuplicatedRingsRule());
				jtsRules.add(getSelfIntersectingRingRule());
				
			break;
			
			case FShape.POLYGON://polygon geometries wont check for nested shells
				jtsRules.add(getValidCoordsRule());
				jtsRules.add(getFewPointsRule());
				jtsRules.add(getClosedRingsRule());
				jtsRules.add(getHolesInShellRule());
				jtsRules.add(getHolesNotNestedRule());
				jtsRules.add(getIntersectingRingsRule());
				jtsRules.add(getNotDuplicatedRingsRule());
				jtsRules.add(getSelfIntersectingRingRule());
				break;
			
			case FShape.NULL:
				return;
		}
			
		} catch (ReadDriverException e) {
			logger.error("Error initializing JTS valid rule. "+
					"Couldnt read shape type of layer", e);
		}
	}
	
	
	private GeometryMustHaveValidCoordinates getValidCoordsRule(){
		GeometryMustHaveValidCoordinates rule = new GeometryMustHaveValidCoordinates(topology, originLyr);
		rule.setTopologyErrorContainer(this.getTopologyErrorContainer());
		rule.setParentRule(this);
		return rule;
	}
	
	private GeometryMustNotHaveFewPoints getFewPointsRule(){
		GeometryMustNotHaveFewPoints rule = new GeometryMustNotHaveFewPoints(topology, originLyr);
		rule.setTopologyErrorContainer(this.getTopologyErrorContainer());
		rule.setParentRule(this);
		return rule;
		
	}
	
	private IGeometryMustBeClosed getClosedRingsRule(){
		IGeometryMustBeClosed rule = new IGeometryMustBeClosed(topology, originLyr, snapTolerance);
		rule.setTopologyErrorContainer(this.getTopologyErrorContainer());
		rule.setParentRule(this);
		return rule;
	}
	
	private MultiPolygonMustNotHaveNestedShells getNestedShellsRule(){
		MultiPolygonMustNotHaveNestedShells rule = 
			new MultiPolygonMustNotHaveNestedShells(topology, originLyr);
		rule.setTopologyErrorContainer(this.getTopologyErrorContainer());
		rule.setParentRule(this);
		return rule;
		
	}
	
	private PolygonHolesMustBeInShell getHolesInShellRule(){
		PolygonHolesMustBeInShell rule = new PolygonHolesMustBeInShell(topology, originLyr, snapTolerance);
		rule.setTopologyErrorContainer(this.getTopologyErrorContainer());
		rule.setParentRule(this);
		return rule;	
	}
	
	private PolygonHolesMustNotBeNested getHolesNotNestedRule(){
		PolygonHolesMustNotBeNested rule = new PolygonHolesMustNotBeNested(topology, originLyr);
		rule.setTopologyErrorContainer(this.getTopologyErrorContainer());
		rule.setParentRule(this);
		return rule;
	}
	
	private PolygonMustNotHaveDuplicatedRings getNotDuplicatedRingsRule(){
		PolygonMustNotHaveDuplicatedRings rule = 
			new PolygonMustNotHaveDuplicatedRings(topology, originLyr, snapTolerance);
		rule.setTopologyErrorContainer(this.getTopologyErrorContainer());
		rule.setParentRule(this);
		return rule;
	}
	
	private PolygonMustHaveConnectedInterior getIntersectingRingsRule(){
		PolygonMustHaveConnectedInterior rule = new PolygonMustHaveConnectedInterior(topology, originLyr);
		rule.setTopologyErrorContainer(this.getTopologyErrorContainer());
		rule.setParentRule(this);
		return rule;
	}
	
	private PolygonMustNotHaveSelfIntersectedRings getSelfIntersectingRingRule(){
		PolygonMustNotHaveSelfIntersectedRings rule = new PolygonMustNotHaveSelfIntersectedRings(topology, originLyr, snapTolerance);
		rule.setTopologyErrorContainer(this.getTopologyErrorContainer());
		rule.setParentRule(this);
		return rule;
	}
	
	
	public void validateFeature(IFeature feature) {
		 Iterator<ITopologyRule> it = this.jtsRules.iterator();
		 while(it.hasNext()){
			 ITopologyRule rule = it.next();
			 rule.validateFeature(feature);
		 }
	}

	public String getName() {
		return RULE_NAME;
	}
	/*
	 * This rule accepts point, line and polygon layers.
	 *  TODO Maybe must we check for getNumGeometries() > 0
	 * for layer??
	 * 
	 * */
	public void checkPreconditions() {
	}
	
	public XMLEntity getXMLEntity(){
		XMLEntity xml = super.getXMLEntity();
		xml.putProperty("numberOfRules", jtsRules.size());
		Iterator<ITopologyRule> rulesIt = jtsRules.iterator();
		while(rulesIt.hasNext()){
			ITopologyRule rule = rulesIt.next();
			xml.addChild(rule.getXMLEntity());
		}
		return xml;
	}
	    
	public void setXMLEntity(XMLEntity xml){
		super.setXMLEntity(xml);
		if(xml.contains("numberOfRules")){
			int numberOfRules = xml.getIntProperty("numberOfRules");
			for(int i = 0; i < numberOfRules; i++){
				XMLEntity subRuleXML = xml.getChild(i);
				//TODO Incluir llamada a RuleFactory y add(rule)
				ITopologyRule rule = TopologyRuleFactory.createFromXML(topology, subRuleXML);
				jtsRules.add(rule);
			}//for
		}//if
	}

	public double getClusterTolerance() {
		return this.snapTolerance;
	}

	public void setClusterTolerance(double clusterTolerance) {
		this.snapTolerance = clusterTolerance;
		initialize();
	}

	public boolean acceptsOriginLyr(FLyrVect originLyr) {
		return true;
	}

	public List<ITopologyErrorFix> getAutomaticErrorFixes() {
		return errorFixes;
	}

	public MultiShapeSymbol getDefaultErrorSymbol() {
		return DEFAULT_ERROR_SYMBOL;
	}

	public MultiShapeSymbol getErrorSymbol() {
		return errorSymbol;
	}
	
	public ITopologyErrorFix getDefaultFixFor(TopologyError topologyError){
		return errorFixes.get(0);
	}
}
 
