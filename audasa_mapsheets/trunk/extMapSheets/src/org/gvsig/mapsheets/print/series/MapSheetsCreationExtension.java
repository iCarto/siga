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
import com.iver.utiles.SimpleFileFilter;

/**
 * This extension deals with gris creation (based on a view/layer or from a
 * shapefile)
 * 
 * @author jldominguez
 *
 */
public class MapSheetsCreationExtension extends Extension {

    private static Logger logger = Logger.getLogger(MapSheetsCreationExtension.class);

    public static Properties extensionProperties = new Properties();

    public static String CODE_ID_SEPARATOR = "_";
    public static int MAX_PRINTAB_MAPS = 200;

    public static Color GRID_COLOR_BORDER = Color.BLACK;
    public static Color GRID_COLOR_FILL = new Color(0,0,0,35);
    public static Color GRID_COLOR_SEL_BORDER = new Color(200,200,0);
    public static Color GRID_COLOR_SEL_FILL = new Color(255,255,0,35);
    public static Color GRID_COLOR_EDIT_BORDER = new Color(192,0,0);
    public static Color GRID_COLOR_EDIT_FILL = new Color(255,0,0,35);

    public MapSheetsCreationExtension() {

    }

    public void initialize() {
	URL prop_file = getClass().getClassLoader().getResource("properties"
		+ File.separator + "mapsheets.properties");
	// ResourceRead
	try {
	    loadProperties(prop_file);
	} catch (Exception ex) {
	    logger.error("While loading props: " + ex.getMessage() + ". Defaults will be used.");
	}

	LayerFactory.registerLayerClassForName(
		MapSheetGrid.class.getCanonicalName(),
		MapSheetGrid.class);

	MapSheetsProjectMapFactory.register();
	MapSheetFrameViewFactory.register();
	registerIcons();

	// about
	java.net.URL newurl = createResourceUrl(
		"about"
			+ File.separator
			+ "about.htm");
	About claseAbout = (About) PluginServices.getExtension(com.iver.cit.gvsig.About.class);
	claseAbout.getAboutPanel().addAboutUrl("Map Sheets", newurl);


    }

    private void registerIcons() {
	PluginServices.getIconTheme().register(
		"create-grid", getClass().getClassLoader().getResource(
			"images" + File.separator + "grid.png"));
	PluginServices.getIconTheme().register(
		"to-shp", getClass().getClassLoader().getResource(
			"images" + File.separator + "toshp.png"));
	PluginServices.getIconTheme().register(
		"from-shp", getClass().getClassLoader().getResource(
			"images" + File.separator + "fromshp.png"));
	PluginServices.getIconTheme().register(
		"adjust-grid", getClass().getClassLoader().getResource(
			"images" + File.separator + "adjust.png"));
	PluginServices.getIconTheme().register(
		"set-map", getClass().getClassLoader().getResource(
			"images" + File.separator + "configmap.png"));
	PluginServices.getIconTheme().register(
		"other-grid", getClass().getClassLoader().getResource(
			"images" + File.separator + "change.png"));
    }

    private void loadProperties(URL pf) throws Exception {

	Properties pp = new Properties();
	pp.load(pf.openStream());
	String aux = "";

	aux = pp.getProperty("grid.sheet.id.separator");
	CODE_ID_SEPARATOR = aux;

	aux = pp.getProperty("grid.sheet.max");
	try {
	    MAX_PRINTAB_MAPS = Integer.parseInt(aux);
	} catch (Exception ex) {
	    logger.error("Bad number: " + aux + ", using default = " + MAX_PRINTAB_MAPS);
	}

	aux = pp.getProperty("grid.sheet.symbol.border.color");
	GRID_COLOR_BORDER = MapSheetsUtils.argbToColor(aux);
	aux = pp.getProperty("grid.sheet.symbol.fill.color");
	GRID_COLOR_FILL = MapSheetsUtils.argbToColor(aux);
	aux = pp.getProperty("grid.sheet.symbol.selection.border.color");
	GRID_COLOR_SEL_BORDER = MapSheetsUtils.argbToColor(aux);
	aux = pp.getProperty("grid.sheet.symbol.selection.fill.color");
	GRID_COLOR_SEL_FILL = MapSheetsUtils.argbToColor(aux);
	aux = pp.getProperty("grid.sheet.symbol.editing.border.color");
	GRID_COLOR_EDIT_BORDER = MapSheetsUtils.argbToColor(aux);
	aux = pp.getProperty("grid.sheet.symbol.editing.fill.color");
	GRID_COLOR_EDIT_FILL = MapSheetsUtils.argbToColor(aux);
    }

