package es.icarto.gvsig.extgia.batch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.gui.tables.handler.BaseTableHandler;
import es.icarto.gvsig.navtableforms.gui.tables.model.AlphanumericTableModel;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.navtable.dataacces.IController;

@SuppressWarnings("serial")
public abstract class BatchVegetationTrabajosAbstractSubForm extends
	BatchAbstractSubForm {

    private static final Logger logger = Logger
	    .getLogger(BatchVegetationTrabajosAbstractSubForm.class);

    public BatchVegetationTrabajosAbstractSubForm(Elements parentElement) {
	super(parentElement);
    }

    @Override
    public void actionCreateRecord() {
	this.position = -1;
	saveButton.removeActionListener(action);
	action = new BatchCreateAction(this, getFormController(), model,
		trabajosTableHandler);
	saveButton.addActionListener(action);
	setListeners();
	fillEmptyValues();
	validateForm();
	PluginServices.getMDIManager().addCentredWindow(this);
    }

    public String getDbTableName() {
	return getParentElement().trabajosTableName;
    }

    public abstract String[] getColumnNames();

    public abstract String[] getColumnDbNames();

    public abstract void getForeignValues(HashMap<String, String> values,
	    String idValue);

    private final class BatchCreateAction implements ActionListener {

	private final IWindow iWindow;
	private final IController iController;
	private final AlphanumericTableModel model;
	private final BaseTableHandler trabajosTableHandler;

	public BatchCreateAction(IWindow iWindow, IController iController,
		AlphanumericTableModel model,
		BaseTableHandler trabajosTableHandler) {
	    this.iWindow = iWindow;
	    this.iController = iController;
	    this.model = model;
	    this.trabajosTableHandler = trabajosTableHandler;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    HashMap<String, String> values = new HashMap<String, String>();

	    // controller values must be cloned to avoid bugs
	    for (Entry<String, String> f : iController.getValues().entrySet()) {
		values.put(f.getKey(), f.getValue());
	    }

	    final TOCLayerManager toc = new TOCLayerManager();
	    FLyrVect layer = toc.getLayerByName(getLayerName());

	    SelectableDataSource recordset;
	    try {
		recordset = layer.getRecordset();
		int selectedElements = 0;
		if (recordset.getSelection().isEmpty()) {
		    logger.warn("No hay registros seleccionados");
		    return;
		}
		selectedElements = recordset.getSelection().cardinality();

		Object[] options = {
			PluginServices.getText(this, "optionPane_yes"),
			PluginServices.getText(this, "optionPane_no") };
		int m = JOptionPane.showOptionDialog(
			null,
			PluginServices.getText(this, "addInfo_msg_I")
				+ selectedElements
				+ " "
				+ PluginServices
					.getText(this, "addInfo_msg_II"), null,
			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.INFORMATION_MESSAGE, null, options,
			options[1]);
		if (m == JOptionPane.OK_OPTION) {
		    String[][] data = getDataForFillingTable(values, recordset,
			    selectedElements);
		    BatchTrabajosTable table = new BatchTrabajosTable(
			    getDbTableName(), data, getColumnNames(),
			    getColumnDbNames(), trabajosTableHandler);
		    PluginServices.getMDIManager().addCentredWindow(table);
		}
	    } catch (Exception e) {
		iController.clearAll();
		position = -1;
		logger.error(e.getMessage(), e);
	    }
	    PluginServices.getMDIManager().closeWindow(iWindow);
	}

	private String[][] getDataForFillingTable(
		HashMap<String, String> values, SelectableDataSource recordset,
		int selectedElements) throws ReadDriverException {
	    String[][] data = new String[selectedElements][];
	    int dataIdx = 0;
	    int idFieldIdx = recordset.getFieldIndexByName(getIdFieldName());
	    FBitSet selection = recordset.getSelection();
	    for (int i = selection.nextSetBit(0); i >= 0; i = selection
		    .nextSetBit(i + 1)) {
		values.put(
			getIdFieldName(),
			recordset.getFieldValue(i, idFieldIdx).getStringValue(
				WRITER));
		getForeignValues(values, recordset.getFieldValue(i, idFieldIdx)
			.getStringValue(WRITER));
		String[] row = new String[getColumnNames().length];
		for (int j = 0; j < getColumnNames().length; j++) {
		    if (values.containsKey(getColumnDbNames()[j])) {
			row[j] = values.get(getColumnDbNames()[j]);
		    } else {
			row[j] = "";
		    }
		}
		data[dataIdx++] = row;
	    }
	    return data;
	}
    }

}
