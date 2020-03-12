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
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
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
package com.iver.core.mdiManager.frames;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;
import javax.swing.JDialog;

/**
 * @author Cesar Martinez Izquierdo <cesar.martinez@iver.es>
 *
 */
public class ExternalFrame extends JDialog implements IFrame, ComponentListener {
	private static final long serialVersionUID = 1L;
	
	private void initFrame() {
		addComponentListener(this);
	}
	


    /**
     * Creates a non-modal dialog without a title with the
     * specified <code>Frame</code> as its owner.  If <code>owner</code>
     * is <code>null</code>, a shared, hidden frame will be set as the
     * owner of the dialog.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     */
    public ExternalFrame(Frame owner) throws HeadlessException {
    	super(owner);
    	initFrame();
    }




	/**
	 *  Stores the minimum allowed size for this JDialog. If it is null, there
	 *  is no minimum size for this window.
	 */
	Dimension minimumSize = null;
	

	public void setHeight(int height) {
		super.setSize(getWidth(), height);
	}


	public void setWidth(int width) {
		super.setSize(width, getHeight());
	}


	public void setX(int x) {
		super.setLocation(x, getX());
	}


	public void setY(int y) {
		super.setLocation(y, getY());
	}


	public Dimension getMinimumSize() {
		return minimumSize;
	}


	public void setMinimumSize(Dimension minSize) {
		minimumSize = minSize;
		adjustToMinSize();
	}
	
	/**
	 * Adjust the window to the minimum size, if necessary.
	 */
	private void adjustToMinSize() {
		if (minimumSize==null) return;
		int height, width;
		Dimension currentSize = getSize();
		boolean modified=false;
		
		if (currentSize.height < minimumSize.height) {
			height = minimumSize.height;
			modified = true;
		}
		else {
			height = currentSize.height;
		}
		
		if (currentSize.width < minimumSize.width) {
			width = minimumSize.width;
			modified = true;
		}
		else {
			width = currentSize.width;
		}
		if (modified) {
			boolean isResizable = isResizable();
			setResizable(false);
			setSize(width, height);
			setResizable(isResizable);
			setVisible(true);
		}
	}

	public void componentHidden(ComponentEvent e) {		
	}

	public void componentMoved(ComponentEvent e) {		
	}

	public void componentResized(ComponentEvent e) {
		// this is necessary because in Java 1.4/1.5, JDialog doesn't have a
		// setMinimum method. It's solved in 1.6, we'll remove this after migration.
		adjustToMinSize();
		
	}

	public void componentShown(ComponentEvent e) {		
	}

}
