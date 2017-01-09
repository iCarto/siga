package es.icarto.gvsig.commons.referencing;

import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;

import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import es.icarto.gvsig.commons.db.ConnectionWrapper;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class PostgisTransformation {

    private static final Logger logger = Logger
	    .getLogger(PostgisTransformation.class);

    /**
     * Take care. This uses a custom row in spatial_ref_sys table to make the
     * reprojection insert into spatial_ref_sys (srid, auth_name, auth_srid,
     * srtext, proj4text) (select 111222, auth_name, auth_srid, srtext,
     * '+proj=utm +zone=29 +ellps=intl +units=m
     * towgs84=-125.098545,-76.000054,-156.198703,0.0,0.0,-1.129,8.30463103
     * +no_defs' from spatial_ref_sys where srid = 23029);
     */
    public GPoint transform(GPoint p, IProjection proj) {
	if (p.getProjection().getAbrev().equals(proj.getAbrev())) {
	    return p;
	}

	String projCode = proj.getAbrev().replace("EPSG:", "");
	String orgProjCode = p.getProjection().getAbrev().replace("EPSG:", "");
	orgProjCode = orgProjCode.equals("23029") ? "111222" : orgProjCode;

	String query = String
		.format("select st_x(a.geom), st_y(a.geom) from (select st_transform(st_geomfromtext('POINT( %s %s)',%s), %s) as geom) a",
			p.getX(), p.getY(), orgProjCode, projCode);
	ConnectionWrapper cw = new ConnectionWrapper(DBSession
		.getCurrentSession().getJavaConnection());
	DefaultTableModel table = cw.execute(query);
	if (table.getRowCount() != 1) {
	    logger.error(this.getClass().getName() + ":" + query);
	    NotificationManager.addWarning("Error transformando el punto");
	    return null;
	}
	double x = (Double) table.getValueAt(0, 0);
	double y = (Double) table.getValueAt(0, 1);

	return new GPoint(proj, x, y);
    }

    public GShape transform(GShape shape, IProjection proj) {
	if (shape.getProjection().getAbrev().equals(proj.getAbrev())) {
	    return shape;
	}

	String projCode = proj.getAbrev().replace("EPSG:", "");
	String orgProjCode = shape.getProjection().getAbrev()
		.replace("EPSG:", "");
	orgProjCode = orgProjCode.equals("23029") ? "111222" : orgProjCode;
	// PostGIS.sqlInsertFeature
	String geom = FConverter.java2d_to_jts(shape).toText();
	String query = String.format(
		"SELECT ST_AsText(ST_Tranform(ST_GeomFromText('%s', %s), %s))",
		geom, orgProjCode, projCode);

	ConnectionWrapper cw = new ConnectionWrapper(DBSession
		.getCurrentSession().getJavaConnection());
	DefaultTableModel table = cw.execute(query);
	if (table.getRowCount() != 1) {
	    logger.error(this.getClass().getName() + ":" + query);
	    NotificationManager.addWarning("Error transformando el punto");
	    return null;
	}
	try {
	    Geometry jtsGeom = new WKTReader().read(table.getValueAt(0, 0)
		    .toString());
	    return new GShape(proj, FConverter.jts_to_java2d(jtsGeom));
	} catch (ParseException e) {
	    logger.error(e.getStackTrace(), e);
	}

	return null;
    }

}
