package es.icarto.gvsig.commons.referencing;

import java.awt.geom.Point2D;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.cresques.geo.Projected;

import com.iver.cit.gvsig.fmap.core.FPoint2D;

@SuppressWarnings("serial")
public class GPoint extends FPoint2D implements Projected {

    private final IProjection proj;

    public GPoint(IProjection proj, double x, double y) {
	this.proj = proj;
	p = new Point2D.Double(x, y);
    }

    public GPoint(IProjection proj, Point2D point) {
	this.proj = proj;
	p = new Point2D.Double(point.getX(), point.getY());
    }

    @Override
    public IProjection getProjection() {
	return proj;
    }

    @Override
    public void reProject(ICoordTrans ct) {
	super.reProject(ct);
    }

    public Point2D toProjection(ICoordTrans ct) {
	return ct.convert(p, null);
    }

}