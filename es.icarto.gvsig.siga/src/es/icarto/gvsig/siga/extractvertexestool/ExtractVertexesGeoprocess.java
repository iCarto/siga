package es.icarto.gvsig.siga.extractvertexestool;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.rmi.server.UID;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.cresques.cts.IProjection;
import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.ShapePointExtractor;
import org.gvsig.symbology.fmap.labeling.ExtendedLabelingFactory;
import org.gvsig.symbology.fmap.labeling.GeneralLabelingStrategy;
import org.gvsig.symbology.fmap.labeling.placements.PointPlacementConstraints;
import org.gvsig.symbology.fmap.styles.SimpleLabelStyle;
import org.gvsig.topology.Messages;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.driver.exceptions.SchemaEditionException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.drivers.featureiterators.FeatureBitsetIterator;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopVisitorException;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.symbols.IMarkerSymbol;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.LayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.shp.IndexedShpDriver;
import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
import com.iver.cit.gvsig.fmap.edition.IRowEdited;
import com.iver.cit.gvsig.fmap.edition.ISchemaManager;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.edition.ShpSchemaManager;
import com.iver.cit.gvsig.fmap.edition.writers.shp.MultiShpWriter;
import com.iver.cit.gvsig.fmap.edition.writers.shp.ShpWriter;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.DefaultLabelingMethod;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.IPlacementConstraints;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelClass;
import com.iver.cit.gvsig.geoprocess.core.fmap.XTypes;
import com.iver.cit.gvsig.util.SnappingCoordinateMap;
import com.iver.utiles.swing.threads.IBackgroundExecution;
import com.iver.utiles.swing.threads.IMonitorableTask;
import com.iver.utiles.swing.threads.MonitorableDecoratorMainFirst;
import com.vividsolutions.jts.geom.Coordinate;

import es.icarto.gvsig.commons.datasources.FieldDescriptionFactory;

public class ExtractVertexesGeoprocess implements IBackgroundExecution {

    private static final String LAYER_FILE_PREFIX = "COORDENADAS";
    private static final String FIELD_FID = "fid";
    private static final String FIELD_ORIGINAL_FID = "orig_fid";
    private static final String FIELD_VERTEX_NR = "vtx_nr";
    private static final String FIELD_RESULTS_VERTEX_NR = "res_vtx_nr";
    private static final String FIELD_X = "x";
    private static final String FIELD_Y = "y";
    private static final String FIELD_LABEL_X = "etiqueta_x";
    private static final String FIELD_LABEL_Y = "etiqueta_y";
    private static final String FIELD_LABEL = "etiqueta";

    private static final Point2D.Double LABEL_MARKER_POINT = new Point2D.Double(0.034129692832764506,
            0.4810126582278481);
    private static final Rectangle2D.Double LABEL_TEXT_FIELD_AREA_1 = new Rectangle2D.Double(0.6497890295358645,
            0.32911392405063306, 0.1940928270042194, 0.10126582278481013);
    private static final Rectangle2D.Double LABEL_TEXT_FIELD_AREA_2 = new Rectangle2D.Double(0.649789029535865,
            0.6329113924050632, 0.18143459915611815, 0.0970464135021097);

    private ExtractVertexesGeoprocessPanel userEntries;

    private LayerDefinition resultLayerDefinition;

    private double clusterTolerance = 0d;

    protected boolean operateOnlyWithSelection = false;

    protected boolean mergeLayerVertexes = false;

    protected boolean alwaysAddLastVertex = false;
    protected IWriter writer;
    protected ISchemaManager schemaManager;
    protected FLyrVect firstLayer;

    public IWriter getShpWriter(SHPLayerDefinition definition) throws Exception {
        int shapeType = definition.getShapeType();
        if (shapeType != XTypes.MULTI) {
            ShpWriter writer = new ShpWriter();
            writer.setFile(definition.getFile());
            writer.initialize(definition);
            return writer;
        } else {
            MultiShpWriter writer = new MultiShpWriter();
            writer.setFile(definition.getFile());
            writer.initialize(definition);
            return writer;
        }

    }

    public void setView(ExtractVertexesGeoprocessPanel viewPanel) {
        this.userEntries = viewPanel;
    }

