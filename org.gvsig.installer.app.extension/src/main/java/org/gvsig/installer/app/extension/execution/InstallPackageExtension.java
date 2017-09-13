/* gvSIG. Geographic Information System of the Valencian Government
 *
 * Copyright (C) 2007-2008 Infrastructures and Transports Department
 * of the Valencian Government (CIT)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 */

/*
 * AUTHORS (In addition to CIT):
 * 2010 {Prodevelop}   {Task}
 */

package org.gvsig.installer.app.extension.execution;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.gvsig.installer.app.extension.BackportrHelper.ApplicationLocator;
import org.gvsig.installer.app.extension.BackportrHelper.PluginsLocator;
import org.gvsig.installer.app.extension.BackportrHelper.PluginsManager;
import org.gvsig.installer.swing.api.SwingInstallerLocator;
import org.gvsig.installer.swing.api.SwingInstallerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.Version;

/**
 * @author <a href="mailto:jpiera@gvsig.org">Jorge Piera Llodr&aacute;</a>
 */
public class InstallPackageExtension extends Extension {

	private static final Logger LOG = LoggerFactory
			.getLogger(InstallPackageExtension.class);

	public void execute(String actionCommand) {
		if ("tools-addonsmanager".equalsIgnoreCase(actionCommand)) {
			PluginsManager manager = PluginsLocator.getManager();
			try {
				PluginServices.getMDIManager().addCentredWindow(
						new InstallPackageWindow(
								manager.getApplicationFolder(), manager
										.getInstallFolder()));
			} catch (Error e) {
				LOG.error("Error creating the wizard to install a package ", e);
			} catch (Exception e) {
				LOG.error("Error creating the wizard to install a package ", e);
			}
		}
	}

	public void initialize() {
		Version version = ApplicationLocator.getManager().getVersion();

		try {
	    	PluginsLocator.getIconThemeManager().getCurrent().registerDefault(
					"tools-addonsmanager",
					this.getClass().getClassLoader().getResource("images/action/tools-addonsmanager.png")
				);
	    	
			SwingInstallerManager manager = SwingInstallerLocator
					.getSwingInstallerManager();
			InputStream is = this.getClass().getResourceAsStream(
					"/defaultDownloadsURLs");
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			for (line = in.readLine(); line != null; line = in.readLine()) {
				line = line.replace("$version", version.getFormat());
				line = line.replace("<%Version%>", version.getFormat());
				try {
					manager.addDefaultDownloadURL(new URL(line));
				} catch (MalformedURLException e) {
					LOG.error(
							"Error creating the default packages download URL pointing to "
									+ line, e);
				}
			}
			manager.getInstallerManager().setVersion(version.getFormat());
		} catch (Throwable e) {
			LOG.error("Error reading the default packages download URL file "
					+ "/defaultDownloadsURLs", e);
		}
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		return true;
	}

    
 
    
}
