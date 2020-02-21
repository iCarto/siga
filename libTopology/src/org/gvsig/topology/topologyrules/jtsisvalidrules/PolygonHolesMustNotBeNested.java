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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.topologyrules.JtsValidRule;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geomgraph.GeometryGraph;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

public class PolygonHolesMustNotBeNested extends AbstractTopologyRule {

	private static List<ITopologyErrorFix> automaticErrorFixes = new ArrayList<ITopologyErrorFix>();

	private static String RULE_NAME = Messages
			.getText("POLYGON_HOLES_MUST_BE_IN_SHELL");

	private static final Color DEFAULT_ERROR_COLOR = Color.BLACK;

	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = (MultiShapeSymbol) SymbologyFactory
			.createDefaultSymbolByShapeType(FShape.MULTI, DEFAULT_ERROR_COLOR);
	static {
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
	}

	private MultiShapeSymbol errorSymbol;

	private JtsValidRule parentRule;

	public PolygonHolesMustNotBeNested(FLyrVect originLyr) {
		super(originLyr);
	}

	public PolygonHolesMustNotBeNested(Topology topology, FLyrVect originLyr) {
		super(topology, originLyr);
	}

	public PolygonHolesMustNotBeNested() {

	}

	public String getName() {
		return Messages.getText("POLYGON_MUST_NOT_HAVE_DUPLICATED_RINGS");
	}

	public void checkPreconditions() throws TopologyRuleDefinitionException {
		int shapeType;
		try {
			shapeType = this.originLyr.getShapeType();
			int numDimensions = FGeometryUtil.getDimensions(shapeType);
			if (numDimensions != 2)
				throw new TopologyRuleDefinitionException(
						"HolesNotNested solo aplica sobre capas de dimension 2");
		} catch (ReadDriverException e) {
			throw new TopologyRuleDefinitionException(
					"Error al tratar de verificar el tipo de geometria");
		}
	}

	public void validateFeature(IFeature feature) {
		Geometry jtsGeo = NewFConverter.toJtsPolygon((FShape) feature
				.getGeometry().getInternalShape());
		if (jtsGeo instanceof Polygon) {
			checkHoles((Polygon) jtsGeo, new GeometryGraph(0, jtsGeo), feature);
		} else if (jtsGeo instanceof MultiPolygon) {
			MultiPolygon multiPoly = (MultiPolygon) jtsGeo;
			for (int i = 0; i < multiPoly.getNumGeometries(); i++) {
				Polygon polygon = (Polygon) multiPoly.getGeometryN(i);
				checkHoles(polygon, new GeometryGraph(0, polygon), feature);
			}
		} else if (jtsGeo instanceof GeometryCollection) {
			MultiPolygon multiPoly = JtsUtil
					.convertToMultiPolygon((GeometryCollection) jtsGeo);
			for (int i = 0; i < multiPoly.getNumGeometries(); i++) {
				Polygon polygon = (Polygon) multiPoly.getGeometryN(i);
				checkHoles(polygon, new GeometryGraph(0, polygon), feature);
			}
		}
	}

	private void checkHoles(Polygon p, GeometryGraph graph, IFeature feature) {
		/*
		 * Holes only can touch in a single point. If this condition is not
		 * passed, we wont check for nested holes
		 */
		boolean disjointHoles = true;

		class MapEntry {
			int i;
			int j;

			MapEntry(int i, int j) {
				this.i = i;
				this.j = j;
			}

			public boolean equals(Object o) {
				if (!(o instanceof MapEntry))
					return false;
				MapEntry other = (MapEntry) o;

				return (other.i == i && other.j == j)
						|| (other.i == j && other.j == i);
			}

			public int hashCode() {
				return 1;
			}
		}
		HashMap<MapEntry, MapEntry> checkedHoles = new HashMap<MapEntry, MapEntry>();

		for (int i = 0; i < p.getNumInteriorRing(); i++) {
			LinearRing innerHole = (LinearRing) p.getInteriorRingN(i);
			Polygon innerHoleAsPoly = JtsUtil.GEOMETRY_FACTORY.createPolygon(
					innerHole, null);
			for (int j = 0; j < p.getNumInteriorRing(); j++) {
				if (i == j)
					continue;

				MapEntry entry = new MapEntry(i, j);
				if (checkedHoles.get(entry) != null)
					continue;

				checkedHoles.put(entry, entry);

				LinearRing testHole = (LinearRing) p.getInteriorRingN(j);
				Polygon testHoleAsPoly = JtsUtil.GEOMETRY_FACTORY
						.createPolygon(testHole, null);
				// TODO Use PreparedGeometry in the next stable release of JTS
				Geometry intersection = EnhancedPrecisionOp.intersection(
						innerHoleAsPoly, testHoleAsPoly);
				Coordinate[] intersectionCoords = intersection.getCoordinates();
				if (intersectionCoords.length > 1) {
					disjointHoles = false;
					Point2D[] pt2d = FGeometryUtil
							.getCoordinatesAsPoint2D(intersectionCoords);
					IGeometry errorGeometry = FGeometryUtil
							.createFPolygon(pt2d);
					addError(errorGeometry, feature);
				}
			}// for i
		}// for j

		if (!disjointHoles)
			return;

		/*
		 * Holes must not be nested (a hole inside a hole)
		 */
		List<Coordinate> insidePoints = new ArrayList<Coordinate>();
		for (int i = 0; i < p.getNumInteriorRing(); i++) {
			LinearRing innerHole = (LinearRing) p.getInteriorRingN(i);
			Coordinate[] innerHolePts = innerHole.getCoordinates();
			for (int j = 0; j < p.getNumInteriorRing(); j++) {
				// don't test hole against itself!
				if (i == j)
					continue;

				LinearRing searchHole = (LinearRing) p.getInteriorRingN(j);
				// if envelopes don't overlap, holes are not nested
				if (!innerHole.getEnvelopeInternal().intersects(
						searchHole.getEnvelopeInternal()))
					continue;

				Coordinate[] searchHolePts = searchHole.getCoordinates();
				Coordinate innerholePt = JtsUtil.findPtNotNode(innerHolePts,
						searchHole, graph);

				boolean inside = SnapCGAlgorithms.isPointInRing(innerholePt,
						searchHolePts);
				if (inside) {
					insidePoints.add(innerholePt);
				}// if

			}// for j
			if (insidePoints.size() > 0) {
				Coordinate[] coords = new Coordinate[insidePoints.size()];
				insidePoints.toArray(coords);
				Point2D[] pt2d = FGeometryUtil.getCoordinatesAsPoint2D(coords);
				IGeometry geometry = FGeometryUtil.createFPolygon(pt2d);
				addError(geometry, feature);
			}

		}// for i
		// if (!isNonNested) {
		// IFeature[] features = {feature};
		// Coordinate nestedPoint = nestedTester.getNestedPoint();
		// FPoint2D pt = new FPoint2D(nestedPoint.x, nestedPoint.y);
		// IGeometry errorGeometry = ShapeFactory.createGeometry(pt);
		// TopologyError error = new TopologyError(errorGeometry, this,
		// features);
		// addTopologyError(error);
		// }
	}

	private void addError(IGeometry geo, IFeature feature) {
		AbstractTopologyRule violatedRule = null;
		if (this.parentRule != null)
			violatedRule = parentRule;
		else
			violatedRule = this;
		JtsValidTopologyError error = new JtsValidTopologyError(geo, violatedRule, feature,
				topology);
		error.setSecondaryRule(this);
		addTopologyError(error);
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