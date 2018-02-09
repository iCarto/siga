package es.icarto.gvsig.extgia.forms;

import es.icarto.gvsig.extgia.forms.cunetas.CunetasForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.CommonMethodsForTestDBForms;

public class TestCunetasForm extends CommonMethodsForTestDBForms {

	@Override
	protected String getSchema() {
		return DBFieldNames.GIA_SCHEMA;
	}

	@Override
	protected String getTableName() {
		return CunetasForm.TABLENAME;
	}

}
