/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2007 IVER T.I. and Generalitat Valenciana.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 */

package com.iver.cit.gvsig.panelGroup;

import javax.swing.JFrame;

import org.gvsig.exceptions.BaseException;
import org.gvsig.gui.beans.buttonspanel.ButtonsPanel;
import org.gvsig.gui.beans.buttonspanel.ButtonsPanelEvent;
import org.gvsig.gui.beans.buttonspanel.ButtonsPanelListener;
import org.gvsig.gui.beans.panelGroup.PanelGroupManager;
import org.gvsig.gui.beans.panelGroup.treePanel.TreePanel;

import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.panelGroup.loaders.PanelGroupLoaderFromExtensionPoint;
import com.iver.cit.gvsig.panelGroup.samples.Samples_ExtensionPointsOfIPanels;

/**
 * <p>Tests {@link PanelGroupDialog PanelGroupDialog}.</p>
 * <p>Tests {@link PanelGroupManager PanelGroupManager}, {@link PanelGroupLoaderFromExtensionPoint PanelGroupLoaderFromExtensionPoint}, 
 *  {@link TreePanel TreePanel}, and a non resizable {@link PanelGroupDialog PanelGroupDialog}.</p>
 * 
 * @version 22/10/2007
 * @author Pablo Piqueras Bartolomé (pablo.piqueras@iver.es) 
 */
public class Test8ButtonsPanel {
	/**
	 * <p>Element for the interface.</p>
	 */
	private static JFrame jFrame;
	private static TreePanel panelGroup;

	/**
	 * <p>Test method for the Test8ButtonsPanel.</p>
	 * <p>Tests the G.U.I. when adds panels with different dimensions, to a resizable <code>PanelGroupDialog</code>.</p>
	 * 
	 * @param args optional arguments
	 */
	public static void main(String[] args) {
		try {
			jFrame = new JFrame();

			PanelGroupManager manager = PanelGroupManager.getManager();
			manager.registerPanelGroup(TreePanel.class);
			manager.setDefaultType(TreePanel.class);

			Samples_ExtensionPointsOfIPanels.loadSample();

			panelGroup = (TreePanel) manager.getPanelGroup(Samples_ExtensionPointsOfIPanels.REFERENCE1);
			
			PanelGroupLoaderFromExtensionPoint loader = new PanelGroupLoaderFromExtensionPoint(Samples_ExtensionPointsOfIPanels.EXTENSIONPOINT1_NAME);
			
			// Creates the IWindow
			PanelGroupDialog panelGroupDialog = new PanelGroupDialog(Samples_ExtensionPointsOfIPanels.REFERENCE1_NAME, "Panel with Buttons", 150, 150, (byte)WindowInfo.RESIZABLE, panelGroup);

			// Begin: Test the normal load
			panelGroupDialog.loadPanels(loader);
			// End: Test the normal load
			
			panelGroupDialog.addButtonPressedListener(new ButtonsPanelListener() {
				/*
				 * @see org.gvsig.gui.beans.buttonspanel.ButtonsPanelListener#actionButtonPressed(org.gvsig.gui.beans.buttonspanel.ButtonsPanelEvent)
				 */
				public void actionButtonPressed(ButtonsPanelEvent e) {
					switch (e.getButton()) {
						case ButtonsPanel.BUTTON_ACCEPT:
							System.out.println("Accept Button pressed.");
							hideJFrame();
							System.exit(0);
							break;
						case ButtonsPanel.BUTTON_CANCEL:
							System.out.println("Cancel Button pressed.");
							hideJFrame();
							System.exit(0);
							break;
						case ButtonsPanel.BUTTON_APPLY:
							System.out.println("Apply Button pressed.");
							break;
					}
				}
			});
			
			jFrame.setTitle("Test non resizable PanelGroupDialog with a tree and using PanelGroupLoaderFromExtensionPoint");
		    jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    jFrame.setSize(panelGroupDialog.getPreferredSize());
		    jFrame.getContentPane().add(panelGroupDialog);
		    
		    jFrame.setResizable(false);
			jFrame.setVisible(true);
		} catch (BaseException bE) {
			System.out.println(bE.getLocalizedMessageStack());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>Hides the {@link JFrame JFrame}, hiding the graphical interface.</p>
	 */
	private static void hideJFrame() {
		jFrame.setVisible(false);
	}
}
