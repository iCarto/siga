package org.gvsig.mapsheets.print.series.gui.utils;

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * GUI utility class to force user to enter numeric value
 * @author jldominguez
 *
 */
public class NumericDocument extends PlainDocument {
	
	     //Variables
	     protected int decimalPrecision = 5;
	     protected boolean allowNegative = true;

	     public NumericDocument() {
	          super();
	     }

	     //Constructor
	     private NumericDocument(int d, boolean n) {
	          super();
	          this.decimalPrecision = d;
	          this.allowNegative = n;
	     }
	   
	     //Insert string method
	     public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
	          if (str != null){
	               if (!isNaturalNumber(str)) { //  == false && str.equals(",") == false && str.equals("-") == false){ //First, is it a valid character?
	                    Toolkit.getDefaultToolkit().beep();
	                    return;
	               }
	               /*
	               else if (str.equals(",") == true && super.getText(0, super.getLength()).contains(",") == true){ //Next, can we place a decimal here?
	                    Toolkit.getDefaultToolkit().beep();
	                    return;
	               }
	               else if (isNaturalNumber(str) == true && super.getText(0, super.getLength()).indexOf(",") != -1 && offset>super.getText(0, super.getLength()).indexOf(",") && super.getLength()-super.getText(0, super.getLength()).indexOf(",")>decimalPrecision && decimalPrecision > 0){ //Next, do we get past the decimal precision limit?
	                    Toolkit.getDefaultToolkit().beep();
	                    return;
	               }
	               else if (str.equals("-") == true && (offset != 0 || allowNegative == false)){ //Next, can we put a negative sign?
	                    Toolkit.getDefaultToolkit().beep();
	                    return;
	               }
	               */
	               //All is fine, so add the character to the text box
	               super.insertString(offset, str, attr);
	          }
	          return;
	     }
	     
	     public static boolean isNaturalNumber(String str) {
	    	 
	    	 try {
	    		 Long.parseLong(str);
	    		 return true;
	    	 } catch (Exception ex) {
	    		 return false;
	    	 }
	    	 
	     }
	}

//	////////////In a method....
//	JTextField txt = new JTextField(10);
//	txt.setDocument(new NumericDocument());

