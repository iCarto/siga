package es.icarto.gvsig.commons.queries;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.miginfocom.swing.MigLayout;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.WindowInfo;

import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.gui.WidgetFactory;

@SuppressWarnings("serial")
public class CustomiceDialog<E> extends AbstractIWindow implements
	ActionListener {

    public static final int CANCEL = -1;
    public static final int OK = 1;

    private int status = CANCEL;

    private final List<JComboBox> order;

    private final DualListBox<E> dualListBox;
    private final OkCancelPanel okPanel;;

    public CustomiceDialog() {
	super(new MigLayout("wrap 1, insets 10", "[center]", ""));
	setWindowTitle("Personalizar consulta");
	setWindowInfoProperties(WindowInfo.MODALDIALOG);
	okPanel = WidgetFactory.okCancelPanel(this, this, this);

	dualListBox = new DualListBox<E>();
	dualListBox.addDestListDataListener(new UpdateOrderBy());
	add(dualListBox, "growx, growy");

	JPanel southPanel = new JPanel(new MigLayout("insets 10",
		"[grow][grow][grow][grow]"));
	southPanel.setBorder(WidgetFactory.borderTitled("Seleccione Orden"));

	order = new ArrayList<JComboBox>(4);
	addOrderCB(southPanel);
	addOrderCB(southPanel);
	addOrderCB(southPanel);
	addOrderCB(southPanel);
	add(southPanel);
    }

    private void addOrderCB(JPanel panel) {
	JComboBox jComboBox = new JComboBox();
	jComboBox.setPrototypeDisplayValue(DualListBox.prototypeCellValue);
	order.add(jComboBox);
	panel.add(jComboBox, "growx");
    }

    public void addSourceElements(List<E> values) {
	dualListBox.addSourceElements(values);
    }

    public void addSourceElements(E[] values) {
	dualListBox.addSourceElements(values);
    }

    public void addDestinationElements(List<E> values) {
	dualListBox.addDestinationElements(values);
    }

    public void clearDestinationListModel() {
	dualListBox.clearDestinationListModel();
    }

    private class UpdateOrderBy implements ListDataListener {

	@Override
	public void intervalAdded(ListDataEvent e) {
	    update((ListModel) e.getSource());
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
	    update((ListModel) e.getSource());
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
	    update((ListModel) e.getSource());
	}

	private void update(ListModel model) {
	    for (JComboBox c : order) {
		c.removeAllItems();
		c.addItem("");
		for (int i = 0; i < model.getSize(); i++) {
		    c.addItem(model.getElementAt(i));
		}
	    }
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals(OkCancelPanel.OK_ACTION_COMMAND)) {
	    status = OK;
	} else {
	    status = CANCEL;
	}
	PluginServices.getMDIManager().closeWindow(this);
    }

    public int open() {
	PluginServices.getMDIManager().addCentredWindow(this);
	return status;
    }

    public List<E> getFields() {
	return dualListBox.getDestList();
    }

    public List<E> getOrderBy() {
	List<E> list = new ArrayList<E>(4);
	for (JComboBox c : order) {
	    if (c.getSelectedItem() == null) {
		continue;
	    }
	    E sel = (E) c.getSelectedItem();
	    if (sel.toString().trim().length() == 0) {
		continue;
	    }
	    if (list.indexOf(sel) == -1) {
		list.add(sel);
	    }

	}

	return list;
    }

    @Override
    protected JButton getDefaultButton() {
	return okPanel.getOkButton();
    }

    @Override
    protected Component getDefaultFocusComponent() {
	return null;
    }
}
