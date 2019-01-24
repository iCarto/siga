package org.gvsig.mapsheets.print.series.fmap;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.mapsheets.print.series.MapSheetsCreationExtension;
import org.gvsig.mapsheets.print.series.tool.MapSheetsDragger;
import org.gvsig.mapsheets.print.series.utils.IMapSheetsIdentified;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.crs.CRSFactory;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.LayerDefinition;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectionSupport;
import com.iver.cit.gvsig.fmap.layers.VectorialDefaultAdapter;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.cit.gvsig.fmap.rendering.FGraphic;
import com.iver.cit.gvsig.fmap.rendering.SingleSymbolLegend;
import com.iver.cit.gvsig.fmap.tools.Behavior.Behavior;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;

/**
 * This subclass of {@link FLyrVect} is used to handle the map sheets grid
 * when it's not being adjusted. The draw method only acts when this grid is
 * not active as grid and being adjusted.
 * 
 * @author jldominguez
 *
 */
public class MapSheetGrid extends FLyrVect implements  IMapSheetsIdentified {

	private static Logger logger = Logger.getLogger(MapSheetGrid.class);
	
	private static int name_id = 1;
	
	/**
	 * Driver that provides data and shepes
	 */
	private SheetMemoryDriver theSmd = null;

	/**
	 * Sets driver that provides shapes and attributes
	 * @param s
	 */
	public void setTrueSource(SheetMemoryDriver s) {
		theSmd = s;
		theSmd.addDefault(ATT_NAME_CODE, ValueFactory.createValue(""));
		theSmd.addDefault(ATT_NAME_SCALE, ValueFactory.createValue(1d));
		theSmd.addDefault(ATT_NAME_DIMX_CM, ValueFactory.createValue(1d));
		theSmd.addDefault(ATT_NAME_DIMY_CM, ValueFactory.createValue(1d));
		theSmd.addDefault(ATT_NAME_OVERLAP, ValueFactory.createValue(10d));
		theSmd.addDefault(ATT_NAME_ROT_RAD, ValueFactory.createValue(0d));
		theSmd.addDefault(ATT_NAME_MUNIC, ValueFactory.createValue(""));
	}
	
	/**
	 * Constants for essential attribute names
	 */
	public static final String ATT_NAME_CODE = "CODE";
	public static final String ATT_NAME_SCALE = "SCALE";
	public static final String ATT_NAME_DIMX_CM = "DIMX_CM";
	public static final String ATT_NAME_DIMY_CM = "DIMY_CM";
	public static final String ATT_NAME_OVERLAP = "OVERLAP";
	public static final String ATT_NAME_ROT_RAD = "ROT_RAD";
	public static final String ATT_NAME_MUNIC = "MUNICIPIO";
	public static final String[] SORTED_GRID_FIELD_NAMES = {
		ATT_NAME_CODE,
		ATT_NAME_SCALE,
		ATT_NAME_DIMX_CM,
		ATT_NAME_DIMY_CM,
		ATT_NAME_OVERLAP,
		ATT_NAME_ROT_RAD,
		ATT_NAME_MUNIC
	};

	public static final String CODE_NEW_PREF = "XX";
	
	public SheetMemoryDriver getTheMemoryDriver() {
		return theSmd;
	}
	
	public MapSheetGrid() {
		setId(System.currentTimeMillis());
	}

	/**
	 * Instantiates a map sheet grid.
	 * 
	 * @param name
	 * @param proj
	 * @param ld
	 * @param mco
	 * @return
	 * @throws Exception
	 */
	public static MapSheetGrid createMapSheetGrid(
			String name, IProjection proj, LayerDefinition ld) throws Exception  {
		
		MapSheetGrid resp = new MapSheetGrid();
		resp.setName(name);
		
		SheetAttributeProvider oddsa = new SheetAttributeProvider(ld);
		SheetMemoryDriver smd = new SheetMemoryDriver(oddsa);
		resp.setTrueSource(smd);
		
		oddsa.setDriver(smd);
		resp.setRecordset(smd);
		
		VectorialDefaultAdapter adap = new VectorialDefaultAdapter();
		adap.setDriver(smd);
		resp.setSource(adap);
		
		resp.setProjection(proj);

		SingleSymbolLegend ssl = new SingleSymbolLegend();
		ssl.setDefaultSymbol(MapSheetsUtils.getFrameSymbol_normal());

		resp.setLegend(ssl);
		resp.setRecordset(adap.getRecordset());
		return resp;
	}
	
