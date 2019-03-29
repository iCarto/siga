package es.icarto.gvsig.extgia;

import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.tools.Behavior.PointBehavior;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgia.forms.ViewPointListener;
import es.icarto.gvsig.extgia.images_in_view.SearchableLayers;

public class ImagesInViewExtension extends AbstractExtension {

    private final SearchableLayers searchableLayers = new SearchableLayers();

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
        listener.setLayers(searchableLayers.getLayers());
        mc.addMapTool(id, new PointBehavior(listener));

        mc.setTool(id);
    }

    @Override
    public boolean isEnabled() {
        View view = getView();
        if (view == null) {
            return false;
        }
        searchableLayers.search(view);
        return searchableLayers.layersFound();

    }

}
