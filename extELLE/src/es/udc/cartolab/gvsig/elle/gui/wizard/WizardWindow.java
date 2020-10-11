/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coruña
 *
 * This file is part of ELLE
 *
 * ELLE is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * ELLE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with ELLE.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package es.udc.cartolab.gvsig.elle.gui.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

import es.icarto.gvsig.commons.gui.AbstractIWindow;

@SuppressWarnings("serial")
public abstract class WizardWindow extends AbstractIWindow implements IWindow,
	WizardListener, ActionListener {

    protected JButton nextButton, prevButton, cancelButton, finishButton;
    private JPanel mainPanel;
    protected List<WizardComponent> views = new ArrayList<WizardComponent>();
    protected int currentPos;
    protected Map<String, Object> properties = new HashMap<String, Object>();
    
    private boolean windowInfoCreated = false;

    public WizardWindow() {
	nextButton = new JButton(PluginServices.getText(this, "next"));
	nextButton.addActionListener(this);
	prevButton = new JButton(PluginServices.getText(this, "previous"));
	prevButton.addActionListener(this);
	cancelButton = new JButton(PluginServices.getText(this, "cancel"));
	cancelButton.addActionListener(this);
	finishButton = new JButton(PluginServices.getText(this, "finish"));
	finishButton.addActionListener(this);

	addWizardComponents();

    }

    public WizardWindow(List<WizardComponent> views) {
	this();
	this.views = views;
    }

    protected abstract void addWizardComponents();

    public void open() {
	setLayout(new BorderLayout(5, 5));
	mainPanel = getMainPanel();
	changeView(0);

	add(mainPanel, BorderLayout.CENTER);
	add(getSouthPanel(), BorderLayout.SOUTH);
	PluginServices.getMDIManager().addCentredWindow(this);
    }

    private JPanel getMainPanel() {
	JPanel panel = new JPanel(new CardLayout());

	for (WizardComponent view : views) {
	    panel.add(view, view.getWizardComponentName());
	}

	return panel;
    }

    private JPanel getSouthPanel() {
	JPanel panel = new JPanel();
	panel.add(cancelButton);
	panel.add(prevButton);
	panel.add(nextButton);
	panel.add(finishButton);
	return panel;
    }

    private void changeView(int position) {
	try {
	    if (position >= 0 && position < views.size()) {
		views.get(currentPos).removeWizardListener(this);
		currentPos = position;
		WizardComponent newView = views.get(currentPos);
		newView.addWizardListener(this);
		CardLayout cl = (CardLayout) mainPanel.getLayout();
		cl.show(mainPanel, newView.getWizardComponentName());
		newView.showComponent();
		updateButtons();
	    }
	} catch (WizardException e) {
	    // TODO error y cerrar
	    e.printStackTrace();
	}
    }

    public void updateButtons() {
	if (views.size() < 2) {
	    prevButton.setVisible(false);
	    nextButton.setVisible(false);
	    finishButton.setText(PluginServices.getText(this, "ok"));
	}
	WizardComponent currentView = views.get(currentPos);
	int nViews = views.size();
	nextButton
		.setEnabled(currentPos != nViews - 1 && currentView.canNext());
	prevButton.setEnabled(currentPos > 0);
	finishButton.setEnabled(currentView.canFinish());
    }

    @Override
    public void wizardChanged() {
	updateButtons();
    }

    public void close() {
	PluginServices.getMDIManager().closeWindow(this);
    }

    protected void finish() {
	boolean close = true;
	try {
	    for (WizardComponent wc : views) {
		wc.finish();
	    }
	} catch (WizardException e) {
	    close = e.closeWizard();
	    if (e.showMessage()) {
		JOptionPane.showMessageDialog(this, e.getMessage(), "",
			JOptionPane.ERROR_MESSAGE);
	    }
	    e.printStackTrace();
	}
	if (close) {
	    close();
	}
    }

    protected void next() {
	try {
	    views.get(currentPos).setProperties();
	    changeView(currentPos + 1);
	    views.get(currentPos).showComponent();
	} catch (WizardException e) {
	    JOptionPane.showMessageDialog(this, e.getMessage(), "",
		    JOptionPane.ERROR_MESSAGE);
	}
    }

    protected void previous() {
	changeView(currentPos - 1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == cancelButton) {
	    close();
	}
	if (e.getSource() == prevButton) {
	    previous();
	}
	if (e.getSource() == nextButton) {
	    next();
	}
	if (e.getSource() == finishButton) {
	    finish();
	}
    }

    public void add(WizardComponent component) {
	views.add(component);
    }

    public void remove(WizardComponent component) {
	views.remove(component);
    }
    
    @Override
    protected JButton getDefaultButton() {
        return null;
    }

    @Override
    protected Component getDefaultFocusComponent() {
        return null;
    }
    
    @Override
    /*
     * Por algún motivo en el panel de cargar constantes en Windows aparece un
     * scrollbar vertical. Este método está para darle un poco más de ancho. La
     * primera vez le damos más ancho y las siguientes llamamos directamente al
     * método padre que tiene la instancia como una variable. El +40 está
     * sacado por prueba y error. 
     */
    public WindowInfo getWindowInfo() {
        if (!windowInfoCreated) {
            windowInfoCreated = true;
            WindowInfo wi = super.getWindowInfo();
            wi.setWidth(wi.getWidth() + 15);            
        }
        return super.getWindowInfo();
    }
}
