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
import org.gvsig.topology.errorfixes.CompleteUndershootFix;
import org.gvsig.topology.topologyrules.JtsValidRule;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.XMLEntity;

/**
 * This rule checks if an FMap geometry is closed, always applying a 
 * given snap tolerance.
 * 
 * To check multigeometries and multipart geometries, we follow the
 * next guideline:
 * 
 * a) if geometry is multipart or multigeometry, to be closed all parts must be closed.
 * b) point geometries are not closed.
 *
 * @author Alvaro Zabala
 *
 */
public class IGeometryMustBeClosed extends AbstractTopologyRule {
		
	static final List<ITopologyErrorFix> automaticFixes = new ArrayList<ITopologyErrorFix>();
	static{
		automaticFixes.add(new CompleteUndershootFix());
	}
	
	private static final Color DEFAULT_ERROR_COLOR = Color.RED;
	
	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = 
		(MultiShapeSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.MULTI, 
											DEFAULT_ERROR_COLOR);
	static{
		DEFAULT_ERROR_SYMBOL.setSize(0.5);
		DEFAULT_ERROR_SYMBOL.setLineWidth(0.5);
		DEFAULT_ERROR_SYMBOL.getOutline().setLineColor(DEFAULT_ERROR_COLOR);
		DEFAULT_ERROR_SYMBOL.setFillColor(DEFAULT_ERROR_COLOR);
	}
	
	/**
	 * Symbol for topology errors caused by a violation of this rule.
	 */
	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
	
	
	
	private double snapTolerance;
	
	private JtsValidRule parentRule;
	
	public IGeometryMustBeClosed(Topology topology, FLyrVect originLyr, double snapTolerance) {
		super(topology, originLyr);
		this.snapTolerance = snapTolerance;
	}
	
	public IGeometryMustBeClosed(){}
	
	public IGeometryMustBeClosed(FLyrVect originLyr, double snapTolerance) {
		this(null, originLyr, snapTolerance);
	}

	public String getName() {
		return Messages.getText("LINEAR_RING_MUST_BE_CLOSED");
	}

	public void checkPreconditions() throws TopologyRuleDefinitionException {
		try {
			int shapeType = this.originLyr.getShapeType();
			if(shapeType == FShape.POINT || 
					shapeType == FShape.MULTIPOINT || 
					shapeType == FShape.TEXT)
				throw new TopologyRuleDefinitionException();
		} catch (ReadDriverException e) {
			e.printStackTrace();
			throw new TopologyRuleDefinitionException("Error leyendo el tipo de geometria del driver",e);
		}	
	}

	public void validateFeature(IFeature feature) {
		IGeometry geometry = feature.getGeometry();
		if(! FGeometryUtil.isClosed(geometry, snapTolerance)){
			IGeometry errorGeometry = FGeometryUtil.
						getGeometryToClose(geometry, snapTolerance);
			addTopologyError(feature, errorGeometry);
		}
	}
	

	private void addTopologyError(IFeature errorFeature, IGeometry errorGeometry) {
		AbstractTopologyRule violatedRule = null;
		if(this.parentRule != null)
			violatedRule = parentRule;
		else
			violatedRule = this;
		JtsValidTopologyError error = 
			new JtsValidTopologyError(errorGeometry, violatedRule, errorFeature, topology);
		error.setSecondaryRule(this);
		addTopologyError(error);
	}
	
	public XMLEntity getXMLEntity(){
		XMLEntity xml = super.getXMLEntity();
		xml.putProperty("snapTolerance", snapTolerance);
		return xml;
	}
	    
	public void setXMLEntity(XMLEntity xml){
		super.setXMLEntity(xml);
		
		if(xml.contains("snapTolerance")){
			snapTolerance = xml.getDoubleProperty("snapTolerance");
		}
	}

	public JtsValidRule getParentRule() {
		return parentRule;
	}

	public void setParentRule(JtsValidRule parentRule) {
		this.parentRule = parentRule;
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