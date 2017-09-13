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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hardcode.gdbms.engine.values.DateValue;
import com.hardcode.gdbms.engine.values.DoubleValue;
import com.hardcode.gdbms.engine.values.FloatValue;
import com.hardcode.gdbms.engine.values.IntValue;
import com.hardcode.gdbms.engine.values.LongValue;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.Value;

/**
 * Clase intervalo.
 *
 * @author Vicente Caballero Navarro
 */
public class FInterval implements IInterval {
	private double from;
	private double to;
	public FInterval(){
	}
	/**
	 * Crea un nuevo FInterval.
	 *
	 * @param from Origen.
	 * @param to Destino.
	 */
	public FInterval(double from, double to) {
		this.from = from;
		this.to = to;
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.rendering.IInterval#isInInterval(com.hardcode.gdbms.engine.values.Value)
	 */
	public boolean isInInterval(Value v) {
		double valor=0;
		if (v instanceof IntValue) {
			valor = ((IntValue) v).getValue();
		} else if (v instanceof DoubleValue) {
			valor = ((DoubleValue) v).getValue();
		} else if (v instanceof FloatValue) {
			valor = ((FloatValue) v).getValue();
		} else if (v instanceof LongValue) {
			valor = ((LongValue) v).getValue();
		} else if (v instanceof DateValue) {
			//TODO POR IMPLEMENTAR
		} else if (v instanceof NullValue){
			return false;
		}
		return ((valor >= from) && (valor <= to));
	}

	/**
	 * Devuelve el número de origen.
	 *
	 * @return Número de inicio.
	 */
	public double getMin() {
		return from;
	}

	/**
	 * Devuelve el número final.
	 *
	 * @return Número final.
	 */
	public double getMax() {
		return to;
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.rendering.IInterval#toString()
	 */
	public String toString() {
		return from + "-" + to;
	}

	/**
	 * Crea un FInterval nuevo a partir de un String con el número inicial un
	 * guión y el número final.
	 *
	 * @param s String.
	 *
	 * @return FInterval nuevo.
	 */
	public static IInterval create(String s) {
		Pattern pattern = Pattern.compile("(-?[^-]*)-(-?.*)");
		Matcher matcher = pattern.matcher(s);
		IInterval inter=new NullIntervalValue(); // temporal pessimism
		if (matcher.find()) {
			try{
				inter = new FInterval(Double.parseDouble(matcher.group(1)),
						Double.parseDouble(matcher.group(2)));
			}catch (NumberFormatException e) {
			}
		}

		return inter;
	}
}
