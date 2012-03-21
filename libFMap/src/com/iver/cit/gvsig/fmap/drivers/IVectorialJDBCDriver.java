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
package com.iver.cit.gvsig.fmap.drivers;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;


public interface IVectorialJDBCDriver extends  IVectorialDatabaseDriver {
	
    public IFeatureIterator getFeatureIterator(String sql) throws ReadDriverException;
	public void open();
	
	/**
	 * Validate the parameters of Driver with the connection and the DBLayerDefinition.
	 * Calling this method is optional. It's not necessary to use it when you create
	 * a driver to create a layer, but if you use it, you have to use setData(...) afterwards
	 * if you are going to use that instance of the driver.
	 *
	 * @param conn Connection`s driver
	 * @param lyrDef DBLayerDefinition
	 *
	 * @throws DBException! If some kind of issue was detected
	 */
	public void validateData(IConnection conn, DBLayerDefinition lyrDef)
	throws DBException;
}

// [eiel-gestion-conexiones]