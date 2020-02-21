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
import java.util.Iterator;

import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;

/**
 * 
 * This class is a try to give a more usable interface to
 * FMap geometries.
 * 
 * 
 * 
 * 
 * @author Alvaro Zabala
 *
 */
public class MultipartShapeIterator {
	
	Shape shape;
	
	public MultipartShapeIterator(Shape shape){
		this.shape = shape;
	}
	
	public Iterator<Shape> getShapeIterator(){
		return new Iterator<Shape>(){
			PathIterator theIterator = shape.getPathIterator(null, FConverter.FLATNESS);
			GeneralPathX currentShape = null;
			GeneralPathX nextShape = null;
			
			
			public boolean hasNext() {
				double[] coords = new double[6];
				while (!theIterator.isDone()) {
					int theType = theIterator.currentSegment(coords);
					
					switch (theType) {
						case PathIterator.SEG_MOVETO://REVISAR, PASA SIEMPRE POR MOVE TO (SE ME PASA ALGUNA LLAMADA)
							if(nextShape != null){
								currentShape = nextShape;
								nextShape = new GeneralPathX();
								nextShape.moveTo(coords[0], coords[1]);
								theIterator.next();
								return true;
							}
							nextShape = new GeneralPathX();
							nextShape.moveTo(coords[0], coords[1]);
						break;

						case PathIterator.SEG_LINETO:
							nextShape.lineTo(coords[0], coords[1]);
							break;

						case PathIterator.SEG_CLOSE:
							nextShape.closePath();
							break;
					} //end switch
					
					theIterator.next();
				} //end while loop
				if(currentShape != nextShape){
					currentShape = nextShape;
					return true;
				}else{
					return false;
				}
			}

			public Shape next() {
				return currentShape;
			}

			public void remove() {
			}
		};
	}
}
