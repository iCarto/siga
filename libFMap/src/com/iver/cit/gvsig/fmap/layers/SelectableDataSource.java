/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package com.iver.cit.gvsig.fmap.layers;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.hardcode.driverManager.Driver;
import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.driver.exceptions.ReloadDriverException;
import com.hardcode.gdbms.driver.exceptions.WriteDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.data.IDataSourceListener;
import com.hardcode.gdbms.engine.data.NoSuchTableException;
import com.hardcode.gdbms.engine.data.SourceInfo;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.data.edition.DataWare;
import com.hardcode.gdbms.engine.data.persistence.Memento;
import com.hardcode.gdbms.engine.data.persistence.MementoContentHandler;
import com.hardcode.gdbms.engine.data.persistence.MementoException;
import com.hardcode.gdbms.engine.instruction.EvaluationException;
import com.hardcode.gdbms.engine.instruction.FieldNotFoundException;
import com.hardcode.gdbms.engine.instruction.SemanticException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueCollection;
import com.hardcode.gdbms.parser.ParseException;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.layers.layerOperations.Selectable;
import com.iver.utiles.NumberUtilities;
import com.iver.utiles.XMLEntity;


/**
 * DataSource seleccionable.
 *
 * @author Fernando González Cortés
 */
public class SelectableDataSource implements DataSource,Selectable {
	private static Logger logger = Logger.getLogger(SelectableDataSource.class.getName());
	private SelectionSupport selectionSupport = new SelectionSupport();
	private DataSource dataSource;

	private int[] mapping = null;
	private String[] alias = null;

	/**
	 * Crea un nuevo SelectableDataSource.
	 *
	 * @param name
	 * @param ds
	 * @throws ReadDriverException TODO
	 */
	public SelectableDataSource(DataSource ds) throws ReadDriverException {
		dataSource = ds;
		dataSource.start();
		// Creamos el mapping de campos externos que no muestran el PK.
		mapExternalFields();
		alias = new String[mapping.length];
		for (int i=0; i < mapping.length; i++)
		{
			alias[i] = getFieldName(i);
		}



	}

	/**
	 * Maps real fields or "external" fields. We don't want to see virtual fields.
	 * @throws ReadDriverException
	 */
	public void mapExternalFields() throws ReadDriverException {
		int numExternalFields = 0;
//		this.dataSource.start();
		int fieldCount=dataSource.getFieldCount();
		for (int i=0; i < fieldCount; i++)
		{
			if (!dataSource.isVirtualField(i))
				numExternalFields++;

		}

		mapping = new int[numExternalFields];

		int j=0;
		for (int i=0; i < fieldCount; i++)
		{
			if (!dataSource.isVirtualField(i)) {
				mapping[j] = i;
				j++;
			}

		}

//		this.dataSource.stop();
	}

	public static SelectableDataSource createSelectableDataSource(XMLEntity xml) throws DriverLoadException, XMLException{

		SelectionSupport ss = new SelectionSupport();
		ss.setXMLEntity(xml.getChild(0));
		XMLEntity xmlDS = xml.getChild(1);
		GDBMSParser parser = new GDBMSParser(xmlDS);
		MementoContentHandler gdbmsHandler = new MementoContentHandler();
		parser.setContentHandler(gdbmsHandler);
		try {
			parser.parse();
		} catch (SAXException e) {
			throw new XMLException(e);
		}
		SelectableDataSource sds;
        try {
            sds = new SelectableDataSource(gdbmsHandler.getDataSource(LayerFactory.getDataSourceFactory(), DataSourceFactory.MANUAL_OPENING));
        } catch (EvaluationException e1) {
            throw new XMLException(e1);
        } catch (ReadDriverException e) {
        	 throw new XMLException(e);
		} catch (DriverLoadException e) {
			 throw new XMLException(e);
		} catch (SemanticException e) {
			 throw new XMLException(e);
		} catch (ParseException e) {
			 throw new XMLException(e);
		} catch (NoSuchTableException e) {
			 throw new XMLException(e);
		}
        sds.selectionSupport=ss;
		return sds;
	}

	public void setDataSourceFactory(DataSourceFactory dsf) {
		dataSource.setDataSourceFactory(dsf);
	}
	public void setSourceInfo(SourceInfo sourceInfo) {
		dataSource.setSourceInfo(sourceInfo);
	}
	/**
	 * Añade el soporte para la selección.
	 *
	 * @param selectionSupport
	 */
	public void setSelectionSupport(SelectionSupport selectionSupport) {
		this.selectionSupport = selectionSupport;
	}

