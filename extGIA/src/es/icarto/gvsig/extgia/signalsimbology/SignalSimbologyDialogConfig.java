package es.icarto.gvsig.extgia.signalsimbology;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.gui.JComboBoxUnits;
import com.iver.cit.gvsig.gui.styling.JComboBoxUnitsReferenceSystem;

import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.gui.WidgetFactory;

@SuppressWarnings("serial")
public class SignalSimbologyDialogConfig extends AbstractIWindow implements
	ActionListener {

    private static final Logger logger = Logger
	    .getLogger(SignalSimbologyDialogConfig.class);

    private final FLyrVect postes;
    private final FLyrVect signals;
    private final JComboBoxUnits units;
    private final JComboBoxUnitsReferenceSystem rs;
    private final JTextField size;
    private final JTextField sizeCartel;

    public SignalSimbologyDialogConfig(FLyrVect postes, FLyrVect signals) {
	this.postes = postes;
	this.signals = signals;
	this.add(new JLabel("Unidades"));
	units = new JComboBoxUnits();
	this.add(units, "wrap, growx");
	this.add(new JLabel(""));
	rs = new JComboBoxUnitsReferenceSystem();
	this.add(rs, "wrap, growx");
	this.add(new JLabel("Tamaño"));
	size = new JTextField();
	size.setText("20");
	this.add(size, "wrap, growx");
	this.add(new JLabel("Tamaño carteles"));
	sizeCartel = new JTextField();
	sizeCartel.setText("50");
	this.add(sizeCartel, "wrap, growx");
	WidgetFactory.okCancelPanel(this, this, this);
    }

    @Override
    protected JButton getDefaultButton() {
	return null;
    }

    @Override
    protected Component getDefaultFocusComponent() {
	return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals(OkCancelPanel.OK_ACTION_COMMAND)) {
	    ApplySignalSimbology s = new ApplySignalSimbology(postes, signals);
	    s.setUnits(units.getSelectedUnitIndex());
	    s.setUnitReferenceSystem(rs.getUnitsReferenceSystem());
	    try {
		s.setSize(Integer.parseInt(size.getText()));
		s.setCartelSize(Integer.parseInt(sizeCartel.getText()));
		s.applySymbology();
	    } catch (Exception ex) {
		logger.warn("Error parseando objeto");
	    }
	}
	closeDialog();
    }

}
