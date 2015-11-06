package es.udc.cartolab.gvsig.elle.constants;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;

public class Constant {

    private static final Logger logger = Logger.getLogger(Constant.class);

    private final static String CONSTANTS_ZOOM_LAYER_FIELD = "municipio_codigo";
    private final static String CONSTANTS_ZOOM_LAYER_NAME = "Constante";

    private final List<String> values;
    private final FLyrVect layer;

    public Constant(List<String> values) {
	this.values = values;

	// Debería lanzar una excepción si la capa no está disponible o la capa
	// debería cargarse más tarde, pero implica cambios que no pueden
	// hacerse ahora
	TOCLayerManager tocManager = new TOCLayerManager();
	this.layer = tocManager.getLayerByName(CONSTANTS_ZOOM_LAYER_NAME);
    }

    private class PositionRetriever implements IPositionRetriever {

	private final FLyrVect l;
	private final int p;

	public PositionRetriever(FLyrVect l, int p) {
	    this.l = l;
	    this.p = p;
	}

	@Override
	public int getPosition() {
	    return p;
	}

	@Override
	public FLyrVect getLayer() {
	    return l;
	}

    }

    // compareIgnoreCase lo he quitado, cuidado de no introducir bugs
    private List<IPositionRetriever> getPositionOnEnvelope(FLyrVect layer,
	    List<String> values) {
	List<IPositionRetriever> list = new ArrayList<IPositionRetriever>();
	List<String> valueList = new ArrayList<String>(values);

	try {
	    SelectableDataSource ds = layer.getRecordset();
	    int index = ds.getFieldIndexByName(CONSTANTS_ZOOM_LAYER_FIELD);
	    for (int i = 0; i < ds.getRowCount() && !valueList.isEmpty(); i++) {
		Value value = ds.getFieldValue(i, index);
		String stringValue = value.toString();

		if (valueList.contains(stringValue)) {
		    list.add(new PositionRetriever(layer, i));
		    valueList.remove(valueList);
		}

	    }
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	}

	return list;
    }

    public Rectangle2D getGeometry() {
	if (layer == null) {
	    return null;
	}
	if (values.isEmpty()) {
	    try {
		return layer.getFullExtent();
	    } catch (ReadDriverException e) {
		logger.error(e.getStackTrace(), e);
		return null;
	    }
	}

	Rectangle2D rectangle = null;

	try {
	    List<IPositionRetriever> posRetrieverList = getPositionOnEnvelope(
		    layer, values);
	    ReadableVectorial source = (layer).getSource();
	    source.start();

	    for (IPositionRetriever posRetriever : posRetrieverList) {
		IGeometry g = source.getShape(posRetriever.getPosition());
		if (layer.getCoordTrans() != null) {
		    g.reProject(layer.getCoordTrans());
		}

		if (rectangle == null) {
		    rectangle = g.getBounds2D();
		} else {
		    rectangle.add(g.getBounds2D());
		}
	    }

	    source.stop();

	    // rectangle = g.getBounds2D();
	    // if (rectangle.getWidth() < 200) {
	    // rectangle.setFrameFromCenter(rectangle.getCenterX(),
	    // rectangle.getCenterY(), rectangle.getCenterX() + 100,
	    // rectangle.getCenterY() + 100);
	    // }
	} catch (ReadDriverException e) {
	    logger.error(e.getStackTrace(), e);
	}
	return rectangle;
    }

}
