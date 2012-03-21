package com.hardcode.gdbms.engine.strategies;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.data.FieldNameAccessSupport;
import com.hardcode.gdbms.engine.data.persistence.Memento;
import com.hardcode.gdbms.engine.data.persistence.MementoException;
import com.hardcode.gdbms.engine.data.persistence.OperationLayerMemento;
import com.hardcode.gdbms.engine.values.Value;


/**
 * Clase que representa el producto cartesiano de dos o m�s tablas. El
 * almacenamiento de dicha tabla se realiza en las propias tablas sobre las
 * que se opera, haciendo los c�lculos en cada acceso para saber en qu� tabla
 * y en qu� posici�n de la tabla se encuentra el dato buscado
 *
 * @author Fernando Gonz�lez Cort�s
 */
public class PDataSource extends OperationDataSource {
	private DataSource[] tables;
	private long tablesArity;
	private FieldNameAccessSupport fnaSupport = new FieldNameAccessSupport(this);

	/**
	 * Creates a new PDataSource object.
	 *
	 * @param tables Array de tablas que forman el producto
	 */
	public PDataSource(DataSource[] tables) {
		this.tables = tables;
	}

	/**
	 * Dado un �ndice de campo en la tabla producto, devuelve el �ndice en la
	 * tabla operando a la cual pertenence el campo
	 *
	 * @param fieldId �ndice en la tabla producto
	 *
	 * @return �ndice en la tabla operando
	 * @throws ReadDriverException TODO
	 */
	private int getFieldIndex(int fieldId) throws ReadDriverException {
		int table = 0;

		while (fieldId >= tables[table].getFieldCount()) {
			fieldId -= tables[table].getFieldCount();
			table++;
		}

		return fieldId;
	}

	/**
	 * Dado un �ndice de campo en la tabla producto, devuelve el �ndice en el
	 * array de tablas de la tabla operando que contiene dicho campo
	 *
	 * @param fieldId �ndice del campo en la tabla producto
	 *
	 * @return �ndice de la tabla en el array de tablas
	 * @throws ReadDriverException TODO
	 */
	private int getTableIndexByFieldId(int fieldId) throws ReadDriverException {
		int table = 0;

		while (fieldId >= tables[table].getFieldCount()) {
			fieldId -= tables[table].getFieldCount();
			table++;
		}

		return table;
	}

	/**
	 * Devuelve la fila de la tabla operando con �ndice tableIndex que contiene
	 * la informaci�n de la fila rowIndex en la tabla producto
	 *
	 * @param rowIndex fila en la tabla producto a la que se quiere acceder
	 * @param tableIndex �ndice de la tabla
	 *
	 * @return fila en la tabla operando de �ndice tableIndex que se quiere
	 * 		   acceder
	 * @throws ReadDriverException TODO
	 * @throws ArrayIndexOutOfBoundsException Si la fila que se pide (rowIndex)
	 * 		   supera el n�mero de filas de la tabla producto
	 */
	private long getTableRowIndexByTablePosition(long rowIndex, int tableIndex)
		throws ReadDriverException {
		if (rowIndex >= tablesArity) {
			throw new ArrayIndexOutOfBoundsException();
		}

		int arity = 1;

		for (int i = tableIndex + 1; i < tables.length; i++) {
			arity *= tables[i].getRowCount();
		}

		long selfArity = tables[tableIndex].getRowCount();

		return (rowIndex / arity) % selfArity;
	}

	/**
	 * @see com.hardcode.gdbms.data.DataSource#getFieldName(int)
	 */
	public String getFieldName(int fieldId) throws ReadDriverException {
		return tables[getTableIndexByFieldId(fieldId)].getFieldName(getFieldIndex(
				fieldId));
	}

	/**
	 * @see com.hardcode.gdbms.data.DataSource#getIntFieldValue(int, int)
	 */
	public Value getFieldValue(long rowIndex, int fieldId)
		throws ReadDriverException {
		int tableIndex = getTableIndexByFieldId(fieldId);

		return tables[tableIndex].getFieldValue(getTableRowIndexByTablePosition(
				rowIndex, tableIndex), getFieldIndex(fieldId));
	}

	/**
	 * @see com.hardcode.gdbms.data.DataSource#getFieldCount()
	 */
	public int getFieldCount() throws ReadDriverException {
		int ret = 0;

		for (int i = 0; i < tables.length; i++) {
			ret += tables[i].getFieldCount();
		}

		return ret;
	}

	/**
	 * @see com.hardcode.gdbms.data.DataSource#getRowCount()
	 */
	public long getRowCount() throws ReadDriverException {
		return tablesArity;
	}

	/**
	 * @see com.hardcode.gdbms.data.DataSource#open(java.io.File)
	 */
	public void start() throws ReadDriverException {
		for (int i = 0; i < tables.length; i++) {
			try {
				tables[i].start();
			} catch (ReadDriverException e) {
				for (int j = 0; j < i; j++) {
					tables[i].stop();
				}

				throw e;
			}
		}

		tablesArity = 1;

		for (int i = 0; i < tables.length; i++) {
			tablesArity *= tables[i].getRowCount();
		}
	}

	/**
	 * @see com.hardcode.gdbms.data.DataSource#close()
	 */
	public void stop() throws ReadDriverException {
		for (int i = 0; i < tables.length; i++) {
			tables[i].stop();
		}
	}

	/**
	 * @see com.hardcode.gdbms.data.DataSource#getFieldIndexByName(String)
	 */
	public int getFieldIndexByName(String fieldName) throws ReadDriverException {
		return fnaSupport.getFieldIndexByName(fieldName);
	}

	/**
	 * @see com.hardcode.gdbms.engine.data.driver.ObjectDriver#getFieldType(int)
	 */
	public int getFieldType(int i) throws ReadDriverException {
		int table = getTableIndexByFieldId(i);

		return tables[table].getFieldType(getFieldIndex(i));
	}

	/**
	 * @see com.hardcode.gdbms.engine.data.DataSource#getMemento()
	 */
	public Memento getMemento() throws MementoException {
		Memento[] mementos = new Memento[tables.length];

		for (int i = 0; i < mementos.length; i++) {
			mementos[i] = tables[i].getMemento();
		}

		return new OperationLayerMemento(getName(), mementos, getSQL());
	}

	public int getFieldWidth(int i) throws ReadDriverException {
		int table = getTableIndexByFieldId(i);

		return tables[table].getFieldWidth(getFieldIndex(i));
	}
}
