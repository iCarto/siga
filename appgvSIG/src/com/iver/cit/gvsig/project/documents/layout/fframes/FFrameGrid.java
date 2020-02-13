package com.iver.cit.gvsig.project.documents.layout.fframes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrintQuality;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.ILineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.IMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleLineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.v02.FSymbol;
import com.iver.cit.gvsig.project.documents.exceptions.SaveException;
import com.iver.cit.gvsig.project.documents.layout.FLayoutUtilities;
import com.iver.cit.gvsig.project.documents.layout.fframes.gui.dialogs.IFFrameDialog;
import com.iver.cit.gvsig.project.documents.layout.gui.Layout;
import com.iver.utiles.StringUtilities;
import com.iver.utiles.XMLEntity;



/**
 * FFrame para introducir una cuadrícula sobre una vista en el Layout.
 *
 * @author Vicente Caballero Navarro
 */
public class FFrameGrid extends FFrame implements IFFrameViewDependence{

	private FFrameView fframeview;
	private double intervalX=10000;
	private double intervalY=10000;

	private Color textColor=Color.black;
	private boolean isLine;
	private int sizeFont=8;
	private int dependenceIndex;
	private Layout layout;
//	private boolean print=false;
	private ILineSymbol symbolLine=new SimpleLineSymbol();
	private IMarkerSymbol symbolPoint=new SimpleMarkerSymbol();
	private Font font=new Font("Arial",Font.PLAIN,sizeFont);

