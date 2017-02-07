package es.icarto.gvsig.extgia.forms;

import java.io.File;

import com.iver.andami.Launcher;

import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.gui.buttons.fileslink.FilesLinkData;
import es.icarto.gvsig.siga.PreferencesPage;

public class FilesLinkDataImp implements FilesLinkData {
    private final Elements element;

    public FilesLinkDataImp(Elements element) {
	this.element = element;
    }

    @Override
    public String getRegisterField() {
	return element.pk;
    }

    @Override
    public String getBaseDirectory() {
	String baseDirectory = null;
	try {
	    baseDirectory = PreferencesPage.getBaseDirectory();
	} catch (Exception e) {
	}

	if (baseDirectory == null || baseDirectory.isEmpty()) {
	    baseDirectory = Launcher.getAppHomeDir();
	}

	baseDirectory = baseDirectory + File.separator + "FILES"
		+ File.separator + "inventario" + File.separator + element;

	return baseDirectory;
    }

    @Override
    public String getFolder(AbstractForm form) {
	String registerValue = form.getFormController().getValue(
		getRegisterField());

	String folderName = getBaseDirectory() + File.separator + registerValue;
	return folderName;
    }
}
