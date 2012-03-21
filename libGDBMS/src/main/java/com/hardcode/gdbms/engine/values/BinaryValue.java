package com.hardcode.gdbms.engine.values;

import java.sql.Types;

import com.hardcode.gdbms.engine.instruction.IncompatibleTypesException;


/**
 *
 */
public class BinaryValue extends AbstractValue {
	private byte[] value;

	/**
	 *
	 */
	BinaryValue(byte[] bytes) {
		value = bytes;
	}

	/**
	 * Crea un nuevo BinaryValue.
	 */
	BinaryValue() {
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < value.length; i++) {
		    byte b = value[i];
		    String s = Integer.toHexString(b);
		    if (s.length() == 1){
		        sb.append("0");
		    }
		    sb.append(s);
		}

		return sb.toString();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param value DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws IncompatibleTypesException DOCUMENT ME!
	 */
	public Value equals(Value value) throws IncompatibleTypesException {
		if (value instanceof NullValue) {
			return ValueFactory.createValue(false);
		}

		if (value instanceof BinaryValue) {
		    BinaryValue bv = (BinaryValue) value;
		    boolean ret = true;
		    if (this.value.length != bv.value.length) 
		        ret = false;
		    else {
			    for (int i = 0; i < this.value.length; i++) {
	                if (this.value[i] != bv.value[i]){
	                    ret = false;
	                    break;
	                }
	            }
		    }
			return ValueFactory.createValue(ret);
		} else {
			throw new IncompatibleTypesException();
		}
	}

	/**
	 * @see com.hardcode.gdbms.engine.values.Value#notEquals()
	 */
	public Value notEquals(Value value) throws IncompatibleTypesException {
		if (value instanceof NullValue) {
			return ValueFactory.createValue(false);
		}

		if (value instanceof BinaryValue) {
			return ValueFactory.createValue(!((BooleanValue)equals(value)).getValue());
		} else {
			throw new IncompatibleTypesException();
		}
	}

	/**
	 * @see com.hardcode.gdbms.engine.values.Value#doHashCode()
	 */
	public int doHashCode() {
		return value.hashCode();
	}

	/**
     * @return
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * @see com.hardcode.gdbms.engine.values.Value#getStringValue(com.hardcode.gdbms.engine.data.driver.ValueWriter)
     */
    public String getStringValue(ValueWriter writer) {
        return writer.getStatementString(value);
    }

    /**
     * @see com.hardcode.gdbms.engine.values.Value#getType()
     */
    public int getSQLType() {
        return Types.LONGVARBINARY;
    }

	public int getWidth() {
		return value.length;
	}
}