	/**
	 * Devuelve el número de campos.
	 *
	 * @return Número de campos.
	 *
	 * @throws DriverException
	 */
	public int getFieldCount() throws ReadDriverException {
		// return dataSource.getFieldCount()-numVirtual;
//		if (mapping.length != dataSource.getFieldCount())
//		{
//			mapExternalFields();
//			RuntimeException e = new RuntimeException("Recalculamos los campos de recordset!!");
//			e.printStackTrace();
//		}
		if (mapping.length!=dataSource.getFieldCount()){
			mapExternalFields();
		}
		return mapping.length;
	}

	/**
	 * Return index field searching by its name
	 *
	 * @param arg0 field name.
	 *
	 * @return field index. -1 if not found
	 *
	 * @throws DriverException
	 * @throws FieldNotFoundException
	 */
	public int getFieldIndexByName(String arg0)
		throws ReadDriverException {
//		int internal = dataSource.getFieldIndexByName(arg0);
//		for (int i=0; i < mapping.length; i++)
//		{
//			if (mapping[i] == internal)
//				return i;
//		}
//		//OJO Parche para rodear poblema de gdbms + FileDriver
//		// Cuando en le fichero existe un campo de nombre 'PK'
//		if (arg0.equalsIgnoreCase("pk")){
//			for (int i=0; i < mapping.length; i++)
//			{
//				if (dataSource.getFieldName(mapping[i]).equalsIgnoreCase(arg0)){
//					return i;
//				}
//			}
//
//		}
		for (int i=0; i < getFieldCount(); i++) {
			// Buscamos en los alias. Si no hay alias, cada alias es igual al fieldname
			if (getFieldAlias(i).compareToIgnoreCase(arg0) == 0)
				return i;
		}
		for (int i=0; i < getFieldCount(); i++) {
			// Por compatibilidad con posibles leyendas guardadas
			if (getFieldName(i).compareToIgnoreCase(arg0) == 0)
				return i;
		}

		return -1;
	}

	/**
	 * Devuelve el nombre del campo a partir del índice.
	 *
	 * @param arg0 índice.
	 *
	 * @return nombre del campo.
	 *
	 * @throws DriverException
	 */
	public String getFieldName(int arg0) throws ReadDriverException {
	    // return dataSource.getFieldName(arg0);
		return dataSource.getFieldName(mapping[arg0]);
	}

	/**
	 * Devuelve el valor a partir del númro de fila y columna.
	 *
	 * @param arg0 número de registro.
	 * @param arg1 número de campo.
	 *
	 * @return Valor.
	 *
	 * @throws DriverException
	 */
	public Value getFieldValue(long arg0, int arg1) throws ReadDriverException {
		if (arg1==-1)
			return null;
		return dataSource.getFieldValue(arg0, mapping[arg1]);
		// return dataSource.getFieldValue(arg0, arg1);
	}

	/**
	 * Devuelve el nombre del DataSource.
	 *
	 * @return Nombre.
	 */
	public String getName() {
		return dataSource.getName();
	}

	/**
	 * Devuelve el número de filas en total.
	 *
	 * @return número de filas.
	 *
	 * @throws DriverException
	 */
	public long getRowCount() throws ReadDriverException {
		return dataSource.getRowCount();
	}

	/**
	 * Inicializa el dataSource.
	 *
	 * @throws DriverException
	 */
	public void start() throws ReadDriverException {
		// logger.debug("dataSource.start()");
		dataSource.start();
	}

	/**
	 * Finaliza el DataSource.
	 *
	 * @throws DriverException
	 */
	public void stop() throws ReadDriverException {
		// logger.debug("dataSource.stop()");
		dataSource.stop();
	}

	/**
	 * Cuando ocurre un evento de cambio en la selección, éste puede ser uno de
	 * una gran cantidad de eventos. Con el fin de no propagar todos estos
	 * eventos, se realiza la propagación de manera manual al final de la
	 * "ráfaga" de eventos
	 */
	public void fireSelectionEvents() {
		selectionSupport.fireSelectionEvents();
	}

	/**
	 * Añade un nuevo Listener al SelectionSupport.
	 *
	 * @param listener SelectionListener.
	 */
	public void addSelectionListener(SelectionListener listener) {
		selectionSupport.addSelectionListener(listener);
	}

	/**
	 * Borra un Listener al SelectionSupport.
	 *
	 * @param listener Listener a borrar.
	 */
	public void removeSelectionListener(SelectionListener listener) {
		selectionSupport.removeSelectionListener(listener);
	}

