package es.icarto.gvsig.siga.extractvertexestool;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.commons.AbstractExtension;

public class ExtractVertexesExtension extends AbstractExtension {

    private ExtractVertexesGeoprocessPanel panel = null;

    @Override
    public void execute(String actionCommand) {
        updateGeoprocessPanel();
        ExtractVertexesPaneContainer container = new ExtractVertexesPaneContainer(panel);
        ExtractVertexesGeoprocess controller = new ExtractVertexesGeoprocess();
        controller.setView(panel);
        container.setCommand(controller);
        container.validate();
        container.repaint();
        PluginServices.getMDIManager().addWindow(container);
    }

    private void updateGeoprocessPanel() {
        FLayers layers = ((View) PluginServices.getMDIManager().getActiveWindow()).getModel().getMapContext()
                .getLayers();
        if (panel == null) {
            panel = new ExtractVertexesGeoprocessPanel(layers);
        } else {
            panel.updateLayers(layers);
        }
    }

    @Override
    public boolean isEnabled() {
        return isViewActive();
    }

}
