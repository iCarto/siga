/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coru?a
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

import java.util.List;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardWindow;

@SuppressWarnings("serial")
public class SigaLoadMapWizard extends WizardWindow {

    public SigaLoadMapWizard(View view, List<WizardComponent> cmps) {
	    super(cmps);
	    properties.put(SigaLoadMapWizardComponent.PROPERTY_VEW, view);
	    setWindowTitle(PluginServices.getText(this, "Load_map"));
        setWindowInfoProperties(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE | WindowInfo.RESIZABLE);
    }

    public SigaLoadMapWizard(View view) {
	    super();
	    properties.put(SigaLoadMapWizardComponent.PROPERTY_VEW, view);
	    setWindowTitle(PluginServices.getText(this, "Load_map"));
        setWindowInfoProperties(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE | WindowInfo.RESIZABLE);
    }

    @Override
    protected void addWizardComponents() {
	views.add(new SigaLoadMapWizardComponent(properties));
	views.add(new LoadConstantsWizardComponent(properties));
    }
}
