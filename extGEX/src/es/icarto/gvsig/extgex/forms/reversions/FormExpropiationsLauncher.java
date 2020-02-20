package es.icarto.gvsig.extgex.forms.reversions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgex.forms.expropiations.ExpropiationsLayerResolver;
import es.icarto.gvsig.extgex.forms.expropiations.FormExpropiations;
import es.icarto.gvsig.navtableforms.gui.TableUtils;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;

public class FormExpropiationsLauncher implements MouseListener {

    private static final int BUTTON_RIGHT = 3;

    @Override
    public void mouseClicked(MouseEvent e) {
        final JTable table = (JTable) e.getComponent();
        if ((e.getClickCount() == 2) && TableUtils.hasRows(table)) {
            openForm(table);
        } else if ((e.getButton() == BUTTON_RIGHT) && TableUtils.hasRows(table)
                && TableUtils.isProperRowSelected(table)) {
            JPopupMenu popup = new JPopupMenu();

            JMenuItem menuOpenForm = new JMenuItem("Abrir expropiaciones");
            menuOpenForm.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    openForm(table);
                }
            });
            popup.add(menuOpenForm);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private void openForm(JTable table) {
        try {

            HashMap<String, String> row = TableUtils.getRow(table.getModel(), table.getSelectedRow());
            String fincaId = row.get("id_finca");
            String layerNameBasedOnIdFinca = ExpropiationsLayerResolver.getLayerNameBasedOnIdFinca(fincaId);
            TOCLayerManager toc = new TOCLayerManager();
            FLyrVect layerExpropiations = toc.getLayerByName(layerNameBasedOnIdFinca);

            int rowIndex = (int) TableUtils.getFeatureIndexFromJTable(table, layerExpropiations.getRecordset());
            if (rowIndex != AbstractNavTable.EMPTY_REGISTER) {
                FormExpropiations formExpropiations = new FormExpropiations(layerExpropiations, null);
                if (formExpropiations.init()) {
                    formExpropiations.setPosition(rowIndex);
                    selectFeaturesInForm(rowIndex, formExpropiations);
                    PluginServices.getMDIManager().addWindow(formExpropiations);
                }
            }
        } catch (ReadDriverException e) {
            e.printStackTrace();
        }
    }

    private void selectFeaturesInForm(int rowIndex, FormExpropiations formExpropiations) {
        formExpropiations.clearSelection();
        formExpropiations.selectFeature(rowIndex);
        formExpropiations.setOnlySelected(true);
    }

}
