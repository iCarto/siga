/*
 * Created on 07-sep-2007
 *
 * gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
 *   Av. Blasco Ibáñez, 50
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
/* CVS MESSAGES:
 *
 * $Id: 
 * $Log: 
 */
package org.gvsig.topology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.Marshaller;

import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.xml.XMLEncodingUtils;
import com.iver.utiles.xmlEntity.generate.XmlTag;

/**
 * Class with the responsability of persist topologies and their elements.
 */
public class TopologyPersister {

	public static final String FILE_PARAM_NAME = "file";
	public static final String DEFAULT_ENCODING = "UTF-8";

	private static Logger logger = Logger.getLogger(TopologyPersister.class
			.getName());

	/**
	 * Persist a topology to the specified file.
	 * 
	 * @param topology
	 *            topology to persist
	 * @param storageParams
	 *            map with params to storage
	 */
	public static void persist(Topology topology, Map<String, ?> storageParams) {
		try {
			String fileName = (String) storageParams.get(FILE_PARAM_NAME);
			File file = new File(fileName);
			FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
			OutputStreamWriter writer = new OutputStreamWriter(fos,
					DEFAULT_ENCODING);
			Marshaller m = new Marshaller(writer);
			m.setEncoding(DEFAULT_ENCODING);

			XMLEntity xml = topology.getXMLEntity();
			xml.putProperty("followHeaderEncoding", true, false);
			m.marshal(xml.getXmlTag());

		} catch (Exception e) {
			logger.error("error guardando la topologia " + topology.getName(),
					e);
		}
	}

	public static Topology load(MapContext mapContext, Map <String, ?> storageParams) {
		String fileName = (String) storageParams.get(FILE_PARAM_NAME);
		try {
			File file = new File(fileName);
			BufferedReader reader =null;
			String encoding = XMLEncodingUtils.getEncoding(file);
			FileInputStream stream = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(stream, encoding));
			XmlTag tag = (XmlTag) XmlTag.unmarshal(reader);
			XMLEntity xml=new XMLEntity(tag);
			return Topology.createFromXML(mapContext, xml);
			
		} catch(Exception e) {
			logger.error("Error cargando la topologia del recurso "+fileName, e);
		} 
		return null;
	}

	public static ITopologyErrorContainer createErrorContainerFromXML(Topology owner,
																		XMLEntity xml) {
		ITopologyErrorContainer solution = null;
		String className = null;
		if (xml.contains("className")) {
			className = xml.getStringProperty("className");
			Class clazz = null;
			try {
				clazz = Class.forName(className);
				solution = (ITopologyErrorContainer) clazz.newInstance();
				solution.setTopology(owner);
				solution.setXMLEntity(xml);
			} catch (Exception e) {
				logger.error(e);
			}
		} else {
			return null;
		}
		return solution;
	}

}
