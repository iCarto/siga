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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.IRuleWithClusterTolerance;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.TopologyRuleDefinitionException;
import org.gvsig.topology.errorfixes.CreateFeatureOverlapPolygonFix;
import org.gvsig.topology.errorfixes.MergeOverlapPolygonFix;
import org.gvsig.topology.errorfixes.SubstractOverlapPolygonFix;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

/**
 * The polygons of a given layer must not overlaps each other (their
 * intersection must be a line or a point, not an area)
 * 
 */
public class PolygonMustNotOverlap extends AbstractTopologyRule implements
		IRuleWithClusterTolerance {

	private double clusterTolerance;

	static final String RULE_NAME = Messages.getText("must_not_overlap");

	private static List<ITopologyErrorFix> automaticErrorFixes = new ArrayList<ITopologyErrorFix>();
	static {
		automaticErrorFixes.add(new SubstractOverlapPolygonFix());
		automaticErrorFixes.add(new MergeOverlapPolygonFix());
		automaticErrorFixes.add(new CreateFeatureOverlapPolygonFix());
	}

	private static final Color DEFAULT_ERROR_COLOR = Color.BLACK;

	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = (MultiShapeSymbol) SymbologyFactory
			.createDefaultSymbolByShapeType(FShape.MULTI, DEFAULT_ERROR_COLOR);
	static {
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
	}

	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;

	protected Map<ComputedTopologyError, ComputedTopologyError> errorEntries;

	/**
	 * Constructor
	 * 
	 * @param topology
	 *            Topology which owns this rule
	 * @param originLyr
	 * @param destinationLyr
	 * @param clusterTolerance
	 */
	public PolygonMustNotOverlap(Topology topology, FLyrVect originLyr,
			double clusterTolerance) {
		super(topology, originLyr);
		setClusterTolerance(clusterTolerance);

		errorEntries = new HashMap<ComputedTopologyError, ComputedTopologyError>();
	}

	public PolygonMustNotOverlap() {
		errorEntries = new HashMap<ComputedTopologyError, ComputedTopologyError>();
	}

	public String getName() {
		return RULE_NAME;
	}

	public void checkPreconditions() throws TopologyRuleDefinitionException {
		try {
			int shapeType = this.originLyr.getShapeType();
			if (FGeometryUtil.getDimensions(shapeType) != 2)
				throw new TopologyRuleDefinitionException(
						"PolygonMustNotOverlap requires a polygonal geometry type");
		} catch (ReadDriverException e) {
			e.printStackTrace();
			throw new TopologyRuleDefinitionException(
					"Error leyendo el tipo de geometria del driver", e);
		}
	}

	public void validateFeature(IFeature feature) {
		IGeometry geom = feature.getGeometry();
		int shapeType = geom.getGeometryType();
		if (shapeType != FShape.CIRCLE && shapeType != FShape.ELLIPSE
				&& shapeType != FShape.MULTI && shapeType != FShape.POLYGON)
			return;
		process(geom, feature);
	}

	/**
	 * Entry class in a map of detected errors, to avoid to change two times the
	 * same error (for example, if a OVERLAPS b, create an error for a and an
	 * error for b)
	 * 
	 * @author Alvaro Zabala
	 * 
	 */
	class ComputedTopologyError {
		String firstFeature;
		String secondFeature;

		public boolean equals(Object o) {
			if (!(o instanceof ComputedTopologyError))
				return false;

			ComputedTopologyError other = (ComputedTopologyError) o;

			return (other.firstFeature.equalsIgnoreCase(firstFeature) &&
				   other.secondFeature.equalsIgnoreCase(secondFeature)) ||
				   (other.firstFeature.equalsIgnoreCase(secondFeature) &&
						   other.secondFeature.equalsIgnoreCase(firstFeature));
				   
		}

		public int hashCode() {
			return 1;
		}
	}
	
	public void ruleChecked(){
		errorEntries.clear();
	}

	protected void process(IGeometry geometry, IFeature feature) {
		Geometry jtsGeom = NewFConverter.toJtsGeometry(geometry);
		Polygon[] polygons = JtsUtil.extractPolygons(jtsGeom);
		for (int i = 0; i < polygons.length; i++) {

			Polygon polygon = polygons[i];
			Envelope env1 = polygon.getEnvelopeInternal();

			// We try to extend a bit the original envelope to ensure
			// recovering geometries in the limit

			double delta = clusterTolerance + 10;
			double minX = env1.getMinX() - delta;
			double minY = env1.getMinY() - delta;
			double maxX = env1.getMaxX() + delta;
			double maxY = env1.getMaxY() + delta;

			Rectangle2D rect = new Rectangle2D.Double(minX, minY, maxX - minX,
					maxY - minY);
			try {
				IFeatureIterator neighbours = originLyr.getSource()
						.getFeatureIterator(rect, null, null, false);
				while (neighbours.hasNext()) {
					IFeature neighbourFeature = neighbours.next();
					if (neighbourFeature.getID().equalsIgnoreCase(
							feature.getID()))
						continue;
					IGeometry geom2 = neighbourFeature.getGeometry();
					Rectangle2D rect2 = geom2.getBounds2D();
					if (rect.intersects(rect2)) {
						Geometry jts2 = NewFConverter.toJtsGeometry(geom2);
						Polygon[] geometriesToProcess = JtsUtil
								.extractPolygons(jts2);

						for (int j = 0; j < geometriesToProcess.length; j++) {
							Polygon poly2 = geometriesToProcess[j];
							//we check if overlaps or if one polygon contains the other
							if (poly2.overlaps(polygon) || poly2.covers(polygon) || polygon.covers(poly2)) {
								ComputedTopologyError errorEntry = new ComputedTopologyError();
								errorEntry.firstFeature = feature.getID();
								errorEntry.secondFeature = neighbourFeature.getID();

								if(! this.errorEntries.containsKey(errorEntry)){
									Geometry errorGeomJts = EnhancedPrecisionOp.intersection(poly2, polygon);
									Polygon[] overlappingPolygons = JtsUtil.extractPolygons(errorGeomJts);
									if(overlappingPolygons.length > 0){
										errorGeomJts = JtsUtil.GEOMETRY_FACTORY.createGeometryCollection(overlappingPolygons);
										IGeometry errorGeom = NewFConverter.toFMap(errorGeomJts);
										TopologyError topologyError = 
											new TopologyError(errorGeom, 
														errorContainer.getErrorFid(), 
														this,  
														feature, 
														neighbourFeature, 
														topology );
										addTopologyError(topologyError);
										
										errorEntries.put(errorEntry, errorEntry);
									}//if length > 0
								}//if
							}// if
						}// for
					}// if
				}// while

			} catch (ReadDriverException e) {
				e.printStackTrace();
				return;
			}

		}
	}

	public double getClusterTolerance() {
		return clusterTolerance;
	}

	public void setClusterTolerance(double clusterTolerance) {
		this.clusterTolerance = clusterTolerance;
	}

	public boolean acceptsOriginLyr(FLyrVect lyr) {
		try {
			int shapeType = lyr.getShapeType();
			return (FGeometryUtil.getDimensions(shapeType) == 2);
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
		return automaticErrorFixes.get(2);
	}

}