    public boolean launchGeoprocess() {
        firstLayer = userEntries.getInputLayer();
        FLayers layers = userEntries.getFLayers();
        String outputFileName;
        int nr = 1;
        do {
            outputFileName = LAYER_FILE_PREFIX + "-" + (nr++);
        } while (layers.getLayer(outputFileName) != null);
        File outputFile = new File(System.getProperty("java.io.tmpdir") + "/" + outputFileName + ".shp");

        SHPLayerDefinition definition = (SHPLayerDefinition) createLayerDefinition();
        definition.setFile(outputFile);
        ShpSchemaManager schemaManager = new ShpSchemaManager(outputFile.getAbsolutePath());
        IWriter writer = null;
        try {
            writer = getShpWriter(definition);
        } catch (Exception e1) {
            String error = PluginServices.getText(this, "Error_escritura_resultados");
            String errorDescription = PluginServices.getText(this, "Error_preparar_escritura_resultados");
            userEntries.error(errorDescription, error);
            return false;
        }
        setResultLayerProperties(writer, schemaManager);

        HashMap<String, Object> params = new HashMap<String, Object>();

        boolean onlySelection = userEntries.isOnlySelectionSelected();
        params.put("layer_selection", new Boolean(onlySelection));
        double distTolerance = userEntries.getClusterTolerance();

        params.put("cluster_tolerance", new Double(distTolerance));
        params.put("merge_layer_vertexes", new Boolean(userEntries.isMergeLayerVertexesSelected()));
        params.put("always_add_last_vertex", new Boolean(userEntries.isAlwaysAddLastVertexSelected()));

        try {
            setParameters(params);
            checkPreconditions();
            IMonitorableTask task1 = createTask();
            if (task1 == null) {
                return false;

            }
            ExtractVertexesAddResultLayerTask task2 = new ExtractVertexesAddResultLayerTask(this);
            task2.setLayers(layers);
            MonitorableDecoratorMainFirst globalTask = new MonitorableDecoratorMainFirst(task1, task2);
            if (globalTask.preprocess()) {
                PluginServices.cancelableBackgroundExecution(globalTask);
            }

        } catch (ExtractVertexesException e) {
            userEntries.error(PluginServices.getText(this, "Error_fallo_geoproceso"),
                    PluginServices.getText(this, "Error_ejecucion"));
            return false;
        }
        return true;
    }

    public void cancel() {
        try {
            schemaManager.removeSchema("");
        } catch (SchemaEditionException e) {
            e.printStackTrace();
        }
    }

    protected FLayer createLayerFrom(IWriter writer) throws ExtractVertexesException {
        SHPLayerDefinition tableDef = (SHPLayerDefinition) ((ShpWriter) writer).getTableDefinition();
        String fileName = ((ShpWriter) writer).getShpPath();
        int fileNameStart = fileName.lastIndexOf(File.separator) + 1;
        if (fileNameStart == -1) {
            fileNameStart = 0;
        }
        String layerName = fileName.substring(fileNameStart, fileName.length() - 4);
        File file = new File(fileName);
        IProjection proj = (tableDef.getProjection() != null) ? tableDef.getProjection() : firstLayer.getProjection();
        try {
            IndexedShpDriver driver = new IndexedShpDriver();
            driver.open(file);
            driver.initialize();
            FLyrVect solution = (FLyrVect) LayerFactory.createLayer(layerName, driver, file, proj);
            setLabel(solution);
            setStyle(solution);
            return solution;
        } catch (Exception e) {
            throw new ExtractVertexesException("Problemas al cargar la capa resultado", e);
        }
    }

    private void setLabel(FLyrVect layer) {
        LabelClass lc = new LabelClass();
        lc.setLabelExpressions(new String[] { "[" + FIELD_LABEL_X + "]", "[" + FIELD_LABEL_Y + "]" });
        lc.getTextSymbol().setDrawWithHalo(true);
        lc.getTextSymbol().setHaloWidth(5);
        lc.getTextSymbol().setFontSize(8);
        lc.getTextSymbol().setTextColor(new Color(50, 50, 50));
        SimpleLabelStyle ls = new SimpleLabelStyle();
        ls.setMarkerPoint((Point2D) LABEL_MARKER_POINT.clone());
        ls.addTextFieldArea((Rectangle2D) LABEL_TEXT_FIELD_AREA_1.clone());
        ls.addTextFieldArea((Rectangle2D) LABEL_TEXT_FIELD_AREA_2.clone());
        lc.setLabelStyle(ls);

        PointPlacementConstraints pl = new PointPlacementConstraints();
        pl.setPlacementMode(IPlacementConstraints.ON_TOP_OF_THE_POINT);

        GeneralLabelingStrategy st = (GeneralLabelingStrategy) ExtendedLabelingFactory.createStrategy(layer,
                new DefaultLabelingMethod(lc), pl, null);
        st.setAllowOverlapping(true);
        layer.setLabelingStrategy(st);
        layer.setIsLabeled(true);
    }

