package es.icarto.gvsig.siga.dimensiontool;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cresques.cts.IProjection;
import org.gvsig.symbology.fmap.labeling.ExtendedLabelingFactory;
import org.gvsig.symbology.fmap.labeling.GeneralLabelingStrategy;
import org.gvsig.symbology.fmap.labeling.placements.LinePlacementConstraints;

import com.hardcode.gdbms.driver.exceptions.InitializeDriverException;
import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.OpenDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.styles.ArrowDecoratorStyle;
import com.iver.cit.gvsig.fmap.core.styles.SimpleLineStyle;
import com.iver.cit.gvsig.fmap.core.symbols.ArrowMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleLineSymbol;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.shp.IndexedShpDriver;
import com.iver.cit.gvsig.fmap.edition.writers.shp.ShpWriter;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerEvent;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.rendering.SingleSymbolLegend;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.DefaultLabelingMethod;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelClass;
import com.iver.cit.gvsig.gui.tokenmarker.ConsoleToken;
import com.iver.cit.gvsig.layers.VectorialLayerEdited;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.cit.gvsig.project.documents.view.snapping.VectorialLayerSnapping;
import com.iver.utiles.console.jedit.KeywordMap;

public class AddDimension {

    private static final String LAYER_NAME = "COTAS";
    private static final String LAYER_FILE_PREFIX = "SIGA_COTAS";
    private static final String GID_FIELD = "GID";
    private static final String LENGTH_FIELD = "LENGTH";
    private static final String LABEL_FIELD = "LABEL";
    private static final List<String> SNAPPERS_TO_IGNORE = Arrays.asList(
            "INFRAESTRUCTURAS", "HIDROGRAFIA", "BASE", "EXTERIOR", "COTAS");

    private static Boolean EXTENSION_ACTIVE = false;
    private static FLyrVect DIMENSIONS_LAYER;
    private static List<String> SNAP_LAYERS;