	public void draw(Graphics2D g, AffineTransform at, Rectangle2D rv, BufferedImage imgBase) {
//		if (print){
//			fframeview.refresh();
//			print=false;
//		}
		ViewPort vp=fframeview.getMapContext().getViewPort();
//		vp.setAffineTransform(at);
		FontRenderContext frc = g.getFontRenderContext();
		double myScale = at.getScaleX() * 0.0234; //FLayoutUtilities.fromSheetDistance(folio.getAncho(),at)/rv.getWidth();
        int scaledFontSize = (int) (myScale * sizeFont);
		Font font=new Font(this.font.getFamily(),this.font.getStyle(),scaledFontSize);
		Rectangle2D.Double r = getBoundingBox(at);
		Rectangle2D rView=fframeview.getBoundingBox(at);
		g.rotate(Math.toRadians(getRotation()), r.x + (r.width / 2),
            r.y + (r.height / 2));
//        AffineTransform atView=fframeview.getATMap();

//		vp.setAffineTransform(at);
        Rectangle2D extent=vp.getAdjustedExtent();
        double extentX=extent.getMinX();
        double extentY=extent.getMinY();

        double restX=(extentX/intervalX) % 1;
        double distX=restX*intervalX;
        //double distPixelsX=FLayoutUtilities.fromSheetDistance(distX,atView);
        double restY=(extentY/intervalY) % 1;
        double distY=restY*intervalY;
        //double distPixelsY=FLayoutUtilities.fromSheetDistance(distY,atView);

        double x=extentX-distX;
        //double pixelsX = rView.getMinX()-distPixelsX;
        double y=extentY-distY;
        //double pixelsY = rView.getMinY()-distPixelsY;

        //fframeview.getMapContext().getViewPort().fromMapPoint(extentX,extentY);
        //double pixelsInterval=FLayoutUtilities.fromSheetDistance(interval,atView);

        g.setColor(Color.black);


        // Dibuja los márgenes
        double valueIntervalX=((extentX/intervalX)-restX) * intervalX-intervalX;
        x=x-intervalX;
        double topX=extentX;
//        if (showCornerText){
//        	x+=intervalX;
//        	topX-=intervalX;
//        }
        while(x<extent.getMaxX()){
        	if (x>topX) {
        		Point2D p2=vp.fromMapPoint(x,extentY);
        		Point2D p1=vp.fromMapPoint(x,extent.getMaxY());
        		if (isLine()){
        			g.setColor(symbolLine.getColor());
        			g.setStroke(new BasicStroke((int)symbolLine.getLineWidth()));
        		}else{
        			g.setColor(symbolPoint.getColor());
        			g.setStroke(new BasicStroke((int)1));
        		}
//        		g.setColor(lineColor);
        		g.drawLine((int)p1.getX(),(int)p1.getY()-5,(int)p1.getX(),(int)p1.getY());
        		g.drawLine((int)p2.getX(),(int)p2.getY(),(int)p2.getX(),(int)p2.getY()+5);
        		TextLayout textaux = new TextLayout(String.valueOf(valueIntervalX),
                        font, frc);

        		double w=textaux.getBounds().getWidth();
        		double h=textaux.getBounds().getHeight();
        		g.setColor(textColor);
        		textaux.draw(g,(int)(p1.getX()-w/2),(int)(p1.getY()-h)-5);
        		textaux.draw(g,(int)(p2.getX()-w/2),(int)(p2.getY()+h*2)+5);
        	}
        	valueIntervalX=valueIntervalX+intervalX;
        	x=x+intervalX;
        }
        double valueIntervalY=((extentY/intervalY)-restY) * intervalY-intervalY;
        y=y-intervalY;
        double topY=extentY;
//        if (showCornerText){
//        	y+=intervalY;
//        	topY-=intervalY;
//        }
        while(y<extent.getMaxY()){
        	if (y>topY) {
        		Point2D p1=vp.fromMapPoint(extentX,y);
        		Point2D p2=vp.fromMapPoint(extent.getMaxX(),y);
        		if (isLine()){
        			g.setColor(symbolLine.getColor());
        			g.setStroke(new BasicStroke((int)symbolLine.getLineWidth()));
        		}else{
        			g.setColor(symbolPoint.getColor());
        			g.setStroke(new BasicStroke((int)1));
        		}
//        		g.setColor(lineColor);
        		g.drawLine((int)p1.getX()-5,(int)p1.getY(),(int)p1.getX(),(int)p1.getY());
        		g.drawLine((int)p2.getX(),(int)p2.getY(),(int)p2.getX()+5,(int)p2.getY());
        		TextLayout textaux = new TextLayout(String.valueOf(valueIntervalY),
                        font, frc);
        		double w=textaux.getBounds().getWidth();
        		double h=textaux.getBounds().getHeight();
        		g.setColor(textColor);
        		textaux.draw(g,(int)(p1.getX()-w-10),(int)(p1.getY()+h/2));
        		textaux.draw(g,(int)p2.getX()+10,(int)(p2.getY()+h/2));
        	}
        	valueIntervalY=valueIntervalY+intervalY;
        	y=y+intervalY;
        }
        if (isLine()){
			g.setColor(symbolLine.getColor());
			g.setStroke(new BasicStroke((int)symbolLine.getLineWidth()));
		}else{
			g.setColor(symbolPoint.getColor());
			g.setStroke(new BasicStroke((int)1));
		}
//        g.setColor(lineColor);

        g.draw(rView);

        x = extentX-distX;
        y = extentY-distY;

        if (isLine) { // Dibuja las líneas.
        	 while(x<extent.getMaxX()){
        		 Point2D antPoint=vp.fromMapPoint(x,extentY);
 	        	if (x>extentX) {
 	                while(y<=extent.getMaxY()){
 	    	        	if (y>=extentY) {
 	    	        		Point2D p=vp.fromMapPoint(x,y);
 	    	        		IGeometry geom=createLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
 	    	        		geom.draw(g,new ViewPort(null),symbolLine);
 	    	        		antPoint=(Point2D)p.clone();
 	    	        	}

 	    	        	y=y+intervalY;
 	    	    	}
 	                Point2D p=vp.fromMapPoint(x,extent.getMaxY());
 	               IGeometry geom=createLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
	        		geom.draw(g,new ViewPort(null),symbolLine);
//	        		g.drawLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
	        		antPoint=(Point2D)p.clone();
 	    	        y=extentY-distY;

 	        	}


 	        	x=x+intervalX;
 	        }
        	 while(y<=extent.getMaxY()){
        		 Point2D antPoint=vp.fromMapPoint(extentX,y);
 	        	if (y>extentY) {
 	                while(x<=extent.getMaxX()){
 	    	        	if (x>=extentX) {
 	    	        		Point2D p=vp.fromMapPoint(x,y);
 	    	        		IGeometry geom=createLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
 	    	        		geom.draw(g,new ViewPort(null),symbolLine);
// 	    	        		g.drawLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
 	    	        		antPoint=p;
 	    	        	}
 	    	        	x=x+intervalX;
 	    	    	}
 	                Point2D p=vp.fromMapPoint(extent.getMaxX(),y);
	        		g.drawLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
	        		antPoint=(Point2D)p.clone();
 	    	        x=extentX-distX;
 	        	}
 	        	y=y+intervalY;
 	        }
        } else { //Dibuja los puntos
	        while(x<=extent.getMaxX()){
	        	if (x>extentX) {
	                while(y<=extent.getMaxY()){
	    	        	if (y>=extentY) {
	    	        		Point2D p=vp.fromMapPoint(x,y);
	    	        		IGeometry geom=createPoint((int)p.getX(),(int)p.getY());
 	    	        		geom.draw(g,new ViewPort(null),symbolPoint);
//	    	        		g.drawLine((int)p.getX()-10,(int)p.getY(),(int)p.getX()+10,(int)p.getY());
//	    	        		g.drawLine((int)p.getX(),(int)p.getY()-10,(int)p.getX(),(int)p.getY()+10);
	    	        	}
	    	        	y=y+intervalY;
	    	    	}
	    	        y=extentY-distY;
	        	}
	        	x=x+intervalX;
	        }

        }

        g.rotate(Math.toRadians(-getRotation()), r.x + (r.width / 2),
                r.y + (r.height / 2));
	}

