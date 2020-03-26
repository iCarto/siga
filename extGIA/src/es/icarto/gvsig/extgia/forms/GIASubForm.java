package es.icarto.gvsig.extgia.forms;

import com.iver.andami.ui.mdiManager.WindowInfo;

import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
import es.icarto.gvsig.siga.PreferencesPage;

@SuppressWarnings("serial")
public class GIASubForm extends AbstractSubForm {

    public GIASubForm(String basicName) {
        super(basicName);
        initGUIPost();
    }

    public GIASubForm() {
        super();
        initGUIPost();
    }

    protected void initGUIPost() {
        addImageHandler("image", PreferencesPage.SIGA_LOGO);
        getWindowInfo().setTitle("");
    }

}
