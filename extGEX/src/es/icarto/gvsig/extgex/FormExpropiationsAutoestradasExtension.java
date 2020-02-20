package es.icarto.gvsig.extgex;

import es.icarto.gvsig.extgex.forms.expropiations.FormExpropiations;

public class FormExpropiationsAutoestradasExtension extends FormExpropiationsExtension {

    @Override
    protected String getLayerName() {
        return FormExpropiations.TOCNAME_AUTOESTRADAS;
    }

}
