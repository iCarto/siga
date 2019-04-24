package es.icarto.gvsig.extgex.forms.expropiations;

import java.math.BigDecimal;

import es.icarto.gvsig.navtableforms.IValidatableForm;
import es.icarto.gvsig.navtableforms.calculation.Calculation;

public class ImportePagadoTotalAutocalculado extends Calculation {

    private static final String MUTUO_ACUERDO_IMPORTE = "mutuo_acuerdo_importe";
    private static final String ANTICIPO_IMPORTE = "anticipo_importe";
    private static final String DEPOSITO_PREVIO_PAGADO_IMPORTE = "deposito_previo_pagado_importe";
    private static final String DEPOSITO_PREVIO_CONSIGNADO_IMPORTE = "deposito_previo_consignado_importe";
    private static final String MUTUO_ACUERDO_PARCIAL_IMPORTE = "mutuo_acuerdo_parcial_importe";
    private static final String PAGOS_VARIOS_IMPORTE = "pagos_varios_importe";
    private static final String DEPOSITO_PREVIO_LEVANTADO_IMPORTE = "deposito_previo_levantado_importe";
    public static final String IMPORTE_PAGADO_TOTAL_AUTOCALCULADO = "importe_pagado_total_autocalculado";
    private static final String DEPOSITO_PREVIO_CONSIGNADO_INDEMNIZACION = "deposito_previo_consignado_indemnizacion";
    private static final String LIMITE_ACUERDO_IMORTE = "limite_acuerdo_importe";
    private static final String INDEMNIZACION_IMPORTE = "indemnizacion_importe";
    private static final String JUSTIPRECIO_IMPORTE = "justiprecio_importe";

    // importe_pagado_total_autocalculado - deposito_previo_consignado_importe +
    // deposito_previo_consignado_indemnizacion + deposito_previo_pagado_importe + mutuo_acuerdo_importe +
    // anticipo_importe + mutuo_acuerdo_parcial_importe + limite_acuerdo_importe + pagos_varios_importe +
    // indemnizacion_importe + justiprecio_importe - deposito_previo_levantado_importe
    public ImportePagadoTotalAutocalculado(IValidatableForm form) {
        super(form);
    }

    @Override
    protected String resultName() {
        return IMPORTE_PAGADO_TOTAL_AUTOCALCULADO;
    }

    @Override
    protected String[] operandNames() {
        return new String[] { DEPOSITO_PREVIO_LEVANTADO_IMPORTE, DEPOSITO_PREVIO_CONSIGNADO_IMPORTE,
                DEPOSITO_PREVIO_CONSIGNADO_INDEMNIZACION, DEPOSITO_PREVIO_PAGADO_IMPORTE, MUTUO_ACUERDO_IMPORTE,
                ANTICIPO_IMPORTE, MUTUO_ACUERDO_PARCIAL_IMPORTE, LIMITE_ACUERDO_IMORTE, PAGOS_VARIOS_IMPORTE,
                INDEMNIZACION_IMPORTE, JUSTIPRECIO_IMPORTE };
    }

    @Override
    protected String calculate() {

        BigDecimal total = new BigDecimal(0);

        total = total.add(operandValue(DEPOSITO_PREVIO_CONSIGNADO_IMPORTE));
        total = total.add(operandValue(DEPOSITO_PREVIO_CONSIGNADO_INDEMNIZACION));
        total = total.add(operandValue(DEPOSITO_PREVIO_PAGADO_IMPORTE));
        total = total.add(operandValue(MUTUO_ACUERDO_IMPORTE));
        total = total.add(operandValue(ANTICIPO_IMPORTE));
        total = total.add(operandValue(MUTUO_ACUERDO_PARCIAL_IMPORTE));
        total = total.add(operandValue(LIMITE_ACUERDO_IMORTE));
        total = total.add(operandValue(PAGOS_VARIOS_IMPORTE));
        total = total.add(operandValue(INDEMNIZACION_IMPORTE));
        total = total.add(operandValue(JUSTIPRECIO_IMPORTE));
        total = total.subtract(operandValue(DEPOSITO_PREVIO_LEVANTADO_IMPORTE));

        return formatter.format(total);
    }
}