	/**
	 * Adds a sheet from geometry and attributes in an array
	 * @param ig
	 * @param vv
	 * @return
	 */
	public MapSheetGridGraphic addSheet(IGeometry ig, Value[] vv) {
		return theSmd.addFeature(ig, vv);
	}

	/**
	 * Adds a sheet from geometry and attributes in a {@link HashMap}
	 * @param ig
	 * @param hm
	 * @return
	 * @throws Exception
	 */
	public MapSheetGridGraphic addSheet(IGeometry ig, HashMap hm) throws Exception {
		
		Value[] vv = null;
		vv = extractAtts(hm);
		return theSmd.addFeature(ig, vv);
	}
	
	private Value[] extractAtts(HashMap atts) throws Exception {
		
		int sz = theSmd.getFieldCount(); // fieldDescs.size();
		Value[] resp = new Value[sz];
		Object obj = null;
		for (int i=0; i<sz; i++) {
			// fd = (FieldDescription) fieldDescs.get(i);
			obj = (atts == null) ? null : atts.get(theSmd.getFieldName(i));
			if (obj == null) {
				resp[i] = theSmd.getDefault(theSmd.getFieldName(i));
			} else {
				resp[i] = (Value) obj;
			}
		}
		return resp;
	}
	

	/**
	 * Draws the layer only if grid is not being adjusted.
	 * When being adjusted, the tool will do the fast drawing
	 * to allow fast adjustments
	 */
	public void draw(
			BufferedImage bi,
			Graphics2D g,
			ViewPort vp,
			Cancellable ca,
			double sca) throws ReadDriverException {
		
		if (isActiveAsGrid()) {
			// ACTIVE, DO NOT PAINT
		} else {
			// NOT ACTIVE, PAINT NORMALLY
			super.draw(bi, g, vp, ca, sca);
		}
		
	}

	/**
	 * 
	 * @return whether this grid is active and being adjusted
	 */
	public boolean isActiveAsGrid() {

		IWindow iw = PluginServices.getMDIManager().getActiveWindow();
		if (iw == null || (!(iw instanceof View))) {
			return false;
		}
		
		View v = (View) iw;
		MapControl mco = v.getMapControl();
		Behavior be = mco.getCurrentMapTool();
		MapSheetsDragger msd = (MapSheetsDragger)
			MapSheetsUtils.findIn(MapSheetsDragger.class, be);
		
		if (msd != null) {
			return (msd.getGrid() == this);
		} else {
			return false;
		}
		 
	}

	/**
	 * Removes the first sheet that contains the provided point
	 * 
	 * @param map_p
	 * @return the removed sheet so caller can do something with it
	 * @throws Exception
	 */
	public MapSheetGridGraphic removeGraphicContaining(Point2D map_p) throws Exception {
		
		ArrayList gras = this.getTheMemoryDriver().getGraphics();
		int len = gras.size();
		MapSheetGridGraphic fgra = null;
		IGeometry ig = null;
		
		for (int i=0; i<len; i++) {
			fgra = (MapSheetGridGraphic) gras.get(i);
			ig = fgra.getGeom();
			
			if (ig.contains(map_p)) {
				
				FBitSet fbs = getSelectionSupport().getSelection();
				if (fbs.get(i) && fbs.cardinality() > 1) {
					// GUI will handle
					throw new RemoveSelectedException(fbs.cardinality());
				} else {
					// not sel or one sel, remove this one
					ArrayList sel_geoms = getSelectedGraphics();
					theSmd.removeGraphic(fgra, true);
					setSelectedGraphics(sel_geoms);
				}
				return fgra;
			}
		}
		return null;
	}


	/**
	 * Sets provided list of {@link FGraphic} as selected in this layer
	 * @param sel_graphs
	 */
	public void setSelectedGraphics(ArrayList sel_graphs) {
		
		SelectionSupport ss = this.getSelectionSupport();
    	FBitSet fbs = ss.getSelection();
    	fbs.clear();
    	
    	ArrayList al = this.getTheMemoryDriver().getGraphics();
    	
    	int len = al.size();
    	FGraphic item = null;
    	int ind = 0;
    	
    	for (int i=0; i<len; i++) {
    		item = (FGraphic) al.get(i);
    		if (sel_graphs.contains(item)) {
    			fbs.set(i);
    		}
    	}
	}

	/**
	 * 
	 * @return array of {@link FGraphic} currently selected 
	 */
	public ArrayList getSelectedGraphics() {
		ArrayList resp = new ArrayList();
		SelectionSupport ss = this.getSelectionSupport();
    	FBitSet fbs = ss.getSelection();
    	ArrayList al = this.getTheMemoryDriver().getGraphics();
    	
    	for(int i=fbs.nextSetBit(0); i>=0; i=fbs.nextSetBit(i+1)) {
    		resp.add(al.get(i));
    	}
		return resp;
	}
	

