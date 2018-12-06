package es.icarto.gvsig.extpm;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.commons.SingleLayerAbstractExtension;
import es.icarto.gvsig.extpm.forms.FormPM;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class FormPMExtension extends SingleLayerAbstractExtension {

    @Override
    public void execute(String actionCommand) {
        TOCLayerManager toc = new TOCLayerManager();
        FLyrVect layer = toc.getLayerByName(getLayerName());
        AbstractForm dialog = new FormPM(layer);
        if (dialog.init()) {
            PluginServices.getMDIManager().addWindow(dialog);
        }
    }

    @Override
    public boolean isEnabled() {
        return DBSession.isActive() && isViewActive() && isLayerLoaded();
    }


    @Override
    protected String getLayerName() {
        return FormPM.TOCNAME;
    }

    
}
