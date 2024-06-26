/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
 *   Av. Blasco Ib��ez, 50
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
package com.iver.cit.gvsig.project.documents.layout.tools;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.AddLayer;
import com.iver.cit.gvsig.fmap.core.adapter.GeometryAdapter;
import com.iver.cit.gvsig.fmap.core.adapter.RectangleAdapter;
import com.iver.cit.gvsig.fmap.tools.Events.PointEvent;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameGraphics;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameGraphicsFactory;
import com.iver.cit.gvsig.project.documents.layout.fframes.FrameFactory;
import com.iver.cit.gvsig.project.documents.layout.gui.Layout;
import com.iver.cit.gvsig.project.documents.layout.tools.listener.ILayoutGraphicListener;
import com.iver.cit.gvsig.project.documents.layout.tools.listener.LayoutPointListener;


/**
 * Implementaci�n de la interfaz LayoutPointListener como herramienta para realizar
 * un rect�ngulo.
 *
 * @author Vicente Caballero Navarro
 */
public class LayoutAddRectangleListenerImpl implements LayoutPointListener, ILayoutGraphicListener {
//	 public static final Image iRectangle = new ImageIcon(AddLayer.class
//	            .getClassLoader().getResource("images/RectangleCursor.png"))
//	            .getImage();
	public static final Image iRectangle = PluginServices.getIconTheme()
		.get("rectangle-cursor").getImage();
	private final Cursor cur = Toolkit.getDefaultToolkit().createCustomCursor(iRectangle,
			new Point(16, 16), "");

	private Layout layout;

	/**
	 * Crea un nuevo LayoutAddRectagleListenerImpl.
	 *
	 * @param l Layout.
	 */
	public LayoutAddRectangleListenerImpl(Layout l) {
		this.layout = l;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.tools.Listeners.PointListener#point(com.iver.cit.gvsig.fmap.tools.PointEvent)
	 */
	public void point(PointEvent event) {
		if (event.getEvent().getButton() == MouseEvent.BUTTON1) {
			if (event.getEvent().getButton() == MouseEvent.BUTTON1) {
				if (layout.getLayoutControl().addGeometryAdapterPoint() == 2) {
					endGraphic();
	            }
			}
		}
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.tools.Listeners.ToolListener#getCursor()
	 */
	public Image getImageCursor() {
		return iRectangle;
	}
	public Cursor getCursor(){
		return cur;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.tools.Listeners.ToolListener#cancelDrawing()
	 */
	public boolean cancelDrawing() {
	    System.out.println("cancelDrawing del ZoomOutListenerImpl");
		return true;
	}

	public void pointDoubleClick(PointEvent event) {
	}

	public void endGraphic() {
		layout.getLayoutControl().getGeometryAdapter().end();
        PluginServices.getMainFrame().enableControls();
        FFrameGraphics fframe =(FFrameGraphics)FrameFactory.createFrameFromName(FFrameGraphicsFactory.registerName);

		fframe.setLayout(layout);
        fframe.setGeometryAdapter(layout.getLayoutControl().getGeometryAdapter());
		fframe.update(FFrameGraphics.RECTANGLE, layout.getLayoutControl().getAT());
		fframe.setBoundBox(layout.getLayoutControl().getGeometryAdapter().getBounds2D());
		layout.getLayoutContext().addFFrame(fframe, true,true);
		layout.getLayoutControl().setGeometryAdapter(new RectangleAdapter());
        layout.getLayoutControl().refresh();
	}

	public GeometryAdapter createGeometryAdapter() {
		return new RectangleAdapter();
	}
}
