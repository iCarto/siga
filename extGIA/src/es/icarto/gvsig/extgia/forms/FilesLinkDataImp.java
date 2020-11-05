package es.icarto.gvsig.extgia.forms;

import java.awt.Component;
import java.io.File;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.iver.andami.Launcher;
import com.iver.andami.PluginServices;

import es.icarto.gvsig.commons.queries.Utils;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.gui.buttons.fileslink.FilesLinkData;
import es.icarto.gvsig.siga.PreferencesPage;
import es.icarto.gvsig.siga.models.InfoEmpresa;
import es.icarto.gvsig.utils.DesktopApi;

public class FilesLinkDataImp implements FilesLinkData {
	
	private static final Logger logger = Logger.getLogger(Utils.class);
	
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
		logger.error(e.getMessage(), e);
	}
	
	if (baseDirectory.isEmpty()) {
		return null;
	}

	baseDirectory = baseDirectory + element;

	return baseDirectory;
    }

    @Override
    public String getFolder(AbstractForm form) {
	String registerValue = form.getFormController().getValue(
		getRegisterField());
	
	if (getBaseDirectory() == null) {
		return null;
	}
	
	String folderName = getBaseDirectory() + File.separator + registerValue;
	return folderName;
    }
}
