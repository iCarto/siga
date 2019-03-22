package es.icarto.gvsig.extgex.locators.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgex.forms.expropiations.FormExpropiations;
import es.udc.cartolab.gvsig.elle.constants.IPositionRetriever;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;

public class FormOpener implements ActionListener {

    private final IPositionRetriever retriever;

    public FormOpener(IPositionRetriever retriever) {
	this.retriever = retriever;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
	FLyrVect layer = retriever.getLayer();
	// if(AlphanumericTableLoader.loadTables() &&
	// (layer != null)) {
	if (layer != null) {
            int position = retriever.getPosition();
	    FormExpropiations form = new FormExpropiations(layer, null);
            if (position != AbstractNavTable.EMPTY_REGISTER && form.init()) {
                form.setPosition(position);
		PluginServices.getMDIManager().addWindow(form);
	    }
	} else {
	    JOptionPane.showMessageDialog(null, PluginServices.getText(this,
		    "alphanumeric_table_no_loaded"));
	}
    }

}
