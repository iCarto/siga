/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
package org.gvsig.rastertools.selectrasterlayer;

import javax.swing.Icon;

import org.gvsig.fmap.raster.layers.FLyrRasterSE;
import org.gvsig.fmap.raster.layers.IRasterLayerActions;
import org.gvsig.raster.gui.IGenericToolBarMenuItem;
import org.gvsig.raster.util.RasterToolsUtil;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.tools.Behavior.Behavior;
import com.iver.cit.gvsig.fmap.tools.Behavior.MouseMovementBehavior;
import com.iver.cit.gvsig.fmap.tools.Behavior.PointBehavior;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;
import com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction;
import com.iver.cit.gvsig.project.documents.view.toc.ITocItem;
import com.iver.cit.gvsig.project.documents.view.toolListeners.StatusBarListener;

/**
 * @author Nacho Brodin (nachobrodin@gmail.com)
 */
public class SelectLayerTocMenuEntry extends AbstractTocContextMenuAction implements IGenericToolBarMenuItem {
	static private SelectLayerTocMenuEntry singleton  = null;
	FLayer lyr = null;

	/*
	 *  (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.IContextMenuAction#getGroup()
	 */
	public String getGroup() {
		return "RasterLayer";
	}

	/*
	 *  (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.IContextMenuAction#getGroupOrder()
	 */
	public int getGroupOrder() {
		return 0;
	}

	/*
	 *  (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.IContextMenuAction#getOrder()
	 */
	public int getOrder() {
		return 0;
	}

	/*
	 *  (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.IContextMenuAction#getText()
	 */
	public String getText() {
		return RasterToolsUtil.getText(this, "seleccionar_capas_raster");
	}
	
	/**
	 * Devuelve un objeto unico a dicha clase
	 * @return
	 */
	static public SelectLayerTocMenuEntry getSingleton() {
		if (singleton == null)
			singleton = new SelectLayerTocMenuEntry();
		return singleton;
	}

	/*
	 *  (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction#isEnabled(com.iver.cit.gvsig.project.documents.view.toc.ITocItem, com.iver.cit.gvsig.fmap.layers.FLayer[])
	 */
	public boolean isEnabled(ITocItem item, FLayer[] selectedItems) {
		return true;
	}

	/*
	 *  (non-Javadoc)
	 * @see com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction#isVisible(com.iver.cit.gvsig.project.documents.view.toc.ITocItem, com.iver.cit.gvsig.fmap.layers.FLayer[])
	 */
	public boolean isVisible(ITocItem item, FLayer[] selectedItems) {
		if ((selectedItems == null) || (selectedItems.length != 1))
			return false;

		if (!(selectedItems[0] instanceof FLyrRasterSE))
			return false;

		return ((FLyrRasterSE) selectedItems[0]).isActionEnabled(IRasterLayerActions.SELECT_LAYER);
	}

	/**
	 * M�todo que se ejecuta cuando se pulsa la entrada en el men� contextual del TOC 
	 * correspondiente a "Zoom a la resoluci�n del raster". Aqu� se crear� el mapTool si 
	 * no se ha hecho antes y se cargar�.
	 */
	public void execute(ITocItem item, FLayer[] selectedItems) {
		BaseView theView = (BaseView) PluginServices.getMDIManager().getActiveWindow();
		MapControl mapCtrl = theView.getMapControl();

		// Listener de eventos de movimiento que pone las coordenadas del rat�n en
		// la barra de estado
		StatusBarListener sbl = new StatusBarListener(mapCtrl);

		loadSelectRasterListener(mapCtrl, sbl);
		mapCtrl.setTool("selectRasterLayer");
	}
	
	/**
	 * Carga el listener de selecci�n de raster en el MapControl.
	 */
	private void loadSelectRasterListener(MapControl mapCtrl, StatusBarListener sbl) {
		if (mapCtrl.getNamesMapTools().get("selectRasterLayer") == null) {
			SelectImageListener sil = new SelectImageListener(mapCtrl);
			mapCtrl.addMapTool("selectRasterLayer", new Behavior[] { new PointBehavior(sil), new MouseMovementBehavior(sbl) });
			mapCtrl.setTool("selectRasterLayer");
		}
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.gvsig.rastertools.generictoolbar.IGenericToolBarMenuItem#getIcon()
	 */
	public Icon getIcon() {
		return PluginServices.getIconTheme().get("select-raster");
	}
}