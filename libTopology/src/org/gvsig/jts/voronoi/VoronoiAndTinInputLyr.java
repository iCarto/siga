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

import org.gvsig.exceptions.BaseException;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;
/**
 * Vectorial layer whose geometries can be triangulated (TIN) or can 
 * participate in a voronoi diagram.
 * 
 * @author Alvaro Zabala
 *
 */
public abstract class VoronoiAndTinInputLyr extends FLyrVect {
	
	public interface VertexVisitor{
		public void visit(Point2D vertex);
	}
	public abstract Point2D getPoint(int geometryIndex);
	
	
	public void visitVertices(VertexVisitor visitor, boolean onlySelection) throws BaseException{
		int shapeCount = getSource().getShapeCount();
		for(int i = 0; i < shapeCount; i++){
			if(onlySelection){
				if(! getRecordset().getSelection().get(i))
					continue;
			}
			Point2D point = getPoint(i);
			visitor.visit(point);
		}//for	
	}
}