	private IGeometry createLine(int i, int j, int k, int l) {
		GeneralPathX gpx=new GeneralPathX();
		gpx.moveTo(i,j);
		gpx.lineTo(k,l);
		return ShapeFactory.createPolyline2D(gpx);
	}
	private IGeometry createPoint(int i, int j){
		return ShapeFactory.createPoint2D(i,j);
	}

	public XMLEntity getXMLEntity() throws SaveException {
		 XMLEntity xml = super.getXMLEntity();
		 try {

			 xml.putProperty("fframegrid","fframegrid");
		 xml.addChild(this.symbolLine.getXMLEntity());
		 xml.addChild(this.symbolPoint.getXMLEntity());
//		 xml.putProperty("showCornerText",this.showCornerText);
		 xml.putProperty("familyFont",this.font.getFamily());
		 xml.putProperty("styleFont",this.font.getStyle());

           xml.putProperty("intervalX", intervalX);
           xml.putProperty("intervalY", intervalY);
           xml.putProperty("isLine", isLine);
//           xml.putProperty("lineColor", StringUtilities.color2String(lineColor));
           xml.putProperty("sizeFont", sizeFont);
           xml.putProperty("textColor", StringUtilities.color2String(textColor));

           if (fframeview != null) {
               Layout layout = fframeview.getLayout();
               IFFrame[] fframes = layout.getLayoutContext().getAllFFrames();

               for (int i = 0; i < fframes.length; i++) {
                   if (fframeview.equals(fframes[i])) {
                       xml.putProperty("index", i);
                       break;
                   }
               }
           }
       } catch (Exception e) {
           throw new SaveException(e, this.getClass().getName());
       }
		 return xml;
	}
	public void setXMLEntity(XMLEntity xml) {
		if (xml.getIntProperty("m_Selected") != 0) {
			this.setSelected(true);
		} else {
			this.setSelected(false);
		}
		this.symbolLine=(ILineSymbol)SymbologyFactory.createSymbolFromXML(xml.getChild(0),"line");
		this.symbolPoint=(IMarkerSymbol)SymbologyFactory.createSymbolFromXML(xml.getChild(1),"point");
//		this.showCornerText=xml.getBooleanProperty("showCornerText");
		this.font=new Font(xml.getStringProperty("familyFont"),xml.getIntProperty("styleFont"),8);

		this.intervalX = xml.getDoubleProperty("intervalX");
		this.intervalY = xml.getDoubleProperty("intervalY");
		this.isLine = xml.getBooleanProperty("isLine");
//		this.lineColor = StringUtilities.string2Color(xml
//				.getStringProperty("lineColor"));
		this.sizeFont = xml.getIntProperty("sizeFont");
		this.textColor = StringUtilities.string2Color(xml
				.getStringProperty("textColor"));

		setRotation(xml.getDoubleProperty("m_rotation"));

		if (xml.contains("index")) {
			dependenceIndex = xml.getIntProperty("index");
		}
	}

	public String getNameFFrame() {
		return PluginServices.getText(this, "cuadricula")+ num;
	}

	public void cloneActions(IFFrame frame) {
		// TODO Auto-generated method stub

	}

	public void setFFrameDependence(IFFrame f) {
		fframeview=(FFrameView)f;
		fframeview.refresh();
		if (fframeview.getMapContext()!=null)
		setBoundBox();
	}

