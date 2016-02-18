package com.iver.cit.gvsig.gui.styling;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.core.CartographicSupport;
import com.iver.utiles.swing.JComboBox;

/**
 * JComboBox to all reference systems
 *
 * @author jaume dominguez faus - jaume.dominguez@iver.es
 */
public class JComboBoxUnitsReferenceSystem extends JComboBox {
    final private String IN_THE_WORLD = PluginServices.getText(this,
	    "in_the_world");
    final private String IN_THE_PAPER = PluginServices.getText(this,
	    "in_the_paper");

    public JComboBoxUnitsReferenceSystem() {
	super();
	addItem(IN_THE_WORLD);
	addItem(IN_THE_PAPER);
    }

    public int getUnitsReferenceSystem() {
	if (this.getSelectedItem().equals(IN_THE_WORLD)) {
	    return CartographicSupport.WORLD;
	} else {
	    return CartographicSupport.PAPER;
	}
    }

}
