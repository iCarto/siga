package org.gvsig.gui.beans.table.models;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.table.DefaultTableModel;

public class ROIsTableModel extends DefaultTableModel implements IModel {
	private static final long serialVersionUID = 8716862990277121681L;

	
	private static boolean[]	canEdit = new boolean[] { true, false, false, false, true};
	private static Class[]   	types   = new Class[] { String.class, Integer.class, Integer.class, Integer.class, JButton.class};
	private static Color[]   	colors  = new Color[] {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN,
		Color.ORANGE, Color.PINK, Color.WHITE, Color.BLACK};
	
	public ROIsTableModel(String[] columnNames) {
		super(new Object[0][5], columnNames);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gui.beans.table.models.IModel#getNewLine()
	 */
	public Object[] getNewLine() {
		Color color = null;
		if (this.getRowCount() < colors.length) {
			color = colors[this.getRowCount()];
		}
		else{
			color = new Color((float)Math.random(),(float)Math.random(),(float)Math.random());
		}
		return new Object[] {"", new Integer(0), new Integer(0), new Integer(0), color};
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int columnIndex) {
		return types [columnIndex];
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return canEdit [columnIndex];
	}
}
