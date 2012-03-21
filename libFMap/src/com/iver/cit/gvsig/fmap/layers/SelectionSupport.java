/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
 *   Av. Blasco Ib��ez, 50
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
package com.iver.cit.gvsig.fmap.layers;

import java.util.ArrayList;
import java.util.Iterator;

import com.iver.cit.gvsig.fmap.operations.selection.LinkSelectionListener;
import com.iver.utiles.XMLEntity;


/**
 * Clase que gestiona las operaci�nes sobre la selecci�n.
 *
 * @author Vicente Caballero Navarro
 */
public class SelectionSupport {
	private FBitSet selection = new FBitSet();
	private ArrayList<SelectionListener> listeners = new ArrayList<SelectionListener>();

	/**
	 * Inserta una nueva selecci�n.
	 *
	 * @param selection FBitSet con la selecci�n.
	 */
	public void setSelection(FBitSet selection) {
		this.selection = selection;
		fireSelectionEvents();
	}

	/**
	 * Devuelve un FBitSet con los �ndices de los elementos seleccionados.
	 *
	 * @return FBitSet.
	 */
	public FBitSet getSelection() {
		return selection;
	}

	/**
	 * Devuelve true si el elemento est� seleccionado.
	 *
	 * @param recordIndex �ndice del registro.
	 *
	 * @return True si est� seleccionado.
	 */
	public boolean isSelected(int recordIndex) {
		return selection.get(recordIndex);
	}

	/**
	 * Elimina la selecci�n.
	 */
	public void clearSelection() {
	    this.selection.clear();
	    fireSelectionEvents();
    }

	/**
	 * A�ade un SelectionListener al ArrayList de Listeners.
	 *
	 * @param listener SelectionListener a a�adir.
	 */
	public void addSelectionListener(SelectionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Borra el SelectionListener que se le pasa como par�metro del ArrayList
	 * de Listener.
	 *
	 * @param listener SlectionListener a borrar.
	 */
	public void removeSelectionListener(SelectionListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Cuando ocurre un evento de cambio en la selecci�n, �ste puede ser uno de
	 * una gran cantidad de eventos. Con el fin de no propagar todos estos
	 * eventos, se realiza la propagaci�n de manera manual al final de la
	 * "r�faga" de eventos
	 */
	public void fireSelectionEvents() {
		for (Iterator<SelectionListener> iter = listeners.iterator(); iter.hasNext();) {
			SelectionListener listener = iter.next();

			listener.selectionChanged(new SelectionEvent());
		}
	}

	/**
	 * Devuelve el XMLEntity con la informaci�n necesaria para reproducir un
	 * objeto igual al actual.
	 *
	 * @return XMLEntity.
	 */
	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className",this.getClass().getName());

		if (selection != null) {
			xml.putProperty("numBitSet", selection.cardinality());

			int n = 0;

			for (int i = 0; i < selection.length(); i++) {
				if (selection.get(i)) {
					xml.putProperty(String.valueOf(n), i);
					n++;
				}
			}
		}

		return xml;
	}

	/**
	 * A partir del XMLEntity reproduce la selecci�n.
	 *
	 * @param xml DOCUMENT ME!
	 */
	public void setXMLEntity(XMLEntity xml) {
		int numBitSet = xml.getIntProperty("numBitSet");

		if (numBitSet != 0) {
			for (int i = 0; i < numBitSet; i++) {
				selection.set(xml.getIntProperty(String.valueOf(i)));
			}
		}
	}

	/**
	 * A partir del XMLEntity reproduce la selecci�n.
	 *
	 * @param xml DOCUMENT ME!
	 */
	public void setXMLEntity03(XMLEntity xml) {
		int numBitSet = xml.getIntProperty("numBitSet");

		if (numBitSet != 0) {
			for (int i = 0; i < numBitSet; i++) {
				selection.set(xml.getIntProperty(String.valueOf(i)));
			}
		}
	}

	public void removeLinkSelectionListener() {
		for (int i=0;i<listeners.size();i++){
			if (listeners.get(i) instanceof LinkSelectionListener)
			listeners.remove(listeners.get(i));
		}
	}
}
