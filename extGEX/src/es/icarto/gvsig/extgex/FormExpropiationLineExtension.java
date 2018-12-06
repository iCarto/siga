package es.icarto.gvsig.extgex;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.commons.SingleLayerAbstractExtension;
import es.icarto.gvsig.extgex.forms.FormExpropiationLine;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class FormExpropiationLineExtension extends SingleLayerAbstractExtension {

    @Override
    public void initialize() {
    }

    @Override
    public void execute(String actionCommand) {
        TOCLayerManager toc = new TOCLayerManager();
        FLyrVect layer = toc.getLayerByName(getLayerName());
        AbstractForm dialog = new FormExpropiationLine(layer, isAmpliacion());
        if (dialog.init()) {
            PluginServices.getMDIManager().addCentredWindow(dialog);
        }
    }

    
    @Override
    public boolean isEnabled() {
        return DBSession.isActive() && isViewActive() && isLayerLoaded();
    }

    
    protected boolean isAmpliacion() {
        return false;
    }

    @Override
    protected String getLayerName() {
        return isAmpliacion() ? FormExpropiationLine.TOCNAME_AMPLIACION : FormExpropiationLine.TOCNAME;
    }
    
}
