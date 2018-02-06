package com.iver.cit.gvsig.project.documents.table.gui;

import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.IWindowFilter;
import com.iver.andami.ui.mdiManager.IWindowVisitor;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

public class LayerTablesIWindowVisitor implements IWindowVisitor, IWindowFilter {

    private final FLyrVect layer;
    private Table table;

    public LayerTablesIWindowVisitor(FLyrVect layer) {
        this.layer = layer;
    }

    @Override
    public boolean filter(IWindow w) {
        if (w instanceof Table) {
            table = (Table) w;
            if (table.getModel().getAssociatedTable() != null && table.getModel().getAssociatedTable().equals(layer)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean accept(IWindow w) {
        return filter(w);
    }

    @Override
    public void visit(IWindow w) {
        if (accept(w)) {
            visitTable(table);
        }
    }

    protected void visitTable(Table t) {
    };

}
