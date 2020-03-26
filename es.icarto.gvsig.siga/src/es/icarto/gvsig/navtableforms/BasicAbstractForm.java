package es.icarto.gvsig.navtableforms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.RowSorter.SortKey;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.queries.Utils;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.siga.PreferencesPage;
import es.udc.cartolab.gvsig.navtable.contextualmenu.ChooseSortFieldDialog;

@SuppressWarnings("serial")
public abstract class BasicAbstractForm extends AbstractForm {

    private static final Logger logger = Logger
	    .getLogger(BasicAbstractForm.class);

    public BasicAbstractForm(FLyrVect layer) {
	super(layer);
	addImageHandler("image", PreferencesPage.SIGA_LOGO);
	addSorterButton();
	setTitle(PluginServices.getText(this, getBasicName()));
    }

    protected void addSorterButton() {
	java.net.URL imgURL = getClass().getClassLoader().getResource(
		"sort.png");
	JButton jButton = new JButton(new ImageIcon(imgURL));
	jButton.setToolTipText("Ordenar registros");

	jButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		URL resource = BasicAbstractForm.this.getClass()
			.getClassLoader().getResource("columns.properties");
		List<Field> fields = Utils.getFields(resource.getPath(),
			getSchema(), getBasicName());

		ChooseSortFieldDialog dialog = new ChooseSortFieldDialog(fields);

		if (dialog.open().equals(OkCancelPanel.OK_ACTION_COMMAND)) {
		    List<Field> sortedFields = dialog.getFields();
		    List<SortKey> sortKeys = new ArrayList<SortKey>();
		    SelectableDataSource sds = getRecordset();
		    for (Field field : sortedFields) {
			try {
			    int fieldIdx = sds.getFieldIndexByName(field
				    .getKey());
			    sortKeys.add(new SortKey(fieldIdx, field
				    .getSortOrder()));
			} catch (ReadDriverException e1) {
			    logger.error(e1.getStackTrace(), e1);
			}
		    }
		    setSortKeys(sortKeys);
		}
	    }
	});
	getActionsToolBar().add(jButton);
    }

    @Override
    public FormPanel getFormBody() {
	if (formBody == null) {
	    InputStream stream = getClass().getClassLoader()
		    .getResourceAsStream("/forms/" + getBasicName() + ".jfrm");
	    if (stream == null) {
		stream = getClass().getClassLoader().getResourceAsStream(
			"/forms/" + getBasicName() + ".xml");
	    }
	    try {
		formBody = new FormPanel(stream);
	    } catch (FormException e) {
		e.printStackTrace();
	    }
	}
	return formBody;
    }

    @Override
    public String getXMLPath() {
	return this.getClass().getClassLoader()
		.getResource("rules/" + getBasicName() + ".xml").getPath();
    }

    protected abstract String getBasicName();

    protected abstract String getSchema();

}
