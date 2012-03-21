/*
 * Created on 30-ago-2005
 *
 * gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
USA.
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
package com.hardcode.gdbms.driver.dxf;

import java.io.File;

import com.hardcode.gdbms.driver.exceptions.OpenDriverException;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.data.driver.SpatialFileDriver;
import com.hardcode.gdbms.engine.spatial.fmap.AbstractFMapFileDriver;
import com.iver.cit.gvsig.fmap.drivers.dxf.DXFMemoryDriver;
import com.iver.cit.gvsig.fmap.layers.VectorialFileAdapter;


public class FMapDXFDriver extends AbstractFMapFileDriver implements SpatialFileDriver {
	
	/**
	 * @see com.hardcode.gdbms.engine.data.driver.FileDriver#open(java.io.File)
	 */
	public void open(File file) throws OpenDriverException {
		VectorialFileAdapter adapter = new VectorialFileAdapter(file);
		adapter.setDriver(new DXFMemoryDriver());
		super.open(adapter);
	}

	/**
	 * @see com.hardcode.gdbms.engine.data.driver.FileDriver#fileAccepted(java.io.File)
	 */
	public boolean fileAccepted(File f) {
		return f.getName().toUpperCase().endsWith(".DXF");
	}

	/**
	 * @see com.hardcode.driverManager.Driver#getName()
	 */
	public String getName() {
		return "FMap DXF Driver";
	}

    public void setDataSourceFactory(DataSourceFactory dsf) {
    }
}
