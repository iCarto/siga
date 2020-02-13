package org.gvsig.symbology.fmap.rendering;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.gvsig.symbology.fmap.styles.SimpleMarkerFillPropertiesStyle;
import org.gvsig.symbology.fmap.symbols.MarkerFillSymbol;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.instruction.FieldNotFoundException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.IFillSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.IMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.IMultiLayerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.cit.gvsig.fmap.rendering.AbstractClassifiedVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.IClassifiedVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.IInterval;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.LegendFactory;
import com.iver.cit.gvsig.fmap.rendering.SymbolLegendEvent;
import com.iver.cit.gvsig.fmap.rendering.VectorialIntervalLegend;
import com.iver.utiles.XMLEntity;
/**
 * Implements a legend where the user can compare two different characteristics
 * of a region in the map. These two "fields" will be compared, on one side,
 * using a color for the region and , on the other side, using a graduated symbol.
 * Both methods will change (the color or the size of the symbol) depending on
 * the value of the fields.
 *
 * @author jaume dominguez faus - jaume.dominguez@iver.es
 */
public class QuantityByCategoryLegend extends AbstractClassifiedVectorLegend implements IClassifiedVectorLegend  {
	private GraduatedSymbolLegend graduatedSymbol;
	private VectorialIntervalLegend colorRamp;
	private ISymbol defaultSymbol = SymbologyFactory.createDefaultSymbolByShapeType(FShape.POLYGON);
	private int shapeType;
	private boolean isUseDefaultSymbol;
	private DataSource ds;

	public QuantityByCategoryLegend() {
		graduatedSymbol = new GraduatedSymbolLegend();
		colorRamp = new VectorialIntervalLegend();
	}


	public void clear() {
		colorRamp.clear();
		graduatedSymbol.clear();
	}

	public String[] getClassifyingFieldNames() {
		ArrayList<String> l = new ArrayList<String>();
		for (int i = 0; i < graduatedSymbol.getClassifyingFieldNames().length; i++) {
			l.add(graduatedSymbol.getClassifyingFieldNames()[i]);
		}

		for (int i = 0; i < colorRamp.getClassifyingFieldNames().length; i++) {
			l.add(colorRamp.getClassifyingFieldNames()[i]);
		}
		return l.toArray(new String[l.size()]);
	}


	@Override
	public int[] getClassifyingFieldTypes() {
		return null;
	}

	public void setClassifyingFieldTypes(int[] fieldTypes) {
		if (fieldTypes.length!=2) {

		}
		colorRamp.setClassifyingFieldTypes(new int[] {fieldTypes[1]});
		graduatedSymbol.setClassifyingFieldTypes(new int[] {fieldTypes[0]});
	}


	/**
	 * Sets the field names required to build this legend. In this case
	 * fieldNames is an array of length 2 where the first element is
	 * the field name for the embedded GraduatedSymbolLegend, and the
	 * second is the field name for the embedded colorRamp (VectorialIntervalLegend)
	 * legend.
	 */
	public void setClassifyingFieldNames(String[] fieldNames) {

		if (fieldNames.length!=2) {

		}
		colorRamp.setClassifyingFieldNames(new String[] {fieldNames[1]});
		graduatedSymbol.setClassifyingFieldNames(new String[] {fieldNames[0]});
	}

	public void addSymbol(Object key, ISymbol symbol) {
//		System.out.println("adding "+key+"["+symbol+"]");

		if(symbol instanceof IFillSymbol)
			colorRamp.addSymbol(key, symbol);
		else if(symbol instanceof IMarkerSymbol)
			graduatedSymbol.addSymbol(key, symbol);

		fireClassifiedSymbolChangeEvent(new SymbolLegendEvent(null, symbol));
	}

	public void delSymbol(Object key) {
		colorRamp.delSymbol(key);
		graduatedSymbol.delSymbol(key);
		fireClassifiedSymbolChangeEvent(
				new SymbolLegendEvent(
						null,
						null));
	}

	public String[] getDescriptions() {
		String[] desc1 = colorRamp.getDescriptions();
		String[] desc2 = graduatedSymbol.getDescriptions();

		String[] descriptions = new String[desc1.length + desc2.length];

		if(desc1.length == 0)
			return desc2;
		else {
			for (int i = 0; i < descriptions.length; i++) {
				descriptions[i] = (i <desc1.length) ? desc1[i] : desc2[i - desc1.length];
			}
			return descriptions;
		}
	}

