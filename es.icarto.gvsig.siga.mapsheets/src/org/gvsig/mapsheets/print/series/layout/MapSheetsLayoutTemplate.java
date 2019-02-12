package org.gvsig.mapsheets.print.series.layout;

import java.awt.Component;
import java.awt.geom.Rectangle2D;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrintQuality;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.gvsig.mapsheets.print.audasa.AudasaTemplate;
import org.gvsig.mapsheets.print.series.MapSheetsPrintExtension;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGridGraphic;
import org.gvsig.mapsheets.print.series.print.MapSheetsPrint;
import org.gvsig.mapsheets.print.series.utils.IMapSheetsIdentified;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.DoubleValue;
import com.hardcode.gdbms.engine.values.NumericValue;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.documents.exceptions.OpenException;
import com.iver.cit.gvsig.project.documents.exceptions.SaveException;
import com.iver.cit.gvsig.project.documents.layout.Attributes;
import com.iver.cit.gvsig.project.documents.layout.Size;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrame;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameOverView;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameScaleBar;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameText;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameTextFactory;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameView;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameViewFactory;
import com.iver.cit.gvsig.project.documents.layout.fframes.FrameFactory;
import com.iver.cit.gvsig.project.documents.layout.fframes.IFFrame;
import com.iver.cit.gvsig.project.documents.layout.gui.Layout;
import com.iver.cit.gvsig.project.documents.view.ProjectView;
import com.iver.utiles.XMLEntity;

/**
 * This subclass of Layout provides special behavior: can be associated to a grid;
 * some of its components will can be refreshed with data from a single sheet.
 *
 * @author jldominguez
 *
 */
public class MapSheetsLayoutTemplate extends Layout implements IMapSheetsIdentified {

    private static Logger logger = Logger.getLogger(MapSheetsLayoutTemplate.class);

    private MapSheetGrid grid = null;
    private ProjectView pView = null;

    //	private FFrameText titleFrame = null;
    private FFrameOverView localFrame = null;

    private IFFrame[] framesFromTemplate = null;
    private AudasaTemplate audasaTemplate = null;

    private final ArrayList<IFFrame> framesDependentOnView = new ArrayList<IFFrame>();

    public static FrameFactory textFrameFactory = null;
    public static FrameFactory viewFrameFactory = null;

    private boolean printSelectedOnly = false;

    private static int NextId = 1;

    private double leftCm;

    private double topCm;

    static {

	try {
	    IFFrame txtf =
		    FFrameTextFactory.createFrameFromName(FFrameTextFactory.registerName);
	    textFrameFactory = txtf.getFrameLayoutFactory();
	} catch (Throwable ex) {
	    textFrameFactory = new FFrameTextFactory();
	}

	try {
	    IFFrame viewf =
		    FFrameTextFactory.createFrameFromName(FFrameViewFactory.registerName);
	    viewFrameFactory = viewf.getFrameLayoutFactory();
	} catch (Throwable th) {
	    viewFrameFactory = new FFrameViewFactory();
	}

    }

    public static int nextId() {
	return NextId++;
    }

    public MapSheetsLayoutTemplate() {
	super();
	setId(System.currentTimeMillis());
    }


