package es.icarto.gvsig.extgex.forms.expropiations;

import java.math.BigDecimal;

import es.icarto.gvsig.navtableforms.IValidatableForm;
import es.icarto.gvsig.navtableforms.calculation.Calculation;

public class ImportePendienteTotalAutocalculado extends Calculation {

    private static final String FINCAS_IMPORTE_PENDIENTE_MEJORAS = "importe_pendiente_mejoras";
    private static final String FINCAS_IMPORTE_PENDIENTE_TERRENOS = "importe_pendiente_terrenos";
    public static final String FINCAS_IMPORTE_PENDIENTE_TOTAL_AUTOCALCULADO = "importe_pendiente_total_autocalculado";

    public ImportePendienteTotalAutocalculado(IValidatableForm form) {
	super(form);
    }

    @Override
    protected String resultName() {
	return FINCAS_IMPORTE_PENDIENTE_TOTAL_AUTOCALCULADO;
    }

    @Override
    protected String[] operandNames() {
	return new String[] { FINCAS_IMPORTE_PENDIENTE_MEJORAS,
		FINCAS_IMPORTE_PENDIENTE_TERRENOS };
    }

    @Override
    protected String calculate() {
	BigDecimal total = new BigDecimal(0);
	total = total.add(operandValue(FINCAS_IMPORTE_PENDIENTE_MEJORAS));
	total = total.add(operandValue(FINCAS_IMPORTE_PENDIENTE_TERRENOS));

	return formatter.format(total);
    }

}
