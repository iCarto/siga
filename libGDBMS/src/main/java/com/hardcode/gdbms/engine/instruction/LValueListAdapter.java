package com.hardcode.gdbms.engine.instruction;

import com.hardcode.gdbms.engine.values.Value;

/**
 * @author Fernando Gonz�lez Cort�s
 */
public class LValueListAdapter extends Adapter {
    
    public int getListLength() {
        return getChilds().length;
    }
    
    public Value getLValue(int index, long rowIndex) throws EvaluationException{
        return ((LValueElementAdapter)getChilds()[index]).evaluate(rowIndex);        
    }

}
