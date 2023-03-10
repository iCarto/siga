package com.iver.cit.gvsig.project.documents.table.operators;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.ExpressionFieldExtension;
import com.iver.cit.gvsig.project.documents.table.AbstractOperator;
import com.iver.cit.gvsig.project.documents.table.IOperator;

/**
 * @author Vicente Caballero Navarro
 */
public class Pi extends AbstractOperator{

	public String addText(String s) {
		return s.concat(toString()+"()");
	}
	public String toString() {
		return "pi";
	}
	public void eval(BSFManager interpreter) throws BSFException {
//		interpreter.eval(ExpressionFieldExtension.BEANSHELL,null,-1,-1,"double pi(){return java.lang.Math.PI;};");
		interpreter.exec(ExpressionFieldExtension.JYTHON,null,-1,-1,"def pi():\n" +
				"  import java.lang.Math\n" +
				"  return java.lang.Math.PI");
	}
	public boolean isEnable() {
		return (getType()==IOperator.NUMBER);
	}
	public String getTooltip(){
		return PluginServices.getText(this,"operator")+":  "+addText("")+"\n"+getDescription();
	}
	public String getDescription() {
        return PluginServices.getText(this, "returns") + ": " +
        PluginServices.getText(this, "numeric_value") + "\n" +
        PluginServices.getText(this, "description") + ": " +
        "The double value that is closer than any other to pi, the ratio of the circumference of a circle to its diameter.";
    }
}
