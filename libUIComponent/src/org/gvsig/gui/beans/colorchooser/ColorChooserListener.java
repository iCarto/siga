/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
*
* Copyright (C) 2005 IVER T.I. and Generalitat Valenciana.
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
*/
package org.gvsig.gui.beans.colorchooser;

import java.util.EventListener;
import java.util.EventObject;

public interface ColorChooserListener extends EventListener {
	/**
	 * Evento que se dispara cuando cambia el valor del componente. Nunca
	 * se dispara mientras se hace un dragged con el rat?n.
	 * @param e
	 */
	public void actionValueChanged(EventObject e);

	/**
	 * Evento que se dispara cuando cambia el valor del componente mientras se
	 * hace un dragged con el rat?n.
	 * @param e
	 */
	public void actionValueDragged(EventObject e);
}