	public IFFrame[] getFFrameDependence() {
		return new IFFrame[] {fframeview};
	}

	 /**
     * Actualiza las dependencias que tenga este FFrame con el resto.
     *
     * @param fframes Resto de FFrames.
     */
    public void initDependence(IFFrame[] fframes) {
        if ((dependenceIndex != -1) && fframes.length>dependenceIndex &&
                fframes[dependenceIndex] instanceof FFrameView) {
            fframeview = (FFrameView) fframes[dependenceIndex];
        }
    }

	public void setIntervalX(double d) {
		intervalX=d;
	}
	public void setIntervalY(double d) {
		intervalY=d;
	}
	public double getIntervalX() {
		return intervalX;
	}
	public double getIntervalY() {
		return intervalY;
	}
	public void setTextColor(Color textcolor) {
		textColor=textcolor;
	}

	public void setIsLine(boolean b) {
		isLine=b;
	}
	public boolean isLine() {
		return isLine;
	}

//	public IFFrameDialog getPropertyDialog() {
//		return new FFrameGridDialog(layout,this);
//	}

	public Color getFontColor() {
		return textColor;
	}

	public int getSizeFont() {
		return sizeFont;
	}

	public void setSizeFont(int sizeFont) {
		this.sizeFont = sizeFont;
	}

	public void setBoundBox() {
		Rectangle2D r=fframeview.getBoundBox();
		Rectangle2D extent=fframeview.getMapContext().getViewPort().getAdjustedExtent();
	    double extentX=extent.getMaxX();
	    double extentY=extent.getMaxY();
	    int lengthX=String.valueOf((long)extentX).length();
	    double myScale = layout.getLayoutControl().getAT().getScaleX() * 0.0234; //FLayoutUtilities.fromSheetDistance(folio.getAncho(),at)/rv.getWidth();
        int scaledFontSize = (int) (myScale * sizeFont);
	    int pixelsX=(int)(lengthX*scaledFontSize*0.7);
	    int lengthY=String.valueOf((long)extentY).length();
	    int pixelsY=(lengthY*scaledFontSize);
	    double dX=FLayoutUtilities.toSheetDistance(pixelsX,layout.getLayoutControl().getAT());
	    double dY=FLayoutUtilities.toSheetDistance(pixelsY,layout.getLayoutControl().getAT());
	    Rectangle2D rBound=new Rectangle2D.Double(r.getMinX()-dY,r.getMinY()-dX,r.getWidth()+dY*2,r.getHeight()+dX*2);
	    super.setBoundBox(rBound);
	}
	 public Rectangle2D getMovieRect(int difx, int dify) {
		 return this.getBoundingBox(null);
	 }

	public void refreshDependence(IFFrame fant, IFFrame fnew) {
		if (fframeview.equals(fant)) {
			fframeview=(FFrameView)fnew;
			fframeview.refresh();
			setBoundBox();
		}

	}

	public void setLayout(Layout l) {
		this.layout=l;

	}

	public void drawHandlers(Graphics2D g) {
		g.setColor(Color.gray);
		super.drawHandlers(g);
		g.setColor(Color.black);
	}

	public Rectangle2D getLastMoveRect() {
		return getBoundBox();
	}

	public void setBoundBox(Rectangle2D r) {
		if (fframeview!=null)
			setBoundBox();
		else
			super.setBoundBox(r);
	}

	public ISymbol getSymbolLine() {
		return symbolLine;
	}

	public void setSymbolLine(ISymbol symbolLine) {
		this.symbolLine = (ILineSymbol)symbolLine;
	}

	public ISymbol getSymbolPoint() {
		return symbolPoint;
	}

	public void setSymbolPoint(ISymbol symbolPoint) {
		this.symbolPoint = (IMarkerSymbol)symbolPoint;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font m_font) {
		this.font=m_font;
	}

//	public void setLineSymbol(ISymbol symbol) {
//		this.symbolLine=symbol;
//
//	}
//
//	public void setPointSymbol(ISymbol symbol) {
//		this.symbolPoint=symbol;
//
//	}

	public int getFontSize() {
		return sizeFont;
	}

	public IFFrameDialog getPropertyDialog() {
		// TODO Auto-generated method stub
		return null;
	}

