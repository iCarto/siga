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
import java.util.Map;

import org.gvsig.exceptions.BaseException;
import org.gvsig.topology.ITopologyErrorFix;
import org.gvsig.topology.TopologyError;

import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.edition.EditableAdapter;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;

/**
 * Abstract base class for all those fixes that modifies a topology error's causing feature.
 * 
 * This fix is not worthy for deletion or adition fixes.
 * 
 * @author Alvaro Zabala
 *
 */
public abstract class AbstractTopologyErrorFix implements ITopologyErrorFix {

	protected EditableAdapter prepareEditableAdapterFromLayer(FLyrVect lyr) throws BaseException{
		
		if(! lyr.isEditing()){
			lyr.setEditing(true);
		}
		
		ReadableVectorial source = lyr.getSource();
		if(!(source instanceof EditableAdapter))
			throw new BaseException(){
				protected Map values() {
					return null;
				}};
		
		EditableAdapter editAdapter = (EditableAdapter) source;
		editAdapter.startComplexRow();
		return editAdapter;
		
	}
	
	
	protected EditableAdapter[] prepareEdition(TopologyError topologyError) throws BaseException{
		EditableAdapter[] solution = null;
		
		List<EditableAdapter> solutionList = new ArrayList<EditableAdapter>();
		
		FLyrVect originLyr = topologyError.getOriginLayer();
		EditableAdapter editAdapter = prepareEditableAdapterFromLayer(originLyr);
		solutionList.add(editAdapter);
		
		FLyrVect destinationLyr = topologyError.getDestinationLayer();
		if(destinationLyr != null){
			EditableAdapter editAdapter2 = prepareEditableAdapterFromLayer(destinationLyr);
			solutionList.add(editAdapter2);
		}
		
		solution = new EditableAdapter[solutionList.size()];
		solutionList.toArray(solution);
		return solution;
	}
	
	
	public void fix(TopologyError topologyError) throws BaseException {
		EditableAdapter[] adapters = prepareEdition(topologyError);
		List<IFeature>[] correctedFeatures = fixAlgorithm(topologyError);
		if(correctedFeatures != null){
			finalizeEdition(adapters, correctedFeatures, getEditionDescription());
			topologyError.getTopology().removeError(topologyError);
		}

	}
	
	/**
	 * It returns a List<IFeature> for each layer related with the topology error.
	 * Index 0 for first layer, Index 1 for second layer, etc
	 * @param error
	 * @return
	 * @throws BaseException
	 */
	public abstract List<IFeature>[] fixAlgorithm(TopologyError error) throws BaseException ;
	
	public abstract String getEditionDescription();
	
	protected void finalizeEdition(EditableAdapter[] adapters, List<IFeature>[] newFeatures, String editionDescription) throws BaseException{
		for(int i = 0; i < adapters.length; i++){
			List<IFeature> features = newFeatures[i];
			EditableAdapter adapter = adapters[i];
			for(int j = 0; j < features.size(); j++ ){
				IFeature feature = features.get(j);
				//FIXME TopologyError must have the feature index of origin and destination features
				int featureIndex = Integer.parseInt(feature.getID());
				//TODO Probar si la llamada a modifyRow actualiza los features dibujados
				adapter.modifyRow(featureIndex, 
										feature, 
										editionDescription, 
										EditionEvent.GRAPHIC);
				adapter.endComplexRow(editionDescription);
			}//for j
		}//for
	}

}
