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
package com.iver.cit.gvsig.fmap.rendering;

import java.util.ArrayList;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.values.DoubleValue;
import com.hardcode.gdbms.engine.values.FloatValue;
import com.hardcode.gdbms.engine.values.IntValue;
import com.hardcode.gdbms.engine.values.LongValue;
import com.hardcode.gdbms.engine.values.ShortValue;
import com.hardcode.gdbms.engine.values.Value;


/**
 * Calcula los intervalos en funci�n del n�mero de intervalos que se pidan.
 *
 * @author Vicente Caballero Navarro
 */
public class QuantileIntervalGenerator {
	protected DataSource sds;
	protected String msFieldName;
	protected int miNumIntervalosSolicitados;
	protected double[] mdaValoresRuptura;
	protected double[] mdaValInit;
	protected int num = 0;

	/**
	 * Crea un nuevo QuantileIntervalGenerator.
	 *
	 * @param layer DOCUMENT ME!
	 * @param field DOCUMENT ME!
	 * @param numIntervals DOCUMENT ME!
	 */
	public QuantileIntervalGenerator(DataSource recordSet, String field,
		int numIntervals) {
		sds = recordSet;
		msFieldName = field;
		miNumIntervalosSolicitados = numIntervals;
	}

	/**
	 * Genera los intervalos.
	 *
	 */
	public void generarIntervalos()
		throws ReadDriverException {
		ArrayList ordenadas = new ArrayList();
		ArrayList coincidencias = new ArrayList();
		int pos = sds.getFieldIndexByName(msFieldName);
		mdaValoresRuptura = new double[miNumIntervalosSolicitados - 1];
		mdaValInit = new double[miNumIntervalosSolicitados - 1];

		//int MARGEN = 5;
		for (int i = 0; i < sds.getRowCount(); i++) {
			insertarEnVector(ordenadas, coincidencias, sds.getFieldValue(i, pos));
		}
		
		int index = 0;
		int posj = 0;

		for (int i = 1; i < miNumIntervalosSolicitados; i++) {
			long x = (long) ((i * sds.getRowCount()) / miNumIntervalosSolicitados);

			for (int j = posj; j < ordenadas.size(); j++) {
				int auxcoin = ((Integer) coincidencias.get(j)).intValue();
				index = index + auxcoin;

				if (x <= index) {
					mdaValoresRuptura[i - 1] = getValue((Value) ordenadas.get(j));

					/*index = (int) ((x + (auxcoin / miNumIntervalosSolicitados)) -
					   1);
					 */
					posj = j + 1;

					if (posj < ordenadas.size()) {
						mdaValInit[i - 1] = getValue((Value) ordenadas.get(posj));
					} else {
						mdaValInit[i - 1] = getValue((Value) ordenadas.get(j));
					}

					num++;

					break;
				}
			}

			//double value=getValue(sds.getFieldValue(x,pos));
		}

		//}
	}