    public MapSheetsLayoutTemplate(MapSheetGrid gr,
	    ProjectView pv,
	    Layout size_dpi_layout,
	    AudasaTemplate audasaTemplate) {

	super();
	setId(System.currentTimeMillis());

	grid = gr;
	pView = pv;

	// set attributes
	Attributes param_atts =
		size_dpi_layout.getLayoutContext().getAtributes();
	Attributes atts = getLayoutContext().getAtributes();
	atts.getAttributes().addAll(param_atts.getAttributes());

	Size forced_size = getPaperSize(size_dpi_layout);
	atts.m_sizePaper = new Size(forced_size.getAlto(), forced_size.getAncho());
	atts.DPI = size_dpi_layout.getLayoutContext().getAtributes().DPI;

	int res_ind = MapSheetsUtils.dpiToPrintQualityIndex(atts.DPI);
	atts.setResolution(res_ind);

	PrintQuality pq = MapSheetsUtils.dpiToPrintQuality(atts.DPI);
	PrintRequestAttributeSet pras = getLayoutContext().getAtributes().getAttributes();
	pras.add(pq);
	atts.m_area = size_dpi_layout.getLayoutContext().getAtributes().getMargins();

	atts.setType(param_atts.getType());
	atts.setUnit(param_atts.getSelTypeUnit());
	atts.setIsLandScape(param_atts.isLandSpace());

	framesFromTemplate = size_dpi_layout.getLayoutContext().getAllFFrames();
	size_dpi_layout.getLayoutContext().nums.clear();
	this.audasaTemplate = audasaTemplate;
    }

    private Size getPaperSize(Layout size_dpi_layout) {

	Attributes atts = size_dpi_layout.getLayoutContext().getAtributes();
	int page_fmt_type = atts.getType();

	Size aux_size = null;

	if (page_fmt_type == Attributes.PREPARE_PAGE_ID_CUSTOM) {
	    aux_size = atts.getPaperSize();
	} else {
	    if (page_fmt_type == Attributes.PREPARE_PAGE_ID_PRINT) {
		aux_size = atts.getPaperSize();
	    } else {
		aux_size = Attributes.sizeForId(page_fmt_type);

		/*
				if (atts.getPageFormat().getOrientation() != PageFormat.LANDSCAPE) {
				    aux_size = new Size(aux_size.getAlto(),aux_size.getAncho());
				} else {
					aux_size = new Size(aux_size.getAncho(),aux_size.getAlto());
				}
		 */
	    }
	}
	return aux_size;
    }

    public MapSheetFrameView getMainViewFrame() {
	IFFrame[] ffs = this.getLayoutContext().getFFrames();
	for (int i=0; i<ffs.length; i++) {
	    if (ffs[i] instanceof MapSheetFrameView) {
		return (MapSheetFrameView) ffs[i];
	    }
	}
	return null;
    }

    public ProjectView getProjectView() {
	return pView;
    }

    public MapSheetGrid getGrid() {
	return grid;
    }

    public void setViewGrid(ProjectView pv, MapSheetGrid gr) {
	pView = pv;
	grid = gr;

	MapSheetFrameView msfv = getMainViewFrame();
	msfv.setView(pView);

	for (IFFrame frame : framesDependentOnView) {
	    if((frame instanceof FFrameOverView) &&
		    frame != null) {
		((FFrameOverView) frame).setFFrameDependence(msfv);
		((FFrameOverView) frame).setView(pView);
		((FFrameOverView) frame).setLayout(this);
	    } else if ((frame instanceof FFrameScaleBar) &&
		    frame != null) {
		((FFrameScaleBar) frame).setFFrameDependence(msfv);
	    }
	    //else if (frame instanceof FFrameLegend) {
	    // legendFrame.setFFrameDependence(viewFrame);
	    // legendFrame.setLayout(this);
	    //}
	}

	MapSheetsUtils.setFactoryInActiveFrames(this);

	updateViewFrameSize();

	try {
	    update(0);
	} catch (Exception ex) {
	    logger.error("While updating with 0: " + ex.getMessage());
	}

    }

    public void addLinkedFrame(int i, MapSheetsFrameText textf) {
	// frameToAttIndex.put(textf, new Integer(i));
	textf.setAttIndex(i);
	getLayoutContext().addFFrame(textf, true, false);
    }

    private void updateViewFrameSize() {

	DoubleValue grid_width = null;
	DoubleValue grid_height = null;

	try {
	    grid_width = (DoubleValue) grid.getTheMemoryDriver().getFieldValue(0, 2);
	    grid_height = (DoubleValue) grid.getTheMemoryDriver().getFieldValue(0, 3);

	    double w_cm = grid_width.doubleValue();
	    double h_cm = grid_height.doubleValue();

	    MapSheetFrameView msfv = getMainViewFrame();
	    msfv.setBoundBox(new Rectangle2D.Double(
		    msfv.getBoundBox().getMinX(),
		    msfv.getBoundBox().getMinY(),
		    w_cm, h_cm));
	    this.repaint();

	} catch (Exception ex) {
	    NotificationManager.addError("While getting w/h. ", ex);
	}


    }


