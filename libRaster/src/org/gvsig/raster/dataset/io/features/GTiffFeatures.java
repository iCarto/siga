/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
package org.gvsig.raster.dataset.io.features;

import org.gvsig.raster.dataset.Params;
import org.gvsig.raster.dataset.io.GdalDriver;
import org.gvsig.raster.dataset.io.GdalWriter;

/**
 * Caracteristicas del formato GeoTiff para escritura.
 * 
 * @version 04/06/2007
 * @author Nacho Brodin (nachobrodin@gmail.com)
 */
public class GTiffFeatures extends WriteFileFormatFeatures {

	public GTiffFeatures() {
		super(GdalDriver.FORMAT_GTiff, "tif", new int[] { -1 }, new int[] { 0, 1, 2, 3, 4, 5 }, GdalWriter.class);
	}

	/**
	 * Carga los parámetros de este driver.
	 */
	public void loadParams() {
		super.loadParams();
		driverParams.setParam(
				"photometric", 
				new Integer(3), 
				Params.CHOICE, 
				new String[]{"YCBR", "MINISBLACK", "MINISWHITE", "RGB", "CMYK", "CIELAB", "ICCLAB", "ITULAB", "CBCR"});
		driverParams.setParam(
				"interleave", 
				new Integer(0), 
				Params.CHOICE, 
				new String[]{ "BAND", "PIXEL"});
		driverParams.setParam(
				"compression", 
				new Integer(3),
				Params.CHOICE, 
				new String[]{"LZW", "PACKBITS", "DEFLATE", "NONE"});
		driverParams.setParam("tfw", new Boolean("false"), Params.CHECK, null);
	}
}
