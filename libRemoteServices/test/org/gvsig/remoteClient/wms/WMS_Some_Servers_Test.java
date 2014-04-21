/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
package org.gvsig.remoteClient.wms;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;

import org.gvsig.remoteClient.wms.ICancellable;
import org.gvsig.remoteClient.wms.WMSProtocolHandler;
import org.gvsig.remoteClient.wms.WMSProtocolHandlerFactory;
import org.gvsig.remoteClient.wms.wms_1_3_0.WMSProtocolHandler1_3_0;

import junit.framework.TestCase;

public class WMS_Some_Servers_Test extends TestCase {

	String[] servers = {
	"http://ags-sdi-public.jrc.ec.europa.eu/arcgis/services/img2k_321_mos/Mapserver/WMSServer",
	"http://www1.euskadi.net/wmsconnector/com.esri.wms.Esrimap?ServiceName=WMS_Euskadi_CBase&SERVICE=WMS"
	};
	
	public void setUp() {
	}



	public void testConnect() {
		for (int i=0; i < servers.length; i++) {
			String s = servers[i];
			long t1 = System.currentTimeMillis();
			try {
				WMSProtocolHandler handler = WMSProtocolHandlerFactory.negotiate(s);
				handler.setHost(s);
				handler.getCapabilities(null, true, null);
				long t2 = System.currentTimeMillis();
				System.out.println(handler.getClass().toString() + " : Test parsing done with apparently no errors in "+ (t2-(float)t1)/1000+" seconds");
	
			} catch (ConnectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}

