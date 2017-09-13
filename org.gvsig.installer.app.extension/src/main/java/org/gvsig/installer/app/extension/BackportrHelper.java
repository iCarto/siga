package org.gvsig.installer.app.extension;

import java.io.File;

import org.gvsig.installer.app.extension.creation.MakePluginPackageExtension;
import org.gvsig.installer.lib.api.InstallerLocator;
import org.gvsig.installer.lib.api.InstallerManager;
import org.gvsig.tools.library.impl.DefaultLibrariesInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iver.andami.Launcher;
import com.iver.andami.iconthemes.IconThemeManager;
import com.iver.cit.gvsig.Version;

/**
 * backport parcial de algunas clases e interfaces de la 2 que son usadas en
 * el codifgo de este plugin.
 * 
 * @author jjdelcerro
 *
 */
public class BackportrHelper {
	private static final Logger logger = LoggerFactory.getLogger(BackportrHelper.class);


	private static boolean alreadyInitialized = false;
	
	private static void initialize() {
	    if( alreadyInitialized ) {
	      return;
	    }
	    alreadyInitialized = true;
	    
	    Version version = new Version();
	    logger.info("BackportHelper inicialinig... (gvsig ver "+version.getMajor()+"."+version.getMinor()+").");

	    if( version.getMajor()==1 && version.getMinor()==11 ) {
	      logger.info("Initializing libraries.");
	      new DefaultLibrariesInitializer(BackportrHelper.class.getClassLoader()).fullInitialize();
	    }
	    
	    File defaultAddonsRepository = PluginsLocator.getManager().getPluginsFolder();
	    InstallerManager installerManager = InstallerLocator.getInstallerManager();
	    installerManager.addLocalAddonRepository(defaultAddonsRepository);
	    installerManager.setDefaultLocalAddonRepository(defaultAddonsRepository);

	    logger.info("BackportHelper inicialzed.");
	    logger.info("defaultAddonsRepository = '"+defaultAddonsRepository.toString()+"'.");
	  }
	
	
	public interface PluginsManager {
		public File getPluginsFolder();
		public File getApplicationFolder();
		public File getInstallFolder();
    }
	public static class PluginsLocator {
		public static PluginsManager getManager() {
			initialize();
			return new DefaultPluginsManager();
		}
	    public static IconThemeManager getIconThemeManager() {
			initialize();
	    	return IconThemeManager.getIconThemeManager();
	    }
	}
	public static class DefaultPluginsManager implements PluginsManager {
		private File pluginsFolder = null;
		
		public File getPluginsFolder() {
			if (this.pluginsFolder != null) {
				return this.pluginsFolder;
			}
			String folder = "gvSIG/extensiones";
			if (!(Launcher.getAndamiConfig() == null || Launcher
					.getAndamiConfig().getPluginsDirectory() == null)) {
				folder = Launcher.getAndamiConfig().getPluginsDirectory();
			}
			this.pluginsFolder = new File(getApplicationFolder(), folder);
			return this.pluginsFolder;
		}

		public File getApplicationFolder() {
			// TODO: check if there is a better way to handle this
			return new File(System.getProperty("user.dir"));
		}
		
	    public File getInstallFolder() {
	        return new File(getApplicationFolder(), "install");
	    }
	}

	public interface ApplicationManager {
		public Version getVersion();
	}
	public static class ApplicationLocator {
		public static ApplicationManager getManager() {
			initialize();
			return new DefaultApplicationManager();
		}
	}
	public static class DefaultApplicationManager implements ApplicationManager {
		public Version getVersion() {
			return new Version();
		}
	}

}