    public void init(ArrayList fd_list,
	    ArrayList ind_list,
	    ArrayList tem_list,
	    double left_cm,
	    double top_cm,
	    boolean is_test) {

        this.leftCm = left_cm;
        this.topCm = top_cm;

	Attributes atts = getLayoutContext().getAtributes();
	double paper_w_cm = atts.m_sizePaper.getAncho();
	double paper_h_cm = atts.m_sizePaper.getAlto();

	double top = atts.m_area[0];
	double bot = atts.m_area[1];
	double lef = atts.m_area[2];
	double rig = atts.m_area[3];

	DoubleValue grid_width = null;
	DoubleValue grid_height = null;

	try {
	    grid_width = (DoubleValue) grid.getTheMemoryDriver().getFieldValue(0, 2);
	    grid_height = (DoubleValue) grid.getTheMemoryDriver().getFieldValue(0, 3);
	} catch (Exception ex) {
	    NotificationManager.addError("While getting w/h. ", ex);
	}

	double w_cm = grid_width.doubleValue();
	double h_cm = grid_height.doubleValue();

	double owh_ratio = w_cm / h_cm;

	boolean has_ov = false;

	if (pView.getMapOverViewContext() != null
		&& pView.getMapOverViewContext().getViewPort() != null
		&& pView.getMapOverViewContext().getViewPort().getAdjustedExtent() != null) {

	    has_ov = true;
	    Rectangle2D ov_ae = pView.getMapOverViewContext().getViewPort().getAdjustedExtent();
	    owh_ratio = ov_ae.getWidth() / ov_ae.getHeight();
	}

	/* _________________
	 * |            [18%] tit
	 * |            [10%] cod
	 * |    M A P   [10%] esc
	 * |            [res] leg
	 * |            [prp] loc
	 * ------------------
	 */
	double right_col_wit_cm = 12d * paper_w_cm / 80d;
	if (right_col_wit_cm < 5) {
	    right_col_wit_cm = 5;
	}
	if (right_col_wit_cm > 10) {
	    right_col_wit_cm = 10;
	}
	double right_col_sep_cm = 0.4;
	double right_col_pos_cm = paper_w_cm - rig - right_col_wit_cm;
	double av_height_cm = paper_h_cm - top - bot;
	double botto_col_cm = paper_h_cm - bot;
	// ========================================================



	// ========================================================
	double title_top_cm = top;
	double title_hei_cm = 0.125 * av_height_cm;

	int n = fd_list.size();
	double act_flds_hei_cm = n * 0.1 * av_height_cm + (n-1) * right_col_sep_cm;
	double act_flds_top_cm = title_top_cm + title_hei_cm + right_col_sep_cm;

	double local_hei_cm = has_ov ? (right_col_wit_cm / owh_ratio) : 0;
	double local_top_cm = botto_col_cm - local_hei_cm;

	/*
		double legen_top_cm = act_flds_top_cm + act_flds_hei_cm + right_col_sep_cm;
		double legen_hei_cm =
			av_height_cm - (n+2) * right_col_sep_cm
			- local_hei_cm - act_flds_hei_cm - title_hei_cm;
	 */
	// ==============================================
	MapSheetFrameView viewFrame = new MapSheetFrameView();
	viewFrame.setBoundBox(new Rectangle2D.Double(
		left_cm, top_cm,
		w_cm, h_cm));
	viewFrame.setFrameLayoutFactory(viewFrameFactory);

	viewFrame.setLayout(this);
	viewFrame.setName("main_frame");
	viewFrame.setTypeScale(MapSheetFrameView.MANUAL);
	NumericValue scale;
	try {
	    scale = ((NumericValue) grid.getCommonAtts().get(grid.ATT_NAME_SCALE));
	    viewFrame.setScale(scale.doubleValue());
	} catch (Exception e) {
	    e.printStackTrace();
	}
	viewFrame.setView(pView);

	if (is_test) {
	    getLayoutContext().getEFS().startComplexCommand();
	    getLayoutContext().addFFrame(viewFrame, true, false);
	    // getLayoutContext().getEFS().endComplexCommand("Added frame");
	} else {
	    getLayoutContext().addFFrame(viewFrame, true, false);
	}

	// ==============================================
	//		if (is_test) {
	//			titleFrame = new FFrameText();
	//		} else {
	//			titleFrame = (FFrameText)
	//			FFrameTextFactory.createFrameFromName(FFrameTextFactory.registerName);
	//		}
	//		titleFrame.setName("title");
	//		titleFrame.addText("[Escribir el título aquí]");
	//		titleFrame.setPos(FFrameText.CENTER);
	//		titleFrame.setBoundBox(new Rectangle2D.Double(
	//				right_col_pos_cm,
	//				title_top_cm,
	//				right_col_wit_cm,
	//				title_hei_cm));
	//		titleFrame.setLayout(this);
	//
	//		if (is_test) {
	//
	//			getLayoutContext().getEFS().startComplexCommand();
	//			getLayoutContext().addFFrame(titleFrame, true, false);
	//			// getLayoutContext().getEFS().endComplexCommand("Added frame");
	//
	//		} else {
	//			getLayoutContext().addFFrame(titleFrame, true, false);
	//		}

	// ==============================================
	//		if (has_ov) {
	//
	//		    	localFrame = (FFrameOverView) FFrameOverViewFactory.createFrameFromName(
	//		    		FFrameOverViewFactory.registerName);
	//			localFrame.setName("local");
	//			localFrame.setFFrameDependence(viewFrame);
	//			localFrame.setView(pView);
	//			localFrame.setBoundBox(new Rectangle2D.Double(
	//				right_col_pos_cm,
	//				local_top_cm,
	//				right_col_wit_cm,
	//				local_hei_cm));
	//			localFrame.setLayout(this);
	//			localFrame.setName("Overview frame");
	//
	//			if (is_test) {
	//				getLayoutContext().getEFS().startComplexCommand();
	//				getLayoutContext().addFFrame(localFrame, true, false);
	//			} else {
	//				getLayoutContext().addFFrame(localFrame, true, false);
	//			}
	//
	//		}

	// ==============================================
	// act fields
	MapSheetsFrameText tem_txt = null;
	FieldDescription _fd_ = null;
	String _tem_ = null;
	Integer fld_ind = null;
	for (int i=0; i<n; i++) {
	    _fd_ = (FieldDescription) fd_list.get(i);
	    _tem_ = (String) tem_list.get(i);
	    tem_txt = new MapSheetsFrameText(_tem_);
	    tem_txt.setFrameLayoutFactory(textFrameFactory);
	    tem_txt.setBoundBox(new Rectangle2D.Double(
		    right_col_pos_cm,
		    act_flds_top_cm + i * (0.1 * av_height_cm + right_col_sep_cm),
		    right_col_wit_cm,
		    0.1 * av_height_cm));
	    tem_txt.setPos(FFrameText.CENTER);
	    tem_txt.setLayout(this);

	    fld_ind = (Integer) ind_list.get(i);
	    addLinkedFrame(fld_ind, tem_txt);
	}
	MapSheetFrameView msfv = getMainViewFrame();
	// ==============================================
	// load frames from template
	//audasaTemplate.setProperty("numero_hoja", (String) grid.getTheMemoryDriver().getCodes().get(0));
	for (int i=0; i<framesFromTemplate.length; i++) {
	    getLayoutContext().addFFrame(framesFromTemplate[i], true, true);
	    if(framesFromTemplate[i] instanceof FFrameText) {
		if(audasaTemplate.hasKey(((FFrameText) framesFromTemplate[i]).getTitle())) {
		    ((FFrameText) framesFromTemplate[i]).addText(
			    audasaTemplate.getProperty(((FFrameText) framesFromTemplate[i]).getTitle()));
		    //		} else {
		    //		    ((FFrameText) framesFromTemplate[i]).addText(" ");
		}
	    } else if (framesFromTemplate[i] instanceof FFrameScaleBar) {
		((FFrameScaleBar) framesFromTemplate[i]).setFFrameDependence(viewFrame);
		framesDependentOnView.add(framesFromTemplate[i]);
	    } else if (framesFromTemplate[i] instanceof FFrameOverView) {
		if (has_ov) {
		    localFrame = (FFrameOverView) framesFromTemplate[i];
		    localFrame.setFFrameDependence(msfv);
		    localFrame.setView(pView);
		    localFrame.setLayout(this);
		    framesDependentOnView.add(localFrame);
		}
	    }
	}
    }