	/**
	 * Borra la selección.
	 */
	public void clearSelection() {
		selectionSupport.clearSelection();
	}

	/**
	 * Develve un FBitSet con los índices de los elementos seleccionados.
	 *
	 * @return FBitset con los elementos seleccionados.
	 */
	public FBitSet getSelection() {
		return selectionSupport.getSelection();
	}

	/**
	 * Devuelve el SelectionSupport.
	 *
	 * @return SelectinSuport.
	 */
	public SelectionSupport getSelectionSupport() {
		return selectionSupport;
	}

	/**
	 * Devuelve true si el elemento está seleccionado.
	 *
	 * @param recordIndex índice del registro.
	 *
	 * @return True si el registro está seleccionado.
	 */
	public boolean isSelected(int recordIndex) {
		return selectionSupport.isSelected(recordIndex);
	}

	/**
	 * Inserta una nueva selección.
	 *
	 * @param selection FBitSet.
	 */
	public void setSelection(FBitSet selection) {
		selectionSupport.setSelection(selection);
	}

	private void putMemento(XMLEntity xml) throws XMLException {
		try {
			GDBMSHandler handler = new GDBMSHandler();
			Memento m = getMemento();
			m.setContentHandler(handler);
			m.getXML();
			XMLEntity child = handler.getXMLEntity();

			xml.addChild(child);
		} catch (MementoException e) {
			throw new XMLException(e);
		} catch (SAXException e) {
			throw new XMLException(e);
		}
	}

	/**
	 * Devuelve el XMLEntity con la información necesaria para reproducir el
	 * DataSource.
	 *
	 * @return XMLEntity.
	 * @throws XMLException
	 */
	public XMLEntity getXMLEntity() throws XMLException {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className",this.getClass().getName());
		xml.addChild(selectionSupport.getXMLEntity());
		putMemento(xml);

		return xml;
	}

	/**
	 * @see com.hardcode.gdbms.engine.data.DataSource#getWhereFilter()
	 */
	public long[] getWhereFilter() throws IOException {
		return dataSource.getWhereFilter();
	}

	/*
	 * @see com.hardcode.gdbms.engine.data.ReadDriver#getFieldType(int)
	 */
	public int getFieldType(int i) throws ReadDriverException {
		// return dataSource.getFieldType(i);
		if (i>mapping.length-1)
			return dataSource.getFieldType(i);
		return dataSource.getFieldType(mapping[i]);
	}

	/*
	 * @see com.hardcode.gdbms.engine.data.DataSource#getDataSourceFactory()
	 */
	public DataSourceFactory getDataSourceFactory() {
		return dataSource.getDataSourceFactory();
	}

	/*
	 * @see com.hardcode.gdbms.engine.data.DataSource#getAsString()
	 */
	public String getAsString() throws ReadDriverException {
		return dataSource.getAsString();
	}

	/*
	 * @throws DriverException
	 * @see com.hardcode.gdbms.engine.data.DataSource#remove()
	 */
	public void remove() throws WriteDriverException {
		dataSource.remove();
	}

	/*
	 * @see com.hardcode.gdbms.engine.data.DataSource#getMemento()
	 */
	public Memento getMemento() throws MementoException {
		return dataSource.getMemento();
	}

	/*
	 * @see com.hardcode.gdbms.engine.data.DataSource#getSourceInfo()
	 */
	public SourceInfo getSourceInfo() {
		return dataSource.getSourceInfo();
	}

    /*
     * @see com.hardcode.gdbms.engine.data.DataSource#getPrimaryKeys()
     */
    public int[] getPrimaryKeys() throws ReadDriverException {
    	return dataSource.getPrimaryKeys();
    }

    /*
     * @see com.hardcode.gdbms.engine.data.DataSource#getPKValue(long)
     */
    public ValueCollection getPKValue(long rowIndex) throws ReadDriverException {
        return dataSource.getPKValue(rowIndex);
    }

    /*
     * @see com.hardcode.gdbms.engine.data.DataSource#getPKName(int)
     */
    public String getPKName(int fieldId) throws ReadDriverException {
        return dataSource.getPKName(fieldId);
    }

    /*
     * @see com.hardcode.gdbms.engine.data.DataSource#getPKType(int)
     */
    public int getPKType(int i) throws ReadDriverException {
        return dataSource.getPKType(i);
    }

