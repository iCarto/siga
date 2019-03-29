package es.icarto.gvsig.siga.locatorbycoords;

import static es.icarto.gvsig.navtableforms.ormlite.domainvalidator.ValidatorComponent.INVALID_COLOR;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.commons.gui.IWindowClosed;
import es.icarto.gvsig.commons.gui.PlaceholderTextField;
import es.udc.cartolab.gvsig.elle.constants.ZoomTo;

@SuppressWarnings("serial")
public class LocatorByCoordsDialog extends AbstractIWindow implements
DocumentListener {

    private final static int WIDGET_SIZE = 23;
    private final LocatorByCoordsModel model;

    private JComboBox inputProj;
    private JTextField inputX;
    private JTextField inputY;

    private JComboBox outputProj;
    private JTextField outputY;
    private JTextField outputX;

    private JButton zoomBt;

    public LocatorByCoordsDialog(LocatorByCoordsModel locatorByCoordsModel) {
	super();
	this.model = locatorByCoordsModel;
	setupUI();
	setWindowTitle("Localizar Coordenadas");
	setWindowInfoProperties(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
	setWindowClosed(new IWindowClosed() {
	    @Override
	    public void windowClosed(IWindow window) {
		model.getZoomTo().clearAllGraphics();;
	    }
	});
    }

    private void setupUI() {
	// setupMagicField();
	setupInput();
	setupOuput();
	setupZoomBt();
    }

    private void setupMagicField() {
	JTextField input = new PlaceholderTextField(
		"Pegue o escriba la localización");
	this.add(input, "wrap, span, growx, pushx");
    }

    private void setupInput() {
	inputX = new PlaceholderTextField("utm X (548076,36) o Long (-8,4062)");
	// 548202,058 - 23029
	inputX.setColumns(WIDGET_SIZE);
	inputX.getDocument().addDocumentListener(this);
	this.add(inputX, "growx");

	inputY = new PlaceholderTextField("utm Y (4803838,61) o Lat (43,386)");
	// 4804052,835 - 23029
	inputY.setColumns(WIDGET_SIZE);
	inputY.getDocument().addDocumentListener(this);
	this.add(inputY, "growx");

	inputProj = new JComboBox();
	this.add(inputProj, "wrap, growx");

	inputProj.setModel(new DefaultComboBoxModel(model.getProjCodes()
		.toArray(new CoordProvider[0])));
	inputProj.setPrototypeDisplayValue("EPSG:XXXXXXXXX");
	inputProj.setSelectedItem(model.getDefaultInputProj());
	inputProj.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		valueChanged();
	    }
	});

    }

    private void setupOuput() {

	outputX = new JTextField();
	outputX.setColumns(WIDGET_SIZE);
	outputX.setEditable(false);
	this.add(outputX, "growx");

	outputY = new JTextField();
	outputY.setColumns(WIDGET_SIZE);
	outputY.setEditable(false);
	this.add(outputY, "growx");

	outputProj = new JComboBox();
	this.add(outputProj, "wrap, growx");

	outputProj.setModel(new DefaultComboBoxModel(model.getProjCodes()
		.toArray(new CoordProvider[0])));
	outputProj.setPrototypeDisplayValue("EPSG:XXXXXXX");
	outputProj.setSelectedItem(model.getDefaultOuputProj());
	outputProj.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		valueChanged();
	    }
	});
    }

    private void setupZoomBt() {
	zoomBt = new JButton("Zoom");
	zoomBt.setEnabled(false);
	zoomBt.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		ZoomTo zoom = model.getZoomTo();
		CoordProvider iProv = (CoordProvider) inputProj
			.getSelectedItem();
		zoom.zoom(iProv.toGPoint(inputX.getText(), inputY.getText()),
			true);
	    }
	});
	this.add(zoomBt, "cell 2 3, growx");
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
	valueChanged();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
	valueChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
	// nothing to do here
    }

    private void valueChanged() {
	CoordProvider iProv = (CoordProvider) inputProj.getSelectedItem();

	final boolean validX = iProv.validX(inputX.getText());
	final boolean validY = iProv.validY(inputY.getText());

	if (validX) {
	    inputX.setBackground(UIManager.getColor("TextField.background"));
	} else {
	    inputX.setBackground(INVALID_COLOR);
	}

	if (validY) {
	    inputY.setBackground(UIManager.getColor("TextField.background"));
	} else {
	    inputY.setBackground(INVALID_COLOR);
	}

	if (validX && validY) {
	    String[] o = iProv.transform(inputX.getText(), inputY.getText(),
		    outputProj.getSelectedItem());
	    outputX.setText(o[0]);
	    outputY.setText(o[1]);
	    zoomBt.setEnabled(true);
	} else {
	    zoomBt.setEnabled(false);
	    outputX.setText("");
	    outputY.setText("");
	}
    }

    @Override
    protected JButton getDefaultButton() {
	return zoomBt;
    }

    @Override
    protected Component getDefaultFocusComponent() {
	return zoomBt;
    }

}
