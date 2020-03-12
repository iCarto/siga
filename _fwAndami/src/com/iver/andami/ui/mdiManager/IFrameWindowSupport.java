package com.iver.andami.ui.mdiManager;

import java.awt.Component;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JInternalFrame;

public interface IFrameWindowSupport {

    public abstract Iterator<IWindow> getWindowIterator();

    public abstract boolean contains(IWindow v);

    public abstract boolean contains(JInternalFrame wnd);

    public abstract JDialog getJDialog(IWindow p);

    public abstract JInternalFrame getJInternalFrame(IWindow p);

    /**
     * Gets the frame associated to the provided IWindow panel.
     * The frame will usually be a JInternalFrame or a JDialog.
     *
     * @param panel The IWindow panel whose frame wants to be retrieved.
     *
     * @return The associated frame, it will usually be a JInternalFrame or
     * a JDialog.
     */
    public abstract Component getFrame(IWindow panel);

    public abstract JInternalFrame createJInternalFrame(IWindow p);

    public abstract IWindow getWindow(Component dlg);

    public abstract void closeWindow(IWindow v);

    public abstract void updateWindowInfo(IWindow win, WindowInfo windowInfo);

}