package com.iver.cit.gvsig.fmap.core.symbols;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
/**
 * 
 * ITextSymbol.java<br>
 * Represents an ISymbol that draws a text.
 * 
 * @author jaume dominguez faus - jaume.dominguez@iver.es Dec 11, 2007
 *
 */
public interface ITextSymbol extends ISymbol {
	/**
	 * Establishes the font that will be used to render this ITextSymbol.
	 * @param font
	 */
	public abstract void setFont(Font font);

	/**
	 * Returns the currently set font.
	 * @return Font
	 */
	public abstract Font getFont();

	/**
	 * Returns the currently color set to be applied to the text
	 * @return Color
	 */
	public abstract Color getTextColor();

	/**
	 * Sets the color of the text
	 * @param color
	 */
	public abstract void setTextColor(Color color);

	/**
	 * Returns the text contained by this symbol
	 * @return
	 * @deprecated ?do i need it?
	 */
	public abstract String getText();

	/**
	 * Sets the text to be rendered by this symbol
	 * @param text, a String
	 */
	public abstract void setText(String text);

	/**
	 * Sets the font size currently set to this symbol
	 * @param d
	 */
	public abstract void setFontSize(double d);

	/**
	 * Computes a FShape wrapping the text to be applied
	 * @param p target location
	 * @return
	 */
	public abstract FShape getTextWrappingShape(FPoint2D p);
	
	public abstract Rectangle getBounds();
	
	public abstract void setAutoresizeEnabled(boolean autoresizeFlag);
	
	public abstract boolean isAutoresizeEnabled();
	
	public Color getHaloColor();
	public void setHaloColor(Color haloColor);

	public float getHaloWidth();
	public void setHaloWidth(float haloWidth);

	public boolean isDrawWithHalo();
	public void setDrawWithHalo(boolean drawWithHalo);

}