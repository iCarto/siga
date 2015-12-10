package es.icarto.gvsig.siga;

import com.iver.cit.gvsig.fmap.MapControl;

import es.icarto.gvsig.commons.referencing.PostgisTransformation;
import es.icarto.gvsig.siga.gotoextension.GoToDialog;
import es.icarto.gvsig.siga.gotoextension.GoToModel;
import es.udc.cartolab.gvsig.elle.constants.ZoomTo;

public class GoToExtension extends AbstractExtension {

    @Override
    public void execute(String actionCommand) {
	GoToModel model = new GoToModel();
	final MapControl mapControl = getView().getMapControl();

	final ZoomTo zoomTo = new ZoomTo(mapControl);
	zoomTo.setTranformation(new PostgisTransformation());
	model.setZoomTo(zoomTo);
	model.setDefaultOuputProj(mapControl.getProjection());

	GoToDialog pane = new GoToDialog(model);
	pane.openDialog();
    }

    @Override
    public boolean isEnabled() {
	return getView() != null;
    }

}