	/**
	 * Esta funci�n busca en el vector de datos la posici�n que le corresponde
	 * al valor almacenado en vdValor y devuelve dicha posici�n en
	 * vdValorAInsertar. Para hallar la posici�n se realiza una b�squeda
	 * binaria. Si se trata de un elemento que ya est� en el vector devolvemos
	 * el �ndice que le corresponde en rlIndiceCorrespondiente y false en
	 * rbNuevoElemento. Si se trata de un nuevo elemento que hay que
	 * insertar... devolvemos el �ndice en el que ir�a y True en
	 * rbNuevoElemento En caso de que ocurra alg�n error devuelve false
	 *
	 * @param rVectorDatos ArrayList con los datos.
	 * @param coincidencia �ndice.
	 * @param vdValorAInsertar Valor a insertar.
	 */
	protected void insertarEnVector(ArrayList rVectorDatos,
		ArrayList coincidencia, Value vdValorAInsertar) {
		int llIndiceIzq;
		int llIndiceDer;
		int llMedio;
		int indice = -1;
		double ldValorComparacion;
		double valorAInsertar = getValue(vdValorAInsertar);

		if (rVectorDatos.size() == 0) {
			rVectorDatos.add(vdValorAInsertar);
			coincidencia.add(new Integer(1));

			return;
		}

		llIndiceIzq = 0;
		llIndiceDer = rVectorDatos.size() - 1;
		llMedio = (llIndiceIzq + llIndiceDer) / 2; //'Divisi�n entera!

		while (llIndiceIzq <= llIndiceDer) {
			//'Coger el valor situado en la mitad de la zona de b�squeda como valor de comparaci�n
			ldValorComparacion = getValue((Value) rVectorDatos.get(llMedio));

			//'Si el valor a insertar es mayor que el valor de comparaci�n...
			if (valorAInsertar > ldValorComparacion) {
				//      'La zona de b�squeda queda restringida a la parte de la derecha
				llIndiceIzq = llMedio + 1;
				llMedio = (llIndiceIzq + llIndiceDer) / 2;

				//    'Si el valor a insertar es menor que el valor de comparaci�n...
			} else if (valorAInsertar < ldValorComparacion) {
				//          'La zona de b�squeda queda restringida a la parte de la derecha
				llIndiceDer = llMedio - 1;
				llMedio = (llIndiceIzq + llIndiceDer) / 2;

				//        'Si el valor de comparaci�n coincide con el valor a insertar
			} else if (valorAInsertar == ldValorComparacion) {
				indice = llMedio;

				int index = rVectorDatos.indexOf(vdValorAInsertar);
				int coin = ((Integer) coincidencia.get(index)).intValue() + 1;
				coincidencia.remove(index);
				coincidencia.add(index, new Integer(coin));

				return;
			}
		}

		//  'Nota:
		//  'En este caso (cuando en rbNuevoElemento se devuelve True) lo que hay que hacer al salir de esta funci�n
		//  'es a�adir un nuevo elemento al vector y desplazar todos los valores correspondientes a partir de rlIndiceCorrespondiente
		//  '�D�nde va el nuevo elemento?
		//  'El �ltimo sitio estudiado viene dado por el valor de llMedio.
		//  'Si el valor a insertar es menor que el valor almacenado en la posici�n llMedio, el nuevo valor deber� ir a su izquierda.
		//  'Si fuera mayor deber�a ir a su derecha.
		ldValorComparacion = getValue((Value) rVectorDatos.get(llMedio));

		if (valorAInsertar > ldValorComparacion) {
			indice = llMedio + 1;
		} else {
			indice = llMedio;
		}

		rVectorDatos.add(indice, vdValorAInsertar);
		coincidencia.add(indice, new Integer(1));
	}

	/**
	 * Devuelve el valor en un double del Value que se pasa como par�metro.
	 *
	 * @param value Value.
	 *
	 * @return valor.
	 */
	protected double getValue(Value value) {
		if (value instanceof IntValue) {
			return ((IntValue) value).intValue();
		} else if (value instanceof LongValue) {
			return ((LongValue) value).longValue();
		} else if (value instanceof FloatValue) {
			return ((FloatValue) value).floatValue();
		} else if (value instanceof ShortValue) {
			return ((ShortValue) value).shortValue();
		} else if (value instanceof DoubleValue){
			return ((DoubleValue) value).doubleValue();
		}
		return 0;

	}

	/**
	 * Devuelve el valor del punto de ruptura seg�n el �ndice que se pasa como
	 * par�metro.
	 *
	 * @param index �ndice del punto de ruptura.
	 *
	 * @return valor.
	 */
	public double getValRuptura(int index) {
		return mdaValoresRuptura[index];
	}

	/**
	 * Devuelve el valor inicial de cada intervalo.
	 *
	 * @param index �ndice del intervalo.
	 *
	 * @return valor del intervalo.
	 */
	public double getValInit(int index) {
		return mdaValInit[index];
	}

	/**
	 * Devuelve el n�mero de intervalos que se han generado.
	 *
	 * @return N�mero de intervalos generados.
	 */
	public int getNumIntervalGen() {
		return num + 1;
	}
}
