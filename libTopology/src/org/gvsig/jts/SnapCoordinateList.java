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
 * $Log: SnapCoordinateList.java,v $
 * Revision 1.1  2007/09/19 16:37:49  azabala
 * first version in cvs
 *
 *
 */
package org.gvsig.jts;

import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;

/**
 * JTS coordinate list that uses snap to avoid consecutives coordinates at a
 * distance lower than the cluster tolerance.
 * 
 * 
 * FIXME There could be coordinates non consecutives at a distance slower than
 * snap tolerance. 
 * 
 * @author azabala
 * 
 */
public class SnapCoordinateList extends CoordinateList {

	private static final long serialVersionUID = 8784078431309170433L;

	private double snapTolerance;
	
	
	public SnapCoordinateList(double snapTolerance) {
		super();
		this.snapTolerance = snapTolerance;
	}

	public SnapCoordinateList(Coordinate[] coord, double snapTolerance) {
		super();
		this.snapTolerance = snapTolerance;
		add(coord, false);//by default doesnt allow repeated coords
	}

	
	public SnapCoordinateList(Coordinate[] coord, 
							double snapTolerance, 
							boolean allowRepeated) {
		super();
		this.snapTolerance = snapTolerance;
		add(coord, allowRepeated);
	}

	/**
	 * Add a coordinate
	 * 
	 * @param coord
	 *            The coordinates
	 * @param allowRepeated
	 *            if set to false, repeated coordinates are collapsed
	 */
	public void add(Coordinate coord, boolean allowRepeated) {
		// don't add duplicate coordinates
		if (!allowRepeated) {
// with this code we only filter consecutive snapped points			
//			if (size() >= 1) {
//				Coordinate last = (Coordinate) get(size() - 1);
//				if(SnapCGAlgorithms.snapEquals2D(last, coord, snapTolerance))
//					return;
//			}//if
			int size = size();
			/*
			 If size == 1, we check the existing point,
			 if size > 1, we dont check the first point (because closed geometries
			 musnt snap the first and last point
			 * */
			if(size > 1){
				for(int i = 1; i < size; i++){
					Coordinate coordinate = (Coordinate) get(i);
					if(SnapCGAlgorithms.snapEquals2D(coordinate, coord, snapTolerance))
						return;
				}
			}else if(size == 1){
				Coordinate last = (Coordinate) get(0);
				if(SnapCGAlgorithms.snapEquals2D(last, coord, snapTolerance))
					return;
			}
		}//if
		super.add(coord);
	}
}
