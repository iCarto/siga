package es.icarto.gvsig.extgia.forms;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
import es.icarto.gvsig.navtableforms.utils.DBConnectionBaseFormFactory;
import es.icarto.gvsig.navtableforms.utils.FormFactory;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;

public class SIGAFormFactory extends DBConnectionBaseFormFactory {

    private final static SIGAFormFactory instance;

    static {
	instance = new SIGAFormFactory();
    }

    private SIGAFormFactory() {
    }

    public static void registerFormFactory() {
	FormFactory.registerFormFactory(instance);
    }

    @Override
    public AbstractForm createForm(FLyrVect layer) {
	return LaunchGIAForms.getFormDependingOfLayer(layer);
    }

    @Override
    public AbstractForm createSingletonForm(FLyrVect layer) {
	throw new AssertionError("Not implemented jet");
    }

    @Override
    public AbstractForm createForm(String layerName) {
	FLyrVect layer = new TOCLayerManager().getLayerByName(layerName);
	return createForm(layer);
    }

    @Override
    public AbstractForm createSingletonForm(String layerName) {
	FLyrVect layer = new TOCLayerManager().getLayerByName(layerName);
	return createSingletonForm(layer);
    }

    @Override
    public AbstractSubForm createSubForm(String tableName) {
	return null;
    }

    @Override
    public boolean hasMainForm(String layerName) {
	throw new AssertionError("Not implemented jet");
    }

    @Override
    public boolean allLayersLoaded() {
	throw new AssertionError("Not implemented jet");
    }

    @Override
    public void loadLayer(String layerName) {
	loadLayer(layerName, "audasa_extgia");
    }

    @Override
    public void loadTable(String tableName) {
	/*
	 * As nt formfactory only allows register one factory we should do this
	 * ugly if instead of have a factory for each project
	 */
	String schema = "audasa_extgia";
	if (tableName.equalsIgnoreCase("bienes_afectados")
	        || tableName.equalsIgnoreCase("procesos")) {
	    schema = "audasa_expropiaciones";
	}
	loadTable(tableName, schema);
    }
}
