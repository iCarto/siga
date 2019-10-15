package es.icarto.gvsig.extgia.forms.barrera_rigida;

import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;

public class BarreraRigidaCalculateIDValue extends CalculateComponentValue {

    public BarreraRigidaCalculateIDValue(AbstractForm form, HashMap<String, JComponent> allFormWidgets,
            String resultComponentName, String... operatorComponentsNames) {
        super(form, allFormWidgets, resultComponentName, operatorComponentsNames);
    }

    /**
     * ("BR")&("-")&("Número de Barrera")&((Primera letra de
     * "Base de contratista")|("X" si es AG)) ; EJ: BR-001N, BR-001X
     *
     */
    @Override
    public void setValue(boolean validate) {
        JTextField numeroWidget = (JTextField) operatorComponents.get(DBFieldNames.NUMERO_BARRERA_RIGIDA);
        JComboBox baseContratistaWidget = (JComboBox) operatorComponents.get(DBFieldNames.BASE_CONTRATISTA);
        JComboBox tramoWidget = (JComboBox) operatorComponents.get(DBFieldNames.TRAMO);

        if (numeroWidget.getText().isEmpty()) {
            validate = false;
        }

        String id = "";
        if (validate) {
            boolean isAG = ((KeyValue) tramoWidget.getSelectedItem()).getValue().startsWith("AG");
            String lastLetter = ((KeyValue) baseContratistaWidget.getSelectedItem()).getValue().substring(0, 1);
            if (isAG) {
                lastLetter = "X";
            }

            String tipoValue = "BR";
            Integer numeroValue = Integer.valueOf(numeroWidget.getText());
            id = String.format("%s-%03d%s", tipoValue, numeroValue, lastLetter);
        }
        resultComponent.setText(id);
        form.getFormController().setValue(resultComponentName, id);

    }

}
