package es.icarto.gvsig.extgia.forms;

import es.icarto.gvsig.extgia.forms.obras_drenaje.ObrasDrenajeForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.CommonMethodsForTestDBForms;

public class TestObrasDrenajeForm extends CommonMethodsForTestDBForms {

    @Override
    protected String getSchema() {
	return DBFieldNames.GIA_SCHEMA;
    }

    @Override
    protected String getTableName() {
	return ObrasDrenajeForm.TABLENAME;
    }

}
