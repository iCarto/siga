package es.icarto.gvsig.extgia.batch.reconocimientos;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.LaunchGIAForms;

public class AddReconocimientosBatchListener implements ActionListener {

    private final AbstractFormWithLocationWidgets form;
    private final String element;
    private final String formFileName;
    private final String dbTableName;

    public AddReconocimientosBatchListener(AbstractFormWithLocationWidgets form) {
        this.form = form;
        this.element = form.getElement().name();
        this.dbTableName = form.getReconocimientosDBTableName();
        this.formFileName = form.getReconocimientosFormFileName();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean emptySelection = form.getRecordset().getSelection().isEmpty();
        if (emptySelection) {
            showWarning("Debe tener registros seleccionados para añadir reconocimientos en lote");
            return;
        }
        LaunchGIAForms.callBatchReconocimientosSubFormDependingOfElement(element, formFileName, dbTableName);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(form, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

}
