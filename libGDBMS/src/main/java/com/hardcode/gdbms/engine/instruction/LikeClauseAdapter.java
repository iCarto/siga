/*
 * Created on 12-oct-2004
 */
package com.hardcode.gdbms.engine.instruction;

import com.hardcode.gdbms.parser.Node;


/**
 * Adaptador
 *
 * @author Fernando Gonz�lez Cort�s
 */
public class LikeClauseAdapter extends Adapter {
	private String pattern;
	private boolean negated = false;

	/**
	 * Obtiene el patron del like
	 *
	 * @return String
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Obtiene si es LIKE o NOT LIKE
	 *
	 * @return si se usa NOT devuelve true, false en caso contrario
	 */
	public boolean isNegated() {
		return negated;
	}

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Adapter#setEntity(com.hardcode.gdbms.parser.Node)
	 */
	public void setEntity(Node o) {
		super.setEntity(o);

		String text = Utilities.getText(getEntity());
		pattern = text.substring(0, text.lastIndexOf("'"));
		pattern = pattern.substring(pattern.lastIndexOf("'") + 1);

		if (text.trim().startsWith("not")) {
			negated = true;
		}
	}
}
