package es.icarto.gvsig.extgex.forms.expropiations;

import java.math.BigDecimal;

import es.icarto.gvsig.navtableforms.IValidatableForm;
import es.icarto.gvsig.navtableforms.calculation.Calculation;

public class ImporteTotalPagadoCalculation extends Calculation {

    private static final String FINCAS_MUTUO_ACUERDO = "mutuo_acuerdo_importe";
    private static final String FINCAS_ANTICIPO = "anticipo_importe";
    private static final String FINCAS_DEPOSITO_PREVIO_PAGADO = "deposito_previo_pagado_importe";
    private static final String FINCAS_DEPOSITO_PREVIO_CONSIGNADO = "deposito_previo_consignado_importe";
    private static final String FINCAS_MUTUO_ACUERDO_PARCIAL = "mutuo_acuerdo_parcial_importe";
    private static final String FINCAS_PAGOS_VARIOS = "pagos_varios_importe";
    private static final String FINCAS_DEPOSITO_PREVIO_LEVANTADO = "deposito_previo_levantado_importe";
    public static final String FINCAS_IMPORTE_PAGADO_TOTAL_AUTOCALCULADO = "importe_pagado_total_autocalculado";
    private static final String FINCAS_DEPOSITO_PREVIO_CONSIGNADO_INDEMNIZACION = "deposito_previo_consignado_indemnizacion";
    private static final String FINCAS_LIMITE_ACUERDO_IMORTE = "limite_acuerdo_importe";
    private static final String FINCAS_INDEMNIZACION_IMPORTE = "indemnizacion_importe";
    private static final String FINCAS_JUSTIPRECIO_IMPORTE = "justiprecio_importe";

    public ImporteTotalPagadoCalculation(IValidatableForm form) {
	super(form);
    }

    @Override
    protected String resultName() {
	return FINCAS_IMPORTE_PAGADO_TOTAL_AUTOCALCULADO;
    }

    @Override
    protected String[] operandNames() {
	return new String[] { FINCAS_DEPOSITO_PREVIO_LEVANTADO,
		FINCAS_DEPOSITO_PREVIO_CONSIGNADO,
		FINCAS_DEPOSITO_PREVIO_CONSIGNADO_INDEMNIZACION,
		FINCAS_DEPOSITO_PREVIO_PAGADO, FINCAS_MUTUO_ACUERDO,
		FINCAS_ANTICIPO, FINCAS_MUTUO_ACUERDO_PARCIAL,
		FINCAS_LIMITE_ACUERDO_IMORTE, FINCAS_PAGOS_VARIOS,
		FINCAS_INDEMNIZACION_IMPORTE, FINCAS_JUSTIPRECIO_IMPORTE };
    }

    @Override
    protected String calculate() {

	BigDecimal total = new BigDecimal(0);

	total = total.subtract(operandValue(FINCAS_DEPOSITO_PREVIO_LEVANTADO));
	total = total.add(operandValue(FINCAS_DEPOSITO_PREVIO_CONSIGNADO));
	total = total
		.add(operandValue(FINCAS_DEPOSITO_PREVIO_CONSIGNADO_INDEMNIZACION));
	total = total.add(operandValue(FINCAS_DEPOSITO_PREVIO_PAGADO));
	total = total.add(operandValue(FINCAS_MUTUO_ACUERDO));
	total = total.add(operandValue(FINCAS_ANTICIPO));
	total = total.add(operandValue(FINCAS_MUTUO_ACUERDO_PARCIAL));
	total = total.add(operandValue(FINCAS_LIMITE_ACUERDO_IMORTE));
	total = total.add(operandValue(FINCAS_PAGOS_VARIOS));
	total = total.add(operandValue(FINCAS_INDEMNIZACION_IMPORTE));
	total = total.add(operandValue(FINCAS_JUSTIPRECIO_IMPORTE));

	return formatter.format(total);
    }
}
