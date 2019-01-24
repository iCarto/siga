package org.gvsig.mapsheets.print.series;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;
import org.gvsig.mapsheets.print.audasa.AudasaPreferences;
import org.gvsig.mapsheets.print.audasa.AudasaTemplate;
import org.gvsig.mapsheets.print.audasa.VariablesTemplatePanel;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.gui.MapSheetSelectionDialog;
import org.gvsig.mapsheets.print.series.gui.MapSheetsSettingsPanel;
import org.gvsig.mapsheets.print.series.layout.MapSheetFrameViewFactory;
import org.gvsig.mapsheets.print.series.layout.MapSheetsLayoutTemplate;
import org.gvsig.mapsheets.print.series.layout.MapSheetsProjectMap;
import org.gvsig.mapsheets.print.series.layout.MapSheetsProjectMapFactory;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.plugins.Extension;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.About;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.ProjectFactory;
import com.iver.cit.gvsig.project.documents.layout.ProjectMap;
import com.iver.cit.gvsig.project.documents.layout.gui.Layout;
import com.iver.cit.gvsig.project.documents.view.ProjectView;
import com.iver.cit.gvsig.project.documents.view.ProjectViewFactory;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.utiles.GenericFileFilter;
import com.iver.utiles.SimpleFileFilter;

/**
 * This extension deals with gris creation (based on a view/layer or from a
 * shapefile)
 * 
 * @author jldominguez
 *
 */
public class MapSheetsLoadExtension extends Extension {
	
	public MapSheetsLoadExtension() {
		
	}
	
	 public void initialize() {
        // about
        //java.net.URL newurl = createResourceUrl(
        //		"about"
        //		+ File.separator
        //		+ "about.htm");
        //About claseAbout = (About) PluginServices.getExtension(com.iver.cit.gvsig.About.class);
        //claseAbout.getAboutPanel().addAboutUrl("Map Sheets", newurl);
	 }


	 public void execute(String comm) {

		 if (comm.compareToIgnoreCase("MAP_SHEETS_FROM_FILE") == 0) {

			 MapControl mc = null;

				try {

					IWindow w = PluginServices.getMDIManager().getActiveWindow();
					if (w instanceof View) {
						View v = (View) w;
						mc = v.getMapControl();
					} else {
						throw new Exception("Current view is not a view: " + w.getClass().getName());
					}

					JFileChooser jfc = new JFileChooser();
					SimpleFileFilter filterGrid = new SimpleFileFilter("grid",
							PluginServices.getText(MapSheetsUtils.class, "grid_files"));
					jfc.setFileFilter(filterGrid);
					if (jfc.showOpenDialog((Component) PluginServices.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
						File file = jfc.getSelectedFile();
						if (file.exists()){
							MapSheetGrid msg = MapSheetsUtils.loadMapSheetsGrid(file);
							mc.getMapContext().getLayers().addLayer(msg);
							MapSheetsUtils.setOnlyActive(msg, mc.getMapContext().getLayers());
						} else {
							throw new Exception("File does not exist: " + file.getAbsolutePath());
						}
					}
				} catch (Exception e) {
					NotificationManager.addError("While opening grid SHP: " +
							e.getMessage(),e);
				}
		 }
	 }


	 public boolean isEnabled() {
	        return true;
	 }


	 public boolean isVisible() {
	        try {
	            IWindow w = PluginServices.getMDIManager().getActiveWindow();

	            if (w instanceof View) {
	                return true; 
	            }
	        } catch (Exception ex) {
	            return false;
	        }

	        return false;
	 }


}
