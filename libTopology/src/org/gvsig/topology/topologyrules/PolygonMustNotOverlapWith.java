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
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.ITwoLyrRule;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.errorfixes.MergeOverlapWithPolygonFix;
import org.gvsig.topology.errorfixes.SubstractOverlapPolygonWithFix;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.XMLEntity;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

/**
 * Topololy rule that checks than polygon geometries of origin layer
 * doesnt checks with polygon geometries of destination layer.
 * 
 * @author Alvaro Zabala
 *
 */
public class PolygonMustNotOverlapWith extends PolygonMustNotOverlap 
										implements ITwoLyrRule{
	final static String RULE_NAME = Messages.getText("must_not_overlap_with");
	
	FLyrVect destinationLyr;
	
	private static List<ITopologyErrorFix> automaticErrorFixes = new ArrayList<ITopologyErrorFix>();
	static {
		automaticErrorFixes.add(new SubstractOverlapPolygonWithFix());
		automaticErrorFixes.add(new MergeOverlapWithPolygonFix());
	}

	private static final Color DEFAULT_ERROR_COLOR = Color.BLUE;

	private static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = (MultiShapeSymbol) SymbologyFactory
			.createDefaultSymbolByShapeType(FShape.MULTI, DEFAULT_ERROR_COLOR);
	static {
		DEFAULT_ERROR_SYMBOL.setDescription(RULE_NAME);
	}

	private MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;
	
	
	
	/**
	 * Constructor.
	 * 
	 * @param topology
	 * @param originLyr
	 * @param destinationLyr
	 * @param clusterTolerance
	 */
	public PolygonMustNotOverlapWith(Topology topology, 
									 FLyrVect originLyr,
									 FLyrVect destinationLyr,
									 double clusterTolerance){
		super(topology, originLyr, clusterTolerance);
		setDestinationLyr(destinationLyr);
	}
	
	public PolygonMustNotOverlapWith(){
		
	}
	
	@Override
	public String getName() {
		return RULE_NAME;
	}
	
	@Override
	public List<ITopologyErrorFix> getAutomaticErrorFixes() {
		return automaticErrorFixes;
	}
	
	public XMLEntity getXMLEntity(){
		XMLEntity xml = super.getXMLEntity();
		xml.putProperty("destinationLayerName", this.destinationLyr.getName());
		return xml;
	}
	@Override    
	public void setXMLEntity(XMLEntity xml){
		super.setXMLEntity(xml);
		String destinationLayerName = "";
		if (xml.contains("destinationLayerName")) {
			destinationLayerName = xml.getStringProperty("destinationLayerName");
			this.destinationLyr = (FLyrVect) topology.getLayer(destinationLayerName);
		}//if
	}
	
	@Override
	protected void process(IGeometry geometry, IFeature feature) {
		Geometry jtsGeom = NewFConverter.toJtsGeometry(geometry);
		Polygon[] polygons = JtsUtil.extractPolygons(jtsGeom);
		for (int i = 0; i < polygons.length; i++) {
			Polygon polygon = polygons[i];
			Envelope env1 = polygon.getEnvelopeInternal();

			// We try to extend a bit the original envelope to ensure
			// recovering geometries in the limit

			double delta = getClusterTolerance() + 10;
			double minX = env1.getMinX() - delta;
			double minY = env1.getMinY() - delta;
			double maxX = env1.getMaxX() + delta;
			double maxY = env1.getMaxY() + delta;

			Rectangle2D rect = new Rectangle2D.Double(minX, minY, maxX - minX,
					maxY - minY);

			try {
				IFeatureIterator neighbours = destinationLyr.
													getSource().
													getFeatureIterator(rect, null, null, false);
				while (neighbours.hasNext()) {
					IFeature neighbourFeature = neighbours.next();
					IGeometry geom2 = neighbourFeature.getGeometry();
					Rectangle2D rect2 = geom2.getBounds2D();
					if (rect.intersects(rect2)) {
						Geometry jts2 = NewFConverter.toJtsGeometry(geom2);
						Polygon[] polygons2 = JtsUtil.extractPolygons(jts2);
						int numGeometries = polygons2.length;
						for (int j = 0; j < numGeometries; j++) {
							Polygon poly2 = polygons2[j];
							if (poly2.overlaps(polygon) || poly2.covers(polygon) || polygon.covers(poly2)) {
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
										}
								}//if
						}// for
					}//if
				}// while

			} catch (ReadDriverException e) {
				e.printStackTrace();
				return;
			}
		}//for polygons
	}


//	public List<ITopologyErrorFix> getAutomaticErrorFixes() {
//		return automaticErrorFixes;
//	}
	
	public boolean acceptsDestinationLyr(FLyrVect destLyr) {
		try {
			int shapeType = destLyr.getShapeType();
			return (FGeometryUtil.getDimensions(shapeType) == 2);
		} catch (ReadDriverException e) {
			e.printStackTrace();
			return false;
		}
	}


	public FLyrVect getDestinationLyr() {
		return destinationLyr;
	}


	public void setDestinationLyr(FLyrVect destinationLyr) {
		this.destinationLyr = destinationLyr;
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