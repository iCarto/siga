package org.gvsig.gpe.gui.dialogs;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFrame;

import org.gvsig.gpe.GPERegister;

import com.iver.andami.messages.NotificationManager;

/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
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
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
/* CVS MESSAGES:
 *
 * $Id$
 * $Log$
 *
 */
/**
 * @author Jorge Piera LLodr? (jorge.piera@iver.es)
 */
public class SelectVersionPanelTest {
	private static String writersFile = "config" + File.separatorChar + "writer.properties";
	
	public static void main(String[] args) {
		loadWriters();
		SelectVersionWindow panel = new SelectVersionWindow();
		createFrame(panel);
	}	

	/**
	 * Load the GPE writers from a file
	 */
	private static  void loadWriters(){
		File file = new File(writersFile);
		if (!file.exists()){
			NotificationManager.addWarning("File not found",
					new FileNotFoundException());
			return;
		}
		try {
			GPERegister.addWritersFile(file);
		} catch (Exception e) {
			NotificationManager.addWarning("GPE writers file not found",
					new FileNotFoundException());
		}
	}

	private static void createFrame(Component component){
		JFrame f = new JFrame();
		f.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				System.exit(0);
			}
		});
		f.getContentPane().add(component);	    
		f.setBounds(0,0,300,400);
		f.setVisible(true);
	}
}

