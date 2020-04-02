package es.icarto.gvsig.extgex.forms.expropiations;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiFrame.SelectableToolBar;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.StartEditing;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.icarto.gvsig.extgex.FormExpropiationsExtension;
import es.icarto.gvsig.extgex.cad.AddFincaCADTool;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;
import es.udc.cartolab.gvsig.navtable.listeners.PositionEvent;
import es.udc.cartolab.gvsig.navtable.listeners.PositionListener;

@SuppressWarnings("serial")
public class AddFincaAction extends AbstractAction implements PositionListener {

    private static final Logger logger = Logger.getLogger(AddFincaAction.class);

    private final AbstractNavTable nt;
    private final FLyrVect layer;

    private final StartEditing startEditingExt;

    public AddFincaAction(FLyrVect layer, AbstractNavTable nt) {
        super();
        this.nt = nt;
        this.layer = layer;
        putValue(SHORT_DESCRIPTION, "Añadir finca");
        ImageIcon icon = new ImageIcon(getClass().getResource("/add_finca.png"));
        putValue(SMALL_ICON, icon);
        putValue(ACTION_COMMAND_KEY, "NAVTABLE");

        onPositionChange(null); // to enable the button if the first feature does not have geom
        startEditingExt = (StartEditing) PluginServices.getExtension(StartEditing.class);
        removeFromToolbar();
        nt.addPositionListener(this);
    }

    private void removeFromToolbar() {
        for (SelectableToolBar foo : PluginServices.getMainFrame().getToolbars()) {
            if (foo.getName().equals(FormExpropiationsExtension.TOOLBAR_NAME)) {
                for (int i = 0; i < foo.getComponentCount(); i++) {
                    Component c = foo.getComponent(i);
                    if (c instanceof JButton) {
                        JButton button = (JButton) c;
                        if (button.getActionCommand().equals("TOOLBAR")) {
                            foo.remove(c);
                        }
                    }
                }
            }
        }
    }

    private void addToToolbar() {
        for (SelectableToolBar foo : PluginServices.getMainFrame().getToolbars()) {
            if (foo.getName().equals(FormExpropiationsExtension.TOOLBAR_NAME)) {
                JButton add = foo.add(this);
                add.setActionCommand("TOOLBAR");
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!e.getActionCommand().equals("TOOLBAR")) {

            nt.removePositionListener(this);

            nt.clearSelection();
            nt.selectCurrentFeature();

            PluginServices.getMDIManager().closeWindow(nt);

            addToToolbar();

        }

        IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();

        if (!(iWindow instanceof View)) {
            return;
        }
        View view = (View) iWindow;

        FLayers layers = view.getMapControl().getMapContext().getLayers();
        layers.setAllActives(false);
        layer.setActive(true);
        if (!layer.isEditing()) {
            startEditingExt.startEditing(view, layer);
        }

        CADExtension.setCADTool(AddFincaCADTool.KEY, true);
        PluginServices.getMainFrame().enableControls();

    }

    @Override
    public void onPositionChange(PositionEvent e) {
        int numReg = (int) nt.getPosition();
        try {
            IGeometry geometry = layer.getSource().getFeature(numReg).getGeometry();
            setEnabled(geometry == null);
        } catch (ExpansionFileReadException e1) {
            logger.error(e1.getStackTrace(), e1);
        } catch (ReadDriverException e1) {
            logger.error(e1.getStackTrace(), e1);
        }

    }

    @Override
    public void beforePositionChange(PositionEvent e) {
        // nothing to do here
    }

}
