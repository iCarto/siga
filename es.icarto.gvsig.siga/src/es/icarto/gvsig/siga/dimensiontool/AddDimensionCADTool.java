package es.icarto.gvsig.siga.dimensiontool;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.IntValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.EditionUtilities;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileWriteException;
import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
import com.iver.cit.gvsig.exceptions.validate.ValidateRowException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FGeometryCollection;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.ISpatialWriter;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.gui.cad.CADTool;
import com.iver.cit.gvsig.gui.cad.DefaultCADTool;
import com.iver.cit.gvsig.gui.cad.exception.CommandException;
import com.iver.cit.gvsig.gui.cad.tools.InsertionCADTool;
import com.iver.cit.gvsig.project.documents.table.gui.TablesFor;

import es.icarto.gvsig.siga.dimensiontool.AddDimensionCADToolContext.AddDimensionCADToolState;

public class AddDimensionCADTool extends InsertionCADTool {

    public static final String CAD_ID = "_addDimension";

    private static Logger logger = Logger.getLogger(AddDimensionCADTool.class
            .getName());
    protected AddDimensionCADToolContext _fsm;
    protected Point2D firstPoint;
    protected Point2D antPoint;
    private final ArrayList list = new ArrayList();
    private FLyrVect dimensionsLayer;

    private final ArrayList points = new ArrayList();

    /**
     * Crea un nuevo PolylineCADTool.
     */
    public AddDimensionCADTool() {

    }

    /**
     * Método de incio, para poner el código de todo lo que se requiera de una
     * carga previa a la utilización de la herramienta.
     */
    @Override
    public void init() {
        // clear();
        if (_fsm == null) {
            _fsm = new AddDimensionCADToolContext(this);
        } else {
            clear();
        }
    }

    @Override
    public void clear() {
        super.init();
        this.setMultiTransition(true);
        points.clear();
        list.clear();
        firstPoint = null;
        antPoint = null;
        // con esto limpio el ultimo punto pulsado para reinicializar el
        // seguimiento de los snappers
        getCadToolAdapter().setPreviousPoint((double[]) null);
        _fsm = new AddDimensionCADToolContext(this);
    }

    public IGeometry getGeometry() {
        IGeometry[] geoms = (IGeometry[]) list.toArray(new IGeometry[0]);
        FGeometryCollection fgc = new FGeometryCollection(geoms);
        // No queremos guardar FGeometryCollections:
        GeneralPathX gp = new GeneralPathX();
        gp.append(fgc.getPathIterator(null, FConverter.FLATNESS), true);
        IGeometry newGeom = ShapeFactory.createPolyline2D(gp);
        return newGeom;
    }

    public void endGeometry() {
        IGeometry[] geoms = (IGeometry[]) list.toArray(new IGeometry[0]);
        FGeometryCollection fgc = new FGeometryCollection(geoms);

        // No queremos guardar FGeometryCollections:
        GeneralPathX gp = new GeneralPathX();
        gp.append(fgc.getPathIterator(null, FConverter.FLATNESS), true);
        IGeometry newGeom = null;
        newGeom = ShapeFactory.createPolyline2D(gp);
        addGeometry(newGeom);
    }

