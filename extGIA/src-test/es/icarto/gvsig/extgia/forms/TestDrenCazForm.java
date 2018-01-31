package es.icarto.gvsig.extgia.forms;

import es.icarto.gvsig.extgia.forms.dren_caz.DrenCazForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.CommonMethodsForTestDBForms;

public class TestDrenCazForm extends CommonMethodsForTestDBForms {

	@Override
	protected String getSchema() {
		return DBFieldNames.GIA_SCHEMA;
	}

	@Override
	protected String getTableName() {
		return DrenCazForm.TABLENAME;
	}

}
