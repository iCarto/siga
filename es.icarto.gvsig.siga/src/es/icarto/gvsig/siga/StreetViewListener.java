package es.icarto.gvsig.siga;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.crs.CRSFactory;
import com.iver.cit.gvsig.fmap.tools.BehaviorException;
import com.iver.cit.gvsig.fmap.tools.PointSelectionListener;
import com.iver.cit.gvsig.fmap.tools.Events.PointEvent;

import es.icarto.gvsig.commons.referencing.GPoint;
import es.icarto.gvsig.commons.referencing.PostgisTransformation;
import es.icarto.gvsig.utils.DesktopApi;

public class StreetViewListener extends PointSelectionListener {

    private static final Logger logger = Logger
	    .getLogger(StreetViewListener.class);

    private final PostgisTransformation transformation;
    private final IProjection EPSG4326;

    public StreetViewListener(MapControl mc) {
	super(mc);
	transformation = new PostgisTransformation();
	EPSG4326 = CRSFactory.getCRS("EPSG:4326");
    }

    /**
     * The image to display when the cursor is active.
     */
    private final Image img = PluginServices.getIconTheme()
	    .get(OpenStreetViewExtension.KEY + "-cursor").getImage();

    /**
     * The cursor used to work with this tool listener.
     *
     * @see #getCursor()
     */
    private final Cursor cur = Toolkit.getDefaultToolkit().createCustomCursor(
	    img, new Point(16, 16), "");

    @Override
    public Cursor getCursor() {
	return cur;
    }

    @Override
    public void point(PointEvent event) throws BehaviorException {

	// Tolerancia de 3 pixels
	Point2D p = event.getPoint();
	final Point2D tmpPoint = mapCtrl.getViewPort().toMapPoint(
		(int) p.getX(), (int) p.getY());
	GPoint mapPoint = new GPoint(mapCtrl.getProjection(), tmpPoint);

	try {
	    PluginServices.getMDIManager().setWaitCursor();
	    // final double tol = mapCtrl.getViewPort().toMapDistance(3);
	    // lookForClosestPointFeature(mapPoint, tol);
	    openInPoint(mapPoint);
	} catch (URISyntaxException e) {
	    logger.error(e.getStackTrace(), e);
	} finally {
	    PluginServices.getMDIManager().restoreCursor();
	}

    }

    private void openInPoint(GPoint mapPoint) throws URISyntaxException {
	GPoint latLng = transformation.transform(mapPoint, EPSG4326);
	if (latLng == null) {
	    return;
	}
	// http://stackoverflow.com/a/542965/930271
	String baseUri = "http://maps.google.com/maps?q=&layer=c&cbll=%s,%s";
	String uri = String.format(baseUri, latLng.getY(), latLng.getX());

	DesktopApi.browse(new URI(uri));
    }

    @Override
    public void pointDoubleClick(PointEvent event) throws BehaviorException {
    }

}
