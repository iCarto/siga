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
package org.gvsig.fmap.core;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.iver.cit.gvsig.fmap.core.v02.FConverter;

/**
 * This class receives a Shape implementation and extracts its coordinates.
 * 
 * 
 * @author Alvaro Zabala
 * 
 * @see java.awt.Shape
 *
 */
public class ShapePointExtractor {
	
	public static List<Point2D[]> extractPoints(PathIterator theIterator){
		List<Point2D[]> solution = new ArrayList<Point2D[]>();
		double[] coords = new double[6];
		List<Point2D> previousPartPoints = null;
		List<Point2D> partPoints = null;
		while (!theIterator.isDone()) {
			int theType = theIterator.currentSegment(coords);
			switch (theType) {
				case PathIterator.SEG_MOVETO://REVISAR, PASA SIEMPRE POR MOVE TO (SE ME PASA ALGUNA LLAMADA)
					if(partPoints != null){
						int numPoints = partPoints.size();
						Point2D[] points = new Point2D[numPoints];
						partPoints.toArray(points);
						solution.add(points);
						
						previousPartPoints = partPoints;
						
						partPoints = new ArrayList<Point2D>();
						Point2D pt = new Point2D.Double(coords[0], coords[1]);
						partPoints.add(pt);
						
					}else{
						partPoints = new ArrayList<Point2D>();
						Point2D pt = new Point2D.Double(coords[0], coords[1]);
						partPoints.add(pt);
					}
				break;

				case PathIterator.SEG_LINETO:
					Point2D pt = new Point2D.Double(coords[0], coords[1]);
					partPoints.add(pt);
					break;

				case PathIterator.SEG_CLOSE:
					partPoints.add(partPoints.get(0));
					break;
				default:
					//QUAD_TO y CUBIC_TO would appear? I suposse Flattening
					//computes curves
					break;
			} //end switch
			
			theIterator.next();
		} //end while loop
		
		if(previousPartPoints != partPoints){
			int numPoints = partPoints.size();
			Point2D[] points = new Point2D[numPoints];
			partPoints.toArray(points);
			solution.add(points);
		}
		
		return solution;
	}
	
	public static List<Point2D[]> extractPoints(Shape shape){
		return extractPoints(shape.getPathIterator(null, FConverter.FLATNESS));
	}
	
}
