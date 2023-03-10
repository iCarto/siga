package es.icarto.gvsig.extgia.forms.barrera_metalica;

import javax.swing.JTable;
import javax.swing.JTextField;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.pretiles.PretilesCalculateSistemaContencion;
import es.icarto.gvsig.extgia.preferences.Elements;

@SuppressWarnings("serial")
public class BarreraMetalicaForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "barrera_metalica";

    public BarreraMetalicaForm(FLyrVect layer) {
        super(layer);
        addChained("clase_contencion", "nivel_contencion");
        addCalculation(new PretilesCalculateSistemaContencion(this));
    }

    @Override
    public Elements getElement() {
        return Elements.Barrera_Metalica;
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