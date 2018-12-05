package es.icarto.gvsig.extgex;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.plugins.IExtension;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.gui.cad.CADTool;
import com.iver.cit.gvsig.gui.cad.tools.PolylineCADTool;
import com.iver.cit.gvsig.listeners.CADListenerManager;
import com.iver.cit.gvsig.listeners.EndGeometryListener;

import es.icarto.gvsig.extgex.cad.AddFincaCADTool;
import es.icarto.gvsig.extgex.cad.GextJoinCADTool;
import es.icarto.gvsig.extgex.forms.FormExpropiationLine;
import es.icarto.gvsig.extgex.forms.expropiations.FormExpropiations;
import es.icarto.gvsig.extgex.forms.reversions.FormReversions;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.extgia.forms.LaunchGIAForms;
import es.udc.cartolab.gvsig.users.DBConnectionExtension;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class InitializationExtension extends Extension {

    private static final String KEY_NAME = "es.udc.cartolab.gvsig.navtable";

    @Override
    public void initialize() {
	NTEndGeometryListener listener = new NTEndGeometryListener();
	CADListenerManager.addEndGeometryListener(KEY_NAME, listener);
    }

    @Override
    public void postInitialize() {
	CADExtension.addCADTool("_join", new GextJoinCADTool());
	PluginServices.getMDIManager().closeAllWindows();
	// launch dbconnection dialog
	IExtension dbconnection = PluginServices
		.getExtension(DBConnectionExtension.class);
	initAddFincaCADTool();
	dbconnection.execute(null);
    }

    private void initAddFincaCADTool() {

	AddFincaCADTool tool = new AddFincaCADTool();
	CADExtension.addCADTool(AddFincaCADTool.KEY, tool);
    }

    private class NTEndGeometryListener implements EndGeometryListener {

	@Override
	public void endGeometry(FLayer layer, String cadToolKey) {

	    if (layer.getName().equals(FormReversions.TOCNAME)) {
		endReversionesGeometry(layer);
	    } else if (layer.getName().equals(FormExpropiations.TOCNAME)) {
		endFincasGeometry(layer, cadToolKey);
	    } else if (layer.getName().equals(FormExpropiationLine.TOCNAME) || layer.getName().equals(FormExpropiationLine.TOCNAME_AMPLIACION)) {
		endLineExpropiationGeometry(layer);
	    } else {
		LaunchGIAForms.callFormDependingOfLayer((FLyrVect) layer, true);
	    }
	}

	private void endLineExpropiationGeometry(FLayer layer) {
	    boolean isAmpliacion = layer.getName().equals(FormExpropiationLine.TOCNAME_AMPLIACION);
	    FormExpropiationLine dialog = new FormExpropiationLine(
		    (FLyrVect) layer, isAmpliacion);
	    if (dialog.init()) {
		PluginServices.getMDIManager().addCentredWindow(dialog);
		dialog.last();
	    }
	}

	private void endFincasGeometry(FLayer layer, String cadToolKey) {
	    DBSession.getCurrentSession().setSchema(
		    DBNames.EXPROPIATIONS_SCHEMA);
	    IGeometry insertedGeom = null;
	    FLyrVect lv = (FLyrVect) layer;

	    CADTool cadTool = CADExtension.getCADTool();
	    if (cadTool instanceof PolylineCADTool) {
		insertedGeom = ((PolylineCADTool) cadTool).getGeometry();
	    }
	    FormExpropiations dialog = new FormExpropiations(lv, insertedGeom);
	    if (dialog.init()) {
		PluginServices.getMDIManager().addCentredWindow(dialog);
		if (cadToolKey.equals(AddFincaCADTool.KEY)) {
		    int pos = lv.getSelectionSupport().getSelection()
			    .nextSetBit(0);
		    if (pos != -1) {
			dialog.setPosition(pos);
		    }
		} else {
		    dialog.last();
		}
	    }
	}

	private void endReversionesGeometry(FLayer layer) {
	    CADTool cadTool = CADExtension.getCADTool();
	    IGeometry insertedGeom = null;
	    if (cadTool instanceof PolylineCADTool) {
		insertedGeom = ((PolylineCADTool) cadTool).getGeometry();
	    }
	    es.icarto.gvsig.extgex.forms.reversions.FormReversions dialog = new es.icarto.gvsig.extgex.forms.reversions.FormReversions(
		    (FLyrVect) layer, insertedGeom);
	    if (dialog.init()) {
		PluginServices.getMDIManager().addCentredWindow(dialog);
		dialog.last();
	    }
	}

    }

    @Override
    public void execute(String actionCommand) {
    }

    @Override
    public boolean isEnabled() {
	return false;
    }

    @Override
    public boolean isVisible() {
	return false;
    }

}
