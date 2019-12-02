package es.icarto.gvsig.siga.dimensiontool;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.commons.AbstractExtension;

public class AddDimensionExtension extends AbstractExtension {

    @Override
    public void initialize() {
        super.initialize();
        CADExtension
                .addCADTool(AddDimensionCADTool.CAD_ID, new AddDimensionCADTool());
    }

    @Override
    public void execute(String actionCommand) {
        new AddDimension().execute();
    }

    @Override
    public boolean isEnabled() {
        return (PluginServices.getMDIManager().getActiveWindow() instanceof View && (CADExtension
                .getEditionManager().getActiveLayerEdited() == null || AddDimension
                .isExtensionActive()));
    }

    @Override
    public boolean isVisible() {
        return true;
    }

}
