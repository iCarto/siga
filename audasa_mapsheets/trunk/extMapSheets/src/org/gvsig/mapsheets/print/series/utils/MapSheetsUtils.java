package org.gvsig.mapsheets.print.series.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.print.attribute.standard.PrintQuality;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.exolab.castor.xml.Marshaller;
import org.gvsig.mapsheets.print.series.MapSheetsCreationExtension;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGridGraphic;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGrigSymbol;
import org.gvsig.mapsheets.print.series.fmap.MapSheetsGridWriteTask;
import org.gvsig.mapsheets.print.series.fmap.MemoryDriverImpl;
import org.gvsig.mapsheets.print.series.fmap.SheetMemoryDriver;
import org.gvsig.mapsheets.print.series.gui.utils.IProgressListener;
import org.gvsig.mapsheets.print.series.gui.utils.SheetComboItem;
import org.gvsig.mapsheets.print.series.layout.MapSheetFrameView;
import org.gvsig.mapsheets.print.series.layout.MapSheetsFrameText;
import org.gvsig.mapsheets.print.series.layout.MapSheetsLayoutTemplate;
import org.gvsig.mapsheets.print.series.print.MapSheetsPrint;

import com.hardcode.driverManager.Driver;
import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.FileNotFoundDriverException;
import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.OpenDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.Print;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.FGeometryCollection;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleFillSymbol;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.drivers.LayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.drivers.dbf.DBFDriver;
import com.iver.cit.gvsig.fmap.drivers.shp.IndexedShpDriver;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.edition.writers.shp.ShpWriter;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrDefault;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.SelectionSupport;
import com.iver.cit.gvsig.fmap.rendering.FGraphic;
import com.iver.cit.gvsig.fmap.tools.CompoundBehavior;
import com.iver.cit.gvsig.fmap.tools.Behavior.Behavior;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.documents.layout.Attributes;
import com.iver.cit.gvsig.project.documents.layout.LayoutContext;
import com.iver.cit.gvsig.project.documents.layout.Size;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameView;
import com.iver.cit.gvsig.project.documents.layout.fframes.IFFrame;
import com.iver.cit.gvsig.project.documents.layout.gui.Layout;
import com.iver.cit.gvsig.project.documents.view.ProjectView;
import com.iver.cit.gvsig.project.documents.view.ProjectViewFactory;
import com.iver.utiles.SimpleFileFilter;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;
import com.iver.utiles.xml.XMLEncodingUtils;
import com.iver.utiles.xmlEntity.generate.XmlTag;

/**
 * Various static utility methods.
 *
 * @author jldominguez
 *
 */
public class MapSheetsUtils {

    private static Logger logger = Logger.getLogger(MapSheetsUtils.class);

    private static final double META_BOX_MIN_HEIGHT_CM = 1.5;
    private static final double META_BOX_MAX_HEIGHT_CM = 4;

    public static final double META_BOX_PLUS_MARGIN = 1.1;

    private static final String TITLE_LINES_SEPARATOR = "\\|";
    private static final double TITLE_INTER_LINE_RATIO = 0.25;

    public static double DEFAULT_LENGTH_FOR_POINTS = 0.001;

    public static int getDPI(Layout lyt) {

	PrintQuality pq = (PrintQuality)
		lyt.getLayoutContext().getAtributes().getAttributes().get(PrintQuality.class);

	if (pq.equals(PrintQuality.NORMAL)){
	    return 300;
	} else if (pq.equals(PrintQuality.HIGH)){
	    return 600;
	} else if (pq.equals(PrintQuality.DRAFT)){
	    return 72;
	}
	return 300;
    }

    /**
     * Get meta box (cajetin) height in cm
     * @param psi paper size in cm
     * @return
     */
    public static double getMetaBoxHeightFromPaperSize(Size psi) {

	double maxdim = psi.getAlto();
	if (maxdim < psi.getAncho()) {
	    maxdim = psi.getAncho();
	}
	return META_BOX_MIN_HEIGHT_CM +
		(META_BOX_MAX_HEIGHT_CM-META_BOX_MIN_HEIGHT_CM) *
		maxdim / Attributes.METRIC_A0_PAPER_SIZE.getAlto();
    }




    public static Rectangle2D getBBForCenterMapSizeScale(
	    double cx, double cy,
	    double mapw, double maph, int scaleDenom) {

	double map_w_world_mm = mapw * scaleDenom;
	double map_h_world_mm = maph * scaleDenom;

	double res_minx_m = cx - 0.5 * map_w_world_mm * 0.001;
	double res_miny_m = cy - 0.5 * map_h_world_mm * 0.001;

	double res_w_m = map_w_world_mm * 0.001;
	double res_h_m = map_h_world_mm * 0.001;
	return new Rectangle2D.Double(res_minx_m, res_miny_m, res_w_m, res_h_m);
    }




    public static Rectangle2D getItemBB(int x, int y, int xc, int yc, Rectangle2D bb) {

	double w = bb.getWidth() / xc;
	double h = bb.getHeight() / yc;
	double minx = bb.getMinX() + x * w;
	double miny = bb.getMaxY() - (1+y) * h;
	return new Rectangle2D.Double(minx, miny, w, h);
    }


    private static DecimalFormat df = null;

    public static String getFormattedDouble(double d, int decs) {

	String acc = "#.";
	if (decs > 0) {
	    for (int i=0; i<decs; i++) {
		acc = acc + "#";
	    }
	} else {
	    acc = "#";
	}
	df = new DecimalFormat(acc);
	DecimalFormatSymbols dfs = new DecimalFormatSymbols();
	df.setDecimalFormatSymbols(dfs);
	return df.format(d);
    }




    public static void createPdfMaps(
	    MapSheetsLayoutTemplate lay_template,
	    boolean all_sheets,
	    boolean highlight,
	    FLayer back_lyr,
	    File targetFolder,
	    String base_name,
	    IProgressListener progListen,
	    Cancellable canc) {

	MapSheetGrid gri = lay_template.getGrid();
	SheetMemoryDriver drv = gri.getTheMemoryDriver();

	ArrayList sel_ind = null;

	if (lay_template.getMainViewFrame() instanceof MapSheetFrameView) {
	    lay_template.getMainViewFrame().setHighlight(highlight);
	}

	if (all_sheets) {
	    int n = 0;
	    sel_ind = new ArrayList();
	    try {
		n = lay_template.getGrid().getTheMemoryDriver().getShapeCount();
		for (int i=0; i<n; i++) {
		    sel_ind.add(new Integer(i));
		}
	    } catch (ReadDriverException e) {
		NotificationManager.addError(e);
	    }
	} else {
	    sel_ind = getSelectedIndices(lay_template.getGrid());
	}

	int count = sel_ind.size();
	int ind_it = 0;

	File t_file = null;
	String code = null;
	for (int i=0; i<count; i++) {

	    if (progListen != null) {
		progListen.progress(1+i, count+1);
	    }


	    ind_it = ((Integer) sel_ind.get(i)).intValue();
	    try {
		lay_template.update(ind_it);
		code = drv.getFieldValue(ind_it,0).toString();
		lay_template.updateAudasaSheetCode(code);
	    } catch (Exception e) {
		NotificationManager.addError(e);
		return;
	    }

	    t_file = new File(
		    targetFolder.getAbsolutePath()
		    + File.separator
		    + base_name + "_" + code + ".pdf");

	    if (canc.isCanceled()) {
		// progListen.cancelled(PluginServices.getText(canc, "Cancelled_by_user"));
		break;
	    }

	    try {
		createSinglePdfMap(lay_template, t_file, back_lyr);
	    } catch (Exception ex) {
		ex.printStackTrace();
		NotificationManager.addError(ex);
	    }
	}

	if (lay_template.getMainViewFrame() instanceof MapSheetFrameView) {
	    lay_template.getMainViewFrame().setHighlight(false);
	}

	if (progListen != null) {
	    progListen.progress(count+1, count+1);
	}
	try{ Thread.sleep(100); } catch (Exception ex) { }

    }