    public void update(int i)  throws Exception {

	MapSheetGridGraphic gri = grid.getGraphic(i);
	updateWithSheet(gri);
	String code = (String) grid.getCodes().get(i);
	updateAudasaSheetCode(code);
    }

    public void updateWithSheet(MapSheetGridGraphic gri) {

	MapSheetFrameView msfv = getMainViewFrame();
	MapSheetsUtils.checkFrameListensToViewPort(msfv);
	updateView(msfv, gri.getGeom());
	IFFrame[] ffs = this.getLayoutContext().getFFrames();
	MapSheetsFrameText aux;
	Value v;
	for (int i=0; i<ffs.length; i++) {
	    if (ffs[i] instanceof MapSheetsFrameText) {
		aux = (MapSheetsFrameText) ffs[i];
		v = gri.getAttributes()[aux.getAttIndex()];
		updateFrame(aux, v);
	    }
	}
	this.repaint();
    }

    private void updateFrame(IFFrame frame, Value va) { // , int index) {

	if (!(frame instanceof MapSheetsFrameText)) {
	    return;
	}
	MapSheetsFrameText ftxt = (MapSheetsFrameText) frame;
	String txt = "";
	if (va instanceof DoubleValue) {
	    DoubleValue dv = (DoubleValue) va;
	    txt = "" + Math.round(dv.doubleValue());
	} else {
	    txt = va.toString();
	}
	ftxt.setText(txt);
    }

