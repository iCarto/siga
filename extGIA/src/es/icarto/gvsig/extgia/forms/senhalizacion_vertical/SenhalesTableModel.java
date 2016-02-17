package es.icarto.gvsig.extgia.forms.senhalizacion_vertical;

import java.awt.Dimension;

import javax.swing.ImageIcon;

import com.iver.cit.gvsig.fmap.edition.IEditableSource;

import es.icarto.gvsig.extgia.signalsimbology.SenhalesAlgorithm;
import es.icarto.gvsig.navtableforms.gui.tables.filter.IRowFilter;
import es.icarto.gvsig.navtableforms.gui.tables.model.AlphanumericTableModel;

@SuppressWarnings("serial")
public class SenhalesTableModel extends AlphanumericTableModel {

    private final SenhalesAlgorithm alg;

    public SenhalesTableModel(IEditableSource source, String[] colNames,
	    String[] colAliases, IRowFilter filter) {
	super(source, colNames, colAliases, filter);

	this.alg = new SenhalesAlgorithm(new Dimension(40, 25));
    }

    public SenhalesTableModel(IEditableSource source, String[] colNames,
	    String[] colAliases) {
	super(source, colNames, colAliases);
	this.alg = new SenhalesAlgorithm(new Dimension(40, 25));
    }

    private String stringValue(Object o) {
	return o != null ? o.toString().trim() : "";
    }

    @Override
    public Object getValueAt(int row, int col) {
	if (getColumnCount() - 1 == col) {
	    return getIcon(row);
	} else {
	    return super.getValueAt(row, col);
	}
    }

    private ImageIcon getIcon(int row) {
	String tipoValue = stringValue(getValueAt(row, 1));
	String codigoValue = stringValue(getValueAt(row, 2));
	String idSenhalValue = stringValue(getValueAt(row, 0));
	return this.alg.getIcon(tipoValue, codigoValue, idSenhalValue);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
	if (getColumnCount() - 1 == columnIndex) {
	    return ImageIcon.class;
	}
	return super.getColumnClass(columnIndex);
    }
}
