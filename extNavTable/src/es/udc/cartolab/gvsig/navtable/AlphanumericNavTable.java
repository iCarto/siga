/*
 * This file is part of NavTable
 * Copyright (C) 2009 - 2010  Cartolab (Universidade da Coru?a)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Authors:
 *   Juan Ignacio Varela Garc?a <nachouve (at) gmail (dot) com>
 *   Pablo Sanxiao Roca <psanxiao (at) gmail (dot) com>
 *   Javier Est?vez Vali?as <valdaris (at) gmail (dot) com>
 *   Francisco Puga Alonso <fran.puga (at) gmail (dot) com>
 */
package es.udc.cartolab.gvsig.navtable;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.commands.EditionCommandException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.core.DefaultRow;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.IEditableSource;
import com.iver.cit.gvsig.fmap.edition.IWriteable;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.udc.cartolab.gvsig.navtable.dataacces.TableController;
import es.udc.cartolab.gvsig.navtable.format.ValueFactoryNT;
import es.udc.cartolab.gvsig.navtable.utils.EditionListener;

/**
 * @author Nacho Varela
 * @author Javier Estevez
 * @author Pablo Sanxiao
 * @author Andres Maneiro
 */
@SuppressWarnings("serial")
public class AlphanumericNavTable extends NavTable {

    private static final Logger logger = Logger
	    .getLogger(AlphanumericNavTable.class);

    private JButton newB = null;
    protected IEditableSource model;
    protected HashMap<String, String> defaultValues = null;
    private ArrayList<Integer> indexesOfRowsAdded = null;

    public AlphanumericNavTable(IEditableSource model, String dataName)
	    throws ReadDriverException {
	super(null, dataName);
	this.isAlphanumericNT = true;
	this.model = model;
	this.indexesOfRowsAdded = new ArrayList<Integer>();
	this.openEmptyLayers = true;
    }

    public AlphanumericNavTable(IEditableSource model, String dataName,
	    HashMap<String, String> defaultValues) throws ReadDriverException {
	super(null, dataName);
	this.isAlphanumericNT = true;
	this.model = model;
	this.defaultValues = defaultValues;
	this.openEmptyLayers = true;
    }

    @Override
    public boolean init() {
	if (!super.init()) {
	    return false;
	}

	zoomB.setVisible(false);
	alwaysZoomCB.setVisible(false);
	fixScaleCB.setVisible(false);

	URL imgURL = getClass().getResource("/table_add.png");
	ImageIcon imagenNewRegister = new ImageIcon(imgURL);
	newB = new JButton(imagenNewRegister);
	newB.setToolTipText(PluginServices.getText(this, "new_register"));

	newB.addActionListener(this);
	zoomB.getParent().add(newB);
	// We must to rewrite selectionB listener and the others

	return true;
    }

    @Override
    protected boolean initController() {
	try {
	    layerController = new TableController(model);
	    layerController.read(getPosition());
	} catch (ReadDriverException e) {
	    logger.error(e.getStackTrace(), e);
	    return false;
	}
	return true;
    }

    @Override
    protected void setLayerListeners() {
	super.setLayerListeners();
	listener = new EditionListener(this);
	model.addEditionListener(listener);
    }

    @Override
    protected void removeLayerListeners() {
	super.removeLayerListeners();
	model.removeEditionListener(listener);
    }

    @Override
    protected boolean isSaveable() {
	stopCellEdition();
	if (model instanceof IWriteable) {
	    return true;
	} else {
	    JOptionPane.showMessageDialog(this, String.format(
		    PluginServices.getText(this, "non_editable"),
		    layer.getName()));
	    return false;
	}
    }