    public static ArrayList getSelectedIndices(FLyrVect lyr) {

	SelectionSupport ss = lyr.getSelectionSupport();
	FBitSet fbs = ss.getSelection();

	ArrayList resp = new ArrayList();
	for(int i=fbs.nextSetBit(0); i>=0; i=fbs.nextSetBit(i+1)) {
	    resp.add(new Integer(i));
	}
	return resp;
    }


    public static HashMap getSelectedIndicesHM(FLyrVect lyr) {

	SelectionSupport ss = lyr.getSelectionSupport();
	FBitSet fbs = ss.getSelection();

	HashMap resp = new HashMap();
	for(int i=fbs.nextSetBit(0); i>=0; i=fbs.nextSetBit(i+1)) {
	    resp.put(new Integer(i), "x");
	}
	return resp;
    }

    public static ArrayList getBBoxesOfGeometries(ArrayList geoms) {

	ArrayList resp = new ArrayList();

	if (geoms == null || geoms.size() == 0) {
	    return resp;
	}

	Iterator iter = geoms.iterator();
	IGeometry igeo = null;
	while (iter.hasNext()) {
	    igeo = (IGeometry) iter.next();
	    resp.add(igeo.getBounds2D());
	}
	return resp;
    }




    private static void createSinglePdfMap(
	    MapSheetsLayoutTemplate lyt_tem,
	    File target_file,
	    FLayer back_lyr) throws Exception {

	Attributes _attributes = lyt_tem.getLayoutContext().getAtributes();
	double w = 0;
	double h = 0;

	w = ((_attributes.m_sizePaper.getAncho() * Attributes.DPISCREEN)
		/ Attributes.PULGADA);
	h = ((_attributes.m_sizePaper.getAlto() * Attributes.DPISCREEN)
		/ Attributes.PULGADA);

	FLayer clo_lyr = null;

	if (back_lyr != null) {
	    clo_lyr = cloneLayer(back_lyr);
	    clo_lyr.setVisible(true);
	    addBackLayer(lyt_tem, clo_lyr);
	}

	Document doc_ = new Document(new Rectangle((float) w, (float) h));
	FileOutputStream fos = new FileOutputStream(target_file);
	PdfWriter writer = PdfWriter.getInstance(doc_, fos);

	// add map image
	doc_.open();
	// print to file
	Print print = new Print();
	print.setLayout(lyt_tem);

	PdfContentByte cb = writer.getDirectContent();
	Graphics2D g2 = cb.createGraphicsShapes((float) w, (float) h);
	print.print(g2, _attributes.getPageFormat(), 0);

	g2.dispose();

	doc_.close();
	fos.close();
	writer.close();
	doc_=null;
	fos=null;
	writer=null;
	print=null;

	if (clo_lyr != null) {
	    lyt_tem.getMainViewFrame().getMapContext().getLayers().removeLayer(clo_lyr);
	}

	Runtime.getRuntime().gc();
    }




    /**
     * Dirty utility method to clone a layer because other ways do not work
     * properly.
     *
     * @param ras
     * @return
     * @throws Exception
     */
    public static FLayer cloneLayer(FLayer lyr) throws Exception {

	FLayers lyrs = lyr.getParentLayer();
	int index = getIndexOfLayer(lyr, lyrs);

	XMLEntity xmle = lyrs.getXMLEntity();
	FLayers new_lyrs = new FLayers();

	new_lyrs.setMapContext(lyrs.getMapContext());

	new_lyrs.setXMLEntity(xmle);
	return new_lyrs.getLayer(index);
    }



    public static int getIndexOfLayer(FLayer lay, FLayers play) {

	int len = play.getLayersCount();
	for (int i=0; i<len; i++) {
	    if (play.getLayer(i) == lay) {
		return i;
	    }
	}
	return -1;
    }



    public static void addBackLayer(
	    MapSheetsLayoutTemplate lyt_tem, FLayer lyr) {

	FFrameView main_frame = lyt_tem.getMainViewFrame();
	if (main_frame == null) {
	    NotificationManager.addError(new Exception("Main view frame not found in layout template (?)"));
	} else {
	    main_frame.getMapContext().getLayers().addLayer(0, lyr);
	}
    }


    public static void removeLayer(
	    MapSheetsLayoutTemplate lyt_tem, FLayer lyr) {

	FFrameView main_frame = lyt_tem.getMainViewFrame();
	if (main_frame == null) {
	    NotificationManager.addError(new Exception("Main view frame not found in layout template (?)"));
	} else {
	    main_frame.getMapContext().getLayers().removeLayer(lyr);
	}
    }


    //	private static void addSeparator(Layout lyt, MapSheetsSettings msett, double leftspace) {
    //
    //		Attributes atts = msett.getAuxLayout().getLayoutContext().getAtributes();
    //		double paper_w_cm = 0;
    //		double paper_h_cm = 0;
    //
    //		paper_w_cm = atts.getPaperSize().getAncho() ; // cm
    //		paper_h_cm = atts.getPaperSize().getAlto() ; // cm
    //
    //		double cajetin_height_cm = getMetaBoxHeightFromPaperSize(atts.getPaperSize());
    //
    //		double bot_margin_cm = atts.getMargins()[1];
    //		double rig_margin_cm = atts.getMargins()[3];
    //		double lef_margin_cm = atts.getMargins()[2];
    //
    //
    //		double frame_w = paper_w_cm - rig_margin_cm - lef_margin_cm;
    //		double line_x = lef_margin_cm + frame_w * leftspace;
    //
    //		double line_y1 = paper_h_cm - bot_margin_cm - 0.9 * cajetin_height_cm;
    //		double line_y2 = paper_h_cm - bot_margin_cm - 0.1 * cajetin_height_cm;
    //
    //		FFrameGraphics graf = new FFrameGraphics();
    //		PolyLineAdapter poad = new PolyLineAdapter();
    //
    //		poad.addPoint(new Point2D.Double(line_x, line_y1));
    //		poad.obtainShape(new Point2D.Double(line_x, line_y2));
    //
    //		graf.update(FFrameGraphics.LINE, null);
    //		graf.setGeometryAdapter(poad);
    //		ISymbol sym = SymbologyFactory.createDefaultSymbolByShapeType(FShape.LINE, Color.BLACK);
    //		graf.setFSymbol(sym);
    //
    //		// lyt.getLayoutContext().addF Frame(graf, true, false);
    //		addFrame(lyt.getLayoutContext(), graf, 0);
    //	}

    // =====================================================================
    // =====================================================================

    private static BufferedImage aux_bim = new BufferedImage(5,5,BufferedImage.TYPE_3BYTE_BGR);
    private static Graphics auxGraphics = aux_bim.getGraphics();

    public static ArrayList[] createFrames(
	    boolean cover_view_selected,
	    boolean selected_only,
	    Rectangle2D useful_map_cm,
	    ViewPort vp,
	    long scale,
	    int overlap_pc,
	    IProjection iproj,
	    FLyrVect ler) throws Exception {

	if (cover_view_selected) {
	    return createCoverFrames(
		    useful_map_cm,
		    vp.getAdjustedExtent(),
		    null,
		    scale,
		    overlap_pc,
		    iproj);
	} else {
	    return createFeatureFrames(
		    useful_map_cm,
		    overlap_pc,
		    selected_only,
		    ler,
		    scale,
		    iproj);
	}

    }

