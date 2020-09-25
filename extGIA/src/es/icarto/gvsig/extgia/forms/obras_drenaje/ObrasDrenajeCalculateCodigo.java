package es.icarto.gvsig.extgia.forms.obras_drenaje;

import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;

public class ObrasDrenajeCalculateCodigo extends CalculateComponentValue {

    public ObrasDrenajeCalculateCodigo(AbstractForm form,
            Map<String, JComponent> allFormWidgets, String resultComponentName,
            String... operatorComponentsNames) {
    super(form, allFormWidgets, resultComponentName, operatorComponentsNames);
    // TODO Auto-generated constructor stub
    }

    @Override
    public void setValue(boolean validate) {
    String materialCode = "";
    String codigo = "";
    
    if (validate) {
        JTextField pkWidget = (JTextField) operatorComponents.get(DBFieldNames.PK);
        JComboBox materialWidget = (JComboBox) operatorComponents.get(DBFieldNames.MATERIAL);
        JTextField seccionWidget = (JTextField)operatorComponents.get(DBFieldNames.SECCION);
    
        if (((KeyValue) materialWidget.getSelectedItem()) != null) {
            String materialValue = ((KeyValue) materialWidget.getSelectedItem()).getValue();
    
            if (materialValue.equals("Hormigón armado")) {
            materialCode = "HA";
            } else if (materialValue.equals("Hormigón en masa")) {
            materialCode = "HM";
            } else if (materialValue.equals("Acero corrugado")) {
            materialCode = "AC";
            } else if (materialValue.equals("Otros")) {
            materialCode = "OT";
            }
        }
        
        if (!pkWidget.getText().isEmpty() && !materialCode.isEmpty() && !seccionWidget.getText().isEmpty()) {
        codigo = pkWidget.getText() + " " + materialCode + " " + seccionWidget.getText(); 
        }
        
    }
    
    resultComponent.setText(codigo);
    form.getFormController().setValue(resultComponentName, codigo);

    }

}