	public void print(Graphics2D g, AffineTransform at, FShape shape,
			PrintRequestAttributeSet properties) {
		fframeview.refresh();

		ViewPort vp=fframeview.getMapContext().getViewPort();
//		vp.setAffineTransform(at);
		FontRenderContext frc = g.getFontRenderContext();
		PrintQuality pq = (PrintQuality) properties.get(PrintQuality.class);
		double factor=1;
		if (pq.equals(PrintQuality.NORMAL)){
			factor= (double) 300/72;
		}else if (pq.equals(PrintQuality.HIGH)){
			factor= (double) 600/72;
		}else if (pq.equals(PrintQuality.DRAFT)){
			//	unitFactor *= 72; (which is the same than doing nothing)
		}
		sizeFont*=factor;
		double myScale = at.getScaleX() * 0.0234; //FLayoutUtilities.fromSheetDistance(folio.getAncho(),at)/rv.getWidth();
        int scaledFontSize = (int) (myScale * sizeFont);
		Font font=new Font(this.font.getFamily(),this.font.getStyle(),scaledFontSize);
		Rectangle2D.Double r = getBoundingBox(at);
		Rectangle2D rView=fframeview.getBoundingBox(at);
		g.rotate(Math.toRadians(getRotation()), r.x + (r.width / 2),
            r.y + (r.height / 2));
//        AffineTransform atView=fframeview.getATMap();

//		vp.setAffineTransform(at);
        Rectangle2D extent=vp.getAdjustedExtent();
        double extentX=extent.getMinX();
        double extentY=extent.getMinY();

        double restX=(extentX/intervalX) % 1;
        double distX=restX*intervalX;
        //double distPixelsX=FLayoutUtilities.fromSheetDistance(distX,atView);
        double restY=(extentY/intervalY) % 1;
        double distY=restY*intervalY;
        //double distPixelsY=FLayoutUtilities.fromSheetDistance(distY,atView);

        double x=extentX-distX;
        //double pixelsX = rView.getMinX()-distPixelsX;
        double y=extentY-distY;
        //double pixelsY = rView.getMinY()-distPixelsY;

        //fframeview.getMapContext().getViewPort().fromMapPoint(extentX,extentY);
        //double pixelsInterval=FLayoutUtilities.fromSheetDistance(interval,atView);

        g.setColor(Color.black);


        // Dibuja los márgenes
        double valueIntervalX=((extentX/intervalX)-restX) * intervalX-intervalX;
        x=x-intervalX;
        double topX=extentX;
//        if (showCornerText){
//        	x+=intervalX;
//        	topX-=intervalX;
//        }
        while(x<extent.getMaxX()){
        	if (x>topX) {
        		Point2D p2=vp.fromMapPoint(x,extentY);
        		Point2D p1=vp.fromMapPoint(x,extent.getMaxY());
        		if (isLine()){
        			g.setColor(symbolLine.getColor());
        			g.setStroke(new BasicStroke((int)(symbolLine.getLineWidth()*factor)));
        		}else{
        			g.setColor(symbolPoint.getColor());
        			g.setStroke(new BasicStroke((int)1));
        		}
//        		g.setColor(lineColor);
        		g.drawLine((int)p1.getX(),(int)p1.getY()-5,(int)p1.getX(),(int)p1.getY());
        		g.drawLine((int)p2.getX(),(int)p2.getY(),(int)p2.getX(),(int)p2.getY()+5);
        		TextLayout textaux = new TextLayout(String.valueOf(valueIntervalX),
                        font, frc);

        		double w=textaux.getBounds().getWidth();
        		double h=textaux.getBounds().getHeight();
        		g.setColor(textColor);
        		textaux.draw(g,(int)(p1.getX()-w/2),(int)(p1.getY()-h)-5);
        		textaux.draw(g,(int)(p2.getX()-w/2),(int)(p2.getY()+h*2)+5);
        	}
        	valueIntervalX=valueIntervalX+intervalX;
        	x=x+intervalX;
        }
        double valueIntervalY=((extentY/intervalY)-restY) * intervalY-intervalY;
        y=y-intervalY;
        double topY=extentY;
//        if (showCornerText){
//        	y+=intervalY;
//        	topY-=intervalY;
//        }
        while(y<extent.getMaxY()){
        	if (y>topY) {
        		Point2D p1=vp.fromMapPoint(extentX,y);
        		Point2D p2=vp.fromMapPoint(extent.getMaxX(),y);
        		if (isLine()){
        			g.setColor(symbolLine.getColor());
        			g.setStroke(new BasicStroke((int)(symbolLine.getLineWidth()*factor)));
        		}else{
        			g.setColor(symbolPoint.getColor());
        			g.setStroke(new BasicStroke((int)1));
        		}
//        		g.setColor(lineColor);
        		g.drawLine((int)p1.getX()-5,(int)p1.getY(),(int)p1.getX(),(int)p1.getY());
        		g.drawLine((int)p2.getX(),(int)p2.getY(),(int)p2.getX()+5,(int)p2.getY());
        		TextLayout textaux = new TextLayout(String.valueOf(valueIntervalY),
                        font, frc);
        		double w=textaux.getBounds().getWidth();
        		double h=textaux.getBounds().getHeight();
        		g.setColor(textColor);
        		textaux.draw(g,(int)(p1.getX()-w-10),(int)(p1.getY()+h/2));
        		textaux.draw(g,(int)p2.getX()+10,(int)(p2.getY()+h/2));
        	}
        	valueIntervalY=valueIntervalY+intervalY;
        	y=y+intervalY;
        }
        if (isLine()){
			g.setColor(symbolLine.getColor());
			g.setStroke(new BasicStroke((int)(symbolLine.getLineWidth()*factor)));
		}else{
			g.setColor(symbolPoint.getColor());
			g.setStroke(new BasicStroke((int)1));
		}
//        g.setColor(lineColor);

        g.draw(rView);

        x = extentX-distX;
        y = extentY-distY;

        if (isLine) { // Dibuja las líneas.
        	 while(x<extent.getMaxX()){
        		 Point2D antPoint=vp.fromMapPoint(x,extentY);
 	        	if (x>extentX) {
 	                while(y<=extent.getMaxY()){
 	    	        	if (y>=extentY) {
 	    	        		Point2D p=vp.fromMapPoint(x,y);
 	    	        		IGeometry geom=createLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
 	    	        		geom.draw(g,new ViewPort(null),symbolLine);
 	    	        		antPoint=(Point2D)p.clone();
 	    	        	}

 	    	        	y=y+intervalY;
 	    	    	}
 	                Point2D p=vp.fromMapPoint(x,extent.getMaxY());
 	               IGeometry geom=createLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
	        		geom.draw(g,new ViewPort(null),symbolLine);
//	        		g.drawLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
	        		antPoint=(Point2D)p.clone();
 	    	        y=extentY-distY;

 	        	}


 	        	x=x+intervalX;
 	        }
        	 while(y<=extent.getMaxY()){
        		 Point2D antPoint=vp.fromMapPoint(extentX,y);
 	        	if (y>extentY) {
 	                while(x<=extent.getMaxX()){
 	    	        	if (x>=extentX) {
 	    	        		Point2D p=vp.fromMapPoint(x,y);
 	    	        		IGeometry geom=createLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
 	    	        		geom.draw(g,new ViewPort(null),symbolLine);
// 	    	        		g.drawLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
 	    	        		antPoint=p;
 	    	        	}
 	    	        	x=x+intervalX;
 	    	    	}
 	                Point2D p=vp.fromMapPoint(extent.getMaxX(),y);
	        		g.drawLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
	        		antPoint=(Point2D)p.clone();
 	    	        x=extentX-distX;
 	        	}
 	        	y=y+intervalY;
 	        }
        } else { //Dibuja los puntos
	        while(x<=extent.getMaxX()){
	        	if (x>extentX) {
	                while(y<=extent.getMaxY()){
	    	        	if (y>=extentY) {
	    	        		Point2D p=vp.fromMapPoint(x,y);
	    	        		IGeometry geom=createPoint((int)p.getX(),(int)p.getY());
 	    	        		geom.draw(g,new ViewPort(null),symbolPoint);
//	    	        		g.drawLine((int)p.getX()-10,(int)p.getY(),(int)p.getX()+10,(int)p.getY());
//	    	        		g.drawLine((int)p.getX(),(int)p.getY()-10,(int)p.getX(),(int)p.getY()+10);
	    	        	}
	    	        	y=y+intervalY;
	    	    	}
	    	        y=extentY-distY;
	        	}
	        	x=x+intervalX;
	        }

        }

        g.rotate(Math.toRadians(-getRotation()), r.x + (r.width / 2),
                r.y + (r.height / 2));

	}
}
