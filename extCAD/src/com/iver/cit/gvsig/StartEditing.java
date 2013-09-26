package com.iver.cit.gvsig;

import java.awt.Component;

import javax.swing.JOptionPane;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.plugins.Extension;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.edition.rules.IRule;
import com.iver.cit.gvsig.fmap.edition.rules.RulePolygon;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.gui.cad.CADTool;
import com.iver.cit.gvsig.gui.tokenmarker.ConsoleToken;
import com.iver.cit.gvsig.layers.VectorialLayerEdited;
import com.iver.cit.gvsig.project.documents.table.ProjectTable;
import com.iver.cit.gvsig.project.documents.table.gui.Table;
import com.iver.cit.gvsig.project.documents.view.IProjectView;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.cit.gvsig.project.documents.view.snapping.VectorialLayerSnapping;
import com.iver.utiles.console.jedit.KeywordMap;
import com.iver.utiles.console.jedit.Token;

/**
 * @author Vicente Caballero Navarro
 * @author Francisco Puga <fpuga@cartolab.es>
 */
public class StartEditing extends Extension {

    @Override
    public void initialize() {
    }

    @Override
    public void execute(String actionCommand) {

	IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();

	if (iWindow instanceof View) {
	    View view = (View) iWindow;

	    IProjectView model = view.getModel();
	    FLayer[] actives = model.getMapContext().getLayers().getActives();

	    if (actives.length == 1 && actives[0] instanceof FLyrVect) {
		FLyrVect lv = (FLyrVect) actives[0];
		if (isReprojected(view, lv)) {
		    return;
		}
		if (cancelIfLayerIsJoined(lv)) {
		    return;
		}

		warnIfLayerNotWriteable(lv);

		startEditing(view, lv);
		// starts snapping over every visible layer
		VectorialLayerSnapping snapTo = new VectorialLayerSnapping(lv);
		snapTo.setSnappinTo(view.getMapControl().getMapContext()
			.getLayers());
	    }
	}

    }

    public void startEditing(View view, FLyrVect lv) {
	CADExtension.initFocus();
	view.showConsole();
	EditionManager editionManager = CADExtension.getEditionManager();
	MapControl mapControl = view.getMapControl();
	editionManager.setMapControl(mapControl);

	lv.addLayerListener(editionManager);
	try {
	    ILegend legendOriginal = lv.getLegend().cloneLegend();

	    lv.setEditing(true);
	    VectorialEditableAdapter vea = (VectorialEditableAdapter) lv
		    .getSource();

	    vea.getRules().clear();
	    if (vea.getShapeType() == FShape.POLYGON) {
		IRule rulePol = new RulePolygon();
		vea.getRules().add(rulePol);
	    }

	    VectorialLayerEdited vle = (VectorialLayerEdited) editionManager
		    .getLayerEdited(lv);
	    vle.setLegend(legendOriginal);

	    vea.getCommandRecord().addCommandListener(mapControl);

	    // If an associated table exists its model it's changed to
	    // VectorialEditableAdapter
	    ProjectExtension pe = (ProjectExtension) PluginServices
		    .getExtension(ProjectExtension.class);
	    ProjectTable pt = pe.getProject().getTable(lv);
	    if (pt != null) {
		pt.setModel(vea);
		changeModelTable(pt, vea);
		if (lv.isJoined()) {
		    pt.restoreDataSource();
		}
	    }

	    startCommandsApplicable(view, lv);
	    view.repaintMap();

	} catch (XMLException e) {
	    NotificationManager.addError(e.getMessage(), e);
	} catch (StartEditionLayerException e) {
	    NotificationManager.addError(e.getMessage(), e);
	} catch (ReadDriverException e) {
	    NotificationManager.addError(e.getMessage(), e);
	} catch (DriverLoadException e) {
	    NotificationManager.addError(e.getMessage(), e);
	}
    }

    private boolean isReprojected(View view, FLyrVect lv) {
	if (!view.getProjection().getAbrev()
		.equals(lv.getProjection().getAbrev())) {
	    NotificationManager.showMessageInfo(PluginServices.getText(this,
		    "no_es_posible_editar_capa_reproyectada"), null);
	    return true;
	}
	return false;
    }

