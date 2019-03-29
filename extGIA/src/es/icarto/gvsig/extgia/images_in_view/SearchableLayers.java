package es.icarto.gvsig.extgia.images_in_view;

import java.util.ArrayList;
import java.util.List;

import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.extgia.preferences.Elements;

public class SearchableLayers {
    private final List<FLyrVect> searchableLayers = new ArrayList<FLyrVect>();

    public void search(View view) {
        FLayers layers = view.getMapControl().getMapContext().getLayers();
        FLayer[] actives = layers.getActives();
        searchableLayers.clear();
        for (FLayer layer : actives) {
            if (layer instanceof FLyrVect) {
                Elements e = isGIALayerName(layer.getName());
                if ((e != null) && (e.imagenesTableName != null)) {
                    searchableLayers.add((FLyrVect) layer);
                }
            }
        }

    }

    private Elements isGIALayerName(String layerName) {
        for (Elements e : Elements.values()) {
            if (e.layerName.equals(layerName)) {
                return e;
            }
        }
        return null;
    }

    public boolean layersFound() {
        return searchableLayers.size() > 0;
    }

    public List<FLyrVect> getLayers() {
        return searchableLayers;
    }

}
