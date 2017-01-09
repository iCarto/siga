package es.icarto.gvsig.navtableforms.gui;

import es.icarto.gvsig.commons.gui.tables.NotEditableTableModel;
import es.icarto.gvsig.commons.utils.Field;

@SuppressWarnings("serial")
public class CustomTableModel extends NotEditableTableModel {

    public String getColumnKey(int colIndex) {
	return ((Field) columnIdentifiers.get(colIndex)).getKey();
    }

}
