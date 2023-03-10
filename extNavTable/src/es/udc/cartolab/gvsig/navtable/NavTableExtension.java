/*
 * This file is part of NavTable
 * Copyright (C) 2009 - 2010  Cartolab (Universidade da Coru?a)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Authors:
 *   Juan Ignacio Varela Garc?a <nachouve (at) gmail (dot) com>
 *   Pablo Sanxiao Roca <psanxiao (at) gmail (dot) com>
 *   Javier Est?vez Vali?as <valdaris (at) gmail (dot) com>
 *   Francisco Puga Alonso <fpuga (at) cartolab (dot) com>
 *   Jorge Lopez Fernandez <jlopez (at) cartolab (dot) es>
 */
package es.udc.cartolab.gvsig.navtable;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.preferences.IPreference;
import com.iver.andami.preferences.IPreferenceExtension;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.About;
import com.iver.cit.gvsig.fmap.edition.IEditableSource;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.gui.panels.FPanelAbout;
import com.iver.cit.gvsig.project.documents.table.gui.Table;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;
import com.iver.utiles.extensionPoints.ExtensionPoints;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;

import es.udc.cartolab.gvsig.navtable.contextualmenu.FiltersAddon;
import es.udc.cartolab.gvsig.navtable.contextualmenu.INavTableContextMenu;
import es.udc.cartolab.gvsig.navtable.contextualmenu.SorterAddon;
import es.udc.cartolab.gvsig.navtable.preferences.NavTablePreferencesPage;
import es.udc.cartolab.gvsig.navtable.preferences.Preferences;
import es.udc.cartolab.gvsig.navtable.utils.NavTableTocMenuEntry;

public class NavTableExtension extends Extension implements IPreferenceExtension {

    private IPreference[] preferencesPage;
    protected static Logger logger = Logger.getLogger("NavTable");


    public void execute(String actionCommand) {
	if (enableNavtable()) {
	    openNavtable();
	} else if (enableAlphanumericNavtable()) {
	    openAlphanumericNavtable();
	}
    }

    private void openNavtable() {
	IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();
	if(iWindow instanceof BaseView){
	    for (FLyrVect vectorialLyr : getActiveVectorialLayersOnTheActiveWindow()) {
		NavTable navtable = new NavTable(vectorialLyr);
		if (navtable.init()) {
		    PluginServices.getMDIManager().addCentredWindow(navtable);
		}
	    }
	} else if(isActiveWindowAttTableFromLayer()){
	    AlphanumericData data = ((Table) iWindow).getModel().getAssociatedTable();
	    if(data instanceof FLyrVect){
		NavTable nt = new NavTable((FLyrVect) data);
		if(nt.init()){
		    PluginServices.getMDIManager().addCentredWindow(nt);
		}
	    }
	}
    }

    private void openAlphanumericNavtable() {
	Table table = (Table) PluginServices.getMDIManager().getActiveWindow();
	IEditableSource model = table.getModel().getModelo();
	AlphanumericNavTable viewer;
	try {
	    viewer = new AlphanumericNavTable(model, table.getModel().getName());
	    if (viewer.init()) {
		PluginServices.getMDIManager().addCentredWindow(viewer);
	    }
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}
    }


    public void initialize() {
	About about = (About) PluginServices.getExtension(About.class);
	FPanelAbout panelAbout = about.getAboutPanel();
	java.net.URL aboutURL = this.getClass().getResource("/about.htm");
	panelAbout.addAboutUrl("NavTable", aboutURL);

	// Entry at TOC contextual menu
	ExtensionPoints extensionPoints = ExtensionPointsSingleton
		.getInstance();
	extensionPoints.add("View_TocActions", "NavTable",
		new NavTableTocMenuEntry());

	// Add NavTable "official" context menu addons to the extension point
	INavTableContextMenu filtersAddon = new FiltersAddon();
	extensionPoints.add(AbstractNavTable.NAVTABLE_CONTEXT_MENU,
		filtersAddon.getName(), filtersAddon);
	INavTableContextMenu sorterAddon = new SorterAddon();
	extensionPoints.add(AbstractNavTable.NAVTABLE_CONTEXT_MENU,
		sorterAddon.getName(), sorterAddon);

	// Creating config Dir
	File configDir;
	String configDirStr;
	try {
	    configDirStr = com.iver.andami.Launcher.getAppHomeDir()
		    + "NavTable";
	} catch (java.lang.NoSuchMethodError e) {
	    configDirStr = System.getProperty("user.home") + File.separator
		    + "gvSIG" + File.separator + "navTable";

	}
	configDir = new File(configDirStr);
	Preferences p = new Preferences(configDir);
    }


    public boolean isVisible() {
	return true;
    }

    public boolean isEnabled() {
	return enableNavtable() || enableAlphanumericNavtable();
    }

    protected boolean enableNavtable() {
	return !getActiveVectorialLayersOnTheActiveWindow().isEmpty() || isActiveWindowAttTableFromLayer();
    }

    protected boolean enableAlphanumericNavtable() {
	IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();
	if ((iWindow != null) && (iWindow.getClass() == Table.class)
		&& !(isActiveWindowAttTableFromLayer())) {
	    return true;
	}
	return false;
    }

    private boolean isActiveWindowAttTableFromLayer() {
	IWindow w = PluginServices.getMDIManager().getActiveWindow();
	if(!(w instanceof Table)){
	    return false;
	}
	return ((Table) w).getModel().getAssociatedTable() instanceof FLyrVect;
    }


    private ArrayList<FLyrVect> getActiveVectorialLayersOnTheActiveWindow() {
	IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();
	ArrayList<FLyrVect> activeVectorialLayers = new ArrayList<FLyrVect>();

	if (iWindow instanceof BaseView) {
	    FLayer[] activeLayers = ((BaseView) iWindow).getMapControl()
		    .getMapContext().getLayers().getActives();
	    for (FLayer lyr : activeLayers) {
		if (lyr instanceof FLyrVect) {
		    activeVectorialLayers.add((FLyrVect) lyr);
		}
	    }
	}
	return activeVectorialLayers;
    }


    public IPreference[] getPreferencesPages() {
	if (preferencesPage == null) {
	    preferencesPage = new IPreference[] { new NavTablePreferencesPage() };
	}
	return preferencesPage;
    }

}