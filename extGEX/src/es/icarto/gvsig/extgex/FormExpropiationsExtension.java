package es.icarto.gvsig.extgex;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.commons.SingleLayerAbstractExtension;
import es.icarto.gvsig.extgex.forms.expropiations.FormExpropiations;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class FormExpropiationsExtension extends SingleLayerAbstractExtension {

    public static final Object TOOLBAR_NAME = "extgex.formularios";

    @Override
    public void execute(String actionCommand) {
        DBSession.getCurrentSession().setSchema(DBNames.EXPROPIATIONS_SCHEMA);
        FLyrVect layer = getLayer();
        AbstractForm dialog = new FormExpropiations(layer, null);
        if (dialog.init()) {
            PluginServices.getMDIManager().addWindow(dialog);
        }
    }

    private FLyrVect getLayer() {
        TOCLayerManager toc = new TOCLayerManager();
        return toc.getLayerByName(getLayerName());
    }

   
    @Override
    public boolean isEnabled() {
        return DBSession.isActive() && isViewActive() && isLayerLoaded();
    }

    @Override
    protected String getLayerName() {
        return isAmpliacion() ? null : DBNames.LAYER_FINCAS;
    }
    
    protected boolean isAmpliacion() {
        return false;
    }


}
