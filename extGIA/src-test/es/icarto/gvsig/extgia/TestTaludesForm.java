package es.icarto.gvsig.extgia;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.jeta.forms.components.panel.FormPanel;

import es.icarto.gvsig.navtableforms.ormlite.ORMLite;
import es.icarto.gvsig.navtableforms.ormlite.domain.DomainValues;
import es.icarto.gvsig.navtableforms.utils.AbeilleParser;
import es.icarto.gvsig.navtableforms.validation.rules.ValidationRule;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class TestTaludesForm {

    private static FormPanel form;
    private HashMap<String, JComponent> widgets;

    @BeforeClass
    public static void doSetupBeforeClass() throws Exception {
	form = new FormPanel(TaludesForm.ABEILLE_FILENAME);
	initializegvSIGDrivers();
	DBSession.createConnection("localhost", 5434, "audasa_test",
		"audasa_extgia_dominios", "postgres", "postgres");
    }

    private static void initializegvSIGDrivers() throws Exception {
	final String fwAndamiDriverPath = "../_fwAndami/gvSIG/extensiones/com.iver.cit.gvsig/drivers";
	final File baseDriversPath = new File(fwAndamiDriverPath);
	if (!baseDriversPath.exists()) {
	    throw new Exception("Can't find drivers path: "
		    + fwAndamiDriverPath);
	}

	LayerFactory.setDriversPath(baseDriversPath.getAbsolutePath());
	if (LayerFactory.getDM().getDriverNames().length < 1) {
	    throw new Exception("Can't find drivers in path: "
		    + fwAndamiDriverPath);
	}
    }

    @Before
    public void doSetup() {
	this.widgets = AbeilleParser.getWidgetsFromContainer(form);
    }

    // TODO: abeille-xml files must be in the classpath, new
    // FormPanel(abeillePath) does't work in other case. To handle this: Run
    // Configuration -> TestTaludesForm -> Classpath -> Advanced -> add
    // extGIA/forms
    // see
    // http://java.net/nonav/projects/abeille/lists/users/archive/2005-06/message/7
    @Test
    public void test_allWidgetsHaveName() {

	for (final JComponent widget : this.widgets.values()) {
	    assertNotNull(widget.getName());
	    assertTrue(widget.getName().trim().length() > 0);
	}
    }

    @Test
    public void test_domainValuesMatchWidgetNames() throws Exception {

	final HashMap<String, DomainValues> domainValues = ORMLite
		.getAplicationDomainObject("forms/audasa.xml")
		.getDomainValues();

	for (final String domainValue : domainValues.keySet()) {
	    assertNotNull(domainValue, this.widgets.get(domainValue));
	}
    }

    @Test
    public void test_validationRulesMatchWidgetNames() throws Exception {

	final HashMap<String, Set<ValidationRule>> validationRules = ORMLite
		.getAplicationDomainObject("forms/audasa.xml")
		.getValidationRules();

	for (final String validationRule : validationRules.keySet()) {
	    assertNotNull(validationRule, this.widgets.get(validationRule));
	}
    }

    @Test
    public void test_widgetsWithOutDatabaseField() throws SQLException {
	final String[] columns = DBSession.getCurrentSession().getColumns(
		"audasa_extgia", "taludes");

	final Set<String> columnsSet = new HashSet<String>(
		Arrays.asList(columns));

	for (final JComponent widget : this.widgets.values()) {

	    if (!(widget instanceof JTable)) {
		assertTrue(widget.getName(),
			columnsSet.contains(widget.getName()));
	    }
	}
    }
    // test_comboboxWidgetHasADomainValue
    // test_comboboxWidgetsWithOutDomain
    // test_widgetWithOutValidationRule

}