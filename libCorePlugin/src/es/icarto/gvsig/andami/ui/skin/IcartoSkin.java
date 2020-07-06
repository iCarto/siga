package es.icarto.gvsig.andami.ui.skin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.DefaultDesktopManager;
import javax.swing.DesktopManager;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.ui.mdiFrame.GlassPane;
import com.iver.andami.ui.mdiFrame.MDIFrame;
import com.iver.andami.ui.mdiFrame.NewStatusBar;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.IWindowFilter;
import com.iver.andami.ui.mdiManager.IWindowVisitor;
import com.iver.andami.ui.mdiManager.MDIManager;
import com.iver.andami.ui.mdiManager.MDIUtilities;
import com.iver.andami.ui.mdiManager.SingletonDialogAlreadyShownException;
import com.iver.andami.ui.mdiManager.SingletonWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

public class IcartoSkin extends Extension implements MDIManager {

    // Magik Numbers added for the method 'centreJInternalFrame'
    private static final int DefaultXMargin = 20;
    private static final int DefaultYMargin = 20;
    private static final int MinimumXMargin = 130;
    private static final int MinimumYMargin = 60;

    /**
     * Variable privada <code>desktopManager</code> para usarlo cuando sale una
     * ventana que no queremos que nos restaure las que tenemos maximizadas.
     * Justo antes de usar el setMaximize(false), le pegamos el cambiazo.
     */
    private static DesktopManager desktopManager = new DefaultDesktopManager();

    private static Logger logger = Logger.getLogger(IcartoSkin.class.getName());

    /** Panel de la MDIFrame */
    private final MyDesktopPane panel = new MyDesktopPane();

    private MDIFrame mainFrame;

    private final GlassPane glassPane = new GlassPane();

    /**
     * Associates JInternalFrames with the IWindow they contain
     */
    private WindowSupport fws;

    private Cursor lastCursor = null;

    @Override
    public void init(MDIFrame f) {
        // Inicializa el Frame y la consola
        mainFrame = f;
        mainFrame.setGlassPane(glassPane);
        panel.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        mainFrame.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setDesktopManager(desktopManager);

        fws = new WindowSupport(mainFrame, panel);

        // TODO (jaume) esto no debería de estar aquí...
        // molaría más en un diálogo de preferencias
        // es sólo una prueba
        KeyStroke controlTab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_MASK);

