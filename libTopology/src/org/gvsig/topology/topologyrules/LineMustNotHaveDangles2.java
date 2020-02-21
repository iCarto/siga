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

import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.jts.SnappingCoordinateMapWithCounter;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;
import org.gvsig.topology.TopologyError;
import org.gvsig.topology.errorfixes.ExtendDangleToNearestBoundaryPointFix;
import org.gvsig.topology.errorfixes.ExtendDangleToNearestVertexFix;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;


public class LineMustNotHaveDangles2 extends LineMustNotHavePseudonodes {
	final static String RULE_NAME = Messages.getText("must_not_have_dangles");
	
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

	public LineMustNotHaveDangles2(Topology topology, FLyrVect originLyr,
			double clusterTolerance) {
		super(topology, originLyr, clusterTolerance);
	}

	public LineMustNotHaveDangles2() {
		super();
	}

	public String getName() {
		return RULE_NAME;
	}

	/*
	 * The only difference of this class method (LineMustNotHaveDangles2)
	 * and superclass method (LineMustNotHavePseudonodes) is 
	 * cardinality check (node degree = 2 for pseudonodes, == 1 for dangles)
	 */
	protected void process(Geometry geometry, IFeature feature) {
		if (geometry instanceof GeometryCollection) {
			GeometryCollection geomCol = (GeometryCollection) geometry;
			for (int i = 0; i < geomCol.getNumGeometries(); i++) {
				Geometry geomI = geomCol.getGeometryN(i);
				process(geomI, feature);
			}
		} else if (geometry instanceof LineString) {
			LineString lineString = (LineString) geometry;
			Envelope lnEnv = lineString.getEnvelopeInternal();
			 
			//We try to extend a bit the original envelope to ensure
			//recovering geometries in the limit
			double minX = lnEnv.getMinX() - 10;
			double minY = lnEnv.getMinY() - 10;
			double maxX = lnEnv.getMaxX() + 10;
			double maxY = lnEnv.getMaxY() + 10;

			Rectangle2D rect = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
			
			SnappingCoordinateMapWithCounter coordinateMap = new SnappingCoordinateMapWithCounter(
					getClusterTolerance());

			// we consideer a pseudonode a coordinate with degree 2 (it only
			// connects two geometries)
			Coordinate firstPoint = lineString.getCoordinateN(0);
			coordinateMap.put(firstPoint, firstPoint);

			Coordinate lastPoint = lineString.getCoordinateN(lineString
					.getNumPoints() - 1);
			coordinateMap.put(lastPoint, lastPoint);

			try {
				IFeatureIterator neighbours = originLyr.getSource()
						.getFeatureIterator(rect, null, null, false);
				while (neighbours.hasNext()) {
					IFeature neighbourFeature = neighbours.next();
					if (neighbourFeature.getID().equalsIgnoreCase(
							feature.getID()))
						continue;
					Geometry geom2 = NewFConverter.toJtsGeometry(neighbourFeature.getGeometry());
					ArrayList<LineString> geometriesToProcess = new ArrayList<LineString>();
					if (geom2 instanceof LineString) {
						geometriesToProcess.add((LineString) geom2);
					} else if (geom2 instanceof MultiLineString) {
						MultiLineString multiLine = (MultiLineString) geom2;
						int numLines = multiLine.getNumGeometries();
						for (int i = 0; i < numLines; i++) {
							LineString line = (LineString) multiLine
									.getGeometryN(i);
							geometriesToProcess.add(line);
						}
					} else if (geom2 instanceof GeometryCollection) {
						MultiLineString multiLine = JtsUtil
								.convertToMultiLineString((GeometryCollection) geom2);
						int numLines = multiLine.getNumGeometries();
						for (int i = 0; i < numLines; i++) {
							LineString line = (LineString) multiLine
									.getGeometryN(i);
							geometriesToProcess.add(line);
						}
					} else {
						System.out.println("Encontrado:" + geom2.toString()
								+ " en regla de dangles");
					}

					int numGeometries = geometriesToProcess.size();
					for (int i = 0; i < numGeometries; i++) {
						LineString lineString2 = geometriesToProcess.get(i);
						Coordinate firstPoint2 = lineString2.getCoordinateN(0);
						Coordinate lastPoint2 = lineString2.getCoordinateN(lineString2
								.getNumPoints() - 1);
						
						coordinateMap.put(lastPoint2, lastPoint);
						coordinateMap.put(firstPoint2, firstPoint);

					}//for

				}//while
				
				int firstPointDegree = coordinateMap.getCount(firstPoint);
				int lastPointDegree = coordinateMap.getCount(lastPoint);
				
				if(firstPointDegree == 1){//A dangle is a node with degree 1, it only connects two lines
					
					//we dont add two times the same error
					Coordinate existingNode = (Coordinate) pseudoNodesCoordMap.get(firstPoint);
					if(existingNode == null){
						IGeometry errorGeom = 
							ShapeFactory.createPoint2D(firstPoint.x,
													   firstPoint.y);
					    TopologyError topologyError = 
							new TopologyError(errorGeom, 
												this, 
											 feature,
											topology);
					    topologyError.setID(errorContainer.getErrorFid());
					    addTopologyError(topologyError);
					    
					    pseudoNodesCoordMap.put(firstPoint, firstPoint);
					}
				}
				
				if(lastPointDegree == 1){
					Coordinate existingNode = (Coordinate) pseudoNodesCoordMap.get(lastPoint);
					if(existingNode == null){
						IGeometry errorGeom = 
							ShapeFactory.createPoint2D(lastPoint.x,
									lastPoint.y);
					    TopologyError topologyError = 
							new TopologyError(errorGeom, 
												this, 
											 feature,
											topology);
					    topologyError.setID(errorContainer.getErrorFid());
					    addTopologyError(topologyError);
					}
				}

			} catch (ReadDriverException e) {
				e.printStackTrace();
				return;
			}
		} else {
			System.out.println("Encontrado:" + geometry.toString()
					+ " en regla de dangles");
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
		return automaticErrorFixes.get(1);
	}
}
