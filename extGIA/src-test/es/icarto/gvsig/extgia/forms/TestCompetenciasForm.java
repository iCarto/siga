package es.icarto.gvsig.extgia.forms;

import es.icarto.gvsig.extgia.forms.competencias.CompetenciasForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.CommonMethodsForTestDBForms;

public class TestCompetenciasForm extends CommonMethodsForTestDBForms {

    @Override
    protected String getSchema() {
	return DBFieldNames.GIA_SCHEMA;
    }

    @Override
    protected String getTableName() {
	return CompetenciasForm.TABLENAME;
    }

}
