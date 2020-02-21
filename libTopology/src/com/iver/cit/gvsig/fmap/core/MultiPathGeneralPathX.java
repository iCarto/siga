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
package com.iver.cit.gvsig.fmap.core;

import com.graphbuilder.curve.MultiPath;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;

/**
 * GeneralPathX which could return a capi Multipath.
 * @author Alvaro Zabala
 *
 */
public class MultiPathGeneralPathX extends GeneralPathX {
	
	final static int DIMENSIONS = 2;
	
	public MultiPath getMultiPath(){
		return new MultiPath(DIMENSIONS){
			public double getFlatness() {
				return FConverter.FLATNESS;
			}
			
			public void lineTo(double[] p) {
				verifyCoords(p);
				MultiPathGeneralPathX.this.lineTo(p[0], p[1]);
			}

			/**
			Appends a point of type MOVE_TO.

			@throws IllegalArgumentException If the point is null or the dimension of the point does not meet the
			dimension requirement specified in the constructor.
			@see #lineTo(double[])
			*/
			public void moveTo(double[] p) {
				verifyCoords(p);
				MultiPathGeneralPathX.this.moveTo(p[0], p[1]);
			}
			
			private void verifyCoords(double[] p){
				if (p == null)
					throw new IllegalArgumentException("Point cannot be null.");

				if (p.length < getDimension())
					throw new IllegalArgumentException("p.length >= dimension required");
			}
			
			
			
		};
		
	}
}
