package com.iver.cit.gvsig.fmap.rendering.styling.labeling;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrintQuality;

import org.cresques.cts.ICoordTrans;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.NumericValue;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.CartographicSupport;
import com.iver.cit.gvsig.fmap.core.CartographicSupportToolkit;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleTextSymbol;
import com.iver.cit.gvsig.fmap.core.v02.FConstant;
import com.iver.cit.gvsig.fmap.core.v02.FLabel;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.utiles.StringUtilities;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;

/**
 * LabelingStrategy used when the user wants to use label sizes, rotations, etc. from
 * the values included in fields of the datasource's table
 *
 * @author jaume dominguez faus - jaume.dominguez@iver.es
 *
 */
public class AttrInTableLabelingStrategy implements ILabelingStrategy, CartographicSupport {

	public static final double MIN_TEXT_SIZE = 3;
	private ILabelingMethod method = new DefaultLabelingMethod();
	private IZoomConstraints zoom;
	private int idTextField=-1;
	private int idHeightField=-1;
	private int idRotationField=-1;
	private int idColorField=-1;
	private FLyrVect layer;
//	private double unitFactor = 1D;
	private double fixedSize=10;
	private Color fixedColor;
	private int unit = -1; //(pixel)
	private boolean useFixedSize;
	private boolean useFixedColor;
	private boolean withHalo = false;
	private Color haloColor = Color.WHITE;
	private float haloWidth = 3;
	public Color getHaloColor() {
		return haloColor;
	}

	public void setHaloColor(Color haloColor) {
		this.haloColor = haloColor;
	}

	public float getHaloWidth() {
		return haloWidth;
	}

	public void setHaloWidth(float haloWidth) {
		this.haloWidth = haloWidth;
	}

	public void setWithHalo(boolean withHalo) {
		this.withHalo = withHalo;
	}

	private int referenceSystem;
	// private double  printDPI_;
	private Font font;
	private Color colorFont;
	private String textFieldName;
	private String rotationFieldName;
	private String heightFieldName;
	private String colorFieldName;
	private PrintRequestAttributeSet properties;

	public ILabelingMethod getLabelingMethod() {
		return this.method;
	}

	public void setLabelingMethod(ILabelingMethod method) {
		this.method = method;
	}

	public IPlacementConstraints getPlacementConstraints() {
		return null; // (automatically handled by the driver)
	}

	public void setPlacementConstraints(IPlacementConstraints constraints) {
		// nothing
	}

	public IZoomConstraints getZoomConstraints() {
		return zoom;
	}

	public void setZoomConstraints(IZoomConstraints constraints) {
		this.zoom = constraints;
	}

