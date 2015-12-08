package es.icarto.gvsig.siga.gotoextension;

import static es.icarto.gvsig.navtableforms.ormlite.domainvalidator.ValidatorComponent.INVALID_COLOR;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.commons.gui.IWindowClosed;
import es.icarto.gvsig.commons.gui.PlaceholderTextField;
import es.udc.cartolab.gvsig.elle.constants.ZoomTo;

@SuppressWarnings("serial")
public class GoToDialog extends AbstractIWindow implements DocumentListener {

    private static final Logger logger = Logger.getLogger(GoToDialog.class);

    private final static int WIDGET_SIZE = 21;
    private final GoToModel model;

    private JComboBox inputProj;
    private JTextField inputX;
    private JTextField inputY;

    private JComboBox outputProj;
    private JTextField outputY;
    private JTextField outputX;

    private JButton zoomBt;

    public GoToDialog(GoToModel goToModel) {
	super();
	this.model = goToModel;
	setupUI();
	setWindowTitle("Zoom");
	setWindowInfoProperties(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
	setWindowClosed(new IWindowClosed() {
	    @Override
	    public void windowClosed(IWindow window) {
		model.getZoomTo().resetPoint();
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
		"Pegue o escriba la localizaci�n");
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
	this.add(inputProj, "wrap");

	inputProj.setModel(new DefaultComboBoxModel(model.getProjCodes()));
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
	this.add(outputProj, "wrap");

	outputProj.setModel(new DefaultComboBoxModel(model.getProjCodes()));
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
	zoomBt.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		ZoomTo zoom = model.getZoomTo();
		CoordProvider iProv = (CoordProvider) outputProj
			.getSelectedItem();
		zoom.zoom(iProv.toGPoint(outputX.getText(), outputY.getText()),
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
    public void openDialog() {
	if (getWindowInfo().isModeless()) {
	    PluginServices.getMDIManager().addCentredWindow(this);
	    setFocusCycleRoot(true);
	    zoomBt.requestFocusInWindow();
	} else {
	    // Si la ventana es modal el c�digo se queda bloqueado tras a�adir
	    // la ventana hasta que el usuario la cierra, y antes de que la
	    // ventana sea a�adida el JRootPane no es creado, por lo que con
	    // ventanas modales no se puede user un default button. A no ser que
	    // se haga algo un poco distinto
	    PluginServices.getMDIManager().addCentredWindow(this);
	}
    }

}
