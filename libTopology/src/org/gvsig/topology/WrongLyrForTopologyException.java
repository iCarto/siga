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
* $Log: WrongLyrForTopologyException.java,v $
* Revision 1.1  2007/09/19 16:37:15  azabala
* first version in cvs
*
*
*/
package org.gvsig.topology;

/**
 * Exception launched when we try to add a layer that is not
 * a FLyrVect to a Topology FLayers.
 * 
 * @author azabala
 *
 */
public class WrongLyrForTopologyException extends RuntimeException {
	
	private static final long serialVersionUID = -6058138466815385213L;

	public WrongLyrForTopologyException(String text, Exception e) {
		super(text,e);
	}
	
	public WrongLyrForTopologyException(String text){
		super(text);
	}
	
	public WrongLyrForTopologyException(){
		super();
	}
	
	public WrongLyrForTopologyException(Exception e){
		super(e);
	}
}

