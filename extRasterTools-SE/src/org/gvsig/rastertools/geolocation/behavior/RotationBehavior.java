/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2006 IVER T.I. and Generalitat Valenciana.
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
package org.gvsig.rastertools.geolocation.behavior;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;

import org.gvsig.raster.datastruct.Extent;
import org.gvsig.raster.util.RasterToolsUtil;

import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.tools.BehaviorException;
import com.iver.cit.gvsig.fmap.tools.Listeners.RectangleListener;
import com.iver.cit.gvsig.fmap.tools.Listeners.ToolListener;


/**
 * Comportamiento que se aplica a la herramienta de rotation. El cursor del rat?n cambiar? cuando
 * se encuentre en las esquinas y en la zona exterior al raster, permitiendo el rotar la imagen al pinchar y 
 * arrastrar.
 * 
 * Nacho Brodin (nachobrodin@gmail.com)
 *
 */
public class RotationBehavior extends TransformationBehavior {
	//N?mero de pixeles de ancho del borde donde el cursor se activar?. Son pixeles del canvas de la vista.
	//De esta forma da igual la escala a la que est? la imagen siempre tiene la misma precisi?n
	private int 							PX_SELEC_BASE = 12;
	private int 							LONG_CORNER_BASE = 18;
	private int 							PX_SELEC = PX_SELEC_BASE;
	private int 							LONG_CORNER = LONG_CORNER_BASE;

	private Color							rectangleColor = Color.RED;
	
	private RectangleListener listener;
	
	private final Image rotImg = new ImageIcon(getClass().getClassLoader().getResource(
		"images/Rotar.gif")).getImage(); 

	/**
	 * Puntos de inicio y final para el arrastre de la imagen.
	 */
	private Point2D 						beforePoint = null;
	private Point2D 						nextPoint = null;
	
	/**
	 * Variable que si est? a true permite que se pinte el marco de la imagen. Se activa al
	 * comenzar el redimensionado y se desactiva al terminar
	 */
	private boolean 						isRotable = false;
	private AffineTransform                 boxRot = null;
	/**
	 * Valor de la rotaci?n.
	 */
	private double                          rotation = 0;
	
	/**
	 * Lista de flags de redimensionado para cada lado del raster
	 * [0]-esquina superior derecha
	 * [1]-esquina superior izquierda
	 * [2]-esquina inferior derecha
	 * [3]-esquina inferior izquierda
	 */
	private boolean[]						cornerActive = {false, false, false, false};
	private boolean 						init = false;
	/**
	 * Indica cual ser? la velocidad de rotaci?n
	 */
	private double							incrRot = 0.005;	
		
	/**
	 * Crea un nuevo RectangleBehavior.
	 *
	 * @param zili listener.
	 */
	public RotationBehavior(GeoRasterBehavior grb, Cursor cur, ITransformIO windowIO) {
		grBehavior = grb;
		defaultCursor = cur;
		this.trIO = windowIO;
	}

	/**
	 * Inicializaci?n. Calcula la longitud de la esquina y el ancho en p?xeles
	 * de la zona de detecci?n del puntero de rat?n. Inicialmente esto est? calculado en 
	 * unidades pixel de la vista pero la detecci?n de puntero, debido a la rotaci?n posible
	 * del raster ha de hacerse en unidades del raster en disco por ello ha de calcularse
	 * una escala entre ambas unidades cuando se carga la aplicaci?n.
	 *
	 */
	private void init() {
		ViewPort vp = grBehavior.getMapControl().getMapContext().getViewPort();		
		lyr = grBehavior.getLayer();
		if(lyr == null) 
			return ;
		
		Extent ext = lyr.getDataSource().getExtent();
		AffineTransform atImg = lyr.getAffineTransform();
				
		//Establecer una escala entre las coordenadas de la vista y las coordenadas pixel
		Point2D ul = new Point2D.Double(ext.getULX(), ext.getULY());
		Point2D lr = new Point2D.Double(ext.getLRX(), ext.getLRY());
		Point2D ulPx = new Point2D.Double();
		Point2D lrPx = new Point2D.Double();
		Point2D ulVp = new Point2D.Double();
		Point2D lrVp = new Point2D.Double();
		double esc = 1;
		try {
			atImg.inverseTransform(ul, ulPx);
			atImg.inverseTransform(lr, lrPx);
			ulVp = vp.fromMapPoint(ul);
			lrVp = vp.fromMapPoint(lr);
			esc = Math.abs(lrPx.getX() - ulPx.getX()) / Math.abs(lrVp.getX() - ulVp.getX());
		} catch (NoninvertibleTransformException e1) {
			return;
		}
		
		//Escalar PX_SELEC y LONG_CORNER para tenerlo en coordenadas pixel
		
		PX_SELEC = (int)(PX_SELEC_BASE * esc);
		LONG_CORNER = (int)(LONG_CORNER_BASE * esc);
		init = true;
	}
	
