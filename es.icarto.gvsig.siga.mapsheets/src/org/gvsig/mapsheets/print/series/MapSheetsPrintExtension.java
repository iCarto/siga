package org.gvsig.mapsheets.print.series;

import java.io.File;

import javax.swing.JOptionPane;

import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.gui.MapSheetViewGridDialog;
import org.gvsig.mapsheets.print.series.gui.PrintSelectionDialog;
import org.gvsig.mapsheets.print.series.layout.MapSheetsLayoutTemplate;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.project.documents.view.ProjectView;

/**
 * This extension deals with map sheets printing only.
 * 
 * @author jldominguez
 *
 */
public class MapSheetsPrintExtension extends Extension {

	public void execute(String actionCommand) {
		
		if (actionCommand.compareToIgnoreCase("MAP_SHEETS_PRINT") == 0) {
            IWindow w = PluginServices.getMDIManager().getActiveWindow();
            if (w instanceof MapSheetsLayoutTemplate) {
            	
            	MapSheetsLayoutTemplate tem = (MapSheetsLayoutTemplate) w;
            	if (hasGrid(tem)) {
                	PrintSelectionDialog dlg = new PrintSelectionDialog(tem);
                	PluginServices.getMDIManager().addWindow(dlg);
            	} else {
            		JOptionPane.showMessageDialog(
            				tem,
            				PluginServices.getText(this, "Cannot_print_because_no_grid_assciated"),
            				PluginServices.getText(this, "Print"),
            				JOptionPane.WARNING_MESSAGE);
            	}
            }
		}
		
		if (actionCommand.compareToIgnoreCase("MAP_SHEETS_SET_NEW_GRID") == 0) {
            IWindow w = PluginServices.getMDIManager().getActiveWindow();
            if (w instanceof MapSheetsLayoutTemplate) {
            	
            	MapSheetsLayoutTemplate tem = (MapSheetsLayoutTemplate) w;
            	MapSheetViewGridDialog dlg = new MapSheetViewGridDialog();
            	PluginServices.getMDIManager().addWindow(dlg);
            	
            	if (!dlg.isAccepted()) {
            		return;
            	}
            	
            	MapSheetGrid gri = dlg.getGrid();
            	ProjectView pvi = dlg.getView();
            	pvi = MapSheetsUtils.cloneProjectView(pvi);
            	
            	if (gri != null && pvi != null) {
            		tem.setViewGrid(pvi, gri);
            	}
            }
		}


	}

	private boolean hasGrid(MapSheetsLayoutTemplate tem) {
		return (tem.getGrid() != null);
	}

	public void initialize() {
		registerIcons();
	}

	private void registerIcons() {
    	PluginServices.getIconTheme().register(
    			"printer-opts", getClass().getClassLoader().getResource(
    					"images" + File.separator + "print_setts.png"));
	}

	public boolean isEnabled() {
        try {
            IWindow w = PluginServices.getMDIManager().getActiveWindow();

            if (w instanceof MapSheetsLayoutTemplate) {
            	return true;
            }
        } catch (Exception ex) {
            return false;
        }

        return false;		
	}

	public boolean isVisible() {
		return true; // isEnabled();
	}
	
	public static String PRINTER_SETTINGS_RESTORE_FILE = null;
	public static String PRINTER_SETTINGS_PRINTER_NAME = null;
	
	public void terminate() {
		
		if (PRINTER_SETTINGS_RESTORE_FILE != null &&
				PRINTER_SETTINGS_PRINTER_NAME != null) {
			
			MapSheetsUtils.printerSettingsSaveRestore(
					PRINTER_SETTINGS_PRINTER_NAME,
					PRINTER_SETTINGS_RESTORE_FILE, false);
			
			try {
				Thread.sleep(200);
				File delfile = new File(PRINTER_SETTINGS_RESTORE_FILE);
				if (delfile.exists()) {
					delfile.delete();
				}
			} catch (Exception ex) { }
		}
	}

}
