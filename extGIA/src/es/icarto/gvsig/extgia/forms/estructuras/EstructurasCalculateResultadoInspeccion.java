package es.icarto.gvsig.extgia.forms.estructuras;

import java.awt.Color;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.IValidatableForm;
import es.icarto.gvsig.navtableforms.calculation.Calculation;

public class EstructurasCalculateResultadoInspeccion extends Calculation {

    public EstructurasCalculateResultadoInspeccion(IValidatableForm form) {
    super(form);
    }

    @Override
    protected String resultName() {
    return DBFieldNames.RESULTADO;
    }

    @Override
    protected String[] operandNames() {
    return new String[] { DBFieldNames.RESULTADO_BASICA, "tipo_inspeccion" };
    }

    @Override
    protected String calculate() {
    JComboBox resultadoBasicaWidget = (JComboBox) operands.get(DBFieldNames.RESULTADO_BASICA);
    JComboBox tipoInspeccionWidget = (JComboBox) operands.get("tipo_inspeccion");
    if (tipoInspeccionWidget.getSelectedItem().toString().equalsIgnoreCase("Principal")) {
        resultWidget.setBackground(Color.WHITE);
        resultWidget.setEditable(true);
    }
    if (!resultadoBasicaWidget.getSelectedItem().toString().isEmpty()) {
    return resultadoBasicaWidget.getSelectedItem().toString();
    }
    return null;
    }




}
