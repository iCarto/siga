package es.icarto.gvsig.extgia.signalsimbology;

import java.awt.Point;
import java.io.IOException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.instruction.FieldNotFoundException;
import com.hardcode.gdbms.engine.values.IntValue;
import com.hardcode.gdbms.engine.values.StringValue;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.exceptions.layers.LegendLayerException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.CartographicSupport;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.MultiLayerMarkerSymbol;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.LegendFactory;

import es.icarto.gvsig.commons.utils.FileNameUtils;

public class ApplySignalSimbology {

    private static final Point NOFFSET = new Point(0, 0);

    private static final Logger logger = Logger
	    .getLogger(ApplySignalSimbology.class);

    private final FLyrVect senhales;
    private final FLyrVect postes;
    private final SenhalesAlgorithm alg;
    private int unitIdx;
    private int unitRS = CartographicSupport.WORLD;

    public ApplySignalSimbology(FLyrVect postes, FLyrVect signals) {
	this.senhales = signals;
	this.postes = postes;
	setSimpleLegend();
	this.alg = new SenhalesAlgorithm(null);
	String[] distanceNames = MapContext.getDistanceNames();
	for (int i = 0; i < distanceNames.length; i++) {
	    if (distanceNames[i].equals("Centimetros")) {
		unitIdx = i;
	    }
	}
    }

    // FLayer mantiene durante el proceso de cambio de leyenda referencias a la
    // antigua leyenda. Si la antigua leyenda ocupa mucha memoria y la nueva
    // tambi?n se puede producir un OutOfMemory
    private void setSimpleLegend() {
	IVectorLegend simpleLegend = LegendFactory
		.createSingleSymbolLegend(FShape.POINT);
	try {
	    this.postes.setLegend(simpleLegend);
	} catch (LegendLayerException e) {
	    logger.error(e.getStackTrace(), e);
	}
    }

    public void applySymbology() {
	try {
	    applyLegend(postes);
	} catch (LegendLayerException e) {
	    logger.error(e.getStackTrace(), e);
	} catch (ReadDriverException e) {
	    logger.error(e.getStackTrace(), e);
	} catch (FieldNotFoundException e) {
	    logger.error(e.getStackTrace(), e);
	} catch (RuntimeException e) {
	    logger.error(e.getStackTrace(), e);
	    NotificationManager.addWarning("Error aplicando simbolog?a", e);
	}
    }

    private void applyLegend(FLyrVect poste) throws ReadDriverException,
    FieldNotFoundException, LegendLayerException {

	final String fieldName = "id_elemento_senhalizacion";
	final String[] classifyingFieldNames = new String[] { fieldName };
	final int[] classifyingFieldTypes = new int[] { Types.INTEGER };

	ReadableVectorial source = poste.getSource();

	SignalLegend legend = new SignalLegend(poste.getShapeType());
	legend.setDataSource(source.getRecordset());
	legend.setClassifyingFieldNames(classifyingFieldNames);
	legend.setClassifyingFieldTypes(classifyingFieldTypes);

	createLegend(legend, senhales);

	poste.setLegend(legend);
	poste.setMinScale(0);
	poste.setMaxScale(10000);
    }

    private int getIdx(FLyrVect layer, String fieldName) {
	try {
	    SelectableDataSource recordset = layer.getRecordset();
	    return recordset.getFieldIndexByName(fieldName);
	} catch (ReadDriverException e) {
	    throw new RuntimeException(e);
	}
    }

    private void createLegend(SignalLegend legend, FLyrVect senhales)
	    throws ReadDriverException {

	int idIndex = getIdx(senhales, "id_elemento_senhalizacion");
	int tipoIdx = getIdx(senhales, "tipo_senhal");
	int codigoIdx = getIdx(senhales, "codigo_senhal");
	int idSenhalIdx = getIdx(senhales, "id_senhal_vertical");
	int observacionesIdx = getIdx(senhales, "observaciones");

	ReadableVectorial source = senhales.getSource();

	Map<Value, Integer> list = new HashMap<Value, Integer>();
	SignalCache cache = new SignalCache();

	for (int i = 0; i < source.getShapeCount(); i++) {
	    Value[] attributes = source.getFeature(i).getAttributes();
	    Value tipo = attributes[tipoIdx];
	    Value codigo = attributes[codigoIdx];
	    Value id = attributes[idIndex];
	    Value idSenhal = attributes[idSenhalIdx];
	    Value observaciones = attributes[observacionesIdx];
	    final Integer count = list.get(id);
	    if (count == null) {
		ISymbol symbol = setPictureSymbol(cache, 0, tipo, codigo,
			idSenhal, observaciones);
		legend.addSymbol(id, symbol);
		list.put(id, 0);
	    } else {
		MultiLayerMarkerSymbol multi = new MultiLayerMarkerSymbol();
		final ISymbol symbolByValue = legend.getSymbolByValue(id);
		if (symbolByValue instanceof MultiLayerMarkerSymbol) {
		    MultiLayerMarkerSymbol s = (MultiLayerMarkerSymbol) symbolByValue;
		    for (int j = 0; j < s.getLayerCount(); j++) {
			multi.addLayer(s.getLayer(j));
		    }
		} else {
		    multi.addLayer(symbolByValue);
		}

		ISymbol symbol = setPictureSymbol(cache, (count + 1), tipo,
			codigo, idSenhal, observaciones);
		multi.addLayer(symbol);
		legend.delSymbol(id);
		legend.addSymbol(id, multi);
		list.put(id, count + 1);
	    }
	}
    }

    // private int counter = 0; // TODO

    private ISymbol setPictureSymbol(SignalCache cache, int offset,
	    Value tipoValue, Value codigoValue, Value idSenhalValue,
	    Value observacionesValue) {
	String tipo = stringValue(tipoValue);
	String codigo = stringValue(codigoValue);
	String idSenhal = stringValue(idSenhalValue);
	String observaciones = stringValue(observacionesValue);
	String file = this.alg.getFilename(tipo, codigo, idSenhal);
	int size = this.alg.getSize(tipo, codigo, observaciones);

	SignalSymbol symbol = null;
	try {
	    symbol = new SignalSymbol(cache, file);
	} catch (IOException e) {
	    logger.error(e.getStackTrace(), e);
	}
	symbol.setDescription(FileNameUtils.removeExtension(file));
	symbol.setOffset(offset == 0 ? NOFFSET : new Point(0, size * offset));
	symbol.setSize(size);

	symbol.setUnit(unitIdx);
	symbol.setReferenceSystem(unitRS);
	return symbol;
    }

    private String stringValue(Value v) {
	if (v instanceof StringValue) {
	    return v.toString().trim();
	} else if (v instanceof IntValue) {
	    return v.toString();
	}
	return "";

    }

    public void setUnits(int selectedUnitIndex) {
	unitIdx = selectedUnitIndex;
    }

    public void setUnitReferenceSystem(int unitRS) {
	this.unitRS = unitRS;
    }

    public void setSize(int size) {
	this.alg.setPictureSize(size);
    }

    public void setCartelSize(int cartelSize) {
	this.alg.setCartelSize(cartelSize);
    }

    public void setHitoSize(int hitoSize) {
	this.alg.setHitoSize(hitoSize);
    }
}
