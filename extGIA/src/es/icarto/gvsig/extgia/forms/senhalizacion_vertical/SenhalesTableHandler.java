package es.icarto.gvsig.extgia.forms.senhalizacion_vertical;

import java.util.Map;

import javax.swing.JComponent;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.edition.IEditableSource;

import es.icarto.gvsig.extgia.forms.GIAAlphanumericTableHandler;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
import es.icarto.gvsig.navtableforms.gui.tables.filter.IRowFilter;
import es.icarto.gvsig.navtableforms.gui.tables.filter.IRowFilterImplementer;
import es.icarto.gvsig.navtableforms.utils.TOCTableManager;

public class SenhalesTableHandler extends GIAAlphanumericTableHandler {

    public SenhalesTableHandler(String tableName,
	    Map<String, JComponent> widgets, String foreignKeyId,
	    String[] colNames, String[] colAliases, int[] colWidths,
	    AbstractForm form, Class<? extends AbstractSubForm> subFormClass) {

	super(tableName, widgets, foreignKeyId, colNames, colAliases,
		colWidths, form, subFormClass);
	jtable.setRowHeight(36);
    }

    @Override
    protected void createTableModel() throws ReadDriverException {
	SenhalesTableModel model = null;
	model = createFromTableWithFilterSenhales(sourceTableName,
		getDestinationKey(), getOriginKeyValue(), colNames, colAliases);

	jtable.setModel(model);

	if (subform != null) {
	    subform.setModel(model);
	}
    }

    public static SenhalesTableModel createFromTableWithFilterSenhales(
	    String sourceTable, String rowFilterName, String rowFilterValue,
	    String[] columnNames, String[] columnAliases)
		    throws ReadDriverException {

	TOCTableManager toc = new TOCTableManager();
	IEditableSource model = toc.getTableModelByName(sourceTable);
	int fieldIndex = model.getRecordset()
		.getFieldIndexByName(rowFilterName);
	IRowFilter filter = new IRowFilterImplementer(fieldIndex,
		rowFilterValue);
	return new SenhalesTableModel(model, columnNames, columnAliases, filter);
    }
}
