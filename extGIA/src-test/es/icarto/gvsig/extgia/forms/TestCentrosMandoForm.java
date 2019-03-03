package es.icarto.gvsig.extgia.forms;

import es.icarto.gvsig.extgia.forms.centros_mando.CentrosMandoForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.CommonMethodsForTestDBForms;

public class TestCentrosMandoForm extends CommonMethodsForTestDBForms {

    @Override
    protected String getSchema() {
        return DBFieldNames.GIA_SCHEMA;
    }

    @Override
    protected String getTableName() {
        return CentrosMandoForm.TABLENAME;
    }

}