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

package org.gvsig.installer.app.extension.creation;

import org.gvsig.installer.app.extension.BackportrHelper.ApplicationLocator;
import org.gvsig.installer.app.extension.BackportrHelper.PluginsLocator;
import org.gvsig.installer.app.extension.BackportrHelper.PluginsManager;
import org.gvsig.installer.swing.api.SwingInstallerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.Version;

/**
 * @author <a href="mailto:jpiera@gvsig.org">Jorge Piera Llodr&aacute;</a>
 */
public class MakePluginPackageExtension extends Extension {

	private static final Logger LOG = LoggerFactory
			.getLogger(MakePluginPackageExtension.class);

	public void execute(String actionCommand) {
		if ("tools-devel-pack-plugin".equalsIgnoreCase(actionCommand)) {
			PluginsManager manager = PluginsLocator.getManager();

			try {
				PluginServices.getMDIManager().addCentredWindow(
						new MakePluginPackageWindow(manager
								.getApplicationFolder(), manager
								.getInstallFolder()));
			} catch (Exception e) {
				LOG.error("Error creating teh wizard to create an installer ",
						e);
			}
		}
	}

	public void initialize() {

	}

	@Override
	public void postInitialize() {
		super.postInitialize();
		Version version = ApplicationLocator.getManager().getVersion();
		SwingInstallerLocator.getSwingInstallerManager().setApplicationVersion(
				version.getFormat());
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		return true;
	}

}
