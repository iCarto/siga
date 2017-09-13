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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JPanel;

import org.gvsig.i18n.Messages;
import org.gvsig.installer.app.extension.utils.WindowInstallerListener;
import org.gvsig.installer.swing.api.SwingInstallerLocator;
import org.gvsig.installer.swing.api.execution.AbstractInstallPackageWizard;
import org.gvsig.installer.swing.api.execution.InstallPackageWizardException;
import org.gvsig.tools.locator.LocatorException;

import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

/**
 * @author <a href="mailto:jpiera@gvsig.org">Jorge Piera Llodr&aacute;</a>
 */
public class InstallPackageWindow extends JPanel implements IWindow {


	private static final long serialVersionUID = 4719868181091291809L;
	WindowInfo windowInfo = null;

	public InstallPackageWindow(File applicationFolder, File installFolder)
			throws LocatorException, InstallPackageWizardException {
		super();
		AbstractInstallPackageWizard installPackageWizard = SwingInstallerLocator
				.getSwingInstallerManager().createInstallPackageWizard(
						applicationFolder, installFolder);
		installPackageWizard
				.setWizardActionListener(new WindowInstallerListener(this));
		this.setLayout(new BorderLayout());
		add(installPackageWizard, BorderLayout.CENTER);
	}

	public WindowInfo getWindowInfo() {
		if (windowInfo == null) {
			windowInfo = new WindowInfo(WindowInfo.MODELESSDIALOG
					| WindowInfo.ICONIFIABLE | WindowInfo.RESIZABLE);
			Dimension dim = getPreferredSize();
			windowInfo.setWidth((int) dim.getWidth());
			windowInfo.setHeight((int) dim.getHeight());
			windowInfo.setTitle(Messages.getText("install_package"));
		}
		return windowInfo;
	}

	public Object getWindowProfile() {
		return WindowInfo.DIALOG_PROFILE;
	}
}
