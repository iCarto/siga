package com.hardcode.gdbms.engine.strategies;

import com.hardcode.gdbms.engine.instruction.CustomAdapter;
import com.hardcode.gdbms.engine.instruction.SelectAdapter;
import com.hardcode.gdbms.engine.instruction.UnionAdapter;


/**
 * Manejador de las distintas estrategias disponibles para ejecutar las
 * instrucciones
 *
 * @author Fernando Gonz�lez Cort�s
 */
public class StrategyManager {
	/**
	 * Obtiene la estrategia m�s adecuada en funci�n de la instrucci�n a
	 * ejecutar y de las condiciones actuales del sistema
	 *
	 * @param instr Instrucci�n que se desea ejecutar
	 *
	 * @return estrategia capaz de ejecutar la instrucci�n
	 */
	public static Strategy getStrategy(SelectAdapter instr) {
		return new FirstStrategy();
	}

	/**
	 * Obtiene la estrategia �ptima para ejecutar la instrucci�n de union que
	 * se pasa como par�metro
	 *
	 * @param instr instrucci�n que se quiere ejecutar
	 *
	 * @return
	 */
	public static Strategy getStrategy(UnionAdapter instr) {
		return new FirstStrategy();
	}

	/**
	 * Gets the only strategy to execute custom queries
	 *
	 * @param instr root node of the custom query to execute
	 *
	 * @return Strategy
	 */
	public static Strategy getStrategy(CustomAdapter instr) {
		return new FirstStrategy();
	}
}
