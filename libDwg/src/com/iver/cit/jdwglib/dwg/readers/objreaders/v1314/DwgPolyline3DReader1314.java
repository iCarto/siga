/*
 * Created on 25-ene-2007 by azabala
 *
 */
package com.iver.cit.jdwglib.dwg.readers.objreaders.v1314;

import java.util.List;

import com.iver.cit.jdwglib.dwg.CorruptedDwgEntityException;
import com.iver.cit.jdwglib.dwg.DwgHandleReference;
import com.iver.cit.jdwglib.dwg.DwgObject;
import com.iver.cit.jdwglib.dwg.DwgUtil;
import com.iver.cit.jdwglib.dwg.objects.DwgPolyline3D;

/**
 * @author alzabord
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DwgPolyline3DReader1314 extends AbstractDwg1314Reader{

	/* (non-Javadoc)
	 * @see com.iver.cit.jdwglib.dwg.readers.IDwgObjectReader#readSpecificObj(int[], int, com.iver.cit.jdwglib.dwg.DwgObject)
	 */
	public void readSpecificObj(int[] data, int offset, DwgObject dwgObj) throws RuntimeException, CorruptedDwgEntityException {
		if(! (dwgObj instanceof DwgPolyline3D))
	    	throw new RuntimeException("ArcReader 14 solo puede leer DwgPolyline3d");
		DwgPolyline3D l = (DwgPolyline3D)dwgObj;
		
		int bitPos = offset;
		bitPos = headTailReader.readObjectHeader(data, bitPos, l);
		
		List val = DwgUtil.getRawChar(data, bitPos);
		bitPos = ((Integer) val.get(0)).intValue();
		int flags = ((Integer) val.get(1)).intValue();
		l.setSplineFlags(flags);
		
		val = DwgUtil.getRawChar(data, bitPos);
		bitPos = ((Integer) val.get(0)).intValue();
		int closedFlags = ((Integer) val.get(1)).intValue();
		l.setClosedFlags(closedFlags);
		
		bitPos = headTailReader.readObjectTailer(data, bitPos, l);
		
		DwgHandleReference firstVerHdl = new DwgHandleReference();
		bitPos = firstVerHdl.read(data, bitPos);
		l.setFirstVertexHandle(firstVerHdl);
		
		DwgHandleReference lastVerHdl = new DwgHandleReference();
		bitPos = lastVerHdl.read(data, bitPos);
		l.setLastVertexHandle(lastVerHdl);
		
		DwgHandleReference seqendHdl = new DwgHandleReference();
		bitPos = seqendHdl.read(data, bitPos);
		l.setSeqendHandle(seqendHdl);
	}
}
