package com.iver.andami.ui.mdiManager;

import java.awt.Dimension;
import java.awt.Rectangle;

public interface IWindowProperties {

    /**
     * @param sv
     * @param i
     */
    public abstract void setX(IWindow iWindow, int x);

    /**
     * @param sv
     * @param i
     */
    public abstract void setY(IWindow iWindow, int y);

    /**
     * @param sv
     * @param i
     */
    public abstract void setHeight(IWindow iWindow, int height);

    /**
     * @param sv
     * @param i
     */
    public abstract void setWidth(IWindow iWindow, int width);

    /**
     * @param sw
     * @param maximized
     */
    public abstract void setMaximized(IWindow iWindow, boolean maximized);

    /**
     * @param sw
     * @param maximized
     */
    public abstract void setNormalBounds(IWindow iWindow, Rectangle normalBounds);

    /**
     * Sets the minimum allowed size for the provided singleton window.
     *
     * @param sw
     * @param minSize
     */
    public abstract void setMinimumSize(IWindow iWindow, Dimension minSize);

    /**
     * @param sv
     * @param string
     */
    public abstract void setTitle(IWindow iWindow, String title);

}