    public void addGeometry(IGeometry geometry) {
        try {
            this.dimensionsLayer.setEditing(true);
            this.dimensionsLayer.setWaitTodraw(true);
            this.dimensionsLayer.setProperty("stoppingEditing", new Boolean(
                    true));
            VectorialEditableAdapter vea = (VectorialEditableAdapter) this.dimensionsLayer
                    .getSource();
            Integer maxGid = 0;
            SelectableDataSource sds = this.dimensionsLayer.getRecordset();
            for (long i = 0, iLen = sds.getRowCount(); i < iLen; i++) {
                Value val = sds.getFieldValue(i, 0);
                if (val instanceof IntValue) {
                    int gid = ((IntValue) val).intValue();
                    if (maxGid < gid) {
                        maxGid = gid;
                    }
                }
            }

            vea.startEdition(EditionEvent.GRAPHIC);
            Value[] values = new Value[3];
            values[0] = ValueFactory.createValue(maxGid + 1);
            double length = CADExtension.getEditionManager().getMapControl()
                    .getMapContext().getViewPort()
                    .distanceWorld(firstPoint, antPoint);
            values[1] = ValueFactory.createValue(length);
            values[2] = ValueFactory.createValue(Math.round(length)
                    + " "
                    + MapContext.getDistanceAbbr()[MapContext
                            .getDistancePosition("Metros")]);
            DefaultFeature feature = new DefaultFeature(geometry, values, (vea.getRowCount() + 1) + "");
            vea.addRow(feature, "", EditionEvent.GRAPHIC);

            ISpatialWriter writer = (ISpatialWriter) vea.getWriter();
            TablesFor.layer(dimensionsLayer).stopEditingCell();
            vea.cleanSelectableDatasource();
            dimensionsLayer.setRecordset(vea.getRecordset()); // Queremos que el
                                                              // recordset
            // del layer
            // refleje los cambios en los campos.
            ILayerDefinition lyrDef = EditionUtilities
                    .createLayerDefinition(dimensionsLayer);
            String aux = "FIELDS:";
            FieldDescription[] flds = lyrDef.getFieldsDesc();
            for (int i = 0; i < flds.length; i++) {
                aux = aux + ", " + flds[i].getFieldAlias();
            }
            System.err.println("Escribiendo la capa " + lyrDef.getName()
                    + " con los campos " + aux);
            lyrDef.setShapeType(dimensionsLayer.getShapeType());
            writer.initialize(lyrDef);
            vea.stopEdition(writer, EditionEvent.GRAPHIC);
            this.dimensionsLayer.setWaitTodraw(false);
            this.dimensionsLayer.setProperty("stoppingEditing", new Boolean(
                    false));
            this.dimensionsLayer.setEditing(false);
        } catch (StartEditionLayerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExpansionFileWriteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ValidateRowException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReadDriverException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (StopWriterVisitorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (StartWriterVisitorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InitializeWriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.iver.cit.gvsig.gui.cad.CADTool#transition(com.iver.cit.gvsig.fmap
     * .layers.FBitSet, double, double)
     */
    @Override
    public void transition(double x, double y, InputEvent event) {
        _fsm.addPoint(x, y, event);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.iver.cit.gvsig.gui.cad.CADTool#transition(com.iver.cit.gvsig.fmap
     * .layers.FBitSet, double)
     */
    @Override
    public void transition(double d) {
        _fsm.addValue(d);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.iver.cit.gvsig.gui.cad.CADTool#transition(com.iver.cit.gvsig.fmap
     * .layers.FBitSet, java.lang.String)
     */
    @Override
    public void transition(String s) throws CommandException {
        if (!super.changeCommand(s)) {
            if (s.equals(PluginServices.getText(this, "removePoint"))) {
                _fsm.removePoint(null, points.size());
            } else {
                _fsm.addOption(s);
            }
        }
    }

    /**
     * Equivale al transition del prototipo pero sin pasarle como parámetro el
     * editableFeatureSource que ya estará creado.
     *
     * @param sel
     *            Bitset con las geometrías que estén seleccionadas.
     * @param x
     *            parámetro x del punto que se pase en esta transición.
     * @param y
     *            parámetro y del punto que se pase en esta transición.
     */
    @Override
    public void addPoint(double x, double y, InputEvent event) {
        AddDimensionCADToolState actualState = (AddDimensionCADToolState) _fsm
                .getPreviousState();
        Point2D point = new Point2D.Double(x, y);

        // if (status.equals("Polyline.FirstPoint")) {
        // antPoint = new Point2D.Double(x, y);

        // if (firstPoint == null) {
        // firstPoint = (Point2D) antPoint.clone();
        // }
        // } else

        if (firstPoint == null) {
            firstPoint = new Point2D.Double(x, y);
        }
        point = new Point2D.Double(x, y);

        if (antPoint != null) {
            GeneralPathX elShape = new GeneralPathX(GeneralPathX.WIND_EVEN_ODD,
                    2);
            elShape.moveTo(antPoint.getX(), antPoint.getY());
            elShape.lineTo(point.getX(), point.getY());
            list.add(ShapeFactory.createPolyline2D(elShape));

        }
        if (antPoint == null) {
            antPoint = (Point2D) firstPoint.clone();
        }
        antPoint = point;

        // [LBD]
        points.add(point);
    }

    public void removePoint(InputEvent event) {
        AddDimensionCADToolState actualState = (AddDimensionCADToolState) _fsm
                .getPreviousState();
        String status = actualState.getName();

        // System.out.println("Acción removePoint, estado = " + status);
        if (status.equals("Polyline.FirstPoint")) {
            cancel();
            // prueba para actualizar el ultimo punto pulsado
            getCadToolAdapter().setPreviousPoint((double[]) null);
        } else if (status.equals("Polyline.NextPointOrLineOrClose")
                || status.equals("Polyline.NextPointOrArcOrClose")) {
            System.out.println("Numero de coordenadas antes de borrar: "
                    + points.size());
            if (points.size() == 1) {
                cancel();
                // prueba para actualizar el ultimo punto pulsado
                getCadToolAdapter().setPreviousPoint((double[]) null);
            } else if (points.size() == 2) {
                // prueba para actualizar el ultimo punto pulsado
                getCadToolAdapter().setPreviousPoint(
                        (Point2D) points.get(points.size() - 2));

                list.remove(list.size() - 1);
                points.remove(points.size() - 1);
                antPoint = (Point2D) points.get(points.size() - 1);
            } else {
                // prueba para actualizar el ultimo punto pulsado
                getCadToolAdapter().setPreviousPoint(
                        (Point2D) points.get(points.size() - 2));

                list.remove(list.size() - 1);
                points.remove(points.size() - 1);
                antPoint = (Point2D) points.get(points.size() - 1);
            }
        }

    }

    /**
     * Método para dibujar la lo necesario para el estado en el que nos
     * encontremos.
     *
     * @param g
     *            Graphics sobre el que dibujar.
     * @param selectedGeometries
     *            BitSet con las geometrías seleccionadas.
     * @param x
     *            parámetro x del punto que se pase para dibujar.
     * @param y
     *            parámetro x del punto que se pase para dibujar.
     */
    @Override
    public void drawOperation(Graphics g, double x, double y) {
        for (int i = 0; i < list.size(); i++) {
            ((IGeometry) list.get(i)).cloneGeometry().draw((Graphics2D) g,
                    getCadToolAdapter().getMapControl().getViewPort(),
                    DefaultCADTool.geometrySelectSymbol);
        }
        if (antPoint != null) {
            drawLine((Graphics2D) g, antPoint, new Point2D.Double(x, y),
                    DefaultCADTool.geometrySelectSymbol);
            if (PluginServices.getMainFrame() != null) {
                ViewPort vp = getCadToolAdapter().getMapControl().getViewPort();
                double dist = vp.distanceWorld(antPoint, new Point2D.Double(x,
                        y))
                        / MapContext.getDistanceTrans2Meter()[vp
                                .getDistanceUnits()];

                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(2);
                PluginServices.getMainFrame().getStatusBar()
                        .setMessage(
                                "4",
                                "Long: "
                                        + nf.format(dist)
                                        + " "
                                        + MapContext.getDistanceAbbr()[vp
                                                .getDistanceUnits()]);

            }
        } else {
            PluginServices.getMainFrame().getStatusBar().setMessage("4", "");
        }
    }

    /**
     * Método para dibujar la lo necesario para el estado en el que nos
     * encontremos.
     *
     * @param g
     *            Graphics sobre el que dibujar.
     * @param selectedGeometries
     *            BitSet con las geometrías seleccionadas.
     * @param listaPuntos
     *            lista con los puntos a dibujar
     */
    @Override
    public void drawOperation(Graphics g, ArrayList listaPuntos) {
        for (int i = 0; i < list.size(); i++) {
            ((IGeometry) list.get(i)).cloneGeometry().draw((Graphics2D) g,
                    getCadToolAdapter().getMapControl().getViewPort(),
                    CADTool.drawingSymbol);
        }
        // ahora debemos pintar las lineas que vienen en la lista
        if (listaPuntos != null && listaPuntos.size() > 1) {
            if (antPoint != null) {
                Point2D puntoInicial = antPoint;
                for (int i = 0; i < listaPuntos.size(); i++) {
                    Point2D puntoFinal = (Point2D) listaPuntos.get(i);
                    drawLine((Graphics2D) g, puntoInicial, puntoFinal);
                    puntoInicial = puntoFinal;
                    // pintamos los puntos para que se note el snapping
                    if (i < listaPuntos.size() - 1) {
                        Point2D actual = null;
                        actual = CADExtension.getEditionManager()
                                .getMapControl().getViewPort()
                                .fromMapPoint(puntoInicial);
                        int sizePixels = 12;
                        int half = sizePixels / 2;
                        g.drawRect((int) (actual.getX() - half),
                                (int) (actual.getY() - half), sizePixels,
                                sizePixels);
                    }
                }
            } else {
                Point2D puntoInicial = (Point2D) listaPuntos.get(0);
                for (int i = 1; i < listaPuntos.size(); i++) {
                    Point2D puntoFinal = (Point2D) listaPuntos.get(i);
                    drawLine((Graphics2D) g, puntoInicial, puntoFinal);
                    puntoInicial = puntoFinal;

                    // pintamos los puntos para que se note el snapping
                    if (i < listaPuntos.size() - 1) {
                        Point2D actual = null;
                        actual = CADExtension.getEditionManager()
                                .getMapControl().getViewPort()
                                .fromMapPoint(puntoInicial);
                        int sizePixels = 12;
                        int half = sizePixels / 2;
                        g.drawRect((int) (actual.getX() - half),
                                (int) (actual.getY() - half), sizePixels,
                                sizePixels);
                    }

                }
            }
        }
    }

    /**
     * Add a diferent option.
     *
     * @param sel
     *            DOCUMENT ME!
     * @param s
     *            Diferent option.
     */
    @Override
    public void addOption(String s) {

    }

    /*
     * (non-Javadoc)
     *
     * @see com.iver.cit.gvsig.gui.cad.CADTool#addvalue(double)
     */
    @Override
    public void addValue(double d) {
    }

    public void cancel() {
        // endGeometry();
        list.clear();
        // TODO NachoV added:
        points.clear();
        antPoint = firstPoint = null;
    }

    @Override
    public void end() {
        this.setQuestion(null);
        PluginServices.getMainFrame().getStatusBar().setMessage("4", "");
        AddDimension.stopEdition();
    }

    @Override
    public String getName() {
        return PluginServices.getText(this,
                "es.icarto.gvsig.siga.AddDimensionExtension.toolTitle");
    }

    @Override
    public String toString() {
        return CAD_ID;
    }

    @Override
    public boolean isMultiTransition() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setMultiTransition(boolean condicion) {
        // TODO Auto-generated method stub

    }

    public void setPreviousTool(DefaultCADTool tool) {
        // TODO Auto-generated method stub

    }

    @Override
    public void transition(InputEvent event) {
        _fsm.removePoint(event, points.size());
    }

    public FLyrVect getDimensionsLayer() {
        return dimensionsLayer;
    }

    public void setDimensionsLayer(FLyrVect dimensionsLayer) {
        this.dimensionsLayer = dimensionsLayer;
    }

}
