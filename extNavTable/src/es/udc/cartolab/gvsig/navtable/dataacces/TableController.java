/*
 * Copyright (c) 2011. iCarto
 *
 * This file is part of extNavTableForms
 *
 * extNavTableForms is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * extNavTableForms is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with extNavTableForms.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package es.udc.cartolab.gvsig.navtable.dataacces;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.BlankValue;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.validate.ValidateRowException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.core.DefaultRow;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.IEditableSource;
import com.iver.cit.gvsig.fmap.edition.IWriteable;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.udc.cartolab.gvsig.navtable.AbstractNavTable;
import es.udc.cartolab.gvsig.navtable.ToggleEditing;
import es.udc.cartolab.gvsig.navtable.format.ValueFactoryNT;
import es.udc.cartolab.gvsig.navtable.format.ValueFormatNT;

/**
 * Class to manage CRUD (Create, Read, Update, Delete) operations on a Table.
 * 
 * @author Andrés Maneiro <amaneiro@icarto.es>
 * @author @author Francisco Puga <fpuga@cartolab.es>
 * 
 */
public class TableController implements IController {
    
    
    private static final Logger logger = Logger
	    .getLogger(TableController.class);

    public static int NO_ROW = -1;

    private IEditableSource model;
    private HashMap<String, Integer> indexes;
    private HashMap<String, Integer> types;
    private HashMap<String, String> values;
    private HashMap<String, String> valuesChanged;

    public TableController(IEditableSource model) {
	this.model = model;
	this.indexes = new HashMap<String, Integer>();
	this.types = new HashMap<String, Integer>();
	this.values = new HashMap<String, String>();
	this.valuesChanged = new HashMap<String, String>();
    }

    public void initMetadata() {
	try {
	    SelectableDataSource sds = model.getRecordset();
	    for (int i = 0; i < sds.getFieldCount(); i++) {
		String name = sds.getFieldName(i);
		indexes.put(name, i);
		types.put(name, sds.getFieldType(i));
	    }
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	    clearAll();
	}
    }
    
    public long create(HashMap<String, String> newValues) throws Exception {
    	return create(newValues, true);
    }

    /**
     * By default we want create to open and close the edition itself. But there are cases, like
     * when creating multiple register at once (batch) where this is slow, and it's better
     * handle the edition from the caller.
     */
    public long create(HashMap<String, String> newValues, boolean closeEdition) throws Exception {

	initMetadata();
	Value[] vals = createValuesFromHashMap(newValues);

	ToggleEditing te = new ToggleEditing();
	if (!model.isEditing()) {
	    te.startEditing(model);
	}
	long newPosition = NO_ROW;
	if (model instanceof IWriteable) {
	    IRow row = new DefaultRow(vals);
	    newPosition = model.doAddRow(row, EditionEvent.ALPHANUMERIC);
	}
	if (closeEdition) {
		te.stopEditing(model);		
	}
	read(newPosition);
	return newPosition;
    }

    private Value[] createValuesFromHashMap(HashMap<String, String> newValues) {
	Value[] vals = new Value[indexes.size()];
	for (int i = 0; i < indexes.size(); i++) {
	    vals[i] = ValueFactoryNT.createNullValue();
	}
	for (String key : newValues.keySet()) {
	    try {
		vals[getIndex(key)] = ValueFactoryNT.createValueByType(
			newValues.get(key), types.get(key));
	    } catch (ParseException e) {
		vals[getIndex(key)] = ValueFactoryNT.createNullValue();
	    }
	}
	return vals;
    }

    @Override
    public void read(long position) throws ReadDriverException {
	SelectableDataSource sds = model.getRecordset();
	clearAll();
	if (position != AbstractNavTable.EMPTY_REGISTER) {
	    for (int i = 0; i < sds.getFieldCount(); i++) {
		String name = sds.getFieldName(i);
		indexes.put(name, i);
		types.put(name, sds.getFieldType(i));
		values.put(
			name,
			sds.getFieldValue(position, i).getStringValue(
				new ValueFormatNT()));
	    }
	}
    }

    @Override
    public void update(long position) throws ReadDriverException {
	ToggleEditing te = new ToggleEditing();
	boolean wasEditing = model.isEditing();
	if (!wasEditing) {
	    te.startEditing(model);
	}
	this.modifyValues(model, (int) position, getIndexesOfValuesChanged(),
		getValuesChanged().values().toArray(new String[0]));
	if (!wasEditing) {
	    te.stopEditing(model);
	}
	read((int) position);
    }
    
