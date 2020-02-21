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
package org.gvsig.topology.errorfixes;

import java.util.Map;

import org.gvsig.exceptions.BaseException;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.Messages;
import org.gvsig.topology.TopologyError;

import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.edition.EditableAdapter;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;

/**
 * The Delete fix removes features that would collapse during the validate 
 * process based on the topology's cluster tolerance. 
 * 
 * @author Alvaro Zabala
 *
 */
public class DeleteTopologyErrorFix implements ITopologyErrorFix {

	public void fix(TopologyError topologyError) throws BaseException {
		FLyrVect originLyr = topologyError.getOriginLayer();
		if(! originLyr.isEditing()){
				originLyr.setEditing(true);
		}
		ReadableVectorial source = originLyr.getSource();
		if(!(source instanceof EditableAdapter))
			throw new BaseException(){
				protected Map values() {
					return null;
				}};
		
		EditableAdapter editAdapter = (EditableAdapter) source;
		IFeature causingFeature = topologyError.getFeature1();
		
		//TODO En un futuro el getID no tiene porque coincidir con el indice del driver.
		//hacer que topology error tenga los indices de los features implicados.
		editAdapter.removeRow(Integer.parseInt(causingFeature.getID()),getEditionDescription(), EditionEvent.ROW_EDITION);
		
		topologyError.getTopology().removeError(topologyError);
	}
	
	public String getEditionDescription() {
		return Messages.getText("DELETE_FIX");
	}

}
