/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
 *   Av. Blasco Ibáñez, 50
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
package com.iver.cit.gvsig.fmap.rendering;

import com.hardcode.gdbms.engine.data.DataSource;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.ISLDCompatible;
import com.iver.cit.gvsig.fmap.core.SLDTags;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.IFillSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ILineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.IMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.core.v02.FSymbol;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.utiles.XMLEntity;


/**
 * Implements a legend composed by single symbols.
 * 
 * @author   Vicente Caballero Navarro
 */
public class SingleSymbolLegend extends AbstractLegend implements IVectorLegend {
	private ISymbol defaultSymbol;
    private int shapeType = FShape.POLYGON; // Por defecto, tipo polígono
	private ZSort zSort;

	/**
	 * Constructor method 
	 */
	public SingleSymbolLegend() {	}


	/**
	 * Convenience fast constructor.
	 *
	 * @param style Símbolo.
	 */
	public SingleSymbolLegend(ISymbol style) {
		defaultSymbol = style;

		if (style instanceof MultiShapeSymbol) {
			shapeType = FShape.MULTI;
		} else if (style instanceof IMarkerSymbol) {
			shapeType = FShape.POINT;
		} else if (style instanceof ILineSymbol) {
			shapeType = FShape.LINE;
		} else if (style instanceof IFillSymbol) {
			shapeType = FShape.POLYGON;
		}
	}

	public void setDefaultSymbol(ISymbol s) {
		if (s == null) throw new NullPointerException("Default symbol cannot be null");
		setShapeType(s.getSymbolType());
		ISymbol old = defaultSymbol;
		defaultSymbol = s;
		fireDefaultSymbolChangedEvent(new SymbolLegendEvent(old, s));
	}


	public ISymbol getSymbol(int recordIndex) {
		return defaultSymbol;
	}

	public ISymbol getDefaultSymbol() {
		if(defaultSymbol==null)
			defaultSymbol=SymbologyFactory.createDefaultFillSymbol();
		return defaultSymbol;
	}


	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className",this.getClass().getName());
		xml.addChild(defaultSymbol.getXMLEntity());

		if (zSort != null) {
			XMLEntity zSortXML = zSort.getXMLEntity();
			zSortXML.putProperty("id", "zSort");
			xml.addChild(zSortXML);
		}
		return xml;
	}

	public void setXMLEntity(XMLEntity xml) {
        ISymbol auxSym = SymbologyFactory.createSymbolFromXML(xml.getChild(0), null);
		setDefaultSymbol(auxSym);
		
		XMLEntity zSortXML = xml.firstChild("id", "zSort");
		if (zSortXML != null) {
			zSort = new ZSort(this);
			zSort.setXMLEntity(zSortXML);
		}
	}


	public ILegend cloneLegend() throws XMLException {
		return (ILegend) LegendFactory.createFromXML(getXMLEntity());
	}


	public void setDataSource(DataSource ds) {
		// No hacemos nada, no lo vamos a usar
	}

	public int getShapeType() {
		return shapeType;
	}

	public void setShapeType(int shapeType) {
		if (this.shapeType != shapeType) {
			defaultSymbol = SymbologyFactory.createDefaultSymbolByShapeType(shapeType);
			this.shapeType = shapeType;
		}
	}



    public ISymbol getSymbolByFeature(IFeature feat) {
        return defaultSymbol;
    }

	public void useDefaultSymbol(boolean b) {
		// TODO Auto-generated method stub
	}

    public String[] getUsedFields() {
        return new String[0];
    }

    public boolean isUseDefaultSymbol() {
    	return true;

    }


    public ZSort getZSort() {
    	return zSort;

    }

	public void setZSort(ZSort zSort) {
		if (zSort == null) {
			removeLegendListener(this.zSort);
		}
		this.zSort = zSort;
		addLegendListener(zSort);
	}



	public String getClassName() {
		return getClass().getName();
	}

    public boolean isSuitableForShapeType(int shapeType) {
		return getShapeType() == shapeType;
	} 

}
