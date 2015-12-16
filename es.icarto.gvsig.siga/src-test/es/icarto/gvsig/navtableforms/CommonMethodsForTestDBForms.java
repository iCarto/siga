package es.icarto.gvsig.navtableforms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.icarto.gvsig.commons.testutils.Drivers;
import es.icarto.gvsig.commons.testutils.TestProperties;
import es.icarto.gvsig.navtableforms.ormlite.ORMLite;
import es.icarto.gvsig.navtableforms.ormlite.ORMLiteAppDomain;
import es.icarto.gvsig.navtableforms.ormlite.XMLSAXParser;
import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.rules.ValidationRule;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.DomainValues;
import es.icarto.gvsig.navtableforms.utils.AbeilleParser;
import es.udc.cartolab.gvsig.users.utils.DBSession;
import es.udc.cartolab.gvsig.users.utils.DBSessionPostGIS;

public abstract class CommonMethodsForTestDBForms {

    private ORMLiteAppDomain ado;
    private FormPanel formPanel;
    private HashMap<String, JComponent> widgets;
    IValidatableForm form = null;

    @BeforeClass
    public static void doSetupBeforeClass() {
	Drivers.initgvSIGDrivers(TestProperties.driversPath);
	try {
	    DBSessionPostGIS.createConnection("localhost", 5432, "audasa_test",
		    null, "postgres", "postgres");
	} catch (DBException e) {
	    e.printStackTrace();
	}

    }

    @Before
    public void doSetup() throws FormException {
	ORMLite ormLite = new ORMLite(getMetadataFile());
	ado = ormLite.getAppDomain();

	InputStream file;
	try {
	    file = new FileInputStream("forms/" + getTableName() + ".jfrm");
	} catch (FileNotFoundException e) {
	    try {
		file = new FileInputStream("forms/" + getTableName() + ".xml");
	    } catch (FileNotFoundException e1) {
		throw new RuntimeException("abeille file not found");
	    }
	}

	formPanel = new FormPanel(file);

	widgets = AbeilleParser.getWidgetsFromContainer(formPanel);
    }

    @Test
    public void testXMLIsValid() throws SAXException {
	boolean thrown = false;
	File file = new File(getMetadataFile());
	assertTrue("File not exists: " + getMetadataFile(), file.exists());
	try {
	    new XMLSAXParser(getMetadataFile());
	} catch (ParserConfigurationException e) {
	    thrown = true;
	} catch (SAXException e) {
	    thrown = true;
	} catch (IOException e) {
	    thrown = true;
	}
	assertFalse(thrown);
    }

    @Test
    public void test_allWidgetsHaveName() {

	for (final JComponent widget : this.widgets.values()) {
	    assertNotNull(widget.getName());
	    assertTrue(widget.getName().trim().length() > 0);
	}
    }

    @Test
    public void test_domainValuesMatchComboBoxesNames() throws Exception {

	final HashMap<String, DomainValues> domainValues = ado
		.getDomainValues();

	for (final String domainValue : domainValues.keySet()) {
	    JComponent cb = this.widgets.get(domainValue);
	    if (!(cb instanceof JComboBox)) {
		if (cb.getName().startsWith("pk") && (cb instanceof JTextField)) {
		    continue;
		}
		fail(domainValue);
	    }
	}
	assertTrue(true);
    }

    @Test
    public void test_ComboBoxesNamesMatchDomainValues() {
	final HashMap<String, DomainValues> domainValues = ado
		.getDomainValues();

	for (JComponent cb : widgets.values()) {
	    if (cb instanceof JComboBox) {
		if (domainValues.get(cb.getName()) == null) {
		    fail(cb.getName());
		}
	    }
	}
	assertTrue(true);
    }

    @Test
    public void test_validationRulesMatchWidgetNames() throws Exception {

	final HashMap<String, Set<ValidationRule>> validationRules = new HashMap<String, Set<ValidationRule>>();
	for (String key : ado.getDomainValidators().keySet()) {
	    validationRules.put(key, ado.getDomainValidators().get(key)
		    .getRules());
	}
	for (final String validationRule : validationRules.keySet()) {
	    assertNotNull(validationRule, this.widgets.get(validationRule));
	}
    }

    @Test
    public void test_widgetsWithOutDatabaseField() throws SQLException {
	final Set<String> columnsSet = getColums();

	for (final JComponent widget : this.widgets.values()) {

	    if (!(widget instanceof JTable)) {
		assertTrue(widget.getName(),
			columnsSet.contains(widget.getName()));
	    }
	}
    }

    @Test
    public void tableNameIsInProperties() throws IOException {
	InputStream input = new FileInputStream("config/text_es.properties");
	Properties props = new Properties();
	props.load(input);
	assertNotNull(props.getProperty(getTableName()));
    }

    private Set<String> getColums() throws SQLException {
	final String[] columns = DBSession.getCurrentSession().getColumns(
		getSchema(), getTableName());

	final Set<String> columnsSet = new HashSet<String>(
		Arrays.asList(columns));
	return columnsSet;
    }

    protected String getMetadataFile() {
	return "rules/" + getTableName() + ".xml";
    }

    protected abstract String getSchema();

    protected abstract String getTableName();

}