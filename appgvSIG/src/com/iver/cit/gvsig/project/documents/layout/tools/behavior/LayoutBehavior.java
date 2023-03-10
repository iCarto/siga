/*
 * Created on 28-oct-2004
 */
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
package com.iver.cit.gvsig.project.documents.layout.tools.behavior;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.VolatileImage;

import com.iver.cit.gvsig.fmap.tools.BehaviorException;
import com.iver.cit.gvsig.project.documents.layout.LayoutControl;
import com.iver.cit.gvsig.project.documents.layout.fframes.IFFrame;
import com.iver.cit.gvsig.project.documents.layout.tools.listener.LayoutToolListener;


/**
 * Ejecuta acciones respondiendo a eventos, por
 * delegaci?n desde el Layout.
 *
 * @author Vicente Caballero Navarro
 */
public abstract class LayoutBehavior implements ILayoutBehavior {
	private LayoutControl layout;
	public abstract LayoutToolListener getListener();

	public void paintComponent(Graphics g) {
//		g.drawImage(getLayoutControl().getImage(),0,
//                0, layout);

//		getLayoutControl().getLayoutDraw().drawGrid((Graphics2D) g);
//        getLayoutControl().getLayoutDraw().drawRuler((Graphics2D) g, Color.black);
//		getLayoutControl().getGeometryAdapter().paint((Graphics2D) g, layout.getAT(), true);
//		getLayoutControl().getLayoutDraw().drawHandlers((Graphics2D) g, Color.black);
//		long t1 = System.currentTimeMillis();

		VolatileImage image = createVolatileImage();
		Graphics gh = image.createGraphics();
		gh.setColor(getLayoutControl().getBackground());
		gh.fillRect(0, 0, image.getWidth(), image.getHeight());

		getLayoutControl().getLayoutDraw().drawRectangle((Graphics2D) gh);
		gh.drawImage(getLayoutControl().getImage(), 0,0, null);
		gh.drawImage(getLayoutControl().getImgRuler(), 0,0, null);
//		long t2 = System.currentTimeMillis();

		getLayoutControl().getGeometryAdapter().paint((Graphics2D) gh, layout.getAT(), true);
		getLayoutControl().getLayoutDraw().drawHandlers((Graphics2D) gh, Color.black);
//		getLayoutControl().drawCursor(gh);
		g.drawImage(image, 0, 0, null);
//		long t3 = System.currentTimeMillis();
//		System.out.println("t1 = " + (t2-t1) + " t2=" + (t3-t2));

	}

	public void setLayoutControl(LayoutControl lc) {
		layout = lc;
	}

	public Image getImageCursor() {
		return getListener().getImageCursor();
	}
	public Cursor getCursor() {
		return getListener().getCursor();
	}

	public LayoutControl getLayoutControl() {
		return layout;
	}

	public void mouseClicked(MouseEvent e) throws BehaviorException {
	}

	public void mouseEntered(MouseEvent e) throws BehaviorException {
	}

	public void mouseExited(MouseEvent e) throws BehaviorException {
	}

	public void mousePressed(MouseEvent e) throws BehaviorException {
		 if (e.getButton() == MouseEvent.BUTTON1) {
			    layout.setPointAnt();
	            layout.setFirstPoint();
		 }
	}

	public void mouseReleased(MouseEvent e) throws BehaviorException {
		if (e.getButton() != MouseEvent.BUTTON3) {
			layout.setLastPoint();
			layout.setPointAnt();
		}
	}

	public void mouseDragged(MouseEvent e) throws BehaviorException {
		if (e.getButton() != MouseEvent.BUTTON3) {
			layout.setLastPoint();
		}
	}

	public void mouseMoved(MouseEvent e) throws BehaviorException {
	}

	public void mouseWheelMoved(MouseWheelEvent e) throws BehaviorException {
	}

	public boolean isAdjustable() {
		return false;
	}

	protected VolatileImage createVolatileImage() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
		VolatileImage image = null;
		int transparency = getLayoutControl().getImage().getTransparency();
		int width = getLayoutControl().getWidth();
		int height = getLayoutControl().getHeight();
		image = gc.createCompatibleVolatileImage(width, height, transparency);

		int valid = image.validate(gc);

		if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
			image = this.createVolatileImage();
			return image;
		}

		return image;
	}
}
