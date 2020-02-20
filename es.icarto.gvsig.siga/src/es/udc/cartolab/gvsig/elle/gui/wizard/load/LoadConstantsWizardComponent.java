package es.udc.cartolab.gvsig.elle.gui.wizard.load;

import java.awt.BorderLayout;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiFrame.NewStatusBar;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.ProjectView;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.commons.utils.StrUtils;
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

    private static final Logger logger = Logger.getLogger(LoadConstantsWizardComponent.class);

    private boolean reload = false;

    private ConstantsPanel constantsPanel;

    public final static String PROPERTY_VIEW = "view";

    public LoadConstantsWizardComponent(Map<String, Object> properties, boolean reload) {
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
        final Collection<String> tablesAffectedByConstant = constantsPanel.getTablesAffectedByConstant();
        if (reload) {
            List<ELLEMap> loadedMaps = MapDAO.getInstance().getLoadedMaps();
            for (ELLEMap m : loadedMaps) {
                if (m.getView().equals(view) && m.getName().equals(view.getModel().getName())) {
                    map = m;
                }
            }

            // TODO. Que hacer si map es null aquí? Por ahora nos contentamos
            // con algo parecido al Null Object Pattern para evitar chequeos de
            // null sobre map

            map = map != null ? map : new ELLEMap(null, null);
            map.setWhereOnAllLayers(where);
            map.setWhereOnAllOverviewLayers(where);
            setWhereOnLoc(map);
            new ConstantReload(view, where, tablesAffectedByConstant, constantsPanel.buildWhereForLoc());
        } else {
            Object tmp = properties.get(SigaLoadMapWizardComponent.PROPERTY_MAP_NAME);
            String mapName = (tmp == null ? "" : tmp.toString());

            try {
                map = MapDAO.getInstance().getMap(view, mapName);
                map.setWhereOnAllLayers(where);
                map.setWhereOnAllOverviewLayers(where);
                setWhereOnLoc(map);
                setWhereOnExpropiacionLayers(map);

                map.load(view.getProjection(), tablesAffectedByConstant);

                loadLegends(view, mapName);

                if (view.getModel().getName().equals("ELLE View") && (view.getModel() instanceof ProjectView)) {
                    ((ProjectView) view.getModel()).setName(mapName);
                }

                ZoomTo zoomTo = new ZoomTo(view.getMapControl());
                zoomTo.zoom(constantsPanel.getZoomGeometry());
            } catch (Exception e) {
                logger.error(e.getStackTrace(), e);
                throw new WizardException(e);
            }

        }

        writeCouncilsLoadedInStatusBar();

    }

    private void setWhereOnExpropiacionLayers(ELLEMap map) {
        LayerProperties lp = map.getLayer("Fincas");
        if (lp != null) {
            if (StrUtils.isEmptyString(lp.getWhere())) {
                lp.setWhere("WHERE tramo NOT IN ('13', '14', '15', '16')");
            } else {
                lp.setWhere(lp.getWhere() + " AND tramo NOT IN ('13', '14', '15', '16')");
            }
        }

        lp = map.getLayer("Fincas_Ampliacion");
        if (lp != null) {
            if (StrUtils.isEmptyString(lp.getWhere())) {
                lp.setWhere("WHERE tramo IN ('13', '14')");
            } else {
                lp.setWhere(lp.getWhere() + " AND tramo IN ('13', '14')");
            }
        }

        lp = map.getLayer("Fincas_Autoestradas");
        if (lp != null) {
            if (StrUtils.isEmptyString(lp.getWhere())) {
                lp.setWhere("WHERE tramo IN ('15', '16')");
            } else {
                lp.setWhere(lp.getWhere() + " AND tramo IN ('15', '16')");
            }
        }

        lp = map.getLayer("Linea_Expropiacion");
        if (lp != null) {
            if (StrUtils.isEmptyString(lp.getWhere())) {
                lp.setWhere("WHERE NOT ampliacion");
            } else {
                lp.setWhere(lp.getWhere() + " AND NOT ampliacion");
            }
        }

        lp = map.getLayer("Linea_Expropiacion_Ampliacion");
        if (lp != null) {
            if (StrUtils.isEmptyString(lp.getWhere())) {
                lp.setWhere("WHERE ampliacion");
            } else {
                lp.setWhere(lp.getWhere() + " AND ampliacion");
            }
        }
    }

    private void setWhereOnLoc(ELLEMap map) {
        String where = constantsPanel.buildWhereForLoc();

        LayerProperties lp = map.getOverviewLayer("Provincias_galicia_loc");
        if (lp != null) {
            lp.setWhere(where);
        }

        lp = map.getOverviewLayer("Autopistas_loc");
        if (lp != null) {
            lp.setWhere(where);
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

    private void loadLegends(FLayers layers, boolean overview, String mapName) throws SQLException, IOException {
        for (int i = 0; i < layers.getLayersCount(); i++) {
            FLayer layer = layers.getLayer(i);
            if (layer instanceof FLyrVect) {

                LoadLegend.loadLegend((FLyrVect) layer, mapName, overview, LoadLegend.DB_LEGEND);
            } else if (layer instanceof FLayers) {
                loadLegends((FLayers) layer, overview, mapName);
            }
        }
    }

    private void writeCouncilsLoadedInStatusBar() {
        final NewStatusBar statusBar = PluginServices.getMainFrame().getStatusBar();
        String msg = constantsPanel.getStatusBarMsg();
        statusBar.setMessage("constants", msg);
    }
}
