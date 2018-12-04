package es.icarto.gvsig.extgex.forms;

import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import es.icarto.gvsig.extgia.forms.GIAAlphanumericTableHandler;
import es.icarto.gvsig.extgia.forms.GIASubForm;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
import es.icarto.gvsig.navtableforms.utils.FormFactory;


/*
 * Está aquí únicamente para que los getClass().getClassLoader().getResource cojan el paquete extgex y no otro
 */
public class GEXAlphanumericTableHandler extends GIAAlphanumericTableHandler {
        private static final Logger logger = Logger
            .getLogger(GIAAlphanumericTableHandler.class);
        
        public GEXAlphanumericTableHandler(String tableName,
            Map<String, JComponent> widgets, String foreignKeyId,
            String[] colNames, String[] colAliases, int[] colWidths,
            AbstractForm form, Class<? extends AbstractSubForm> subFormClass) {
        super(tableName, widgets, foreignKeyId, colNames,
            colAliases, colWidths, form, subFormClass);

        }
}
