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
package org.gvsig.referencing;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.geotools.referencefork.geometry.DirectPosition2D;
import org.geotools.referencefork.referencing.operation.builder.MappedPosition;
import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.core.ShapePointExtractor;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

public class LineLyrAdapter implements List<MappedPosition>{

	protected FLyrVect lineLyr;
	
	public LineLyrAdapter(){
	}
	
	public LineLyrAdapter(FLyrVect lyr) throws BaseException{
		setLayer(lyr);
	}
	
	
	public void setLayer(FLyrVect lyr) throws BaseException{
		if((lyr.getShapeType() != FShape.LINE) && (lyr.getShapeType() != FShape.MULTI))
			throw new IllegalArgumentException("Requiered line layer");
		this.lineLyr = lyr;
	}

	public boolean add(MappedPosition o) {
		return false;
	}

	public void add(int index, MappedPosition element) {
	}

	public boolean addAll(Collection<? extends MappedPosition> c) {
		return false;
	}

	public boolean addAll(int index, Collection<? extends MappedPosition> c) {
		return false;
	}

	public void clear() {
	}

	public boolean contains(Object o) {
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		return false;
	}

	public MappedPosition get(int index) {
		MappedPosition solution = null;
		try {
			IGeometry geometry = lineLyr.getSource().getShape(index);
			List<Point2D[]> points = ShapePointExtractor.extractPoints(geometry);
			Point2D[] firstPart = points.get(0);
			DirectPosition2D source = new DirectPosition2D(firstPart[0]);
			DirectPosition2D target = new DirectPosition2D(firstPart[1]);
			solution = new MappedPosition(source, target);
		} catch (ExpansionFileReadException e) {
			e.printStackTrace();
		} catch (ReadDriverException e) {
			e.printStackTrace();
		} catch(ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		return solution;
	}

	public int indexOf(Object o) {
		return 0;
	}

	public boolean isEmpty() {
		return false;
	}

	public Iterator<MappedPosition> iterator() {
		int shapeCount;
		try {
			shapeCount = this.lineLyr.getSource().getShapeCount();
		} catch (ReadDriverException e) {
			e.printStackTrace();
			shapeCount = 0;
		}
		final int numElements = shapeCount;
		return new Iterator<MappedPosition>(){
			int idx = 0;
			public boolean hasNext() {
				return idx < numElements;
			}

			public MappedPosition next() {
				MappedPosition solution = get(idx);
				idx++;
				return solution;
			}

			public void remove() {
			}};
	}

	public int lastIndexOf(Object o) {
		return 0;
	}

	public ListIterator<MappedPosition> listIterator() {
		return null;
	}

	public ListIterator<MappedPosition> listIterator(int index) {
		return null;
	}

	public boolean remove(Object o) {
		return false;
	}

	public MappedPosition remove(int index) {
		return null;
	}

	public boolean removeAll(Collection<?> c) {
		return false;
	}

	public boolean retainAll(Collection<?> c) {
	
		return false;
	}

	public MappedPosition set(int index, MappedPosition element) {
		return null;
	}

	public int size() {
		try {
			return lineLyr.getSource().getShapeCount();
		} catch (ReadDriverException e) {
			return 0;
		}
	}

	public List<MappedPosition> subList(int fromIndex, int toIndex) {
		return null;
	}

	public Object[] toArray() {
		int size = size();
		MappedPosition[] solution = new MappedPosition[size];
		Iterator<MappedPosition> iterator = iterator();
		int idx = 0;
		while(iterator.hasNext()){
			MappedPosition position = iterator.next();
			solution[idx] = position;
			idx++;
		}
		return solution;
	}

	public <T> T[] toArray(T[] a) {
		return null;
	}

}
