package es.icarto.gvsig.extgex.navtable.decorators.printreports;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.HashMap;

import javax.imageio.ImageIO;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.hardcode.gdbms.driver.exceptions.InitializeDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.DateValue;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.StringValue;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;
import com.vividsolutions.jts.geom.Point;

import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.extgex.utils.retrievers.CultivosRetriever;
import es.icarto.gvsig.extgex.utils.retrievers.LocalizacionRetriever;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;

public class PrintReportsData implements JRDataSource {

    // Variables defined in jasper report template. To be calculated in real
    // time.

    private static final String JASPER_TIPOCULTIVO_OTROS = "tipo_cultivo_otros";
    private static final String JASPER_TIPOCULTIVO_VINHA = "tipo_cultivo_vinha";
    private static final String JASPER_TIPOCULTIVO_MONTE = "tipo_cultivo_monte";
    private static final String JASPER_TIPOCULTIVO_LABRADIO = "tipo_cultivo_labradio";
    private static final String JASPER_TIPOCULTIVO_PRADO = "tipo_cultivo_prado";
    private static final String JASPER_TIPOCULTIVO_INCULTO = "tipo_cultivo_inculto";
    private static final String JASPER_TIPOCULTIVO_TERRENO = "tipo_cultivo_terreno";
    private static final String JASPER_TIPOCULTIVO_EDIFICACION = "tipo_cultivo_edificacion";

    private static final String JASPER_ESCALA = "escala";
    private static final String JASPER_IMAGEFROMVIEW = "image_from_view";
    private static final int JASPER_IMAGEWIDTH = 246;
    private static final int JASPER_IMAGEHEIGHT = 163;

    private static final String JASPER_COORDENADA_UTM_Y = "coordenada_utm_y";
    private static final String JASPER_COORDENADA_UTM_X = "coordenada_utm_x";
    private static final String JASPER_LOCALIZACION_UC = "localizacion_uc";
    private static final String JASPER_LOCALIZACION_TRAMO = "localizacion_tramo";
    private static final String JASPER_LOCALIZACION_AYUNTAMIENTO = "localizacion_ayuntamiento";
    private static final String JASPER_LOCALIZACION_PARROQUIASUBTRAMO = "localizacion_parroquia_subtramo";

    private boolean isDataSourceReady = false;
    private int currentPosition = -1;
    private FLyrVect layer;
    private Point centroid = null;

    private HashMap<String, Object> values;
    private String fincaID = null;
    private DecimalFormat utmFormat;

    public PrintReportsData() {
    }

    public void prepareDataSource(String layerName, long currentPosition) {
	this.currentPosition = (int) currentPosition;
	TOCLayerManager toc = new TOCLayerManager();
	layer = toc.getLayerByName(layerName);
	utmFormat = new DecimalFormat("###,###,###,##0.000");
	prepareDataSource();
    }

    @Override
    public boolean next() throws JRException {
	// just need to print 1 register
	isDataSourceReady = !isDataSourceReady;
	return isDataSourceReady;
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
	return values.get(field.getName());
    }

    private void prepareDataSource() {
	values = new HashMap<String, Object>();
	try {
	    SelectableDataSource sds = layer.getRecordset();
	    Object value;
	    for (int index = 0; index < sds.getFieldCount(); index++) {
		if (java.sql.Types.INTEGER == sds.getFieldType(index)) {
		    value = sds.getFieldValue(currentPosition, index)
			    .toString();
		    if (!value.equals("")) {
			value = Integer.parseInt((String) value);
		    } else {
			value = null;
		    }
		} else if (java.sql.Types.DOUBLE == sds.getFieldType(index)) {
		    value = sds.getFieldValue(currentPosition, index)
			    .toString();
		    if (!value.equals("")) {
			value = Double.parseDouble((String) value);
		    } else {
			value = null;
		    }
		} else if (java.sql.Types.DATE == sds.getFieldType(index)) {
		    value = getDateValue(sds, index);
		} else {// string or anything else
		    value = sds.getFieldValue(currentPosition, index);
		}
		values.put(sds.getFieldName(index), value);
	    }
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	}

	// image
	values.put(JASPER_IMAGEFROMVIEW, getImageFromView());
	values.put(JASPER_ESCALA, getScaleFromView());

	// localizacion
	LocalizacionRetriever localizacion = new LocalizacionRetriever(
		getIDFinca());
	values.put(JASPER_LOCALIZACION_UC,
		localizacion.getValue(DBNames.FIELD_UC_FINCAS));
	values.put(JASPER_LOCALIZACION_TRAMO,
		localizacion.getValue(DBNames.FIELD_TRAMO_FINCAS));
	values.put(JASPER_LOCALIZACION_AYUNTAMIENTO,
		localizacion.getValue(DBNames.FIELD_AYUNTAMIENTO_FINCAS));
	values.put(JASPER_LOCALIZACION_PARROQUIASUBTRAMO,
		localizacion.getValue(DBNames.FIELD_PARROQUIASUBTRAMO_FINCAS));
	values.put(JASPER_COORDENADA_UTM_X, getCoordinateXFromView());
	values.put(JASPER_COORDENADA_UTM_Y, getCoordinateYFromView());

	// cultivos
	CultivosRetriever finca = new CultivosRetriever(getIDFinca());
	values.put(JASPER_TIPOCULTIVO_EDIFICACION,
		finca.hasCultivo(DBNames.VALUE_CULTIVOS_EDIFICACION));
	values.put(JASPER_TIPOCULTIVO_INCULTO,
		finca.hasCultivo(DBNames.VALUE_CULTIVOS_INCULTO));
	values.put(JASPER_TIPOCULTIVO_LABRADIO,
		finca.hasCultivo(DBNames.VALUE_CULTIVOS_LABRADIO));
	values.put(JASPER_TIPOCULTIVO_MONTE,
		finca.hasCultivo(DBNames.VALUE_CULTIVOS_MONTE));
	values.put(JASPER_TIPOCULTIVO_PRADO,
		finca.hasCultivo(DBNames.VALUE_CULTIVOS_PRADO));
	values.put(JASPER_TIPOCULTIVO_TERRENO,
		finca.hasCultivo(DBNames.VALUE_CULTIVOS_TERRENO));
	values.put(JASPER_TIPOCULTIVO_VINHA,
		finca.hasCultivo(DBNames.VALUE_CULTIVOS_VINHA));
	values.put(JASPER_TIPOCULTIVO_OTROS,
		finca.hasCultivo(DBNames.VALUE_CULTIVOS_OTROS));

    }

