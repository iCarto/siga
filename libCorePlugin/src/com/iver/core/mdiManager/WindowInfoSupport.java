/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
package com.iver.core.mdiManager;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.Hashtable;

import com.iver.andami.plugins.PluginClassLoader;
import com.iver.andami.ui.mdiFrame.MainFrame;
import com.iver.andami.ui.mdiFrame.NoSuchMenuException;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.SingletonWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;


/**
 * This class listens to changes in WindowInfo objects, and reflects this
 * changes in the associated window.
 */
public class WindowInfoSupport {
	private static int serialId = 0;
	
	/**
	 * Support class which associates Frames and Windows
	 */
	private FrameWindowSupport fws;

	// Correspondencias entre las ventanas y su informacion
	private Hashtable<IWindow, WindowInfo> viewInfo = new Hashtable<IWindow, WindowInfo>();
	private Hashtable<WindowInfo, IWindow> infoView = new Hashtable<WindowInfo, IWindow>();
	private WindowPropertyChangeListener windowInfoListener = new WindowPropertyChangeListener();
	private SingletonWindowSupport sws;
	private MainFrame mdiFrame;

	/**
	 * Creates a new ViewInfoSupport object.
	 */
	public WindowInfoSupport(MainFrame frame, FrameWindowSupport fvs,
		SingletonWindowSupport svs) {
		this.fws = fvs;
		this.sws = svs;
		this.mdiFrame = frame;
	}

	/**
	 * Devuelve la vista cuyo identificador es el parametro
	 *
	 * @param id Identificador de la vista que se quiere obtener
	 *
	 * @return La vista o null si no hay ninguna vista con ese identificador
	 */
	public IWindow getWindowById(int id) {
		Enumeration<WindowInfo> en = infoView.keys();

		while (en.hasMoreElements()) {
			WindowInfo vi = en.nextElement();

			if (vi.getId() == id) {
				return infoView.get(vi);
			}
		}

		return null;
	}

	public synchronized WindowInfo getWindowInfo(IWindow w) {
		WindowInfo wi = viewInfo.get(w);

		if (wi != null) {
			fws.updateWindowInfo(w, wi);
		}
		else {
			wi = w.getWindowInfo();

			//Para el título
			if (wi.getHeight() != -1) {
				wi.setHeight(wi.getHeight() + 40);
			}

			wi.addPropertyChangeListener(windowInfoListener);
			viewInfo.put(w, wi);
			infoView.put(wi, w);
			wi.setId(serialId++);
		}

		return wi;
	}

	public void deleteWindowInfo(IWindow p) {
		WindowInfo vi = viewInfo.remove(p);
		infoView.remove(vi);
	}


	public class WindowPropertyChangeListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			WindowInfo winInfo = (WindowInfo) evt.getSource();
			IWindow win = infoView.get(winInfo);

			if (win instanceof SingletonWindow) {
				SingletonWindow sw = (SingletonWindow) win;

				if (evt.getPropertyName().equals("x")) {
					sws.setX(sw, ((Integer) evt.getNewValue()).intValue());
				} else if (evt.getPropertyName().equals("y")) {
					sws.setY(sw, ((Integer) evt.getNewValue()).intValue());
				} else if (evt.getPropertyName().equals("height")) {
					sws.setHeight(sw, ((Integer) evt.getNewValue()).intValue());
				} else if (evt.getPropertyName().equals("width")) {
					sws.setWidth(sw, ((Integer) evt.getNewValue()).intValue());
				} else if (evt.getPropertyName().equals("maximized")) {
					sws.setMaximized(sw, ((Boolean) evt.getNewValue()).booleanValue());
				} else if (evt.getPropertyName().equals("normalBounds")) {
					sws.setNormalBounds(sw, (Rectangle) evt.getNewValue());
				} else if (evt.getPropertyName().equals("minimumSize")) {
					sws.setMinimumSize(sw, (Dimension) evt.getNewValue());
				} else if (evt.getPropertyName().equals("title")) {
					sws.setTitle(sw, (String) evt.getNewValue());

					try {
						mdiFrame.changeMenuName(new String[] {
								"Ventana", (String) evt.getOldValue()
						}, (String) evt.getNewValue(),
						(PluginClassLoader) getClass().getClassLoader());
					} catch (NoSuchMenuException e) {
						/*
						 * No se hace nada porque puede modificarse el título de
						 * una ventana antes de ser añadida a Andami
						 */
					}
				}
			} else {
				if (evt.getPropertyName().equals("x")) {
					fws.setX(win, ((Integer) evt.getNewValue()).intValue());
				} else if (evt.getPropertyName().equals("y")) {
					fws.setY(win, ((Integer) evt.getNewValue()).intValue());
				} else if (evt.getPropertyName().equals("height")) {
					fws.setHeight(win, ((Integer) evt.getNewValue()).intValue());
				} else if (evt.getPropertyName().equals("width")) {
					fws.setWidth(win, ((Integer) evt.getNewValue()).intValue());
				} else if (evt.getPropertyName().equals("minimumSize")) {
					fws.setMinimumSize(win, (Dimension) evt.getNewValue());
				} else if (evt.getPropertyName().equals("title")) {
					fws.setTitle(win, (String) evt.getNewValue());
					try{
						mdiFrame.changeMenuName(new String[] {
								"Ventana", (String) evt.getOldValue()
						}, (String) evt.getNewValue(),
						(PluginClassLoader) getClass().getClassLoader());
					} catch (NoSuchMenuException e) {
						/*
						 * No se hace nada porque puede modificarse el título de
						 * una ventana antes de ser añadida a Andami
						 */
					}
				}
			}
		}
	}
}
