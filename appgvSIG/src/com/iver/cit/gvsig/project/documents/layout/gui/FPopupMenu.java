/*
 * Created on 02-mar-2004
 *
 * gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
 *   Av. Blasco Ib��ez, 50
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
package com.iver.cit.gvsig.project.documents.layout.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.iver.cit.gvsig.project.documents.IContextMenuAction;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.AbstractLayoutContextMenuAction;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.BeforeLayoutMenuEntry;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.BehindLayoutMenuEntry;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.CancelLayoutMenuEntry;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.CopyLayoutMenuEntry;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.CutLayoutMenuEntry;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.PasteLayoutMenuEntry;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.PositionLayoutMenuEntry;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.PropertyLayoutMenuEntry;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.RefreshLayoutMenuEntry;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.SelectAllLayoutMenuEntry;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.SimplifyLayoutMenuEntry;
import com.iver.cit.gvsig.project.documents.layout.contextmenu.gui.TerminateLayoutMenuEntry;
import com.iver.cit.gvsig.project.documents.layout.fframes.IFFrame;
import com.iver.utiles.extensionPoints.ExtensionPoint;
import com.iver.utiles.extensionPoints.ExtensionPoints;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;

/**
 * Menu de bot�n derecho para el Layout.
 * Se pueden a�adir entradas facilmente desde una extensi�n,
 * creando una clase derivando de LayoutMenuEntry, y a�adiendola en
 * est�tico (o en tiempo de carga de la extensi�n) a FPopupMenu.
 * (Las entradas actuales est�n hechas de esa manera).
 *
 * @author Vicente Caballero Navarro
 *
 */

public class FPopupMenu extends JPopupMenu {
	//private static ArrayList menuEntrys = new ArrayList();
    protected Layout layout;
    private ExtensionPoint extensionPoint;
    private IFFrame[] selecteds;
    private static String extPoint="Layout_PopupActions";
    //private JMenuItem capa;

    public static void registerExtensionPoint() {

    	ExtensionPoints extensionPoints = ExtensionPointsSingleton.getInstance();
    	extensionPoints.add(extPoint,"Terminate",new TerminateLayoutMenuEntry());
    	extensionPoints.add(extPoint,"Cancel",new CancelLayoutMenuEntry());
    	extensionPoints.add(extPoint,"Copy",new CopyLayoutMenuEntry());
    	extensionPoints.add(extPoint,"Cut",new CutLayoutMenuEntry());
    	extensionPoints.add(extPoint,"Paste",new PasteLayoutMenuEntry());
    	extensionPoints.add(extPoint,"Simplify",new SimplifyLayoutMenuEntry());
    	extensionPoints.add(extPoint,"Property",new PropertyLayoutMenuEntry());
    	extensionPoints.add(extPoint,"SelectAll",new SelectAllLayoutMenuEntry());
    	extensionPoints.add(extPoint,"Behind",new BehindLayoutMenuEntry());
    	extensionPoints.add(extPoint,"Before",new BeforeLayoutMenuEntry());
    	extensionPoints.add(extPoint,"Position",new PositionLayoutMenuEntry());
    	extensionPoints.add(extPoint,"Refresh",new RefreshLayoutMenuEntry());

    }
    /**
     * Creates a new FPopupMenu object.
     *
     * @param nodo DOCUMENT ME!
     * @param vista DOCUMENT ME!
     */
    public FPopupMenu(Layout layout) {
        super();
        this.initialize(layout);
    }

    private void initialize(Layout layout) {
        this.layout = layout;
        this.extensionPoint = (ExtensionPoint)ExtensionPointsSingleton.getInstance().get(extPoint);
		this.selecteds = this.layout.getLayoutContext().getFFrameSelected();
		IContextMenuAction[] actions = this.getActionList();
		this.createMenuElements(actions);
    }

    private IContextMenuAction[] getActionList() {
		ArrayList actionArrayList = new ArrayList();
		Iterator iter = this.extensionPoint.keySet().iterator();
		AbstractLayoutContextMenuAction action;
		while (iter.hasNext()) {
			action = null;
			try {
				action = (AbstractLayoutContextMenuAction)this.extensionPoint.create((String)iter.next());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			if (action != null) {
				action.setLayout(layout);
				if (action.isVisible(null,this.selecteds)) {
					actionArrayList.add(action);
				}
			}
		}
		IContextMenuAction[] result = (IContextMenuAction[])Array.newInstance(IContextMenuAction.class,actionArrayList.size());
		System.arraycopy(actionArrayList.toArray(),0,result,0,actionArrayList.size());
		Arrays.sort(result,new CompareAction());

		return result;
    }

	public class CompareAction implements Comparator{
		public int compare(Object o1, Object o2) {
			return this.compare((IContextMenuAction)o1,(IContextMenuAction)o2);
		}

		public int compare(IContextMenuAction o1, IContextMenuAction o2) {
			NumberFormat formater = NumberFormat.getInstance();
			formater.setMinimumIntegerDigits(3);
			String key1= ""+formater.format(o1.getGroupOrder())+o1.getGroup()+formater.format(o1.getOrder());
			String key2= ""+formater.format(o2.getGroupOrder())+o2.getGroup()+formater.format(o2.getOrder());
			return key1.compareTo(key2);
		}
	}

	private void createMenuElements(IContextMenuAction[] actions) {
		String group = null;
		for (int i=0;i < actions.length;i++) {
			IContextMenuAction action = actions[i];
			MenuItem item = new MenuItem(action.getText(),action);
			item.setEnabled(action.isEnabled(null,this.selecteds));
			if (!action.getGroup().equals(group)) {
				if (group != null) this.addSeparator();
				group = action.getGroup();
			}
			this.add(item);
		}

	}


	public class MenuItem extends JMenuItem implements ActionListener{
		private IContextMenuAction action;
		public MenuItem(String text,IContextMenuAction documentAction) {
			super(text);
			this.action = documentAction;
			String tip = this.action.getDescription();
			if (tip != null && tip.length() > 0) {
				this.setToolTipText(tip);
			}
			this.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			this.action.execute(layout.getLayoutContext(), FPopupMenu.this.selecteds);
		}
	}
}