    private static ArrayList[] createFeatureFrames(
	    Rectangle2D use_map_size_cm,
	    int overlap_pc,
	    boolean selected_only,
	    FLyrVect lyr,
	    long scale,
	    IProjection iproj) throws Exception {

	// FLyrVect lyr = null; // setts.getFeaturesLayer();
	SelectionSupport ss = lyr.getSelectionSupport();
	ReadableVectorial rv = lyr.getSource();
	FGeometryCollection filter_geo = null;
	IGeometry shp = null;
	IGeometry[] aux_g = new IGeometry[1];

	Rectangle2D r_acum = null;
	// Rectangle2D useful_map_cm = getUsefulMapSize(setts);

	FBitSet fbs = ss.getSelection();

        if (selected_only) {

            if (fbs.length() == 0) {
                throw new Exception(PluginServices.getText(
                        MapSheetsUtils.class, "No_selected_geometries_found"));
            }

	    for(int i=fbs.nextSetBit(0); i>=0; i=fbs.nextSetBit(i+1)) {
                shp = rv.getShape(i);
		if (r_acum == null) {
		    r_acum = shp.getBounds2D();
		    r_acum = (Rectangle2D) r_acum.clone();
		} else {
		    r_acum = (Rectangle2D) r_acum.clone();
		    r_acum.add(shp.getBounds2D());
		}

                if (filter_geo == null) {
                    aux_g[0] = shp;
                    filter_geo = new FGeometryCollection(aux_g);
                } else {
                    filter_geo.addGeometry(shp);
                }
	    }

	} else {

	    int n = rv.getShapeCount();

	    if (n == 0) {
		throw new Exception(PluginServices.getText(MapSheetsUtils.class, "No_geometries_found"));
	    }

	    for (int i=0; i<n; i++) {
		shp = rv.getShape(i);
		if (r_acum == null) {
		    r_acum = shp.getBounds2D();
		    r_acum = (Rectangle2D) r_acum.clone();
		} else {
		    r_acum = (Rectangle2D) r_acum.clone();
		    r_acum.add(shp.getBounds2D());
		}

		if (filter_geo == null) {
		    aux_g[0] = shp;
		    filter_geo = new FGeometryCollection(aux_g);
		} else {
		    filter_geo.addGeometry(shp);
		}

	    }
	}

	return createCoverFrames(use_map_size_cm, r_acum, filter_geo, scale, overlap_pc, iproj);
    }

    private static ArrayList[] createCoverFrames(
	    Rectangle2D useful_map_cm,
	    Rectangle2D aoi_ext,
	    IGeometry filter_geom,
	    long scale,
	    int overlap_pc,
	    IProjection iproj) throws Exception {

	ArrayList[] grid_codes = getGrid(
		useful_map_cm,
		aoi_ext,
		filter_geom,
		scale,
		overlap_pc,
		iproj);

	ArrayList rects = grid_codes[0];
	ArrayList geoms = new ArrayList();

	int sz = rects.size();
	for (int i=0; i<sz; i++) {
	    geoms.add(rectToGeom((Rectangle2D) rects.get(i)));
	}
	grid_codes[0] = geoms;
	return grid_codes;
    }

    public static IGeometry rectToGeom(Rectangle2D r) {

	GeneralPathX gpx = new GeneralPathX();
	gpx.moveTo(r.getMinX(), r.getMinY());
	gpx.lineTo(r.getMinX()+r.getWidth(), r.getMinY());
	gpx.lineTo(r.getMinX()+r.getWidth(), r.getMinY()+r.getHeight());
	gpx.lineTo(r.getMinX(), r.getMinY()+r.getHeight());
	gpx.lineTo(r.getMinX(), r.getMinY());
	return ShapeFactory.createPolygon2D(gpx);
    }









    /**
     *
     * @param useful_map_cm
     * @param aext
     * @param filter_geom
     * @param req_scale
     * @param clearance_pc
     * @param proj
     * @return geoms y codes
     * @throws Exception
     */
    private static ArrayList[] getGrid(
	    Rectangle2D useful_map_cm,
	    Rectangle2D aext,
	    IGeometry filter_geom,
	    long req_scale,
	    double clearance_pc,
	    IProjection proj) throws Exception {

	// double should_be_one = proj.getScale(0, 0.01
	// * useful_map_cm.getWidth(), useful_map_cm.getWidth(), 2.54);
	ArrayList[] resp = new ArrayList[2];

	double meters_per_mapunit = 1;
	if (!proj.isProjected()) {
	    meters_per_mapunit = 2 * Math.PI * proj.getDatum().getESemiMajorAxis() / 360.0;
	}

	double sheet_w_map_units = 0.01 * useful_map_cm.getWidth() * req_scale / meters_per_mapunit;
	double sheet_h_map_units = 0.01 * useful_map_cm.getHeight() * req_scale / meters_per_mapunit;

	long hor_sheet_count_long = 0;
	double aux_w = -1;
        while (aux_w < aext.getWidth()) {
	    hor_sheet_count_long++;
	    aux_w = hor_sheet_count_long * sheet_w_map_units
		    - 0.01 * clearance_pc * sheet_w_map_units * (hor_sheet_count_long - 1);
	}

	long ver_sheet_count_long = 0;
	double aux_h = -1;
        while (aux_h < aext.getHeight()) {
	    ver_sheet_count_long++;
	    aux_h = ver_sheet_count_long * sheet_h_map_units
		    - 0.01 * clearance_pc * sheet_h_map_units * (ver_sheet_count_long - 1);
	}

	ArrayList resp1 = new ArrayList();
	ArrayList resp2 = new ArrayList();
	// Rectangle2D[(int) (hor_sheet_count_long * ver_sheet_count_long)];
	double item_x = 0;
	double item_y = 0;
	Rectangle2D item_rect = null;
	double ini_offset_w = 0.5 * (aux_w - aext.getWidth());
	double ini_offset_h = 0.5 * (aux_h - aext.getHeight());
	double inc_w = (1.0 - clearance_pc / 100.0) * sheet_w_map_units;
	double inc_h = (1.0 - clearance_pc / 100.0) * sheet_h_map_units;

	String name_col = "";

	for (int i=0; i<hor_sheet_count_long; i++) {
	    item_x = aext.getMinX() - ini_offset_w + i * inc_w;
	    name_col = nextLetter(name_col);
	    for (int j=0; j<ver_sheet_count_long; j++) {
		item_y = aext.getMaxY() + ini_offset_h - j * inc_h - sheet_h_map_units;
		item_rect = new Rectangle2D.Double(
			item_x, item_y,
			sheet_w_map_units, sheet_h_map_units);

		if (filter_geom == null || filter_geom.intersects(item_rect)) {
		    resp1.add(item_rect);
		    resp2.add(name_col.toUpperCase() + MapSheetsCreationExtension.CODE_ID_SEPARATOR + (j+1));
		}
	    }
	}

        if (resp1.size() > MapSheetsCreationExtension.MAX_PRINTAB_MAPS) {

            String msg = PluginServices.getText(MapSheetsUtils.class,
                    "Too_many_sheets");
            msg = msg + ": " + resp1.size() + " (max: "
                    + MapSheetsCreationExtension.MAX_PRINTAB_MAPS + ")";

            throw new Exception(msg);
        }

	resp[0] = resp1;
	resp[1] = resp2;
	return resp;
    }


    private static Rectangle2D getRect(double w, double h, double cx, double cy) {
	return new Rectangle2D.Double(cx-w/2, cy-h/2, w, h);
    }

    public static SimpleFillSymbol SHEET_GRID_SYMBOL_NORMAL = null;
    public static SimpleFillSymbol SHEET_GRID_SYMBOL_EDIT = null;

