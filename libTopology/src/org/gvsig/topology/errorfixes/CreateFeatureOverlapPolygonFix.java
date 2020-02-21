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
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.Messages;
import org.gvsig.topology.TopologyError;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.edition.EditableAdapter;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

public class CreateFeatureOverlapPolygonFix extends AbstractTopologyErrorFix {

	
	public List<IFeature>[] fixAlgorithm(TopologyError error) throws BaseException {
		IGeometry errorGeometry = error.getGeometry();
		Geometry errorGeoJts = NewFConverter.toJtsGeometry(errorGeometry);
		
		
		IFeature firstFeature = error.getFeature1();
		Geometry firstJts = NewFConverter.toJtsGeometry(firstFeature.getGeometry());
		Geometry[] first = null;
		if(firstJts instanceof GeometryCollection){
			first = JtsUtil.extractGeometries((GeometryCollection) firstJts);
		}else
			first = new Geometry[]{firstJts} ;
		
		
		Geometry[] second = null;
		if(errorGeoJts instanceof GeometryCollection){
			second = JtsUtil.extractGeometries((GeometryCollection) errorGeoJts);
		}else
		{
			second = new Geometry[]{errorGeoJts};
		}
		
		
		for (int i = 0; i < first.length; i++) {
			Geometry geom = first[i];
			Geometry partialSolution = null;
			for (int j = 0; j < second.length; j++) {
				Geometry aux = EnhancedPrecisionOp.difference(geom, second[j]);
				if(partialSolution == null)
					partialSolution = aux;
				else
					partialSolution = EnhancedPrecisionOp.union(partialSolution, aux);
			}//for
			first[i] = partialSolution;
		}//for i
		
		Geometry newFirstJts = JtsUtil.GEOMETRY_FACTORY.createGeometryCollection(first);
		IGeometry newFirst = NewFConverter.toFMap(newFirstJts);
		
		IFeature secondFeature = error.getFeature2();
		Geometry secondJts = NewFConverter.toJtsGeometry(secondFeature.getGeometry());
		Geometry[] third = null;
		if(secondJts instanceof GeometryCollection){
			third = JtsUtil.extractGeometries((GeometryCollection) secondJts);
		}else
		{
			third = new Geometry[]{secondJts};
		}
		
		for (int i = 0; i < third.length; i++) {
			Geometry geom = third[i];
			Geometry partialSolution = null;
			for (int j = 0; j < second.length; j++) {
				Geometry aux = EnhancedPrecisionOp.difference(geom, second[j]);
				if(partialSolution == null)
					partialSolution = aux;
				else
					partialSolution = EnhancedPrecisionOp.union(partialSolution, aux);
			}//for
			third[i] = partialSolution;
		}//for i
		Geometry newThirdJts = JtsUtil.GEOMETRY_FACTORY.createGeometryCollection(third);
		IGeometry newThird = NewFConverter.toFMap(newThirdJts);
		
		
		firstFeature.setGeometry(newFirst);
		secondFeature.setGeometry(newThird);
		
		int valueLenght = firstFeature.getAttributes().length;
		Value[] newValues = new Value[valueLenght];
		for(int i = 0; i < valueLenght; i++){
			newValues[i] = ValueFactory.createNullValue();
		}
		
		String newId = error.getOriginLayer().getSource().getShapeCount()+"";
		DefaultFeature newFeature = new DefaultFeature(errorGeometry, newValues, newId );
		
		List<IFeature> firstLyrFeatures = new ArrayList<IFeature>();
		firstLyrFeatures.add(firstFeature);
		firstLyrFeatures.add(secondFeature);
		firstLyrFeatures.add(newFeature);
		return (List<IFeature>[]) new List[]{firstLyrFeatures};
	}

	public void fix(TopologyError error) throws BaseException {
		EditableAdapter[] adapters = prepareEdition(error);
		List<IFeature>[] correctedFeatures = fixAlgorithm(error);
		if (correctedFeatures != null) {
			List<IFeature> firstLyr = correctedFeatures[0];
			IFeature firstFeature = firstLyr.get(0);
			adapters[0].modifyRow(Integer.parseInt(firstFeature.getID()), 
					firstFeature, 
					getEditionDescription(), 
					EditionEvent.GRAPHIC);
			
			IFeature secondFeature = firstLyr.get(1);
			adapters[0].modifyRow(Integer.parseInt(secondFeature.getID()), 
					secondFeature, 
					getEditionDescription(), 
					EditionEvent.GRAPHIC);
			

			
			adapters[0].doAddRow(firstLyr.get(2), EditionEvent.GRAPHIC);
		
			
			adapters[0].endComplexRow(getEditionDescription());
			error.getTopology().removeError(error);
		}
	}

	public String getEditionDescription() {
		return Messages.getText("CREATE_FEATURE_OVERLAP_AREA_FIX");
	}

}
