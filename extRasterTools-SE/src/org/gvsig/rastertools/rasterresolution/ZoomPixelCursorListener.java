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
package org.gvsig.rastertools.rasterresolution;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.gvsig.fmap.raster.layers.FLyrRasterSE;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.tools.BehaviorException;
import com.iver.cit.gvsig.fmap.tools.Events.PointEvent;
import com.iver.cit.gvsig.fmap.tools.Listeners.PointListener;


/**
 * Listener para la tool de zoom a la resolución del raster. Este es el encargado de recibir
 * la petición desde la vista y calcular las coordenadas del raster.
 *
 * @author Nacho Brodin (nachobrodin@gmail.com)
 */
public class ZoomPixelCursorListener implements PointListener {
	private final Image 	img = new ImageIcon(MapControl.class.getResource(
											"images/ZoomPixelCursor.gif")).getImage();
	private Cursor 			cur = Toolkit.getDefaultToolkit().createCustomCursor(	img,
																					new Point(16, 16), "");
	private MapControl 		mapCtrl;


	/**
	 * Constructor. Asigna el MapControl
	 * @param mc
	 */
	public ZoomPixelCursorListener(MapControl mc) {
		this.mapCtrl = mc;
	}

	/**
	 * Evento de selección de punto sobre la vista. Aquin se calcularán las coordenadas
	 * y dimensiones de la petición centrando la petición en el punto pinchado en la vista.
	 */
	public void point(PointEvent event) throws BehaviorException {
		Point2D pReal = mapCtrl.getMapContext().getViewPort().toMapPoint(event.getPoint());
		//Point imagePoint = new Point((int) event.getPoint().getX(), (int) event.getPoint().getY());
		ViewPort v = mapCtrl.getMapContext().getViewPort();

		FLayer[] actives = mapCtrl.getMapContext().getLayers().getActives();
		Rectangle2D ext = null;
		try {
			ext = actives[0].getFullExtent();
		} catch (ReadDriverException e1) {
			NotificationManager.addError("Error al obtener el extent", e1);
		}

		ArrayList attr = ((FLyrRasterSE)actives[0]).getAttributes();
		int width = 0, height = 0;
		for (int i=0; i<attr.size(); i++) {
			Object[] a = (Object []) attr.get(i);
			if (a[0].toString().equals("Width"))
				width = ((Integer)a[1]).intValue();
			if (a[0].toString().equals("Height"))
				height = ((Integer)a[1]).intValue();
		}


		if(	ext != null &&
				width != 0 &&
				height != 0){

			double wcOriginCenterX = pReal.getX();
			double wcOriginCenterY = pReal.getY();

			//Hallamos la relación entre el pixel y las WC a partir de la imagen de la capa
			double relacionPixelWcWidth =  (ext.getMaxX() - ext.getMinX())/width;
			double relacionPixelWcHeight = (ext.getMaxY() - ext.getMinY())/height;
			//double desplazamientoX = ext.getMinX();
			//double desplazamientoY = ext.getMinY();

			double wcOriginX = wcOriginCenterX - ((v.getImageWidth()*relacionPixelWcWidth)/2);
			double wcOriginY = wcOriginCenterY - ((v.getImageHeight()*relacionPixelWcHeight)/2);

			double wcDstMinX = wcOriginX;
			double wcDstMinY = wcOriginY;
			double wcDstMaxX = wcDstMinX + (v.getImageWidth()*relacionPixelWcWidth);
			double wcDstMaxY = wcDstMinY + (v.getImageHeight()*relacionPixelWcHeight);

			double wcDstWidth = wcDstMaxX - wcDstMinX;
			double wcDstHeight = wcDstMaxY - wcDstMinY;

			ext = new Rectangle2D.Double(wcDstMinX, wcDstMinY, wcDstWidth, wcDstHeight);
			mapCtrl.getMapContext().getViewPort().setExtent(ext);
		}
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.tools.Listeners.ToolListener#getCursor()
	 */
	public Cursor getCursor() {
		return cur;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.tools.Listeners.ToolListener#cancelDrawing()
	 */
	public boolean cancelDrawing() {
		return false;
	}

	public void pointDoubleClick(PointEvent event) throws BehaviorException {
		// TODO Auto-generated method stub

	}

}
