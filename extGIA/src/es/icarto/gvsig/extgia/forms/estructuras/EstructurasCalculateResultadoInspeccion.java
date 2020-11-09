package es.icarto.gvsig.extgia.forms.estructuras;

import javax.swing.JComboBox;

import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.IValidatableForm;
import es.icarto.gvsig.navtableforms.calculation.Calculation;

public class EstructurasCalculateResultadoInspeccion extends Calculation {

    public EstructurasCalculateResultadoInspeccion(IValidatableForm form) {
    super(form);
    setCalculateOnlyWhenValid(false);
    }

    @Override
    protected String resultName() {
    return DBFieldNames.RESULTADO;
    }

    @Override
    protected String[] operandNames() {
    return new String[] { DBFieldNames.RESULTADO_BASICA};
    }

    @Override
    protected String calculate() {
    JComboBox resultadoBasicaWidget = (JComboBox) operands.get(DBFieldNames.RESULTADO_BASICA);
    return resultadoBasicaWidget.getSelectedItem().toString();
    }




}
