/*
 * Created on 17-sep-2007
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
* $Log: TopologyRuleDefinitionException.java,v $
* Revision 1.1  2007/09/19 09:05:06  azabala
* first version in cvs
*
*
*/
package org.gvsig.topology;


/**
 * Exception throwed when a topology rule is wrong defined.
 * (its preconditions are violated: type of geometry, etc.)
 * @author azabala
 *
 */
public class TopologyRuleDefinitionException extends Exception {

	private static final long serialVersionUID = -7663792269602823273L;

	public TopologyRuleDefinitionException(String text, Exception e) {
		super(e);
	}
	
	public TopologyRuleDefinitionException(){
		super();
	}
	
	
	
	public TopologyRuleDefinitionException(String text){
		super(text);
	}
	
	public TopologyRuleDefinitionException(Exception e){
		super(e);
	}
	
	
}

