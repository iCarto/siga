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
public class StartsWith extends AbstractOperator{

	public String addText(String s) {
		return toString()+"("+s+",\"\")";
	}
	public String toString() {
		return "startsWith";
	}
	public void eval(BSFManager interpreter) throws BSFException {
//		interpreter.eval(ExpressionFieldExtension.BEANSHELL,null,-1,-1,"boolean startsWith(String value1,String value2){return value1.startsWith(value2);};");
		interpreter.exec(ExpressionFieldExtension.JYTHON,null,-1,-1,"def startsWith(value1,value2):\n" +
				" return value1.startswith(value2)");
	}
	public boolean isEnable() {
		return (getType()==IOperator.STRING);
	}
	public String getTooltip(){
		return PluginServices.getText(this,"operator")+":  "+toString()+"("+PluginServices.getText(this,"parameter")+"1,"+PluginServices.getText(this,"parameter")+"2"+")"+"\n"+getDescription();
	}
	public String getDescription() {
        return PluginServices.getText(this, "parameter") + "1"+": " +
        PluginServices.getText(this, "string_value") + "\n"+
        PluginServices.getText(this, "parameter") + "2"+": " +
        PluginServices.getText(this, "string_value") + "\n"+
        PluginServices.getText(this, "returns") + ": " +
        PluginServices.getText(this, "boolean_value") + "\n" +
        PluginServices.getText(this, "description") + ": " +
        "Tests if this parameter1 starts with the specified parameter2.";
    }
}
