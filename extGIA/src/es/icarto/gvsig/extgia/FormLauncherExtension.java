package es.icarto.gvsig.extgia;

import javax.swing.JOptionPane;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgia.forms.LaunchGIAForms;
import es.icarto.gvsig.extgia.forms.SIGAFormFactory;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;

public class FormLauncherExtension extends AbstractExtension {

    private FLyrVect layer;

    @Override
    public void execute(String actionCommand) {
	this.layer = getLayerFromTOC(actionCommand);
	if (this.layer != null) {
	    LaunchGIAForms.callFormDependingOfLayer(this.layer, false);
	} else {
	    JOptionPane.showMessageDialog(null, "La capa " + actionCommand
		    + " no está cargada en el TOC", "Capa no encontrada",
		    JOptionPane.ERROR_MESSAGE);
	}
    }

    private FLyrVect getLayerFromTOC(String actionCommand) {
	final TOCLayerManager toc = new TOCLayerManager();
	return toc.getLayerByName(actionCommand);
    }

    @Override
    public boolean isEnabled() {
	return true;
    }

    @Override
    public void initialize() {
	SIGAFormFactory.registerFormFactory();
    }

}
