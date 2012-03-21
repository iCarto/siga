package com.hardcode.gdbms.engine.data.indexes;

import java.io.File;
import java.io.IOException;


/**
 * Implementaci�n de un conjunto de �ndices en memoria, aunque puede haber un
 * conjunto de Long.MAXVALUE �ndices, en memoria, el tama�o  m�ximo es de
 * Integer.MAXVALUE. Otras implementaciones de VariableIndexSet en memoria
 * pueden no tener esta restricci�n
 *
 * @author Fernando Gonz�lez Cort�s
 */
public class FixedMemoryIndexSet extends MemoryIndexSet implements FixedIndexSet {
	/**
	 * Creates a new MemoryIndexSet object.
	 *
	 * @param initialCapacity Capacidad inicial del conjunto de �ndices. Deber�
	 * 		  de ser la capacidad m�xima que pueda llegar a tener el conjunto
	 */
	public FixedMemoryIndexSet(int initialCapacity) {
		indexes = new long[initialCapacity];
	}

	/**
	 * @see com.hardcode.gdbms.engine.data.indexes.VariableIndexSet#open(java.io.File)
	 */
	public void open(File f) {
	}

	/**
	 * @see com.hardcode.gdbms.engine.data.indexes.FixedIndexSet#setIndex(long,
	 * 		long)
	 */
	public void setIndex(long index, long value) throws IOException {
		indexes[(int) index] = value;
	}

	/**
	 * @see com.hardcode.gdbms.engine.data.indexes.BaseIndexSet#getIndexCount()
	 */
	public long getIndexCount() {
		return indexes.length;
	}
}
