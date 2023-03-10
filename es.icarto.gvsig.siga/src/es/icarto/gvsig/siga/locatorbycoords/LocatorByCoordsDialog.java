package es.icarto.gvsig.siga.locatorbycoords;

import static es.icarto.gvsig.navtableforms.ormlite.domainvalidator.ValidatorComponent.INVALID_COLOR;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
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

    private final static int UTM_WIDGET_SIZE = 26;
    private final static int WGS84_WIDGET_SIZE = 17;
    private final LocatorByCoordsModel model;

    private JComboBox inputProj;
    private JTextField inputX;
    private JTextField inputY;
    
    private JTextField inputLatDegrees;
    private JTextField inputLatMinutes;
    private JTextField inputLatSeconds;
    private JRadioButton selectLatN;
    private JRadioButton selectLatS;
    
    private JTextField inputLonDegrees;
    private JTextField inputLonMinutes;
    private JTextField inputLonSeconds;
    private JRadioButton selectLonE;
    private JRadioButton selectLonO;
    
    private JComboBox outputProj;
    private JTextField outputY;
    private JTextField outputX;

    private JButton zoomBt;

    public LocatorByCoordsDialog(LocatorByCoordsModel locatorByCoordsModel) {
	super();
	this.model = locatorByCoordsModel;
	setupWGS84DecimalUI(null);
	setWindowTitle("Localizar Coordenadas");
	setWindowInfoProperties(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE | WindowInfo.RESIZABLE);
	setWindowClosed(new IWindowClosed() {
	    @Override
	    public void windowClosed(IWindow window) {
		model.getZoomTo().clearAllGraphics();;
	    }
	});
    }
    
    private boolean isCBValueWGS84(CoordProvider iProv) {
    if (iProv.toString().equalsIgnoreCase("WGS84")) {
        return true;
    }else {
        return false;
    }
    }
    
    private boolean isCBValueWGS84DMS(CoordProvider iProv) {
    if (iProv.toString().equalsIgnoreCase("WGS84 (GMS)")) {
        return true;
    }else {
        return false;
    }
    }

    private void setupWGS84DecimalUI(CoordProvider iProv) {
	paintDecimalInput(iProv);
	paintOutput("span 3, growx");
	paintZoomBt("cell 6 3, growx");
    }
    
    private void setupWGS84DmsUI(CoordProvider iProv) {
    paintInputDMS(iProv);
    paintOutput("span 5, growx");
    paintZoomBt("cell 10 3, growx");
    }
    
    private void setupUtmUI(CoordProvider iProv) {
    paintUtmInput(iProv);
    paintOutput("span 1, growx");
    paintZoomBt("cell 2 3, growx");
    }

    private void paintUtmInput(CoordProvider iProv) {
    inputX = new PlaceholderTextField("utm X (548076,36)");
    // 548202,058 - 23029
    inputX.setColumns(UTM_WIDGET_SIZE);
    inputX.getDocument().addDocumentListener(this);
    this.add(inputX, "growx");

    inputY = new PlaceholderTextField("utm Y (4803838,61)");
    // 4804052,835 - 23029
    inputY.setColumns(UTM_WIDGET_SIZE);
    inputY.getDocument().addDocumentListener(this);
    this.add(inputY, "growx");

    paintInputCB(iProv);
    }
    
    private void paintDecimalInput(CoordProvider iProv) {
	inputX = new PlaceholderTextField("Long (8,4062)");
	// 548202,058 - 23029
	inputX.setColumns(WGS84_WIDGET_SIZE);
	inputX.getDocument().addDocumentListener(this);
	this.add(inputX, "growx");
	
	paintLonSelect();

	inputY = new PlaceholderTextField("Lat (43,386)");
	// 4804052,835 - 23029
	inputY.setColumns(WGS84_WIDGET_SIZE);
	inputY.getDocument().addDocumentListener(this);
	this.add(inputY, "growx");
	
	paintLatSelect();

	paintInputCB(iProv);

    }
    
    private void paintInputDMS(CoordProvider iProv) {
    
    inputLonDegrees = new PlaceholderTextField("grados");
    inputLonDegrees.setColumns(6);
    inputLonDegrees.getDocument().addDocumentListener(this);
    this.add(inputLonDegrees, "growx");
    
    inputLonMinutes = new PlaceholderTextField("minutos");
    inputLonMinutes.setColumns(6);
    inputLonMinutes.getDocument().addDocumentListener(this);
    this.add(inputLonMinutes, "growx");
    
    inputLonSeconds = new PlaceholderTextField("segundos");
    inputLonSeconds.setColumns(8);
    inputLonSeconds.getDocument().addDocumentListener(this);
    this.add(inputLonSeconds, "growx");
       
    paintLonSelect();
    
    inputLatDegrees = new PlaceholderTextField("grados");
    inputLatDegrees.setColumns(6);
    inputLatDegrees.getDocument().addDocumentListener(this);
    this.add(inputLatDegrees, "growx");
    
    inputLatMinutes = new PlaceholderTextField("minutos");
    inputLatMinutes.setColumns(6);
    inputLatMinutes.getDocument().addDocumentListener(this);
    this.add(inputLatMinutes, "growx");
    
    inputLatSeconds = new PlaceholderTextField("segundos");
    inputLatSeconds.setColumns(8);
    inputLatSeconds.getDocument().addDocumentListener(this);
    this.add(inputLatSeconds, "growx");
    
    paintLatSelect();
    
    paintInputCB(iProv);
    }

    private void paintLonSelect() {
    selectLonE = new JRadioButton("E", false);
    selectLonO = new JRadioButton("O", true);
    selectLonE.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        valueChanged();
        }
    });
    selectLonO.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        valueChanged();
        }
    });
    
    ButtonGroup lonGroup = new ButtonGroup();
    lonGroup.add(selectLonE);
    lonGroup.add(selectLonO);
    this.add(selectLonE, "");
    this.add(selectLonO, "");
    }

    private void paintLatSelect() {
    selectLatN = new JRadioButton("N", true);
    selectLatS = new JRadioButton("S", false);
    selectLatN.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        valueChanged();
        }
    });
    selectLatS.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        valueChanged();
        }
    });
    
    ButtonGroup latGroup = new ButtonGroup();
    latGroup.add(selectLatN);
    latGroup.add(selectLatS);
    this.add(selectLatN, "grow x");
    this.add(selectLatS, "grow x"); 
    }
    
    private void paintInputCB(CoordProvider code) {
    inputProj = new JComboBox();
    this.add(inputProj, "wrap, growx");

    inputProj.setModel(new DefaultComboBoxModel(model.getProjCodes()
        .toArray(new CoordProvider[0])));
    inputProj.setPrototypeDisplayValue("EPSG:XXXXXXXXX");
    if (code == null) {
        inputProj.setSelectedItem(model.getDefaultInputProj());
    }else {
        inputProj.setSelectedItem(code);
    }
    inputProj.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        valueInputCBChanged();
        }
    });
    }

    private void paintOutput(String cellSpan) {

	outputX = new JTextField();
	outputX.setEditable(false);
	this.add(outputX, cellSpan);

	outputY = new JTextField();
	outputY.setEditable(false);
	this.add(outputY, cellSpan);
	
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

    private void paintZoomBt(String cellSpan) {
	zoomBt = new JButton("Zoom");
	zoomBt.setEnabled(false);
	zoomBt.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		ZoomTo zoom = model.getZoomTo();
		CoordProvider iProv = (CoordProvider) inputProj
			.getSelectedItem();
		if (isCBValueWGS84DMS(iProv)){
		    String[] coords = transformWGS84DMSCoords(iProv);
		    CoordProvider iProvOut = (CoordProvider) outputProj
		            .getSelectedItem();
		    zoom.zoom(iProvOut.toGPoint(coords[0], coords[1]),
                    true);
		}else if (isCBValueWGS84(iProv)) {
		    String[] coords = transformWGS84DecimalCoords(iProv);
            CoordProvider iProvOut = (CoordProvider) outputProj
                    .getSelectedItem();
            zoom.zoom(iProvOut.toGPoint(coords[0], coords[1]),
                    true);
		}else {
		    zoom.zoom(iProv.toGPoint(inputX.getText(), inputY.getText()),
		            true);
	    }
	    }
	});
	this.add(zoomBt, cellSpan);
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
	CoordProvider iProvInput = (CoordProvider) inputProj.getSelectedItem();
	CoordProvider iProvOutput = (CoordProvider) outputProj.getSelectedItem();
	String[] coords = null;
	        
	if (isCBValueWGS84DMS(iProvInput)) {
	    coords = transformWGS84DMSCoords(iProvInput);

	}else if (isCBValueWGS84(iProvInput)) {
	    coords = transformWGS84DecimalCoords(iProvInput);
	}else {
	    coords = transformDecimalCoords(iProvInput);
	}
	    
	if (coords != null) {
	    setOutPutAsFormat(iProvOutput, coords);
	    zoomBt.setEnabled(true);
	} else {
	    zoomBt.setEnabled(false);
	    outputX.setText("");
	    outputY.setText("");
	}

    }
    
    private void valueInputCBChanged() {
    
    CoordProvider iProv = (CoordProvider) inputProj.getSelectedItem();
    
    if (isCBValueWGS84DMS(iProv)) {
        this.removeAll();
        setupWGS84DmsUI(iProv);
    }else if (isCBValueWGS84(iProv)){
        this.removeAll();
        setupWGS84DecimalUI(iProv);
    }else {
        this.removeAll();
        setupUtmUI(iProv);
    }
    
    }

    private void setOutPutAsFormat(CoordProvider iProv, String[] coords) {
    if (isCBValueWGS84DMS(iProv)) {
        outputX.setText(iProv.toWGS84DMS(coords[0], "lon"));
        outputY.setText(iProv.toWGS84DMS(coords[1], "lat"));
    }else if (isCBValueWGS84(iProv)) {
        outputX.setText(iProv.toWGS84(coords[0], "lon"));
        outputY.setText(iProv.toWGS84(coords[1], "lat"));
    }else {
        outputX.setText(coords[0]);
        outputY.setText(coords[1]);
    }
    
    }
    
    private String[] transformWGS84DMSCoords(CoordProvider iProvInput) {
    final boolean validLonDegrees = iProvInput.validDMSValue(inputLonDegrees.getText());
    final boolean validLonMinutes = iProvInput.validDMSValue(inputLonMinutes.getText());
    final boolean validLonSeconds = iProvInput.validDMSValue(inputLonSeconds.getText());
    
    final boolean validLatDegrees = iProvInput.validDMSValue(inputLatDegrees.getText());
    final boolean validLatMinutes = iProvInput.validDMSValue(inputLatMinutes.getText());
    final boolean validLatSeconds = iProvInput.validDMSValue(inputLatSeconds.getText());
    
    if (validLonDegrees) {
        inputLonDegrees.setBackground(UIManager.getColor("TextField.background"));
    } else {
        inputLonDegrees.setBackground(INVALID_COLOR);
    }
    if (validLonMinutes) {
        inputLonMinutes.setBackground(UIManager.getColor("TextField.background"));
    } else {
        inputLonMinutes.setBackground(INVALID_COLOR);
    }
    if (validLonSeconds) {
        inputLonSeconds.setBackground(UIManager.getColor("TextField.background"));
    } else {
        inputLonSeconds.setBackground(INVALID_COLOR);
    }
    
    if (validLatDegrees) {
        inputLatDegrees.setBackground(UIManager.getColor("TextField.background"));
    } else {
        inputLatDegrees.setBackground(INVALID_COLOR);
    }
    if (validLatMinutes) {
        inputLatMinutes.setBackground(UIManager.getColor("TextField.background"));
    } else {
        inputLatMinutes.setBackground(INVALID_COLOR);
    }
    if (validLatSeconds) {
        inputLatSeconds.setBackground(UIManager.getColor("TextField.background"));
    } else {
        inputLatSeconds.setBackground(INVALID_COLOR);
    }
    
    String x = null;
    if (validLonDegrees && validLonMinutes && validLonSeconds) {
    String lonZone = (selectLonE.isSelected()?"E":"O");
    x = iProvInput.dmsToDecimalDegrees(inputLonDegrees.getText(), 
            inputLonMinutes.getText(), inputLonSeconds.getText(), lonZone);
    }
    
    String y = null;
    if (validLatDegrees && validLatMinutes && validLatSeconds) {
    String latZone = (selectLatN.isSelected()?"N":"S");
    y = iProvInput.dmsToDecimalDegrees(inputLatDegrees.getText(), 
            inputLatMinutes.getText(), inputLatSeconds.getText(), latZone);
    }
  
    if (x == null || y == null) {
        return null;
    }else {
        final boolean validX = iProvInput.validX(x);
        final boolean validY = iProvInput.validY(y);
    
        if (validX && validY) {
            return iProvInput.transform(x, y,
                    outputProj.getSelectedItem());
        }else {
            return null;
        }
    }
    }
    
    private String[] transformWGS84DecimalCoords(CoordProvider iProvInput) {
    final boolean validX = iProvInput.validX(inputX.getText()) && iProvInput.numberIsPositive(inputX.getText());
    final boolean validY = iProvInput.validY(inputY.getText()) && iProvInput.numberIsPositive(inputY.getText());

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
    
    String lonZone = (selectLonE.isSelected()?"E":"O");
    String latZone = (selectLatN.isSelected()?"N":"S");
    
    String x = inputX.getText();
    if (lonZone.equalsIgnoreCase("O")) {
        x = "-" + inputX.getText();
    }
    
    String y = inputY.getText();
    if (latZone.equalsIgnoreCase("S")) {
        x = "-" + inputY.getText();
    }
    
    if (validX && validY) {
        return iProvInput.transform(x, y,
                outputProj.getSelectedItem());
    }else {
        return null;
    }
    
    }

    private String[] transformDecimalCoords(CoordProvider iProvInput) {
    final boolean validX = iProvInput.validX(inputX.getText());
	final boolean validY = iProvInput.validY(inputY.getText());

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
	    return iProvInput.transform(inputX.getText(), inputY.getText(),
		    outputProj.getSelectedItem());
	} else {
	    return null;
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
    
    @Override
    public Object getWindowProfile() {
        return WindowInfo.EXTERNAL_PROFILE;
    }


}