	public void draw(BufferedImage image, Graphics2D g, ViewPort viewPort, Cancellable cancel, double _dpi)
	throws ReadDriverException {
		if (idTextField==-1)
			return;
//		double scale = viewPort.getScale();//deprecated
		double scale = layer.getMapContext().getScaleView();
//		double fontScaleFactor = FConstant.FONT_HEIGHT_SCALE_FACTOR;


		SimpleTextSymbol sym = new SimpleTextSymbol();

		sym.setFont(getFont());

		sym.setUnit(unit);
		sym.setReferenceSystem(referenceSystem);
		sym.setDrawWithHalo(isWithHalo());
		if (isWithHalo()) {
			sym.setHaloWidth(getHaloWidth());
			sym.setHaloColor(getHaloColor());
		}

		if (zoom==null ||
			( zoom.isUserDefined() && (scale <= zoom.getMaxScale())
			&& (scale >= zoom.getMinScale()) ) ) {
			try {
				// limit the labeling to the visible extent
				FBitSet bs = layer.queryByRect(viewPort.getAdjustedExtent());

				ReadableVectorial source = layer.getSource();
				SelectableDataSource recordSet = layer.getRecordset();
				recordSet.start();
				boolean reproject=layer.getProjection()!=null && !layer.getProjection().getAbrev().equals(
						layer.getMapContext().getViewPort().getProjection().getAbrev()) &&
						(layer.getCoordTrans()!=null);


				if ((idTextField == -1) || (idTextField >= recordSet.getFieldCount())) {
					System.err.println("Ha habido un error. Se ha perdido el campo de etiquetado. Probablemente por quitar un join o edición externa.");
					return;
				}

				for(int i=bs.nextSetBit(0); i>=0 && !cancel.isCanceled(); i=bs.nextSetBit(i+1)) {
					Value[] vv = recordSet.getRow(i);
					double size;
					Color color = null;
					if (useFixedSize){
						// uses fixed size
						size = fixedSize;// * fontScaleFactor;
					} else if (idHeightField != -1) {
						// text size is defined in the table
						try {
							size = ((NumericValue) vv[idHeightField]).doubleValue();// * fontScaleFactor;
						} catch (ClassCastException ccEx) {
							if (!NullValue.class.equals(vv[idHeightField].getClass())) {
								throw new ReadDriverException("Unknown", ccEx);
							}
							// a null value
							Logger.getAnonymousLogger().
								warning("Null text height value for text '"+vv[idTextField].toString()+"'");
							continue;
						}
					} else {
						// otherwise will use the size in the symbol
						size = sym.getFont().getSize();
					}

					size = CartographicSupportToolkit.
								getCartographicLength(this,
													  size,
													  viewPort,
													  _dpi);
					  // MapContext.getScreenDPI());
//								toScreenUnitYAxis(this,
//												  size,
//												  viewPort
//												 );

					if (size <= MIN_TEXT_SIZE) {
						// label is too small to be readable, will be skipped
						// this speeds up the rendering in wider zooms
						continue;
					}

					sym.setFontSize(size);

					if (useFixedColor){
						color = fixedColor;
					} else if (idColorField != -1) {
						// text size is defined in the table
						try {
							color = new Color(((NumericValue) vv[idColorField]).intValue());
						} catch (ClassCastException ccEx) {
							if (!NullValue.class.equals(vv[idColorField].getClass())) {
								throw new ReadDriverException("Unknown", ccEx);
							}
							// a null value
							Logger.getAnonymousLogger().
								warning("Null color value for text '"+vv[idTextField].toString()+"'");
							continue;
						}
					} else {
						color = sym.getTextColor();
					}

					sym.setTextColor(color);

					double rotation = 0D;
					if (idRotationField!= -1) {
						// text rotation is defined in the table
						if(!(vv[idRotationField] instanceof NullValue))
							rotation = -Math.toRadians(((NumericValue) vv[idRotationField]).doubleValue());
					}

					IGeometry geom = source.getShape(i);
					if (reproject){
						geom.reProject(layer.getCoordTrans());
					}
					sym.setText(vv[idTextField].toString());
					sym.setRotation(rotation);

					FLabel[] aux = geom.createLabels(0, true);
					for (int j = 0; j < aux.length; j++) {
						FPoint2D p = new FPoint2D(aux[j].getOrig());
						p.transform(viewPort.getAffineTransform());
						if (idRotationField == -1)
							sym.setRotation(aux[j].getRotation());
						
						if (properties==null) {
							sym.draw(g, null, p, cancel);
						}
						else
							sym.print(g, null, p, properties);
					}


				}
				recordSet.stop();
			} catch (VisitorException e) {
				Logger.getAnonymousLogger().log(Level.SEVERE, "Could not get the layer extent.\n" +
						e.getMessage());
			} catch (ExpansionFileReadException e) {
				Logger.getAnonymousLogger().log(Level.SEVERE, "Could not draw annotation in the layer" +
						"\""+layer.getName()+"\".\nIs the layer being edited?.\n"+e.getMessage());
			} catch (ReadDriverException e) {
				Logger.getAnonymousLogger().log(Level.SEVERE, "Could not draw annotation in the layer.\n" +
						e.getMessage());
			}

		}
	}

	public String getClassName() {
		return getClass().getName();
	}

	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", getClassName());
		xml.putProperty("labelingStrategy", "labelingStrategy");

		try {
			String height=getHeightField();
			if( height!= null)
				xml.putProperty("HeightField", height);
		} catch (ReadDriverException e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Acessing TextHeight field.\n"+e.getMessage());
		}

		try {
			String color=getColorField();
			if(color != null)
				xml.putProperty("ColorField", color);
		} catch (ReadDriverException e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Acessing ColorField field.\n"+e.getMessage());
		}

		try {
			String text=getTextField();
			if(text != null)
				xml.putProperty("TextField", text);
		} catch (ReadDriverException e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Acessing TextField field.\n"+e.getMessage());
		}

		try {
			String rotation=getRotationField();
			if (rotation != null)
				xml.putProperty("RotationField", rotation);
		} catch (ReadDriverException e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Acessing RotationField field.\n"+e.getMessage());
		}

