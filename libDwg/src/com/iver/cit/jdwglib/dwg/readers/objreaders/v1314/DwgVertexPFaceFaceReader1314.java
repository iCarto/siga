/*
 * Created on 03-feb-2007
 *
 * gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
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
* $Id$
* $Log$
* Revision 1.1.2.2  2007-03-21 19:49:16  azabala
* implementation of dwg 12, 13, 14.
*
* Revision 1.2  2007/03/20 19:57:09  azabala
* source code cleaning
*
* Revision 1.1  2007/02/05 07:03:22  azabala
* *** empty log message ***
*
*
*/
package com.iver.cit.jdwglib.dwg.readers.objreaders.v1314;

import java.util.List;

import com.iver.cit.jdwglib.dwg.CorruptedDwgEntityException;
import com.iver.cit.jdwglib.dwg.DwgObject;
import com.iver.cit.jdwglib.dwg.DwgUtil;
import com.iver.cit.jdwglib.dwg.objects.DwgVertexPFaceFace;

public class DwgVertexPFaceFaceReader1314 extends AbstractDwg1314Reader {

	public void readSpecificObj(int[] data, int offset, DwgObject dwgObj)
			throws RuntimeException, CorruptedDwgEntityException {

		 if(! (dwgObj instanceof DwgVertexPFaceFace))
		    	throw new RuntimeException("ArcReader 14 solo puede leer DwgVertexPFaceFace");
		 DwgVertexPFaceFace v = (DwgVertexPFaceFace) dwgObj;

		 int bitPos = offset;

		 bitPos = headTailReader.readObjectHeader(data, offset, v);

		 List val = DwgUtil.getBitShort(data, bitPos);
		 bitPos = ((Integer) val.get(0)).intValue();
		 int v1 = ((Integer) val.get(1)).intValue();

		 val = DwgUtil.getBitShort(data, bitPos);
		 bitPos = ((Integer) val.get(0)).intValue();
		 int v2 = ((Integer) val.get(1)).intValue();

		 val = DwgUtil.getBitShort(data, bitPos);
		 bitPos = ((Integer) val.get(0)).intValue();
		 int v3 = ((Integer) val.get(1)).intValue();

		 val = DwgUtil.getBitShort(data, bitPos);
		 bitPos = ((Integer) val.get(0)).intValue();
		 int v4 = ((Integer) val.get(1)).intValue();

		 v.setVerticesidx(new int[]{v1, v2, v3, v4});

		 bitPos = headTailReader.readObjectTailer(data, bitPos, v);
	}

}

