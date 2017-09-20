package es.icarto.gvsig.extgia;

import java.util.ArrayList;
import java.util.List;

import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.tools.Behavior.PointBehavior;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgia.forms.ViewPointListener;
import es.icarto.gvsig.extgia.preferences.Elements;

public class ImagesInViewExtension extends AbstractExtension {

    private final List<FLyrVect> searchableLayers = new ArrayList<FLyrVect>();

    @Override
    public void initialize() {
        super.initialize();
        registerCursor();
    }

    @Override
    public void execute(String actionCommand) {

        View view = getView();

        MapControl mc = view.getMapControl();

        /*
         * Aqui podriamos jugar a hacer un setListener en PointBehavior
         * en lugar de hacer esto, o que se gestiona como field y que como
         * variable. O el clasico if (!mc.getNamesMapTools().containsKey(id)) {
         */
        ViewPointListener listener = new ViewPointListener(mc);
        listener.setCursorImage(id + ".cursor");
        listener.setLayers(searchableLayers);
        mc.addMapTool(id, new PointBehavior(listener));

        mc.setTool(id);
    }

    private Elements isGIALayerName(String layerName) {
        for (Elements e : Elements.values()) {
            if (e.layerName.equals(layerName)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        View view = getView();
        if (view == null) {
            return false;
        }

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
        return searchableLayers.size() > 0;
    }

}
