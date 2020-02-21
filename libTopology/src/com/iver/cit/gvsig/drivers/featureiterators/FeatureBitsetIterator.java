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
package com.iver.cit.gvsig.drivers.featureiterators;

import org.cresques.cts.IProjection;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.featureiterators.DefaultFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;

/**
 * 
 * Iterates over the selected features of a layer.
 * 
 * @author Alvaro Zabala
 *
 */
public class FeatureBitsetIterator extends DefaultFeatureIterator {

	
	FBitSet selection;

	public FeatureBitsetIterator(ReadableVectorial source,
										IProjection sourceProj, 
										IProjection targetProj, 
										String[] fieldNames)
			throws ReadDriverException {
		super(source, sourceProj, targetProj, fieldNames);
		selection = this.source.getRecordset().getSelection();
		this.currentFeature = selection.nextSetBit(0);
	}
	
	
	public FeatureBitsetIterator(FBitSet selection, 
								ReadableVectorial source) throws ReadDriverException{
		super(source, null, null, null);
		this.selection = selection;
		this.currentFeature = selection.nextSetBit(0);
	}
	
	public void setSelection(FBitSet bitSet){
		this.selection = bitSet;
		this.currentFeature = selection.nextSetBit(0);
	}

	public FeatureBitsetIterator(ReadableVectorial source)
			throws ReadDriverException {
		super(source);
	}
	
	public boolean hasNext() throws ReadDriverException {
		return currentFeature != -1;
	}

	public IFeature next() throws ReadDriverException {
		try {
			IGeometry geom = chekIfCloned(source.getShape(currentFeature));
			reprojectIfNecessary(geom);
			Value[] regAtt = getValues(currentFeature);
			IFeature feat  = new DefaultFeature(geom, regAtt, currentFeature + "");
			currentFeature = selection.nextSetBit(currentFeature + 1);
			return feat;
		} catch (ExpansionFileReadException e) {
			throw new ReadDriverException("",e);
		} 
	}
	
}
