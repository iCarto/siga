package org.gvsig.gui.beans.incrementabletask;

/* gvSIG. Geographic Information System of the Valencian Government
 *
 * Copyright (C) 2007-2008 Infrastructures and Transports Department
 * of the Valencian Government (CIT)
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 */

import java.util.EventListener;

/**
 * Interfaz para la ventana IncrementableTask, aqui se define los
 * comportamientos de los botones de la ventana.
 *
 * @version 23/04/2007
 * @author BorSanZa - Borja S�nchez Zamorano (borja.sanchez@iver.es)
 */
public interface IncrementableListener extends EventListener {
	/**
	 * Invocado cuando se aprieta el boton Resumir de la ventana
	 * @param e
	 */
	public void actionResumed(IncrementableEvent e);

	/**
	 * Invocado cuando se aprieta el boton Suspender de la ventana
	 * @param e
	 */
	public void actionSuspended(IncrementableEvent e);

	/**
	 * Invocado cuando se aprieta el boton Cancelar de la ventana
	 * @param e
	 */
	public void actionCanceled(IncrementableEvent e);
}
