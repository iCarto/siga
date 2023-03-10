package es.icarto.gvsig.navtableforms.gui.tables.handler;

import java.util.Map;

import javax.swing.JComponent;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;

import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.gui.tables.menu.VectorialUpdateJTableContextualMenu;
import es.icarto.gvsig.navtableforms.gui.tables.model.TableModelFactory;
import es.icarto.gvsig.navtableforms.gui.tables.model.VectorialTableModel;
import es.icarto.gvsig.navtableforms.utils.FormFactory;

/**
 * VectorialTableHandler
 * 
 * Handler for relationships tables that link to a vectorial form.
 * 
 * @author Jorge L?pez Fern?ndez <jlopez@cartolab.es>
 */

public class VectorialTableHandler extends BaseTableHandler {

    public VectorialTableHandler(String layerName,
	    Map<String, JComponent> widgets, String[] foreignKeyId,
	    String[] colNames, String[] colAliases) {
	super(layerName, widgets, foreignKeyId, colNames, colAliases);
	FormFactory.checkAndLoadLayerRegistered(layerName);
    }
    
    public VectorialTableHandler(String layerName,
	    Map<String, JComponent> widgets, String foreignKeyId,
	    String[] colNames, String[] colAliases) {
	this(layerName, widgets, new String[] {foreignKeyId}, colNames, colAliases);
    }

    @Override
    protected void createTableModel() throws ReadDriverException {
	model = TableModelFactory.createFromLayerWithFilter(sourceTableName, destinationKey,
			originKeyValue, colNames, colAliases);
	jtable.setModel(model);
    }

    @Deprecated
    public void reload(AbstractForm form) {
	reload();
	((VectorialUpdateJTableContextualMenu) listener).setDialog(form);
    }

    @Override
    protected void createTableListener() {
	listener = new VectorialUpdateJTableContextualMenu(sourceTableName);
    }

}