    private void updateView(FFrameView vf, IGeometry geo) {
	vf.getView().getMapContext().getViewPort().setExtent(geo.getBounds2D());
    }

    /**
     * helps in persistency
     */
    private long msid = -1;
    public long getId() {
	return msid;
    }

    public void setId(long id) {
	msid = id;
	try { Thread.sleep(40); } catch (Exception e) {}
    }

    @Override
    public XMLEntity getXMLEntity() {

	XMLEntity resp = new XMLEntity();
	resp.putProperty("className", this.getClass().getName());

	XMLEntity ch_fra = new XMLEntity();
	IFFrame[] ffs = getLayoutContext().getFFrames();
	int cnt = ffs.length;
	for (int i=0; i<cnt; i++) {
	    try {
		ch_fra.addChild(ffs[i].getXMLEntity());
	    } catch (SaveException e) {
		logger.error("While getting XML: " + e.getMessage());
		return resp;
	    }
	}
	resp.addChild(ch_fra);
	/*
		XMLEntity pv_xml = null;

		try {
			pv_xml = getProjectView().getXMLEntity();
		} catch (SaveException e) {
			logger.error("While getting XML of proj view: " + e.getMessage());
			return resp;
		}

		resp.addChild(pv_xml);
		// =================================
		resp.putProperty("grid_msid", this.getGrid().getId());
	 */
	resp.putProperty("msid", msid);

	Attributes atts = getLayoutContext().getAtributes();
	resp.putProperty("paper_w", atts.m_sizePaper.getAncho());
	resp.putProperty("paper_h", atts.m_sizePaper.getAlto());
	resp.putProperty("margin_0", atts.m_area[0]);
	resp.putProperty("margin_1", atts.m_area[1]);
	resp.putProperty("margin_2", atts.m_area[2]);
	resp.putProperty("margin_3", atts.m_area[3]);
	resp.putProperty("dpi", atts.DPI);

	resp.putProperty("type", atts.getType());
	resp.putProperty("type_unit", atts.getSelTypeUnit());
	resp.putProperty("landscape", atts.isLandSpace());
	resp.putProperty("gridName", grid.getName());
	return resp;
    }

