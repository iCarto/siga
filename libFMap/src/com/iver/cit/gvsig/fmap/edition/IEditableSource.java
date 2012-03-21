package com.iver.cit.gvsig.fmap.edition;

import java.io.IOException;

import com.hardcode.driverManager.Driver;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.iver.cit.gvsig.exceptions.commands.EditionCommandException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileWriteException;
import com.iver.cit.gvsig.exceptions.layers.CancelEditingLayerException;
import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
import com.iver.cit.gvsig.exceptions.layers.StopEditionLayerException;
import com.iver.cit.gvsig.exceptions.validate.ValidateRowException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.edition.commands.CommandRecord;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 */
public interface IEditableSource {
	/**
     * M�todo invocado cuando se comienza la edici�n, para poner en marcha las
     * estructuras de datos necesarias para la misma, notificar al servidor en
     * protocolos en los que sea necesario, ...
	 * @throws StartEditionLayerException
	 * @throws StartWriterVisitorException
     *
     * @throws EditionException Si no se logra poner la fuente de datos en
     *         edici�n
     */
    void startEdition(int sourceType) throws StartWriterVisitorException ;

    /**
     * Invocado cuando termina la edici�n. En funci�n de la clase concreta que
     * implemente este m�todo se generar� el fichero con los resultados de la
     * edici�n, se realizar� una transacci�n a la base de datos, etc.
     * @throws StopEditionLayerException
     * @throws StopWriterVisitorException
     *
     * @throws EditionException Si no se consiguen llevar a cabo las
     *         modificaciones
     */
   void stopEdition(IWriter writer, int sourceType) throws StopWriterVisitorException;

    /**
     * Cancela la edici�n sin escribir los cambios
     * @throws CancelEditingLayerException
     *
     * @throws IOException Si se produce un error
     */
    void cancelEdition(int sourceType) throws CancelEditingLayerException;

    /**
     * Si el �ndice se corresponde a una geometria de las originales de la capa
     * en edici�n y no ha sido modificada ni eliminada devuelve la geometria
     * original. Si ha sido modificada debera de buscar en el fichero de
     * expansi�n y si ha sido eliminada debera devolver null
     *
     * @param index �ndice de la geometr�a.
     *
     * @return Geometr�a.
     * @throws ReadDriverException
     * @throws ExpansionFileReadException
     *
     * @throws IOException Si se produce un error con el fichero de expansi�n
     * @throws DriverIOException Si se produce un error accediendo a las
     *         geometr�as originales
     */
    IRowEdited getRow(int index) throws ReadDriverException, ExpansionFileReadException;

    /**
     * Devuelve el n�mero de geometrias que hay actualmente en edici�n.
     *
     * @return N�mero de geometr�as.
     * @throws ReadDriverException
     *
     * @throws DriverIOException Si se produce un error accediendo a la capa
     * @throws DriverException
     */
    int getRowCount() throws ReadDriverException;

    /**
     * A�ade una geometria al fichero de expansi�n y guarda la correspondencia
     * en una tabla asociada.
     *
     * @param g geometr�a a guardar.
     * @throws ValidateRowException
     * @throws ExpansionFileWriteException
     * @throws ReadDriverException
     *
     * @throws DriverIOException Si se produce un error accediendo a las
     *         geometr�as originales
     * @throws IOException Si se produce un error con el fichero de expansi�n
     */
     int addRow(IRow row,String descrip, int sourceType) throws ValidateRowException, ReadDriverException, ExpansionFileWriteException;

    /**
     * Deshace la �ltima acci�n realizada. Si no hay m�s acciones no realiza
     * ninguna acci�n
     * @throws EditionCommandException
     *
     * @throws DriverIOException Si se produce un error accediendo a las
     *         geometr�as originales
     * @throws IOException Si se produce un error con el fichero de expansi�n
     */
    void undo() throws EditionCommandException;

    /**
     * Rehace la �ltima acci�n deshecha. Si no hay m�s acciones no hace nada
     * @throws EditionCommandException
     *
     * @throws DriverIOException Si se produce un error accediendo a las
     *         filas originales
     */
    void redo() throws EditionCommandException;

