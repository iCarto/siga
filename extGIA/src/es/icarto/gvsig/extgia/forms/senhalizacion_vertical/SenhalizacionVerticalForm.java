package es.icarto.gvsig.extgia.forms.senhalizacion_vertical;

import java.net.URL;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.forms.GIAAlphanumericTableHandler;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.siga.forms.reports.NavTableComponentsPrintButton;

@SuppressWarnings("serial")
public class SenhalizacionVerticalForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "senhalizacion_vertical";

    JTextField elementoSenhalizacionIDWidget;
    CalculateComponentValue elementoSenhalizacionid;

    private JButton printReportB;

    private URL reportPath;

    private PrintSVReportObserver printListener;

    public static String[] senhalesColNames = { "id_senhal_vertical",
	    "tipo_senhal", "codigo_senhal", "leyenda", "fecha_instalacion",
	    "fecha_reposicion", "codigo_senhal" };

    public static String[] senhalesColAlias = { "ID Señal", "Tipo Señal",
	    "Código Señal", "Leyenda", "Instalación", "Reposición", "Icono" };

    public SenhalizacionVerticalForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosVegetacionColNames,
		DBFieldNames.trabajosVegetacionColAlias,
		DBFieldNames.trabajosColWidths, this));

	addTableHandler(new SenhalesTableHandler(
		"senhalizacion_vertical_senhales", getWidgets(),
		getElementID(), senhalesColNames, senhalesColAlias, new int[] {
			20, 55, 45, 135, 45, 40, 30 }, this,
		    SenhalizacionVerticalSenhalesSubForm.class));
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();

	if (elementoSenhalizacionIDWidget.getText().isEmpty()) {
	    elementoSenhalizacionid = new SenhalizacionVerticalCalculateIDValue(
		    this, getWidgetComponents(),
		    DBFieldNames.ID_ELEMENTO_SENHALIZACION,
		    DBFieldNames.ID_ELEMENTO_SENHALIZACION);
	    elementoSenhalizacionid.setValue(true);
	}

	addNewButtonsToActionsToolBar();
    }

    private void addNewButtonsToActionsToolBar() {
	if (printReportB == null) {
	    addPrintButton();
	}
	printReportB.removeActionListener(printListener);

	printListener = new PrintSVReportObserver(this, reportPath.getPath(),
		getElementID(), getPrimaryKeyValue());
	printReportB.addActionListener(printListener);
    }

    public ImageIcon getPrintIcon() {
	java.net.URL imgURL = this.getClass().getClassLoader()
		.getResource("images/print-report.png");
	ImageIcon icon = new ImageIcon(imgURL);
	return icon;
    }

    private void addPrintButton() {
	reportPath = this.getClass().getClassLoader()
		.getResource("reports/senhalizacion_vertical.jasper");
	JPanel actionsToolBar = this.getActionsToolBar();

	NavTableComponentsPrintButton ntPrintButton = new NavTableComponentsPrintButton();

	printListener = new PrintSVReportObserver(this, reportPath.getPath(),
		getElementID(), getPrimaryKeyValue());
	printReportB = ntPrintButton.createButton(
		PluginServices.getText(this, "printReportsToolTip"),
		getPrintIcon(), printListener);

	printReportB.setName("printButton");
	actionsToolBar.add(printReportB);
    }

    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();

	elementoSenhalizacionIDWidget = (JTextField) widgets
		.get(DBFieldNames.ID_ELEMENTO_SENHALIZACION);
    }

    @Override
    public JTable getReconocimientosJTable() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public JTable getTrabajosJTable() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getBasicName() {
	return TABLENAME;
    }

    @Override
    public Elements getElement() {
	return Elements.Senhalizacion_Vertical;
    }

    @Override
    protected boolean hasSentido() {
	return true;
    }

    @Override
    public String getElementID() {
	return "id_elemento_senhalizacion";
    }

    @Override
    public String getElementIDValue() {
	return elementoSenhalizacionIDWidget.getText();
    }
}