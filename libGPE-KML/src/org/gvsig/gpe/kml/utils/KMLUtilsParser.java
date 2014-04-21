package org.gvsig.gpe.kml.utils;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.Hashtable;

import javax.xml.namespace.QName;

import org.gvsig.gpe.GPEDefaults;
import org.gvsig.gpe.utils.StringUtils;
import org.gvsig.gpe.xml.XmlProperties;
import org.gvsig.gpe.xml.stream.IXmlStreamReader;
import org.gvsig.gpe.xml.stream.XmlStreamException;


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
/* CVS MESSAGES:
 *
 * $Id: GMLUtilsParser.java 189 2007-11-21 12:45:56Z csanchez $
 * $Log$
 * Revision 1.8  2007/06/28 13:05:09  jorpiell
 * The Qname has been updated to the 1.5 JVM machine. The schema validation is made in the GPEWriterHandlerImplementor class
 *
 * Revision 1.7  2007/05/18 10:41:01  csanchez
 * Actualización libGPE-GML eliminación de clases inecesarias
 *
 * Revision 1.6  2007/05/16 13:00:48  csanchez
 * Actualización de libGPE-GML
 *
 * Revision 1.5  2007/05/16 09:29:12  jorpiell
 * The polygons has to be closed
 *
 * Revision 1.4  2007/05/15 10:14:45  jorpiell
 * The element and the feature is managed like a Stack
 *
 * Revision 1.3  2007/05/15 09:35:09  jorpiell
 * the tag names cant have blanc spaces
 *
 * Revision 1.2  2007/05/07 12:58:42  jorpiell
 * Add some methods to manage the multigeometries
 *
 * Revision 1.1  2007/02/28 11:48:31  csanchez
 * *** empty log message ***
 *
 * Revision 1.1  2007/02/20 10:53:20  jorpiell
 * Añadidos los proyectos de kml y gml antiguos
 *
 * Revision 1.3  2007/01/15 13:11:00  csanchez
 * Sistema de Warnings y Excepciones adaptado a BasicException
 *
 * Revision 1.2  2006/12/22 11:25:44  csanchez
 * Nuevo parser GML 2.x para gml's sin esquema
 *
 * Revision 1.1  2006/08/10 12:00:49  jorpiell
 * Primer commit del driver de Gml
 *
 *
 */
/**
 * @author Jorge Piera Llodr� (piera_jor@gva.es)
 * @author Carlos S�nchez Peri��n (sanchez_carper@gva.es)
 */
public class KMLUtilsParser {

	/**
	 * It returns a HashTable with the XML attributes. It has been 
	 * created because the parser doesn't has a getAttribiute(AttributeName)
	 * method.

	 * @param parser
	 * @return
	 * @throws XmlStreamException 
	 */
	public static Hashtable getAttributes(IXmlStreamReader parser) throws XmlStreamException{
		Hashtable hash = new Hashtable();
		int num_atributos = parser.getAttributeCount();
		for (int i=0 ; i<parser.getAttributeCount() ; i++){
			QName atributo = parser.getAttributeName(i);
			String valor=parser.getAttributeValue(i);
			if (valor!=null)
				hash.put(atributo, valor);
		}
		return hash;
	}	
	
	/**
	 * Remove the blanc symbol from a tag
	 * @param tag
	 * Tag name
	 * @return
	 * The tag without blancs
	 */
	public static String removeBlancSymbol(QName tag){
		if (tag == null){
			return null;
		}
		String blancSpace = GPEDefaults.getStringProperty(XmlProperties.DEFAULT_BLANC_SPACE);
		if (blancSpace == null){
			blancSpace = Kml2_1_Tags.DEFAULT_BLANC_SPACE;
		}
		// PROBLEM WITH COMPATIBILITY OF "replaceAll()" WITH IBM J9 JAVA MICROEDITION
		return StringUtils.replaceAllString(tag.getLocalPart(), blancSpace, " ");
		// return tag.replaceAll(blancSpace," ");
	}
	
	/**
	 * Replace the blancs of a tag with the
	 * deafult blanc symbol
	 * @param name
	 * @return
	 * A tag with blancs
	 */
	public static String addBlancSymbol(QName name){
		if (name == null){
			return null;
		}
		String blancSpace = GPEDefaults.getStringProperty(XmlProperties.DEFAULT_BLANC_SPACE);
		if (blancSpace == null){
			blancSpace = Kml2_1_Tags.DEFAULT_BLANC_SPACE;
		}
		// PROBLEM WITH COMPATIBILITY OF "replaceAll()" WITH IBM J9 JAVA MICROEDITION
		return StringUtils.replaceAllString(name.getLocalPart()," ",blancSpace);
		//return tag.replaceAll(" ",blancSpace);
	}
	
	public static Color fromABGRtoColor(String abgr) {
		long in = Long.decode("#"+ abgr);
		int alpha = (int) (in >> 24) & 0xFF;
		int blue = (int) (in >> 16) & 0xFF;
		int green = (int) (in >> 8) & 0xFF;
		int red = (int) (in >> 0) & 0xFF;
		return new Color(red, green, blue, alpha);
	}
	
	public static String padWith0(String s) {
		if ( s.length() < 2 )
	      { // pad on left with zeros
			s = "00".substring( 0, 2 - s.length() ) + s;
	      }
		return s;
	}

	public static String fromColorToABGR(Color color) {
		String abgr;
		String alpha = padWith0(Integer.toHexString(color.getAlpha()));
		String blue = padWith0(Integer.toHexString(color.getBlue()));
		String green = padWith0(Integer.toHexString(color.getGreen()));
		String red = padWith0(Integer.toHexString(color.getRed()));
		abgr = alpha + blue + green + red;
		return abgr;
	}

}

