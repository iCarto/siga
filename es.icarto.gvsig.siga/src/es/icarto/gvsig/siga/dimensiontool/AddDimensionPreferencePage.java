package es.icarto.gvsig.siga.dimensiontool;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import com.iver.andami.PluginServices;
import com.iver.andami.preferences.StoreException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.gui.cad.tools.SelectionCADTool;
import com.iver.cit.gvsig.gui.preferences.EditionPreferencePage;
import com.iver.cit.gvsig.project.documents.view.snapping.ISnapper;

public class AddDimensionPreferencePage extends EditionPreferencePage {

    @Override
    public void initializeValues() {
        List layersToSnap = new ArrayList();
        List<String> layersNames = AddDimension.getLayersToSnap();
        for (String name : layersNames) {
            FLayer lyr = layers.getLayer(name);
            if (lyr instanceof FLyrVect) {
                layersToSnap.add(lyr);
            }
        }
        initializeSnap(getJTableSnapping().getModel(), layersToSnap);
    }

    @Override
    protected boolean addFollowGeometryOption() {
        return false;
    }

    @Override
    protected boolean addDeleteButtonOption() {
        return false;
    }

    @Override
    public void storeValues() throws StoreException {
        TableModel tm = getJTableSnapping().getModel();
        List<String> layersToSnap = new ArrayList<String>();
        for (int i = 0; i < tm.getRowCount(); i++) {
            String layerName = (String) tm.getValueAt(i, 1);
            FLyrVect lyr = (FLyrVect) layers.getLayer(layerName);
            if (lyr != null) {
                Boolean bUseCache = (Boolean) tm.getValueAt(i, 0);
                Integer maxFeat = (Integer) tm.getValueAt(i, 2);

                // Decidimos si vamos a habilitar el spatialCache DESPUES, justo
                // antes de renderizar.
                // Necesitamos un método que explore las capas en edición y mire
                // las
                // capas sobre las
                // que se necestia el cache. Aquí lo que hacemos es añadir las
                // seleccionadas a la
                // lista de capas asociadas al snapping de los temas activos en
                // edición.
                // Lo del máximo de features en caché, tiene que ser para cada
                // capa
                // distinto. Pero no
                // puedes "chafar" el que ya hay, porque puedes fastidiar a otra
                // capa en edición.
                // Como máximo, lo que podemos hacer es que si es mayor al que
                // hay,
                // lo subimos. Si
                // se solicita uno menor, lo dejamos como está.
                // Otra opción sería no hacer caso de esto para cada capa, y
                // ponerlo
                // de forma global.
                // lyr.setSpatialCacheEnabled(bUseCache.booleanValue());
                lyr.setMaxFeaturesInEditionCache(maxFeat.intValue());
                if (bUseCache.booleanValue()) {
                    layersToSnap.add(layerName);
                }
            }
        }

        AddDimension.setLayersToSnap(layersToSnap);

        try {
            SelectionCADTool.tolerance = Integer.parseInt(getJTxtTolerance()
                    .getText());

        } catch (Exception e) {
            throw new StoreException(PluginServices.getText(this,
                    "tolerancia_incorrecta"), e);
        }
        mapContext.invalidate();
    }

    protected void deleteSnapper(String string) {
        // Nothing to do
    }

    protected void addSnapper(ISnapper snapper) {
        // Nothing to do
    }

    @Override
    public String getTitle() {
        return PluginServices.getText(this, "add_dimension_config_snappers");
    }
}
