package es.udc.cartolab.gvsig.elle.gui.wizard.load;

import es.icarto.gvsig.commons.gui.tables.NotEditableTableModel;


@SuppressWarnings("serial")
public class MunicipiosTableModel extends NotEditableTableModel {

    @Override
    public Object getValueAt(int row, int column) {
	if (column == 2) {
	    String ap = super.getValueAt(row, 3) == null ? "" : super
		    .getValueAt(row, 3).toString();
	    String ag = super.getValueAt(row, 4) == null ? "" : super
		    .getValueAt(row, 4).toString();
	    String sep = (!ag.isEmpty() && !ap.isEmpty() ? " | " : "");
	    return "<html>" + ap + sep + ag;
	}
	return super.getValueAt(row, column);
    }
}
