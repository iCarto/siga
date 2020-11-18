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

import es.icarto.gvsig.extgia.forms.GIASubForm;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.gui.tables.handler.BaseTableHandler;
import es.icarto.gvsig.navtableforms.gui.tables.model.AlphanumericTableModel;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.navtable.ToggleEditing;
import es.udc.cartolab.gvsig.navtable.dataacces.IController;
import es.udc.cartolab.gvsig.navtable.format.ValueFormatNT;

@SuppressWarnings("serial")
public class BatchAbstractSubForm extends GIASubForm {

    protected static final ValueFormatNT WRITER = new ValueFormatNT();

    private static final Logger logger = Logger
	    .getLogger(BatchAbstractSubForm.class);

    private final String primaryKey;

    protected BaseTableHandler trabajosTableHandler;

    private Elements parentElement;

    public BatchAbstractSubForm(String basicName) {
	super(basicName);
	setForeingKey(new HashMap<String, String>());
	primaryKey = basicName.endsWith("_trabajos") ? "id_trabajo"
		: "n_inspeccion";
    }

    public BatchAbstractSubForm(Elements parentElement) {
	this(parentElement.batchTrabajosBasicName);
	this.parentElement = parentElement;
    }

    public void setTrabajoTableHandler(BaseTableHandler trabajosTableHandler) {
	this.trabajosTableHandler = trabajosTableHandler;
    }

    public Elements getParentElement() {
	return parentElement;
    }

    @Override
    public void actionCreateRecord() {
	this.position = -1;
	saveButton.removeActionListener(action);
	action = new BatchCreateAction(this, getFormController(), model);
	saveButton.addActionListener(action);
	setListeners();
	fillEmptyValues();
	PluginServices.getMDIManager().addCentredWindow(this);
    }

    public String getLayerName() {
	return parentElement.layerName;
    }

    public String getIdFieldName() {
	return parentElement.pk;
    }

    private final class BatchCreateAction implements ActionListener {

	private final IWindow iWindow;
	private final IController iController;
	private final AlphanumericTableModel model;

	public BatchCreateAction(IWindow iWindow, IController iController,
		AlphanumericTableModel model) {
	    this.iWindow = iWindow;
	    this.iController = iController;
	    this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    HashMap<String, String> values = getValuesToBeWritten();

	    try {
	    SelectableDataSource recordset = getParentTableRecordset();
		 
		
		final FBitSet selection = recordset.getSelection();
		int selectionCount = selection.cardinality();
		if (selection.isEmpty()) {
		    logger.warn("No record selected");
		    return;
		}

		int m = askUserBeforeContinue(selectionCount);
		if (m != JOptionPane.OK_OPTION) {
			return;
		}
		
		int idFieldIndex = recordset.getFieldIndexByName(getIdFieldName());
		ToggleEditing te = new ToggleEditing();
		if (!model.getSource().isEditing()) {
		    te.startEditing(model.getSource());
		}
		for (int i = selection.nextSetBit(0); i >= 0; i = selection.nextSetBit(i+1)) {
			values.put(getIdFieldName(), recordset.getFieldValue(i, idFieldIndex).getStringValue(WRITER));
		    values.remove(primaryKey);
		    iController.create(values, false);
		}
	    te.stopEditing(model.getSource());
	    showInfoMsgToUser(selectionCount);
		model.dataChanged();
	    } catch (Exception e) {
		iController.clearAll();
		position = -1;
		logger.error(e.getStackTrace());
	    } finally {
	    	PluginServices.getMDIManager().closeWindow(iWindow);	    	
	    }
	}

	private void showInfoMsgToUser(int selectionCount) {
		JOptionPane.showMessageDialog(
		    null,
		    PluginServices.getText(this, "addedInfo_msg_I")
		    + selectionCount
		    + " "
		    + PluginServices.getText(this,
			    "addedInfo_msg_II"));
	}

	

	private SelectableDataSource getParentTableRecordset()
			throws ReadDriverException {
		final TOCLayerManager toc = new TOCLayerManager();
		FLyrVect layer = toc.getLayerByName(getLayerName());
		SelectableDataSource recordset = layer.getRecordset();
		return recordset;
	}

	// controller values must be cloned to avoid bugs
	private HashMap<String, String> getValuesToBeWritten() {
		HashMap<String, String> values = new HashMap<String, String>();
	    for (Entry<String, String> f : iController.getValues().entrySet()) {
		values.put(f.getKey(), f.getValue());
	    }
		return values;
	}
    }
    
    private int askUserBeforeContinue(int selectionCount) {
		Object[] options = {
			PluginServices.getText(this, "optionPane_yes"),
			PluginServices.getText(this, "optionPane_no") };
		int m = JOptionPane.showOptionDialog(
			null,
			PluginServices.getText(this, "addInfo_msg_I")
			+ selectionCount
			+ " "
			+ PluginServices
			.getText(this, "addInfo_msg_II"), null,
			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.INFORMATION_MESSAGE, null, options,
			options[1]);
		return m;
	}

}
