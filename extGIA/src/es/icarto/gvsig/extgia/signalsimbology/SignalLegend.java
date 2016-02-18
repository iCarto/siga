package es.icarto.gvsig.extgia.signalsimbology;

import java.awt.Color;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.instruction.FieldNotFoundException;
import com.hardcode.gdbms.engine.instruction.IncompatibleTypesException;
import com.hardcode.gdbms.engine.instruction.SemanticException;
import com.hardcode.gdbms.engine.values.BooleanValue;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.StringValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.v02.FSymbol;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.cit.gvsig.fmap.rendering.AbstractClassifiedVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.ICustomLegend;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.IVectorialUniqueValueLegend;
import com.iver.cit.gvsig.fmap.rendering.LegendClearEvent;
import com.iver.cit.gvsig.fmap.rendering.LegendFactory;
import com.iver.cit.gvsig.fmap.rendering.NullUniqueValue;
import com.iver.cit.gvsig.fmap.rendering.SymbolLegendEvent;
import com.iver.cit.gvsig.fmap.rendering.ZSort;
import com.iver.utiles.StringUtilities;
import com.iver.utiles.XMLEntity;

// based on VectorialUniqueValueLegend {
public class SignalLegend extends AbstractClassifiedVectorLegend implements
	IVectorialUniqueValueLegend, ICustomLegend {
    private static final Logger log = Logger.getLogger(SignalLegend.class);

    protected int fieldId;

    protected DataSource dataSource;

    private boolean ownOrder = false;

    private ArrayList orders = new ArrayList();

    private final TreeMap<Value, ISymbol> symbols = new TreeMap<Value, ISymbol>(
	    new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
		    if (ownOrder) {
			try {
			    if (((BooleanValue) ((Value) o1).equals((Value) o2))
				    .getValue()) {
				return 0;
			    }
			} catch (IncompatibleTypesException e) {
			    log.info(
				    "Cannot compare the values: "
					    + o1.toString() + " - "
					    + o2.toString(), e);
			}
			if (orders.indexOf(o1.toString()) < orders.indexOf(o2
				.toString())) {
			    return -1;
			} else if (orders.indexOf(o1.toString()) > orders
				.indexOf(o2.toString())) {
			    return 1;
			}
			return 0;
		    }
		    if ((o1 != null) && (o2 != null)) {
			Value v2 = (Value) o2;
			Value v1 = (Value) o1;
			BooleanValue boolVal;

			// TODO estas dos comprobaciones son por evitar un bug
			// en el gdbms, cuando se solucione se puede eliminar.
			if (v1 instanceof NullValue && v2 instanceof NullValue) {
			    return 0;
			}

			if (v1 instanceof NullValue) {
			    return -1;
			}

			if (v2 instanceof NullValue) {
			    return 1;
			}

			try {
			    boolVal = (BooleanValue) (v1.greater(v2));

			    if (boolVal.getValue()) {
				return 1;
			    }

			    boolVal = (BooleanValue) (v1.less(v2));

			    if (boolVal.getValue()) {
				return -1;
			    }
			} catch (IncompatibleTypesException e) {
			    // TODO Auto-generated catch block
			    // e.printStackTrace();
			}

			try {
			    if (((BooleanValue) v1.equals(v2)).getValue()) {
				return 0;
			    }
			} catch (IncompatibleTypesException e) {
			    // TODO Auto-generated catch block
			    // e.printStackTrace();
			}

			if (v1 instanceof StringValue) {
			    return -1;
			}

			if (v2 instanceof StringValue) {
			    return 1;
			}
		    }

		    return 0;
		}
	    }); // Para poder ordenar

    private final ArrayList<Value> keys = new ArrayList<Value>(); // En lugar de
    // un
    // HashSet,
    // para
    // tener
    // acceso por
    // índice

    private String labelFieldName;

    private String labelFieldHeight;

    private String labelFieldRotation;

    private ISymbol defaultSymbol;

    private int shapeType;

    private String valueType = NullValue.class.getName();

    private boolean useDefaultSymbol = false;

    private Color[] selectedColors = null;

    /**
     * Constructor method
     */
    public SignalLegend() {
    }

    /**
     * Constructor method
     *
     * @param shapeType
     *            Type of the shape.
     */
    public SignalLegend(int shapeType) {
	setShapeType(shapeType);
    }

    @Override
    public void setShapeType(int shapeType) {
	if (this.shapeType != shapeType) {
	    ISymbol old = defaultSymbol;
	    defaultSymbol = SymbologyFactory
		    .createDefaultSymbolByShapeType(shapeType);
	    fireDefaultSymbolChangedEvent(new SymbolLegendEvent(old,
		    defaultSymbol));
	    this.shapeType = shapeType;
	}
    }

    @Override
    public void setValueSymbolByID(int id, ISymbol symbol) {
	ISymbol old = symbols.put(keys.get(id), symbol);
	fireClassifiedSymbolChangeEvent(new SymbolLegendEvent(old, symbol));
    }

    /**
     * Used in the table that shows the legend
     *
     * @deprecated use setValueSymbolByID(int id, ISymbol symbol);
     * @param id
     * @param symbol
     */
    @Deprecated
    public void setValueSymbol(int id, ISymbol symbol) {
	ISymbol old = symbols.put(keys.get(id), symbol);
	fireClassifiedSymbolChangeEvent(new SymbolLegendEvent(old, symbol));
    }

    @Override
    public Object[] getValues() {
	return symbols.keySet().toArray(new Object[0]);
    }

    @Override
    public void addSymbol(Object key, ISymbol symbol) {
	ISymbol resul;
	resul = symbols.put((Value) key, symbol);

	if (resul != null) {
	    log.error("Error: la clave " + key + " ya existía. Resul = "
		    + resul);
	    log.warn("symbol nuevo:" + symbol.getDescription() + " Sviejo= "
		    + resul.getDescription());
	} else {
	    keys.add((Value) key);

	    if (!key.getClass().equals(NullValue.class)) {
		valueType = key.getClass().getName();
	    }
	}
	fireClassifiedSymbolChangeEvent(new SymbolLegendEvent(resul, symbol));

    }

    @Override
    public void clear() {
	keys.clear();
	ISymbol[] olds = symbols.values().toArray(new ISymbol[0]);
	symbols.clear();
	removeLegendListener(getZSort());
	setZSort(null);

	fireLegendClearEvent(new LegendClearEvent(olds));
    }

    @Override
    public String[] getDescriptions() {
	String[] descriptions = new String[symbols.size()];
	ISymbol[] auxSym = getSymbols();

	for (int i = 0; i < descriptions.length; i++) {
	    descriptions[i] = auxSym[i].getDescription();
	}

	return descriptions;
    }

    @Override
    public ISymbol[] getSymbols() {
	return symbols.values().toArray(new ISymbol[0]);
    }

    @Override
    public void setClassifyingFieldNames(String[] fNames) {
	super.setClassifyingFieldNames(fNames);
	try {
	    fieldId = dataSource
		    .getFieldIndexByName(getClassifyingFieldNames()[0]);
	} catch (NullPointerException e) {
	    log.warn("data source not set");
	} catch (ReadDriverException e) {
	    log.warn("failed setting field id");
	}
    }

    /*
     * @see com.iver.cit.gvsig.fmap.rendering.IVectorialLegend#getSymbol(int)
     */
    @Override
    public ISymbol getSymbol(int recordIndex) throws ReadDriverException {
	Value val = dataSource.getFieldValue(recordIndex, fieldId);
	ISymbol theSymbol = getSymbolByValue(val);

	return theSymbol;
    }

    /**
     * Devuelve un símbolo a partir de una IFeature. OJO!! Cuando usamos un
     * feature iterator de base de datos el único campo que vendrá rellenado es
     * el de fieldID. Los demás vendrán a nulos para ahorra tiempo de creación.
     *
     * @param feat
     *            IFeature
     *
     * @return Símbolo.
     */
    @Override
    public ISymbol getSymbolByFeature(IFeature feat) {
	// Value val =
	// feat.getAttribute(FLyrVect.forTestOnlyVariableUseIterators_REMOVE_THIS_FIELD
	// ? 0 :fieldId);
	Value val = feat.getAttribute(0);
	// Para evitar que salte un error cuando hemos borrado un campo en el
	// que estaba basada una leyenda
	if (val == null) {
	    return getDefaultSymbol();
	}
	// Fin
	ISymbol theSymbol = getSymbolByValue(val);

	if (theSymbol != null) {
	    return theSymbol;
	}
	return null;
    }

    @Override
    public ISymbol getDefaultSymbol() {

	if (defaultSymbol == null) {
	    defaultSymbol = SymbologyFactory
		    .createDefaultSymbolByShapeType(shapeType);
	    fireDefaultSymbolChangedEvent(new SymbolLegendEvent(null,
		    defaultSymbol));
	}
	return defaultSymbol;
    }

    @Override
    public XMLEntity getXMLEntity() {
	XMLEntity xml = new XMLEntity();
	xml.putProperty("className", this.getClass().getName());
	xml.putProperty("fieldNames", getClassifyingFieldNames()[0]);
	if (getClassifyingFieldTypes() != null) {
	    xml.putProperty("fieldTypes", getClassifyingFieldTypes()[0]);
	}
	xml.putProperty("ownOrder", isOwnOrder());

	xml.putProperty("orders", getOrders().toArray());

	if (selectedColors != null) {
	    String[] strColors = new String[selectedColors.length];
	    for (int i = 0; i < strColors.length; i++) {
		strColors[i] = StringUtilities.color2String(selectedColors[i]);
	    }
	    xml.putProperty("colorScheme", strColors);
	}

	xml.putProperty("labelfield", labelFieldName);
	xml.putProperty("labelFieldHeight", labelFieldHeight);
	xml.putProperty("labelFieldRotation", labelFieldRotation);

	xml.putProperty("useDefaultSymbol", useDefaultSymbol);
	xml.addChild(getDefaultSymbol().getXMLEntity());
	xml.putProperty("numKeys", keys.size());

	if (keys.size() > 0) {
	    xml.putProperty("tipoValueKeys", valueType);

	    String[] sk = new String[keys.size()];
	    int[] stk = new int[keys.size()];
	    Object[] values = getValues();
	    String[] sv = new String[values.length];
	    int[] stv = new int[values.length];

	    for (int i = 0; i < keys.size(); i++) {
		Value key = keys.get(i);
		sk[i] = key.toString();
		stk[i] = key.getSQLType();
	    }

	    for (int i = 0; i < values.length; i++) {
		Value value = (Value) values[i];
		if (value instanceof NullUniqueValue) {
		    sv[i] = ((NullUniqueValue) value).toString();
		    stv[i] = ((NullUniqueValue) value).getSQLType();
		} else {
		    sv[i] = value.toString();
		    stv[i] = value.getSQLType();
		}

		// ISymbol symbol = symbols.get(value);
		// PARCHE
		ISymbol symbol = getSymbolByKey(value);
		// FIN DEL PARCHE

		if (symbol != null) {
		    xml.addChild(symbol.getXMLEntity());
		}
	    }

	    xml.putProperty("keys", sk);
	    xml.putProperty("values", sv);
	    xml.putProperty("typeKeys", stk);
	    xml.putProperty("typeValues", stv);
	}

	if (getZSort() != null) {
	    XMLEntity xmlZSort = getZSort().getXMLEntity();
	    xmlZSort.putProperty("id", "zSort");
	    xml.addChild(xmlZSort);
	}
	return xml;
    }

    @Override
    public void setXMLEntity03(XMLEntity xml) {
	clear();
	setClassifyingFieldNames(new String[] { xml
		.getStringProperty("fieldName") });

	int useDefaultSymbol = xml.getIntProperty("useDefaultSymbol");

	if (useDefaultSymbol == 1) {
	    setDefaultSymbol(FSymbol.createFromXML03(xml.getChild(0)));
	} else {
	    setDefaultSymbol(null);
	}

	int numKeys = xml.getIntProperty("numKeys");

	if (numKeys > 0) {
	    String className = xml.getStringProperty("tipoValueKeys");
	    String[] sk = xml.getStringArrayProperty("keys");
	    String[] sv = xml.getStringArrayProperty("values");
	    Value auxValue;
	    Value auxValue2;

	    for (int i = 0; i < numKeys; i++) {
		try {
		    auxValue = ValueFactory.createValue(sk[i], className);
		    auxValue2 = ValueFactory.createValue(sv[i], className);

		    ISymbol sym = FSymbol.createFromXML03(xml.getChild(i
			    + useDefaultSymbol));

		    symbols.put(auxValue2, sym);
		    keys.add(auxValue);

		} catch (SemanticException e) {
		    log.error("Exception", e);
		    e.printStackTrace();
		}
	    }
	}
    }

    @Override
    public void setXMLEntity(XMLEntity xml) {
	clear();
	if (xml.contains("fieldName")) {
	    setClassifyingFieldNames(new String[] { xml
		    .getStringProperty("fieldName") });
	} else {
	    setClassifyingFieldNames(xml.getStringArrayProperty("fieldNames"));
	}

	if (xml.contains("fieldTypes")) {
	    setClassifyingFieldTypes(new int[] { xml
		    .getIntProperty("fieldTypes") });
	}

	if (xml.contains("colorScheme")) {
	    String[] strColors = xml.getStringArrayProperty("colorScheme");

	    Color[] cc = new Color[strColors.length];
	    for (int i = 0; i < cc.length; i++) {
		cc[i] = StringUtilities.string2Color(strColors[i]);
	    }
	    setColorScheme(cc);
	}

	if (xml.contains("ownOrder")) {
	    setOwnOrder(xml.getBooleanProperty("ownOrder"));
	}
	if (xml.contains("orders")) {
	    String[] ord = xml.getStringArrayProperty("orders");
	    ArrayList arrayOrd = new ArrayList();
	    for (int i = 0; i < ord.length; i++) {
		arrayOrd.add(ord[i]);
	    }
	    setOrders(arrayOrd);
	}

	useDefaultSymbol = xml.getBooleanProperty("useDefaultSymbol");
	setDefaultSymbol(SymbologyFactory.createSymbolFromXML(xml.getChild(0),
		null));

	int numKeys = xml.getIntProperty("numKeys");

	if (numKeys > 0) {
	    String className = xml.getStringProperty("tipoValueKeys");
	    String[] sk = xml.getStringArrayProperty("keys");
	    if (sk.length == 0) {
		sk = new String[] { "" };
	    }
	    String[] sv = xml.getStringArrayProperty("values");
	    if (sv.length == 0) {
		sv = new String[] { "" };
	    }
	    Value auxValue = null;
	    Value auxValue2 = null;
	    ISymbol sym;
	    int[] stk = null;
	    if (xml.contains("typeKeys")) {
		stk = xml.getIntArrayProperty("typeKeys");
		int[] stv = xml.getIntArrayProperty("typeValues");
		for (int i = 0; i < numKeys; i++) {
		    auxValue = getValue(sk[i], stk[i]);
		    if (auxValue instanceof NullValue) {
			auxValue = new NullUniqueValue();
		    } else {
			keys.add(auxValue);
		    }
		}

		boolean foundNullValue = false;
		for (int i = 0; i < sv.length; i++) {
		    auxValue2 = getValue(sv[i], stv[i]);
		    if (auxValue2 instanceof NullValue) {
			foundNullValue = true;
			auxValue2 = new NullUniqueValue();
			sym = getDefaultSymbol();
		    } else {
			if (foundNullValue) {
			    if (stv.length == stk.length
				    && xml.getChildrenCount() > stv.length) {
				sym = SymbologyFactory.createSymbolFromXML(
					xml.getChild(i + 1), null);
			    } else {
				sym = SymbologyFactory.createSymbolFromXML(
					xml.getChild(i), null);
			    }
			} else {
			    sym = SymbologyFactory.createSymbolFromXML(
				    xml.getChild(i + 1), null);
			}
		    }

		    symbols.put(auxValue2, sym);
		}
		if (!foundNullValue && useDefaultSymbol) {
		    auxValue2 = new NullUniqueValue();
		    sym = getDefaultSymbol();
		    symbols.put(auxValue2, sym);
		}
	    } else {

		for (int i = 0; i < numKeys; i++) {
		    auxValue = getValue(sk[i]);
		    if (auxValue == null) { // Default
			auxValue = new NullUniqueValue();
		    }
		    keys.add(auxValue);
		}

		boolean foundNullValue = false;
		for (int i = 0; i < sv.length; i++) {
		    auxValue2 = getValue(sv[i]);
		    if (auxValue2 == null) { // Default
			foundNullValue = true;
			auxValue2 = new NullUniqueValue();
			sym = getDefaultSymbol();
		    } else {
			sym = SymbologyFactory.createSymbolFromXML(
				xml.getChild(i + 1), null);
		    }

		    symbols.put(auxValue2, sym);
		}
		if (!foundNullValue && useDefaultSymbol) {
		    auxValue2 = new NullUniqueValue();
		    sym = getDefaultSymbol();
		    symbols.put(auxValue2, sym);
		}
	    }
	}

	XMLEntity zSortXML = xml.firstChild("id", "zSort");
	if (zSortXML != null) {
	    ZSort zSort = new ZSort(this);
	    zSort.setXMLEntity(zSortXML);
	    addLegendListener(zSort);
	    setZSort(zSort);
	}
    }

    @Override
    public void setDefaultSymbol(ISymbol s) {
	ISymbol mySymbol = defaultSymbol;

	if (s == null) {
	    throw new NullPointerException("Default symbol cannot be null");
	}

	ISymbol old = mySymbol;
	defaultSymbol = s;
	fireDefaultSymbolChangedEvent(new SymbolLegendEvent(old, s));
    }

    /**
     * Returns the value using the its value in a string.
     *
     *
     * @param s
     *            String with the value.
     * @deprecated Method used until 1.0 alpha 855 You should use
     *             getValue(String s,int type);
     * @return Value.
     */
    @Deprecated
    private Value getValue(String s) {
	Value val = new NullUniqueValue();
	if (s.equals("Resto de Valores")) {
	    return val;
	}
	try {
	    try {
		val = ValueFactory.createValueByType(s, Types.INTEGER);

		return val;
	    } catch (NumberFormatException e) {
	    }

	    try {
		val = ValueFactory.createValueByType(s, Types.BIGINT);

		return val;
	    } catch (NumberFormatException e) {
	    }

	    try {
		val = ValueFactory.createValueByType(s, Types.FLOAT);

		return val;
	    } catch (NumberFormatException e) {
	    }

	    try {
		val = ValueFactory.createValueByType(s, Types.DOUBLE);

		return val;
	    } catch (NumberFormatException e) {
	    }

	    val = ValueFactory.createValueByType(s, Types.LONGVARCHAR);

	} catch (ParseException e) {
	    log.warn("parse exception", e);
	}

	return val;
    }

    /**
     * Devuelve el valor a partir de su valor en un string.
     *
     * @param s
     *            String con el valor.
     *
     * @return Value.
     */
    private Value getValue(String s, int type) {
	Value val = new NullUniqueValue();
	if (type == Types.OTHER) {
	    return val;
	}
	try {
	    val = ValueFactory.createValueByType(s, type);
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	return val;
    }

    @Override
    public ILegend cloneLegend() throws XMLException {
	return LegendFactory.createFromXML(getXMLEntity());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.iver.cit.gvsig.fmap.rendering.VectorialLegend#setDataSource(com.hardcode
     * .gdbms.engine.data.DataSource)
     */
    @Override
    public void setDataSource(DataSource ds) throws FieldNotFoundException,
	    ReadDriverException {
	dataSource = ds;
	ds.start();
	if (getClassifyingFieldNames() != null
		&& getClassifyingFieldNames().length > 0) {
	    fieldId = ds.getFieldIndexByName(getClassifyingFieldNames()[0]);
	}
	ds.stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.iver.cit.gvsig.fmap.rendering.UniqueValueLegend#getSymbolByValue(
     * com.hardcode.gdbms.engine.values.Value)
     */
    @Override
    public ISymbol getSymbolByValue(Value key) {

	ISymbol symbol = symbols.get(key);

	// PARCHE
	// ISymbol symbol = getSymbolByKey(key);
	// FIN DEL PARCHE

	if (symbol != null) {
	    return symbol;
	} else if (useDefaultSymbol) {
	    return getDefaultSymbol();
	}
	return null;

    }

    private ISymbol getSymbolByKey(Value key) {
	// FIXME: Esto es un parche para sustituir symbols.get(key)
	// porque parece que no funciona bien el metodo get sobre un
	// TreeMap cuyas claves son Values. Si se consigue que funcione
	// correctamente, eliminar este metodo.
	if (key == null) {
	    return null;
	}
	ISymbol symbol = null;
	Set<Entry<Value, ISymbol>> entrySet = symbols.entrySet();
	Iterator<Entry<Value, ISymbol>> it = entrySet.iterator();
	while (it.hasNext()) {
	    Entry<Value, ISymbol> entry = it.next();
	    try {
		if (((BooleanValue) key.equals(entry.getKey())).getValue()) {
		    symbol = entry.getValue();
		}
	    } catch (IncompatibleTypesException e) {
		log.info("Cannot compare the values: " + key.toString() + " - "
			+ entry.getKey().toString(), e);
	    }
	}
	if (symbol != null) {
	    return symbol;
	}
	return null;
    }

    @Override
    public int getShapeType() {
	return shapeType;
    }

    @Override
    public void useDefaultSymbol(boolean b) {
	useDefaultSymbol = b;
    }

    /**
     * Devuelve si se utiliza o no el resto de valores para representarse.
     *
     * @return True si se utiliza el resto de valores.
     */
    @Override
    public boolean isUseDefaultSymbol() {
	return useDefaultSymbol;
    }

    @Override
    public void delSymbol(Object key) {
	keys.remove(key);
	ISymbol removedSymbol = symbols.remove(key);
	if (removedSymbol != null) {
	    fireClassifiedSymbolChangeEvent(new SymbolLegendEvent(
		    removedSymbol, null));
	}
    }

    @Override
    public String getClassName() {
	return getClass().getName();
    }

    @Override
    public void replace(ISymbol oldSymbol, ISymbol newSymbol) {
	// if (symbols.containsValue(oldSymbol)) {
	Iterator<Entry<Value, ISymbol>> it = symbols.entrySet().iterator();
	while (it.hasNext()) {
	    Entry<Value, ISymbol> entry = it.next();
	    if (entry.getValue().equals(oldSymbol)) {
		entry.setValue(newSymbol);
		fireClassifiedSymbolChangeEvent(new SymbolLegendEvent(
			oldSymbol, newSymbol));
		break;
	    }
	}
	if (oldSymbol.equals(this.getDefaultSymbol())) {
	    this.setDefaultSymbol(newSymbol);
	}

	// }
    }

    public Color[] getColorScheme() {
	return selectedColors;
    }

    public void setColorScheme(Color[] cc) {
	this.selectedColors = cc;
    }

    public boolean isOwnOrder() {
	return ownOrder;
    }

    public void setOwnOrder(boolean ownOrder) {
	this.ownOrder = ownOrder;
    }

    public ArrayList getOrders() {
	return orders;
    }

    public void setOrders(ArrayList orders) {
	this.orders = orders;
    }
}