		if(getFont() != null){
			xml.putProperty("fontSize", getFont().getSize());
			xml.putProperty("fontName", getFont().getName());
			xml.putProperty("fontStyle", getFont().getStyle());
		}
		if(getColorFont() != null)
			xml.putProperty("Color", StringUtilities.color2String(getColorFont()));
		xml.putProperty("useFixedSize", useFixedSize);
		xml.putProperty("useFixedColor", useFixedColor);
		xml.putProperty("fixedColor", StringUtilities.color2String(fixedColor));
		xml.putProperty("fixedSize", fixedSize);
		xml.putProperty("Unit", unit);
		xml.putProperty("referenceSystem",referenceSystem);
		if (isWithHalo()) {
			xml.putProperty("haloWidth", getHaloWidth());
			xml.putProperty("haloColor", StringUtilities.color2String(getHaloColor()));
		}

		return xml;

	}

	public String getRotationField() throws ReadDriverException {
		if (idRotationField == -1) return null;
		return ((SelectableDataSource) layer.getRecordset())
				.getFieldName(idRotationField);
	}

	public int getRotationFieldId() {
		return idRotationField;
	}

	public void setRotationFieldId(int fieldId) {
		this.idRotationField = fieldId;
	}

	public String getTextField() throws ReadDriverException {
		if (idTextField == -1) return null;
		try {
			String fieldName = ((SelectableDataSource) layer.getRecordset()).getFieldAlias(idTextField);
			return fieldName;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			// Probablmente hemos quitado un join y la leyenda se basaba en un campo de la segunda tabla
			e.printStackTrace();
			return null;
		}
	}

	public int getTextFieldId() {
		return idTextField;
	}

	public void setTextFieldId(int fieldId) {
		this.idTextField = fieldId;
	}

	public String getHeightField() throws ReadDriverException {
		if (idHeightField == -1) return null;
		return ((SelectableDataSource) layer.getRecordset())
				.getFieldAlias(idHeightField);
	}

	public int getHeightFieldId() {
		return idHeightField;
	}

	public void setHeightFieldId(int fieldId) {
		this.idHeightField = fieldId;
	}

	public String getColorField() throws ReadDriverException {
		if (idColorField == -1) return null;
		return ((SelectableDataSource) layer.getRecordset())
				.getFieldAlias(idColorField);
	}

	public int getColorFieldId() {
		return idColorField;
	}

	public void setColorFieldId(int fieldId) {
		this.idColorField = fieldId;
	}


	public void setXMLEntity(XMLEntity xml) {
		if (xml.contains("TextField" ))
			setTextField(xml.getStringProperty("TextField"));

		if (xml.contains("HeightField"))
			setHeightField(xml.getStringProperty("HeightField"));

		if (xml.contains("ColorField"))
			setColorField(xml.getStringProperty("ColorField"));

		if (xml.contains("RotationField"))
			setRotationField(xml.getStringProperty("RotationField"));

		if (xml.contains("Unit"))
			setUnit(xml.getIntProperty("Unit"));

		if (xml.contains("fontName")){
			Font font=new Font(xml.getStringProperty("fontName"),xml.getIntProperty("fontStyle"),xml.getIntProperty("fontSize"));
			setFont(font);
		}
		if (xml.contains("useFixedSize")){
			useFixedSize=xml.getBooleanProperty("useFixedSize");
			fixedSize=xml.getDoubleProperty("fixedSize");
		}
		if (xml.contains("useFixedColor")){
			useFixedColor=xml.getBooleanProperty("useFixedColor");
			fixedColor=StringUtilities.string2Color(xml.getStringProperty("fixedColor"));
		}
		if (xml.contains("referenceSystem"))
			referenceSystem=xml.getIntProperty("referenceSystem");
		
		if (xml.contains("haloWidth")) {
			float haloWidth = xml.getFloatProperty("haloWidth");
			setHaloColor(StringUtilities.string2Color(xml.getStringProperty("haloColor")));
			setHaloWidth(haloWidth);
			setWithHalo(true);
		}


	}

	public void setTextField(String textFieldName) {
		this.textFieldName=textFieldName;
	}

	public void setRotationField(String rotationFieldName) {
		if (rotationFieldName != null) {
			this.rotationFieldName=rotationFieldName;
		} else idRotationField = -1;
	}

	/**
	 * Sets the field that contains the size of the text. The size is computed
	 * in meters. To use any other unit, call setUnit(int) with the scale factor from meters
	 * (for centimeters, call <b>setUnitFactor(0.01))</b>.
	 * @param heightFieldName
	 */
	public void setHeightField(String heightFieldName) {
		if (heightFieldName != null) {
			this.heightFieldName=heightFieldName;
		} else idHeightField = -1;
	}


	public void setColorField(String colorFieldName) {
		if (colorFieldName != null) {
			this.colorFieldName=colorFieldName;
		} else idColorField = -1;
	}

	public void print(Graphics2D g, ViewPort viewPort, Cancellable cancel, PrintRequestAttributeSet properties) throws ReadDriverException {
		this.properties=properties;

		double _dpi = 72;
		PrintQuality resolution=(PrintQuality)properties.get(PrintQuality.class);
		if (resolution.equals(PrintQuality.NORMAL)){
			_dpi = 300;
		} else if (resolution.equals(PrintQuality.HIGH)){
			_dpi = 600;
		} else if (resolution.equals(PrintQuality.DRAFT)){
			_dpi = 72;
		}

		draw(null, g, viewPort, cancel, _dpi);
		this.properties=null;
	}

	public void setUsesFixedSize(boolean b) {
		useFixedSize = b;
	}

	public boolean usesFixedSize() {
		return useFixedSize;
	}

	public double getFixedSize() {
		return fixedSize;
	}

	public void setFixedSize(double fixedSize) {
		this.fixedSize = fixedSize;
	}

	public void setUsesFixedColor(boolean b) {
		useFixedColor = b;
	}

	public boolean usesFixedColor() {
		return useFixedColor;
	}

	public Color getFixedColor() {
		return fixedColor;
	}

	public void setFixedColor(Color fixedColor) {
		this.fixedColor = fixedColor;
	}


	public void setUnit(int unitIndex) {
		unit = unitIndex;

	}

	public int getUnit() {
		return unit;
	}

	public String[] getUsedFields() {
		Vector v = new Vector();
		try {
			if (getHeightField()!=null) v.add(getHeightField());
			if (getRotationField()!=null) v.add(getRotationField());
			if (getTextField()!=null) v.add(getTextField());
			if (getHeightField()!=null) v.add(getHeightField());
		} catch (ReadDriverException e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage());
		}
		return (String[]) v.toArray(new String[v.size()]);
	}

	public int getReferenceSystem() {
		return referenceSystem;
	}

	public void setReferenceSystem(int referenceSystem) {
		this.referenceSystem = referenceSystem;
	}

	public double toCartographicSize(ViewPort viewPort, double dpi, FShape shp) {
		// not required here
		throw new Error("Undefined in this context");
	}

	public void setCartographicSize(double cartographicSize, FShape shp) {
		// not required here
		throw new Error("Undefined in this context");
	}

	public double getCartographicSize(ViewPort viewPort, double dpi, FShape shp) {
		// not required here
		throw new Error("Undefined in this context");

	}

	public void setLayer(FLayer layer) {
		this.layer = (FLyrVect) layer;
		try{
			SelectableDataSource sds=this.layer.getRecordset();
			if (textFieldName!=null){
				idTextField = sds.getFieldIndexByName(textFieldName);
//				if (idTextField==-1){
//					if (textFieldName.startsWith("link_")){
//						textFieldName=textFieldName.substring(5, textFieldName.length());
//						textFieldName="j_"+textFieldName;
//					}
//					idTextField = sds.getFieldIndexByName(textFieldName);
//				}
			}
			if (rotationFieldName!=null)
			idRotationField = sds.getFieldIndexByName(rotationFieldName);
			if (heightFieldName!=null)
			idHeightField = sds.getFieldIndexByName(heightFieldName);
			if (colorFieldName!=null)
			idColorField = sds.getFieldIndexByName(colorFieldName);
		} catch (ReadDriverException e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage());
		}
	}

	public boolean shouldDrawLabels(double scale) {
		return layer.isWithinScale(scale);
	}

	public Color getColorFont() {
		return colorFont;
	}

	public void setColorFont(Color colorFont) {
		this.colorFont = colorFont;
	}

	public Font getFont() {
		if(font == null)
			font = SymbologyFactory.DefaultTextFont;
		return font;
	}

	public void setFont(Font selFont) {
		this.font = selFont;
	}

	public boolean isWithHalo() {
		return withHalo;
	}
}
