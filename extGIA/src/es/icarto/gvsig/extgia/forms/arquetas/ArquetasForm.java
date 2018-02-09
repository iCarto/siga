package es.icarto.gvsig.extgia.forms.arquetas;

import javax.swing.JTable;
import javax.swing.JTextField;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.preferences.Elements;

@SuppressWarnings("serial")
public class ArquetasForm extends AbstractFormWithLocationWidgets {

	public static final String TABLENAME = "arquetas";

	public ArquetasForm(FLyrVect layer) {
		super(layer);
	}

	@Override
	public Elements getElement() {
		return Elements.Arquetas;
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