    /*
     * @throws DriverException
     * @see com.hardcode.gdbms.engine.data.DataSource#getPKCardinality()
     */
    public int getPKCardinality() throws ReadDriverException {
        return dataSource.getPKCardinality();
    }

    /*
     * @see com.hardcode.gdbms.engine.data.DataSource#getRow(long)
     */
    public Value[] getRow(long rowIndex) throws ReadDriverException {
    	Value[] withoutVirtuals = new Value[mapping.length];
    	Value[] internal = dataSource.getRow(rowIndex);
    	for (int i=0; i < mapping.length; i++)
    	{
    		if (mapping[i] < internal.length)
    			withoutVirtuals[i] = internal[mapping[i]];

    	}
        return withoutVirtuals;
    }

    /*
     * @see com.hardcode.gdbms.engine.data.DataSource#getFieldNames()
     */
    public String[] getFieldNames() throws ReadDriverException {
    	int fieldCount=getFieldCount();
    	String[] fieldNames = new String[fieldCount];
//		int j=0;
//		int fieldCount=dataSource.getFieldCount();
//		for (int i=0; i < fieldCount; i++)
//		{
//			if (!dataSource.isVirtualField(i))
//				fieldNames[j++] = dataSource.getFieldName(i);
//
//		}
		for (int i=0; i < fieldCount; i++)
		{
			fieldNames[i] = getFieldAlias(i);

		}

    	return fieldNames;
    }

    /*
     * @see com.hardcode.gdbms.engine.data.DataSource#getPKNames()
     */
    public String[] getPKNames() throws ReadDriverException {
        return dataSource.getPKNames();
    }

	public void removeLinksSelectionListener() {
		selectionSupport.removeLinkSelectionListener();
	}

    /*
     * @throws DriverException
     * @see com.hardcode.gdbms.engine.data.DataSource#getDataWare(int)
     */
    public DataWare getDataWare(int arg0) throws ReadDriverException {
        return dataSource.getDataWare(arg0);
    }

	public int getFieldWidth(int i) throws ReadDriverException {
		return dataSource.getFieldWidth(mapping[i]);
		// return dataSource.getFieldWidth(i);
	}

	public boolean isVirtualField(int fieldId) throws ReadDriverException {
		return dataSource.isVirtualField(fieldId);
	}

	/**
	 * Useful to writers, to know the field definitions.
	 * NOTE: Maximun precision: 6 decimals. (We may need to change this)
	 * @return Description of non virtual fields
	 * @throws DriverException
	 */
	public FieldDescription[] getFieldsDescription() throws ReadDriverException{
		int numFields = getFieldCount();
		FieldDescription[] fieldsDescrip = new FieldDescription[numFields];
		for (int i = 0; i < numFields; i++) {
			fieldsDescrip[i] = new FieldDescription();
			int type = getFieldType(i);
			fieldsDescrip[i].setFieldType(type);
			fieldsDescrip[i].setFieldName(getFieldName(i));
			fieldsDescrip[i].setFieldLength(getFieldWidth(i));
			try{
				fieldsDescrip[i].setFieldAlias(getFieldAlias(i));
			}catch (Exception e) {
				fieldsDescrip[i].setFieldAlias(getFieldName(i));
			}
			if (NumberUtilities.isNumeric(type))
			{
				if (!NumberUtilities.isNumericInteger(type))
					// TODO: If there is a lost in precision, this should be changed.
					fieldsDescrip[i].setFieldDecimalCount(6);
			}
			else
				fieldsDescrip[i].setFieldDecimalCount(0);
			// TODO: ¿DEFAULTVALUE?
			// fieldsDescrip[i].setDefaultValue(get)
		}
		return fieldsDescrip;
	}

	public String getFieldAlias(int i) {
		try{
			return alias[i];
		}catch (ArrayIndexOutOfBoundsException e) {
			try {
				return getFieldName(i);
			} catch (ReadDriverException e1) {
				return null;
			}
		}
	}
	public void setFieldAlias(int idField, String aliasName) {
		alias[idField] = aliasName;
	}


	public Driver getDriver() {
		return this.dataSource.getDriver();
	}

	public void reload() throws ReloadDriverException {
		dataSource.reload();
		try {
			mapExternalFields();
		} catch (ReadDriverException e) {
			throw new ReloadDriverException(getDriver().getName(),e);
		}

	}

	public void addDataSourceListener(IDataSourceListener listener) {
		dataSource.addDataSourceListener(listener);

	}

	public void removeDataSourceListener(IDataSourceListener listener) {
		dataSource.removeDataSourceListener(listener);

	}
}
