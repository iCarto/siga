package es.icarto.gvsig.extgia.forms;

import es.icarto.gvsig.extgia.forms.lineas_distribucion_ufd.LineasDistribucionUFDForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.CommonMethodsForTestDBForms;

public class TestLineasDistribucionUFDForm extends CommonMethodsForTestDBForms {

    @Override
    protected String getSchema() {
	return DBFieldNames.GIA_SCHEMA;
    }

    @Override
    protected String getTableName() {
	return LineasDistribucionUFDForm.TABLENAME;
    }

}
