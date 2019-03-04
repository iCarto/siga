package es.icarto.gvsig.extgia.forms.pretiles;

import javax.swing.JTable;
import javax.swing.JTextField;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.preferences.Elements;

@SuppressWarnings("serial")
public class PretilesForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "pretiles";

    public PretilesForm(FLyrVect layer) {
        super(layer);
        addChained("clase_contencion", "nivel_contencion");
        addCalculation(new PretilesCalculateSistemaContencion(this));
    }

    @Override
    public Elements getElement() {
        return Elements.Pretiles;
    }

    @Override
    public String getElementID() {
        return "gid";
    }

    @Override
    public String getElementIDValue() {
        JTextField idWidget = (JTextField) getWidgets().get(getElementID());
        return idWidget.getText();
    }

    @Override
    public JTable getReconocimientosJTable() {
        return null;
    }

    @Override
    public JTable getTrabajosJTable() {
        return null;
    }

    @Override
    public String getBasicName() {
        return TABLENAME;
    }

    @Override
    protected boolean hasSentido() {
        return true;
    }

}