package com.iver.cit.gvsig.fmap.edition;

import java.util.HashMap;

import com.iver.cit.gvsig.exceptions.expansionfile.CloseExpansionFileException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileWriteException;
import com.iver.cit.gvsig.exceptions.expansionfile.OpenExpansionFileException;
import com.iver.cit.gvsig.fmap.core.IRow;


/**
 * Maneja el fichero de extensi�n en el que se almacenan las modificacionesy adici�nes
 * durante la edici�n. Los �ndices que devuelve esta clase en sus m�todos
 * addRow y modifyRow son invariables, es decir, que si se invoca un
 * m�todo addRow que retorna un 8, independientemente de las operaciones
 * que se realicen posteriormente, una llamada a getRow(8) retornar�
 * dicha fila. Si esta geometr�a es eliminada posteriormente, se retornar� un
 * null. Esto �ltimo se cumple mientras no se invoque el m�todo compact, mediante
 * el que se reorganizan las geometr�as dejando en el fichero s�lo las que tienen
 * validez en el momento de realizar la invocaci�n.
 */
public interface ExpansionFile {
	/**
	 * A�ade una geometria al final del fichero y retorna el �ndice que ocupa
	 * esta geometria en el mismo
	 *
	 * @param row DOCUMENT ME!
	 * @param status TODO
	 * @param indexInternalFields fields that where valid when this row was added.
	 *
	 * @return calculated index of the new row.
	 * @throws ExpansionFileWriteException TODO
	 */
	int addRow(IRow row, int status, int indexInternalFields) throws ExpansionFileWriteException;

	/**
	 * Modifica la index-�sima geometr�a del fichero devolviendo la posici�n en
	 * la que se pone la geometria modificada.
	 *
	 * @param calculated index of row to be modified
	 * @param row newRow that replaces the old row.
	 *
	 * @return new calculated index of the modified row.
	 * @throws ExpansionFileWriteException TODO
	 */
	int modifyRow(int index, IRow row, int indexInternalFields) throws ExpansionFileWriteException;

	/**
	 * Obtiene la geometria que hay en el �ndice 'index' o null si la geometr�a
	 * ha sido invalidada.
	 *
	 * @param index caculatedIndex of the row to be read.
	 * @return
	 * @throws ExpansionFileReadException TODO
	 */
	IRowEdited getRow(int index) throws ExpansionFileReadException;

	/**
	 * Invalida una geometr�a, de cara a una futura compactaci�n del fichero
	 *
	 * @param index DOCUMENT ME!
	 */
	//void invalidateRow(int index);

	/**
	 * Realiza una compactaci�n del fichero que maneja esta clase
	 *
	 * @param relations DOCUMENT ME!
	 */
	void compact(HashMap relations);

	/**
	 * Devuelve el n�mero de geometr�as del fichero.
	 *
	 * @return n�mero de geometr�as.
	 */
	//int getRowCount();

    /**
     * Mueve el puntero de escritura de manera que las siguientes escrituras
     * machacar�n la �ltima fila
     */
    void deleteLastRow();

    /**
     * Abre el fichero de expansi�n para comenzar la edici�n
     * @throws OpenExpansionFileException TODO
     */
    void open() throws OpenExpansionFileException;

    /**
     * Cierra el fichero de expansi�n al terminar la edici�n
     * @throws CloseExpansionFileException TODO
     */
    void close() throws CloseExpansionFileException;

	/**
	 * @param previousExpansionFileIndex
	 */
	//void validateRow(int previousExpansionFileIndex);
	//BitSet getInvalidRows();

    int getSize();
}
