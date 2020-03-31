package es.icarto.gvsig.commons.gui;

import java.io.InputStream;

import javax.swing.ImageIcon;

import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.icarto.gvsig.siga.PreferencesPage;

@SuppressWarnings("serial")
public abstract class BasicAbstractWindow extends AbstractIWindow {

    protected FormPanel formPanel;

    public BasicAbstractWindow() {
	super();
	formPanel = getFormPanel();
	this.add(formPanel);
	addImageHandler("image", PreferencesPage.SIGA_LOGO);
    }

    public FormPanel getFormPanel() {
	if (formPanel == null) {
	    InputStream stream = getClass().getClassLoader()
		    .getResourceAsStream("/forms/" + getBasicName() + ".jfrm");
	    if (stream == null) {
		stream = getClass().getClassLoader().getResourceAsStream(
			"/forms/" + getBasicName() + ".xml");
	    }
	    try {
		formPanel = new FormPanel(stream);
	    } catch (FormException e) {
		e.printStackTrace();
	    }
	}
	return formPanel;
    }

    protected abstract String getBasicName();

    /**
     * Instead of create an implementation of ImageHandler that only sets a path
     * (FixedImageHandler) this utiliy method sets the image without doing
     * anything more
     * 
     * @param imgComponent
     *            . Name of the abeille widget
     * @param absPath
     *            . Absolute path to the image or relative path from andami.jar
     */
    protected void addImageHandler(String imgComponent, String absPath) {
	ImageComponent image = (ImageComponent) formPanel.getComponentByName(imgComponent);
        if (image != null) {
            ImageIcon icon = new ImageIcon(absPath);
            image.setIcon(icon);
        }
    }

}
