package com.hardcode.gdbms.engine.values;

import com.hardcode.gdbms.engine.instruction.IncompatibleTypesException;


/**
 * Clase padre de todos los wrappers sobre tipos del sistema
 *
 * @author Fernando Gonz�lez Cort�s
 */
public abstract class AbstractValue implements Value {
	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#and(com.hardcode.gdbms.engine.values.value);
	 */
	public Value and(Value value) throws IncompatibleTypesException {
		throw new IncompatibleTypesException("Cannot operate with " + value +
			" and " + this);
	}

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#or(com.hardcode.gdbms.engine.values.value);
	 */
	public Value or(Value value) throws IncompatibleTypesException {
		throw new IncompatibleTypesException("Cannot operate with " + value +
			" and " + this);
	}

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#producto(com.hardcode.gdbms.engine.values.value);
	 */
	public Value producto(Value value) throws IncompatibleTypesException {
		throw new IncompatibleTypesException("Cannot operate with " + value +
			" and " + this);
	}

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#suma(com.hardcode.gdbms.engine.values.value);
	 */
	public Value suma(Value value) throws IncompatibleTypesException {
		throw new IncompatibleTypesException("Cannot operate with " + value +
			" and " + this);
	}

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#inversa(com.hardcode.gdbms.engine.values.Value)
	 */
	public Value inversa() throws IncompatibleTypesException {
		throw new IncompatibleTypesException(this +
			" does not have inverse value");
	}

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#equals(com.hardcode.gdbms.engine.values.Value)
	 */
	public Value equals(Value value) throws IncompatibleTypesException {
		throw new IncompatibleTypesException("Cannot operate with " + value +
			" and " + this);
	}

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#notEquals(com.hardcode.gdbms.engine.values.Value)
	 */
	public Value notEquals(Value value) throws IncompatibleTypesException {
		throw new IncompatibleTypesException("Cannot operate with " + value +
			" and " + this);
	}

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#greater(com.hardcode.gdbms.engine.values.Value)
	 */
	public Value greater(Value value) throws IncompatibleTypesException {
		throw new IncompatibleTypesException("Cannot operate with " + value +
			" and " + this);
	}

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#less(com.hardcode.gdbms.engine.values.Value)
	 */
	public Value less(Value value) throws IncompatibleTypesException {
		throw new IncompatibleTypesException("Cannot operate with " + value +
			" and " + this);
	}

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#greaterEqual(com.hardcode.gdbms.engine.values.Value)
	 */
	public Value greaterEqual(Value value) throws IncompatibleTypesException {
		throw new IncompatibleTypesException("Cannot operate with " + value +
			" and " + this);
	}

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#lessEqual(com.hardcode.gdbms.engine.values.Value)
	 */
	public Value lessEqual(Value value) throws IncompatibleTypesException {
		throw new IncompatibleTypesException("Cannot operate with " + value +
			" and " + this);
	}

	/**
	 * @see com.hardcode.gdbms.engine.values.Operations#like(com.hardcode.gdbms.engine.values.Value)
	 */
	public Value like(Value value) throws IncompatibleTypesException {
		throw new IncompatibleTypesException("Cannot operate with " + value +
			" and " + this);
	}

	/**
	 * @see com.hardcode.gdbms.engine.values.Value#doEquals(java.lang.Object)
	 */
	public boolean doEquals(Object obj) {
		if (obj instanceof Value) {
			try {
				return ((BooleanValue) this.equals((Value) obj)).getValue();
			} catch (IncompatibleTypesException e) {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return doEquals(obj);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return doHashCode();
	}
	
}
