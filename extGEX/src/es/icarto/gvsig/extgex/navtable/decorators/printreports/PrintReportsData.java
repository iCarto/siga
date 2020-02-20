package es.icarto.gvsig.extgex.navtable.decorators.printreports;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

import javax.imageio.ImageIO;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.hardcode.gdbms.driver.exceptions.InitializeDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.StringValue;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;
import com.vividsolutions.jts.geom.Point;

import es.icarto.gvsig.extgex.forms.expropiations.ExpropiationsLayerResolver;
import es.icarto.gvsig.extgex.preferences.DBNames;

public class PrintReportsData implements JRDataSource {

    // Variables defined in jasper report template. To be calculated in real
    // time.

    private static final String JASPER_ESCALA = "escala";
    private static final String JASPER_IMAGEFROMVIEW = "image_from_view";
    private static final int JASPER_IMAGEWIDTH = 250;
    private static final int JASPER_IMAGEHEIGHT = 170;

    private static final String JASPER_COORDENADA_UTM_Y = "coordenada_utm_y";
    private static final String JASPER_COORDENADA_UTM_X = "coordenada_utm_x";

    private boolean isDataSourceReady = false;
    private int currentPosition = -1;
    private final FLyrVect layer;
    private Point centroid = null;

    private HashMap<String, Object> values;
    private String fincaID = null;
    private DecimalFormat utmFormat;

    public PrintReportsData(FLyrVect layer) {
        this.layer = layer;
    }

    public void prepareDataSource(long currentPosition) {
        this.currentPosition = (int) currentPosition;
        utmFormat = new DecimalFormat("###,###,###,##0.000");
        prepareDataSource();
    }

    @Override
    public boolean next() throws JRException {
        // just need to print 1 register
        isDataSourceReady = !isDataSourceReady;
        return isDataSourceReady;
    }

    public Object getValue(String name) {
        return values.get(name);
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        return values.get(field.getName());
    }

    private void prepareDataSource() {
        values = new HashMap<String, Object>();

        // image
        values.put(JASPER_IMAGEFROMVIEW, getImageFromView());
        values.put(JASPER_ESCALA, getScaleFromView());

        // values.put(JASPER_COORDENADA_UTM_X, getCoordinateXFromView());
        // values.put(JASPER_COORDENADA_UTM_Y, getCoordinateYFromView());
        values.put(JASPER_COORDENADA_UTM_X, getX());
        values.put(JASPER_COORDENADA_UTM_Y, getY());

    }

    public String getIDFinca() {
        if (fincaID == null) {
            int indexOfIDFinca = -1;
            try {
                for (int i = 0; i < layer.getRecordset().getFieldCount(); i++) {
                    if (layer.getRecordset().getFieldName(i).equalsIgnoreCase(DBNames.FIELD_IDFINCA)) {
                        indexOfIDFinca = i;
                        break;
                    }
                }
                if (indexOfIDFinca != -1) {
                    fincaID = ((StringValue) layer.getRecordset().getFieldValue(currentPosition, indexOfIDFinca))
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

    private double getX() {
        if (centroid == null) {
            centroid = getCentroid();
        }
        return centroid.getX();
    }

    private double getY() {
        if (centroid == null) {
            centroid = getCentroid();
        }
        return centroid.getY();
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
            BaseView view = (BaseView) PluginServices.getMDIManager().getActiveWindow();
            MapContext mapContext = view.getMapControl().getMapContext();
            return Double.toString(mapContext.getScaleView());
        }
        return centroid;
    }

    private Object getImageFromView() {
        if (isGeometryNull()) {
            return PluginServices.getPluginServices("es.icarto.gvsig.extgex").getClassLoader()
                    .getResource("images/image-not-available.png");
        }
        BufferedImage bufferedImage = calculateImage();
        java.net.URL mapInReport = PluginServices.getPluginServices("es.icarto.gvsig.extgex").getClassLoader()
                .getResource("images/map-for-report.png");
        try {
            if (bufferedImage != null) {
                ImageIO.write(bufferedImage, "png", new File(mapInReport.getFile()));
            }
            return mapInReport;
        } catch (IOException e) {
            e.printStackTrace();
            return PluginServices.getPluginServices("es.icarto.gvsig.extgex").getClassLoader()
                    .getResource("images/image-not-available.png");
        } catch (NullPointerException npe) {
            return PluginServices.getPluginServices("es.icarto.gvsig.extgex").getClassLoader()
                    .getResource("images/image-not-available.png");
        }
    }

    private BufferedImage calculateImage() {
        if (!(PluginServices.getMDIManager().getActiveWindow() instanceof BaseView)) {
            return null;
        }
        BaseView view = (BaseView) PluginServices.getMDIManager().getActiveWindow();
        MapControl mapControl = view.getMapControl();

        ViewPort vp = mapControl.getViewPort();
        int x = (vp.getImageWidth() / 2) - (JASPER_IMAGEWIDTH / 2);
        int y = (vp.getImageHeight() / 2) - (JASPER_IMAGEHEIGHT / 2);
        return mapControl.getImage().getSubimage(x, y, JASPER_IMAGEWIDTH, JASPER_IMAGEHEIGHT);
    }

    private boolean isGeometryNull() {
        ReadableVectorial source = layer.getSource();
        try {
            source.start();
            IGeometry g = source.getShape(Long.valueOf(currentPosition).intValue());
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

    public String getLogoUrl() {
        String idFinca = getIDFinca();
        String idTramo = idFinca.substring(0, 2);
        String logoPath = ExpropiationsLayerResolver.getInfoEmpresa(layer).getReportLogo(idTramo);
        return logoPath;
    }
}
