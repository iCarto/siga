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

import java.awt.Shape;

import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.DefaultRow;
import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;

/**
 * IFeature which forms part of a Delaunay/Voronoi triangulation, so
 * its geometry is a FTriangle.
 * 
 * @see FTriangle
 * 
 * @author Alvaro Zabala
 *
 */
public class TriangleFeature extends DefaultRow implements IFeature{

	private FGeometry geometry;
	
	public TriangleFeature(FTriangle triangle, Value[] attributes, String fid){
		super(attributes, fid);
		this.geometry = ShapeFactory.createGeometry(triangle);
	}
	
	public IGeometry getGeometry() {
		return geometry;
	}

	public void setGeometry(IGeometry geom) {
		if(! (geom instanceof FGeometry))
			throw new IllegalArgumentException("VoroniFeature requires a FGeometry geometry. Found: "+geom.getClass().getName());
		FGeometry fgeometry = (FGeometry) geom;
		Shape internalShape = geometry.getInternalShape();
		if(! ( internalShape instanceof FTriangle))
				throw new IllegalArgumentException("VoroniFeature requires a FTriangle as internal shape of FGeometry. Found: "+ internalShape.getClass().getName());
		this.geometry = fgeometry;
				
		
	}

	public IRow cloneRow() {
		IGeometry geom= geometry.cloneGeometry();
		Value[] attri=null;
		if (getAttributes()!=null)
			attri=(Value[])getAttributes().clone();
		DefaultFeature df=new DefaultFeature(geom,attri,getID());
		return df;
	}
	
	

}
