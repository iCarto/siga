package es.icarto.gvsig.commons.queries;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;

import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.udc.cartolab.gvsig.navtable.dataacces.IController;

public class MockController implements IController {

    private Map<String, JComponent> widgets;

    public MockController(Map<String, JComponent> widgets) {
	this.widgets = widgets;
    }

    @Override
    public long create(HashMap<String, String> newValues) throws Exception {
	return 0;
    }
    @Override
	public long create(HashMap<String, String> newValues, boolean closeEdition)
			throws Exception {
		return 0;
	}

    @Override
    public void read(long position) throws ReadDriverException {
    }

    @Override
    public void update(long position) throws ReadDriverException,
	    StopWriterVisitorException {
    }

    @Override
    public void delete(long position) throws StopWriterVisitorException,
	    InitializeWriterException, StartWriterVisitorException,
	    ReadDriverException {
    }

    @Override
    public void clearAll() {
    }

    @Override
    public int getIndex(String fieldName) {
	return 0;
    }

    @Override
    public int[] getIndexesOfValuesChanged() {
	return null;
    }

    @Override
    public String getValue(String fieldName) {
	return stringValue(fieldName);
    }
    
    protected String stringValue(String name) {
	JComponent jComponent = widgets.get(name);
	String value = "";
	if (jComponent instanceof JTextField) {
	    value = ((JTextField) jComponent).getText().trim();
	} else if (jComponent instanceof JComboBox) {
	    Object selectedItem = ((JComboBox) jComponent).getSelectedItem();
	    if (selectedItem != null) {
		if (selectedItem instanceof KeyValue) {
		    value = ((KeyValue) selectedItem).getKey();
		} else {
		    value = selectedItem.toString().trim();
		}
	    }
	}
	return value;
    }

    @Override
    public String getValueInLayer(String fieldName) {
	return null;
    }

    @Override
    public HashMap<String, String> getValues() {
	return null;
    }

    @Override
    public HashMap<String, String> getValuesOriginal() {
	return null;
    }

    @Override
    public HashMap<String, String> getValuesChanged() {
	return null;
    }

    @Override
    public void setValue(String fieldName, String value) {
    }

    @Override
    public int getType(String fieldName) {
	return 0;
    }

    @Override
    public long getRowCount() throws ReadDriverException {
	return 0;
    }

}
