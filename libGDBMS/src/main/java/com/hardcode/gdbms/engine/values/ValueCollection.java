package com.hardcode.gdbms.engine.values;

import java.sql.Types;
import java.util.ArrayList;

import com.hardcode.gdbms.engine.instruction.IncompatibleTypesException;


/**
 * ArrayValue. Contains an array of Values
 *
 * @author Fernando Gonz�lez Cort�s
 */
public class ValueCollection extends AbstractValue {
	private ArrayList values = new ArrayList();

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#equals(com.hardcode.gdbms.engine.values.Value)
	 */
	public Value equals(Value value) throws IncompatibleTypesException {
		if (value instanceof NullValue) {
			return ValueFactory.createValue(false);
		}

		if (!(value instanceof ValueCollection)) {
			throw new IncompatibleTypesException(value + " is not an array");
		}

		ValueCollection arrayValue = (ValueCollection) value;

		for (int i = 0; i < values.size(); i++) {
			BooleanValue res = (BooleanValue) (((Value) values.get(i)).equals(arrayValue.get(
						i)));

			if (!res.getValue()) {
				return ValueFactory.createValue(false);
			}
		}

		return ValueFactory.createValue(true);
	}

	/**
	 * Gets the ith value of the array
	 *
	 * @param i
	 *
	 * @return
	 */
	public Value get(int i) {
		return (Value) values.get(i);
	}

	/**
	 * Gets the array size
	 *
	 * @return int
	 */
	public int getValueCount() {
		return values.size();
	}

	/**
	 * Adds a value to the end of the array
	 *
	 * @param value value to add
	 */
	public void add(Value value) {
		values.add(value);
	}

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#notEquals(com.hardcode.gdbms.engine.values.Value)
	 */
	public Value notEquals(Value value) throws IncompatibleTypesException {
		if (value instanceof NullValue) {
			return ValueFactory.createValue(false);
		}

		BooleanValue bv = (BooleanValue) equals(value);

		return ValueFactory.createValue(!bv.getValue());
	}

	/**
	 * @see com.hardcode.gdbms.engine.values.Value#doHashCode()
	 */
	public int doHashCode() {
		int acum = 0;

		for (int i = 0; i < values.size(); i++) {
			Value elem = (Value) values.get(i);
			acum += elem.hashCode();
		}

		return acum;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param values
	 */
	public void setValues(Value[] values) {
		this.values.clear();

		for (int i = 0; i < values.length; i++) {
			this.values.add(values[i]);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Value[] getValues() {
		return (Value[]) values.toArray(new Value[0]);
	}

    /**
     * @see com.hardcode.gdbms.engine.values.Value#getStringValue(com.hardcode.gdbms.engine.data.driver.ValueWriter)
     */
    public String getStringValue(ValueWriter writer) {
        throw new UnsupportedOperationException("ValueCollection does not have a standard string representation");
    }

    /**
     * @see com.hardcode.gdbms.engine.values.Value#getSQLType()
     */
    public int getSQLType() {
        return Types.OTHER;
    }

	public int getWidth() {
		return this.getValueCount();
	}
}
