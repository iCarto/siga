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
 
package org.gvsig.gui.beans.wizard.panel;

import javax.swing.JPanel;


/**
 * <p>
 * This interface has to be implemented by all the classes
 * that are a part of the wizard. 
 * </p>
 * @author <a href="mailto:jpiera@gvsig.org">Jorge Piera Llodr&aacute;</a>
 */
public interface OptionPanel {
	/**
	 * Returns a title for the panel.
	 * @return
	 * The panel title.
	 */
	public String getPanelTitle();

	/**
	 * This method is called when the next button is clicked
	 */
	public void nextPanel() throws NotContinueWizardException;

	/**
	 * This method is called when the last button is clicked
	 */
	public void lastPanel();
	
	/**
	 * This method is called when the panel is displayed
	 */
	public void updatePanel();
		
	/**
	 * Return the panel to display.
	 * @return
	 * The panel.
	 */
	public JPanel getJPanel();
}

