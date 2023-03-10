/*
 * Created on 25-ene-2007 by azabala
 *
 */
package com.iver.cit.jdwglib.dwg.readers.objreaders.v1314;

import com.iver.cit.jdwglib.dwg.CorruptedDwgEntityException;
import com.iver.cit.jdwglib.dwg.DwgObject;
import com.iver.cit.jdwglib.dwg.objects.DwgEndblk;

/**
 * @author alzabord
 *
 */
public class DwgEndBlkReader1314 extends AbstractDwg1314Reader{

	/* (non-Javadoc)
	 * @see com.iver.cit.jdwglib.dwg.readers.IDwgObjectReader#readSpecificObj(int[], int, com.iver.cit.jdwglib.dwg.DwgObject)
	 */
	public void readSpecificObj(int[] data, int offset, DwgObject dwgObj) throws RuntimeException, CorruptedDwgEntityException {
		if(! (dwgObj instanceof DwgEndblk))
	    	throw new RuntimeException("ArcReader 14 solo puede leer DwgEndBlk");

		int bitPos = offset;
		bitPos = headTailReader.readObjectHeader(data, offset, dwgObj );
		bitPos = headTailReader.readObjectTailer(data, bitPos, dwgObj);
	}

}