    public void setXMLEntity(XMLEntity xml, Project p) throws OpenException {

	Attributes atts = getLayoutContext().getAtributes();
	atts.DPI = xml.getIntProperty("dpi");
	double w = xml.getDoubleProperty("paper_w");
	double h = xml.getDoubleProperty("paper_h");
	atts.m_sizePaper = new Size(w,h);
	double m0 = xml.getDoubleProperty("margin_0");
	double m1 = xml.getDoubleProperty("margin_1");
	double m2 = xml.getDoubleProperty("margin_2");
	double m3 = xml.getDoubleProperty("margin_3");
	atts.m_area = new double[4];
	atts.m_area[0] = m0;
	atts.m_area[1] = m1;
	atts.m_area[2] = m2;
	atts.m_area[3] = m3;

	int au = xml.getIntProperty("type");
	atts.setType(au);
	au = xml.getIntProperty("type_unit");
	atts.setUnit(au);
	boolean boo = xml.getBooleanProperty("landscape");
	atts.setIsLandScape(boo);

	// ===================================
	msid = xml.getLongProperty("msid");
	// ===================================
	// long aux = xml.getLongProperty("grid_msid");
	// MapSheetGrid2 gri = getGridWithId(p, aux);
	// this.setGrid(gri);
	// ====================================
	XMLEntity ch_fra = xml.getChild(0);
	int cnt = ch_fra.getChildrenCount();
	XMLEntity fra_ite = null;
	IFFrame ifra = null;
	String gridName = xml.getStringProperty("gridName");
	MapSheetFrameView viewFrame = getMainViewFrame();
	for (int i=0; i<cnt; i++) {
	    fra_ite = ch_fra.getChild(i);
	    ifra = createFrame(fra_ite, p);
	    this.getLayoutContext().addFFrame(ifra, true, false);

	    if (ifra.getName().compareToIgnoreCase("main_frame") == 0) {
		viewFrame = (MapSheetFrameView) ifra;
	    }
	    //			if (ifra.getName().compareToIgnoreCase("title") == 0) {
	    //				this.titleFrame = (FFrameText) ifra;
	    //			}
	    if (ifra.getName().compareToIgnoreCase("Overview frame") == 0) {
		this.localFrame = (FFrameOverView) ifra;
	    }
	    /*
			if (ifra.getName().compareToIgnoreCase("legend") == 0) {
				this.legendFrame = (FFrameLegend) ifra;
			}
	     */
	}

	IFFrame[] frames = getLayoutContext().getAllFFrames();
	for (IFFrame frame: frames) {
	    if (frame instanceof FFrameScaleBar) {
		((FFrameScaleBar) frame).initDependence(frames);
	    }
	}


	FLayer lyr = viewFrame.getView().getMapContext().getLayers().getLayer(gridName);
	if (lyr instanceof MapSheetGrid) {
	    setViewGrid(viewFrame.getView(), (MapSheetGrid) lyr);
	}
	viewFrame.setView(MapSheetsUtils.cloneProjectView(viewFrame.getView()));

    }


