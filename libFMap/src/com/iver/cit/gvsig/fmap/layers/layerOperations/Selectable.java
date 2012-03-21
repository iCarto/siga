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
package com.iver.cit.gvsig.fmap.layers.layerOperations;

import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.SelectionListener;


/**
 * Interfaz que implementan las capas en las cuales se puede realizar una
 * selecci�n.
 */
public interface Selectable {
	/**
	 * Establece la selecci�n de la capa. No lanza nin�n evento, ya que se
	 * lanzan manualmente mediante el m�todo fireSelectionEvents
	 *
	 * @param selection
	 */
	void setSelection(FBitSet selection);

	/**
	 * Devuelve true si el �ndice que se pasa como par�metro corresponde a un
	 * registro seleccionado y false en caso contrario
	 *
	 * @param index �ndice.
	 *
	 * @return True si esta seleccionado.
	 */
	boolean isSelected(int index);

	/**
	 * Elimina la selecci�n de la capa. No lanza nin�n evento, ya que se lanzan
	 * manualmente mediante el m�todo fireSelectionEvents
	 */
	void clearSelection();

	/**
	 * Obtiene el bitset que contiene la informaci�n de los registros
	 * seleccionados de la capa
	 *
	 * @return BitSet con los �ndices de los elementos seleccionados.
	 */
	FBitSet getSelection();

	/**
	 * Cuando ocurre un evento de cambio en la selecci�n, �ste puede ser uno de
	 * una gran cantidad de eventos. Con el fin de no propagar todos estos
	 * eventos, se realiza la propagaci�n de manera manual al final de la
	 * "r�faga" de eventos
	 */
	void fireSelectionEvents();

	/**
	 * A�ade un listener de selecci�n a la capa
	 *
	 * @param listener listener que se quiere a�adir
	 */
	public void addSelectionListener(SelectionListener listener);

	/**
	 * Elimina un listener de selecci�n de la capa
	 *
	 * @param listener listener que se quiere eliminar
	 */
	public void removeSelectionListener(SelectionListener listener);
}
