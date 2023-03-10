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
import com.iver.cit.jdwglib.dwg.objects.DwgInsert;

/**
 * @author alzabord
 */
public class DwgInsertReader1314 extends AbstractDwg1314Reader{

	/* (non-Javadoc)
	 * @see com.iver.cit.jdwglib.dwg.readers.IDwgObjectReader#readSpecificObj(int[], int, com.iver.cit.jdwglib.dwg.DwgObject)
	 */
	public void readSpecificObj(int[] data, int offset, DwgObject dwgObj) throws RuntimeException, CorruptedDwgEntityException {
		if(! (dwgObj instanceof DwgInsert))
	    	throw new RuntimeException("ArcReader 14 solo puede leer DwgInsert");
		DwgInsert insert = (DwgInsert) dwgObj;
		int bitPos = offset;
		bitPos = headTailReader.readObjectHeader(data, bitPos, insert);
		
		List val = DwgUtil.getBitDouble(data, bitPos);
		bitPos = ((Integer)val.get(0)).intValue();
		double x = ((Double)val.get(1)).doubleValue();
		
		val = DwgUtil.getBitDouble(data, bitPos);
		bitPos = ((Integer)val.get(0)).intValue();
		double y = ((Double)val.get(1)).doubleValue();

		val = DwgUtil.getBitDouble(data, bitPos);
		bitPos = ((Integer)val.get(0)).intValue();
		double z = ((Double)val.get(1)).doubleValue();
		insert.setInsertionPoint(new double[]{x, y, z});
		
		val = DwgUtil.getBitDouble(data, bitPos);
		bitPos = ((Integer)val.get(0)).intValue();
		x = ((Double)val.get(1)).doubleValue();
		
		val = DwgUtil.getBitDouble(data, bitPos);
		bitPos = ((Integer)val.get(0)).intValue();
		y = ((Double)val.get(1)).doubleValue();

		val = DwgUtil.getBitDouble(data, bitPos);
		bitPos = ((Integer)val.get(0)).intValue();
		z = ((Double)val.get(1)).doubleValue();
		insert.setScale(new double[]{x, y, z});
		
		val = DwgUtil.getBitDouble(data, bitPos);
		bitPos = ((Integer)val.get(0)).intValue();
		double rotation = ((Double)val.get(1)).doubleValue();
		insert.setRotation(rotation);
		
		val = DwgUtil.getBitDouble(data, bitPos);
		bitPos = ((Integer)val.get(0)).intValue();
		x = ((Double)val.get(1)).doubleValue();
		
		val = DwgUtil.getBitDouble(data, bitPos);
		bitPos = ((Integer)val.get(0)).intValue();
		y = ((Double)val.get(1)).doubleValue();

		val = DwgUtil.getBitDouble(data, bitPos);
		bitPos = ((Integer)val.get(0)).intValue();
		z = ((Double)val.get(1)).doubleValue();
		insert.setExtrusion(new double[]{x, y, z});
		
		val = DwgUtil.testBit(data, bitPos);
		bitPos = ((Integer)val.get(0)).intValue();
		boolean hasAttr = ((Boolean)val.get(1)).booleanValue();
		
		bitPos = headTailReader.readObjectTailer(data, bitPos, insert);
		
		DwgHandleReference handle = new DwgHandleReference();
		bitPos = handle.read(data, bitPos);
		insert.setBlockHeaderHandle(handle);
		
		if(hasAttr){
			handle = new DwgHandleReference();
			bitPos = handle.read(data, bitPos);
			insert.setFirstAttribHandle(handle);
			
			handle = new DwgHandleReference();
			bitPos = handle.read(data, bitPos);
			insert.setLastAttribHandle(handle);
			
			handle = new DwgHandleReference();
			bitPos = handle.read(data, bitPos);
			insert.setSeqendHandle(handle);
		}
	}

}
