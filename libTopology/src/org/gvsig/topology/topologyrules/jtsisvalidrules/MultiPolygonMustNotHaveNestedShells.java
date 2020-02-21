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

import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.topologyrules.JtsValidRule;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geomgraph.GeometryGraph;

public class MultiPolygonMustNotHaveNestedShells extends AbstractTopologyRule {

	static final String RULE_NAME = Messages.getText("MULTIPOL_NOT_HAVE_NESTED_SHELLS");

	private static List<ITopologyErrorFix> automaticErrorFixes = new ArrayList<ITopologyErrorFix>();
	

	private static final Color DEFAULT_ERROR_COLOR = Color.BLACK;

	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = 
		(MultiShapeSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.MULTI, 
											DEFAULT_ERROR_COLOR);
	static{
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
		DEFAULT_ERROR_SYMBOL.setSize(2);
	}


	private MultiShapeSymbol errorSymbol;
	
	JtsValidRule parentRule;
	
	public JtsValidRule getParentRule() {
		return parentRule;
	}

	public void setParentRule(JtsValidRule parentRule) {
		this.parentRule = parentRule;
	}

	public MultiPolygonMustNotHaveNestedShells(Topology topology,
			FLyrVect originLyr) {
		super(topology, originLyr);
	}
	
	public MultiPolygonMustNotHaveNestedShells(FLyrVect originLyr) {
		super(originLyr);
	}
	
	public MultiPolygonMustNotHaveNestedShells(){}

	public String getName() {
		return Messages.getText("POLYGON_MUST_NOT_HAVE_DUPLICATED_RINGS");
	}

	public void checkPreconditions() throws TopologyRuleDefinitionException {
		int shapeType;
		try {
			shapeType = this.originLyr.getShapeType();
			if (shapeType != FShape.MULTI)
				throw new TopologyRuleDefinitionException(
						"La regla ShellsNotNested solo aplica sobre multipoligonos");
		} catch (ReadDriverException e) {
			throw new TopologyRuleDefinitionException(
					"Error al tratar de verificar el tipo de geometria");
		}
	}

	private void checkShellsNotNested(MultiPolygon mp, GeometryGraph graph,
			IFeature feature) {
		for (int i = 0; i < mp.getNumGeometries(); i++) {
			Polygon p = (Polygon) mp.getGeometryN(i);
			LinearRing shell = (LinearRing) p.getExteriorRing();
			for (int j = 0; j < mp.getNumGeometries(); j++) {
				if (i == j)
					continue;
				Polygon p2 = (Polygon) mp.getGeometryN(j);
				checkShellNotNested(shell, p2, graph, feature);
			}// for
		}// for
	}

	/**
	 * Check if a shell is incorrectly nested within a polygon. This is the case
	 * if the shell is inside the polygon shell, but not inside a polygon hole.
	 * (If the shell is inside a polygon hole, the nesting is valid.)
	 * <p>
	 * The algorithm used relies on the fact that the rings must be properly
	 * contained. E.g. they cannot partially overlap (this has been previously
	 * checked by <code>checkRelateConsistency</code> )
	 */
	private void checkShellNotNested(LinearRing shell, Polygon p,
			GeometryGraph graph, IFeature feature) {

		Coordinate[] shellPts = shell.getCoordinates();
		// test if shell is inside polygon shell
		LinearRing polyShell = (LinearRing) p.getExteriorRing();
		Coordinate[] polyPts = polyShell.getCoordinates();
		Coordinate shellPt = JtsUtil.findPtNotNode(shellPts, polyShell, graph);
		// if no point could be found, we can assume that the shell is outside
		// the polygon
		if (shellPt == null)
			return;
		boolean insidePolyShell = CGAlgorithms.isPointInRing(shellPt, polyPts);
		if (!insidePolyShell)
			return;

		// if no holes, this is an error!
		if (p.getNumInteriorRing() <= 0) {
			FPoint2D point = new FPoint2D(shellPt.x, shellPt.y);
			IGeometry errorGeometry = ShapeFactory.createGeometry(point);
			AbstractTopologyRule violatedRule = null;
			if(this.parentRule != null)
				violatedRule = parentRule;
			else
				violatedRule = this;
			JtsValidTopologyError topologyError = 
				new JtsValidTopologyError(errorGeometry,violatedRule, feature, topology);
			topologyError.setSecondaryRule(this);
			addTopologyError(topologyError);
		}

		/**
		 * Check if the shell is inside one of the holes. This is the case if
		 * one of the calls to checkShellInsideHole returns a null coordinate.
		 * Otherwise, the shell is not properly contained in a hole, which is an
		 * error.
		 */
		Coordinate badNestedPt = null;
		for (int i = 0; i < p.getNumInteriorRing(); i++) {
			LinearRing hole = (LinearRing) p.getInteriorRingN(i);
			badNestedPt = checkShellInsideHole(shell, hole, graph);
			if (badNestedPt == null)
				return;
		}
		FPoint2D point = new FPoint2D(badNestedPt.x, badNestedPt.y);
		IGeometry errorGeometry = ShapeFactory.createGeometry(point);
		AbstractTopologyRule violatedRule = null;
		if(this.parentRule != null)
			violatedRule = parentRule;
		else
			violatedRule = this;
		JtsValidTopologyError error = 
			new JtsValidTopologyError(errorGeometry, violatedRule, feature, topology);
		error.setSecondaryRule(this);
		addTopologyError(error);
	}

	private Coordinate checkShellInsideHole(LinearRing shell, LinearRing hole,
			GeometryGraph graph) {
		Coordinate[] shellPts = shell.getCoordinates();
		Coordinate[] holePts = hole.getCoordinates();
		// TODO: improve performance of this - by sorting pointlists for
		// instance?
		Coordinate shellPt = JtsUtil.findPtNotNode(shellPts, hole, graph);
		// if point is on shell but not hole, check that the shell is inside the
		// hole
		if (shellPt != null) {
			boolean insideHole = CGAlgorithms.isPointInRing(shellPt, holePts);
			if (!insideHole) {
				return shellPt;
			}
		}
		Coordinate holePt = JtsUtil.findPtNotNode(holePts, shell, graph);
		// if point is on hole but not shell, check that the hole is outside the
		// shell
		if (holePt != null) {
			boolean insideShell = CGAlgorithms.isPointInRing(holePt, shellPts);
			if (insideShell) {
				return holePt;
			}
			return null;
		}
		return null;
	}

	

	public void validateFeature(IFeature feature) {
		Geometry geometry = feature.getGeometry().toJTSGeometry();
		if(geometry instanceof MultiPolygon){
			MultiPolygon multiPolygon = (MultiPolygon) geometry;
			checkShellsNotNested(multiPolygon, new GeometryGraph(0, multiPolygon), feature);
		}else if(geometry instanceof GeometryCollection){
			GeometryCollection geomCol = (GeometryCollection) geometry;
			MultiPolygon multiPol = JtsUtil.convertToMultiPolygon(geomCol);
			if(multiPol.getNumGeometries() > 0)
			{
				checkShellsNotNested(multiPol, new GeometryGraph(0, multiPol), feature );
			}
		}	
	}
	
	public boolean acceptsOriginLyr(FLyrVect lyr) {
		try {
			return lyr.getShapeType() == FShape.MULTI;
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