    private Date getDateValue(SelectableDataSource sds, int index)
	    throws ReadDriverException {
	Value val = sds.getFieldValue(currentPosition, index);
	if (val instanceof NullValue) {
	    return null;
	} else {
	    return ((DateValue) val).getValue();
	}
    }

    public String getIDFinca() {
	if (fincaID == null) {
	    int indexOfIDFinca = -1;
	    try {
		for (int i = 0; i < layer.getRecordset().getFieldCount(); i++) {
		    if (layer.getRecordset().getFieldName(i)
			    .equalsIgnoreCase(DBNames.FIELD_IDFINCA)) {
			indexOfIDFinca = i;
			break;
		    }
		}
		if (indexOfIDFinca != -1) {
		    fincaID = ((StringValue) layer.getRecordset()
			    .getFieldValue(currentPosition, indexOfIDFinca))
			    .getValue();
		}
		return fincaID;
	    } catch (ReadDriverException e) {
		e.printStackTrace();
		return fincaID;
	    }
	} else {
	    return fincaID;
	}
    }

    private String getCoordinateYFromView() {
	if (centroid == null) {
	    centroid = getCentroid();
	}
	return utmFormat.format(centroid.getY());
    }

    private String getCoordinateXFromView() {
	if (centroid == null) {
	    centroid = getCentroid();
	}
	return utmFormat.format(centroid.getX());
    }

    private Point getCentroid() {
	try {
	    IFeature feature = layer.getSource().getFeature(currentPosition);
	    return feature.getGeometry().toJTSGeometry().getCentroid();
	} catch (ExpansionFileReadException e) {
	    e.printStackTrace();
	    return null;
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    private Object getScaleFromView() {
	if (PluginServices.getMDIManager().getActiveWindow() instanceof BaseView) {
	    BaseView view = (BaseView) PluginServices.getMDIManager()
		    .getActiveWindow();
	    MapContext mapContext = view.getMapControl().getMapContext();
	    return Double.toString(mapContext.getScaleView());
	}
	return centroid;
    }

    private Object getImageFromView() {
	if (isGeometryNull()) {
	    return PluginServices.getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource("images/image-not-available.png");
	}
	BufferedImage bufferedImage = calculateImage();
	java.net.URL mapInReport = PluginServices
		.getPluginServices("es.icarto.gvsig.extgex").getClassLoader()
		.getResource("images/map-for-report.png");
	try {
	    if (bufferedImage != null) {
		ImageIO.write(bufferedImage, "png",
			new File(mapInReport.getFile()));
	    }
	    return mapInReport;
	} catch (IOException e) {
	    e.printStackTrace();
	    return PluginServices.getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource("images/image-not-available.png");
	} catch (NullPointerException npe) {
	    return PluginServices.getPluginServices("es.icarto.gvsig.extgex")
		    .getClassLoader()
		    .getResource("images/image-not-available.png");
	}
    }

    private BufferedImage calculateImage() {
	if (PluginServices.getMDIManager().getActiveWindow() instanceof BaseView) {
	    BaseView view = (BaseView) PluginServices.getMDIManager()
		    .getActiveWindow();
	    MapControl mapControl = view.getMapControl();
	    ViewPort vp = mapControl.getViewPort();
	    int widthImageFromJasperReport = JASPER_IMAGEWIDTH;
	    int heightImageFromJasperReport = JASPER_IMAGEHEIGHT;
	    int x = (vp.getImageWidth() / 2) - (widthImageFromJasperReport / 2);
	    int y = (vp.getImageHeight() / 2)
		    - (heightImageFromJasperReport / 2);
	    return mapControl.getImage().getSubimage(x, y,
		    widthImageFromJasperReport, heightImageFromJasperReport);
	}
	return null;
    }

    private boolean isGeometryNull() {
	ReadableVectorial source = layer.getSource();
	try {
	    source.start();
	    IGeometry g = source.getShape(Long.valueOf(currentPosition)
		    .intValue());
	    source.stop();
	    if (g == null) {
		return true;
	    }
	    return false;
	} catch (InitializeDriverException e) {
	    e.printStackTrace();
	    return true;
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	    return true;
	}
    }
}
