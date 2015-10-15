package es.udc.cartolab.gvsig.elle.gui.wizard.load;

import java.awt.BorderLayout;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiFrame.NewStatusBar;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.ProjectView;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.udc.cartolab.gvsig.elle.constants.ConstantReload;
import es.udc.cartolab.gvsig.elle.constants.ZoomTo;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.gui.wizard.save.LayerProperties;
import es.udc.cartolab.gvsig.elle.utils.ELLEMap;
import es.udc.cartolab.gvsig.elle.utils.LoadLegend;
import es.udc.cartolab.gvsig.elle.utils.MapDAO;

@SuppressWarnings("serial")
public class LoadConstantsWizardComponent extends WizardComponent {

    private boolean reload = false;

    private ConstantsPanel constantsPanel;

    public final static String PROPERTY_VIEW = "view";

    public LoadConstantsWizardComponent(Map<String, Object> properties,
	    boolean reload) {
	super(properties);
	this.reload = reload;
	setUpUI();
    }

    public LoadConstantsWizardComponent(Map<String, Object> properties) {
	this(properties, false);
    }

    private void setUpUI() {
	this.setLayout(new BorderLayout());
	constantsPanel = new ConstantsPanel(reload);
	this.add(constantsPanel, BorderLayout.CENTER);
    }

    @Override
    public boolean canFinish() {
	return true;
    }

    @Override
    public boolean canNext() {
	return true;
    }

    @Override
    public String getWizardComponentName() {
	return "constants_wizard_component";
    }

    @Override
    public void setProperties() throws WizardException {
    }

    @Override
    public void showComponent() throws WizardException {
    }

    @Override
    public void finish() throws WizardException {

	Object aux = properties.get(PROPERTY_VIEW);
	if (!(aux instanceof View)) {
	    throw new WizardException("Couldn't retrieve the view");
	}
	View view = (View) aux;

	ELLEMap map = null;
	String where = constantsPanel.buildWhereAndSetConstants();
	final Collection<String> tablesAffectedByConstant = constantsPanel
		.getTablesAffectedByConstant();
	if (reload) {
	    List<ELLEMap> loadedMaps = MapDAO.getInstance().getLoadedMaps();
	    for (ELLEMap m : loadedMaps) {
		if (m.getView().equals(view)
			&& m.getName().equals(view.getModel().getName())) {
		    map = m;
		}
	    }

	    // TODO. Que hacer si map es null aqu�? Por ahora nos contentamos
	    // con algo parecido al Null Object Pattern para evitar chequeos de
	    // null sobre map

	    map = map != null ? map : new ELLEMap(null, null);
	    map.setWhereOnAllLayers(where);
	    map.setWhereOnAllOverviewLayers(where);
	    setWhereOnProvinciasLoc(map);
	    new ConstantReload(view, where, tablesAffectedByConstant,
		    constantsPanel.buildWhereForProvinciasLoc());
	} else {
	    Object tmp = properties
		    .get(SigaLoadMapWizardComponent.PROPERTY_MAP_NAME);
	    String mapName = (tmp == null ? "" : tmp.toString());

	    try {
		map = MapDAO.getInstance().getMap(view, mapName);
		map.setWhereOnAllLayers(where);
		map.setWhereOnAllOverviewLayers(where);
		setWhereOnProvinciasLoc(map);

		map.load(view.getProjection(), tablesAffectedByConstant);

		loadLegends(view, mapName);

		if (view.getModel().getName().equals("ELLE View")
			&& (view.getModel() instanceof ProjectView)) {
		    ((ProjectView) view.getModel()).setName(mapName);
		}

		ZoomTo zoomTo = new ZoomTo(view.getMapControl());
		zoomTo.zoom(constantsPanel.getZoomGeometry());
	    } catch (Exception e) {
		throw new WizardException(e);
	    }

	}

	writeCouncilsLoadedInStatusBar();

    }

    private void setWhereOnProvinciasLoc(ELLEMap map) {
	LayerProperties overviewLayer = map
		.getOverviewLayer("Provincias_galicia_loc");
	if (overviewLayer != null) {
	    String where = constantsPanel.buildWhereForProvinciasLoc();
	    overviewLayer.setWhere(where);
	}
    }

    private void loadLegends(View view, String mapName) throws WizardException {
	FLayers layers = view.getMapControl().getMapContext().getLayers();
	try {
	    loadLegends(layers, false, mapName);
	    layers = view.getMapOverview().getMapContext().getLayers();
	    loadLegends(layers, true, mapName);
	} catch (SQLException e) {
	    throw new WizardException(e);
	} catch (IOException e) {
	    throw new WizardException(e);
	}
    }

    private void loadLegends(FLayers layers, boolean overview, String mapName)
	    throws SQLException, IOException {
	for (int i = 0; i < layers.getLayersCount(); i++) {
	    FLayer layer = layers.getLayer(i);
	    if (layer instanceof FLyrVect) {

		LoadLegend.loadLegend((FLyrVect) layer, mapName, overview,
			LoadLegend.DB_LEGEND);
	    } else if (layer instanceof FLayers) {
		loadLegends((FLayers) layer, overview, mapName);
	    }
	}
    }

    private void writeCouncilsLoadedInStatusBar() {
	final NewStatusBar statusBar = PluginServices.getMainFrame()
		.getStatusBar();
	String msg = constantsPanel.getStatusBarMsg();
	statusBar.setMessage("constants", msg);
    }
}