    @Override
    public void postInitialize() {

    }


    @Override
    public void terminate() {

    }


    public void execute(String comm) {

	if (comm.compareToIgnoreCase("MAP_SHEETS_GENERATE_DIALOG") == 0) {
	    IWindow w = PluginServices.getMDIManager().getActiveWindow();

	    if (w != null && w instanceof View) {

		// FloatingManagerDialog.cancelFromOutside();

		View v = (View) w;
		MapControl mc = v.getMapControl();
		MapContext mx = mc.getMapContext();

		MapSheetsSettingsPanel panel = new MapSheetsSettingsPanel(v);
		PluginServices.getMDIManager().addCentredWindow(panel);
		if(panel.hasCancelled()) {
		    return;
		}

		AudasaTemplate variables;
		if (panel.getSelectedTemplate().toLowerCase().endsWith(".gvt") ||
			panel.getSelectedTemplate().equals(AudasaPreferences.A4_POLICIA_MARGENES) ||
			panel.getSelectedTemplate().equals(AudasaPreferences.A4_POLICIA_MARGENES_LEYENDA) ||
			panel.getSelectedTemplate().equals(AudasaPreferences.A3_POLICIA_MARGENES) ||
			panel.getSelectedTemplate().equals(AudasaPreferences.A3_POLICIA_MARGENES_LEYENDA)) {
		    variables = new AudasaTemplate();
		} else {
		    // ask the user
		    VariablesTemplatePanel variablesPanel = new VariablesTemplatePanel(panel.getSelectedTemplate());
		    PluginServices.getMDIManager().addCentredWindow(variablesPanel);
		    variables = variablesPanel.getAudasaTemplateVariables();
		}

		//preload selection dialog variables
		Layout layout = panel.getMapLayout();
		MapSheetSelectionDialog dlg = new MapSheetSelectionDialog(mx, null, layout);
		//				 PluginServices.getMDIManager().addWindow(dlg);


		Object[] grid_auxlyt = dlg.getSelectedAndAuxLayout();
		MapSheetGrid msg = (MapSheetGrid) grid_auxlyt[0];
		Layout auxlayout = (Layout) grid_auxlyt[1];

		if (msg != null) {

		    //double left_cm = 0.1 * dlg.getLeftMargin();
		    //double top_cm = 0.1 * dlg.getTopMargin();
		    double left_cm = AudasaPreferences.VIEW_X_POSITION;
		    double top_cm = AudasaPreferences.VIEW_Y_POSITION;
		    String selected = MapSheetsSettingsPanel.getSelectedTemplate();
		    if (MapSheetsSettingsPanel.getSelectedTemplate().contains("A4") || MapSheetsSettingsPanel.getFormatComboBox().equals("A4")) {
			left_cm = AudasaPreferences.VIEW_X_POSITION_A4;
			top_cm = AudasaPreferences.VIEW_Y_POSITION_A4;
		    }
		    ArrayList act_flds = dlg.getActiveFieldsList();
		    ArrayList act_flds_tem = dlg.getActiveFieldsTemplateList();
		    ArrayList act_flds_idx = dlg.getActiveFieldsIndexList();

		    // x
		    // MapSheetsLayoutTemplate
		    // msg.setVisible(false);
		    Project p = v.getModel().getProject();

		    ProjectView _pv = (ProjectView) v.getModel();
		    MapContext _mc = _pv.getMapContext();
		    MapContext _omc = _pv.getMapOverViewContext();

		    MapContext clo_mc =
			    MapSheetsUtils.cloneMapContextRemoveGrids(_mc);
		    MapContext clo_omc =
			    MapSheetsUtils.cloneMapContextRemoveGrids(_omc);

		    Dimension aux_dim = _mc.getViewPort().getImageSize();
		    clo_mc.getViewPort().setImageSize(aux_dim);

		    ProjectView cloned_pv = new ProjectView();

		    cloned_pv.setName(_pv.getName());
		    cloned_pv.setProjectDocumentFactory(new ProjectViewFactory());
		    cloned_pv.setMapContext(clo_mc);
		    cloned_pv.setMapOverViewContext(clo_omc);

		    // FLayers ll = cloned_pv.getMapContext().getLayers();
		    // MapSheetsUtils.setGridsToVisible(ll, false);

		    double wh_ratio = 1;

		    try {
			wh_ratio = MapSheetsUtils.getWHRatio(msg);
		    } catch (Exception e) {
			NotificationManager.addError(e);
		    }

		    MapSheetsUtils.initViewPort(cloned_pv, wh_ratio);

		    MapSheetsLayoutTemplate mslt = new MapSheetsLayoutTemplate(
			    msg,
			    cloned_pv,
			    auxlayout,
			    variables);
		    // mslt.setGrid(msg);
		    ProjectMap _pmap = ProjectFactory.createMap("Sheets layout");

		    MapSheetsProjectMap mspm = new MapSheetsProjectMap();
		    mspm.setName("Layout Template " + MapSheetsLayoutTemplate.nextId());
		    mspm.setProjectDocumentFactory(_pmap.getProjectDocumentFactory());

		    mspm.setModel(mslt);
		    mslt.setProjectMap(mspm);
		    p.addDocument(mspm);

		    mslt.init(act_flds,
			    act_flds_idx,
			    act_flds_tem,
			    left_cm,
			    top_cm,
			    false);

		    PluginServices.getMDIManager().addWindow(mslt);
		    try {
			mslt.update(0);
		    } catch (Exception e) {
			NotificationManager.addError(e);
		    }
		}

	    } else {
		NotificationManager.showMessageError(
			"Bad window (not a View): " + w,
			new Exception("Bad window: " + w));
	    }
	    return;
	}

	// =============================================================================
	// =============================================================================
	if (comm.compareToIgnoreCase("MAP_SHEETS_FROM_SHP") == 0) {

	    MapContext mx = null;
	    MapControl mc = null;

	    try {

		IWindow w = PluginServices.getMDIManager().getActiveWindow();
		if (w instanceof View) {
		    View v = (View) w;
		    mc = v.getMapControl();
		    mx = mc.getMapContext();
		} else {
		    throw new Exception("Current view is not a view: " + w.getClass().getName());
		}

		JFileChooser jfc = new JFileChooser();
		SimpleFileFilter filterShp = new SimpleFileFilter("shp",
			PluginServices.getText(MapSheetsUtils.class, "shp_files"));
		jfc.setFileFilter(filterShp);
		if (jfc.showOpenDialog((Component) PluginServices.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
		    File openFile = jfc.getSelectedFile();
		    if (openFile.exists()){
			String error_cause = MapSheetsUtils.validMapSheetsGridShp(openFile);
			if (error_cause == null) {
			    // ok
			    MapSheetsUtils.addMapSheetsGrid2(openFile, mc);
			} else {
			    throw new Exception(error_cause);
			}
		    } else {
			throw new Exception("File does not exist: " + openFile.getAbsolutePath());
		    }

		}
	    } catch (Exception e) {
		NotificationManager.addError("While opening grid SHP: " +
			e.getMessage(),e);
	    }

	}
	// =============================================================================
	// =============================================================================
    }


    public boolean isEnabled() {
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


    public boolean isVisible() {
	return true;
    }



    private java.net.URL createResourceUrl(String path) {
	return getClass().getClassLoader().getResource(path);
    }

}
