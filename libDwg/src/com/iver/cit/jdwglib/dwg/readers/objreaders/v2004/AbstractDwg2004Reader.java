/*
 * Created on 25-ene-2007
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
* $Id: AbstractDwg15Reader.java,v 1.1.2.2 2007/03/21 19:49:16 azabala Exp $
* $Log: AbstractDwg15Reader.java,v $
* Revision 1.1.2.2  2007/03/21 19:49:16  azabala
* implementation of dwg 12, 13, 14.
*
* Revision 1.1  2007/01/25 20:05:58  azabala
* start of implementation of specific versions' object readers
*
*
*/
package com.iver.cit.jdwglib.dwg.readers.objreaders.v2004;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.iver.cit.jdwglib.dwg.CorruptedDwgEntityException;
import com.iver.cit.jdwglib.dwg.DwgHandleReference;
import com.iver.cit.jdwglib.dwg.DwgObject;
import com.iver.cit.jdwglib.dwg.DwgUtil;
import com.iver.cit.jdwglib.dwg.objects.DwgArc;
import com.iver.cit.jdwglib.dwg.objects.DwgAttrib;
import com.iver.cit.jdwglib.dwg.objects.DwgBlock;
import com.iver.cit.jdwglib.dwg.objects.DwgCircle;
import com.iver.cit.jdwglib.dwg.objects.DwgEllipse;
import com.iver.cit.jdwglib.dwg.objects.DwgEndblk;
import com.iver.cit.jdwglib.dwg.objects.DwgInsert;
import com.iver.cit.jdwglib.dwg.objects.DwgLine;
import com.iver.cit.jdwglib.dwg.objects.DwgLwPolyline;
import com.iver.cit.jdwglib.dwg.objects.DwgMText;
import com.iver.cit.jdwglib.dwg.objects.DwgPoint;
import com.iver.cit.jdwglib.dwg.objects.DwgPolyline3D;
import com.iver.cit.jdwglib.dwg.objects.DwgSeqend;
import com.iver.cit.jdwglib.dwg.objects.DwgSpline;
import com.iver.cit.jdwglib.dwg.objects.DwgText;
import com.iver.cit.jdwglib.dwg.objects.DwgVertex3D;
import com.iver.cit.jdwglib.dwg.objects.DwgAttdef;
import com.iver.cit.jdwglib.dwg.objects.DwgSolid;
import com.iver.cit.jdwglib.dwg.readers.DwgFileVR2004Reader;
import com.iver.cit.jdwglib.dwg.readers.IDwgFileReader;
import com.iver.cit.jdwglib.dwg.readers.IDwgObjectReader;

public abstract class AbstractDwg2004Reader implements IDwgObjectReader{

	private static Logger logger = Logger.getLogger(AbstractDwg2004Reader.class.getName());

	/**
	 * Reads the header of a dwg object2004
	 * */
	public int readObjectHeader(int[] data, int offset, DwgObject dwgObject) throws RuntimeException, CorruptedDwgEntityException{
		int bitPos = offset;


		Integer mode = (Integer) DwgUtil.getBits(data, 2, bitPos);
		bitPos = bitPos + 2;
		dwgObject.setMode(mode.intValue());


//		ArrayList v = DwgUtil.getBitShort(data, bitPos);
		ArrayList v = DwgUtil.getBitLong(data, bitPos); //Según especificaciones
		bitPos = ((Integer) v.get(0)).intValue();
		int rnum = ((Integer) v.get(1)).intValue();
		dwgObject.setNumReactors(rnum);

		//FIXME: ¿Por qué hace esto? Si entra en cualquiera de las condiciones
		// se perdería un bit.
		// ¿Es correcto o, tal vez están equivocadas las especificaciones en este punto?
		// Vale, en otro punto de las especificaciones dice "Only entities have this flag"
		if(dwgObject instanceof DwgLine) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgPoint) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgCircle) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgArc) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgLwPolyline) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgEllipse) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgMText) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgText) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgBlock) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgEndblk) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgPolyline3D) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgVertex3D) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgSeqend) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgInsert) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgAttrib) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgSpline) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgAttdef) dwgObject.setXDicObjFlag(true);
		else if(dwgObject instanceof DwgSolid) dwgObject.setXDicObjFlag(true);
		else {
			v = DwgUtil.testBit(data, bitPos);
			bitPos = ((Integer)v.get(0)).intValue();
			boolean XdicFlag = ((Boolean) v.get(1)).booleanValue();
			dwgObject.setXDicObjFlag(XdicFlag);
		}
		v = DwgUtil.testBit(data, bitPos);
		bitPos = ((Integer) v.get(0)).intValue();
		boolean nolinks = ((Boolean) v.get(1)).booleanValue();
		dwgObject.setNoLinks(nolinks);

