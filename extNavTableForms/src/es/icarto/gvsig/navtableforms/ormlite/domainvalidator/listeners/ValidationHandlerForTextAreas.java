package es.icarto.gvsig.navtableforms.ormlite.domainvalidator.listeners;

import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import es.icarto.gvsig.navtableforms.IValidatableForm;

public class ValidationHandlerForTextAreas implements KeyListener, FocusListener {

    private IValidatableForm dialog = null;

    public ValidationHandlerForTextAreas(IValidatableForm dialog) {
	this.dialog = dialog;
    }
    
    private void confirmChange(ComponentEvent e) {
    if (!dialog.isFillingValues()) {
        JTextArea textArea = ((JTextArea) e.getSource());
        dialog.getFormController().setValue(textArea.getName(),
            textArea.getText());
        dialog.setChangedValues();
        dialog.validateForm();
    }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    confirmChange(e);
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
    confirmChange(e);
    }

}
