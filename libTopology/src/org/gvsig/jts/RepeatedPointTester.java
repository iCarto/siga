/*
 * Created on 17-sep-2007
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
 * $Id$
 * $Log: RepeatedPointTester.java,v $
 * Revision 1.2  2007/09/19 16:38:06  azabala
 * generics collections
 *
 * Revision 1.1  2007/09/19 09:01:20  azabala
 * first version in cvs
 *
 *
 */
package org.gvsig.jts;

import java.util.Collection;
import java.util.HashSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This class is very close to
 * com.vividsolutions.jts.operation.valid.RepeatedPointTester with one
 * exception.
 * 
 * JTS implementation stops when the tester has found a repeated point.
 * 
 * This implementation analyzes all geometry's point, and returns all repeated
 * coordinates.
 * 
 * It also allows to remove repeated points.
 * 
 * 
 * 
 * @author azabala
 * 
 */
public class RepeatedPointTester {
	protected HashSet<Coordinate> repeatedCoords;

	public RepeatedPointTester() {
		repeatedCoords = new HashSet<Coordinate>();
	}

	public Collection<Coordinate> getRepeatedCoordinates() {
		return new HashSet<Coordinate>(repeatedCoords);
	}
	
	public void clear(){
		repeatedCoords.clear();
	}

	public boolean hasRepeatedPoint(Geometry g) {
		if (g.isEmpty())
			return false;
		if (g instanceof Point)
			return false;
		else if (g instanceof MultiPoint)
			return hasRepeatedPoint(((MultiPoint) g).getCoordinates());
		// LineString also handles LinearRings
		else if (g instanceof LineString)
			return hasRepeatedPoint(((LineString) g).getCoordinates());
		else if (g instanceof Polygon)
			return hasRepeatedPoint((Polygon) g);
		else if (g instanceof GeometryCollection)
			return hasRepeatedPoint((GeometryCollection) g);
		else
			throw new UnsupportedOperationException(g.getClass().getName());
	}

	public boolean hasRepeatedPoint(Coordinate[] coord) {
		boolean solution = false;
		for (int i = 1; i < coord.length; i++) {
			if (coord[i - 1].equals(coord[i])) {
				repeatedCoords.add(coord[i]);
				solution = true;
			}// if
		}// for
		return solution;
	}
	
	
	public Geometry removeRepeatedPoints(Geometry g){
		
		if(g instanceof GeometryCollection){
			if(! (g instanceof MultiPoint))
				return removeRepeatedPoints((GeometryCollection)g);
		}
		Coordinate[] coords = g.getCoordinates();
		CoordinateList coordList = new CoordinateList(coords, false);
		Coordinate[] correctedCoords = coordList.toCoordinateArray();
		return JtsUtil.createGeometry(correctedCoords, g.getGeometryType());
	}
	
	public Geometry removeRepeatedPoints(GeometryCollection gc){
		Geometry[] geoms = new Geometry[gc.getNumGeometries()];
		for (int i = 0; i < gc.getNumGeometries(); i++) {
			Geometry g = gc.getGeometryN(i);
			geoms[i] = removeRepeatedPoints(g);
		}//for
		return new GeometryFactory().createGeometryCollection(geoms);
	}
	
	
	private boolean hasRepeatedPoint(Polygon p) {
		boolean solution = false;
		if (hasRepeatedPoint(p.getExteriorRing().getCoordinates()))
			solution = true;
		for (int i = 0; i < p.getNumInteriorRing(); i++) {
			if (hasRepeatedPoint(p.getInteriorRingN(i).getCoordinates()))
				solution = true;
		}
		return solution;
	}

	private boolean hasRepeatedPoint(GeometryCollection gc) {
		for (int i = 0; i < gc.getNumGeometries(); i++) {
			Geometry g = gc.getGeometryN(i);
			if (hasRepeatedPoint(g))
				return true;
		}
		return false;
	}
}
