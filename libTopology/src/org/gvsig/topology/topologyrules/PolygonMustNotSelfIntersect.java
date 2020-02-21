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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.jts.SnapLineStringSelfIntersectionChecker;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.IRuleWithClusterTolerance;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.errorfixes.SplitSelfIntersectingPolygonFix;

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
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * The polygons of a layer must not have self intersections
 * 
 */
public class PolygonMustNotSelfIntersect extends AbstractTopologyRule implements
		IRuleWithClusterTolerance {

	static final String RULE_NAME = Messages
			.getText("polygon_must_not_self_intersect");

	private static List<ITopologyErrorFix> automaticErrorFixes =
		new ArrayList<ITopologyErrorFix>();
	static{
		automaticErrorFixes.add(new SplitSelfIntersectingPolygonFix());
	}
	
	private static final Color DEFAULT_ERROR_COLOR = new Color(100, 0, 84);
	
	
	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = 
		(MultiShapeSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.MULTI, 
											DEFAULT_ERROR_COLOR);
	static{
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
		DEFAULT_ERROR_SYMBOL.setSize(5);
	}
	
	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
	
	private double clusterTolerance;

	
	public PolygonMustNotSelfIntersect(Topology topology, FLyrVect originLyr,
			double clusterTolerance) {
		super(topology, originLyr);
		this.clusterTolerance = clusterTolerance;
	}

	public PolygonMustNotSelfIntersect() {
	}

	public void checkPreconditions() throws TopologyRuleDefinitionException {
		try {
			if (FGeometryUtil.getDimensions(originLyr.getShapeType()) != 2)
				throw new TopologyRuleDefinitionException(
						"La capa no es de poligonos o multigeometrias");
		} catch (ReadDriverException e) {
			throw new TopologyRuleDefinitionException(
					"Error de driver al chequear las precondiciones de la regla");
		}
	}

	public String getName() {
		return RULE_NAME;
	}

	public void validateFeature(IFeature feature) {
		IGeometry geom = feature.getGeometry();
		int shapeType = geom.getGeometryType();
		if(FGeometryUtil.getDimensions(shapeType) != 2)
			return;
		Geometry jtsGeom = NewFConverter.toJtsGeometry(geom);
		process(jtsGeom, feature);
	}

	protected void process(Geometry jtsGeom, IFeature feature) {
		if (jtsGeom instanceof MultiPolygon) {
			MultiPolygon multi = (MultiPolygon) jtsGeom;
			int numPol = multi.getNumGeometries();
			for (int i = 0; i < numPol; i++) {
				Polygon pol = (Polygon) multi.getGeometryN(i);
				process(pol, feature);
			}
		} else if (jtsGeom instanceof GeometryCollection) {
			MultiPolygon multi = JtsUtil
					.convertToMultiPolygon((GeometryCollection) jtsGeom);
			process(multi, feature);

		} else if (jtsGeom instanceof Polygon) {
			Polygon pol = (Polygon) jtsGeom;
			List<Coordinate> selfIntersections = new ArrayList<Coordinate>();
			LineString shell = pol.getExteriorRing();
			SnapLineStringSelfIntersectionChecker checker = new SnapLineStringSelfIntersectionChecker(
					shell, getClusterTolerance());
			if (checker.hasSelfIntersections()) {
				Coordinate[] selfIntersection = checker.getSelfIntersections();
				selfIntersections.addAll(Arrays.asList(selfIntersection));
			}

			int numHoles = pol.getNumInteriorRing();
			for (int i = 0; i < numHoles; i++) {
				LineString hole = pol.getInteriorRingN(i);
				checker = new SnapLineStringSelfIntersectionChecker(hole,
						getClusterTolerance());
				if (checker.hasSelfIntersections()) {
					Coordinate[] selfIntersection = checker
							.getSelfIntersections();
					selfIntersections.addAll(Arrays.asList(selfIntersection));
				}// if
			}// for

			if (selfIntersections.size() > 0) {
				Iterator<Coordinate> coordsIt = selfIntersections.iterator();
				int count = 0;
				int numberOfSelfCoords = selfIntersections.size();
				double[] x = new double[numberOfSelfCoords];
				double[] y = new double[numberOfSelfCoords];
				while (coordsIt.hasNext()) {
					Coordinate coord = (Coordinate) coordsIt.next();
					x[count] = coord.x;
					y[count] = coord.y;
					count++;
				}

				FMultiPoint2D multiPoint = new FMultiPoint2D(x, y);
				TopologyError topologyError = new TopologyError(multiPoint,
													this, feature, topology);
				topologyError.setID(errorContainer.getErrorFid());
				addTopologyError(topologyError);
			}//if
		}// if polygon
	}

	public double getClusterTolerance() {
		return clusterTolerance;
	}

	public void setClusterTolerance(double clusterTolerance) {
		this.clusterTolerance = clusterTolerance;
	}

	public boolean acceptsOriginLyr(FLyrVect lyr) {
		try {
			return FGeometryUtil.getDimensions(lyr.getShapeType()) == 2;
		} catch (ReadDriverException e) {
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