    // fpuga. 10/02/2015
    // Workaround: JDBCWriter, used to write values to alphanumeric postgres tables don't send null
    // values to the database, so, there is no way to "blank" an already set value. It's not 
    // enough modify Xtypes to send NullValue because in this case when a new row is inserted
    // a null is send to the pk and the not null constraint is not satisfied. Probably a new 
    // Value type DefaultValue is need to be included in gvSIG code.
    public void modifyValues(IEditableSource source, int rowPosition,
	    int[] attIndexes, String[] attValues) {
	try {
	    Value[] attributes = getNewAttributes(
		    source, rowPosition, attIndexes, attValues);

	    IRow newRow = new DefaultRow(attributes);
	    source.modifyRow(rowPosition, newRow, "NAVTABLE MODIFY",
		    EditionEvent.ALPHANUMERIC);
	} catch (ExpansionFileReadException e) {
	    logger.error(e.getMessage(), e);
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	} catch (ValidateRowException e) {
	    logger.error(e.getMessage(), e);
	}
    }
    
    private Value[] getNewAttributes(IEditableSource source, 
	    int rowPosition, 
	    int[] attIndexes, 
	    String[] attValues) {

	int type;
	try {
	    FieldDescription[] fieldDesc = source.getTableDefinition().getFieldsDesc();
	    Value[] attributes = source.getRow(rowPosition).getAttributes();
	    for (int i = 0; i < attIndexes.length; i++) {
		String att = attValues[i];
		int idx = attIndexes[i];
		if (att == null || att.length() == 0) {
		    attributes[idx] = new BlankValue();
		} else {
		    type = fieldDesc[idx].getFieldType();
		    try {
			attributes[idx] = ValueFactoryNT.createValueByType(att, type);
		    } catch (ParseException e) {
		        logger.warn(e.getStackTrace(), e);
		    }
		}
	    }
	    return attributes;
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	    return null;
	}
    }
    
    // end workaround

    @Override
    public void delete(long position) throws StopWriterVisitorException,
	    InitializeWriterException, StartWriterVisitorException,
	    ReadDriverException {

	model.startEdition(EditionEvent.ALPHANUMERIC);

	IWriteable w = (IWriteable) model;
	IWriter writer = w.getWriter();

	ITableDefinition tableDef = model.getTableDefinition();
	writer.initialize(tableDef);

	model.doRemoveRow((int) position, EditionEvent.ALPHANUMERIC);
	model.stopEdition(writer, EditionEvent.ALPHANUMERIC);
	model.getRecordset().reload();
	clearAll();
    }

    @Override
    public void clearAll() {
	indexes.clear();
	types.clear();
	values.clear();
	valuesChanged.clear();
    }

    @Override
    public int getIndex(String fieldName) {
	return indexes.get(fieldName);
    }

    @Override
    public int[] getIndexesOfValuesChanged() {
	int[] idxs = new int[valuesChanged.size()];
	Set<String> names = valuesChanged.keySet();
	int i = 0;
	for (String name : names) {
	    idxs[i] = indexes.get(name);
	    i++;
	}
	return idxs;
    }

    @Override
    public String getValue(String fieldName) {
	if (valuesChanged.containsKey(fieldName)) {
	    return valuesChanged.get(fieldName);
	}
	return values.get(fieldName);
    }

    @Override
    public String getValueInLayer(String fieldName) {
	return values.get(fieldName);
    }

    @Override
    public HashMap<String, String> getValues() {
	HashMap<String, String> val = values;
	for (String k : valuesChanged.keySet()) {
	    val.put(k, valuesChanged.get(k));
	}
	return val;
    }

    @Override
    public HashMap<String, String> getValuesOriginal() {
	return values;
    }

    @Override
    public HashMap<String, String> getValuesChanged() {
	return valuesChanged;
    }

    @Override
    public void setValue(String fieldName, String value) {
	valuesChanged.put(fieldName, value);
    }

    @Override
    public int getType(String fieldName) {
	return types.get(fieldName);
    }

    @Override
    public long getRowCount() throws ReadDriverException {
	return model.getRowCount();
    }

    @Override
    public TableController clone() {
	return new TableController(model);
    }
}
