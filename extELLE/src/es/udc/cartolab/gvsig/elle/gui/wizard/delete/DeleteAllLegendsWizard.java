package es.udc.cartolab.gvsig.elle.gui.wizard.delete;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.WindowInfo;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardWindow;

@SuppressWarnings("serial")
public class DeleteAllLegendsWizard extends WizardWindow {

    public DeleteAllLegendsWizard() {
	    super();
	    setWindowTitle(PluginServices.getText(this, "delete_legends"));
        setWindowInfoProperties(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE | WindowInfo.RESIZABLE);
    }

    @Override
    protected void addWizardComponents() {
	views.add(new DeleteAllLegendsWizardComponent(properties));
    }

}
