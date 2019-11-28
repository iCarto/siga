package es.icarto.gvsig.extpm.forms.filesLink;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.siga.PreferencesPage;
import es.icarto.gvsig.siga.models.InfoEmpresaGIA;
import es.icarto.gvsig.utils.DesktopApi;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;

public class FilesLinkAction {
	
	private static final String PM_TRAMO_FIELD = "loc_tramo";

    private SelectableDataSource recordset = null;
    private String directoryLayerName = null;
    private String directoryFieldName = null;
    protected static Logger logger = Logger.getLogger("extPM");

    public FilesLinkAction(AbstractNavTable dialog, FilesLinkData data) {
	this.recordset = data.getRecordset();
	this.directoryLayerName = data.getDirectoryLayerName();
	this.directoryFieldName = data.getDirectoryFieldName();
    }

    public SelectableDataSource getRecordSet() {
	return this.recordset;
    }

    public String getDirectoryLayerName() {
	return this.directoryLayerName;
    }

    public String getDirectoryFieldName() {
	return directoryFieldName;
    }

    public void showFiles(long registerIndex) {
	try {
	    SelectableDataSource recordset = getRecordSet();
	    int fieldIdx = recordset
		    .getFieldIndexByName(getDirectoryFieldName());
	    String registerValue = recordset.getFieldValue(registerIndex,
		    fieldIdx).toString();
	    
	    InfoEmpresaGIA infoEmpresa = new InfoEmpresaGIA();
	    int tramoField = recordset.getFieldIndexByName(PM_TRAMO_FIELD);
	    String tramoValue = recordset.getFieldValue(registerIndex, 
	    		tramoField).toString();
	    
	    String baseDirectory = null;
	    if (infoEmpresa.getCompany(tramoValue).equalsIgnoreCase(DBNames.AUDASA_COMPANY)) {
	    baseDirectory = PreferencesPage.getAPExpropiationsBaseDirectory();	
	    }else if (infoEmpresa.getCompany(tramoValue).equalsIgnoreCase(DBNames.AUTOESTRADAS_COMPANY)){
	    baseDirectory = PreferencesPage.getAGExpropiationsBaseDirectory();
	    }

	    if (baseDirectory.isEmpty()) {
	    DesktopApi.showError("Configure correctamente el directorio en la página de preferencias");
		return;
	    }
	    
	    String folderPath = baseDirectory
		    + directoryLayerName;
	    String folderName = folderPath + File.separator + registerValue;
	    File folder = new File(folderName);
	    logger.debug("Folder name is: " + folderName);
	    logger.debug("Folder absolute path is: " + folder.getAbsolutePath());

	    if (folder.exists()) {
		/*
		 * TODO: Improve how to do this. *Theorically*, Java is
		 * multiplatform :/
		 * 
		 * Desktop API only works with JVM 1.6, so when using this
		 * extension with gvSIG portable on windows (jvm 1.5) is needed
		 * to change for the next.
		 * 
		 * Disclaimer: I do know that is possible to do
		 * System.getProperty(os.version) to know which OS you are
		 * running, but as users can use different versions of Windows
		 * and I don't know which information gives each one depending
		 * on its version (if "W7", "Windows 2000", "Win XP",...) I
		 * prefer do that explicity.
		 * 
		 * Patches to improve this are welcome!
		 */

		// Code to use in windows platform with gvSIG portable
		// String commands = "explorer /select," +
		// folderName;//.getAbsolutePath();
		// String[] commands = new String[] { "cmd.exe", "/C", "start",
		// folder.getAbsolutePath() };
		// Process process = Runtime.getRuntime().exec(commands);

		Desktop desktop = Desktop.getDesktop();
		desktop.open(folder);

	    } else {
		int answer = JOptionPane.showConfirmDialog(null, String.format(
			PluginServices.getText(this,
				"folder_not_exists_create_it"), folderName),
			PluginServices.getText(this, "warning"),
			JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
		    if (folder.mkdirs()) { // will make *all* directories in the
					   // path
			Desktop desktop = Desktop.getDesktop();
			desktop.open(folder);
		    } else {
			JOptionPane.showMessageDialog(
				null,
				PluginServices.getText(this,
					"folder_could_not_be_created")
					+ ": "
					+ folderName);
		    }
		}
	    }
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	} catch (IOException e) {
	    logger.error(e.getMessage(), e);
	}

    }

}
