package es.icarto.gvsig.siga.extractvertexestool;

import es.icarto.gvsig.commons.AbstractExtension;

public class ExtractVertexesExtension extends AbstractExtension {

    @Override
    public void execute(String actionCommand) {
        ExtractVertexesGeoprocessPanel panel = new ExtractVertexesGeoprocessPanel(getView());
        ExtractVertexesPaneContainer container = new ExtractVertexesPaneContainer(panel);
        ExtractVertexesGeoprocess controller = new ExtractVertexesGeoprocess();
        controller.setView(panel);
        container.setCommand(controller);
        container.openDialog();
    }

    @Override
    public boolean isEnabled() {
        return isViewActive();
    }

}
