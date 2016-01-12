package es.icarto.gvsig.extgia.consultas;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;
import com.toedter.calendar.JDateChooser;

import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.navtableforms.DependencyHandler;
import es.icarto.gvsig.navtableforms.FillHandler;
import es.icarto.gvsig.navtableforms.IValidatableForm;
import es.icarto.gvsig.navtableforms.chained.ChainedHandler;
import es.icarto.gvsig.navtableforms.ormlite.ORMLite;
import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.ValidatorForm;
import es.icarto.gvsig.navtableforms.utils.AbeilleParser;
import es.udc.cartolab.gvsig.navtable.dataacces.IController;
import es.udc.cartolab.gvsig.navtable.format.DateFormatNT;

@SuppressWarnings("serial")
public abstract class ValidatableForm extends AbstractIWindow implements
	IValidatableForm {

    private final ORMLite ormlite;
    private final FillHandler fillHandler;
    private final DependencyHandler dependencyHandler;
    private final ChainedHandler chainedHandler;
    private final Map<String, JComponent> widgets;
    protected FormPanel formPanel;
    private final IController mockController;
    private boolean fillingValues;

    public ValidatableForm() {
	ormlite = new ORMLite(getClass().getClassLoader()
		.getResource("rules/" + getBasicName() + ".xml").getPath());
	formPanel = getFormPanel();
	this.add(formPanel);
	widgets = AbeilleParser.getWidgetsFromContainer(formPanel);
	dependencyHandler = new DependencyHandler(ormlite, widgets, this);
	mockController = new MockController(widgets);
	fillHandler = new FillHandler(widgets, mockController,
		ormlite.getAppDomain());
	chainedHandler = new ChainedHandler();
	initWidgets();
	fillValues();
	setListeners();
    }

    protected void initWidgets() {
	for (JComponent c : getWidgets().values()) {
	    if (c instanceof JDateChooser) {
		initDateChooser((JDateChooser) c);
	    }
	}
    }

    private void initDateChooser(JDateChooser c) {
	SimpleDateFormat dateFormat = DateFormatNT.getDateFormat();
	c.setDateFormatString(dateFormat.toPattern());
	c.getDateEditor().setEnabled(false);
	c.getDateEditor().getUiComponent()
		.setBackground(new Color(255, 255, 255));
	c.getDateEditor().getUiComponent()
		.setFont(new Font("Arial", Font.PLAIN, 11));
	c.getDateEditor().getUiComponent().setToolTipText(null);
    }

    protected void fillValues() {
	fillingValues = true;
	fillHandler.fillValues();
	dependencyHandler.fillValues();
	chainedHandler.fillValues();
	fillingValues = false;
    }

    protected abstract String getBasicName();

    @Override
    public FormPanel getFormPanel() {
	if (formPanel == null) {
	    InputStream stream = getClass().getClassLoader()
		    .getResourceAsStream("/forms/" + getBasicName() + ".jfrm");
	    try {
		formPanel = new FormPanel(stream);
	    } catch (FormException e) {
		e.printStackTrace();
	    }
	}
	return formPanel;
    }

    @Override
    public boolean isFillingValues() {
	return fillingValues;
    }

    @Override
    public IController getFormController() {
	return mockController;
    }

    @Override
    public void setChangedValues() {
    }

    @Override
    public FillHandler getFillHandler() {
	return fillHandler;
    }

    @Override
    public void validateForm() {
    }

    @Override
    public ValidatorForm getValidatorForm() {
	return null;
    }

    @Override
    public Map<String, JComponent> getWidgets() {
	return widgets;
    }

    @Override
    public void windowClosed() {
	removeListeners();
	super.windowClosed();
    }

    protected void setListeners() {
	dependencyHandler.setListeners();
	chainedHandler.setListeners();
    }

    protected void removeListeners() {
	dependencyHandler.removeListeners();
	chainedHandler.removeListeners();
    }

    protected void addChained(JComponent chained, JComponent parent) {
	chainedHandler.add(this, chained, parent);
    }

    protected void addChained(String chained, String parent) {
	chainedHandler.add(this, widgets.get(chained), widgets.get(parent));
    }

    protected void addChained(JComponent chained, JComponent... parents) {
	chainedHandler.add(this, chained, Arrays.asList(parents));
    }

    protected void addChained(String chained, String... parents) {
	List<JComponent> parentList = new ArrayList<JComponent>();
	for (String parent : parents) {
	    parentList.add(widgets.get(parent));
	}
	chainedHandler.add(this, widgets.get(chained), parentList);
    }

    /**
     * Instead of create an implementation of ImageHandler that only sets a path
     * (FixedImageHandler) this utiliy method sets the image without doing
     * anything more
     * 
     * @param imgComponent
     *            . Name of the abeille widget
     * @param absPath
     *            . Absolute path to the image or relative path from andami.jar
     */
    protected void addImageHandler(String imgComponent, String absPath) {
	ImageComponent image = (ImageComponent) formPanel
		.getComponentByName(imgComponent);
	ImageIcon icon = new ImageIcon(absPath);
	image.setIcon(icon);
    }

    @Override
    protected JButton getDefaultButton() {
	return null;
    }

    @Override
    protected Component getDefaultFocusComponent() {
	return null;
    }

}
