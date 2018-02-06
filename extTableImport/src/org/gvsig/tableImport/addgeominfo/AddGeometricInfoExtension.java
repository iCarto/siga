package org.gvsig.tableImport.addgeominfo;

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

import org.gvsig.tableImport.addgeominfo.gui.AddGeometricInfoPanel;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.IProjectView;
import com.iver.cit.gvsig.project.documents.view.gui.View;


/**
 * <p>Extension that allows user to select which geometric parameter wants to add to a vector layer.</p>
 * <p>After the selection, that information will be added, if the layer can be edited, as new columns of
 *  the associated data table.</p>
 * 
 * @author Pablo Piqueras Bartolomé (pablo.piqueras@iver.es)
 */
public class AddGeometricInfoExtension extends Extension {
	/*
	 * (non-Javadoc)
	 * @see com.iver.andami.plugins.IExtension#execute(java.lang.String)
	 */
	public void execute(String actionCommand) {
		if (actionCommand.equals("ADD_GEOMETRIC_INFO_TO_TABLE")) {
			IWindow view = PluginServices.getMDIManager().getActiveWindow();
			if (view instanceof View) {
				new AddGeometricInfoPanel((View)view);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.andami.plugins.IExtension#initialize()
	 */
	public void initialize() {
		registerIcons();
	}
	
	protected void registerIcons() {
		PluginServices.getIconTheme().registerDefault(
			"add-geom-info",
			this.getClass().getClassLoader().getResource("images/add-geom-info-icon.png")
		);
		PluginServices.getIconTheme().registerDefault(
				"button-ok-icon",
				this.getClass().getClassLoader().getResource("images/button-ok-icon.png")
			);
		PluginServices.getIconTheme().registerDefault(
				"button-cancel-icon",
				this.getClass().getClassLoader().getResource("images/button-cancel-icon.png")
			);
		PluginServices.getIconTheme().registerDefault(
				"double-left-arrow-icon",
				this.getClass().getClassLoader().getResource("images/double-left-arrow-icon.png")
			);
		PluginServices.getIconTheme().registerDefault(
				"double-right-arrow-icon",
				this.getClass().getClassLoader().getResource("images/double-right-arrow-icon.png")
			);
		PluginServices.getIconTheme().registerDefault(
				"layer-group",
				this.getClass().getClassLoader().getResource("images/layerGroup.png")
			);
		PluginServices.getIconTheme().registerDefault(
				"left-arrow-icon",
				this.getClass().getClassLoader().getResource("images/left-arrow-icon.png")
			);
		PluginServices.getIconTheme().registerDefault(
				"multi-icon",
				this.getClass().getClassLoader().getResource("images/multi-icon.png")
			);
		PluginServices.getIconTheme().registerDefault(
				"MultiPoint",
				this.getClass().getClassLoader().getResource("images/MultiPoint.png")
			);
		PluginServices.getIconTheme().registerDefault(
				"Point",
				this.getClass().getClassLoader().getResource("images/Point.png")
			);
		PluginServices.getIconTheme().registerDefault(
				"Polygon",
				this.getClass().getClassLoader().getResource("images/Polygon.png")
			);
		PluginServices.getIconTheme().registerDefault(
				"Rect",
				this.getClass().getClassLoader().getResource("images/Rect.png")
			);
		PluginServices.getIconTheme().registerDefault(
				"right-arrow-icon",
				this.getClass().getClassLoader().getResource("images/right-arrow-icon.png")
			);
		PluginServices.getIconTheme().registerDefault(
				"save-icon",
				this.getClass().getClassLoader().getResource("images/save-icon.png")
			);

	}


	/*
	 * (non-Javadoc)
	 * @see com.iver.andami.plugins.IExtension#isEnabled()
	 */
	public boolean isEnabled() {
		com.iver.andami.ui.mdiManager.IWindow f = PluginServices.getMDIManager().getActiveWindow();

		if (f == null) {
			return false;
		}
		
		if (f instanceof View) {
			View view = (View) f;
		
			IProjectView model = view.getModel();
			MapContext map = model.getMapContext();
			
			return hasVectorVisibleLayers(map.getLayers());
		} 
		
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.andami.plugins.IExtension#isVisible()
	 */
	public boolean isVisible() {
		com.iver.andami.ui.mdiManager.IWindow f = PluginServices.getMDIManager().getActiveWindow();

		if (f == null) {
			return false;
		}
		
		if (f instanceof View) {
			View view = (View) f;
		
			IProjectView model = view.getModel();
			MapContext map = model.getMapContext();
		
			return map.getLayers().getLayersCount() > 0;
		} else {
			return false;
		}
	}

	/**
	 * <p>Finds recursively if there is any visible vector layer.</p>
	 * 
	 * @param root the root node
	 * @return <code>true</code> if the layer is found and is visible in the tree; otherwise <code>false</code>
	 */
    private boolean hasVectorVisibleLayers(FLayers root) {
		if (root != null) {
			FLayer node;

			for (int i = 0; i < root.getLayersCount(); i++) {
				node = root.getLayer(i);

				if (node instanceof FLyrVect) {
					if (node.isVisible())
						return true;
				}
				else {
					if (node instanceof FLayers) {
						if (hasVectorVisibleLayers((FLayers) node))
							return true;
					}
				}
			}
		}
		
		return false;
    }
}
