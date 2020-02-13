/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib��ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package org.gvsig.symbology.fmap.rendering;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.gvsig.symbology.fmap.labeling.parse.LabelExpressionParser;
import org.gvsig.symbology.fmap.labeling.parse.ParseException;
import org.gvsig.symbology.fmap.rendering.filter.operations.Expression;
import org.gvsig.symbology.fmap.rendering.filter.operations.ExpressionException;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.instruction.FieldNotFoundException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.fmap.Messages;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.drivers.legend.LegendDriverException;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.cit.gvsig.fmap.rendering.AbstractClassifiedVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.LegendFactory;
import com.iver.cit.gvsig.fmap.rendering.SymbolLegendEvent;
import com.iver.utiles.XMLEntity;


/**
 *
 * Implements a vectorial legend which represents the elements of a layer
 * depending on the value of an expression. That is, if the expression is
 * evaluated to true, then the symbol associated to the expression is painted.
 * In other case it is not showed.
 *
 * @author Pepe Vidal Salvador - jose.vidal.salvador@iver.es
 */
public class VectorFilterExpressionLegend extends AbstractClassifiedVectorLegend  {

	private int shapeType;
	private ISymbol defaultSymbol;
	private String labelFieldName;
	private String labelFieldHeight;
	private String labelFieldRotation;
	private boolean useDefaultSymbol = false;
	private String[] fNames;
	private Hashtable<String, Value> parser_symbol_table = new Hashtable<String, Value>();

	private ArrayList<Item> newSymbols = new ArrayList<Item>() {
		private static final long serialVersionUID = 1L;

		public int indexOf(String expr) {
			return super.indexOf(new Item(expr, null));
		}
	};


	private class Item {
		private ISymbol sym;
		private String expression;
		private Expression expParser;

		public Item(String expression, ISymbol sym) {
			this.expression = expression;
			this.sym = sym;

			try {
				this.expParser =  createExpressionParser(this.expression);
			} catch (ParseException e) {
				e.printStackTrace();
				Logger.getLogger(getClass()).error(Messages.getString("invalid_filter_expression"));
			}

		}

		private Expression createExpressionParser(String expressionString) throws ParseException {
			LabelExpressionParser parser = new LabelExpressionParser(new StringReader(expressionString), parser_symbol_table);
			try {
				parser.LabelExpression();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Expression expression = (Expression) parser.getStack().pop();
			return expression;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (!obj.getClass().equals(Item.class)) return false;
			return this.expression.equals(((Item) obj).expression);
		}

		public ISymbol getSym() {
			return sym;
		}

		public String getStringExpression() {
			return expression;
		}

		public Expression getExpressionForParser() {
			return expParser;
		}
	}

	/**
	 * Constructor method
	 *
	 * @param type shapetype of the layer
	 * @param fieldNames classifying field names used in the legend
	 */
	public VectorFilterExpressionLegend(int type,String[] fieldNames) {
		setShapeType(type);
		this.setClassifyingFieldNames(fieldNames);
		this.fNames = fieldNames;
	}

	/**
	 * Constructor method
	 *
	 */
	public VectorFilterExpressionLegend() { }


	public ISymbol getSymbolByFeature(IFeature feat) {
		ISymbol returnSymbol = null;
		Object result = null;
		fNames =  getClassifyingFieldNames();
		try {
			updateSymbolsTable(feat, fNames);

			for (int i = 0; i < newSymbols.size(); i++) {
				Expression expression = newSymbols.get(i).expParser;

				result = expression.evaluate();
				if (result.equals("Default")){
					return defaultSymbol;
				}

				if(result != null && (Boolean)result==true) {
					returnSymbol = newSymbols.get(i).sym;
					if (returnSymbol != null) {
						return returnSymbol;
					}
				}
			}
		} catch (ExpressionException e) {
			e.printStackTrace();
		} catch (LegendDriverException e) {
			Logger.getLogger(getClass()).error(Messages.getString("invalid_url"));
		}

		if(useDefaultSymbol)
			return getDefaultSymbol();

		return null;
	}
	/**
	 * Returns a HashTable containing the name of the field of an specific feature
	 * and its values
	 *
	 * @param feat specific feature
	 * @param fNames field names
	 * @return HashTable
	 * @throws LegendDriverException
	 */
	private void updateSymbolsTable(IFeature feat, String[] fNames) throws LegendDriverException {
		if (fNames != null)
			for (int j = 0; j < fNames.length; j++) {
				if(feat.getAttribute(j) != null)
					parser_symbol_table.put(fNames[j], feat.getAttribute(j));
				else throw new LegendDriverException(LegendDriverException.CLASSIFICATION_FIELDS_NOT_FOUND);
			}
	}


	public void addSymbol(Object key, ISymbol symbol) {
		newSymbols.add(new Item((String)key.toString(),
				symbol));
	}

	public void clear() {
		newSymbols.clear();
	}

	public void delSymbol(Object key) {
		ISymbol mySymbol = null;
		for (int i = 0; i < newSymbols.size(); i++) {
			if (newSymbols.get(i).sym.equals(key))
				newSymbols.remove(i);
		}
		fireClassifiedSymbolChangeEvent(new SymbolLegendEvent(mySymbol,null));
	}


	public void replace(ISymbol oldSymbol, ISymbol newSymbol) {

		for (int i = 0; i < newSymbols.size(); i++) {
			if (newSymbols.get(i).sym.equals(oldSymbol))
				newSymbols.get(i).sym = newSymbol;
		}

		fireClassifiedSymbolChangeEvent(new SymbolLegendEvent(oldSymbol,newSymbol));
	}