    public void execute() {
        IWindow window = PluginServices.getMDIManager().getActiveWindow();
        if (window instanceof View) {
            if (EXTENSION_ACTIVE
                    && CADExtension.getCADTool() instanceof AddDimensionCADTool) {
                stopEdition();
            } else {
                View view = (View) window;
                MapControl mc = view.getMapControl();
                FLayer layer = mc.getMapContext().getLayers()
                        .getLayer(LAYER_NAME);
                if (layer == null) {
                    try {
                        layer = createTempLayer(view.getProjection());
                        FLayers layers = mc.getMapContext().getLayers();
                        layers.addLayer(layers.getLayersCount(), layer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (!(layer instanceof FLyrVect)) {
                    return;
                }
                DIMENSIONS_LAYER = (FLyrVect) layer;
                startEdition(view, DIMENSIONS_LAYER);
            }
        }
    }

    public static List<String> getDefaultSnappingLayers() {
        List<String> layerNames = new ArrayList<String>();
        IWindow window = PluginServices.getMDIManager().getActiveWindow();
        if (window instanceof View) {
            checkDefaultSnappingLayer(layerNames, ((View) window)
                    .getMapControl().getMapContext().getLayers());
        }
        return layerNames;
    }

    private static void checkDefaultSnappingLayer(List<String> layerNames,
            FLayer layer) {
        if (SNAPPERS_TO_IGNORE.indexOf(layer.getName()) != -1) {
            return;
        }
        if (layer instanceof FLayers) {
            FLayers layers = (FLayers) layer;
            for (int i = 0, iLen = layers.getLayersCount(); i < iLen; i++) {
                checkDefaultSnappingLayer(layerNames, layers.getLayer(i));
            }
        } else if (layer instanceof FLyrVect) {
            layerNames.add(layer.getName());
        }
    }

    private void startEdition(View view, FLyrVect layer) {
        try {
            EXTENSION_ACTIVE = true;

            // Set up CAD Tools¡
            MapControl mc = view.getMapControl();
            DIMENSIONS_LAYER.setEditing(true);
            CADExtension.getEditionManager().editionChanged(
                    LayerEvent.createEditionChangedEvent(DIMENSIONS_LAYER,
                            "edition"));
            DIMENSIONS_LAYER.setEditing(false);
            view.showConsole();
            CADExtension.setCADTool(AddDimensionCADTool.CAD_ID, true);
            ((AddDimensionCADTool) CADExtension.getCADTool())
                    .setDimensionsLayer(DIMENSIONS_LAYER);
            CADExtension.getEditionManager().setMapControl(mc);
            CADExtension.getCADToolAdapter().configureMenu();
            view.getConsolePanel().setTokenMarker(
                    new ConsoleToken(new KeywordMap(true)));
            CADExtension.initFocus();

            // Set up the cursor
            Image cursor = PluginServices.getIconTheme()
                    .get("cad-selection-icon").getImage();
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Cursor c = toolkit.createCustomCursor(cursor, new Point(16, 16),
                    "img");
            mc.setCursor(c);

            // Set up snapping
            ArrayList<FLayer> layersToSnap = new ArrayList<FLayer>();
            for (String layerName : getLayersToSnap()) {
                FLayer lyr = mc.getMapContext().getLayers().getLayer(layerName);
                if (lyr != null) {
                    layersToSnap.add(lyr);
                }
            }
            VectorialLayerSnapping snapTo = new VectorialLayerSnapping(
                    DIMENSIONS_LAYER);
            snapTo.setSnappinTo(view.getMapControl().getMapContext()
                    .getLayers());
            ((VectorialLayerEdited) CADExtension.getEditionManager()
                    .getLayerEdited(DIMENSIONS_LAYER))
                    .setLayersToSnap(layersToSnap);
        } catch (StartEditionLayerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void setLayersToSnap(List<String> layers) {
        SNAP_LAYERS = layers;
    }

    public static List<String> getLayersToSnap() {
        if (SNAP_LAYERS == null) {
            SNAP_LAYERS = getDefaultSnappingLayers();
        }
        return SNAP_LAYERS;
    }

    public static boolean isExtensionActive() {
        return EXTENSION_ACTIVE;
    }

    public static void stopEdition() {
        IWindow window = PluginServices.getMDIManager().getActiveWindow();
        if (window instanceof View) {
            View view = (View) window;
            view.getMapControl().setTool("zoomIn");
            view.hideConsole();
            view.repaintMap();
            CADExtension.clearView();
            PluginServices.getMainFrame().enableControls();
            CADExtension.getEditionManager().editionChanged(
                    LayerEvent.createEditionChangedEvent(DIMENSIONS_LAYER,
                            "edition"));
        }
        EXTENSION_ACTIVE = false;
    }

    private FLyrVect createTempLayer(IProjection proj)
            throws OpenDriverException, InitializeDriverException,
            LoadLayerException, InitializeWriterException,
            StartWriterVisitorException, StopWriterVisitorException {
        String fileName = System.getProperty("java.io.tmpdir") + "/"
                + LAYER_FILE_PREFIX
                + System.currentTimeMillis() + ".shp";
        File file = new File(fileName);
        ShpWriter writer = new ShpWriter();
        writer.setFile(file);
        SHPLayerDefinition resultLayerDefinition = new SHPLayerDefinition();
        resultLayerDefinition.setShapeType(FShape.LINE);
        resultLayerDefinition.setProjection(proj);
        resultLayerDefinition.setName(LAYER_NAME);
        FieldDescription[] fields = new FieldDescription[3];
        fields[0] = new FieldDescription();
        fields[0].setFieldLength(10);
        fields[0].setFieldDecimalCount(0);
        fields[0].setFieldName(GID_FIELD);
        fields[0].setFieldType(Types.INTEGER);
        fields[1] = new FieldDescription();
        fields[1].setFieldLength(10);
        fields[1].setFieldDecimalCount(10);
        fields[1].setFieldName(LENGTH_FIELD);
        fields[1].setFieldType(Types.DOUBLE);
        fields[2] = new FieldDescription();
        fields[2].setFieldName(LABEL_FIELD);
        fields[2].setFieldType(Types.CHAR);
        resultLayerDefinition.setFieldsDesc(fields);
        writer.initialize(resultLayerDefinition);
        writer.preProcess();
        writer.postProcess();
        resultLayerDefinition.setFile(file);
        IndexedShpDriver driver = new IndexedShpDriver();
        driver.open(file);
        driver.initialize();
        FLyrVect layer = (FLyrVect) LayerFactory.createLayer(LAYER_NAME,
                driver,
                file, proj);

        // Labeling
        LabelClass lc = new LabelClass();
        lc.setLabelExpressions(new String[] { "[" + LABEL_FIELD + "]" });
        lc.getTextSymbol().setDrawWithHalo(true);
        lc.getTextSymbol().setHaloWidth(6);
        lc.getTextSymbol().setFontSize(8);

        LinePlacementConstraints pl = new LinePlacementConstraints();
        pl.setPlacementMode(LinePlacementConstraints.HORIZONTAL);

        GeneralLabelingStrategy st = (GeneralLabelingStrategy) ExtendedLabelingFactory
                .createStrategy(layer,
                new DefaultLabelingMethod(lc), pl, null);
        st.setAllowOverlapping(true);
        layer.setLabelingStrategy(st);
        layer.setIsLabeled(true);

        // Styling
        ArrowDecoratorStyle arrowStyle = new ArrowDecoratorStyle();
        arrowStyle.setFlipAll(true);
        arrowStyle.setFlipFirst(true);
        arrowStyle.setScaleArrow(false);

        ArrowMarkerSymbol arrowSymbol = (ArrowMarkerSymbol) arrowStyle
                .getMarker();
        arrowSymbol.setSize(10);
        arrowSymbol.setSharpness(25);
        arrowSymbol.setColor(Color.BLACK);

        SimpleLineStyle style = new SimpleLineStyle();
        style.setArrowDecorator(arrowStyle);

        SimpleLineSymbol symbol = new SimpleLineSymbol();
        symbol.setLineStyle(style);
        symbol.setLineColor(Color.BLACK);
        symbol.setLineWidth(1);

        SingleSymbolLegend legend = new SingleSymbolLegend();
        legend.setDefaultSymbol(symbol);
        layer.setLegend(legend);

        return layer;
    }

}
