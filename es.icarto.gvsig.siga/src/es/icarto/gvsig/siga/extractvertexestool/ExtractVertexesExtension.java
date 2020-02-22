package es.icarto.gvsig.siga.extractvertexestool;

import com.iver.andami.PluginServices;
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
        View view = getView();

        if (panel == null) {
            panel = new ExtractVertexesGeoprocessPanel(view);
        } else {
            panel.updateLayers(view);
        }
    }

    @Override
    public boolean isEnabled() {
        return isViewActive();
    }

}