    private void setStyle(FLyrVect layer) {
        ILegend legend = layer.getLegend();
        IMarkerSymbol symbol = (IMarkerSymbol) legend.getDefaultSymbol();
        symbol.setColor(new Color(100, 100, 100));
        symbol.setSize(5);
    }

    public FLayer getResult() throws ExtractVertexesException {
        if (!(writer instanceof MultiShpWriter)) {
            return createLayerFrom(writer);
        } else {
            IWriter[] writers = ((MultiShpWriter) writer).getWriters();
            if (writers.length > 1) {
                FLayers solution = new FLayers();// (map,null);
                solution.setMapContext(firstLayer.getMapContext());
                String name = ((MultiShpWriter) writer).getFileName();
                int fileNameStart = name.lastIndexOf(File.separator) + 1;
                if (fileNameStart == -1) {
                    fileNameStart = 0;
                }
                name = name.substring(fileNameStart, name.length() - 4);
                solution.setName(name);
                for (int i = 0; i < writers.length; i++) {
                    solution.addLayer(createLayerFrom(writers[i]));
                }
                return solution;
            } else if (writers.length == 0) {
                return null;
            } else {
                return createLayerFrom(writers[0]);
            }
        }
    }

    public void setResultLayerProperties(IWriter writer, ISchemaManager schemaManager) {
        this.writer = writer;
        this.schemaManager = schemaManager;
    }

    public void process() throws ExtractVertexesException {
        try {
            createTask().run();
        } catch (Exception e) {
            throw new ExtractVertexesException("Error al ejecutar la tarea", e);
        }
    }

    @Override
    public IMonitorableTask createTask() {
        return new ExtractVertexesGeoprocessTask(this);
    }

    public void checkPreconditions() throws ExtractVertexesException {
        int lyrDimensions;
        try {
            lyrDimensions = FGeometryUtil.getDimensions(firstLayer.getShapeType());

            if (lyrDimensions < 1) {
                throw new ExtractVertexesException(
                        "Geoproceso convertir polígonos o lineas a puntos con capa de puntos");
            }

        } catch (ReadDriverException e) {
            throw new ExtractVertexesException("Error intentando acceder al tipo de geometria de capa vectorial");
        }

    }

    public ILayerDefinition createLayerDefinition() {
        if (resultLayerDefinition == null) {
            ILayerDefinition resultLayerDefinition = new SHPLayerDefinition();
            resultLayerDefinition.setShapeType(XTypes.POINT);
            FieldDescription[] fields = getFields();
            resultLayerDefinition.setFieldsDesc(fields);
            return resultLayerDefinition;
        }
        return resultLayerDefinition;
    }

    private FieldDescription[] getFields() {
        FieldDescriptionFactory factory = new FieldDescriptionFactory();
        factory.setDefaultNumericLength(10);
        factory.addBigInteger(FIELD_FID);
        factory.addBigInteger(FIELD_ORIGINAL_FID);
        factory.addBigInteger(FIELD_VERTEX_NR);
        factory.addBigInteger(FIELD_RESULTS_VERTEX_NR);
        factory.setDefaultNumericLength(20);
        factory.setDefaultDecimalCount(5);
        factory.addDouble(FIELD_X);
        factory.addDouble(FIELD_Y);
        factory.setDefaultStringLength(25);
        factory.addString(FIELD_LABEL_X);
        factory.addString(FIELD_LABEL_Y);
        factory.addString(FIELD_LABEL).setFieldLength(200);
        return factory.getFields();
    }

    public void setParameters(Map params) throws ExtractVertexesException {
        Boolean onlySelection = (Boolean) params.get("layer_selection");
        if (onlySelection != null) {
            this.operateOnlyWithSelection = onlySelection.booleanValue();
        }

        Boolean mergeLayerVertexes = (Boolean) params.get("merge_layer_vertexes");
        if (mergeLayerVertexes != null) {
            this.mergeLayerVertexes = mergeLayerVertexes.booleanValue();
        }

        Boolean alwaysAddLastVertex = (Boolean) params.get("always_add_last_vertex");
        if (alwaysAddLastVertex != null) {
            this.alwaysAddLastVertex = alwaysAddLastVertex.booleanValue();
        }

        Double clusterToleranceD = (Double) params.get("cluster_tolerance");
        if (clusterToleranceD != null) {
            clusterTolerance = clusterToleranceD.doubleValue();
        }
    }

