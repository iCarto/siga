package es.icarto.gvsig.extgia.forms;

import es.icarto.gvsig.extgia.forms.fibra_optica_derivaciones.FibraOpticaDerivacionesForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.CommonMethodsForTestDBForms;

public class TestFibraOpticaDerivacionesForm extends CommonMethodsForTestDBForms {

    @Override
    protected String getSchema() {
	return DBFieldNames.GIA_SCHEMA;
    }

    @Override
    protected String getTableName() {
	return FibraOpticaDerivacionesForm.TABLENAME;
    }

}
