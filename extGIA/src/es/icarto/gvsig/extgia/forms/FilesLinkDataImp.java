package es.icarto.gvsig.extgia.forms;

import java.io.File;

import com.iver.andami.Launcher;

import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.gui.buttons.fileslink.FilesLinkData;
import es.icarto.gvsig.siga.PreferencesPage;
import es.icarto.gvsig.siga.models.InfoEmpresa;

public class FilesLinkDataImp implements FilesLinkData {
    private final Elements element;
    private final String empresaName;

    public FilesLinkDataImp(Elements element, String empresaName) {
	this.element = element;
	this.empresaName = empresaName;
    }

    @Override
    public String getRegisterField() {
	return element.pk;
    }

    @Override
    public String getBaseDirectory() {
	String baseDirectory = null;
	try {
		if (empresaName.equalsIgnoreCase(DBFieldNames.AUDASA_COMPANY)) {
	    baseDirectory = PreferencesPage.getAPInventoryBaseDirectory();
		}else if (empresaName.equalsIgnoreCase(DBFieldNames.AUTOESTRADAS_COMPANY)){
		baseDirectory = PreferencesPage.getAGInventoryBaseDirectory();
		}
	} catch (Exception e) {
	}

	if (baseDirectory == null || baseDirectory.isEmpty()) {
	    baseDirectory = Launcher.getAppHomeDir();
	}

	baseDirectory = baseDirectory + element;

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
