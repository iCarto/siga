package org.gvsig.gpe.gml.parser;

import org.gvsig.gpe.parser.GPEReaderBaseTest;

/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
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
 *   Av. Blasco Ib??ez, 50
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
 * $Id: GMLReaderBaseTest.java 202 2007-11-27 12:00:11Z jpiera $
 * $Log$
 * Revision 1.5  2007/05/18 10:41:01  csanchez
 * Actualización libGPE-GML eliminación de clases inecesarias
 *
 * Revision 1.4  2007/04/20 08:33:37  jorpiell
 * Test updated
 *
 * Revision 1.3  2007/04/20 08:13:43  csanchez
 * Actualizacion protoripo libGPE
 *
 * Revision 1.2  2007/04/13 07:17:57  jorpiell
 * Add the writting tests for the simple geometries
 *
 * Revision 1.1  2007/04/12 10:24:12  jorpiell
 * Add the GML and schema tests
 *
 *
 */
/**
 * @author Carlos S?nchez Peri??n (sanchez_carper@gva.es)
 */
public abstract class GMLReaderBaseTest extends GPEReaderBaseTest {

	//Aqui se le pasa la clase 
	public Class getGPEParserClass() {
		return org.gvsig.gpe.gml.parser.GPEGmlSFP0Parser.class;
	}

	/**
	 * Return if the GML has a schema
	 * @return
	 */
	public abstract boolean hasSchema();

}
