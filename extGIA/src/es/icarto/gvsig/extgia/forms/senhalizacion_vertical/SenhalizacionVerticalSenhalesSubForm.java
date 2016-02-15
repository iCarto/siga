package es.icarto.gvsig.extgia.forms.senhalizacion_vertical;

import es.icarto.gvsig.extgia.forms.GIASubForm;

@SuppressWarnings("serial")
public class SenhalizacionVerticalSenhalesSubForm extends GIASubForm {

    public static final String TABLENAME = "senhalizacion_vertical_senhales";

    public SenhalizacionVerticalSenhalesSubForm() {
	super(TABLENAME);
	addChained("codigo_senhal", "tipo_senhal");
	addChained("nombre_senhal", "codigo_senhal");

	SenhalesImageHandler imageHandler = new SenhalesImageHandler(
		"img_senhal", "tipo_senhal", "codigo_senhal", this);
	addImageHandler(imageHandler);
    }
}