	/**
	 * 
	 * @param cloned whether geometries must be cloned before returning
	 * to avoid changes in original geometries
	 * 
	 * @return array of geometries of this layer 
	 */
	public ArrayList getGeometries(boolean cloned) {
		return theSmd.getGeoms(cloned);
	}

	/**
	 * 
	 * @return array of codes of layer's features (main attribute)
	 */
	public ArrayList getCodes() {
		return theSmd.getCodes();
	}

	
	public static String createNewName() {
		
		return PluginServices.getText(MapSheetsUtils.class, "Grid") + "-" + (name_id++);
	}

	public static LayerDefinition createDefaultLyrDesc() {
		
		LayerDefinition resp = new LayerDefinition();
		FieldDescription[] flds = new FieldDescription[7];
		
		FieldDescription fld = null;
		
		fld = new FieldDescription();
		fld.setFieldName(ATT_NAME_CODE);
		fld.setFieldLength(10);
		fld.setFieldDecimalCount(10);
		fld.setFieldType(Types.VARCHAR);
		flds[0] = fld;
		
		fld = new FieldDescription();
		fld.setFieldName(ATT_NAME_SCALE);
		fld.setFieldLength(10);
		fld.setFieldDecimalCount(10);
		fld.setFieldType(Types.DOUBLE);
		flds[1] = fld;
		
		fld = new FieldDescription();
		fld.setFieldName(ATT_NAME_DIMX_CM);
		fld.setFieldLength(10);
		fld.setFieldDecimalCount(10);
		fld.setFieldType(Types.DOUBLE);
		flds[2] = fld;
		
		fld = new FieldDescription();
		fld.setFieldName(ATT_NAME_DIMY_CM);
		fld.setFieldLength(10);
		fld.setFieldDecimalCount(10);
		fld.setFieldType(Types.DOUBLE);
		flds[3] = fld;

		fld = new FieldDescription();
		fld.setFieldName(ATT_NAME_OVERLAP);
		fld.setFieldLength(10);
		fld.setFieldDecimalCount(10);
		fld.setFieldType(Types.DOUBLE);
		flds[4] = fld;

		fld = new FieldDescription();
		fld.setFieldName(ATT_NAME_ROT_RAD);
		fld.setFieldLength(10);
		fld.setFieldDecimalCount(10);
		fld.setFieldType(Types.DOUBLE);
		flds[5] = fld;

		fld = new FieldDescription();
		fld.setFieldName(ATT_NAME_MUNIC);
		fld.setFieldLength(10);
		fld.setFieldDecimalCount(10);
		fld.setFieldType(Types.VARCHAR);
		flds[6] = fld;

		resp.setFieldsDesc(flds);
		return resp;
	}

	public MapSheetGridGraphic getGraphic(int i) throws Exception {
		return theSmd.getGraphic(i);
	}



	/*
	public void addGeometry(IGeometry new_rect) {
		// TODO Auto-generated method stub
	}
	*/

	public ArrayList getFieldDescs() throws Exception  {
		
		ArrayList resp = new ArrayList();
		
		int n = theSmd.getFieldsDescription().length;
		for (int i=0; i<n; i++) {
			resp.add(theSmd.getFieldsDescription()[i]);
		}
		return resp;
	}

	public HashMap getCommonAtts() throws Exception {
		
		HashMap resp = new HashMap();
		/*
		 * 
		 */
		// ATT_NAME_SCALE,
		// ATT_NAME_DIMX_CM,
		// ATT_NAME_DIMY_CM,
		// ATT_NAME_OVERLAP
		Value v = null;
		if (theSmd.getShapeCount() > 0) {
			v = theSmd.getFieldValue(0, 1);
			resp.put(ATT_NAME_SCALE, v);
			v = theSmd.getFieldValue(0, 2);
			resp.put(ATT_NAME_DIMX_CM, v);
			v = theSmd.getFieldValue(0, 3);
			resp.put(ATT_NAME_DIMY_CM, v);
			v = theSmd.getFieldValue(0, 4);
			resp.put(ATT_NAME_OVERLAP, v);
		}
		return resp;
	}

