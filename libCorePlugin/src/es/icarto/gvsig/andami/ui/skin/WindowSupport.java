/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package es.icarto.gvsig.andami.ui.skin;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.iver.andami.ui.mdiFrame.MDIFrame;
import com.iver.andami.ui.mdiManager.IFrameWindowSupport;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.IWindowInfoSupport;
import com.iver.andami.ui.mdiManager.IWindowListener;
import com.iver.andami.ui.mdiManager.IWindowProperties;
import com.iver.andami.ui.mdiManager.SingletonWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.core.mdiManager.frames.ExternalFrame;
import com.iver.core.mdiManager.frames.IFrame;
import com.iver.core.mdiManager.frames.InternalFrame;

public class WindowSupport implements IWindowInfoSupport, IFrameWindowSupport, IWindowProperties {

    private static final Logger logger = Logger.getLogger(WindowSupport.class);

    private final Hashtable<Container, IWindow> frameView = new Hashtable<Container, IWindow>();
    private final Hashtable<IWindow, Container> viewFrame = new Hashtable<IWindow, Container>();
    private final Image icon;
    private final MDIFrame mainFrame;
    private final WindowStack windowStack;

    private static int serialId = 0;
    private final Hashtable<IWindow, WindowInfo> viewInfo = new Hashtable<IWindow, WindowInfo>();
    private final Hashtable<WindowInfo, IWindow> infoView = new Hashtable<WindowInfo, IWindow>();
    private final WindowPropertyChangeListener windowInfoListener;

    private final SingletonWindowSupport sws;
    private final ModalSupport modalSupport;
    private final ExternalSupport externalSupport;
    private final InternalSupport internalSupport;

    public WindowSupport(MDIFrame mainFrame, MyDesktopPane panel) {
        this.mainFrame = mainFrame;
        icon = mainFrame.getIconImage();
        this.windowStack = new WindowStack(this);
        sws = new SingletonWindowSupport(this, this);
        windowInfoListener = new WindowPropertyChangeListener(mainFrame, infoView, sws, this);
        modalSupport = new ModalSupport(this);
        externalSupport = new ExternalSupport(this);
        internalSupport = new InternalSupport(this, panel);
    }

    @Override
    public Iterator<IWindow> getWindowIterator() {
        return viewFrame.keySet().iterator();
    }

    @Override
    public boolean contains(IWindow iWindow) {
        return viewFrame.containsKey(iWindow);
    }

    @Override
    public boolean contains(JInternalFrame frame) {
        return frameView.contains(frame);
    }

    @Override
    public JDialog getJDialog(IWindow iWindow) {
        JDialog dlg = (JDialog) viewFrame.get(iWindow);

        if (dlg == null) {
            dlg = createJDialog(iWindow);
            viewFrame.put(iWindow, dlg);
            frameView.put(dlg, iWindow);
        }
        return dlg;
    }

    @Override
    public JInternalFrame getJInternalFrame(IWindow iWindow) {
        JInternalFrame frame = (JInternalFrame) viewFrame.get(iWindow);

        if (frame == null) {
            frame = createJInternalFrame(iWindow);
            viewFrame.put(iWindow, frame);
            frameView.put(frame, iWindow);
        }

        return frame;
    }

    private JDialog createJDialog(IWindow iWindow) {
        WindowInfo windowInfo = this.getWindowInfo(iWindow);
        ExternalFrame dlg = new ExternalFrame(mainFrame);
        // https://stackoverflow.com/questions/8504731/how-to-completely-remove-an-icon-from-jdialog
        // https://alvinalexander.com/blog/post/jfc-swing/change-image-icon-on-java-dialog-jdialog
        if (icon != null) {
            dlg.setIconImage(icon);
        }
        dlg.getContentPane().add((JPanel) iWindow);
        dlg.setSize(getWidth(iWindow, windowInfo), getHeight(iWindow, windowInfo) + 30);
        dlg.setTitle(windowInfo.getTitle());
        dlg.setResizable(windowInfo.isResizable());
        dlg.setMinimumSize(windowInfo.getMinimumSize());
        // ModalityType modal = windowInfo.isModal() ? Dialog.ModalityType.APPLICATION_MODAL
        // : Dialog.ModalityType.MODELESS;
        // dlg.setModalityType(modal);
        dlg.setModal(windowInfo.isModal());
        return dlg;
    }

    /**
     * Gets the frame associated to the provided IWindow panel.
     * The frame will usually be a JInternalFrame or a JDialog.
     *
     * @param iWindow
     *            The IWindow panel whose frame wants to be retrieved.
     *
     * @return The associated frame, it will usually be a JInternalFrame or
     *         a JDialog.
     */
    @Override
    public Component getFrame(IWindow iWindow) {
        Object object = viewFrame.get(iWindow);
        if (object != null && object instanceof Component) {
            return (Component) object;
        }
        return null;
    }

