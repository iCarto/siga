package es.icarto.gvsig.extgex;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgex.slopes_and_curves.SlopesAndCurvesForm;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class SlopesAndCurvesExtension extends AbstractExtension {

    @Override
    public void execute(String actionCommand) {
        SlopesAndCurvesForm form = new SlopesAndCurvesForm();
        form.openDialog();
        form.setOnRightUpperCorner();
    }

    @Override
    public boolean isEnabled() {
        return DBSession.isActive() && isViewActive();
    }
}