//		Segun las especificaciones, en R2004+ el color se debe leer como un
//		CMC, sin embargo aquí no parece que sea así
//		v = DwgUtil.getCmColor(data, bitPos, dwgObject.getVersion());
		v = DwgUtil.getBitShort(data, bitPos);
		bitPos = ((Integer) v.get(0)).intValue();
		int color = ((Integer) v.get(1)).intValue();
		dwgObject.setColor(color);

		v = DwgUtil.getBitDouble(data, bitPos);
		bitPos = ((Integer) v.get(0)).intValue();
		float ltscale = ((Double) v.get(1)).floatValue();

		Integer ltflag = (Integer) DwgUtil.getBits(data, 2, bitPos);
		/*
		 * 00: bylayer;
		 * 01: byblock;
		 * 10: continuous;
		 * 11: linetype handlen present at end of object
		 */
		bitPos = bitPos + 2;

		Integer psflag = (Integer) DwgUtil.getBits(data, 2, bitPos);
		/*
		 * 00: bylayer;
		 * 01: byblock;
		 * 10: continuous;
		 * 11: linetype handlen present at end of object
		 */
		bitPos = bitPos + 2;

		v = DwgUtil.getBitShort(data, bitPos);
		bitPos = ((Integer) v.get(0)).intValue();
		int invis = ((Integer) v.get(1)).intValue();

		v = DwgUtil.getRawChar(data, bitPos);
		bitPos = ((Integer) v.get(0)).intValue();
		int weight = ((Integer) v.get(1)).intValue();

		return bitPos;
	}

	/**
	 * Reads the tailer of a dwg object2004
	 * */
	public int readObjectTailer(int[] data, int offset, DwgObject dwgObject) throws RuntimeException, CorruptedDwgEntityException{
		int bitPos = offset;
		/*
		 * Subentity ref handle. Esto se aplica sobre VERTEX, ATTRIB, SEQEND
		 */
		if (dwgObject.getMode() == 0x0) {
			DwgHandleReference subEntityHandle = new DwgHandleReference();
			bitPos = subEntityHandle.read(data, bitPos);
			dwgObject.setSubEntityHandle(subEntityHandle);
//		} else {
//			logger.warn("entMode != 0 : "+dwgObject.getMode()+ " " + dwgObject.getClass().getName());
		}

		/*
		 * Reactors handles TODO No se están usando para setear nada en
		 * DwgObject
		 */
		DwgHandleReference reactorHandle;
		for (int i = 0; i < dwgObject.getNumReactors(); i++) {
			reactorHandle = new DwgHandleReference();
			bitPos = reactorHandle.read(data, bitPos);
			dwgObject.addReactorHandle(reactorHandle);
		}

		/*
		 * XDICOBJHANDLE
		 */
		if(dwgObject.isXDicObjFlag()!= true){
			DwgHandleReference xDicObjHandle = new DwgHandleReference();
			bitPos = xDicObjHandle.read(data, bitPos);
			dwgObject.setXDicObjHandle(xDicObjHandle);
		}

		if (!dwgObject.isNoLinks()) {

			DwgHandleReference previousHandle = new DwgHandleReference();
			bitPos = previousHandle.read(data, bitPos);
			dwgObject.setPreviousHandle(previousHandle);

			DwgHandleReference nextHandle = new DwgHandleReference();
			bitPos = nextHandle.read(data, bitPos);
			dwgObject.setNextHandle(nextHandle);

		}


		/*
		 * Layer Handle code
		 */

		DwgHandleReference handle = new DwgHandleReference();
		bitPos = handle.read(data, bitPos);
		dwgObject.setLayerHandle(handle);



		if (dwgObject.getLinetypeFlags() == 0x3) {
			DwgHandleReference lineTypeHandle = new DwgHandleReference();
			bitPos = lineTypeHandle.read(data, bitPos);
			dwgObject.setLineTypeHandle(lineTypeHandle);

		}

		if (dwgObject.getPlotstyleFlags() == 0x3) {
			DwgHandleReference plotStyleHandle = new DwgHandleReference();
			bitPos = plotStyleHandle.read(data, bitPos);
			dwgObject.setPlotStyleHandle(plotStyleHandle);
		}
		return bitPos;
	}

	protected IDwgFileReader headTailReader;
	public void setFileReader(IDwgFileReader headTailReader) {
		if( ! (headTailReader instanceof DwgFileVR2004Reader))
			throw new RuntimeException("Tratando de leer entidad de DWG 2004 con"+
					headTailReader.getClass().getName());
		this.headTailReader = headTailReader;
	}
}