    @Override
    public JInternalFrame createJInternalFrame(IWindow iWindow) {
        WindowInfo windowInfo = this.getWindowInfo(iWindow);
        JInternalFrame frame = new InternalFrame();
        if (icon != null) {
            frame.setFrameIcon(new ImageIcon(icon));
        }

        frame.getContentPane().add((JPanel) iWindow);
        frame.setClosable(!windowInfo.isNotClosable());
        frame.setSize(getWidth(iWindow, windowInfo), getHeight(iWindow, windowInfo));
        frame.setTitle(windowInfo.getTitle());
        frame.setVisible(windowInfo.isVisible());
        frame.setResizable(windowInfo.isResizable());
        frame.setIconifiable(windowInfo.isIconifiable());
        frame.setMaximizable(windowInfo.isMaximizable());
        // Si se intenta maximizar un JInternalFrame sin haberlo añadido al JDestkopPanel se produce un error porqué no
        // es capaz de calcular las dimensiones, por tanto no puede hacerse aquí
        // try {
        // nuevo.setMaximum(wi.isMaximized());
        // } catch (PropertyVetoException e) {
        // logger.error(e.getStackTrace(), e);
        // }
        frame.setLocation(windowInfo.getX(), windowInfo.getY());
        frame.setMinimumSize(windowInfo.getMinimumSize());

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        return frame;
    }

    @Override
    public IWindow getWindow(Component dlg) {
        return frameView.get(dlg);
    }

    @Override
    public void closeWindow(IWindow iWindow) {
        Object c = viewFrame.remove(iWindow);
        frameView.remove(c);
    }

    private int getWidth(IWindow iWindow, WindowInfo windowInfo) {
        if (windowInfo.getWidth() == -1) {
            JPanel p = (JPanel) iWindow;

            return p.getSize().width;
        } else {
            return windowInfo.getWidth();
        }
    }

    private int getHeight(IWindow iWindow, WindowInfo windowInfo) {
        if (windowInfo.getHeight() == -1) {
            JPanel p = (JPanel) iWindow;

            return p.getSize().height;
        } else {
            return windowInfo.getHeight();
        }
    }

    @Override
    public void updateWindowInfo(IWindow iWindow, WindowInfo windowInfo) {
        Object o = viewFrame.get(iWindow);
        if (windowInfo != null && o != null) {
            if (o instanceof JComponent) {
                JComponent component = (JComponent) o;
                windowInfo.updateWidth(component.getWidth());
                windowInfo.updateHeight(component.getHeight());
                windowInfo.updateX(component.getX());
                windowInfo.updateY(component.getY());
                windowInfo.updateClosed(!component.isShowing());
                if (component instanceof JInternalFrame) {
                    JInternalFrame iframe = (JInternalFrame) component;
                    windowInfo.updateNormalBounds(iframe.getNormalBounds());
                    windowInfo.updateMaximized(iframe.isMaximum());
                }
            }
        }
    }

    /**
     * Devuelve la vista cuyo identificador es el parametro
     *
     * @param id
     *            Identificador de la vista que se quiere obtener
     *
     * @return La vista o null si no hay ninguna vista con ese identificador
     */
    @Override
    public IWindow getWindowById(int id) {
        Enumeration<WindowInfo> en = infoView.keys();

        while (en.hasMoreElements()) {
            WindowInfo vi = en.nextElement();

            if (vi.getId() == id) {
                return infoView.get(vi);
            }
        }

        return null;
    }

    @Override
    public synchronized WindowInfo getWindowInfo(IWindow iWindow) {
        WindowInfo windowInfo = viewInfo.get(iWindow);

        if (windowInfo != null) {
            this.updateWindowInfo(iWindow, windowInfo);
        } else {
            windowInfo = iWindow.getWindowInfo();

            // Para el título
            if (windowInfo.getHeight() != -1) {
                windowInfo.setHeight(windowInfo.getHeight() + 40);
            }

            windowInfo.addPropertyChangeListener(windowInfoListener);
            viewInfo.put(iWindow, windowInfo);
            infoView.put(windowInfo, iWindow);
            windowInfo.setId(serialId++);
        }

        return windowInfo;
    }

    public void add(IWindow iWindow, ActionListener actionListener) {
        windowStack.add(iWindow, actionListener);
    }

    public void setActive(IWindow iWindow) {
        windowStack.setActive(iWindow);
    }

    public void remove(IWindow iWindow) {
        windowStack.remove(iWindow);
    }

    public IWindow getActiveWindow() {
        return windowStack.getActiveWindow();
    }

