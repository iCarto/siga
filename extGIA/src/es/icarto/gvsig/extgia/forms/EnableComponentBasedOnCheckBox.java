package es.icarto.gvsig.extgia.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class EnableComponentBasedOnCheckBox implements ActionListener {

    private final JComponent[] components;
    private final JCheckBox checkbox;
    private boolean removeDependentValues;

    public EnableComponentBasedOnCheckBox(JCheckBox checkbox,
	    JComponent... components) {

	this.checkbox = checkbox;
	this.components = components;

    }

    public void setRemoveDependentValues(boolean removeDependentValues) {
	this.removeDependentValues = removeDependentValues;
    }

    public void setListeners() {
	checkbox.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	enableComponentsIfCheckBoxIsSelected();
    }

    private void enableComponentsIfCheckBoxIsSelected() {
	boolean enabled = checkbox.isSelected();
	for (JComponent c : components) {
	    c.setEnabled(enabled);
	    if (!enabled) {
		removeValue(c);
	    }
	}
    }

    private void removeValue(JComponent c) {
	if (c instanceof JFormattedTextField) {
	    ((JFormattedTextField) c).setText("");
	} else if (c instanceof JTextField) {
	    ((JTextField) c).setText("");
	} else if (c instanceof JComboBox) {
	    ((JComboBox) c).setSelectedIndex(0);
	    System.out.println("EnableComponentBasedOnCheckBox: Buggy Feature");
	} else if (c instanceof JCheckBox) {
	    ((JCheckBox) c).setSelected(false);
	} else if (c instanceof JTextArea) {
	    ((JTextArea) c).setText("");
	}
    }

    public void removeListeners() {
	checkbox.removeActionListener(this);
    }

    public void fillSpecificValues() {
	enableComponentsIfCheckBoxIsSelected();
    }

}