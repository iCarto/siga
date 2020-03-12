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
package com.iver.core.mdiManager;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiFrame.MDIFrame;
import com.iver.andami.ui.mdiManager.IFrameWindowSupport;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.IWindowInfoSupport;
import com.iver.andami.ui.mdiManager.IWindowProperties;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.core.mdiManager.frames.ExternalFrame;
import com.iver.core.mdiManager.frames.IFrame;
import com.iver.core.mdiManager.frames.InternalFrame;


public class FrameWindowSupport implements IFrameWindowSupport, IWindowProperties{
    private Hashtable<Container, IWindow> frameView = new Hashtable<Container, IWindow>();
    private Hashtable<IWindow, Container> viewFrame = new Hashtable<IWindow, Container>();
    private Image icon;
    private IWindowInfoSupport vis;
	private JFrame mainFrame;


    public FrameWindowSupport(MDIFrame mainFrame) {
    	this.mainFrame = mainFrame;
        icon = mainFrame.getIconImage();
    }

 
    @Override
    public Iterator<IWindow> getWindowIterator(){
    	return viewFrame.keySet().iterator();
    }


    @Override
    public boolean contains(IWindow v){
    	return viewFrame.containsKey(v);
    }



	@Override
    public boolean contains(JInternalFrame wnd) {
		return frameView.contains(wnd);
	}


 
    @Override
    public JDialog getJDialog(IWindow p) {
        JDialog dlg = (JDialog) viewFrame.get(p);

        if (dlg == null) {
            WindowInfo vi = vis.getWindowInfo(p);
            ExternalFrame nuevo = new ExternalFrame(mainFrame);

            nuevo.getContentPane().add((JPanel) p);
            nuevo.setSize(getWidth(p, vi), getHeight(p, vi) + 30);
            nuevo.setTitle(vi.getTitle());
            nuevo.setResizable(vi.isResizable());
            nuevo.setMinimumSize(vi.getMinimumSize());

            viewFrame.put(p, nuevo);
            frameView.put(nuevo, p);

            nuevo.setModal(vi.isModal());
            return nuevo;
        } else {
            return dlg;
        }
    }


  
    @Override
    public JInternalFrame getJInternalFrame(IWindow p) {
    	JInternalFrame frame = (JInternalFrame) viewFrame.get(p);

        if (frame == null) {
            frame = createJInternalFrame(p);
            viewFrame.put(p, frame);
            frameView.put(frame, p);
        } 
        
        return frame;
    }
    
  
    @Override
    public Component getFrame(IWindow panel) {
    	Object object = viewFrame.get(panel);
    	if (object!=null && object instanceof Component) {
    		return (Component) object;
    	}
    	else {
    		PluginServices.getLogger().error("window_not_found_"+panel.getWindowInfo().getTitle());
    		return null;
    	}
    }

  
    @Override
    public JInternalFrame createJInternalFrame(IWindow p) {
        WindowInfo wi = vis.getWindowInfo(p);
        JInternalFrame nuevo = new InternalFrame();
        if (icon != null){
            nuevo.setFrameIcon(new ImageIcon(icon));
        }
        
        nuevo.getContentPane().add((JPanel) p);
        nuevo.setClosable(!wi.isNotClosable());
        nuevo.setSize(getWidth(p, wi), getHeight(p, wi));
        nuevo.setTitle(wi.getTitle());
        nuevo.setVisible(wi.isVisible());
        nuevo.setResizable(wi.isResizable());
        nuevo.setIconifiable(wi.isIconifiable());
        nuevo.setMaximizable(wi.isMaximizable());
        nuevo.setLocation(wi.getX(), wi.getY());
        nuevo.setMinimumSize(wi.getMinimumSize());

        nuevo.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        return nuevo;
    }


    @Override
    public IWindow getWindow(Component dlg){
    	return frameView.get(dlg);
    }


    @Override
    public void closeWindow(IWindow v){
    	Object c = viewFrame.remove(v);
    	frameView.remove(c);
    }


 
    @Override
    public void setX(IWindow win, int x) {
    	IFrame frame = (IFrame) viewFrame.get(win);
    	frame.setX(x);
    }

 
    @Override
    public void setY(IWindow win, int y) {
    	IFrame frame = (IFrame) viewFrame.get(win);
    	frame.setY(y);
    }

    @Override
    public void setHeight(IWindow win, int height) {
    	IFrame frame = (IFrame) viewFrame.get(win);
    	frame.setHeight(height);
    }


    @Override
    public void setWidth(IWindow win, int width) {
    	IFrame frame = (IFrame) viewFrame.get(win);
    	frame.setWidth(width);
    }



    @Override
    public void setTitle(IWindow win, String title) {
    	IFrame frame = (IFrame) viewFrame.get(win);
    	frame.setTitle(title);
    }


	@Override
    public void setMinimumSize(IWindow win, Dimension minSize) {
    	IFrame frame = (IFrame) viewFrame.get(win);
    	frame.setMinimumSize(minSize);
	}


    public void setVis(IWindowInfoSupport vis) {
        this.vis = vis;
    }

 


    private int getWidth(IWindow v, WindowInfo wi) {
        if (wi.getWidth() == -1) {
            JPanel p = (JPanel) v;

            return p.getSize().width;
        } else {
            return wi.getWidth();
        }
    }


    private int getHeight(IWindow v, WindowInfo wi) {
        if (wi.getHeight() == -1) {
            JPanel p = (JPanel) v;

            return p.getSize().height;
        } else {
            return wi.getHeight();
        }
    }

    /* (non-Javadoc)
     * @see com.iver.core.mdiManager.IFrameWindowSupport#updateWindowInfo(com.iver.andami.ui.mdiManager.IWindow, com.iver.andami.ui.mdiManager.WindowInfo)
     */
    @Override
    public void updateWindowInfo(IWindow win, WindowInfo windowInfo) {
    	Object o = viewFrame.get(win);
    	if (windowInfo!=null && o!=null) {
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


    @Override
    public void setMaximized(IWindow iWindow, boolean maximized) {
    }


    @Override
    public void setNormalBounds(IWindow iWindow, Rectangle normalBounds) {
    }

}
