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

import java.util.ArrayList;
import java.util.List;

import org.gvsig.exceptions.BaseException;
import org.gvsig.topology.Messages;
import org.gvsig.topology.TopologyError;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.edition.EditableAdapter;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

public class CreateNewFeatureForGapFix extends AbstractTopologyErrorFix {

	
	public List<IFeature>[] fixAlgorithm(TopologyError error) throws BaseException {
		IGeometry errorGeometry = error.getGeometry();
		FLyrVect originLyr = error.getOriginLayer();
		int numFields = originLyr.getRecordset().getFieldCount();
		Value[] newValues = new Value[numFields];
		for(int i = 0; i < numFields; i++){
			newValues[i] = ValueFactory.createNullValue();
		}
		String newId = error.getOriginLayer().getSource().getShapeCount()+"";
		DefaultFeature newFeature = new DefaultFeature(errorGeometry, newValues, newId );
		
		List<IFeature> firstLyrFeatures = new ArrayList<IFeature>();
		firstLyrFeatures.add(newFeature);
		return (List<IFeature>[]) new List[]{firstLyrFeatures};
	}

	public void fix(TopologyError error) throws BaseException {
		EditableAdapter[] adapters = prepareEdition(error);
		List<IFeature>[] correctedFeatures = fixAlgorithm(error);
		if (correctedFeatures != null) {
			List<IFeature> firstLyr = correctedFeatures[0];
			IFeature feature = firstLyr.get(0);
			adapters[0].doAddRow(feature, EditionEvent.GRAPHIC);
			adapters[0].endComplexRow(getEditionDescription());
			error.getTopology().removeError(error);
		}
	}

	public String getEditionDescription() {
		return Messages.getText("CREATE_NEW_FEATURE_FOR_GAPS");
	}

}
