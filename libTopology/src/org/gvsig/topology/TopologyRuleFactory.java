/*
 * Created on 10-abr-2006
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

import java.util.Map;

import org.apache.log4j.Logger;

import com.iver.cit.gvsig.fmap.Messages;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.NotExistInXMLEntity;
import com.iver.utiles.XMLEntity;

/**
 * Factory to create ITopologyRule implementations.
 * 
 * @author Alvaro Zabala
 * 
 */
public class TopologyRuleFactory {

	private static Logger logger = Logger.getLogger(TopologyRuleFactory.class
			.getName());

	public static ITopologyRule createFromXML(Topology ownerOfRule,
			XMLEntity xml) {
		String className = null;
		try {
			className = xml.getStringProperty("className");
		} catch (NotExistInXMLEntity e) {
			logger.error("Class name not set.\n"
					+ " Maybe you forgot to add the"
					+ " putProperty(\"className\", yourClassName)"
					+ " call in the getXMLEntity method of your class", e);
			return null;
		}
		Class clazz = null;
		IOneLyrRule obj = null;
		String s = className;
		try {
			clazz = Class.forName(className);
			if (xml.contains("desc")) {
				s += " \"" + xml.getStringProperty("desc") + "\"";
			}
			obj = (IOneLyrRule) clazz.newInstance();
			logger.info(Messages.getString("creating") + "....... " + s);
			try {
				obj.setTopology(ownerOfRule);
				obj.setXMLEntity(xml);
				
				if(obj.getOriginLyr() == null)
					throw new TopologyRuleDefinitionException("Regla topologica sin capa de origen");
				
				if(obj instanceof ITwoLyrRule){
					if(((ITwoLyrRule)obj).getDestinationLyr() == null){
						throw new TopologyRuleDefinitionException("Regla que aplica a dos capas no ha especificado la segunda capa");
					}
				}
				
			} catch (NotExistInXMLEntity neiXML) {
				logger.error(Messages.getString("failed_creating_object")
						+ ": " + s);
				throw neiXML;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return (ITopologyRule) obj;
	}

	public static ITopologyRule createRule(Class<?> ruleClass, 
										Map<String, Object> params, 
									Topology ruleOwner) throws TopologyRuleDefinitionException{
		
		try{	
			ITopologyRule rule = (ITopologyRule) ruleClass.newInstance();
			FLyrVect originLyr = (FLyrVect) params.get("originLyr");
			if(originLyr == null)
				throw new TopologyRuleDefinitionException("Regla topologica sin capa de origen");
			((IOneLyrRule)rule).setOriginLyr(originLyr);
			if (ITwoLyrRule.class.isAssignableFrom(ruleClass)){
				FLyrVect destinationLyr = (FLyrVect) params.get("destinationLyr");
				if(destinationLyr == null)
					throw new TopologyRuleDefinitionException("Regla que aplica a dos capas no ha especificado la segunda capa");
				((ITwoLyrRule)rule).setDestinationLyr(destinationLyr);
			}	
			return rule;
		} catch (InstantiationException e) {
			throw new TopologyRuleDefinitionException(
					"Error construyendo la regla topologica", e);
		} catch (IllegalAccessException e) {
			throw new TopologyRuleDefinitionException(
					"Error construyendo la regla topologica", e);
		}//catch
	}
}
