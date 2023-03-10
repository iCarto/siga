/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2007 IVER T.I. and Generalitat Valenciana.
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
package org.gvsig.georeferencing.ui.zoom.tools;


/**
 * Listener para las herramientas de la vista
 * 
 * 17/01/2008
 * @author Nacho Brodin (nachobrodin@gmail.com)
 */
public interface ToolListener {
	/**
	 * Informa de que la herramienta est? activa.
	 * @param ev
	 */
	public void onTool(ToolEvent ev);
	/**
	 * Informa de que la herramienta est? activa.
	 * @param ev
	 */
	public void offTool(ToolEvent ev);
	/**
	 * Evento de finalizaci?n de las acciones de la tool
	 * @param ev ToolEvent
	 */
	public void endAction(ToolEvent ev);
}
