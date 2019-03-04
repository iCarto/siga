package es.icarto.gvsig.extgia.forms;

import es.icarto.gvsig.extgia.forms.pretiles.PretilesForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.CommonMethodsForTestDBForms;

public class TestPretilesForm extends CommonMethodsForTestDBForms {

    @Override
    protected String getSchema() {
        return DBFieldNames.GIA_SCHEMA;
    }

    @Override
    protected String getTableName() {
        return PretilesForm.TABLENAME;
    }

}