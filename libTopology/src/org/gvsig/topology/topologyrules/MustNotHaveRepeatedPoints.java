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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.RepeatedPointTester;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.errorfixes.RemoveRepeatedCoordsFix;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FMultiPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;


/**
 * This rule checks that a geometry (line or polygon)
 * dont have consecutive repeated points)
 * 
 * @author azabala
 *
 */
public class MustNotHaveRepeatedPoints extends AbstractTopologyRule{

	//TODO Test that internazionalization are running well
	static final String RULE_NAME = Messages.getText("must_not_have_repeated_points");
	
	/**
	 * Symbol for topology errors caused by a violation of this rule.
	 */
	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
	
	
	private static List<ITopologyErrorFix> automaticErrorFixes =
		new ArrayList<ITopologyErrorFix>();
	static{
		automaticErrorFixes.add(new RemoveRepeatedCoordsFix());
	}
	
	
	private static final Color DEFAULT_ERROR_COLOR = Color.BLACK;
	
	
	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = 
		(MultiShapeSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.MULTI, 
											DEFAULT_ERROR_COLOR);
	static{
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
		DEFAULT_ERROR_SYMBOL.setSize(2);
	}

	
	/**
	 * tests if a geometry has repeated points
	 */
	RepeatedPointTester tester = new RepeatedPointTester();
	
	/**
	 * Default constructor
	 *
	 */
	public MustNotHaveRepeatedPoints(Topology topology, FLyrVect originLyr){
		super(topology, originLyr);
	}
	
	public MustNotHaveRepeatedPoints(FLyrVect originLyr){
		super(originLyr);
	}
	
	public MustNotHaveRepeatedPoints(){}
	
	public String getName() {
		return RULE_NAME;
	}

	
	public void checkPreconditions() throws TopologyRuleDefinitionException {
		//This rule doesnt apply to Point vectorial layers.
		try {
			int shapeType = this.originLyr.getShapeType();
			if(shapeType == FShape.POINT)
				throw new TopologyRuleDefinitionException("La regla MustNotHaveRepeatedPoints no aplica a capas de puntos");
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
		if(tester.hasRepeatedPoint(jtsGeometry)){
			Collection<Coordinate> repeatedCoords = tester.getRepeatedCoordinates();
		    int numRepeatedPoints = repeatedCoords.size();
		    double[] x = new double[numRepeatedPoints];
		    double[] y = new double[numRepeatedPoints];
		    Iterator<Coordinate> coordsIt = repeatedCoords.iterator();
		    int count = 0;
		    while(coordsIt.hasNext()){
		    	Coordinate coord = (Coordinate) coordsIt.next();
		    	x[count] = coord.x;
		    	y[count] = coord.y;
		    	count++;
		    }
		    
		    FMultiPoint2D multiPoint = new FMultiPoint2D(x, y);
		    TopologyError topologyError = 
					new TopologyError(multiPoint, 
										this, 
									 feature,
									topology);
			topologyError.setID(errorContainer.getErrorFid());
			addTopologyError(topologyError);
		}//if
		tester.clear();
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
 
