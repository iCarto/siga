package org.gvsig.gpe.gml.parser.v2.geometries;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.gvsig.gpe.gml.parser.GPEDefaultGmlParser;
import org.gvsig.gpe.gml.utils.GMLTags;
import org.gvsig.gpe.xml.stream.IXmlStreamReader;
import org.gvsig.gpe.xml.stream.XmlStreamException;
import org.gvsig.gpe.xml.utils.CompareUtils;

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
 * $Id:PolygonPropertyTypeBinding.java 371 2008-01-10 09:30:19Z jpiera $
 * $Log$
 * Revision 1.2  2007/05/24 07:29:47  csanchez
 * Añadidos Alias GML2
 *
 * Revision 1.1  2007/05/15 07:30:38  jorpiell
 * Add the geometryProperties tags
 *
 *
 */
/**
 * It parses a gml:polygonProperty object. Example:
 * <p>
 * <pre>
 * <code>
 * &lt;polygonProperty&gt;
 * &lt;Polygon gid="_877789"&gt;
 * &lt;outerBoundaryIs&gt;
 * &lt;LinearRing&gt;
 * &lt;coordinates&gt;0.0,0.0 100.0,0.0 50.0,100.0 0.0,0.0&lt;/coordinates&gt;
 * &lt;/LinearRing&gt;
 * &lt;/outerBoundaryIs&gt;
 * &lt;/Polygon&gt;
 * &lt;/polygonProperty&gt;
 * </code>
 * </pre>
 * </p> 
 * @author Jorge Piera LLodr� (jorge.piera@iver.es)
 */
public class PolygonPropertyTypeBinding {
	
	/**
	 * It parses the gml:PolygonProperty tag
	 * @param parser
	 * The XML parser
	 * @param handler
	 * The GPE parser that contains the content handler and
	 * the error handler
	 * @return
	 * A polygon
	 * @throws XmlStreamException
	 * @throws IOException
	 */
	public Object parse(IXmlStreamReader parser,GPEDefaultGmlParser handler) throws XmlStreamException, IOException {
		boolean endFeature = false;
		int currentTag;
		Object polygon = null;		
		
		QName tag = parser.getName();
		currentTag = parser.getEventType();

		while (!endFeature){
			switch(currentTag){
			case IXmlStreamReader.START_ELEMENT:
					if (CompareUtils.compareWithNamespace(tag,GMLTags.GML_POLYGON)){
						polygon = handler.getProfile().getPolygonTypeBinding().
						parse(parser, handler);
					}
					break;
				case IXmlStreamReader.END_ELEMENT:
					if ((CompareUtils.compareWithNamespace(tag,GMLTags.GML_POLYGONPROPERTY))||
					(CompareUtils.compareWithNamespace(tag,GMLTags.GML_EXTENTOF))||
					(CompareUtils.compareWithNamespace(tag,GMLTags.GML_COVERAGE)))
					{						
						endFeature = true;						
					}
					break;
				case IXmlStreamReader.CHARACTERS:			
					
					break;
				}
				if (!endFeature){					
					currentTag = parser.next();
					tag = parser.getName();
				}
			}			
		return polygon;	
	}
}

