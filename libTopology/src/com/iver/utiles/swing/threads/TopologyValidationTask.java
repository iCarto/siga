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
package com.iver.utiles.swing.threads;

import org.gvsig.topology.Messages;
import org.gvsig.topology.Topology;

import com.iver.cit.gvsig.fmap.MapContext;

public class TopologyValidationTask extends CancellableProgressTask {

	static final String NOTE_PREFIX = Messages.getText("VALIDANDO_REGLA");
	Topology topology;
	MapContext mapContext;

	public TopologyValidationTask(Topology topology, MapContext mapContext) {
		this.topology = topology;
		this.mapContext = mapContext;
		super.statusMessage = Messages.getText("VALIDANDO_TOPOLOGIA");
		super.currentNote = NOTE_PREFIX
				+ Messages.getText(topology.getRule(0).getName());
	}

	public void run() throws Exception {
		topology.validate(this);
		if(topology.getNumberOfErrors() > 0 && ! isCanceled()){
//			//first of all, we check for a topology error layer for the same geometry
//			FLayers mapContextLyrs = mapContext.getLayers();
//			for(int i = 0; i < mapContextLyrs.getLayersCount(); i++){
//				FLayer layer = mapContextLyrs.getLayer(i);
//				if(layer.getName().equalsIgnoreCase(topology.getName()+"_error")){
//					mapContextLyrs.removeLayer(layer);
//				}
//			}
			topology.updateErrorLyr();
			
			
//			mapContext.beginAtomicEvent();
//			mapContextLyrs.replaceLayer(topology.getName(),topology);
//			mapContext.endAtomicEvent();
		}//if
		finished = true;
	}
	

	public void finished() {super.finished = true;}

}
