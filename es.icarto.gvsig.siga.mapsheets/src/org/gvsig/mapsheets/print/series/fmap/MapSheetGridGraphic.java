package org.gvsig.mapsheets.print.series.fmap;

import java.awt.geom.Rectangle2D;

import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.cit.gvsig.fmap.rendering.FGraphic;
import com.iver.utiles.XMLEntity;

/**
 * This class holds info (atts and geometry) of a single map sheet grid.
 * Initially intended to use in a graphics layer, currently simply as a
 * info node.
 * 
 * @author jldominguez
 *
 */
public class MapSheetGridGraphic extends FGraphic {
	
	private Value[] atts = null;
	
	public MapSheetGridGraphic() {
		super(null, -1);
	}
	
	public MapSheetGridGraphic(IGeometry geom, int sym_id, Value[] a) {
		super(geom, sym_id);
		atts = a;
	}
	
	public Value[] getAttributes() {
		return atts;
	}
	
	public void setAttributes(Value[] vv) {
		atts = vv;
	}
	
	public void setXMLEntity(XMLEntity xml) throws XMLException {

		int aux_int = xml.getIntProperty("idsym");
		this.setIdSymbol(aux_int);
		
		double x = 0;
		double y = 0;
		double w = 0;
		double h = 0;
		try {
			x = xml.getDoubleProperty("bbox_x");
			y = xml.getDoubleProperty("bbox_y");
			w = xml.getDoubleProperty("bbox_w");
			h = xml.getDoubleProperty("bbox_h");
			IGeometry ig = MapSheetsUtils.rectToGeom(new Rectangle2D.Double(
					x,y,w,h));
			setGeom(ig);
		} catch (Exception ex) {
			setGeom(null);
		}
		
		int n = 0;
		try {
			n =  xml.getIntProperty("att_n");
			atts = new Value[n];
			
			String val_arr = xml.getStringProperty("att_vals"); 
			String typ_arr = xml.getStringProperty("att_types");
			String[] val_arr_split = val_arr.split("_SEP_"); 
			String[] typ_arr_split = typ_arr.split(","); 
			
			for (int i=0; i<n; i++) {
				if (i>=val_arr_split.length) {
					atts[i] = ValueFactory.createValueByType(
							"",
							Integer.parseInt(typ_arr_split[i]));
				} else {
					atts[i] = ValueFactory.createValueByType(
							val_arr_split[i],
							Integer.parseInt(typ_arr_split[i]));
				}
			}
		} catch (Exception ex) {
			atts = new Value[0];
		}

	}

	public XMLEntity getXMLEntity() throws XMLException {
		
		XMLEntity xml = new XMLEntity();
		
		xml.putProperty("idsym", getIdSymbol());

		IGeometry geo = this.getGeom();
		if (geo == null) {
		} else {
			Rectangle2D r = geo.getBounds2D();
			xml.putProperty("bbox_x", r.getMinX());
			xml.putProperty("bbox_y", r.getMinY());
			xml.putProperty("bbox_w", r.getWidth());
			xml.putProperty("bbox_h", r.getHeight());
		}
		
		if (atts != null) {
			
			int n = atts.length;
			xml.putProperty("att_n", n);
			String aux = "";
			String att_types = "";
			String att_vals = "";
			for (int i=0; i<n; i++) {
				if (i==0) {
					att_types = att_types + atts[i].getSQLType();
					att_vals = att_vals + atts[i].toString();
				} else {
					att_types = att_types + "," + atts[i].getSQLType();
					att_vals = att_vals + "_SEP_" + atts[i].toString();
				}
			}
			xml.putProperty("att_types", att_types);
			xml.putProperty("att_vals", att_vals);
		}
		return xml;
	}


	
	

}
