package es.icarto.gvsig.extgia.forms.pretiles;

import java.math.BigDecimal;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import es.icarto.gvsig.navtableforms.IValidatableForm;
import es.icarto.gvsig.navtableforms.calculation.Calculation;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;

public class PretilesCalculateSistemaContencion extends Calculation {

    public PretilesCalculateSistemaContencion(IValidatableForm form) {
        super(form);
    }

    @Override
    protected String resultName() {
        return "sistema_contencion";
    }

    @Override
    protected String[] operandNames() {
        return new String[] { "nivel_contencion", "anchura_trabajo", "deflexion" };
    }

    @Override
    protected String calculate() {
        JComboBox nivelContencionWidget = (JComboBox) operands.get("nivel_contencion");
        String nivelContencionValue = ((KeyValue) nivelContencionWidget.getSelectedItem()).getKey();
        if (nivelContencionValue.trim().isEmpty()) {
            return null;
        }

        JComboBox anchuraTrabajoWidget = (JComboBox) operands.get("anchura_trabajo");
        String anchuraTrabajoValue = ((KeyValue) anchuraTrabajoWidget.getSelectedItem()).getKey();
        if (anchuraTrabajoValue.trim().isEmpty()) {
            return null;
        }

        JTextField deflexionWidget = (JTextField) operands.get("deflexion");
        String deflexionValue = deflexionWidget.getText();
        if (deflexionValue.trim().isEmpty()) {
            return null;
        }
        BigDecimal deflexion = operandValue("deflexion");

        return nivelContencionValue + " " + anchuraTrabajoValue + " D=" + formatter.format(deflexion);
    }

}
