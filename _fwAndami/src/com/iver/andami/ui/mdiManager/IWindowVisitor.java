package com.iver.andami.ui.mdiManager;

public interface IWindowVisitor {

    public boolean accept(IWindow w);

    public void visit(IWindow w);

}