    public boolean registerWindowSingleton(Class<? extends SingletonWindow> class1, Object windowModel,
            WindowInfo windowInfo) {
        return sws.registerWindow(class1, windowModel, windowInfo);
    }

    public boolean containsSingleton(SingletonWindow sw) {
        return sws.contains(sw);
    }

    public void openSingletonWindow(SingletonWindow sw, JInternalFrame frame) {
        sws.openSingletonWindow(sw, frame);
    }

    public Component getFrameSingleton(SingletonWindow sw) {
        return sws.getFrame(sw);
    }

    public Component getFrameSingleton(Class viewClass, Object model) {
        return sws.getFrame(viewClass, model);
    }

    public Component[] getFramesSingleton(Object model) {
        return sws.getFrames(model);
    }

    public void closeWindowSingleton(SingletonWindow sw) {
        sws.closeWindow(sw);
    }

    @Override
    public void setX(IWindow iWindow, int x) {
        IFrame frame = (IFrame) viewFrame.get(iWindow);
        frame.setX(x);
    }

    @Override
    public void setY(IWindow iWindow, int y) {
        IFrame frame = (IFrame) viewFrame.get(iWindow);
        frame.setY(y);
    }

    @Override
    public void setHeight(IWindow iWindow, int height) {
        IFrame frame = (IFrame) viewFrame.get(iWindow);
        frame.setHeight(height);
    }

    @Override
    public void setWidth(IWindow iWindow, int width) {
        IFrame frame = (IFrame) viewFrame.get(iWindow);
        frame.setWidth(width);
    }

    @Override
    public void setTitle(IWindow iWindow, String title) {
        IFrame frame = (IFrame) viewFrame.get(iWindow);
        frame.setTitle(title);
    }

    @Override
    public void setMinimumSize(IWindow win, Dimension minSize) {
        IFrame frame = (IFrame) viewFrame.get(win);
        frame.setMinimumSize(minSize);
    }

    @Override
    public void setMaximized(IWindow iWindow, boolean maximized) {
    }

    @Override
    public void setNormalBounds(IWindow iWindow, Rectangle normalBounds) {
    }

    public IWindow addModalDialog(IWindow iWindow) {
        JDialog dlg = this.getJDialog(iWindow);

        centerDialog(dlg);

        modalSupport.pushDialog(dlg);

        dlg.setVisible(this.getWindowInfo(iWindow).isVisible());
        return iWindow;
    }

    /**
     * Esta implementación sólo es válida para trabajar con JDialog, no funciona con JFrame
     */
    public IWindow addExternalWindow(IWindow iWindow) {
        JDialog dlg = this.getJDialog(iWindow);
        externalSupport.pushDialog(dlg);
        dlg.setVisible(this.getWindowInfo(iWindow).isVisible());
        return iWindow;
    }

    /**
     * Situa un diálogo modal en el centro de la pantalla
     *
     * @param dlg
     *            Diálogo que se quiere situar
     */
    private void centerDialog(JDialog dlg) {
        int offSetX = dlg.getWidth() / 2;
        int offSetY = dlg.getHeight() / 2;

        dlg.setLocation((mainFrame.getWidth() / 2) - offSetX, (mainFrame.getHeight() / 2) - offSetY);
    }

    public void setWaitCursor() {
        externalSupport.setWaitCursor();
        modalSupport.setWaitCursor();
    }

    public void restoreCursor() {
        modalSupport.restoreCursor();
        externalSupport.setWaitCursor();
    }

    void closeJInternalFrame(JInternalFrame frame) {
        try {
            IWindow iWindow = this.getWindow(frame);
            frame.setClosed(true);
            callWindowClosed(iWindow);
        } catch (PropertyVetoException e) {
            logger.error("Not compatible with property veto's. Use ViewInfo instead.", e);
        }
    }

    /**
     * If <code>window</code> implements IWindowListener, sent it the
     * windowActivated event.
     *
     * @param window
     *            The IWindow which has to be notified.
     */
    private void callWindowClosed(IWindow window) {
        if (window instanceof IWindowListener) {
            ((IWindowListener) window).windowClosed();
        }
    }

    void closeWindow2(IWindow iWindow) {
        modalSupport.closeWindow(iWindow);
        closeJInternalFrame(iWindow);
        externalSupport.closeWindow(iWindow);
    }

    private void closeJInternalFrame(IWindow iWindow) {
        Container container = viewFrame.get(iWindow);
        if (container == null) {
            return;
        }
        if (container instanceof JInternalFrame) {
            JInternalFrame frame = (JInternalFrame) container;
            this.closeJInternalFrame(frame);
        }

    }

    public IWindow addInternalWindow(IWindow iWindow) {
        return internalSupport.addWindow(iWindow);
    }

    public void activateInternalWindow(JInternalFrame frame) {
        internalSupport.activateWindow(frame);
    }
}
