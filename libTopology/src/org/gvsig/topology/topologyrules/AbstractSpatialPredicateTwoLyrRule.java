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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.topology.AbstractTopologyRule;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.ITwoLyrRule;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;

import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.XMLEntity;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractSpatialPredicateTwoLyrRule extends AbstractTopologyRule implements ITwoLyrRule {

	protected FLyrVect destinationLyr;
	
	protected static List<ITopologyErrorFix> automaticErrorFixes = new ArrayList<ITopologyErrorFix>();
	
	protected static final Color DEFAULT_ERROR_COLOR = Color.LIGHT_GRAY;

	protected static final MultiShapeSymbol DEFAULT_ERROR_SYMBOL = (MultiShapeSymbol) SymbologyFactory
			.createDefaultSymbolByShapeType(FShape.MULTI, DEFAULT_ERROR_COLOR);
	
	protected MultiShapeSymbol errorSymbol = DEFAULT_ERROR_SYMBOL;	
	
	
	public AbstractSpatialPredicateTwoLyrRule(Topology topology, 
											 FLyrVect originLyr,
											 FLyrVect destinationLyr){
		super(topology, originLyr);
		this.destinationLyr = destinationLyr;
	}
	
	
	
	/**
	 * Constructor without topology param
	 * 
	 * @param originLyr
	 */
	public AbstractSpatialPredicateTwoLyrRule(FLyrVect originLyr, FLyrVect destinationLyr){
		this(null, originLyr, destinationLyr);
	}
	
	
	
	public AbstractSpatialPredicateTwoLyrRule(){
		super();
	}
	
	public FLyrVect getDestinationLyr() {
		return destinationLyr;
	}

	public void setDestinationLyr(FLyrVect destinationLyr) {
		this.destinationLyr = destinationLyr;
	}
	
	public XMLEntity getXMLEntity(){
		XMLEntity xml = super.getXMLEntity();
		xml.putProperty("destinationLayerName", this.destinationLyr.getName());
		return xml;
	}
	    
	public void setXMLEntity(XMLEntity xml){
		super.setXMLEntity(xml);
		String destinationLayerName = "";
		if (xml.contains("destinationLayerName")) {
			destinationLayerName = xml.getStringProperty("destinationLayerName");
			this.destinationLyr = (FLyrVect) topology.getLayer(destinationLayerName);
		}//if
	}
	
	protected TopologyError createTopologyError(Geometry errorGeometry, 
												IFeature feature, 
												IFeature neighbour){
		IGeometry errorGeom = NewFConverter.toFMap(errorGeometry);
		TopologyError topologyError = 
			new TopologyError(errorGeom, 
						errorContainer.getErrorFid(), 
						this,  
						feature, 
						neighbour, 
						topology );
		return topologyError;
	}

	@Override
	public void validateFeature(IFeature feature) {
		IGeometry geom = feature.getGeometry();
		int shapeType = geom.getGeometryType();
		if(acceptsOriginGeometryType(shapeType))
		{
			Rectangle2D bounds = geom.getBounds2D();
			Point2D extendedLL = new Point2D.Double(bounds.getMinX(), bounds.getMinY());
			Point2D extendedUL = new Point2D.Double(bounds.getMaxX(), bounds.getMaxY());
			
			bounds.add(extendedLL);
			bounds.add(extendedUL);
			
			try {
				IFeatureIterator neighbours = getDestinationLyr().
											  getSource().getFeatureIterator(bounds, null, 
													    						null, false);
				checkWithNeighbourhood(feature, bounds, neighbours);	
				
			} catch (BaseException e) {
				e.printStackTrace();
			}
		}//accepts
	}
	
	protected void checkWithNeighbourhood(IFeature feature, Rectangle2D extendedBounds, IFeatureIterator neighbourhood) throws BaseException{
		Geometry firstGeometry = NewFConverter.toJtsGeometry(feature.getGeometry());
		while (neighbourhood.hasNext()) {
			IFeature neighbourFeature = neighbourhood.next();
			IGeometry geom2 = neighbourFeature.getGeometry();
			if(acceptsDestinationGeometryType(geom2.getGeometryType())){
				Rectangle2D rect2 = geom2.getBounds2D();
				if (extendedBounds.intersects(rect2)) {
					Geometry jtsGeom2 = NewFConverter.toJtsGeometry(geom2);
					checkSpatialPredicate(feature, firstGeometry, neighbourFeature, jtsGeom2 );
				}
			}
			
		}//while
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

	
	protected abstract boolean checkSpatialPredicate(IFeature feature,Geometry firstGeometry, 
								IFeature neighbourFeature, Geometry jtsGeom2);

	/**
	 * Tells if this rule applies to the specified  geometry type
	 * for the first feature.
	 * @param shapeType
	 * @return
	 */
	protected abstract boolean acceptsOriginGeometryType(int shapeType);
	/**
	 * Tells if this rule applies to the specified  geometry type
	 * for the destination feature.
	 * @param shapeType
	 * @return
	 */
	protected abstract boolean acceptsDestinationGeometryType(int shapeType);
}
