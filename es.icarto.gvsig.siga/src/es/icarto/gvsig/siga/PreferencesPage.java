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

    public static final String AG_INVENTORY_FOLDER_KEY = "AGInventoryFolder";
    public static final String AP_INVENTORY_FOLDER_KEY = "APInventoryFolder";
    public static final String AG_EXPROPIATIONS_FOLDER_KEY = "AGExpropiationsFolder";
    public static final String AP_EXPROPIATIONS_FOLDER_KEY = "APExpropiationsFolder";

    private final String id;

    private FolderChooser agInventoryFolder;
    private FolderChooser apInventoryFolder;
    private FolderChooser agExpropiationsFolder;
    private FolderChooser apExpropiationsFolder;

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
	
	agExpropiationsFolder = new FolderChooser(panel, PluginServices.getText(this,
			"AG Expropiaciones"), "");
	
	agInventoryFolder = new FolderChooser(panel, PluginServices.getText(this,
			"AG Inventario"), "");
	
	apExpropiationsFolder = new FolderChooser(panel, PluginServices.getText(this,
			"AP Expropiaciones"), "");
	
	apInventoryFolder = new FolderChooser(panel, PluginServices.getText(this,
			"AP Inventario"), "");
	
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
	
	if (!agInventoryFolder.isFolder()) {
	    String msg = String.format(baseMsg, agInventoryFolder.getFolderPath());
	    throw new StoreException(msg);
	}
	
	if (!apInventoryFolder.isFolder()) {
	    String msg = String.format(baseMsg, apInventoryFolder.getFolderPath());
	    throw new StoreException(msg);
	}
	
	if (!agExpropiationsFolder.isFolder()) {
	    String msg = String.format(baseMsg, agExpropiationsFolder.getFolderPath());
	    throw new StoreException(msg);
	}
	
	if (!apExpropiationsFolder.isFolder()) {
	    String msg = String.format(baseMsg, agExpropiationsFolder.getFolderPath());
	    throw new StoreException(msg);
	}

	xml.putProperty(AG_INVENTORY_FOLDER_KEY, agInventoryFolder.getFolderPath());
	xml.putProperty(AP_INVENTORY_FOLDER_KEY, apInventoryFolder.getFolderPath());
	xml.putProperty(AG_EXPROPIATIONS_FOLDER_KEY, agExpropiationsFolder.getFolderPath());
	xml.putProperty(AP_EXPROPIATIONS_FOLDER_KEY, apExpropiationsFolder.getFolderPath());

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
	agInventoryFolder.setSelectedFile(getValue(xml, AG_INVENTORY_FOLDER_KEY));
	apInventoryFolder.setSelectedFile(getValue(xml, AP_INVENTORY_FOLDER_KEY));
	agExpropiationsFolder.setSelectedFile(getValue(xml, AG_EXPROPIATIONS_FOLDER_KEY));
	apExpropiationsFolder.setSelectedFile(getValue(xml, AP_EXPROPIATIONS_FOLDER_KEY));
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
    
    public static String getAGInventoryBaseDirectory() {
    PluginServices ps = PluginServices.getPluginServices(PLUGIN_NAME);
    XMLEntity xml = ps.getPersistentXML();
    if (xml.contains(AG_INVENTORY_FOLDER_KEY)) {
    	baseDirectory = xml.getStringProperty(AG_INVENTORY_FOLDER_KEY);
    }
    return baseDirectory;
    }
     
    public static String getAPInventoryBaseDirectory() {
    PluginServices ps = PluginServices.getPluginServices(PLUGIN_NAME);
    XMLEntity xml = ps.getPersistentXML();
    if (xml.contains(AP_INVENTORY_FOLDER_KEY)) {
    	baseDirectory = xml.getStringProperty(AP_INVENTORY_FOLDER_KEY);
    }
    return baseDirectory;
    }
    
    public static String getAGExpropiationsBaseDirectory() {
    PluginServices ps = PluginServices.getPluginServices(PLUGIN_NAME);
    XMLEntity xml = ps.getPersistentXML();
    if (xml.contains(AG_EXPROPIATIONS_FOLDER_KEY)) {
    	baseDirectory = xml.getStringProperty(AG_EXPROPIATIONS_FOLDER_KEY);
    }
    return baseDirectory;
    }
    
    public static String getAPExpropiationsBaseDirectory() {
    PluginServices ps = PluginServices.getPluginServices(PLUGIN_NAME);
    XMLEntity xml = ps.getPersistentXML();
    if (xml.contains(AP_EXPROPIATIONS_FOLDER_KEY)) {
    	baseDirectory = xml.getStringProperty(AP_EXPROPIATIONS_FOLDER_KEY);
    }
    return baseDirectory;
    }

}