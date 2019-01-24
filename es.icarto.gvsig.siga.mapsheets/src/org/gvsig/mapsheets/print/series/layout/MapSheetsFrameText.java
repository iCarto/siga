package org.gvsig.mapsheets.print.series.layout;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.gvsig.mapsheets.print.series.utils.IMapSheetsIdentified;

import com.iver.cit.gvsig.project.documents.exceptions.SaveException;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameText;
import com.iver.cit.gvsig.project.documents.layout.fframes.FrameFactory;
import com.iver.cit.gvsig.project.documents.layout.fframes.IFFrame;
import com.iver.cit.gvsig.project.documents.layout.gui.Layout;
import com.iver.utiles.XMLEntity;

/**
 * Spaecial text frame that keeps a text template with a wildcard.
 * Will be refreshed for each different sheet.
 * Wildcard will be replaced with associated attribute value.
 * 
 * @author jldominguez
 *
 */
public class MapSheetsFrameText extends FFrameText implements IMapSheetsIdentified{
	
	public static String WILDCARD = "__TEXT__";
	private String template = "" + WILDCARD;
	private int attIndex = -1;
	
	public MapSheetsFrameText() {
		super();
		msid = System.currentTimeMillis();
	}
	
	public MapSheetsFrameText(String pc_template) {
		super();
		msid = System.currentTimeMillis();
		template = pc_template.replaceAll("\\*", WILDCARD);
	}
	
	public void setAttIndex(int ind) {
		attIndex = ind;
	}
	
	public int getAttIndex() {
		return attIndex;
	}
	
	public void setText(String str) {
		clearText();
		String tmp = new String(template);
		addText(tmp.replaceAll(WILDCARD, str));
	}
	
	public IFFrame cloneFFrame(Layout lyt) {
		// return super.cloneFFrame(lyt);
		return this;
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
	
	public XMLEntity getXMLEntity() throws SaveException {
		XMLEntity resp = super.getXMLEntity();
		
		resp.putProperty("shortClassName", getClass().getName());
		resp.putProperty("template", template);
		resp.putProperty("msid", this.msid);
		resp.putProperty("att_index", this.attIndex);
		
		resp.putProperty("bbox_x", getBoundBox().x);
		resp.putProperty("bbox_y", getBoundBox().y);
		resp.putProperty("bbox_w", getBoundBox().width);
		resp.putProperty("bbox_h", getBoundBox().height);
		return resp;
	}

	public void setXMLEntity (XMLEntity xml) {
		super.setXMLEntity(xml);
		template = xml.getStringProperty("template");
		msid = xml.getLongProperty("msid");
		attIndex = xml.getIntProperty("att_index");
		
		double x = xml.getDoubleProperty("bbox_x");
		double y = xml.getDoubleProperty("bbox_y");
		double w = xml.getDoubleProperty("bbox_w");
		double h = xml.getDoubleProperty("bbox_h");
		this.setBoundBox(new Rectangle2D.Double(x,y,w,h));
		
		setFrameLayoutFactory(MapSheetsLayoutTemplate.textFrameFactory);
	}
	
    public void draw(Graphics2D g, AffineTransform at, Rectangle2D rv,
            BufferedImage imgBase) {
    	
    	super.draw(g, at, rv, imgBase);
    }
    
	public void setFrameLayoutFactory(FrameFactory flf) {
		super.setFrameLayoutFactory(flf);
	}

}