        PluginServices.registerKeyStroke(controlTab, new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                IWindow[] views = getAllWindows();
                if (views.length <= 0 || e.getID() == KeyEvent.KEY_PRESSED) {
                    return false;
                }

                int current = 0;
                for (int i = 0; i < views.length; i++) {
                    if (views[i].equals(getActiveWindow())) {
                        current = i;
                        break;
                    }
                }
                addWindow(views[(current + 1) % views.length]);
                return true;
            }

        });
    }

    private boolean singletonPreviouslyAdded(WindowInfo wi, IWindow p) {
        if (p instanceof SingletonWindow) {
            SingletonWindow sw = (SingletonWindow) p;
            if (fws.registerWindowSingleton(sw.getClass(), sw.getWindowModel(), wi)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IWindow addWindow(IWindow iWindow) throws SingletonDialogAlreadyShownException {

        WindowInfo windowInfo = fws.getWindowInfo(iWindow);

        MDIUtilities.checkWindowInfo(windowInfo, iWindow);

        if (singletonPreviouslyAdded(windowInfo, iWindow)) {
            return addWindowSingletonPreviouslyAdded(windowInfo, iWindow);
        }

        if (windowInfo.isModal()) {
            return fws.addModalDialog(iWindow);
        }

        if (WindowInfo.EXTERNAL_PROFILE.equals(iWindow.getWindowProfile())) {
            return fws.addExternalWindow(iWindow);
        }

        return fws.addInternalWindow(iWindow);
    }

    private IWindow addWindowSingletonPreviouslyAdded(WindowInfo wi, IWindow iWindow) {
        SingletonWindow singletonWindow = (SingletonWindow) iWindow;
        // Si la vista no está actualmente abierta
        final int originalX = iWindow.getWindowInfo().getX();
        final int originalY = iWindow.getWindowInfo().getY();
        if (!fws.containsSingleton(singletonWindow)) {
            fws.addInternalWindow(iWindow);

            iWindow.getWindowInfo().setX(originalX);
            iWindow.getWindowInfo().setY(originalY);

            return iWindow;
        } else {
            // La vista está actualmente abierta
            JInternalFrame frame = (JInternalFrame) fws.getFrameSingleton(singletonWindow);
            fws.activateInternalWindow(frame);
            fws.setActive(iWindow);
            return fws.getWindow(frame);
        }
    }

    @Override
    public IWindow addCentredWindow(IWindow p) throws SingletonDialogAlreadyShownException {
        IWindow window = addWindow(p);
        centreFrame(window);
        return window;
    }

    /**
     * Centers the Frame in the contentPane of the MainFrame. If the frame can't
     * be showed completely, it tries to show its top-left corner.
     *
     * @param iWindow
     *            The IWindow to center
     */
    private synchronized void centreFrame(IWindow iWindow) {
        Component window = fws.getFrame(iWindow);
        if (window == null) {
            return;
        }

        // The top-left square of frame reference
        Point newReferencePoint = new Point();

        // A reference to the panel where the JInternalFrame will be displayed
        Container contentPane = ((JFrame) PluginServices.getMainFrame()).getContentPane();

        // Get the NewStatusBar component
        NewStatusBar newStatusBar = ((NewStatusBar) contentPane.getComponent(1));
        JDesktopPane jDesktopPane = ((JDesktopPane) contentPane.getComponent(2));

        // The last substraction is for if there is any menu,... at left
        int visibleWidth = contentPane.getWidth() - contentPane.getX();
        // The last substraction is for if there is any menu,... at top
        int visibleHeight = contentPane.getHeight() - newStatusBar.getHeight() - contentPane.getY()
                - Math.abs(jDesktopPane.getY() - contentPane.getY());
        int freeWidth = visibleWidth - window.getWidth();
        int freeHeight = visibleHeight - window.getHeight();

        // Calculate the new point reference (Assure that the top-left corner is
        // showed)
        if (freeWidth < 0) {
            if (visibleWidth > MinimumXMargin) {
                newReferencePoint.x = DefaultXMargin;
            } else {
                newReferencePoint.x = 0;
            }
        } else {
            newReferencePoint.x = freeWidth / 2;
        }

        if (freeHeight < 0) {
            if (visibleHeight > MinimumYMargin) {
                newReferencePoint.y = DefaultYMargin;
            } else {
                newReferencePoint.y = 0;
            }
        } else {
            newReferencePoint.y = freeHeight / 2;
        }

        // Set the new location for this JInternalFrame
        window.setLocation(newReferencePoint);
    }

    @Override
    public IWindow getActiveWindow() {

        // Sólo funciona correctamente con Ventanas Externas, usar JDialog en lugar de JInternalFrame para algunas
        // ventanas cuando las ventanas externas
        // antes eran PALETTE
        JInternalFrame frame = panel.getSelectedFrame();
        if (frame == null) {
            return null;
        }

        IWindow iWindow = fws.getWindow(frame);
        if (iWindow == null) {
            return null;
        }

        if (iWindow.getWindowInfo().isPalette()) {
            return fws.getActiveWindow();
        }

        return fws.getWindow(frame);
    }

    @Override
    public IWindow getFocusWindow() {
        JInternalFrame frame = panel.getSelectedFrame();
        if (frame == null) {
            return null;
        }

        IWindow iWindow = fws.getWindow(frame);
        if (iWindow == null) {
            return null;
        }
        return fws.getWindow(frame);

    }

    @Override
    public void closeWindow(IWindow p) {
        fws.closeWindow2(p);
    }

    @Override
    public void closeAllWindows() {
        ArrayList<IWindow> eliminar = new ArrayList<IWindow>();
        Iterator<IWindow> i = fws.getWindowIterator();

        while (i.hasNext()) {
            eliminar.add(i.next());
        }

        for (Iterator<IWindow> iter = eliminar.iterator(); iter.hasNext();) {
            IWindow vista = iter.next();
            closeWindow(vista);
        }
    }

    @Override
    public WindowInfo getWindowInfo(IWindow w) {
        WindowInfo wi = fws.getWindowInfo(w);
        return wi;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void execute(String actionCommand) {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void setWaitCursor() {
        if (mainFrame != null) {
            glassPane.setVisible(true);
            lastCursor = mainFrame.getCursor();
            fws.setWaitCursor();
            glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
    }

    @Override
    public void restoreCursor() {
        if (mainFrame != null) {
            glassPane.setVisible(false);
            fws.restoreCursor();
            glassPane.setCursor(lastCursor);
        }
    }

    @Override
    public boolean closeSingletonWindow(Object model) {
        JInternalFrame[] frames = (JInternalFrame[]) fws.getFramesSingleton(model);
        if (frames.length == 0) {
            return false;
        }
        for (int i = 0; i < frames.length; i++) {
            fws.closeJInternalFrame(frames[i]);
        }
        return true;
    }

    @Override
    public IWindow[] getAllWindows() {
        ArrayList<IWindow> windows = new ArrayList<IWindow>();
        Iterator<IWindow> i = fws.getWindowIterator();

        while (i.hasNext()) {
            windows.add(i.next());
        }
        return windows.toArray(new IWindow[0]);
    }

    @Override
    public <T extends IWindow> List<T> getAllWindows(IWindowFilter filter) {
        ArrayList<T> windows = new ArrayList<T>();
        for (IWindow w : getAllWindows()) {
            if (filter.filter(w)) {
                windows.add((T) w);
            }
        }
        return windows;
    }

    @Override
    public void process(IWindowVisitor visitor) {
        for (IWindow w : getAllWindows()) {
            visitor.visit(w);
        }
    }

    @Override
    public IWindow[] getOrderedWindows() {
        TreeMap<Integer, IWindow> windows = new TreeMap<Integer, IWindow>();
        Iterator<IWindow> winIterator = fws.getWindowIterator();

        Component frame;
        IWindow win;
        /**
         * The order of the window in the JDesktopPane. Smaller numbers are
         * closer to the foreground.
         */
        int zPosition;
        while (winIterator.hasNext()) {
            win = winIterator.next();
            frame = fws.getFrame(win);
            zPosition = panel.getPosition(frame);
            int layer = panel.getLayer(frame);

            if (!(frame instanceof JDialog)) { // JDialogs are not in inside the
                // LayeredPane
                // flatten all the layers
                if (layer == JLayeredPane.DEFAULT_LAYER.intValue()) {
                    zPosition += 50000;
                } else if (layer == JLayeredPane.PALETTE_LAYER.intValue()) {
                    zPosition += 40000;
                } else if (layer == JLayeredPane.MODAL_LAYER.intValue()) {
                    zPosition += 30000;
                } else if (layer == JLayeredPane.POPUP_LAYER.intValue()) {
                    zPosition += 20000;
                } else if (layer == JLayeredPane.DRAG_LAYER.intValue()) {
                    zPosition += 10000;
                }
            }
            windows.put(new Integer(zPosition), win);
        }
        winIterator = windows.values().iterator();
        ArrayList<IWindow> winList = new ArrayList<IWindow>();
        while (winIterator.hasNext()) {
            winList.add(winIterator.next());
        }

        return winList.toArray(new IWindow[0]);
    }

    @Override
    public void setMaximum(IWindow v, boolean bMaximum) throws PropertyVetoException {
        JInternalFrame f = fws.getJInternalFrame(v);
        f.setMaximum(bMaximum);
    }

    @Override
    public void changeWindowInfo(IWindow w, WindowInfo wi) {
        JInternalFrame f = fws.getJInternalFrame(w);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JDesktopPane pnl = f.getDesktopPane();

        // Under rare circumstances, corrupt views get written into the
        // gvSIG project file. If we have one of those: just skip it!
        // Next time the user saves the project file, it will be eliminated.
        if (f == null || pnl == null) {
            return;
        }

        pnl.remove(f);
        int width;
        int height;
        if (wi.getWidth() != -1) {
            width = wi.getWidth();
        } else {
            width = f.getWidth();
        }
        if (wi.getHeight() != -1) {
            height = wi.getHeight();
        } else {
            height = f.getHeight();
        }
        f.setSize(new Dimension(width, height));
        f.setLocation(wi.getX(), wi.getY());
        if (wi.isPalette()) {
            pnl.add(f, JLayeredPane.PALETTE_LAYER);
            f.setFocusable(false);
        } else {
            pnl.add(f, JLayeredPane.DEFAULT_LAYER);
            f.setFocusable(true);
            if (wi.isClosed()) {
                closeWindow(w);
            }
        }

        if (wi.isMaximized()) {
            try {
                f.setMaximum(true);
            } catch (PropertyVetoException e) {
                logger.error(e.getMessage(), e);
            }
            f.setNormalBounds(wi.getNormalBounds());
        }
        fws.activateInternalWindow(f);
    }

    @Override
    public void refresh(IWindow win) {
        Component frame = fws.getFrame(win);
        if (frame != null) {
            frame.setVisible(true);
        }
    }

    @Override
    public void setBackgroundImage(ImageIcon image, String typeDesktop) {
        panel.setBackgroundImage(image, typeDesktop);
    }

    /**
     * We need this in order to addMenuBar to windows, for example
     *
     * @return
     */
    @Override
    public void addJMenuBarToWindow(IWindow w, JMenuBar menuBar) {
        Component c = fws.getFrame(w);
        if (c instanceof JDialog) {
            ((JDialog) c).setJMenuBar(menuBar);
        }
        if (c instanceof JInternalFrame) {
            ((JInternalFrame) c).setJMenuBar(menuBar);
        }
    }
}