    @Override
    public SelectableDataSource getRecordset() {
	try {
	    return model.getRecordset();
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    @Override
    protected boolean isEditing() {
	return model.isEditing();
    }

    @Override
    @Deprecated
    // deprecated by fpuga, 28/02/2014
    protected void updateValue(int row, int col, String newValue) {
	ToggleEditing te = new ToggleEditing();
	try {
	    te.modifyValue(model, row, col, newValue);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    @Override
    public boolean saveRecord() {
	if (isSaveable()) {
	    setSavingValues(true);
	    int[] attIndexes = getIndexes();
	    String[] attValues = getValues();
	    int currentPos = Long.valueOf(getPosition()).intValue();
	    try {
		ToggleEditing te = new ToggleEditing();
		boolean wasEditing = model.isEditing();
		if (!wasEditing) {
		    te.startEditing(model);
		}
		te.modifyValues(model, currentPos, attIndexes, attValues);
		if (!wasEditing) {
		    te.stopEditing(model);
		}
		setChangedValues(false);
		return true;
	    } catch (Exception e) {
		logger.error(e.getMessage(), e);
		return false;
	    } finally {
		setSavingValues(false);
	    }
	}
	return false;
    }

    public void addRecord() {
	// Create a new empty record
	// showWarning();
	if (isOnlySelected()) {
	    setOnlySelected(false);
	}
	try {
	    ToggleEditing te = new ToggleEditing();
	    if (!model.isEditing()) {
		te.startEditing(model);
	    }
	    if (model instanceof IWriteable) {
		SelectableDataSource sds = getRecordset();
		int numAttr = sds.getFieldCount();
		Value[] values = createDefaultValues(numAttr);
		IRow row = new DefaultRow(values);
		indexesOfRowsAdded.add(model.doAddRow(row,
			EditionEvent.ALPHANUMERIC) + 1);

		setChangedValues(true);
		setPosition(sds.getRowCount());
	    }
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    private Value[] createDefaultValues(int numAttr) {
	Value[] values = new Value[numAttr];
	if (defaultValues == null) {
	    for (int i = 0; i < numAttr; i++) {
		values[i] = ValueFactoryNT.createNullValue();
	    }
	} else {
	    SelectableDataSource sds = getRecordset();
	    for (int i = 0; i < numAttr; i++) {
		if (defaultValues.get(sds.getFieldAlias(i)) == null) {
		    values[i] = ValueFactoryNT.createNullValue();
		} else {
		    values[i] = ValueFactoryNT.createValue(defaultValues
			    .get(sds.getFieldAlias(i)));
		}
	    }
	}
	return values;
    }

    /**
     * Shows a warning to the user if there's unsaved data.
     * 
     */
    @Override
    public boolean showWarning() {
	if (getPosition() == AbstractNavTable.EMPTY_REGISTER) {
	    return true;
	}
	if (isChangedValues()) {
	    boolean save = false;
	    Object[] options = {
		    PluginServices.getText(this, "saveButtonTooltip"),
		    PluginServices.getText(this, "ignoreButton") };
	    int response = JOptionPane.showOptionDialog(this,
		    PluginServices.getText(this, "unsavedDataMessage"),
		    PluginServices.getText(this, "unsavedDataTitle"),
		    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
		    null, // do not use a custom Icon
		    options, // the titles of buttons
		    options[1]); // default button title
	    if (response == 0) {
		save = true;
	    } else {
		save = false;
		setChangedValues(false);
		deleteAddedRows();
	    }
	    if (save) {
		saveRecord();
	    }
	}
	return true;
    }

    private void deleteAddedRows() {
	try {
	    for (int index : indexesOfRowsAdded) {
		model.undoAddRow(index, EditionEvent.ROW_EDITION);
	    }
	    indexesOfRowsAdded.clear();
	} catch (EditionCommandException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void deleteRecord() {
	try {
	    model.startEdition(EditionEvent.ALPHANUMERIC);

	    IWriteable w = (IWriteable) model;
	    IWriter writer = w.getWriter();

	    ITableDefinition tableDef = model.getTableDefinition();
	    writer.initialize(tableDef);

	    model.doRemoveRow((int) getPosition(), EditionEvent.ALPHANUMERIC);
	    model.stopEdition(writer, EditionEvent.ALPHANUMERIC);

	    // keep the current position within boundaries
	    setPosition(getPosition());

	} catch (StartWriterVisitorException e) {
	    logger.error(e.getMessage(), e);
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	} catch (InitializeWriterException e) {
	    logger.error(e.getMessage(), e);
	} catch (StopWriterVisitorException e) {
	    logger.error(e.getMessage(), e);
	}

    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == newB) {
	    addRecord();
	} else if (e.getSource() == removeB) {
	    int answer = JOptionPane.showConfirmDialog(null,
		    PluginServices.getText(null, "confirm_delete_register"),
		    null, JOptionPane.YES_NO_OPTION);
	    if (answer == JOptionPane.YES_OPTION) {
		deleteRecord();
	    }
	} else {
	    super.actionPerformed(e);
	}
    }

    @Override
    public void windowClosed() {
	this.newB.removeActionListener(this);
	super.windowClosed();
	this.model.removeEditionListener(listener);
    }

}
