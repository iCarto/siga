package es.icarto.gvsig.siga;

import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.iver.andami.PluginServices;
import com.iver.andami.preferences.AbstractPreferencePage;
import com.iver.andami.preferences.StoreException;
import com.iver.utiles.XMLEntity;

import es.icarto.gvsig.commons.gui.FolderChooser;

@SuppressWarnings("serial")
public class PreferencesPage extends AbstractPreferencePage {

    public static final String PLUGIN_NAME = "es.icarto.gvsig.siga";

    public static final String APP_NAME = "SIGA";
    public static final String APP_DESC = "Sistema de Información y Gestión de Autopistas";

    // Maybe it should be on cfg instead that in the plugin folder
    static {
	SIGA_LOGO = new File(
		"gvSIG/extensiones/es.icarto.gvsig.siga/images/logo_siga.png")
		.getAbsolutePath();
	SIGA_REPORT_LOGO = new File(
		"gvSIG/extensiones/es.icarto.gvsig.siga/images/logo_siga_report.png")
		.getAbsolutePath();
	LOGO_PATH = new File("gvSIG/extensiones/es.icarto.gvsig.siga/images/")
		.getAbsolutePath() + "/";
	AUDASA_REPORT_LOGO = new File("gvSIG/extensiones/es.icarto.gvsig.siga/images/logo_audasa_report.png").getAbsolutePath();
    }

    public static final String AUDASA_LOGO = "gvSIG/extensiones/es.icarto.gvsig.siga/images/logo_audasa.png";
    public static final String AUDASA_REPORT_LOGO;
    public static final String AUTOESTRADAS_LOGO = "gvSIG/extensiones/es.icarto.gvsig.siga/images/logo_autoestradas.png";
    public static final String SIGA_LOGO;
    public static final String SIGA_REPORT_LOGO;
    public static final String LOGO_PATH;
    public static final String IMG_UNAVAILABLE = "gvSIG/extensiones/es.icarto.gvsig.siga/images/img_no_disponible.jpg";

    public static final String FILES_FOLDER_KEY = "FilesDir";
    public static final String EXPROPIATATIONS_FOLDER_KEY = "es.icarto.gvsig.extgex.ExpropiationsFolderExtension.folder";

    private final String id;

    private FolderChooser filesFolder;
    private FolderChooser expropiationsFolder;

    private final PluginServices pluginServices;

    static String baseDirectory = "";

    public PreferencesPage() {
	super();
	id = this.getClass().getName();
	pluginServices = PluginServices.getPluginServices(PLUGIN_NAME);
	initPanel();
    }

    private void initPanel() {
	JPanel panel = new JPanel(new MigLayout("wrap 3"));

	filesFolder = new FolderChooser(panel, PluginServices.getText(this,
		"files_directory"), "");

	expropiationsFolder = new FolderChooser(panel,
		"Directorio de expropiaciones", "");
	add(panel);
    }

    @Override
    public void setChangesApplied() {
	// nothing to do here
    }

    @Override
    public void storeValues() throws StoreException {

	XMLEntity xml = pluginServices.getPersistentXML();

	String baseMsg = "%s no es un directorio válido";
	if (!filesFolder.isFolder()) {
	    String msg = String.format(baseMsg, filesFolder.getFolderPath());
	    throw new StoreException(msg);
	}

	if (!expropiationsFolder.isFolder()) {
	    String msg = String.format(baseMsg,
		    expropiationsFolder.getFolderPath());
	    throw new StoreException(msg);
	}

	xml.putProperty(FILES_FOLDER_KEY, filesFolder.getFolderPath());

	xml.putProperty(EXPROPIATATIONS_FOLDER_KEY,
		expropiationsFolder.getFolderPath());

    }

    @Override
    public String getID() {
	return id;
    }

    @Override
    public ImageIcon getIcon() {
	return null;
    }

    @Override
    public JPanel getPanel() {
	return this;
    }

    @Override
    public String getTitle() {
	return APP_NAME;
    }

    @Override
    public void initializeDefaults() {
    }

    @Override
    public void initializeValues() {
	XMLEntity xml = pluginServices.getPersistentXML();
	filesFolder.setSelectedFile(getValue(xml, FILES_FOLDER_KEY));
	expropiationsFolder.setSelectedFile(getValue(xml,
		EXPROPIATATIONS_FOLDER_KEY));
    }

    private String getValue(XMLEntity xml, String key) {
	if (xml.contains(key)) {
	    return xml.getStringProperty(key);
	}
	return "";
    }

    @Override
    public boolean isValueChanged() {
	// save always
	return true;
    }

    public static String getBaseDirectory() {
	PluginServices ps = PluginServices.getPluginServices(PLUGIN_NAME);
	XMLEntity xml = ps.getPersistentXML();
	if (xml.contains(FILES_FOLDER_KEY)) {
	    baseDirectory = xml.getStringProperty(FILES_FOLDER_KEY);
	}
	return baseDirectory;
    }

}