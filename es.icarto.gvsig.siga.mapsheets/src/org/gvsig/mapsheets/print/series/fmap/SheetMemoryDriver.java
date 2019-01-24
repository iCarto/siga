package org.gvsig.mapsheets.print.series.fmap;

import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.gvsig.mapsheets.print.series.MapSheetsCreationExtension;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.driver.exceptions.WriteDriverException;
import com.hardcode.gdbms.engine.data.driver.ObjectDriver;
import com.hardcode.gdbms.engine.data.edition.DataWare;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueCollection;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.BoundedShapes;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.utiles.XMLEntity;

/**
 * In-memory vector driver to keep map sheet grids.
 *  
 * @author jldominguez
 *
 */
public class SheetMemoryDriver extends SelectableDataSource
implements VectorialDriver, ObjectDriver, BoundedShapes {

	public static final String NAME = "SheetMemoryDriver";

	private HashMap defaultValue = new HashMap();
	
	// private ArrayList _geoms = new ArrayList();
	// private ArrayList rows = new ArrayList();
	private ArrayList graphics = new ArrayList();

	private Rectangle2D extent = new Rectangle2D.Double(-180, -90, 360, 180);
	
	public MapSheetGridGraphic addFeature(IGeometry ig, Value[] ats) {
		
		MapSheetGridGraphic msg = new MapSheetGridGraphic(ig, 0, ats);
		graphics.add(msg);
		// geoms.add(ig);
		Rectangle2D r = (Rectangle2D) ig.getBounds2D().clone();
		if (graphics.size() == 1) {
			extent = r;
		} else {
			extent.add(r);
		}
		return msg;
	}
	
	public ArrayList getGraphics() {
		return graphics;
	}
	
	public SheetMemoryDriver(SheetAttributeProvider empty_ds) throws Exception {
		super(empty_ds);
	}
	
	public long getRowCount() throws ReadDriverException {
		return getShapeCount();
	}
	
	public void addGraphic(MapSheetGridGraphic msg, boolean update_ext) {
		graphics.add(msg);

		if (update_ext) {
			Rectangle2D r = (Rectangle2D) msg.getGeom().getBounds2D().clone();
			if (graphics.size() == 1) {
				extent = r;
			} else {
				extent.add(r);
			}
		}
	}
	
	public boolean removeGraphic(MapSheetGridGraphic msg, boolean update_ext) {
		
		boolean resp = graphics.remove(msg);
		if (resp && update_ext) {
			updateExtent();
		}
		return resp;
	}
	
	public void addDefault(String fld_name, Value v){
		defaultValue.put(fld_name, v);
	}
	
	public Value getDefault(String fld_name) {
		Object obj = defaultValue.get(fld_name);
		if (obj != null) {
			return (Value) obj; 
		} else {
			return ValueFactory.createNullValue();
		}
	}
	
	
	public String getName() {
		return NAME;
	}

	public int getShapeType() {
		return FShape.POLYGON;
	}


	
    /**
     * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldValue(long, int)
     */
    public Value getFieldValue(long rowIndex, int fieldId) throws ReadDriverException {
    	
    	if (rowIndex >= graphics.size()) {
    		throw new ReadDriverException(
    				this.getName(),
    				new Exception(
    				"Index out of bounds: " + rowIndex + " (size = " + graphics.size() + ")"));
    	}
    	
    	MapSheetGridGraphic item = (MapSheetGridGraphic) graphics.get((int) rowIndex);
    	return item.getAttributes()[fieldId];
    }
    


	public Rectangle2D getFullExtent() throws ReadDriverException,
			ExpansionFileReadException {
		
		return extent;
	}


	public IGeometry getShape(int index) throws ReadDriverException {
		
    	if (index >= graphics.size()) {
    		throw new ReadDriverException(
    				this.getName(),
    				new Exception(
    				"Index out of bounds: " + index + " (size = " + graphics.size() + ")"));
    	}
    	
    	MapSheetGridGraphic item = (MapSheetGridGraphic) graphics.get(index);
    	return item.getGeom();
	}
	
	public MapSheetGridGraphic getGraphic(int index) throws Exception {
    	if (index >= graphics.size()) {
    		throw new Exception(
    				"Index out of bounds: " + index + " (size = " + graphics.size() + ")");
    	}
    	MapSheetGridGraphic item = (MapSheetGridGraphic) graphics.get(index);
    	return item;
	}



	public int getShapeCount() throws ReadDriverException {
		return graphics.size();
	}
	
	public ArrayList getGeoms(boolean cloned) {
		ArrayList resp = new ArrayList();
		int len = graphics.size();
		
		MapSheetGridGraphic item = null;

		if (cloned) {
			for (int i=0; i<len; i++) {
				item = (MapSheetGridGraphic) graphics.get(i);
				resp.add(item.getGeom().cloneGeometry());
			}
		} else {
			for (int i=0; i<len; i++) {
				item = (MapSheetGridGraphic) graphics.get(i);
				resp.add(item.getGeom());
			}
		}
		return resp;
	}
	
	public ArrayList getCodes() {
		
		ArrayList resp = new ArrayList();
		int n = graphics.size();
		MapSheetGridGraphic item = null;
		String str = null;
		
		for (int i=0; i<n; i++) {
			item = (MapSheetGridGraphic) graphics.get(i);
			str = item.getAttributes()[0].toString();
			resp.add(str);
		}
		return resp;
	}
	

	public Rectangle2D getShapeBounds(int index) throws ReadDriverException,
			ExpansionFileReadException {
		return getShape(index).getBounds2D();
	}


	public int getShapeType(int index) throws ReadDriverException {
		return getShapeType();
	}
	
	public boolean removeGraphic(int index) throws Exception {
    	if (index >= graphics.size()) {
    		return false;
    	}
    	graphics.remove(index);
    	updateExtent();
    	return true;
	}
	
	public boolean removeGraphic(IGeometry ig) throws Exception {
		
		int n = graphics.size();
		MapSheetGridGraphic item = null;
		for (int i=0; i<n; i++) {
			item = (MapSheetGridGraphic) graphics.get(i);
			if (item.getGeom() == ig) {
				graphics.remove(item);
				updateExtent();
				return true;
			}
		}
		return false;
		
	}
	




	public int getHighestXXCode() throws Exception {
		
		long len = getRowCount();
		String it_code = "";
		int max_resp = 0;
		int aux_int = 0;
		int pref_sep_len =
			MapSheetGrid.CODE_NEW_PREF.length() +
			MapSheetsCreationExtension.CODE_ID_SEPARATOR.length();
		
		for (long i=0; i<len; i++) {
			it_code = getFieldValue(i, 0).toString();
			if (it_code.indexOf(
					MapSheetGrid.CODE_NEW_PREF +
					MapSheetsCreationExtension.CODE_ID_SEPARATOR) == 0) {
				
				it_code = it_code.substring(pref_sep_len);
				try {
					aux_int = Integer.parseInt(it_code);
				} catch (Exception exc) {
					continue;
				}
				if (aux_int > max_resp) {
					max_resp = aux_int;
				}
			}
		}
		return max_resp;
	}
	
	
	
	public void setXMLEntity(XMLEntity xml) throws XMLException {

		String ext_str = xml.getStringProperty("extent");
		extent = MapSheetsUtils.strToRect(ext_str);
		// ===========================================
		XMLEntity ch_def = xml.getChild(0);
		XMLEntity ch_item = null;
		Object[] aux = null;
		String aux_str = null;
		Value aux_v = null;
		int cnt = ch_def.getChildrenCount();
		for (int i=0; i<cnt; i++) {
			
			ch_item = ch_def.getChild(i);
			aux = nameDefValFromXML(ch_item);
			aux_str = (String) aux[0];
			aux_v = (Value) aux[1];
			addDefault(aux_str, aux_v);
		}
		// ===========================================
		XMLEntity ch_gra = xml.getChild(1);
		cnt = ch_gra.getChildrenCount();
		MapSheetGridGraphic aux_i = null;
		for (int i=0; i<cnt; i++) {
			ch_item = ch_gra.getChild(i);
			aux_i = new MapSheetGridGraphic();
			aux_i.setXMLEntity(ch_item);
			graphics.add(aux_i);
		}

		
	}
	
	public XMLEntity getXMLEntity() throws XMLException {
		
		XMLEntity xml = new XMLEntity();
		// ==============================
		String ext_str = MapSheetsUtils.rectToStr(extent);
		xml.putProperty("extent", ext_str);
		// ==============================
		XMLEntity ch_def = new XMLEntity();
		
		Iterator iter = defaultValue.keySet().iterator();
		String name = "";
		Value v = null;
		XMLEntity item_ch = null;
		while (iter.hasNext()) {
			name = (String) iter.next();
			v = (Value) defaultValue.get(name);
			item_ch = createChild(name, v);
			ch_def.addChild(item_ch);
		}
		xml.addChild(ch_def);
		// ==============================
		XMLEntity ch_gra = new XMLEntity();
		
		iter = graphics.iterator();
		MapSheetGridGraphic item_gra = null;
		
		while (iter.hasNext()) {
			item_gra = (MapSheetGridGraphic) iter.next(); 
			item_ch = item_gra.getXMLEntity();
			ch_gra.addChild(item_ch);
		}
		xml.addChild(ch_gra);
		// ==============================
		return xml;
	}
	
	
	
	
	
	private XMLEntity createChild(String nm, Value v) {
		XMLEntity resp = new XMLEntity();
		resp.putProperty("name", nm);
		resp.putProperty("type", v.getSQLType());
		resp.putProperty("default", v.toString());
		return resp;
	}
	
	private Object[] nameDefValFromXML(XMLEntity xml) throws XMLException {
		Object[] resp = new Object[2];
		
		String name = xml.getStringProperty("name");
		int type = xml.getIntProperty("type");
		String vstr = xml.getStringProperty("default");
		Value v;
		try {
			v = ValueFactory.createValueByType(vstr, type);
		} catch (ParseException e) {
			throw new XMLException(e);
		}
		resp[0] = name;
		resp[1] = v;
		return resp;
	}
	
	
	public void updateExtent() {
		
		ArrayList geoms = this.getGeoms(true);
		if (geoms.size() == 0) {
			extent = new Rectangle2D.Double(-180, -90, 360, 180);
		} else {
			
			IGeometry ig = null;
			if (geoms.size() == 1) {
				ig = (IGeometry) geoms.get(0);
				extent = (Rectangle2D) ig.getBounds2D().clone();
			} else {
				
				ig = (IGeometry) geoms.get(0);
				Rectangle2D new_ext = (Rectangle2D) ig.getBounds2D().clone();
				int len = geoms.size();
				for (int i=1; i<len; i++) {
					ig = (IGeometry) geoms.get(i);
					new_ext.add((Rectangle2D) ig.getBounds2D().clone());
				}
				extent = new_ext;
			}
		}
	}

	

	//       ============
	// empty ============
	//       ============
	public DriverAttributes getDriverAttributes() {
		return null;
	}

	public boolean isWritable() {
		return true;
	}

	private int[] pk = {0};
	public int[] getPrimaryKeys() throws ReadDriverException {
		return pk;
	}

    public void setValues(int row, Value[] values) throws WriteDriverException, ReadDriverException {
	    ((MapSheetGridGraphic) graphics.get(row)).setAttributes(values);
    }

	public void write(DataWare dataWare) throws WriteDriverException, ReadDriverException { }

    public void beginTrans() throws ReadDriverException { }
    public void commitTrans() throws ReadDriverException, WriteDriverException { }
    public void deleteRow(long rowId) throws WriteDriverException, ReadDriverException { }
    public void insertEmptyRow(ValueCollection pk) throws WriteDriverException { }
    public void insertFilledRow(Value[] values) throws WriteDriverException, ReadDriverException { }
    public void rollBackTrans() throws ReadDriverException, WriteDriverException { }






}
