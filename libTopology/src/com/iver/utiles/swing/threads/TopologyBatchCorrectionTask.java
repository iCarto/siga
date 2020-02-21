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

import java.util.List;

import org.gvsig.topology.ITopologyErrorContainer;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.ITopologyRule;
import org.gvsig.topology.Messages;
import org.gvsig.topology.TopologyError;



public class TopologyBatchCorrectionTask extends CancellableProgressTask {

	static final String NOTE_PREFIX = Messages.getText("BATCH_FIXING");
	ITopologyErrorContainer topology;
	ITopologyRule ruleToFix;

	public TopologyBatchCorrectionTask(ITopologyErrorContainer topology, ITopologyRule errorRule) {
		this.topology = topology;
		this.ruleToFix = errorRule;
		super.statusMessage = Messages.getText("FIXING_ERROR");
		super.currentNote = NOTE_PREFIX;
	}

	public void run() throws Exception {
		List<TopologyError> errors = topology.getTopologyErrorsByRule(ruleToFix.getName(),
				null, true);
		int numberOfErrors = errors.size();
		for(int i = 0; i < numberOfErrors; i++){
			if(isCanceled())
				break;
			TopologyError error = errors.get(i);
			ITopologyErrorFix defaultFix = ruleToFix.getDefaultFixFor(error);
			defaultFix.fix(error);
		}
		finished = true;
	}
	

	public void finished() {super.finished = true;}
}
