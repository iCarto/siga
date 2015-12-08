package es.icarto.gvsig.siga;

import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.MapControl;

import es.icarto.gvsig.commons.referencing.GShape;
import es.icarto.gvsig.commons.referencing.PostgisTransformation;
import es.icarto.gvsig.siga.gotoextension.GoToDialog;
import es.icarto.gvsig.siga.gotoextension.GoToModel;
import es.udc.cartolab.gvsig.elle.constants.ZoomTo;

public class GoToExtension extends AbstractExtension {

    private static final Logger logger = Logger.getLogger(GoToExtension.class);

    @Override
    public void execute(String actionCommand) {
	GoToModel model = new GoToModel();
	final MapControl mapControl = getView().getMapControl();

	final ZoomTo zoomTo = new ZoomTo(mapControl);
	zoomTo.setTranformation(new PostgisTransformation());
	model.setZoomTo(zoomTo);
	model.setDefaultInputProj(GoToModel.projCodes[0]);
	model.setDefaultOuputProj(GoToModel.projCodes[1]);

	Rectangle2D fullExtent = null;

	try {
	    fullExtent = mapControl.getMapContext().getFullExtent();
	    new GShape(GoToModel.projCodes[0].getProj(), fullExtent);
	    GoToDialog pane = new GoToDialog(model);
	    pane.openDialog();
	} catch (ReadDriverException e) {
	    logger.error(e.getStackTrace(), e);
	}

    }

    @Override
    public boolean isEnabled() {
	return getView() != null;
    }

}
