package es.icarto.gvsig.extgex;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

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
        AbstractForm dialog = getForm();
        if (dialog.init()) {
            PluginServices.getMDIManager().addWindow(dialog);
        }
    }

    @Override
    public boolean isEnabled() {
        return DBSession.isActive() && isViewActive() && isLayerLoaded();
    }
    
    private AbstractForm getForm() {
        TOCLayerManager toc = new TOCLayerManager();
        FLyrVect layer = toc.getLayerByName(getLayerName());
        
        AbstractForm dialog = new FormExpropiations(layer, null);
        return dialog;
    }

    @Override
    protected String getLayerName() {
        return isAmpliacion() ? FormExpropiations.TOCNAME_AMPLIACION : FormExpropiations.TOCNAME;
    }
    
    protected boolean isAmpliacion() {
        return false;
    }


}
