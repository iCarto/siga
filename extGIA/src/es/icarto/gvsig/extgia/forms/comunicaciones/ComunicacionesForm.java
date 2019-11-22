package es.icarto.gvsig.extgia.forms.comunicaciones;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter.SortKey;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.queries.Utils;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.FilesLinkDataImp;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.gui.buttons.fileslink.FilesLinkButton;
import es.udc.cartolab.gvsig.navtable.contextualmenu.ChooseSortFieldDialog;

@SuppressWarnings("serial")
public class ComunicacionesForm extends AbstractFormWithLocationWidgets {

    private static final Logger logger = Logger
	    .getLogger(ComunicacionesForm.class);

    public static final String TABLENAME = "comunicaciones";

    public ComunicacionesForm(FLyrVect layer) {
	super(layer);
    }

    @Override
    public Elements getElement() {
	return Elements.Comunicaciones;
    }

    @Override
    public String getElementID() {
	return "gid";
    }

    @Override
    public String getElementIDValue() {
	JTextField idWidget = (JTextField) getWidgets().get(getElementID());
	return idWidget.getText();
    }

    @Override
    public JTable getReconocimientosJTable() {
	return null;
    }

    @Override
    public JTable getTrabajosJTable() {
	return null;
    }

    @Override
    public String getBasicName() {
	return TABLENAME;
    }

    @Override
    protected boolean hasSentido() {
	return false;
    }

    private final class FilesLinkDataImpCom extends FilesLinkDataImp {

	public FilesLinkDataImpCom(Elements element) {
	    super(element, infoEmpresa.getCompany(tramoCB.getSelectedItem()));
	}

	@Override
	public String getFolder(AbstractForm form) {
	    return getBaseDirectory();
	};
    }

    @Override
    protected void addNewButtonsToActionsToolBar(final Elements element) {
	JPanel actionsToolBar = this.getActionsToolBar();

	filesLinkButton = new FilesLinkButton(this, new FilesLinkDataImpCom(
		getElement()));
	actionsToolBar.add(filesLinkButton);
    }

    @Override
    // Está aquí porque gid en este elemento es un identificador válido del
    // elemento y no sólo una pk interna. Pero por defecto en ordenar eliminamos
    // ese campo de los posibles
    protected void addSorterButton() {
	final List<String> reserved = Arrays.asList(new String[] { "the_geom",
		"geom" });
	java.net.URL imgURL = getClass().getClassLoader().getResource(
		"sort.png");
	JButton jButton = new JButton(new ImageIcon(imgURL));
	jButton.setToolTipText("Ordenar registros");

	jButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		URL resource = ComunicacionesForm.this.getClass()
			.getClassLoader().getResource("columns.properties");
		List<Field> fields = Utils.getFields(resource.getPath(),
			getSchema(), getBasicName(), reserved);

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
}
