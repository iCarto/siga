package es.udc.cartolab.gvsig.elle.gui.wizard.delete;

import java.awt.Component;

import javax.swing.JButton;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.WindowInfo;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardWindow;

@SuppressWarnings("serial")
public class DeleteAllLegendsWizard extends WizardWindow {

    public DeleteAllLegendsWizard() {
	super();
    }

    public WindowInfo getWindowInfo() {
	return super.getWindowInfo();
    }

    public Object getWindowProfile() {
	return WindowInfo.DIALOG_PROFILE;
    }

    @Override
    protected void addWizardComponents() {
	views.add(new DeleteAllLegendsWizardComponent(properties));
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
