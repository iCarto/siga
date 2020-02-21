/*
 * Created on 19-sep-2007
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
* $Log: SnapRepeatedPointTester.java,v $
* Revision 1.1  2007/09/19 16:37:49  azabala
* first version in cvs
*
*
*/
package org.gvsig.jts;

import java.util.Collection;

import com.iver.cit.gvsig.util.SnappingCoordinateMap;
import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiPoint;

/**
 * RepeatedPointTester implementation that consideers a snap tolerance
 * (points that are at a distance lower than the snap tolerance are consideered
 * the same point)
 * 
 * @author azabala
 *
 */
public class SnapRepeatedPointTester extends RepeatedPointTester {
	
	private double snapTolerance;
	
	private SnappingCoordinateMap repeatedCoords;
	
	/**
	 * Constructor 
	 * @param snapTolerance
	 */
	public SnapRepeatedPointTester(double snapTolerance) {
		repeatedCoords = new SnappingCoordinateMap(snapTolerance);
		this.snapTolerance = snapTolerance;
	}
	
	public Collection<Coordinate> getRepeatedCoordinates() {
		return repeatedCoords.values();
	}
	
	public void clear(){
		repeatedCoords.clear();
	}
	
	
	public boolean hasRepeatedPoint(Coordinate[] coords) {
		boolean solution = false;
		for (int i = 1; i < coords.length; i++) {
			Coordinate previousCoord = coords[i - 1];
			Coordinate coord = coords[i];
			if(SnapCGAlgorithms.snapEquals2D(previousCoord, coord, snapTolerance)){
				if(repeatedCoords.get(coords[i]) == null)
					repeatedCoords.put(coords[i], coords[i]);
				solution = true;
			}
		}// for
		return solution;
	}
	
	
	public Geometry removeRepeatedPoints(Geometry g){
		
		if(g instanceof GeometryCollection){
			if(! (g instanceof MultiPoint))
				return removeRepeatedPoints((GeometryCollection)g);
		}
		Coordinate[] coords = g.getCoordinates();
		SnapCoordinateList coordList = new SnapCoordinateList(coords, snapTolerance, false);
		Coordinate[] correctedCoords = coordList.toCoordinateArray();
		return JtsUtil.createGeometry(correctedCoords, g.getGeometryType());
	}
	
}

