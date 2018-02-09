package es.icarto.gvsig.extgia.forms.cunetas;

import javax.swing.JTable;
import javax.swing.JTextField;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.preferences.Elements;

@SuppressWarnings("serial")
public class CunetasForm extends AbstractFormWithLocationWidgets {
	
	public static final String TABLENAME = "cunetas";

	public CunetasForm(FLyrVect layer) {
		super(layer);
	}

	@Override
	public Elements getElement() {
		return Elements.Cunetas;
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
