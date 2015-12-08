package es.icarto.gvsig.commons.referencing;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.cresques.geo.Projected;

import com.hardcode.gdbms.engine.spatial.fmap.FShapeGeneralPathX;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.Handler;

@SuppressWarnings("serial")
public class GShape implements FShape, Projected {

    private final IProjection proj;
    private final FShape shp;

    public GShape(IProjection proj, FShape shp) {
	this.proj = proj;
	this.shp = shp.cloneFShape();
    }

    public GShape(IProjection proj, Shape shp) {
	this.proj = proj;
	GeneralPathX gpx = new GeneralPathX(shp);

	// FLiteShape
	// FIXME
	this.shp = new FShapeGeneralPathX(gpx, -1);

    }

    @Override
    public Rectangle getBounds() {
	return shp.getBounds();
    }

    @Override
    public Rectangle2D getBounds2D() {
	return shp.getBounds2D();
    }

    @Override
    public boolean contains(double x, double y) {
	return shp.contains(x, y);
    }

    @Override
    public boolean contains(Point2D p) {
	return shp.contains(p);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
	return shp.intersects(x, y, w, h);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
	return shp.intersects(r);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
	return shp.contains(x, y, w, h);
    }

    @Override
    public boolean contains(Rectangle2D r) {
	return shp.contains(r);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
	return shp.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
	return shp.getPathIterator(at, flatness);
    }

    @Override
    public IProjection getProjection() {
	return this.proj;
    }

    @Override
    public int getShapeType() {
	return shp.getShapeType();
    }

    @Override
    public FShape cloneFShape() {
	return shp.cloneFShape();
    }

    @Override
    public void reProject(ICoordTrans ct) {
	// TODO
	shp.reProject(ct);
    }

    @Override
    public Handler[] getStretchingHandlers() {
	return shp.getStretchingHandlers();
    }

    @Override
    public Handler[] getSelectHandlers() {
	return shp.getSelectHandlers();
    }

    @Override
    public void transform(AffineTransform at) {
	shp.transform(at);
    }

}