	/**
	 * Cuando se produce un evento de pintado dibujamos el marco de la imagen para
	 * que el usuario pueda seleccionar y redimensionar.
	 */
	public void paintComponent(Graphics g) {
		if(lyr == null)
			lyr = grBehavior.getLayer();
		
		if(isRotable && lyr != null) {			
			try {
				ViewPort vp = grBehavior.getMapControl().getMapContext().getViewPort();
				AffineTransform at = new AffineTransform(lyr.getAffineTransform());
				Extent ext = lyr.getFullRasterExtent();
				Point2D center = new Point2D.Double(lyr.getPxWidth() / 2, lyr.getPxHeight()/ 2);
				at.transform(center, center);
						
				Point2D ul = new Point2D.Double(ext.getULX(), ext.getULY());
				Point2D lr = new Point2D.Double(ext.getLRX(), ext.getLRY());
								
				AffineTransform T1 = new AffineTransform(1, 0, 0, 1, -center.getX(), -center.getY());
				AffineTransform R1 = new AffineTransform();
				R1.setToRotation(rotation);
				AffineTransform T2 = new AffineTransform(1, 0, 0, 1, center.getX(), center.getY());
				T2.concatenate(R1);
				T2.concatenate(T1);
								
				T2.transform(ul, ul);
				T2.transform(lr, lr);
				
				at.preConcatenate(T2);
				boxRot = new AffineTransform(at);
				at.preConcatenate(vp.getAffineTransform());
												
				vp.getAffineTransform().transform(ul, ul);
				at.inverseTransform(ul, ul);
				
				vp.getAffineTransform().transform(lr, lr);
				at.inverseTransform(lr, lr);
				
				((Graphics2D)g).transform(at);
				g.setColor(rectangleColor);
				((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
				g.fillRect((int)ul.getX(), (int)ul.getY(), (int)lr.getX(), (int)lr.getY());
				((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
				g.drawRect((int)ul.getX(), (int)ul.getY(), (int)lr.getX(), (int)lr.getY());
				((Graphics2D)g).transform(at.createInverse());
			} catch (NoninvertibleTransformException e1) {
				RasterToolsUtil.messageBoxError("error_transformacion1", this, e1);
			}
		}		
	}

	/**
	 * Reimplementaci?n del m?todo mousePressed de Behavior. Si no est? 
	 * activo el cursor por defecto capturamos el punto seleccionado en 
	 * coordenadas del mundo real.
	 *
	 * @param e MouseEvent
	 */
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			ViewPort vp = grBehavior.getMapControl().getMapContext().getViewPort();
			beforePoint = vp.toMapPoint(e.getPoint());
			if(lyr == null)
				lyr = grBehavior.getLayer();
			isRotable = true;
		}
	}

	/**
	 * Reimplementaci?n del m?todo mouseReleased de Behavior. Desactivamos
	 * los flags de redimensionado y a partir de la selecci?n del usuario 
	 * creamos un nuevo extent para la imagen. Con este extent creamos una nueva
	 * capa que sustituir? a la anterior.
	 *
	 * @param e MouseEvent
	 *
	 * @throws BehaviorException Excepci?n lanzada cuando el Behavior.
	 */
	public void mouseReleased(MouseEvent e) throws BehaviorException {
		if(e.getButton() == MouseEvent.BUTTON1) {
			if(boxRot != null)
				lyr.setAffineTransform(boxRot);
			rotation = 0;
			grBehavior.getMapControl().getMapContext().invalidate();
			isRotable = false;
			super.mouseReleased(e);
		}
	}
	
	/**
	 * Cuando arrastramos con el rat?n pulsado significa que estamos rotando la imagen. Para realizar la rotaci?n
	 * medimos la distancia al punto inicial (desde el pto que estamos al pto donde se picho la primera vez). Esta distancia
	 * nos da la velocidad de giro ya que si es grande es que lo habremos movido deprisa y si es peque?a habremos arrastrado
	 * el rat?n despacio. Creamos una matriz de transformaci?n con la identidad y la rotaci?n que hemos medido aplicada. Al
	 * final repintamos para que se pueda usar esta rotaci?n calculada en el repintado. 
	 */
	public void mouseDragged(MouseEvent e) {
		ViewPort vp = grBehavior.getMapControl().getMapContext().getViewPort();
		nextPoint = vp.toMapPoint(e.getPoint());			
		
		//Para la esquinas derechas cuando subimos el puntero giramos a izquierdas
		//Para la esquinas izquierdas cuando subimos el puntero giramos a derechas
		if( ((cornerActive[0] || cornerActive[2]) && (beforePoint.getY() < nextPoint.getY())) ||
			((cornerActive[1] || cornerActive[3]) && (beforePoint.getY() >= nextPoint.getY())))
			rotation += incrRot;
		else
			rotation -= incrRot;
						
		beforePoint = vp.toMapPoint(e.getPoint());
		if(boxRot != null)
			trIO.loadTransform(boxRot);
		grBehavior.getMapControl().repaint();
	}
	
	/**
	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#setListener(org.gvsig.georeferencing.fmap.tools.ToolListener)
	 */
	public void setListener(ToolListener listener) {
		this.listener = (RectangleListener) listener;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#getListener()
	 */
	public ToolListener getListener() {
		return listener;
	}
	
	/**
	 * Cuando movemos el rat?n detecta si estamos en el marco de la 
	 * imagen y pone el icono del cursor del rat?n adecuado.
	 */
	public boolean mouseMoved(MouseEvent ev) throws BehaviorException {
		if(!init)
			init();
		
		ViewPort vp = grBehavior.getMapControl().getMapContext().getViewPort();
		resetBorderSelected();
		
		lyr = grBehavior.getLayer();
		if(lyr == null) {
			setActiveTool(false);
			return false;
		}
		
		AffineTransform atImg = lyr.getAffineTransform();
				
		//Pasar coordenadas del punto a coordenadas reales y luego a coordenadas pixel
		Point2D e = vp.toMapPoint(ev.getX(), ev.getY());
		try {
			atImg.inverseTransform(e, e);
		} catch (NoninvertibleTransformException e1) {
			return false;
		}
				
		//Comprobar si est? dentro del raster
		Point2D p1 = new Point2D.Double(0, 0);
		Point2D p2 = new Point2D.Double(lyr.getDataSource().getWidth(), lyr.getDataSource().getHeight());
		
		//esquina superior izquierda
		if ((e.getX() > (p1.getX() - PX_SELEC) && e.getX() <= (p1.getX() + LONG_CORNER) && e.getY() <= p1.getY() && e.getY() > (p1.getY() - PX_SELEC)) ||
			(e.getX() <= p1.getX() && e.getX() > (p1.getX() - PX_SELEC) && e.getY() > p1.getY() && e.getY() <= (p1.getY() + LONG_CORNER))) {
			setCursor(rotImg);
			setActiveTool(true);
			cornerActive[1] = true;
			return true;
		}
		
		//esquina superior derecha
		if ((e.getX() >= (p2.getX() - LONG_CORNER) && e.getX() < (p2.getX() + PX_SELEC) && e.getY() <= p1.getY() && e.getY() > (p1.getY() - PX_SELEC)) ||
			(e.getX() >= p2.getX() && e.getX() < (p2.getX() + PX_SELEC) && e.getY() > p1.getY() && e.getY() <= (p1.getY() + LONG_CORNER))) {
			setCursor(rotImg);
			setActiveTool(true);
			cornerActive[0] = true;
			return true;
		}
		
		//esquina inferior izquierda
		if ((e.getX() > (p1.getX() - PX_SELEC) && e.getX() <= (p1.getX() + LONG_CORNER) && e.getY() >= p2.getY() && e.getY() < (p2.getY() + PX_SELEC)) ||
			(e.getX() <= p1.getX() && e.getX() > (p1.getX() - PX_SELEC) && e.getY() < p2.getY() && e.getY() >= (p2.getY() - LONG_CORNER))) {
			setCursor(rotImg);
			setActiveTool(true);
			cornerActive[3] = true;
			return true;
		}
		
		//esquina inferior derecha
		if ((e.getX() < (p2.getX() + PX_SELEC) && e.getX() >= (p2.getX() - LONG_CORNER) && e.getY() >= p2.getY() && e.getY() < (p2.getY() + PX_SELEC)) ||
			(e.getX() >= p2.getX() && e.getX() <= (p2.getX() + PX_SELEC) && e.getY() < p2.getY() && e.getY() >= (p2.getY() - LONG_CORNER))){
			setCursor(rotImg);
			setActiveTool(true);
			cornerActive[2] = true;
			return true;
		}
		
		grBehavior.getMapControl().repaint();
		grBehavior.getMapControl().setCursor(defaultCursor);
		return false;
	}
	
	/**
	 * Pone a false todos los elementos del array sideActive. Esto es equivalente 
	 * a eliminar cualquier selecci?n de borde.
	 */
	private void resetBorderSelected() {
		for (int i = 0; i < cornerActive.length; i++)
			cornerActive[i] = false;
	}

}
