package es.icarto.gvsig.navtableforms;

import java.io.InputStream;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

@SuppressWarnings("serial")
public abstract class BasicAbstractForm extends AbstractForm {

    public BasicAbstractForm(FLyrVect layer) {
	super(layer);
	setWindowTitle(PluginServices.getText(this, getBasicName()));
    }

    @Override
    public FormPanel getFormBody() {
	if (formBody == null) {
	    InputStream stream = getClass().getClassLoader()
		    .getResourceAsStream("/forms/" + getBasicName() + ".xml");
	    try {
		formBody = new FormPanel(stream);
	    } catch (FormException e) {
		e.printStackTrace();
	    }
	}
	return formBody;
    }

    @Override
    public String getXMLPath() {
	return this.getClass().getClassLoader()
		.getResource("rules/" + getBasicName() + ".xml").getPath();
    }

    protected abstract String getBasicName();

}