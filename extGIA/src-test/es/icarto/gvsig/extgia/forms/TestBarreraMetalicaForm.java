package es.icarto.gvsig.extgia.forms;

import es.icarto.gvsig.extgia.forms.barrera_metalica.BarreraMetalicaForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.CommonMethodsForTestDBForms;

public class TestBarreraMetalicaForm extends CommonMethodsForTestDBForms {

    @Override
    protected String getSchema() {
        return DBFieldNames.GIA_SCHEMA;
    }

    @Override
    protected String getTableName() {
        return BarreraMetalicaForm.TABLENAME;
    }

}