package com.hardcode.gdbms.engine.function;

import com.hardcode.gdbms.engine.values.Value;


/**
 * Interface to be implemented to create a function. The name will be
 * the string used in the SQL to refeer the function. A function will be 
 * created once for each instruction execution. 
 */
public interface Function {
    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws FunctionException DOCUMENT ME!
     */
    public Value evaluate(Value[] args) throws FunctionException;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isAggregate();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Function cloneFunction();
}