    public static ISymbol getFrameSymbol(boolean editing, boolean sel) {

	ISymbol resp = null;
	if (editing) {
	    resp = getFrameSymbol_editing();
	} else {
	    resp = getFrameSymbol_normal();
	}

	if (sel) {
	    resp = resp.getSymbolForSelection();
	}

	return resp;
    }


    public static ISymbol getFrameSymbol_normal() {
	if (SHEET_GRID_SYMBOL_NORMAL == null) {
	    SHEET_GRID_SYMBOL_NORMAL = new MapSheetGrigSymbol(false, false);
	}
	return SHEET_GRID_SYMBOL_NORMAL;
    }

    public static ISymbol getFrameSymbol_editing() {
	if (SHEET_GRID_SYMBOL_EDIT == null) {
	    SHEET_GRID_SYMBOL_EDIT = new MapSheetGrigSymbol(true, false);
	}
	return SHEET_GRID_SYMBOL_EDIT;
    }




    public static Behavior getBehaviorFrom(Behavior beh, Class clazz) {

	if (clazz.isInstance(beh)) {
	    return beh;
	} else {
	    if (beh instanceof CompoundBehavior) {
		CompoundBehavior cobe = (CompoundBehavior) beh;
		int len = cobe.size();
		for (int i=0; i<len; i++) {
		    if (clazz.isInstance(cobe.getBehavior(i))) {
			return cobe.getBehavior(i);
		    }
		}
		return null;
	    } else {
		return null;
	    }
	}
    }


    public static MediaTracker mt = new MediaTracker(new Container());

    /**
     * Gets an image from a file in a well known format (png, jpg, gif)
     * @param data the image file
     * @return the image object
     */
    public static Image getImage(File data) {

	if (data == null) {
	    logger.error("Cannot open image file with Tracker: file is NULL");
	    return null;
	}

	try {
	    Image img = Toolkit.getDefaultToolkit().createImage(data.getAbsolutePath());
	    mt.addImage(img, 0);
	    mt.waitForID(0, 1000);
	    mt.removeImage(img);

	    return img;
	} catch (Throwable th) {
	    logger.error("While getting downloaded image: " + th.getMessage());
	    return null;
	}
    }

    public static Rectangle2D scaleRect(Rectangle2D r, double sc) {

	Rectangle2D resp = new Rectangle2D.Double(
		r.getCenterX() - sc * 0.5 * r.getWidth(),
		r.getCenterY() - sc * 0.5 * r.getHeight(),
		sc * r.getWidth(),
		sc * r.getHeight());
	return resp;
    }




    public static ArrayList getLayers(
	    FLayers ll,
	    int[] tt,
	    boolean only_vect) throws Exception {

	ArrayList resp = new ArrayList();
	int count = ll.getLayersCount();
	FLayer lyr = null;
	FLyrVect lyrv = null;

	for (int i=0; i<count; i++) {
	    lyr = ll.getLayer(i);
	    if (lyr instanceof FLayers) {
		ArrayList addlist = getLayers((FLayers) lyr, tt, only_vect);
		resp.addAll(addlist);
	    } else {
		if (lyr instanceof FLyrVect || (!only_vect)) {

		    if (lyr instanceof FLyrVect) {
			lyrv = (FLyrVect) lyr;
			if (isOfType(lyrv, tt)) {
			    resp.add(lyrv);
			}
		    } else {
			resp.add(lyr);
		    }
		}
	    }
	}
	return resp;
    }




    private static boolean isOfType(FLyrVect lyr, int[] tt) throws Exception {

	int t = lyr.getShapeType();
	for (int i=0; i<tt.length; i++) {
	    if (t == tt[i]) {
		return true;
	    }
	}
	return false;
    }

    public static void addFrame(LayoutContext lc, IFFrame fra, int lvl) {
	lc.getEFS().doAddFFrame(fra);
	fra.setLevel(lvl);
	lc.updateFFrames();
    }

    public static final String[] LETTERS = {
	"a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
	"k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
	"u", "v", "w", "x", "y", "z" };

    public static boolean isLetter(String str, boolean casesen) {

	if (str == null || str.length() != 1) {
	    return false;
	}

	String aux = str;
	if (!casesen) {
	    aux = aux.toLowerCase();
	}

	char c = aux.charAt(0);
	return c >= 'a' && c <= 'z';
    }

    public static String nextLetter(String _str) {

	if (_str == null || _str.length() == 0) {
	    return "a";
	} else {

	    if (isLetter(_str, false)) {
		if (_str.compareTo("z") == 0) {
		    return "aa";
		} else {
		    char[] c = new char[1];
		    c[0] = _str.charAt(0);
		    c[0] = (char) (1 + c[0]);
		    return new String(c);
		}
	    } else {
		int len = _str.length();
		String lastchar = _str.substring(len-1);
		String butlast = _str.substring(0, len-1);
		if (isLetter(lastchar, true)) {
		    return butlast + nextLetter(lastchar);
		} else {
		    return _str + "a";
		}
	    }
	}
    }

    public static ArrayList getActiveLayers(FLayers lyrs) {

	ArrayList resp = new ArrayList();
	int len = lyrs.getLayersCount();
	for (int i=0; i<len; i++) {
	    if (lyrs.getLayer(i).isActive()) {
		resp.add(lyrs.getLayer(i));
	    }
	}
	return resp;
    }



    public static void toSHP(MapSheetGrid msg, MapContext mco) throws Exception {

	VectorialDriver memdrv = MapSheetsUtils.createDriverFromMapSheetsGrid(msg);

	FLyrVect lyrv = (FLyrVect) LayerFactory.createLayer("", memdrv, msg.getProjection());

	// ExportTo eto = new ExportTo();
	// eto.saveToShp(mco, lyrv);
	saveToShp(mco, lyrv);
    }



