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
package org.gvsig.jts.voronoi;

import java.awt.geom.Point2D;

import org.geotools.referencefork.referencing.operation.builder.TriangulationException;
import org.gvsig.jts.JtsUtil;

import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Subcase of polygon which forms a triangle.
 * 
 * @author Alvaro Zabala
 *
 */
public class FTriangle extends FPolygon2D {

	private static final long serialVersionUID = -8911275314078930227L;

	private Point2D a, b, c;
	
	public FTriangle(Point2D a, Point2D b, Point2D c){
		super(getGeneralPathX(new Point2D[]{a, b, c}));
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public static GeneralPathX getGeneralPathX(Point2D[] trianglePoints){
		GeneralPathX solution = new GeneralPathX();
		solution.moveTo(trianglePoints[0].getX(), trianglePoints[0].getY());
		
		for(int i = 1; i < trianglePoints.length; i++){
			Point2D currentPoint = trianglePoints[i];
			solution.lineTo(currentPoint.getX(), currentPoint.getY());
		}
		solution.closePath();
		return solution;
	}

	public Point2D getA() {
		return a;
	}

	public void setA(Point2D a) {
		this.a = a;
	}

	public Point2D getB() {
		return b;
	}

	public void setB(Point2D b) {
		this.b = b;
	}

	public Point2D getC() {
		return c;
	}

	public void setC(Point2D c) {
		this.c = c;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}
	
	public boolean hasVertex(Point2D point){
		if(a.equals(point) || b.equals(point) || c.equals(point))
			return true;
		else
			return false;
	}
	
	public boolean equals(Object o){
		if(o == null || (! (o instanceof FTriangle)))
			return false;
		FTriangle other = (FTriangle) o;
		return other.hasVertex(a) && other.hasVertex(b) && other.hasVertex(c);
	}
	
	public boolean isAdjacent(FTriangle other) throws TriangulationException{
		int identicalVertices = 0;

	    if (hasVertex(other.getA())) {
	        identicalVertices++;
	    }
	
	    if (hasVertex(other.getB())) {
	        identicalVertices++;
	    }
	
	    if (hasVertex(other.getC())) {
	        identicalVertices++;
	    }
	
	    if (identicalVertices == 3) {
	        throw new TriangulationException("Same triangle");
	    }
	
	    if (identicalVertices == 2) {
	        return true;
	    }
	
	    return false;
	}
	
	

	
	
	public Point2D getCircumCenter() {
        Coordinate jtsA = new Coordinate(a.getX(), a.getY());
        Coordinate jtsB = new Coordinate(b.getX(), b.getY());
        Coordinate jtsC = new Coordinate(c.getX(), c.getY());
        
        Coordinate circumcenter = JtsUtil.getCircumCenter(jtsA, jtsB, jtsC);
        
        return new Point2D.Double(circumcenter.x, circumcenter.y);
    }
	
	
}
