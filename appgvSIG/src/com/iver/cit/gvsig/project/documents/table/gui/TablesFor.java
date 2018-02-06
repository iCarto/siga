package com.iver.cit.gvsig.project.documents.table.gui;

import java.util.List;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.MDIManager;
import com.iver.cit.gvsig.exceptions.layers.CancelEditingLayerException;
import com.iver.cit.gvsig.exceptions.table.CancelEditingTableException;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.table.ProjectTable;

public class TablesFor {

    private final FLyrVect layer;
    private final ProjectTable pt;

    public TablesFor(FLyrVect layer) {
        this.layer = layer;
        this.pt = null;
    }

    public TablesFor(ProjectTable pt) {
        this.layer = null;
        this.pt = pt;
    }

    public static TablesFor layer(FLyrVect layer) {
        return new TablesFor(layer);
    }

    public static TablesFor projectTable(ProjectTable pt) {
        return new TablesFor(pt);

    }

    public void updateSelection() {
        PluginServices.getMDIManager().process(new LayerTablesIWindowVisitor(layer) {
            @Override
            protected void visitTable(Table t) {
                t.updateSelection();
            }
        });
    }

    public void stopEditingCell() {
        PluginServices.getMDIManager().process(new LayerTablesIWindowVisitor(layer) {
            @Override
            protected void visitTable(Table t) {
                t.stopEditingCell();
            }
        });
    }

    public void cancelEditing() throws CancelEditingTableException {
        MDIManager mdiManager = PluginServices.getMDIManager();
        if (layer != null) {
            List<Table> windows = mdiManager.getAllWindows(new LayerTablesIWindowVisitor(layer));
            for (Table t : windows) {
                t.cancelEditing();
            }
        } else if (pt != null) {
            IWindow[] windows = mdiManager.getAllWindows();
            for (int i = 0; i < windows.length; i++) {
                if (windows[i] instanceof Table && ((Table) windows[i]).getModel().equals(pt)) {
                    try {
                        ((Table) windows[i]).cancelEditing();
                    } catch (CancelEditingTableException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public Table get() {
        if (layer != null) {
            IWindow[] views = null;
            try {
                views = PluginServices.getMDIManager().getAllWindows();
                for (int j = 0; j < views.length; j++) {
                    if (views[j] instanceof Table) {
                        Table table = (Table) views[j];
                        if (table.getModel().getAssociatedTable() != null
                                && table.getModel().getAssociatedTable().equals(layer)) {
                            return table;
                        }
                    }
                }
            } catch (NullPointerException e) {
            }
        } else if (pt != null) {
            IWindow[] views = PluginServices.getMDIManager().getAllWindows();
            for (int i = 0; i < views.length; i++) {
                if (views[i] instanceof Table) {
                    Table table = (Table) views[i];
                    ProjectTable model = table.getModel();
                    if (model.equals(pt)) {
                        return table;
                    }
                }
            }
            return null;
        }

        return null;
    }

    public void cancelEdition() throws CancelEditingLayerException {
        MDIManager mdiManager = PluginServices.getMDIManager();
        if (layer != null) {
            List<Table> windows = mdiManager.getAllWindows(new LayerTablesIWindowVisitor(layer));
            for (Table t : windows) {
                t.cancelEditingCell();
                t.getModel().getModelo().cancelEdition(EditionEvent.ALPHANUMERIC);
            }
        }
    }

    public void closeWindow() {
        MDIManager mdiManager = PluginServices.getMDIManager();
        List<Table> windows = mdiManager.getAllWindows(new LayerTablesIWindowVisitor(layer));
        for (Table t : windows) {
            mdiManager.closeWindow(t);
        }
    }

}
