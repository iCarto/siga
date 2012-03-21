package com.hardcode.gdbms.engine.function;

import java.util.HashMap;


/**
 * DOCUMENT ME!
 *
 * @author Fernando Gonz�lez Cort�s
 */
public class FunctionManager {
	private static HashMap nameFunction = new HashMap();
	static {
		addFunction(new ConcatenateFunction());
		addFunction(new DateFunction());
		addFunction(new BooleanFunction());
		addFunction(new Count());
		addFunction(new Sum());
	}

	/**
	 * A�ade una nueva funci�n al sistema
	 *
	 * @param function funci�n
	 *
	 * @throws RuntimeException DOCUMENT ME!
	 */
	public static void addFunction(Function function) {
		if (nameFunction.get(function.getName()) != null) {
			throw new RuntimeException("Ya hay una funci�n llamada " +
				function.getName());
		}

		nameFunction.put(function.getName(), function);
	}

	/**
	 * Obtiene la funcion de nombre name
	 *
	 * @param name nombre de la funcion que se quiere obtener
	 *
	 * @return funci�n o null si no hay ninguna funci�n que devuelva dicho
	 * 		   nombre
	 */
	public static Function getFunction(String name) {
		return ((Function) nameFunction.get(name)).cloneFunction();
	}
}