	public String[] getDescriptions() {
		String[] descriptions = new String[newSymbols.size()];
		ISymbol[] auxSym = getSymbols();

		for (int i = 0; i < descriptions.length; i++)
			descriptions[i] = auxSym[i].getDescription();

		return descriptions;
	}

	public ISymbol[] getSymbols() {

		if (newSymbols != null) {
			ISymbol[] mySymbols = new ISymbol[newSymbols.size()];
			for (int i = 0; i < newSymbols.size(); i++) {
				mySymbols[i] = newSymbols.get(i).sym;
			}
			return mySymbols;
		}
		return null;
	}



	public ILegend cloneLegend() throws XMLException {
		return LegendFactory.createFromXML(getXMLEntity());
	}

	public ISymbol getDefaultSymbol() {
		if(defaultSymbol==null) {
			defaultSymbol = SymbologyFactory.createDefaultSymbolByShapeType(shapeType);
			fireDefaultSymbolChangedEvent(new SymbolLegendEvent(null, defaultSymbol));
		}
		return defaultSymbol;
	}


	public String getClassName() {
		return getClass().getName();
	}

	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", this.getClass().getName());
		if(getClassifyingFieldNames() != null)
			xml.putProperty("fieldNames", getClassifyingFieldNames());
		if(getClassifyingFieldTypes() != null)
			xml.putProperty("fieldTypes", getClassifyingFieldTypes());
		xml.putProperty("labelfield", labelFieldName);
		xml.putProperty("labelFieldHeight", labelFieldHeight);
		xml.putProperty("labelFieldRotation", labelFieldRotation);
		xml.putProperty("useDefaultSym", useDefaultSymbol);
		if (getDefaultSymbol() == null) {
			xml.putProperty("useDefaultSymbol", 0);
		} else {
			xml.putProperty("useDefaultSymbol", 1);
			xml.addChild(getDefaultSymbol().getXMLEntity());
		}

		xml.putProperty("numKeys", newSymbols.size());

		if (newSymbols.size() > 0) {
			xml.putProperty("tipoValueKeys", "Expressions");

			String[] sk = new String[newSymbols.size()];

			for (int i = 0; i < newSymbols.size(); i++) {
				sk[i] = newSymbols.get(i).expression.toString();
			}
			xml.putProperty("keys", getValues());
			int numKeys=0;
			for (int i = 0; i < newSymbols.size(); i++) {
				if (!newSymbols.get(i).getStringExpression().equals("Default")){
					xml.addChild(getSymbols()[i].getXMLEntity());
					numKeys++;
				}
			}
			xml.putProperty("numKeys", numKeys);
		}


		if (getZSort()!=null) {
			XMLEntity xmlZSort = getZSort().getXMLEntity();
			xmlZSort.putProperty("id", "zSort");
			xml.addChild(xmlZSort);
		}
		return xml;
	}

	public void setXMLEntity(XMLEntity xml) {
		clear();
		if (xml.contains("fieldName"))
			setClassifyingFieldNames(new String[] {xml.getStringProperty("fieldName")});
		else if (xml.contains("fieldNames")){
			setClassifyingFieldNames(xml.getStringArrayProperty("fieldNames"));
		}
//		if (xml.contains("fieldTypes"))
//			setClassifyingFieldTypes(new int[] {xml.getIntProperty("fieldTypes")});

		int hasDefaultSymbol = xml.getIntProperty("useDefaultSymbol");
		if (hasDefaultSymbol == 1) {
			useDefaultSymbol = true;
			setDefaultSymbol(SymbologyFactory.createSymbolFromXML(xml.getChild(0), null));
		} else {
			useDefaultSymbol = false;
			setDefaultSymbol(null);
		}
		if (xml.contains("useDefaultSym")){
			useDefaultSymbol=xml.getBooleanProperty("useDefaultSym");
		}
		int numKeys = xml.getIntProperty("numKeys");
		if (numKeys > 0) {
			String[] sk = xml.getStringArrayProperty("keys");
			String auxExpression;

			for (int i = 0; i < numKeys; i++) {
				auxExpression = sk[i];
				newSymbols.add(new Item(auxExpression,SymbologyFactory.createSymbolFromXML(xml.getChild(i + hasDefaultSymbol), null)));
				System.out.println("auxExpression =" + auxExpression + "Symbol =" +
						SymbologyFactory.createSymbolFromXML(xml.getChild(i + hasDefaultSymbol), null)
						.getDescription()+"\n");
			}
		}
	}

	public int getShapeType() {
		return shapeType;
	}

	public ISymbol getSymbol(int i) throws ReadDriverException {

		return null;
	}


	public boolean isUseDefaultSymbol() {
		return useDefaultSymbol;
	}

	public void setDataSource(DataSource ds) throws FieldNotFoundException,
	ReadDriverException {
//		dataSource = ds;
	}

	public void setDefaultSymbol(ISymbol s) throws IllegalArgumentException {
		if (s == null) throw new NullPointerException("Default symbol cannot be null");
		ISymbol old = defaultSymbol;
		defaultSymbol = s;
		fireDefaultSymbolChangedEvent(new SymbolLegendEvent(old, defaultSymbol));
	}

	public void setShapeType(int shapeType) {
		if (this.shapeType != shapeType) {
			setDefaultSymbol(SymbologyFactory.
					createDefaultSymbolByShapeType(shapeType));
			this.shapeType = shapeType;
		}
	}

	public void useDefaultSymbol(boolean b) {
		useDefaultSymbol = b;
	}

	public Object[] getValues() {
		if (newSymbols != null) {
			Object[] myObjects = new Object[newSymbols.size()];
			for (int i = 0; i < newSymbols.size(); i++) {
				myObjects[i] =newSymbols.get(i).expression;
			}
			return myObjects;
		}
		return null;
	}



}
