package es.icarto.gvsig.extgia.signalsimbology;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.CartographicSupport;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.gui.WidgetFactory;

@SuppressWarnings("serial")
public class SignalSimbologyDialog extends AbstractIWindow implements
ActionListener {

    private final FLyrVect postes;
    private final FLyrVect signals;
    private final JComboBox combo;
    private final JButton okBt;

    public SignalSimbologyDialog(FLyrVect postes, FLyrVect signals) {
	this.postes = postes;
	this.signals = signals;
	this.add(new JLabel("   Tamaño:     "));
	combo = new JComboBox();
	combo.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXX");
	combo.addItem("Pequeño");
	combo.addItem("Mediano");
	combo.addItem("Grande");
	combo.setSelectedIndex(1);
	this.add(combo, "wrap, growx");
	okBt = WidgetFactory.okCancelPanel(this, this, this).getOkButton();
    }

    @Override
    protected JButton getDefaultButton() {
	return okBt;
    }

    @Override
    protected Component getDefaultFocusComponent() {
	return combo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals(OkCancelPanel.OK_ACTION_COMMAND)) {
	    try {
		PluginServices.getMDIManager().setWaitCursor();
		ApplySignalSimbology s = new ApplySignalSimbology(postes,
			signals);
		String sizeStr = combo.getSelectedItem().toString();
		if (sizeStr.equals("Pequeño")) {
		    s.setCartelSize(3000);
		    s.setSize(1000);
		    s.setHitoSize(2000);
		} else if (sizeStr.equals("Mediano")) {
		    s.setCartelSize(4500);
		    s.setSize(1000);
		    s.setHitoSize(3000);
		} else {
		    s.setCartelSize(6000);
		    s.setSize(2000);
		    s.setHitoSize(4000);
		}
		String[] distanceNames = MapContext.getDistanceNames();
		for (int i = 0; i < distanceNames.length; i++) {
		    if (distanceNames[i].equals("Centimetros")) {
			s.setUnits(i);
			break;
		    }
		}
		s.setUnitReferenceSystem(CartographicSupport.WORLD);
		s.applySymbology();
	    } catch (Exception ex) {
		ex.printStackTrace();
	    } finally {
		PluginServices.getMDIManager().restoreCursor();
	    }

	}
	closeDialog();
    }
}
