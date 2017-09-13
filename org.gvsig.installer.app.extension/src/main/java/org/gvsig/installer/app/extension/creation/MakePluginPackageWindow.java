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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JPanel;

import org.gvsig.installer.app.extension.utils.WindowInstallerListener;
import org.gvsig.installer.swing.api.SwingInstallerLocator;
import org.gvsig.installer.swing.api.creation.MakePluginPackageWizard;
import org.gvsig.installer.swing.api.creation.MakePluginPackageWizardException;
import org.gvsig.tools.locator.LocatorException;

import org.gvsig.i18n.Messages;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

/**
 * @author <a href="mailto:jpiera@gvsig.org">Jorge Piera Llodr&aacute;</a>
 */
public class MakePluginPackageWindow extends JPanel implements IWindow {

	private static final long serialVersionUID = 3423389124323013058L;
	WindowInfo windowInfo = null;

	public MakePluginPackageWindow(File applicationFolder, File installFolder)
			throws LocatorException, MakePluginPackageWizardException {
		super();
		MakePluginPackageWizard makePluginPackageWizard = SwingInstallerLocator
				.getSwingInstallerManager().createMakePluginPackageWizard(
						applicationFolder, installFolder);
		makePluginPackageWizard
				.setWizardActionListener(new WindowInstallerListener(this));
		this.setLayout(new BorderLayout());
		add(makePluginPackageWizard, BorderLayout.CENTER);
	}

	public WindowInfo getWindowInfo() {
		if (windowInfo == null) {
			windowInfo = new WindowInfo(WindowInfo.MODELESSDIALOG
					| WindowInfo.ICONIFIABLE | WindowInfo.RESIZABLE);
			Dimension dim = getPreferredSize();
			windowInfo.setWidth((int) dim.getWidth());
			windowInfo.setHeight((int) dim.getHeight());
			windowInfo.setTitle(Messages.getText("make_plugin_package"));
		}
		return windowInfo;
	}

	public Object getWindowProfile() {
		return WindowInfo.DIALOG_PROFILE;
	}
	
}
