package es.icarto.gvsig.extgia.batch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.LaunchGIAForms;
import es.icarto.gvsig.navtableforms.gui.tables.handler.BaseTableHandler;

public class AddTrabajosBatchListener implements ActionListener {

    private final String element;
    private final String dbTableName;
    private final BaseTableHandler trabajosTableHandler;
    private final AbstractFormWithLocationWidgets form;

    public AddTrabajosBatchListener(AbstractFormWithLocationWidgets form) {
        this.form = form;
        this.element = form.getElement().name();
        this.dbTableName = form.getTrabajosDBTableName();
        this.trabajosTableHandler = getTrabajosTableHandler();
    }

    private BaseTableHandler getTrabajosTableHandler() {
        BaseTableHandler trabajosTableHandler = null;
        for (BaseTableHandler th : form.getTableHandlers()) {
            if (th.getJTable().getName().equals(form.getTrabajosDBTableName())) {
                trabajosTableHandler = th;
                break;
            }
        }
        return trabajosTableHandler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isSelectionEmpty()) {
            showWarning("Debe tener registros seleccionados para añadir trabajos en lote");
            return;
        }

        LaunchGIAForms.callBatchTrabajosSubFormDependingOfElement(element, dbTableName, trabajosTableHandler);
    }

    private boolean isSelectionEmpty() {
        return form.getRecordset().getSelection().isEmpty();
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(form, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

}
