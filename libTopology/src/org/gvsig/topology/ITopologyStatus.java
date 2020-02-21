/*
 * Created on 07-sep-2007
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
 *
 */
package org.gvsig.topology;

/**
 * This interface has all posible status of a Topology in relation with its
 * status of validation.
 * 
 * <ol>
 * <li>EMPTY. The topology has just been created. It has neither layers nor rules</li>
 * 
 * <li>NOT_VALIDATED. The topology has layers and rules but it hasnt been validated.</li>
 * 
 * <li>VALIDATING. The topoly is validating.</li>
 * 
 * <li> VALIDATED. The topology has been validated and it hasnt any topology error </li>
 * 
 * <li> VALIDATED_WITH_ERRORS. After the validation of the topology there are topology errors</li>
 * 
 * <li>VALIDATED_WITH_DIRTY_ZONES. After the topology validation, any change has been made in the geometry
 * of the topology layers so dirty zones has been added to it.</li>
 * </ol>
 * 
 * @author Alvaro Zabala
 *
 */
public interface ITopologyStatus {
 
	public static final byte VALIDATED = 0;
	 
	public static final byte VALIDATED_WITH_ERRORS = 1;
	 
	public static final byte NOT_VALIDATED = 2;
	
	/**
	 * Status of a topology which hasnt any layer
	 */
	public static final byte EMPTY = 3;
	
	/**
	 * This status is different off VALIDATED_WITH_ERRORS.
	 * Examples:
	 * if we have a validated topology, and we add a new rule,
	 * its status will be VALIDATED_WITH_DIRTY_ZONES.
	 * 
	 * If we have a validated topology, and we edit a feature geometry
	 * of one of its layers, status will be VALIDATED_WITH_DIRTY_ZONES.
	 */
	public static final byte VALIDATED_WITH_DIRTY_ZONES = 4;
	
	public static final byte VALIDATING = 5;
	 
}
 