    private boolean cancelIfLayerIsJoined(FLyrVect lv) {
	if (lv.isJoined()) {
	    int resp = JOptionPane.showConfirmDialog(
		    (Component) PluginServices.getMainFrame(),
		    PluginServices.getText(this, "se_perdera_la_union") + "\n"
			    + PluginServices.getText(this, "desea_continuar"),
		    PluginServices.getText(this, "start_edition"),
		    JOptionPane.YES_NO_OPTION);
	    if (resp != JOptionPane.YES_OPTION) {
		return true;
	    }
	}
	return false;
    }

    private void warnIfLayerNotWriteable(FLyrVect lv) {
	if (!lv.isWritable()) {
	    JOptionPane.showMessageDialog((Component) PluginServices
		    .getMDIManager().getActiveWindow(), PluginServices.getText(
		    this, "this_layer_is_not_self_editable"), PluginServices
		    .getText(this, "warning_title"),
		    JOptionPane.WARNING_MESSAGE);
	}
    }

    /**
     * Use VectorialLayerSnapping vectorialLayerSnapping = new
     * VectorialLayerSnapping((FLyrVect) vle.getLayer());
     * vectorialLayerSnapping.setSnappinTo(layers);
     */
    @Deprecated
    public void setSnappers(VectorialLayerEdited vle, FLayers layers) {
	VectorialLayerSnapping vectorialLayerSnapping = new VectorialLayerSnapping(
		(FLyrVect) vle.getLayer());
	vectorialLayerSnapping.setSnappinTo(layers);
    }

    public static void startCommandsApplicable(View view, FLyrVect lv) {
	if (view == null) {
	    view = (View) PluginServices.getMDIManager().getActiveWindow();
	}
	CADTool[] cadtools = CADExtension.getCADTools();
	KeywordMap keywordMap = new KeywordMap(true);
	for (int i = 0; i < cadtools.length; i++) {
	    try {
		if (cadtools[i].isApplicable(lv.getShapeType())) {
		    keywordMap.add(cadtools[i].getName(), Token.KEYWORD2);
		    keywordMap.add(cadtools[i].toString(), Token.KEYWORD3);
		}
	    } catch (ReadDriverException e) {
		NotificationManager.addError(e.getMessage(), e);
	    }

	}
	ConsoleToken consoletoken = new ConsoleToken(keywordMap);
	view.getConsolePanel().setTokenMarker(consoletoken);
    }

    private void changeModelTable(ProjectTable pt, VectorialEditableAdapter vea) {
	IWindow[] iWindows = PluginServices.getMDIManager().getAllWindows();

	for (IWindow iWindow : iWindows) {
	    if (iWindow instanceof Table) {
		Table table = (Table) iWindow;
		ProjectTable model = table.getModel();
		if (model.equals(pt)) {
		    table.setModel(pt);
		    vea.getCommandRecord().addCommandListener(table);
		}
	    }
	}
    }

    @Override
    public boolean isVisible() {
	IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();

	if (iWindow instanceof View) {
	    View view = (View) iWindow;
	    FLayer[] actives = view.getModel().getMapContext().getLayers()
		    .getActives();
	    if (actives.length == 1) {
		FLayer active = actives[0];
		if (active.isAvailable() && active instanceof FLyrVect
			&& !active.isEditing()) {
		    return true;
		}
	    }
	}
	return false;
    }

    @Override
    public boolean isEnabled() {
	View view = (View) PluginServices.getMDIManager().getActiveWindow();

	if (view == null) {
	    return false;
	}

	FLayer[] actives = view.getModel().getMapContext().getLayers()
		.getActives();
	if (actives.length == 1 && actives[0].isAvailable()
		&& actives[0] instanceof FLyrVect) {
	    if (((FLyrVect) actives[0]).isJoined()) {
		return false;
	    }
	    return true;
	}
	return false;
    }

}
