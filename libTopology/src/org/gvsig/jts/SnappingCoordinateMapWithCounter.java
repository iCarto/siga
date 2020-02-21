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
package org.gvsig.jts;

import java.util.Hashtable;

import com.vividsolutions.jts.geom.Coordinate;
/**
 * A coordinate map that consideers snap tolerance and that mantains
 * a counter with the number of ocurrences of each coordinate (and its
 * snapping region) in the map.
 * @author Alvaro Zabala
 *
 */
public class SnappingCoordinateMapWithCounter extends Hashtable {

	private static final long serialVersionUID = 1L;
	
	private double snapTolerance;

	public SnappingCoordinateMapWithCounter(double snapTolerance) {
		this.snapTolerance = snapTolerance;
	}

	
	public Object put(Object key, Object obj){
		if(! (key instanceof Coordinate) )
			return null;
		
		SnapCoordinateWithCounter keyCoord = new SnapCoordinateWithCounter((Coordinate)key);
		
		SnapCoordinateWithCounter objCoord = (SnapCoordinateWithCounter) super.get(keyCoord);
		if(objCoord != null){
			objCoord.increaseCount();
			return objCoord;
		}else{
			return super.put(keyCoord, keyCoord);
		}
	}
	
	public Object get(Object key){
		if(! (key instanceof Coordinate) )
			return null;
		return super.get(new SnapCoordinateWithCounter((Coordinate)key));
	}
	
	public int getCount(Object key){
		if(! (key instanceof Coordinate) )
			return -1;
		SnapCoordinateWithCounter coord = (SnapCoordinateWithCounter) super.
						get(new SnapCoordinateWithCounter((Coordinate)key));
		if(coord != null)
			return coord.getCount();
		else
			return -1;
	}
	

	
	public boolean containsKey(Object key){
		if(! (key instanceof Coordinate) )
			return false;
		return super.containsKey(new SnapCoordinateWithCounter((Coordinate)key));
	}
	
	public class SnapCoordinateWithCounter extends Coordinate {
		private static final long serialVersionUID = 1L;
		private int count = 0;
		
		public SnapCoordinateWithCounter(Coordinate arg0) {
			super(arg0);
			count++;
		}

		public boolean equals(Object obj) {
			if(! (obj instanceof SnapCoordinateWithCounter))
				return false;
			SnapCoordinateWithCounter other = (SnapCoordinateWithCounter) obj;
			return other.distance(this) <= SnappingCoordinateMapWithCounter.this.snapTolerance;
		}

		public int hashCode() {
			return 1; 
		}
		
		public void increaseCount(){
			count++;
		}
		
		public int getCount(){
			return count;
		}
	}

}
