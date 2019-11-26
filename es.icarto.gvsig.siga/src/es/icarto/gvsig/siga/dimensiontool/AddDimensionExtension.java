package es.icarto.gvsig.siga.dimensiontool;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.project.documents.view.gui.View;

public class AddDimensionExtension extends Extension {

    @Override
    public void initialize() {
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
