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
package es.udc.cartolab.gvsig.elle.gui.wizard.load;

import java.awt.Component;

import javax.swing.JButton;

import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardWindow;

@SuppressWarnings("serial")
public class LoadMapWizard extends WizardWindow {

    public LoadMapWizard(View view) {
	super();
	properties.put(LoadMapWizardComponent.PROPERTY_VEW, view);
    }

    public Object getWindowProfile() {
	return WindowInfo.DIALOG_PROFILE;
    }

    protected void addWizardComponents() {
	views.add(new LoadMapWizardComponent(properties));
	views.add(new LoadLegendWizardComponent(properties));
    }


	@Override
	protected JButton getDefaultButton() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected Component getDefaultFocusComponent() {
		// TODO Auto-generated method stub
		return null;
	}

}