	public String createNewCode() throws Exception {
		int high_xx_ind = theSmd.getHighestXXCode();
		high_xx_ind++;
		return MapSheetGrid.CODE_NEW_PREF + MapSheetsCreationExtension.CODE_ID_SEPARATOR + high_xx_ind; 
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
	


	
	public void setXMLEntity(XMLEntity xml) throws XMLException {
		
		String aux = xml.getStringProperty("name");
		setName(aux);
		aux = xml.getStringProperty("srs");
		IProjection proj = CRSFactory.getCRS(aux);
		msid = xml.getLongProperty("msid");
		
		
		LayerDefinition ld = new LayerDefinition();
		ld.setName("");
		ld.setShapeType(FShape.POLYGON);
		ld.setProjection(proj);
		
		XMLEntity child_ff = xml.getChild(0);
		FieldDescription[] ff = getFldsFromXml(child_ff);
		ld.setFieldsDesc(ff);
		
		SheetAttributeProvider oddsa = new SheetAttributeProvider(ld);
		SheetMemoryDriver smd = null;
		try {
			smd = new SheetMemoryDriver(oddsa);
			XMLEntity child_drv = xml.getChild(1);
			smd.setXMLEntity(child_drv);
		} catch (Exception e) {
			throw new XMLException(e);
		}
		setTrueSource(smd);
		oddsa.setDriver(smd);
		setRecordset(smd);
		
		VectorialDefaultAdapter adap = new VectorialDefaultAdapter();
		adap.setDriver(smd);
		setSource(adap);
		setProjection(proj);

		SingleSymbolLegend ssl = new SingleSymbolLegend();
		ssl.setDefaultSymbol(MapSheetsUtils.getFrameSymbol_normal());
		try {
			setLegend(ssl);
			setRecordset(adap.getRecordset());
		} catch (Exception e) {
			throw new XMLException(e);
		}
	}
	
	public XMLEntity getXMLEntity() throws XMLException {
		
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", this.getClass().getCanonicalName());
		xml.putProperty("name", this.getName());
		xml.putProperty("msid", msid);
		xml.putProperty("srs", this.getProjection().getAbrev());
		
		FieldDescription[] flds;
		try {
			flds = theSmd.getFieldsDescription();
		} catch (ReadDriverException e) {
			throw new XMLException(e);
		} 
		XMLEntity flds_ch = getXmlFromFlds(flds); 
		xml.addChild(flds_ch);
		XMLEntity ch_drv = theSmd.getXMLEntity();
		xml.addChild(ch_drv);
		return xml;
	}

	private XMLEntity getXmlFromFlds(FieldDescription[] flds) {
		XMLEntity resp = new XMLEntity();
		
		XMLEntity chi = null;
		FieldDescription fd;
		for (int i=0; i<flds.length; i++) {
			chi = new XMLEntity();
			fd = flds[i];
			chi.putProperty("name", fd.getFieldName());
			chi.putProperty("type", fd.getFieldType());
			chi.putProperty("len", fd.getFieldLength());
			chi.putProperty("dec", fd.getFieldDecimalCount());
			resp.addChild(chi);
		}
		return resp;
	}
	
	private FieldDescription[] getFldsFromXml(XMLEntity xml) {
		
		int cnt = xml.getChildrenCount();
		FieldDescription[] resp = new FieldDescription[cnt];
		
		XMLEntity chi = null;
		String _name;
		int _type, _len, _dec;
		
		for (int i=0; i<cnt; i++) {
			chi = xml.getChild(i);
			_name = chi.getStringProperty("name");
			_type = chi.getIntProperty("type");
			_len = chi.getIntProperty("len");
			_dec = chi.getIntProperty("dec");
			resp[i] = new FieldDescription();
			resp[i].setFieldName(_name);
			resp[i].setFieldType(_type);
			resp[i].setFieldLength(_len);
			resp[i].setFieldDecimalCount(_dec);
		}
		return resp;
	}

	public MapSheetGridGraphic[] removeSelected() {
		
		ArrayList gras = getTheMemoryDriver().getGraphics();
		ArrayList sel_arr = new ArrayList();
		
    	SelectionSupport ss = getSelectionSupport();
    	FBitSet fbs = ss.getSelection();

    	for(int i=fbs.nextSetBit(0); i>=0; i=fbs.nextSetBit(i+1)) {
    		sel_arr.add(gras.get(i));
    	}
    	
    	int len = sel_arr.size();
    	MapSheetGridGraphic item = null;
    	
    	for (int i=0; i<len; i++) {
    		item = (MapSheetGridGraphic) sel_arr.get(i);
    		getTheMemoryDriver().removeGraphic(item, false);
    	}
    	
    	getTheMemoryDriver().updateExtent();
		fbs.clear();
		
		MapSheetGridGraphic[] resp = null;
		resp = (MapSheetGridGraphic[]) sel_arr.toArray(new MapSheetGridGraphic[0]);
		return resp;
	}
	
	public boolean isWritable() {
		return true;
	}


}
