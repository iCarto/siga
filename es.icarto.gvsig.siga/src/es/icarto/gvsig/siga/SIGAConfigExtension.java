package es.icarto.gvsig.siga;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.preferences.IPreference;
import com.iver.andami.preferences.IPreferenceExtension;
import com.iver.cit.gvsig.About;
import com.iver.cit.gvsig.gui.panels.FPanelAbout;

import es.icarto.gvsig.navtableforms.SIGAFormFactory;
import es.icarto.gvsig.utils.SIGAFormatter;
import es.udc.cartolab.gvsig.elle.ConfigExtension;
import es.udc.cartolab.gvsig.elle.utils.MapFilter;
import es.udc.cartolab.gvsig.users.preferences.UsersPreferencePage;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class SIGAConfigExtension extends Extension implements
	IPreferenceExtension {

    private static final Logger logger = Logger
	    .getLogger(SIGAConfigExtension.class);

    @Override
    public void initialize() {
	setVersion();
	SIGAFormFactory.registerFormFactory();

	UsersPreferencePage.LOGO = PreferencesPage.SIGA_LOGO;
    }

    private void setVersion() {
	About about = (About) PluginServices.getExtension(About.class);
	FPanelAbout panelAbout = about.getAboutPanel();
	Properties props = new Properties();
	try {
	    String file = PluginServices.getPluginServices(this)
		    .getPluginDirectory().getAbsolutePath()
		    + File.separator + "VERSION";
	    props.load(new FileInputStream(file));
	} catch (Exception e) {
	    logger.error(e.getStackTrace(), e);
	}
	String version = props.getProperty("version");
	panelAbout.setCustomVersion(version);
    }

    @Override
    public void postInitialize() {
	ConfigExtension configExt = (ConfigExtension) PluginServices
		.getExtension(ConfigExtension.class);
	configExt.setWizardTitle(PreferencesPage.APP_NAME);
	configExt.setMapFilter(new MapFilter() {
	    @Override
	    public String[] filter(String[] maps) {
		List<String> mapsToShow = new ArrayList<String>();
		for (int i = 0; i < maps.length; i++) {
		    if (maps[i].startsWith("BDD_")) {
			mapsToShow.add(maps[i]);
		    }
		}
		Collections.sort(mapsToShow);
		return mapsToShow.toArray(new String[0]);
	    }
	});
	DBSession.setFormatter(new SIGAFormatter());
    }

    @Override
    public void execute(String actionCommand) {
    }

    @Override
    public boolean isEnabled() {
	return false;
    }

    @Override
    public boolean isVisible() {
	return false;
    }

    @Override
    public IPreference[] getPreferencesPages() {
	IPreference[] preferences = new IPreference[1];
	preferences[0] = new PreferencesPage();
	return preferences;
    }

}
