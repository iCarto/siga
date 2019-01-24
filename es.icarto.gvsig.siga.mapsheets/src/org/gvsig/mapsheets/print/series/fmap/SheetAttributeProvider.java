package org.gvsig.mapsheets.print.series.fmap;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.driver.exceptions.ReloadDriverException;
import com.hardcode.gdbms.engine.data.edition.DataWare;
import com.hardcode.gdbms.engine.data.object.ObjectDataWareImpl;
import com.iver.cit.gvsig.fmap.drivers.LayerDefinition;

/**
 * Implementation of one of the intermediate classes needed to create
 * in-memory vector layer.
 * 
 * @author jldominguez
 *
 */
public class SheetAttributeProvider extends ObjectDataWareImpl {
	
	private LayerDefinition ldef = null;
	
	public SheetAttributeProvider(LayerDefinition ld) {
		ldef = ld;
	}

    public void start() throws ReadDriverException { }
    public void stop() throws ReadDriverException { }
    
    public int getFieldCount() throws ReadDriverException {
    	return ldef.getFieldsDesc().length;
    }
    
    
    private int[] pk = {0};
    public int[] getPrimaryKeys() throws ReadDriverException {
        return pk;
    }



    /**
     * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldName(int)
     */
    public String getFieldName(int i) throws ReadDriverException {
        return ldef.getFieldsDesc()[i].getFieldName();
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldType(int)
     */
    public int getFieldType(int i) throws ReadDriverException {
    	return ldef.getFieldsDesc()[i].getFieldType();
    }

    /**
     * @see com.hardcode.gdbms.engine.data.DataSource#getDataWare(int)
     */
    public DataWare getDataWare(int mode) throws ReadDriverException {
        return null;

    }

	public int getFieldWidth(int i) throws ReadDriverException {
		return ldef.getFieldsDesc()[i].getFieldLength();
	}

	public void reload() throws ReloadDriverException {
	}



}
