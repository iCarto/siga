package es.icarto.gvsig.siga;

import com.iver.cit.gvsig.fmap.MapControl;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.commons.referencing.PostgisTransformation;
import es.icarto.gvsig.siga.locatorbycoords.LocatorByCoordsDialog;
import es.icarto.gvsig.siga.locatorbycoords.LocatorByCoordsModel;
import es.udc.cartolab.gvsig.elle.constants.ZoomTo;

public class LocatorByCoordsExtension extends AbstractExtension {

    @Override
    public void execute(String actionCommand) {
	LocatorByCoordsModel model = new LocatorByCoordsModel();
	final MapControl mapControl = getView().getMapControl();

	final ZoomTo zoomTo = new ZoomTo(mapControl);
	zoomTo.setTranformation(new PostgisTransformation());
	model.setZoomTo(zoomTo);
	model.setDefaultOuputProj(mapControl.getProjection());

	LocatorByCoordsDialog pane = new LocatorByCoordsDialog(model);
	pane.openDialog();
    }

    @Override
    public boolean isEnabled() {
        return isViewActive();
    }

}
