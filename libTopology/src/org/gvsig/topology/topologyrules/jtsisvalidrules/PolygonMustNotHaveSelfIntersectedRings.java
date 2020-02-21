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
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.jts.SnapLineStringSelfIntersectionChecker;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.WrongLyrForTopologyException;
import org.gvsig.topology.errorfixes.ExtendDangleToNearestBoundaryPointFix;
import org.gvsig.topology.errorfixes.ExtendDangleToNearestVertexFix;
import org.gvsig.topology.topologyrules.JtsValidRule;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.XMLEntity;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonMustNotHaveSelfIntersectedRings extends AbstractTopologyRule{ 
	
	private static String RULE_NAME = Messages.getText("POLYGON_MUST_NOT_HAVE_INTERSECTED_RINGS");
	/**
	 * Symbol for topology errors caused by a violation of this rule.
	 */
	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
	
	
	private static List<ITopologyErrorFix> automaticErrorFixes =
		new ArrayList<ITopologyErrorFix>();
	static{
		automaticErrorFixes.add(new ExtendDangleToNearestBoundaryPointFix());
		automaticErrorFixes.add(new ExtendDangleToNearestVertexFix());
	}
	
	
	private static final Color DEFAULT_ERROR_COLOR = Color.RED;
	
	
	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = 
		(MultiShapeSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.MULTI, 
											DEFAULT_ERROR_COLOR);
	static{
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
		DEFAULT_ERROR_SYMBOL.setSize(4);
	}
	
	private double snapTolerance;
	
	private JtsValidRule parentRule;
	
	public PolygonMustNotHaveSelfIntersectedRings(Topology topology, FLyrVect lyr, double snapTolerance){
		super(topology, lyr);
		this.snapTolerance = snapTolerance;
	}
	
	public PolygonMustNotHaveSelfIntersectedRings(){}
	
	public PolygonMustNotHaveSelfIntersectedRings(FLyrVect lyr, double snapTolerance){
		this(null, lyr, snapTolerance);
	}
	
	public String getName() {
		return RULE_NAME;
	}

	public void checkPreconditions() throws TopologyRuleDefinitionException {
		int shapeType;
		try {
			shapeType = this.originLyr.getShapeType();
			int numDimensions = FGeometryUtil.getDimensions(shapeType);
			if(numDimensions != 2)
				throw new WrongLyrForTopologyException("MustNotHaveSelfIntersectedRings solo aplica sobre capas de dimension 2");
		} catch (ReadDriverException e) {
			throw new TopologyRuleDefinitionException(
					"Error al tratar de verificar el tipo de geometria");
		}
	}

	public void validateFeature(IFeature feature) {
		Geometry jtsGeo = NewFConverter.toJtsGeometry(feature.getGeometry());
		if (jtsGeo instanceof Polygon) {
			Polygon polygon = (Polygon) jtsGeo;
			checkPolygon(polygon, feature);
		} else if (jtsGeo instanceof MultiPolygon) {
			MultiPolygon multiPoly = (MultiPolygon) jtsGeo;
			for (int i = 0; i < multiPoly.getNumGeometries(); i++) {
				Polygon polygon = (Polygon) multiPoly.getGeometryN(i);
				checkPolygon(polygon, feature);
			}
		} else if (jtsGeo instanceof GeometryCollection) {
			MultiPolygon multiPoly = JtsUtil
					.convertToMultiPolygon((GeometryCollection) jtsGeo);
			for (int i = 0; i < multiPoly.getNumGeometries(); i++) {
				Polygon polygon = (Polygon) multiPoly.getGeometryN(i);
				checkPolygon(polygon, feature);
			}
		}
	}
	
	
	private  void checkPolygon(Polygon polygon, IFeature feature){
		LineString shell = polygon.getExteriorRing();
		SnapLineStringSelfIntersectionChecker checker =
			new SnapLineStringSelfIntersectionChecker(shell, snapTolerance);
		if(checker.hasSelfIntersections()){
			Coordinate[] selfIntersections = checker.getSelfIntersections();
			addError(selfIntersections, feature);
		}
		
		int numHoles = polygon.getNumInteriorRing();
		for(int i = 0; i < numHoles; i++){
			LineString hole = polygon.getInteriorRingN(i);
			checker = new SnapLineStringSelfIntersectionChecker(hole, snapTolerance);
			if(checker.hasSelfIntersections()){
				Coordinate[] selfIntersections = checker.getSelfIntersections();
				addError(selfIntersections, feature);
			}//if
		}//for
	}
	
	private void addError(Coordinate[] selfIntersections, IFeature feature){
		double[] x = new double[selfIntersections.length];
		double[] y = new double[selfIntersections.length];
		for(int i = 0; i < selfIntersections.length; i++){
			x[i] = selfIntersections[i].x;
			y[i] = selfIntersections[i].y;
		}
//		FMultiPoint2D errorGeo = new FMultiPoint2D(x, y);
		
		//FIXME USAR MULTIPOINT CUANDO FUNCIONE
		com.iver.cit.gvsig.fmap.core.FPoint2D point = 
			new com.iver.cit.gvsig.fmap.core.FPoint2D(x[0], y[0]);
		com.iver.cit.gvsig.fmap.core.IGeometry errorGeo = com.iver.cit.gvsig.fmap.core.ShapeFactory.createGeometry(point);
				
		AbstractTopologyRule violatedRule = null;
		if(this.parentRule != null)
			violatedRule = parentRule;
		else
			violatedRule = this;
		JtsValidTopologyError error = new JtsValidTopologyError(errorGeo, violatedRule, feature, topology );
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
			return FGeometryUtil.getDimensions(lyr.getShapeType()) == 2;
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
}