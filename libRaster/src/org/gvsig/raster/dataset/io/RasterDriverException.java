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
package org.gvsig.raster.dataset.io;

/**
 * Excepci?n lanzada por un driver de raster. Cada driver concreto tiene su propio
 * tipo de excepciones GdalException, MrsSIDException, JNCSFileException ... 
 * Estas son lanzadas hacia el nivel superior como una sola si no pueden ser gestionadas 
 * en el nivel dataset.io.
 * 
 * @author Nacho Brodin (nachobrodin@gmail.com)
 *
 */
public class RasterDriverException extends Exception{
	private static final long serialVersionUID = 7412405741078802490L;

	/**
	 * Constructor. Asigna el texto de la excepci?n
	 * @param msg
	 */
	public RasterDriverException(String msg){
		super(msg);
	}
	
	/**
	 * Constructor. Asigna el texto de la excepci?n y la excepci?n generica.
	 * @param msg
	 */
	public RasterDriverException(String msg, Exception e){
		super(msg, e);
	}
}