    private IFFrame createFrame(XMLEntity xml, Project pro) throws OpenException {

	IFFrame resp = null;
	String cl_name = "";
	if (xml.contains("shortClassName")) {
	    cl_name = xml.getStringProperty("shortClassName");
	}

	if (cl_name.compareToIgnoreCase(MapSheetsFrameText.class.getName()) == 0) {
	    resp = new MapSheetsFrameText();
	    resp.setXMLEntity(xml);
	} else {
	    resp = FFrame.createFromXML(xml, pro, this);
	}
	return resp;
    }


    private MapSheetGrid getGridWithId(Project p, long gri_id) {

	Iterator iter = p.getDocuments().iterator();
	ProjectView pv = null;
	Object obj = null;
	MapSheetGrid resp = null;
	while (iter.hasNext()) {
	    obj = iter.next();
	    if (obj instanceof ProjectView) {
		pv = (ProjectView) obj;
		resp = findGrid(pv.getMapContext().getLayers(), gri_id);
		if (resp != null) {
		    return resp;
		}
	    }
	}
	logger.warn("Grid not found in project. Grid id = " + gri_id);
	return null;
    }


    private MapSheetGrid findGrid(FLayers lyrs, long gri_id) {

	int cnt = lyrs.getLayersCount();
	FLayer lyr = null;
	MapSheetGrid resp = null;

	for (int i=0; i<cnt; i++) {
	    lyr = lyrs.getLayer(i);
	    if (lyr instanceof FLayers) {
		resp = findGrid((FLayers) lyr, gri_id);
		if (resp != null) {
		    return resp;
		}
	    } else {

		if (lyr instanceof MapSheetGrid) {
		    resp = (MapSheetGrid) lyr;
		    if (resp.getId() == gri_id) {
			return resp;
		    }
		}
	    }
	}
	return null;
    }


    private Doc aux_doc = null;