    /**
     * Elimina una geometria. Si es una geometr�a original de la capa en
     * edici�n se marca como eliminada (haya sido modificada o no). Si es una
     * geometr�a a�adida posteriormente se invalida en el fichero de
     * expansi�n, para que una futura compactaci�n termine con ella.
     *
     * @param index �ndice de la geometr�a que se quiere eliminar
     * @throws ExpansionFileReadException
     * @throws ReadDriverException
     *
     * @throws DriverIOException Si se produce un error accediendo a las
     *         geometr�as originales
     * @throws IOException Si se produce un error con el fichero de expansi�n
     */
    void removeRow(int index,String descrip, int sourceType) throws ReadDriverException, ExpansionFileReadException;

    /**
     * Si se intenta modificar una geometr�a original de la capa en edici�n se
     * a�ade al fichero de expansi�n y se registra la posici�n en la que se
     * a�adi�. Si se intenta modificar una geometria que se encuentra en el
     * fichero de expansi�n (por ser nueva o original pero modificada) se
     * invoca el m�todo modifyGeometry y se actualiza el �ndice de la
     * geometria en el fichero.
     *
     * @param index �ndice de la geometr�a que se quiere eliminar
     * @param type TODO
     * @param g Geometr�a nueva
     * @throws ExpansionFileWriteException
     * @throws ValidateRowException
     * @throws ReadDriverException
     * @throws ExpansionFileReadException
     * @throws IOException Si se produce un error con el fichero de expansi�n
     * @throws DriverIOException Si se produce un error accediendo a las
     *         geometr�as originales
     */
    int modifyRow(int index, IRow row,String descrip, int sourceType) throws ValidateRowException, ExpansionFileWriteException, ReadDriverException, ExpansionFileReadException;



    /**
     * Compacta el almacenamiento de las geometr�as que est�n en edici�n. Tras
     * esta operaci�n, el orden de las geometr�as seguramente cambiar� y toda
     * llamada a getGeometry devolver� una geometr�a distinta de null, ya que
     * las eliminadas son borradas definitivamente. Hay que tener especial
     * cuidado al invocar este m�todo ya que cualquier tipo de asociaci�n
     * entre geometr�as y otro tipo de objetos (comandos de edici�n, snapping,
     * ...) que use el �ndice de la geometr�a se ver� afectado por �ste m�todo
     */
    void compact();

    /**
     * Establece la imagen de las geometr�as seleccionadas con el fin de que en
     * una edici�n interactiva se pueda obtener dicha imagen para simular el
     * copiado, rotado, etc
     *
     * @param i imagen
     */
    // void setSelectionImage(Image i);

    /**
     * Obtiene una imagen con las geometr�as seleccionadas
     *
     * @return imagen
     */
    // Image getSelectionImage();

    /**
     * DOCUMENT ME!
     */
    void startComplexRow();

    void endComplexRow(String description);
    public int undoModifyRow(int geometryIndex,int previousExpansionFileIndex, int sourceType) throws EditionCommandException;
    public IRow doRemoveRow(int index, int sourceType) throws ReadDriverException, ExpansionFileReadException;
    public int doModifyRow(int index, IRow feat, int sourceType) throws ExpansionFileWriteException, ReadDriverException, ExpansionFileReadException;
    public int doAddRow(IRow feat, int sourceType) throws ReadDriverException, ExpansionFileWriteException;
    public void undoRemoveRow(int index, int sourceType) throws EditionCommandException;
    public void undoAddRow(int index, int sourceType) throws EditionCommandException;
    public SelectableDataSource getRecordset() throws ReadDriverException;
    public boolean isEditing();
    public FBitSet getSelection() throws ReadDriverException;
    public CommandRecord getCommandRecord();

	public void addEditionListener(IEditionListener listener);

	public void removeEditionListener(IEditionListener listener);

	public ITableDefinition getTableDefinition() throws ReadDriverException;

	public void validateRow(IRow row, int sourceType) throws ValidateRowException;

	/**
	 *  Use it to add, remove or rename fields. If null, you cannot modifiy the table structure
	 *  (for example, with dxf files, dgn files, etc).
	 *  The changes will be applied when stopEditing() is called.
	 */
	public IFieldManager getFieldManager();

	public void saveEdits(IWriter writer, int sourceType) throws StopWriterVisitorException;

	/**
	 * @return
	 */
	public Driver getOriginalDriver();

	/**
	 * Please, use this if you need support for defaultValues.
	 * Don't user getRecordset().getFieldsDescription()!!.
	 * The reason is ResultSetMetadata has no information about
	 * defaultValues in fields.
	 * @return
	 */
	public FieldDescription[] getFieldsDescription();


}
