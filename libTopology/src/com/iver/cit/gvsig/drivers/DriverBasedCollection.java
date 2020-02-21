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
package com.iver.cit.gvsig.drivers;

import java.util.Collection;
import java.util.Iterator;

import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
/**
 * 
 * Implementation of collection which saves and reads
 * its objects from a subjacent driver.
 * 
 * @author Alvaro Zabala
 *
 */
public abstract class DriverBasedCollection 
	implements Collection {

	/**
	 * Adapter associated to this collection
	 */
	protected ReadableVectorial adapter;
	/**
	 * Bitset that marks the index of the added
	 */
	protected FBitSet bitset;
	
//	protected 
	
	
	/**
	 * Constructor
	 * @param adapter
	 */
	public DriverBasedCollection(ReadableVectorial adapter) {
		this.adapter = adapter;
		this.bitset = new FBitSet();
	}


	public boolean add(Object obj) {
		if(! (obj instanceof AbstractDriverCollectionEntry))
			return false;
		AbstractDriverCollectionEntry entry =
			(AbstractDriverCollectionEntry)obj;
		int idx = entry.getDriverIdx();
		
		
		
		
		
		// TODO Auto-generated method stub
		return false;
	}
	

	public boolean addAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	

	public void clear() {
		bitset.clear();
	}

	public boolean contains(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean containsAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEmpty() {
		return bitset.cardinality() == 0;
	}

	public Iterator iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean remove(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean retainAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] toArray(Object[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
