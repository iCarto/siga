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
package org.gvsig.topology.topologyrules;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

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
import org.gvsig.topology.errorfixes.CreateNewFeatureForGapFix;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.swing.threads.CancellableProgressTask;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonMustNotHaveGaps extends AbstractTopologyRule implements
		IRuleWithClusterTolerance {

	private double clusterTolerance;

	static final String RULE_NAME = Messages.getText("must_not_have_gaps");

	
	static{
		automaticErrorFixes = new ArrayList<ITopologyErrorFix>();
		automaticErrorFixes.add(new CreateNewFeatureForGapFix());
	}
	
	private static final Color DEFAULT_ERROR_COLOR = Color.RED;

	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = (MultiShapeSymbol) SymbologyFactory
			.createDefaultSymbolByShapeType(FShape.MULTI, DEFAULT_ERROR_COLOR);
	static {
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
	}

	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
	
	
	private List<Geometry> geometries;

	/**
	 * Constructor
	 * 
	 * @param topology
	 *            Topology which owns this rule
	 * @param originLyr
	 * @param destinationLyr
	 * @param clusterTolerance
	 */
	public PolygonMustNotHaveGaps(Topology topology, FLyrVect originLyr,
			double clusterTolerance) {
		super(topology, originLyr);
		setClusterTolerance(clusterTolerance);
		geometries = new ArrayList<Geometry>();
	}
	
	public PolygonMustNotHaveGaps(){
		geometries = new ArrayList<Geometry>();
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
	
	
	public void checkRule(CancellableProgressTask progressMonitor){
		super.checkRule(progressMonitor);
		findGaps();
	}
	
	private void findGaps(){
		if(geometries.size() > 0){
			Geometry union = JtsUtil.GEOMETRY_FACTORY.
							createGeometryCollection(geometries.toArray(new Geometry[0])).
							buffer(0d);
			Polygon[] polygons = JtsUtil.extractPolygons(union);
			for (int i = 0; i < polygons.length; i++) {
				Polygon polygon = polygons[i];
				int numberOfGaps = polygon.getNumInteriorRing();
				for(int j = 0; j < numberOfGaps; j++){
					LinearRing gap = (LinearRing) polygon.getInteriorRingN(j);
					Polygon gapPoly = JtsUtil.GEOMETRY_FACTORY.createPolygon(gap, null);
					IGeometry errorGeometry = NewFConverter.toFMap(gapPoly);
					TopologyError error = new TopologyError(errorGeometry, this, null, getTopology());
					addTopologyError(error);
				}
			}
		}
	}
	 
	public  void checkRule(CancellableProgressTask progressMonitor, Rectangle2D rect){
		super.checkRule(progressMonitor, rect);
		findGaps();
	}
	

	public void validateFeature(IFeature feature) {
		IGeometry geom = feature.getGeometry();
		int shapeType = geom.getGeometryType();
		if (shapeType != FShape.CIRCLE && shapeType != FShape.ELLIPSE
				&& shapeType != FShape.MULTI && shapeType != FShape.POLYGON)
			return;
		geometries.add(NewFConverter.toJtsGeometry(geom));
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
	
	
}
