package es.icarto.gvsig.extgex.navtable.decorators.printreports;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;

import javax.imageio.ImageIO;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.StringValue;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;
import com.vividsolutions.jts.geom.Point;

import es.icarto.gvsig.extgex.forms.expropiations.ExpropiationsLayerResolver;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.siga.models.InfoEmpresa;

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

        Point centroid = getCentroid();

        // image
        values.put(JASPER_IMAGEFROMVIEW, getImageFromView(centroid));
        values.put(JASPER_ESCALA, getScaleFromView(centroid));

        // values.put(JASPER_COORDENADA_UTM_X, getCoordinateXFromView(centroid));
        // values.put(JASPER_COORDENADA_UTM_Y, getCoordinateYFromView(centroid));
        values.put(JASPER_COORDENADA_UTM_X, getX(centroid));
        values.put(JASPER_COORDENADA_UTM_Y, getY(centroid));

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

    private String getCoordinateYFromView(Point centroid) {
        if (centroid == null) {
            return null;
        }
        return utmFormat.format(centroid.getY());
    }

    private String getCoordinateXFromView(Point centroid) {
        if (centroid == null) {
            return null;
        }
        return utmFormat.format(centroid.getX());
    }

    private Double getX(Point centroid) {
        if (centroid == null) {
            return null;
        }
        return centroid.getX();
    }

    private Double getY(Point centroid) {
        if (centroid == null) {
            return null;
        }
        return centroid.getY();
    }

    private Point getCentroid() {
        try {
            IFeature feature = layer.getSource().getFeature(currentPosition);
            IGeometry geometry = feature.getGeometry();
            if (geometry == null) {
                return null;
            }
            return geometry.toJTSGeometry().getCentroid();
        } catch (ExpansionFileReadException e) {
            e.printStackTrace();
            return null;
        } catch (ReadDriverException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getScaleFromView(Point centroid) {
        BaseView view = getView();
        if (view == null || centroid == null) {
            return null;
        }
        MapContext mapContext = view.getMapControl().getMapContext();
        return Double.toString(mapContext.getScaleView());
    }

    private URL getImageFromView(Point centroid) {
        if (centroid == null) {
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

    private BaseView getView() {
        IWindow window = PluginServices.getMDIManager().getActiveWindow();
        if (!(window instanceof BaseView)) {
            return null;
        }
        return (BaseView) window;
    }

    private BufferedImage calculateImage() {
        BaseView view = getView();
        if (view == null) {
            return null;
        }
        MapControl mapControl = view.getMapControl();

        ViewPort vp = mapControl.getViewPort();
        int x = (vp.getImageWidth() / 2) - (JASPER_IMAGEWIDTH / 2);
        int y = (vp.getImageHeight() / 2) - (JASPER_IMAGEHEIGHT / 2);
        return mapControl.getImage().getSubimage(x, y, JASPER_IMAGEWIDTH, JASPER_IMAGEHEIGHT);
    }

    public String getLogoUrl() {
        String idTramo = getIDFinca().substring(0, 2);
        InfoEmpresa infoEmpresa = ExpropiationsLayerResolver.getInfoEmpresa(layer);
        String logoUrl = infoEmpresa.getReportLogo(idTramo);
        return logoUrl;
    }

    public String getReportName() {
        String idTramo = getIDFinca().substring(0, 2);
        InfoEmpresa infoEmpresa = ExpropiationsLayerResolver.getInfoEmpresa(layer);
        String reportName = infoEmpresa.getReportName(idTramo);
        return reportName;
    }
}
