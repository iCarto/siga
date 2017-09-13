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
package org.gvsig.georeferencing.ui.zoom;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.gvsig.fmap.raster.layers.FLyrRasterSE;
import org.gvsig.georeferencing.ui.zoom.layers.GCPsGraphicLayer;
import org.gvsig.georeferencing.view.BaseZoomView;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.DefaultMapContextDrawer;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.operations.Cancel;
import com.iver.utiles.swing.threads.Cancellable;

/**
 * Gestor de peticiones de zoom sobre el panel con el raster a georreferenciar.
 * Implementa el interfaz IExtensionRequest para que este comunique la peticiones
 * de zoom y ViewRasterRequestManager pueda hacer las peticiones de datos a la capa.
 *
 * 30/12/2007
 * @author Nacho Brodin (nachobrodin@gmail.com)
 */
public class ViewMapRequestManager implements IExtensionRequest {
	private FLayers            lyrs              = null;
	private BaseZoomView       view              = null;
	private MapControl         mapControl        = null;
	private GCPsGraphicLayer   graphicLayer      = null;
	private Color              backGroundColor   = null;
	private FLyrRasterSE       testLayer         = null;

	/**
	 * Asigna la capa a georreferenciar de donde se obtienen los datos.
	 * @param lyr
	 */
	public ViewMapRequestManager(BaseZoomView view, MapControl mapControl) {
		this.mapControl = mapControl;
		this.lyrs = mapControl.getMapContext().getLayers();
		this.view = view;
	}

	/**
	 * Añade una capa raster a la lista de capas
	 * @param lyr
	 * @throws InvalidRequestException
	 */
	public void addTestRasterLayer(FLyrRasterSE lyr) throws InvalidRequestException {
		if(lyrs == null || lyr == null)
			return;
		testLayer = lyr;
		lyrs.addLayer(lyr);
		view.getCanvas().setForceRequest(true);
		fullExtent();
	}

	/**
	 * Elimina la capa de test de la vista de mapa
	 * @throws InvalidRequestException
	 */
	public void removeTestRasterLayer() throws InvalidRequestException {
		if(testLayer != null){
			lyrs.removeLayer(testLayer);
			PluginServices.getMainFrame().enableControls();
		}
		view.getCanvas().setForceRequest(true);
		fullExtent();
	}

	/**
	 * Asigna la capa de puntos de control
	 * @param gl
	 */
	public void setGCPsGraphicLayer(GCPsGraphicLayer gl) {
		this.graphicLayer = gl;
	}

	/**
	 * Calcula la extensión que contendrá la vista a partir del extent
	 * máximo de la capa/s que contienen . Tienen en cuenta las distintan proporciones
	 * entre vista y petición
	 * @param Rectangle2D
	 */
	public Rectangle2D initRequest(Rectangle2D extent) throws InvalidRequestException {
		double x = extent.getX();
		double y = extent.getY();
		double w = extent.getWidth();
		double h = extent.getHeight();
		//Calculamos la extensión de la vista para el extent máximo que va a contener
		//teniendo en cuenta las proporciones de ambos.
		if(extent.getWidth() < extent.getHeight()) {
			if(((double)view.getCanvasWidth() / (double)view.getCanvasHeight()) <= (extent.getWidth() / extent.getHeight())) {
				h = (view.getCanvasHeight() * w) / view.getCanvasWidth();
				y = extent.getCenterY() - (h / 2);
			} else { //p1 < p2
				w = (view.getCanvasWidth() * h) / view.getCanvasHeight();
				x = extent.getCenterX() - (w / 2);
			}
		} else {
			if(((double)view.getCanvasWidth() / (double)view.getCanvasHeight()) >= (extent.getWidth() / extent.getHeight())) {
				w = (view.getCanvasWidth() * h) / view.getCanvasHeight();
				x = extent.getCenterX() - (w / 2);
			} else { //p1 < p2
				h = (view.getCanvasHeight() * w) / view.getCanvasWidth();
				y = extent.getCenterY() - (h / 2);
			}
		}
		Rectangle2D r = new Rectangle2D.Double(x, y, w, h);
		setDrawParams(null, r);
		request(r);
		return r;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.rastertools.georeferencing.ui.zoom.IExtensionRequest#request(java.awt.geom.Rectangle2D)
	 */
	public Rectangle2D request(Rectangle2D extent) throws InvalidRequestException {
		if(extent == null)
			return lyrs.getFullExtent();

		//Obtenemos el viewport y calculamos la matriz de transformación
		ViewPort vp = new ViewPort(null);
		vp.setImageSize(new Dimension(view.getCanvasWidth(), view.getCanvasHeight()));
		vp.setExtent(extent);
		vp.setProjection(mapControl.getMapContext().getProjection());
		try {
			//Dibujamos a través del render de la capa en un graphics como el de la vista
			BufferedImage initImg = new BufferedImage(view.getCanvasWidth(), view.getCanvasHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D img = ((Graphics2D)initImg.getGraphics());
			if(backGroundColor != null && backGroundColor != Color.BLACK) {
				img.setColor(backGroundColor);
				img.fillRect(0, 0, view.getCanvasWidth(), view.getCanvasHeight());
			}

			DefaultMapContextDrawer mapContextDrawer = new DefaultMapContextDrawer();
			mapContextDrawer.setMapContext(lyrs.getMapContext());
			mapContextDrawer.setViewPort(vp);
			Cancel myCancel = new Cancel();
			mapContextDrawer.draw(lyrs, initImg, img, myCancel, mapControl.getMapContext().getScaleView());
			//lyrs.draw(initImg, img, vp, new CancellableClass(), mapControl.getMapContext().getScaleView());

			setDrawParams(initImg, extent);

			if(graphicLayer != null)
				graphicLayer.recalcMapDrawCoordinates();

		} catch (Exception e) {
			throw new InvalidRequestException("Error en al acceso al fichero");
		}
		return extent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.rastertools.georeferencing.ui.zoom.IExtensionRequest#fullExtent(java.awt.Dimension)
	 */
	public void fullExtent() throws InvalidRequestException  {
		this.initRequest(lyrs.getFullExtent());
	}

	/**
	 * Asigna los parámetros para el control de zoom del mapa
	 * @param img BufferedImage
	 * @param vp ViewPort
	 */
	public void setDrawParams(BufferedImage img, Rectangle2D extBuf) {
		if(view != null && lyrs != null) {
			if(img != null)
				view.setDrawParams(img, extBuf, extBuf.getWidth()/img.getWidth(), new Point2D.Double(extBuf.getCenterX(), extBuf.getCenterY()));
			else
				view.setDrawParams(img, extBuf, extBuf.getWidth()/view.getCanvasWidth(), new Point2D.Double(extBuf.getCenterX(), extBuf.getCenterY()));
		}
	}

	/**
	 * Obtiene el color de fondo
	 * @return
	 */
	public Color getBackGroundColor() {
		return backGroundColor;
	}

	/**
	 * Asigna el color de fondo
	 * @param backGroundColor
	 */
	public void setBackGroundColor(Color backGroundColor) {
		this.backGroundColor = backGroundColor;
	}

}
