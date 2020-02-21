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

import org.gvsig.jts.voronoi.VoronoiAndTinInputLyr.VertexVisitor;

/**
 * Visits all vertices of a VoronoiAndTinInputLyr and computes its extent
 * @author Alvaro Zabala
 *
 */
public class MessExtentVertexVisitor implements VertexVisitor{
	int count = 0;
	double argmaxx = 0;
	double argmaxy = 0;
	double argminx = 0;
	double argminy = 0;
	
	public void visit(Point2D vertex) {
		if (count == 0) {
			argmaxx = vertex.getX();
			argminx = vertex.getX();
			argmaxy = vertex.getY();
			argminy = vertex.getY();
		} else {
			if (vertex.getX() < argminx) {
				argminx = vertex.getX();
			}
			if (vertex.getX() > argmaxx) {
				argmaxx = vertex.getX();
			}
			if (vertex.getY() < argminy) {
				argminy = vertex.getY();
			}
			if (vertex.getY() > argmaxy) {
				argmaxy = vertex.getY();
			}
		}
		count++;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double getArgmaxx() {
		return argmaxx;
	}

	public void setArgmaxx(double argmaxx) {
		this.argmaxx = argmaxx;
	}

	public double getArgmaxy() {
		return argmaxy;
	}

	public void setArgmaxy(double argmaxy) {
		this.argmaxy = argmaxy;
	}

	public double getArgminx() {
		return argminx;
	}

	public void setArgminx(double argminx) {
		this.argminx = argminx;
	}

	public double getArgminy() {
		return argminy;
	}

	public void setArgminy(double argminy) {
		this.argminy = argminy;
	}
}//class