    public void showPrintDialog(PrinterJob job, boolean show_setts) {
	if (job != null) {
	    job.printDialog();

	    try {
		job.setPrintable((Printable) PluginServices
			.getExtension(com.iver.cit.gvsig.Print.class));
		job.print();
	    } catch (PrinterException e) {
		e.printStackTrace();
	    }
	} else {

	    PrintService[] aux_m_cachePrintServices = null;
	    // Actualizar attributes
	    PrintRequestAttributeSet aux_att =
		    getLayoutContext().getAtributes().toPrintAttributes();
	    PrintService aux_m_cachePrintService = null;


	    // ------------------ The Printing things --------------------- //
	    DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;

	    // returns the set of printers that support printing a specific
	    // document type (such as GIF)
	    // with a specific set of attributes (such as two sided).
	    // PrintRequestAttributeSet pras = new
	    // HashPrintRequestAttributeSet();
	    // interestingly, the printer dialog's default behavior has changed
	    // with the new API: by default the dialog is not shown.
	    // So we must use the ServiceUI class to create a print dialog
	    // returns the default print service.
	    aux_m_cachePrintServices =
		    PrintServiceLookup.lookupPrintServices(flavor, null);

	    PrintService defaultService = null;

	    if (aux_m_cachePrintService == null) {
		defaultService = PrintServiceLookup.lookupDefaultPrintService();
	    }

	    if (aux_m_cachePrintService == null
		    && defaultService == null
		    && aux_m_cachePrintServices.length > 0) {
		defaultService = aux_m_cachePrintServices[0];
	    }

	    if ((defaultService == null) && (aux_m_cachePrintService == null)) {
		JOptionPane.showMessageDialog((Component) PluginServices
			.getMainFrame(),PluginServices.getText(this,"ninguna_impresora_configurada"));

		return;
	    }


	    try {
		aux_m_cachePrintService = ServiceUI.printDialog(null, 200, 200,
			aux_m_cachePrintServices, defaultService, flavor, aux_att);

	    } catch (RuntimeException ex) {
		logger.error("Error showing print dialog", ex);
		NotificationManager.addError(ex);
	    }

	    if (aux_m_cachePrintService != null) {

		String printer_nam = aux_m_cachePrintService.getName();

		// show printer setts
		// dialog if req by user
		if (show_setts) {
		    // =============================================================================
		    // =============================================================================
		    if (MapSheetsPrintExtension.PRINTER_SETTINGS_PRINTER_NAME == null &&
			    MapSheetsPrintExtension.PRINTER_SETTINGS_RESTORE_FILE == null) {
			String fname =
				System.getProperty("user.home") + File.separator +
				"pri-sett-" + System.currentTimeMillis() + ".txt";

			boolean saved = MapSheetsUtils.printerSettingsSaveRestore(
				printer_nam, fname, true);
			if (saved) {
			    MapSheetsPrintExtension.PRINTER_SETTINGS_PRINTER_NAME = printer_nam;
			    MapSheetsPrintExtension.PRINTER_SETTINGS_RESTORE_FILE = fname;
			}
		    }
		    // =============================================================================
		    // =============================================================================

		    Process dlg_pro = showPrinterDialog(printer_nam);
		    if (dlg_pro != null) {
			try {
			    dlg_pro.waitFor();
			} catch (InterruptedException e) { }
		    }
		}
		// ***************************************
		// JOptionPane.showConfirmDialog(null, "AHORA VAMOS A IMPRIMIR");
		// ***************************************

		DocPrintJob jobNuevo = null;

		jobNuevo = aux_m_cachePrintService.createPrintJob();

		/*
                PrintJobListener pjlistener = new PrintJobAdapter() {
                    public void printDataTransferCompleted(PrintJobEvent e) {
                        getLayoutControl().fullRect();
                    }
                };
                jobNuevo.addPrintJobListener(pjlistener);
		 */


		ArrayList sel_ind = null;

		if (!isPrintSelectedOnly()) {
		    int n = 0;
		    sel_ind = new ArrayList();
		    try {
			n = getGrid().getTheMemoryDriver().getShapeCount();
			for (int i=0; i<n; i++) {
			    sel_ind.add(new Integer(i));
			}
		    } catch (ReadDriverException e) {
			NotificationManager.addError(e);
		    }
		} else {
		    sel_ind = MapSheetsUtils.getSelectedIndices(getGrid());
		}

		int count = sel_ind.size();
		Integer int_obj = null;

		for (int i=0; i<count; i++) {

		    int_obj = (Integer) sel_ind.get(i);
		    try {
			update(int_obj.intValue());
			// ==========================================
			// ==========================================
			jobNuevo = aux_m_cachePrintService.createPrintJob();
			aux_doc = new SimpleDoc(new MapSheetsPrint(this),flavor, null);
			jobNuevo.print(aux_doc, aux_att);
		    } catch (Exception e) {
			NotificationManager.addError(e);
		    }
		}

	    }
	}
    }


    private Process showPrinterDialog(String nam) {
	String cmd = "rundll32 printui.dll PrintUIEntry /e /n \"" + nam + "\"";
	try {
	    return Runtime.getRuntime().exec( cmd );
	} catch (Exception exc) {
	    logger.error("While trying to show printer opts: " + exc.getMessage());
	    NotificationManager.addError("While trying to show printer opts. ", exc);
	}
	return null;
    }

    public boolean isPrintSelectedOnly() {
	return printSelectedOnly;
    }

    public void setPrintSelectedOnly(boolean o) {
	printSelectedOnly = o;
    }

    public void updateAudasaSheetCode(String audasaSheetCode) {
	for (int i=0; i<framesFromTemplate.length; i++) {
	    if (framesFromTemplate[i] instanceof FFrameText) {
		if (((FFrameText) framesFromTemplate[i]).getTitle() != null) {
		    if (((FFrameText) framesFromTemplate[i]).getTitle().equalsIgnoreCase("numero_hoja")) {
			((FFrameText) framesFromTemplate[i]).clearText();
			((FFrameText) framesFromTemplate[i]).addText(audasaSheetCode);
		    }
		}
	    }
	}
    }

    public double getLeftCm() {
        return this.leftCm;
    }

    public double getTopCm() {
        return this.topCm;
    }

}