    public void process(ExtractVertexesGeoprocessTask progressMonitor) throws ExtractVertexesException {
        if (progressMonitor != null) {
            initialize(progressMonitor);
        }

        try {
            writer.preProcess();
        } catch (StartWriterVisitorException e) {
            throw new ExtractVertexesException(e);
        }

        try {
            IFeatureIterator featureIterator = null;
            if (this.operateOnlyWithSelection) {
                FBitSet selection = firstLayer.getRecordset().getSelection();
                featureIterator = new FeatureBitsetIterator(selection, firstLayer.getSource());
            } else {
                featureIterator = firstLayer.getSource().getFeatureIterator();
            }

            int numNewFeatures = 0;
            SnappingCoordinateMap coordinateMap = new SnappingCoordinateMap(clusterTolerance);
            NumberFormat nf = NumberFormat.getInstance(new Locale("es", "ES"));
            while (featureIterator.hasNext()) {
                IFeature feature = featureIterator.next();
                IGeometry fmapGeo = feature.getGeometry();

                List<Point2D[]> pointsParts = ShapePointExtractor.extractPoints(fmapGeo);
                for (int i = 0; i < pointsParts.size(); i++) {
                    Point2D[] points = pointsParts.get(i);
                    int numVertexes = 1;
                    for (int j = 0; j < points.length; j++) {
                        Point2D pt = points[j];
                        Coordinate coord = new Coordinate(pt.getX(), pt.getY());
                        if (coordinateMap.containsKey(coord) && (!alwaysAddLastVertex || j < (points.length - 1))) {
                            continue;
                        } else {
                            coordinateMap.put(coord, coord);
                            IGeometry newGeometry = ShapeFactory.createPoint2D(coord.x, coord.y);
                            Value[] attrs = new Value[] {
                                    ValueFactory.createValue(numNewFeatures),
                                    ValueFactory.createValue(Integer.parseInt(feature.getID())),
                                    ValueFactory.createValue(j + 1),
                                    ValueFactory.createValue(numVertexes),
                                    ValueFactory.createValue(coord.x),
                                    ValueFactory.createValue(coord.y),
                                    ValueFactory.createValue("X: " + nf.format(coord.x)),
                                    ValueFactory.createValue("Y: " + nf.format(coord.y)),
                                    ValueFactory.createValue(numVertexes++ + " - X: " + nf.format(coord.x) + " | Y: "
                                            + nf.format(coord.y)) };
                            writer.process(new DefaultRowEdited(new DefaultFeature(newGeometry, attrs, new UID()
                                    .toString()), IRowEdited.STATUS_ADDED, numNewFeatures++));
                        }
                    }
                }
                if (progressMonitor != null) {
                    progressMonitor.reportStep();
                }
                if (!mergeLayerVertexes && featureIterator.hasNext()) {
                    coordinateMap = new SnappingCoordinateMap(clusterTolerance);
                }
            }
            writer.postProcess();
            if (progressMonitor != null) {
                progressMonitor.finished();
            }
        } catch (StopVisitorException e) {
            throw new ExtractVertexesException("Error al finalizar el guardado de los resultados del geoproceso", e);
        } catch (BaseException e) {
            throw new ExtractVertexesException("Error al acceder a la informacion del driver dentro del geoproceso", e);
        } catch (Exception e) {
            throw new ExtractVertexesException("Error al acceder a la informacion del driver dentro del geoproceso", e);
        }
    }

    public void initialize(ExtractVertexesGeoprocessTask progressMonitor) throws ExtractVertexesException {
        try {
            progressMonitor.setInitialStep(0);
            if (this.operateOnlyWithSelection) {
                progressMonitor.setFinalStep(firstLayer.getRecordset().getSelection().cardinality());
            } else {
                progressMonitor.setFinalStep(firstLayer.getSource().getShapeCount());
            }
            progressMonitor.setDeterminatedProcess(true);
            progressMonitor.setNote(Messages.getText("extract_vertexes_note"));
            progressMonitor.setStatusMessage(Messages.getText("extract_vertexes_layer_message"));
        } catch (ReadDriverException e) {
            throw new ExtractVertexesException("error accediendo al numero de features de una layer", e);
        }
    }

}
