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
package org.gvsig.jts.voronoi.chew;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.gvsig.jts.JtsUtil;
import org.gvsig.jts.voronoi.VoronoiAndTinInputLyr;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * Implementation of List which returns JTS Points from a
 * VoronoiAndTinInputLyr layer.
 * 
 * @author Alvaro Zabala
 *
 * @param <Point>
 */
public class ChewVoronoiList implements List<Point> {
	
	
	VoronoiAndTinInputLyr inputLyr;
	
	public ChewVoronoiList(VoronoiAndTinInputLyr inputLyr) {
		this.inputLyr = inputLyr;
	}

	public boolean add(Point o) {
		return false;
	}

	public void add(int index, Point element) {
	}

	public boolean addAll(Collection c) {
		return false;
	}

	public boolean addAll(int index, Collection c) {
		return false;
	}

	public void clear() {
	}

	public boolean contains(Object o) {
		return false;
	}

	public boolean containsAll(Collection c) {
		return false;
	}

	public Point get(int index) {
		Point2D point = inputLyr.getPoint(index);
		return 	JtsUtil.GEOMETRY_FACTORY.createPoint(new Coordinate(point.getX(), point.getY()));
	}

	public int indexOf(Object o) {
		return 0;
	}

	public boolean isEmpty() {
		return false;
	}

	public Iterator iterator() {
		return new Iterator() {
			int index = 0;

			public boolean hasNext() {
				try {
					return index < inputLyr.getSource().getShapeCount();
				} catch (ReadDriverException e) {
					e.printStackTrace();
					return false;
				}
			}

			public Point next() {
				Point2D point = inputLyr.getPoint(index);
				index++;
				return 	JtsUtil.GEOMETRY_FACTORY.createPoint(new Coordinate(point.getX(), point.getY()));
			}

			public void remove() {
			}
		};
	}

	public int lastIndexOf(Object o) {
		return 0;
	}

	public ListIterator listIterator() {
		return null;
	}

	public ListIterator listIterator(int index) {
		return null;
	}

	public boolean remove(Object o) {
		return false;
	}

	public Point remove(int index) {
		return null;
	}

	public boolean removeAll(Collection c) {
		return false;
	}

	public boolean retainAll(Collection c) {
		return false;
	}

	public Point set(int index, Point element) {
		return null;
	}

	public int size() {
		try {
			return inputLyr.getSource().getShapeCount();
		} catch (ReadDriverException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public List subList(int fromIndex, int toIndex) {
		return null;
	}

	public Point[] toArray() {
		return null;
	}

	public Point[] toArray(Object[] a) {
		return null;
	}
}