    private static void saveToShp(MapContext mco, FLyrVect lyrv) {

	try {
	    JFileChooser jfc = new JFileChooser();
	    SimpleFileFilter filterShp = new SimpleFileFilter("shp",
		    PluginServices.getText(MapSheetsUtils.class, "shp_files"));
	    jfc.setFileFilter(filterShp);
	    if (jfc.showSaveDialog((Component) PluginServices.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
		File newFile = jfc.getSelectedFile();
		String path = newFile.getAbsolutePath();
		if( newFile.exists()){
		    int resp = JOptionPane.showConfirmDialog(
			    (Component) PluginServices.getMainFrame(),
			    PluginServices.getText(MapSheetsUtils.class,"fichero_ya_existe_seguro_desea_guardarlo"),
			    PluginServices.getText(MapSheetsUtils.class,"guardar"), JOptionPane.YES_NO_OPTION);
		    if (resp != JOptionPane.YES_OPTION) {
			return;
		    }
		}
		if (!(path.toLowerCase().endsWith(".shp"))) {
		    path = path + ".shp";
		}
		newFile = new File(path);
		SelectableDataSource sds = lyrv.getRecordset();
		FieldDescription[] fieldsDescrip = sds.getFieldsDescription();

		ShpWriter writer = (ShpWriter) LayerFactory.getWM().getWriter(
			"Shape Writer");
		// loadEnconding(lyrv, writer);
		IndexedShpDriver drv = getOpenShpDriver(newFile);
		SHPLayerDefinition lyrDef = new SHPLayerDefinition();
		lyrDef.setFieldsDesc(fieldsDescrip);
		lyrDef.setFile(newFile);
		lyrDef.setName(newFile.getName());
		lyrDef.setShapeType(lyrv.getTypeIntVectorLayer());
		writer.setFile(newFile);
		writer.initialize(lyrDef);
		writeFeatures(mco, lyrv, writer, drv);

	    }
	} catch (InitializeWriterException e) {
	    NotificationManager.addError(e.getMessage(),e);
	} catch (OpenDriverException e) {
	    NotificationManager.addError(e.getMessage(),e);
	} catch (ReadDriverException e) {
	    NotificationManager.addError(e.getMessage(),e);
	} catch (DriverLoadException e) {
	    NotificationManager.addError(e.getMessage(),e);
	}
    }



    public static VectorialDriver createDriverFromMapSheetsGrid(
	    MapSheetGrid msg) throws Exception {

	MemoryDriverImpl memdrv = new MemoryDriverImpl();
	memdrv.setShapeType(FShape.POLYGON);

	ArrayList fdd = msg.getFieldDescs();
	DefaultTableModel dtm = memdrv.getTableModel();
	FieldDescription fd = null;
	for (int i=0; i< fdd.size(); i++) {
	    fd = (FieldDescription) fdd.get(i);
	    dtm.addColumn(fd.getFieldName());
	}

	int len = msg.getSource().getShapeCount();
	MapSheetGridGraphic item = null;

	for (int i=0; i<len; i++) {
	    item = msg.getGraphic(i);
	    memdrv.addGeometry(item.getGeom(), item.getAttributes());
	}

	return memdrv;
    }



    private static IndexedShpDriver getOpenShpDriver(File fileShp) throws OpenDriverException {
	IndexedShpDriver drv = new IndexedShpDriver();
	if (!fileShp.exists()) {
	    try {
		fileShp.createNewFile();
		File newFileSHX=new File(fileShp.getAbsolutePath().replaceAll("[.]shp",".shx"));
		newFileSHX.createNewFile();
		File newFileDBF=new File(fileShp.getAbsolutePath().replaceAll("[.]shp",".dbf"));
		newFileDBF.createNewFile();
	    } catch (IOException e) {
		throw new FileNotFoundDriverException("SHP",e,fileShp.getAbsolutePath());
	    }
	}
	drv.open(fileShp);
	return drv;
    }

    private static void writeFeatures(MapContext mapContext, FLyrVect layer, IWriter writer, Driver reader) throws ReadDriverException
    {
	PluginServices.cancelableBackgroundExecution(
		new MapSheetsGridWriteTask(mapContext, layer, writer, reader));
    }


    /*
					MapSheetGrid2 __lyr = MapSheetGrid2.createMapSheetGrid("NEW", mc.getProjection());
					mx.getLayers().addLayer(__lyr);
     */
    public static void addMapSheetsGrid2(File shpfile, MapControl mco) throws Exception {

	IndexedShpDriver drv = createShpDriver(shpfile);
	DBFDriver dbf_drv = createDbfDriver(shpfile);

	LayerDefinition ld = new LayerDefinition();
	FieldDescription[] sorted_flds = getFieldDescriptions(dbf_drv);
	ld.setFieldsDesc(sorted_flds);
	ld.setShapeType(FShape.POLYGON);
	ld.setProjection(mco.getProjection());

	String nam = PluginServices.getText(MapSheetsUtils.class, "Grid") + " - "
		+ MapSheetsUtils.getName(shpfile);

	MapSheetGrid lyr =
		MapSheetGrid.createMapSheetGrid(
			nam,
			mco.getProjection(),
			ld);

	IGeometry ig = null;
	Value[] row = null;
	HashMap atts_hm = null;

	int sz = drv.getShapeCount();
	for (int i=0; i<sz; i++) {
	    // order:
	    // CODE string // SCALE // DIMX_CM
	    // DIMY_CM // OVERLAP // ROT_RAD // others
	    ig = drv.getShape(i);
	    row = getValues(dbf_drv, sorted_flds, i);; // (Value[]) vals_list.get(i);
	    lyr.addSheet(ig, row);
	}

	drv.close();
	drv = null;
	dbf_drv.close();
	dbf_drv = null;

	//before adding the new layer, delete all MapSheetGrids in TOC
	FLayers layersInTOC = mco.getMapContext().getLayers();
	for (int i=0; i<layersInTOC.getLayersCount(); i++) {
	    if (layersInTOC.getLayer(i) instanceof MapSheetGrid) {
		mco.getMapContext().getLayers().removeLayer(layersInTOC.getLayer(i));
	    }
	}
	mco.getMapContext().getLayers().addLayer(lyr);

	MapSheetsUtils.setOnlyActive(lyr, mco.getMapContext().getLayers());
    }




    private static String getName(File f) {

	String str = f.getAbsolutePath();
	int lastb = str.lastIndexOf(File.separator);
	if (lastb != -1) {
	    str = str.substring(lastb + File.separator.length());
	}
	int lastp = str.lastIndexOf(".");
	str = str.substring(0, lastp);
	return str;
    }



    public static void addMapSheetsGrid(File shpfile, MapControl mco) throws Exception {

	IndexedShpDriver drv = createShpDriver(shpfile);
	DBFDriver dbf_drv = createDbfDriver(shpfile);
	FieldDescription[] sorted_flds = getFieldDescriptions(dbf_drv);
	// ArrayList igs_list = getGeometries(drv);;
	// ArrayList vals_list = getValuesRows(drv, flds);

	LayerDefinition ld = new LayerDefinition();
	ld.setFieldsDesc(sorted_flds);

	MapSheetGrid msg = MapSheetGrid.createMapSheetGrid(
		MapSheetGrid.createNewName(),
		mco.getProjection(),
		ld);

	Value def_val = null;

	for (int i=0; i<sorted_flds.length; i++) {

	    def_val = getDefValueForType(sorted_flds[i].getFieldType());
	    msg.getTheMemoryDriver().addDefault(
		    sorted_flds[i].getFieldName(),
		    def_val);

	}

	HashMap atts_hm = null;
	Value[] row = null;
	IGeometry ig = null;

	int sz = drv.getShapeCount();
	for (int i=0; i<sz; i++) {

	    // order:
	    //
	    // CODE string
	    // SCALE
	    // DIMX_CM
	    // DIMY_CM
	    // OVERLAP
	    // ROT_RAD
	    // others
	    ig = drv.getShape(i);
	    row = getValues(dbf_drv, sorted_flds, i);; // (Value[]) vals_list.get(i);
	    atts_hm = new HashMap();

	    atts_hm.put(MapSheetGrid.ATT_NAME_CODE, row[0]);
	    atts_hm.put(MapSheetGrid.ATT_NAME_SCALE, row[1]);
	    atts_hm.put(MapSheetGrid.ATT_NAME_DIMX_CM, row[2]);
	    atts_hm.put(MapSheetGrid.ATT_NAME_DIMY_CM, row[3]);
	    atts_hm.put(MapSheetGrid.ATT_NAME_OVERLAP, row[4]);
	    atts_hm.put(MapSheetGrid.ATT_NAME_ROT_RAD, row[5]);

	    for (int j=6; j<row.length; j++) {
		atts_hm.put(sorted_flds[j].getFieldName(), row[j]);
	    }

	    msg.addSheet(ig, atts_hm);
	}

	drv.close();
	drv = null;
	dbf_drv.close();
	dbf_drv = null;

	//before adding the new layer, delete all MapSheetGrids in TOC
	FLayers layersInTOC = mco.getMapContext().getLayers();
	for (int i=0; i<layersInTOC.getLayersCount(); i++) {
	    if (layersInTOC.getLayer(i) instanceof MapSheetGrid) {
		mco.getMapContext().getLayers().removeLayer(layersInTOC.getLayer(i));
	    }
	}
	mco.getMapContext().getLayers().addLayer(msg);

	MapSheetsUtils.setOnlyActive(msg, mco.getMapContext().getLayers());
    }



    public static MapSheetGrid loadMapSheetsGrid(File gridfile) throws Exception {
	String encoding = XMLEncodingUtils.getEncoding(new FileInputStream(gridfile));
	InputStreamReader reader=null;
	MapSheetGrid msg;
	if (encoding!=null) {
	    try {
		reader = new InputStreamReader(new FileInputStream(gridfile), encoding);
	    } catch (UnsupportedEncodingException e) {
		reader = new InputStreamReader(new FileInputStream(gridfile));
	    }
	} else {
	    reader = new InputStreamReader(new FileInputStream(gridfile));
	}
	XmlTag tag = (XmlTag) XmlTag.unmarshal(reader);
	XMLEntity xml=new XMLEntity(tag);
	msg = new MapSheetGrid();
	msg.setXMLEntity(xml);

	return msg;
    }



    public static void saveMapSheetsGrid(File gridfile, MapSheetGrid msg) throws Exception {
	if(gridfile.exists()){
	    int resp = JOptionPane.showConfirmDialog(
		    (Component) PluginServices.getMainFrame(),PluginServices.getText(MapSheetsUtils.class,"gridfile_already_exists"),
		    PluginServices.getText(MapSheetsUtils.class,"guardar"), JOptionPane.YES_NO_OPTION);
	    if (resp != JOptionPane.YES_OPTION) {
		return;
	    }
	}
	// write it out as XML
	try {
	    FileOutputStream fos = new FileOutputStream(gridfile.getAbsolutePath());
	    OutputStreamWriter writer = new OutputStreamWriter(fos, ProjectExtension.PROJECTENCODING);
	    Marshaller m = new Marshaller(writer);
	    m.setEncoding(ProjectExtension.PROJECTENCODING);
	    XMLEntity xml = msg.getXMLEntity();
	    m.marshal(xml.getXmlTag());
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(
		    (Component) PluginServices.getMainFrame(),PluginServices.getText(MapSheetsUtils.class,"error_writing_project")+":\n-"
			    + PluginServices.getText(MapSheetsUtils.class,"the_user_cannot_edit_the_project_because_it_has_not_write_permissions")+".",
			    PluginServices.getText(MapSheetsUtils.class,"warning"), JOptionPane.OK_OPTION);
	}
    }



    private static Value getDefValueForType(int ty) {

	switch (ty) {
	case Types.INTEGER:
	    return ValueFactory.createValue(0);
	case Types.DOUBLE:
	    return ValueFactory.createValue(0d);
	case Types.FLOAT:
	    return ValueFactory.createValue(0f);
	case Types.VARCHAR:
	    return ValueFactory.createValue("");
	default:
	    return ValueFactory.createNullValue();
	}
    }



    private static DBFDriver createDbfDriver(File shpfile) throws Exception {

	DBFDriver resp = new DBFDriver();
	String shpfn = shpfile.getAbsolutePath();
	File dbff = new File(shpfn.substring(0, shpfn.length()-3) + "dbf");
	resp.open(dbff);
	return resp;
    }



    private static Value[] getValues(
	    DBFDriver dbfdrv,
	    FieldDescription[] flds,
	    int n) throws Exception {

	Value[] resp = null;
	FieldDescription[] dbf_flds_desc = dbfdrv.getTableDefinition().getFieldsDesc();
	resp = new Value[dbf_flds_desc.length];
	int a = 0;
	for (int i=0; i<resp.length; i++) {
	    a = getIndexOfField(flds[i].getFieldName(), dbf_flds_desc);
	    resp[i] = dbfdrv.getFieldValue(n,a);
	}
	return resp;
    }



    private static int getIndexOfField(String name, FieldDescription[] flds) {

	FieldDescription desc = null;
	for (int i=0; i<flds.length; i++) {
	    desc = flds[i];
	    if (desc.getFieldName().compareToIgnoreCase(name) == 0) {
		return i;
	    }
	}
	return -1;
    }

    private static FieldDescription[] getFieldDescriptions(DBFDriver dbf_drv) throws Exception {

	ITableDefinition td = dbf_drv.getTableDefinition();

	FieldDescription[] iniflds = td.getFieldsDesc();
	FieldDescription[] sorted_flds = new FieldDescription[iniflds.length];

	for (int i=0; i<MapSheetGrid.SORTED_GRID_FIELD_NAMES.length; i++) {
	    sorted_flds[i] = getFieldDesc(
		    iniflds,
		    MapSheetGrid.SORTED_GRID_FIELD_NAMES[i]);
	}

	for (int i=0; i<(sorted_flds.length-MapSheetGrid.SORTED_GRID_FIELD_NAMES.length); i++) {
	    sorted_flds[MapSheetGrid.SORTED_GRID_FIELD_NAMES.length + i] =
		    getFieldDescNotIn(
			    iniflds,
			    MapSheetGrid.SORTED_GRID_FIELD_NAMES,
			    i);
	}
	// CODE string
	// SCALE
	// DIMX_CM
	// DIMY_CM
	// OVERLAP
	// ROT_RAD
	// others
	return sorted_flds;
    }



    private static FieldDescription getFieldDescNotIn(
	    FieldDescription[] flds,
	    String[] notnames,
	    int n) {


	FieldDescription resp = null;
	int len = flds.length;
	int count = 0;
	for (int i=0; i<len; i++) {
	    resp = flds[i];
	    if (!isIn(resp.getFieldName(), notnames)) {
		if (count == n) {
		    return resp;
		}
		count++;
	    }
	}
	return null;

    }



    private static boolean isIn(String str, String[] strs) {

	int len = strs.length;
	for (int i=0; i<len; i++) {
	    if (str.compareToIgnoreCase(strs[i]) == 0) {
		return true;
	    }
	}
	return false;
    }



    private static FieldDescription getFieldDesc(
	    FieldDescription[] flds,
	    String name) {

	FieldDescription resp = null;
	int len = flds.length;
	for (int i=0; i<len; i++) {
	    resp = flds[i];
	    if (resp.getFieldName().compareToIgnoreCase(name) == 0) {
		return resp;
	    }
	}
	return null;
    }



    private static IndexedShpDriver createShpDriver(File shpfile) throws Exception {

	IndexedShpDriver drv = null;

	drv = new IndexedShpDriver();
	drv.open(shpfile);
	drv.initialize();

	return drv;
    }



    public static String validMapSheetsGridShp(File shpf) throws Exception {

	DBFDriver dbf_drv = createDbfDriver(shpf);
	String resp = "";
	FieldDescription[] flds = dbf_drv.getTableDefinition().getFieldsDesc();
	int len = MapSheetGrid.SORTED_GRID_FIELD_NAMES.length;
	for (int i=0; i<len; i++) {
	    if (!isOneOf(MapSheetGrid.SORTED_GRID_FIELD_NAMES[i], flds)) {
		resp = resp + " " + MapSheetGrid.SORTED_GRID_FIELD_NAMES[i];
	    }
	}

	dbf_drv.close();
	dbf_drv = null;


	// TODO Auto-generated method stub
	if (resp.length() == 0) {
	    return null;
	} else {
	    String str = PluginServices.getText(MapSheetsUtils.class, "Missing_fields");
	    return str + " : " + resp;
	}
    }



    private static boolean isOneOf(String str, FieldDescription[] flds) {

	int len = flds.length;
	for (int i=0; i<len; i++) {
	    if (flds[i].getFieldName().compareToIgnoreCase(str) == 0) {
		return true;
	    }
	}
	return false;
    }


    public static MapSheetGrid getActiveMapSheetGrid(MapContext mx) {

	ArrayList act_lyrs = MapSheetsUtils.getActiveLayers(mx.getLayers());
	if ((act_lyrs.size() == 1) && (act_lyrs.get(0) instanceof MapSheetGrid)) {
	    MapSheetGrid grid = (MapSheetGrid) act_lyrs.get(0);
	    return grid;
	} else {
	    return null;
	}

    }

    public static boolean componentOf(Class cls, Behavior be) {

	if (be instanceof CompoundBehavior) {

	    CompoundBehavior co = (CompoundBehavior) be;
	    int len = co.size();
	    for (int i=0; i<len; i++) {
		if (co.getBehavior(i).getClass() == cls) {
		    return true;
		}
	    }
	    return false;

	} else {
	    return cls == be.getClass();
	}
    }



    public static Object findIn(Class wanted_class,
	    Behavior possibly_compound) {

	if (possibly_compound instanceof CompoundBehavior) {
	    CompoundBehavior co = (CompoundBehavior) possibly_compound;
	    int len = co.size();
	    for (int i=0; i<len; i++) {
		if (co.getBehavior(i).getClass() == wanted_class) {
		    return co.getBehavior(i);
		}
	    }
	    return null;

	} else {
	    if (possibly_compound.getClass() == wanted_class) {
		return possibly_compound;
	    } else {
		return null;
	    }
	}

    }



    public static Rectangle2D undetectableChange(Rectangle2D r) {

	double w = r.getWidth();
	Rectangle2D resp = new Rectangle2D.Double(
		r.getMinX() + w * 0.000001,
		r.getMinY(),
		w,
		r.getHeight());
	return resp;
    }



    public static void setOnlyActive(FLyrDefault lyr, FLayers root) {
	root.setAllActives(false);
	lyr.setActive(true);
    }





    public static Point getPosFor(Graphics2D g, java.awt.Rectangle _bb,
	    String str) {

	Rectangle2D strbb = g.getFontMetrics().getStringBounds(str, g);
	if (strbb.getWidth() > (_bb.width-2)) {
	    return null;
	} else {
	    Point resp = new Point();
	    resp.x = (int) (Math.round(_bb.width-strbb.getWidth()) / 2);
	    resp.y = _bb.height - ((int) (Math.round(_bb.height-strbb.getHeight()) / 2));

	    resp.x = resp.x + _bb.x;
	    resp.y = resp.y + _bb.y;
	    return resp;
	}
    }



    //
    public static void setGridsToVisible(FLayers lyrs, boolean b) {

	int cnt = lyrs.getLayersCount();
	FLayer lyr = null;
	for (int i=0; i<cnt; i++) {

	    lyr = lyrs.getLayer(i);
	    if (lyr instanceof FLayers) {
		setGridsToVisible((FLayers) lyr, b);
	    } else {
		if (lyr instanceof MapSheetGrid) {
		    lyr.setVisible(b);
		}
	    }
	}
    }

    public static double getWHRatio(MapSheetGrid gri) throws Exception {

	FGraphic fg = gri.getGraphic(0);
	if (fg != null && fg instanceof MapSheetGridGraphic) {
	    MapSheetGridGraphic msgg = (MapSheetGridGraphic) fg;
	    Rectangle2D r = msgg.getGeom().getBounds2D();
	    return r.getWidth() / r.getHeight();
	} else {
	    return 1;
	}
    }


    public static void initViewPort(ProjectView pv, double whratio) {

	Dimension dim = pv.getMapContext().getViewPort().getImageSize();
	double newh = (1.0d * dim.width) / whratio;
	int newhi = Math.round((float) newh);
	dim = new Dimension(dim.width,newhi);
	pv.getMapContext().getViewPort().setImageSize(dim);
    }



    public static MapContext cloneMapContextRemoveGrids(MapContext mco) {

	XMLEntity xml = null;
	MapContext resp = null;

	try {
	    xml = mco.getXMLEntity();
	    resp = MapContext.createFromXML(xml);
	    FLayers lyrs = resp.getLayers();
	    removeLayers(lyrs, MapSheetGrid.class);
	} catch (Exception ex) {
	    logger.warn("Expected exception: " + ex.getMessage());
	}

	return resp;
    }


    public static void removeLayers(FLayers lyrs, Class clazz) {

	int cnt = lyrs.getLayersCount();
	for (int i=0; i<cnt; i++) {

	    if (lyrs.getLayer(i).getClass() == clazz) {
		lyrs.removeLayer(i);
		removeLayers(lyrs, clazz);
	    } else {
		if (lyrs.getLayer(i) instanceof FLayers) {
		    removeLayers((FLayers) lyrs.getLayer(i), clazz);
		}
	    }
	}
    }

    public static void joinRadioButtons(ArrayList lis) {

	ButtonGroup bg = new ButtonGroup();
	AbstractButton ab = null;
	AbstractButton ab0 = null;
	int len = lis.size();
	for (int i=0; i<len; i++) {
	    ab = (AbstractButton) lis.get(i);
	    bg.add(ab);
	    if (i==0) {
		ab0 = ab;
	    }
	}
	ab0.setSelected(true);
    }



    public static void checkFrameListensToViewPort(FFrameView fra) {
	fra.getView().getMapContext().getViewPort().addViewPortListener(fra);
    }

    public static PrintQuality dpiToPrintQuality(int dpi) {

	if (dpi > 400) {
	    return PrintQuality.HIGH;
	} else {
	    if (dpi > 120) {
		return PrintQuality.NORMAL;
	    } else {
		return PrintQuality.DRAFT;
	    }
	}

    }

    public static int dpiToPrintQualityIndex(int dpi) {

	if (dpi > 400) {
	    return Attributes.HIGH;
	} else {
	    if (dpi > 120) {
		return Attributes.NORMAL;
	    } else {
		return Attributes.DRAFT;
	    }
	}

    }



    public static boolean validCode(MapSheetGrid gri, String str) throws Exception {

	if (str == null) {
	    return false;
	}

	SheetMemoryDriver drv = gri.getTheMemoryDriver();
	long cnt = drv.getRowCount();
	Value cod_ite = null;
	String cod_str = "";
	for (long i=0; i<cnt; i++) {
	    cod_ite = drv.getFieldValue(i, 0);
	    cod_str = cod_ite.toString();
	    if (cod_str.compareToIgnoreCase(str) == 0) {
		return false;
	    }
	}
	return true;
    }

    public static Color argbToColor(String argb_str) {

	if (argb_str == null || argb_str.length() < 3) {
	    return Color.BLACK;
	}
	String[] parts = argb_str.split(",");
	String a,r,g,b;
	int ai,ri,gi,bi;

	if (parts.length == 3) {
	    r = parts[0];
	    g = parts[1];
	    b = parts[2];

	    try {
		ai = 255;
		ri = Integer.parseInt(r.trim());
		gi = Integer.parseInt(g.trim());
		bi = Integer.parseInt(b.trim());
	    } catch (Exception ex) {
		logger.error("Bad ARGB string: " + argb_str);
		return Color.BLACK;
	    }

	} else {
	    if (parts.length == 4) {
		a = parts[0];
		r = parts[1];
		g = parts[2];
		b = parts[3];

		try {
		    ai = Integer.parseInt(a.trim());
		    ri = Integer.parseInt(r.trim());
		    gi = Integer.parseInt(g.trim());
		    bi = Integer.parseInt(b.trim());
		} catch (Exception ex) {
		    logger.error("Bad ARGB string: " + argb_str);
		    return Color.BLACK;
		}

	    } else {
		return Color.BLACK;
	    }
	}

	return new Color(ri,gi,bi,ai);



    }



    public static String rectToStr(Rectangle2D r) {

	return "" + r.getMinX()
		+ ";" + r.getMinY()
		+ ";" + r.getWidth()
		+ ";" + r.getHeight();
    }

    public static Rectangle2D strToRect(String str) {

	try {
	    String[] parts = str.split(";");
	    double x = Double.parseDouble(parts[0]);
	    double y = Double.parseDouble(parts[1]);
	    double w = Double.parseDouble(parts[2]);
	    double h = Double.parseDouble(parts[3]);
	    return new Rectangle2D.Double(x,y,w,h);
	} catch (Exception ex) {
	    return new Rectangle2D.Double(-1,-1,2,2);
	}

    }

    public static ArrayList getAllLayersFrom(FLayers lyrs) {

	ArrayList resp = new ArrayList();
	ArrayList aux_list = null;
	FLayer lyr = null;
	int cnt = lyrs.getLayersCount();
	for (int i=0; i<cnt; i++) {
	    lyr = lyrs.getLayer(i);
	    if (lyr instanceof FLayers) {
		aux_list = getAllLayersFrom((FLayers) lyr);
		resp.addAll(aux_list);
	    } else {
		resp.add(lyr);
	    }
	}
	return resp;
    }


    public static ProjectView cloneProjectView(ProjectView _pv) {

	MapContext _mc = _pv.getMapContext();
	MapContext _omc = _pv.getMapOverViewContext();

	MapContext clo_mc =
		MapSheetsUtils.cloneMapContextRemoveGrids(_mc);
	MapContext clo_omc =
		MapSheetsUtils.cloneMapContextRemoveGrids(_omc);

	Dimension aux_dim = _mc.getViewPort().getImageSize();
	clo_mc.getViewPort().setImageSize(aux_dim);

	ProjectView cloned_pv = new ProjectView();

	cloned_pv.setName(_pv.getName());
	cloned_pv.setProjectDocumentFactory(new ProjectViewFactory());
	cloned_pv.setMapContext(clo_mc);
	cloned_pv.setMapOverViewContext(clo_omc);
	return cloned_pv;

    }

    public static Project getProject() {
	return ((ProjectExtension)PluginServices.getExtension(ProjectExtension.class)).getProject();
    }

    /**
     *
     * @param gr
     * @param iv
     * @return how it is after calling this
     */
    public static boolean reverseSelection(FLyrVect gr, int iv) {

	SelectionSupport ss = gr.getSelectionSupport();
	if (ss.isSelected(iv)) {
	    ss.getSelection().set(iv, false);
	    return false;
	} else {
	    ss.getSelection().set(iv, true);
	    return true;
	}


    }

    public static PrinterJob printerJob = PrinterJob.getPrinterJob();

    public static void printMapSheetsLayout(
	    final MapSheetsLayoutTemplate tem,
	    final FLayer background_layer,
	    final boolean user_wants_printer_setts) {

	try {
	    printerJob.setPrintable(new MapSheetsPrint());

	    PluginServices.backgroundExecution(new Runnable() {
		public void run() {
		    FLayer cloned = null;
		    if (background_layer != null) {
			cloned = cloneBackgroundLayer(background_layer);
			if (cloned != null) {
			    cloned.setVisible(true);
			    MapSheetsUtils.addBackLayer(tem, cloned);
			}
		    }

		    if (tem.getLayoutContext().getAtributes().getType() == Attributes.PREPARE_PAGE_ID_CUSTOM) {
			tem.showPrintDialog(printerJob, user_wants_printer_setts);
		    } else {
			tem.showPrintDialog(null, user_wants_printer_setts);
		    }

		    if (cloned != null) {
			MapSheetsUtils.removeLayer(tem, cloned);
		    }
		}
	    });
	} catch (Exception e) {
	    NotificationManager.addError("While doing printing job. ", e);
	}


    }

    public static void setFactoryInActiveFrames(Layout lyt) {

	IFFrame[] ffs = lyt.getLayoutContext().getFFrames();
	MapSheetsFrameText aux;
	for (int i=0; i<ffs.length; i++) {
	    if (ffs[i] instanceof MapSheetsFrameText) {
		aux = (MapSheetsFrameText) ffs[i];
		aux.setFrameLayoutFactory(MapSheetsLayoutTemplate.textFrameFactory);
	    }
	}
    }

    public static void forceTargetFolderCreation() {

	File dest = new File(System.getProperty("user.home") + File.separator + "mapsheets");
	if (!dest.exists()) {
	    dest = new File(System.getProperty("user.home") + File.separator + "mapsheets" + File.separator + "xxxxx.txt");
	    dest.getParentFile().mkdirs();
	    dest.delete();
	}

    }

    public static boolean printerSettingsSaveRestore(String name, String fname, boolean save) {

	// RUNDLL32 PRINTUI.DLL,PrintUI /Sr /n "printer" /a "file.dat"
	String cmd = "";
	// "rundll32 printui.dll PrintUIEntry /e /n \"" + nam + "\"";
	// rundll32 printui.dll,PrintUIEntry /f "results.txt" /Xg /n "printer"

	if (save) {
	    cmd = "RUNDLL32 PRINTUI.DLL,PrintUIEntry /n \"" + name + "\" /Ss /a \"" + fname + "\"";
	} else {
	    cmd = "RUNDLL32 PRINTUI.DLL,PrintUIEntry /n \"" + name + "\" /Sr /a \"" + fname + "\" d g u";
	    // rundll32 printui.dll,PrintUIEntry /n "HP LaserJet 5M" /Sr /a laserjet-5M-settings.dat d g u
	}

	try {
	    Runtime.getRuntime().exec( cmd );
	    Thread.sleep(100);
	} catch (Exception exc) {
	    logger.warn("While trying to " + (save ? "save" : "restore") + " printer opts: " + exc.getMessage());
	    NotificationManager.addWarning("Did not " + (save ? "save" : "restore") + " printer settings. ", exc);
	    return false;
	}
	return true;
    }

    private static void streamToFile(InputStream ins, String outfname) {

	try {
	    File f = new File(System.getProperty("user.home") + File.separator + outfname);
	    OutputStream out=new FileOutputStream(f);
	    byte buf[]=new byte[1024];

	    int len;
	    while((len=ins.read(buf))>0) {
		out.write(buf,0,len);
	    }

	    out.close();
	}
	catch (IOException e){}



    }


    public static int[] filterMapSheetGridSheets(ListModel model, MapSheetGrid grid, String fieldName, Value value) {
	int field = -1;
	List<FieldDescription> descriptions;
	try {
	    descriptions = grid.getFieldDescs();
	    for (int i=0; i<descriptions.size(); i++) {
		if (descriptions.get(i).getFieldName().equals(fieldName)) {
		    field = i;
		    break;
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	if (field == -1) {
	    return new int[0];
	}

	List<Integer> sheets = new ArrayList<Integer>();

	for(int n=0; n<model.getSize(); n++) {
	    if (((SheetComboItem) (model.getElementAt(n))).getObject()
		    .getAttributes()[field].equals((Object)value)) {
		sheets.add(n);
	    }
	}

	int[] integers = new int[sheets.size()];

	for (int n=0; n<integers.length; n++) {
	    integers[n] = sheets.get(n).intValue();
	}

	return integers;
    }

    private static FLayer cloneBackgroundLayer(FLayer lyr) {

	FLayer resp = null;
	try {
	    resp = lyr.cloneLayer();
	} catch (Exception exc) {
	    //	    ApplicationLocator.getManager().messageDialog(
	    //		    Messages.getText("_Error_while_processing_bg_layer") +
	    //		    ":    \n\n" + exc.getMessage(),
	    //		    Messages.getText("Print_in_progress"),
	    //		    JOptionPane.ERROR_MESSAGE);
	    logger.info("Error while processing bg layer", exc);
	    resp = null;
	}
	return resp;
    }
}