	public ISymbol[] getSymbols() {
		ISymbol[] symbols1 = colorRamp.getSymbols();
		ISymbol[] symbols2 = graduatedSymbol.getSymbols();

		ISymbol[] symbols = new ISymbol[symbols1.length + symbols2.length];

		if(symbols1.length == 0)
			return symbols2;
		else {
			for (int i = 0; i < symbols.length; i++) {
				symbols[i] = (i < symbols1.length) ? symbols1[i] : symbols2[i - symbols1.length];
			}
		}
		return symbols;
	}

	public Object[] getValues() {
		Object[] objects1 = colorRamp.getValues();
		Object[] objects2 = graduatedSymbol.getValues();

		Object[] objects = new IInterval[objects1.length + objects2.length];

		if(objects1.length == 0)
			return objects2;


		else {
			for (int i = 0; i < objects.length; i++) {
				objects[i] = (i < objects1.length) ? objects1[i] : objects2[i - objects1.length];
			}
			return objects;
		}

	}



	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", getClass().getName());
		xml.putProperty("shapeType", shapeType);
		xml.putProperty("isUseDefaultSymbol", isUseDefaultSymbol);

		xml.addChild(colorRamp.getXMLEntity());
		xml.addChild(graduatedSymbol.getXMLEntity());

		return xml;
	}

	public ILegend cloneLegend() throws XMLException {
		return LegendFactory.createFromXML(getXMLEntity());

	}
	/**
	 * Obtains the GraduatedSymbolLegend
	 *
	 * @return GraduatedSymbolLegend
	 */
	public GraduatedSymbolLegend getGraduatedSymbolLegend() {
		return graduatedSymbol;
	}
	/**
	 * Obtains the VectorialIntervalLegend
	 *
	 * @return VectorialIntervalLegend
	 */
	public VectorialIntervalLegend getColorRampLegend() {
		return colorRamp;
	}

	public void setDataSource(DataSource ds) throws FieldNotFoundException, ReadDriverException {
		// TODO remove it when FLyrVect.forTestOnlyVariableUseIterators_REMOVE_THIS_FIELD is removed
//		if (FLyrVect.forTestOnlyVariableUseIterators_REMOVE_THIS_FIELD) {
			this.ds = ds;
//		}
		graduatedSymbol.setDataSource(ds);
		colorRamp.setDataSource(ds);
	}

	public ISymbol getSymbol(int i) throws ReadDriverException {
		IMarkerSymbol sym1 = (IMarkerSymbol) graduatedSymbol.getSymbol(i);
		ISymbol sym2 =  colorRamp.getSymbol(i);
		IMultiLayerSymbol multiSym = null;
		switch (shapeType%FShape.Z) {
		case FShape.POLYGON:
			/*
			 * symbol from the GraduatedSymbolLegend is a marker, but
			 * what we need is a fill symbol. Will use a MarkerFillSymbol
			 * to enable support for Polygons
			 */
			MarkerFillSymbol aux = new MarkerFillSymbol();
			// tell the fill style to draw the IMarkerSymbol
			// as a IFillSymbol centering it in the shape polygon
			// centroid and applying offset (if any).
			aux.setMarker(sym1);
			SimpleMarkerFillPropertiesStyle p = new SimpleMarkerFillPropertiesStyle();
			p.setFillStyle(SimpleMarkerFillPropertiesStyle.SINGLE_CENTERED_SYMBOL);
			aux.setMarkerFillProperties(p);

			multiSym = SymbologyFactory.
			createEmptyMultiLayerSymbol(FShape.POLYGON);
			multiSym.addLayer(sym2);
			multiSym.addLayer(aux);
			break;
		case FShape.LINE:
			throw new Error("Shape type not yet supported");
		default:
			throw new Error("Unsupported shape type");

		}

		return multiSym;
	}

	public ISymbol getSymbolByFeature(IFeature feat) {
		ISymbol sym1 = null, sym2 = null;
//		try {
			Value gsVal;
//			if (!FLyrVect.forTestOnlyVariableUseIterators_REMOVE_THIS_FIELD) {
//				gsVal = feat.getAttribute(ds.getFieldIndexByName(getClassifyingFieldNames()[0]));
//
//			} else {
				gsVal = feat.getAttribute(0);
//			}
			sym1 = graduatedSymbol.getSymbolByInterval(graduatedSymbol.getInterval(gsVal));

//		} catch (ReadDriverException e) {
//			Logger.getLogger(getClass()).error("Graduated Symbol Legend failed", e);
//		}


//		try {
			Value crVal;
//			if (!FLyrVect.forTestOnlyVariableUseIterators_REMOVE_THIS_FIELD) {
//				crVal = feat.getAttribute(ds.getFieldIndexByName(getClassifyingFieldNames()[1]));
//			} else {
				crVal = feat.getAttribute(1);
//			}
			sym2 = colorRamp.getSymbolByInterval(colorRamp.getInterval(crVal));

//		} catch (ReadDriverException e) {
//			Logger.getLogger(getClass()).error("Color Ramp Symbol Legend failed", e);
//		}

		IMultiLayerSymbol multiSym = null;
		switch (shapeType%FShape.Z) {
		case FShape.POLYGON:
			multiSym = SymbologyFactory.
			createEmptyMultiLayerSymbol(FShape.POLYGON);
			if (sym2 != null) multiSym.addLayer(sym2);
			if (sym1 != null) multiSym.addLayer(sym1);
			break;
		case FShape.LINE:
			throw new Error("Shape type not yet supported");
		default:
			throw new Error("Unsupported shape type");

		}

		return multiSym;
	}

	public int getShapeType() {
		return shapeType;
	}

	public void setShapeType(int shapeType) {
		this.shapeType = shapeType;
		graduatedSymbol.setShapeType(FShape.POINT);
		graduatedSymbol.setDefaultSymbol(SymbologyFactory.createDefaultSymbolByShapeType(FShape.POINT));
		colorRamp.setShapeType(shapeType);
		colorRamp.setDefaultSymbol(SymbologyFactory.createDefaultSymbolByShapeType(shapeType));
	}


	public void setXMLEntity(XMLEntity xml) {
		shapeType = xml.getIntProperty("shapeType");
		isUseDefaultSymbol = xml.getBooleanProperty("isUseDefaultSymbol");
		try {
			colorRamp = (VectorialIntervalLegend) LegendFactory.createFromXML(xml.getChild(0));
			graduatedSymbol = (GraduatedSymbolLegend) LegendFactory.createFromXML(xml.getChild(1));
			colorRamp.setShapeType(shapeType);
			graduatedSymbol.setShapeType(shapeType);
		} catch (XMLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(getClass()).error(e.getFormatString());
		}
	}

	public boolean isUseDefaultSymbol() {
		return isUseDefaultSymbol;
	}

	public void useDefaultSymbol(boolean b) {
		this.isUseDefaultSymbol = b;
	}

	public String[] getUsedFields() {
		// TODO Implement it
		throw new Error("Not yet implemented!");
	}

	public void setGraduateSymbolLegend(ILegend legend) {
		this.graduatedSymbol = (GraduatedSymbolLegend) legend;
	}

	public void setColorRampLegend(ILegend legend) {
		this.colorRamp = (VectorialIntervalLegend) legend;
	}

	public String getClassName() {
		return getClass().getName();
	}

	public void replace(ISymbol oldSymbol, ISymbol newSymbol) {
		ISymbol[] symbols;
		// look first in the graduated symbol legend
		symbols = graduatedSymbol.getSymbols();

		for (int i = 0; i < symbols.length; i++) {
			if (symbols[i].equals(oldSymbol)) {
				graduatedSymbol.replace(oldSymbol, newSymbol);
				return;
			}
		}

		// if the symbol wasn't found yet, proceed with color ramp
		symbols = colorRamp.getSymbols();

		for (int i = 0; i < symbols.length; i++) {
			if (symbols[i].equals(oldSymbol)) {
				colorRamp.replace(oldSymbol, newSymbol);
				return;
			}
		}

	}

	public ISymbol getDefaultSymbol() {
		return defaultSymbol;
	}

	public void setDefaultSymbol(ISymbol s) throws IllegalArgumentException {
		this.defaultSymbol = s;
	}

}
