/* gvSIG. Geographic Information System of the Valencian Government
 *
 * Copyright (C) 2007-2008 Infrastructures and Transports Department
 * of the Valencian Government.
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
 * Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 */
package com.iver.cit.gvsig.project.documents.view.toc.impl;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.gvsig.fmap.swing.toc.TOC;
import org.gvsig.fmap.swing.toc.TOCFactory;
import org.gvsig.fmap.swing.toc.TOCLocator;
import org.gvsig.fmap.swing.toc.action.TOCAction;
import org.gvsig.fmap.swing.toc.event.ActiveLayerChangeEvent;
import org.gvsig.fmap.swing.toc.event.ActiveLayerChangeEventListener;
import org.gvsig.fmap.swing.toc.event.LayerActionEvent;
import org.gvsig.fmap.swing.toc.event.LayerActionEventListener;
import org.gvsig.fmap.swing.toc.event.LegendActionEvent;
import org.gvsig.fmap.swing.toc.event.LegendActionEventListener;
import org.gvsig.tools.dynobject.DynObject;
import org.gvsig.tools.service.Manager;
import org.gvsig.tools.service.ServiceException;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.AtomicEvent;
import com.iver.cit.gvsig.fmap.AtomicEventListener;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.layers.CancelationException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrAnnotation;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerCollectionEvent;
import com.iver.cit.gvsig.fmap.layers.LayerCollectionListener;
import com.iver.cit.gvsig.fmap.layers.LayerPositionEvent;
import com.iver.cit.gvsig.fmap.layers.LegendChangedEvent;
import com.iver.cit.gvsig.fmap.layers.layerOperations.Classifiable;
import com.iver.cit.gvsig.fmap.layers.layerOperations.IHasImageLegend;
import com.iver.cit.gvsig.fmap.layers.layerOperations.LayerCollection;
import com.iver.cit.gvsig.fmap.rendering.IClassifiedLegend;
import com.iver.cit.gvsig.fmap.rendering.ICustomLegend;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.LegendListener;
import com.iver.cit.gvsig.fmap.rendering.SingleSymbolLegend;
import com.iver.cit.gvsig.project.documents.IContextMenuAction;
import com.iver.cit.gvsig.project.documents.view.toc.DnDJTree;
import com.iver.cit.gvsig.project.documents.view.toc.ITocItem;
import com.iver.cit.gvsig.project.documents.view.toc.ITocOrderListener;
import com.iver.cit.gvsig.project.documents.view.toc.TocItemBranch;
import com.iver.cit.gvsig.project.documents.view.toc.TocItemLeaf;
import com.iver.cit.gvsig.project.documents.view.toc.actions.MyMoveListener;
import com.iver.cit.gvsig.project.documents.view.toc.gui.FPopupMenu;
import com.iver.cit.gvsig.project.documents.view.toc.util.EventUtil;

/**
 * Default implementation of the TOC. It's the original gvSIG "TOC.java" with
 * little changes to make it implement the new TOC interface
 * 
 * @author gvSIG Team
 * @author Juan Lucas Dominguez jldominguez prodevelop es
 * @version $Id$
 * 
 */
public class DefaultToc extends JComponent implements TOC,
		TreeExpansionListener, ComponentListener, ITocOrderListener,
		LegendListener, LayerCollectionListener {

	/**
     * 
     */
	private static final long serialVersionUID = 8704106636809906947L;

	private static Logger logger = Logger.getLogger(DefaultToc.class);

	/**
	 * String key to be used in persisted properties
	 */
	public static final String TOC_CURRENT_TOC_KEY = "TOC_CURRENT_TOC";

	// =======================================

	private MapContext mapContext;
	private DnDJTree m_Tree;
	private DefaultTreeModel m_TreeModel;
	private DefaultMutableTreeNode m_Root;
	private TOCRenderer m_TocRenderer;
	private JScrollPane m_Scroller;

	// private ArrayList m_Listeners;
	private HashMap m_ItemsExpanded = new HashMap();
	private NodeSelectionListener nodeSelectionListener = null;

	private TOCFactory tocFactory = null;

	private ArrayList activeLayerChangeListeners = new ArrayList();
	private ArrayList legendActionListeners = new ArrayList();
	private ArrayList layerActionListeners = new ArrayList();

	public DefaultToc(TOCFactory tf) {

		tocFactory = tf;

		this.setLayout(new BorderLayout());
		this.setMinimumSize(new Dimension(100, 80));
		this.setPreferredSize(new Dimension(100, 80));

		// Construct the tree.
		m_Root = new DefaultMutableTreeNode(java.lang.Object.class);
		m_TreeModel = new DefaultTreeModel(m_Root);
		m_Tree = new DnDJTree(m_TreeModel);

		m_TocRenderer = new TOCRenderer();
		m_Tree.setCellRenderer(m_TocRenderer);

		m_Tree.setRootVisible(false);

		m_Tree.setShowsRootHandles(true);

		// Posibilidad de seleccionar de forma aleatoria nodos de la leyenda.
		m_Tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		nodeSelectionListener = new NodeSelectionListener(m_Tree, this);
		m_Tree.addMouseListener(nodeSelectionListener);
		m_Tree.setBackground(UIManager.getColor("Button.background"));
		m_Tree.setBorder(BorderFactory.createEtchedBorder());

		this.addComponentListener(this);

		m_Tree.addTreeExpansionListener(this);

		m_Tree.addOrderListener(this); // listenerx

		m_Tree.setRowHeight(0); // Para que lo determine el renderer

		m_Scroller = new JScrollPane(m_Tree);
		m_Scroller.setBorder(BorderFactory.createEmptyBorder());

		add(m_Scroller);
	}

	public JComponent getComponent() {
		return this;
	}

	public MapContext getMapContext() {
		return mapContext;
	}

	public void makeTocVisibleAreaIncludeLayer(FLayer lyr) {

		int pos = 0;
		FLayers plyr = lyr.getParentLayer();
		if (plyr != null) {
			try {
				pos = getIndexForChild(plyr, lyr);
			} catch (Exception e) {
				logger.error("While proving layer is listed in TOC. ", e);
			}
		}

		JScrollBar verticalBar = m_Scroller.getVerticalScrollBar();
		double widthPerEntry = verticalBar.getMaximum()
				/ lyr.getMapContext().getLayers().getLayersCount();
		verticalBar.setValue((int) widthPerEntry
				* (lyr.getMapContext().getLayers().getLayersCount() - pos - 1));
	}

	private int getIndexForChild(FLayers _lyrs, FLayer _lyr) throws Exception {

		int cnt = _lyrs.getLayersCount();
		for (int i = 0; i < cnt; i++) {
			if (_lyr == _lyrs.getLayer(i)) {
				return i;
			}
		}
		throw new Exception("Layer not found in FLayers");
	}

	public void refresh(FLayer lyr) {
		// needs to be improved
		refresh();
	}

	// =============================================

	/**
	 * Set the FMap. Not part of the API. Used only by DefaultTOCFactory
	 * 
	 * @param mc
	 *            FMap.
	 */
	public void setMapContext(MapContext mc) {

		if (mapContext == mc) {
			return;
		}

		mapContext = mc;

		mapContext.getLayers().addLegendListener(this);

		mapContext.addAtomicEventListener(new AtomicEventListener() {

			/**
			 * @see com.iver.cit.gvsig.fmap.AtomicEventListener#atomicEvent(com.iver.cit.gvsig.fmap.AtomicEvent)
			 */
			public void atomicEvent(AtomicEvent e) {
				if ((e.getLayerCollectionEvents().length > 0)
						|| (e.getLegendEvents().length > 0)) {
//					for (LayerCollectionEvent ev : e.getLayerCollectionEvents()) {
//						System.out.println("Evento: " + ev.getEventType() + " " + ev.getAffectedLayer().getName());
//					}
					SwingUtilities.invokeLater(new Runnable() {

						public void run() {
							refresh();
						}
					});
				}

				if (e.getLayerEvents().length > 0) {
					repaint();
				}

				if (e.getExtentEvents().length > 0) {
					repaint();
				}
			}
		});

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				refresh();
			}
		});
	}

	/**
	 * DOCUMENT ME!
	 */
	private void setExpandedNodes(DefaultMutableTreeNode node) {
		// int i = 0;
		// Las claves sobrantes de m_ItemsExpanded (provocadas
		// por layerRemove, se quitan en el evento layerRemoved
		// de este TOC
		DefaultMutableTreeNode n;
		Enumeration enumeration = node.children();

		// Si el padre no está expandido, no sigas.
		TreePath pathParent = new TreePath(node);
		if (node.getUserObject() instanceof ITocItem) {
			ITocItem itemP = (ITocItem) node.getUserObject();
			if (itemP != null) {
				Boolean b = (Boolean) m_ItemsExpanded.get(itemP.getLabel());
				if ((b!=null) && (b.booleanValue()==false)) {
//					m_Tree.collapsePath(pathParent);
					return;
				}
			}
		}
//		System.out.println("Expandiendo el nodo " + node.getUserObject());
		
		while (enumeration.hasMoreElements()) {
			
			n = (DefaultMutableTreeNode) enumeration.nextElement();
			if (n.getChildCount() > 0) {
				setExpandedNodes(n); // Problem: if we call setExpand on a leaf, there will be a TreeExpansionEvent and 
									 // treeExpanded method is called, where a TRUE is used for the branch (layers group).
									 // To avoid this problem, we will use a boolean variable to indicate we should not update
									 // m_ItmesExpanded while we are in these method (something like a "synchronized" method (semámforo)
			}
			TreePath path = new TreePath(m_TreeModel.getPathToRoot(n));
			ITocItem item = (ITocItem) n.getUserObject();
			Boolean b = (Boolean) m_ItemsExpanded.get(item.getLabel());

			if (b == null) // No estaba en el hash todavía: valor por defecto
			{
				// System.out.println("Primera expansión de " +
				// item.getLabel());
//				m_Tree.expandPath(path);
//				return;
				continue;
			}

			if (b.booleanValue()) {
				// System.out.println("Expansión de " + item.getLabel());
				m_Tree.expandPath(path);
			} else {
				// System.out.println("Colapso de " + item.getLabel());
				m_Tree.collapsePath(path);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.iver.cit.opensig.gui.IToc#refresh()
	 */
	public void refresh() {
		LayerCollection theLayers = mapContext.getLayers();
		m_Root.removeAllChildren();
		m_Root.setAllowsChildren(true);
		System.out.println("Refresh del toc");
		doRefresh(theLayers, m_Root);

		m_TreeModel.reload();
		setExpandedNodes(m_Root);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param theLayers
	 *            DOCUMENT ME!
	 * @param parentNode
	 *            DOCUMENT ME!
	 */
	private void doRefresh(LayerCollection theLayers,
			DefaultMutableTreeNode parentNode) {
		Dimension sizeLeaf = new Dimension(m_Tree.getWidth(), 15);
		Dimension sizeBranch = new Dimension(m_Tree.getWidth(), 25);

		for (int i = theLayers.getLayersCount() - 1; i >= 0; i--) {
			FLayer lyr = theLayers.getLayer(i);
			if (!lyr.isInTOC()) {
				continue;
			}
			TocItemBranch elTema = new TocItemBranch(lyr);
			elTema.setSize(sizeBranch);

			DefaultMutableTreeNode nodeLayer = new DefaultMutableTreeNode(
					elTema);

			m_TreeModel.insertNodeInto(nodeLayer, parentNode,
					parentNode.getChildCount());

			// TreePath path = new
			// TreePath(m_TreeModel.getPathToRoot(nodeLayer));
			// m_Tree.makeVisible(path);
			if (lyr instanceof LayerCollection) {
				LayerCollection group = (LayerCollection) lyr;
				doRefresh(group, nodeLayer);
			} else {
				if ((lyr instanceof Classifiable)
						&& !(lyr instanceof FLyrAnnotation)) {

					Classifiable aux = (Classifiable) lyr;
					ILegend legendInfo = aux.getLegend();

					try {
					    if (legendInfo instanceof ICustomLegend) {
						TocItemLeaf itemLeaf;
						itemLeaf = new TocItemLeaf(
								legendInfo.getDefaultSymbol(), legendInfo
										.getDefaultSymbol()
										.getDescription(),
								aux.getShapeType());
						itemLeaf.setSize(sizeLeaf);

						DefaultMutableTreeNode nodeValue = new DefaultMutableTreeNode(
								itemLeaf);
						m_TreeModel.insertNodeInto(nodeValue, nodeLayer,
								nodeLayer.getChildCount());
						continue;
						
					    }
						if (legendInfo instanceof IClassifiedLegend) {
							IClassifiedLegend cl = (IClassifiedLegend) legendInfo;
							String[] descriptions = cl.getDescriptions();
							ISymbol[] symbols = cl.getSymbols();

							for (int j = 0; j < descriptions.length; j++) {
								TocItemLeaf itemLeaf;
								itemLeaf = new TocItemLeaf(symbols[j],
										descriptions[j], aux.getShapeType());
								itemLeaf.setSize(sizeLeaf);

								DefaultMutableTreeNode nodeValue = new DefaultMutableTreeNode(
										itemLeaf);
								m_TreeModel.insertNodeInto(nodeValue,
										nodeLayer, nodeLayer.getChildCount());

								// TreePath pathSymbol = new
								// TreePath(m_TreeModel.getPathToRoot(
								// nodeValue));
								// m_Tree.makeVisible(pathSymbol);
							}
						}

						if ((legendInfo instanceof SingleSymbolLegend)
								&& (legendInfo.getDefaultSymbol() != null)) {
							TocItemLeaf itemLeaf;
							itemLeaf = new TocItemLeaf(
									legendInfo.getDefaultSymbol(), legendInfo
											.getDefaultSymbol()
											.getDescription(),
									aux.getShapeType());
							itemLeaf.setSize(sizeLeaf);

							DefaultMutableTreeNode nodeValue = new DefaultMutableTreeNode(
									itemLeaf);
							m_TreeModel.insertNodeInto(nodeValue, nodeLayer,
									nodeLayer.getChildCount());

							// TreePath pathSymbol = new
							// TreePath(m_TreeModel.getPathToRoot(
							// nodeValue));
							// m_Tree.makeVisible(pathSymbol);
						}

						if (lyr instanceof IHasImageLegend) {
							TocItemLeaf itemLeaf;
							IHasImageLegend auxLayer = (IHasImageLegend) lyr;
							Image image = auxLayer.getImageLegend();

							if (image != null) {
								itemLeaf = new TocItemLeaf();
								itemLeaf.setImageLegend(image, "",
										new Dimension(image.getWidth(null),
												image.getHeight(null)));// new
																		// Dimension(150,200));

								DefaultMutableTreeNode nodeValue = new DefaultMutableTreeNode(
										itemLeaf);
								m_TreeModel.insertNodeInto(nodeValue,
										nodeLayer, nodeLayer.getChildCount());
							}
						}
					} catch (ReadDriverException e) {
						e.printStackTrace();
					}
				}
			} // if instanceof layers
		}
	}

	/**
	 * @see com.iver.cit.opensig.gui.toc.ITocOrderListener#orderChanged(int,
	 *      int)
	 */
	public void orderChanged(int oldPos, int newPos, FLayers lpd) {
		// LayerCollection layers = mapContext.getLayers();
		// El orden es el contrario, hay que traducir.
		// El orden es el contrario, hay que traducir.
		// /oldPos = layers.getLayersCount() - 1 - oldPos;
		// /newPos = layers.getLayersCount() - 1 - newPos;
		try {
			// Si lpd es un grupo de capas, el usuario tiene más opciones: Mover
			// la capa arriba o abajo del grupo de capas, o añadir la capa a
			// ese grupo de capas

			int newto = lpd.getLayersCount() - newPos - 1;
			String layerName = lpd.getLayer(newto).getName();
			// obtenemos la capa sobre la que hemos dejado caer la otra.
			System.out.println("NewPos = " + layerName);
			boolean bUp = (Boolean) lpd.getProperty("GO_UP");
			lpd.getExtendedProperties().remove("GO_UP");
			mapContext.beginAtomicEvent();
			try {

				FLayers all = mapContext.getLayers();


				int pos = -1;
				for (int j = 0; j < lpd.getLayersCount(); j++) {
					if (lpd.getLayer(j).getName().equalsIgnoreCase(layerName)) {
						pos = lpd.getLayersCount() - j - 1;
						break;
					}
				}
				if (bUp) {
					if (oldPos > pos)
						pos = pos;
					else
						pos = pos - 1;
				}
				else {
					if (oldPos > pos)
						pos = pos + 1;
					else
						pos = pos;
				}

//				System.out.println("oldPos=" + oldPos + " newto=" + newto + " pos="
//						+ pos);
//				if (e.getActionCommand().equalsIgnoreCase("GROUP")) {
//					FLayer origin = lpd.getLayer(lpd.getLayersCount() - oldPos - 1);
//					FLayer dest = lpd.getLayer(layerName);
//					// Creamos una nueva agrupación
//					FLayer[] selected = new FLayer[2];
//					selected[0] = origin;
//					selected[1] = dest;
//					createLayerGroup(selected);
//
//				} else
					lpd.moveTo(oldPos, pos);

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			mapContext.endAtomicEvent();
			
//			final JPopupMenu pop = new JPopupMenu(PluginServices.getText(this,
//					"Move"));
//			JMenuItem itUp = new JMenuItem(PluginServices.getText(this,
//					"Move_above_layer") + " " + layerName);
//			JMenuItem itDown = new JMenuItem(PluginServices.getText(this,
//					"Move_below_layer") + " " + layerName);
//			JMenuItem itGroup = new JMenuItem(PluginServices.getText(this,
//					"Group_with_layer") + " " + layerName);
//
//			itUp.setActionCommand("UP");
//			itDown.setActionCommand("DOWN");
//			itGroup.setActionCommand("GROUP");
//
//			JDialog dlgPop = new JDialog((Frame) null);
//			dlgPop.setUndecorated(true);
//			dlgPop.getContentPane().add(pop);
//			dlgPop.setModal(true);

//			MyMoveListener lis = new MyMoveListener(lpd, newto, oldPos,
//					getMapContext(), dlgPop);

//			itUp.addActionListener(lis);
//			itDown.addActionListener(lis);
//			itGroup.addActionListener(lis);
//
//			pop.add(itUp);
//			pop.add(itDown);
//			pop.add(itGroup);
//
//			PointerInfo pi = MouseInfo.getPointerInfo();
//			final Point p = pi.getLocation();
//			//SwingUtilities.convertPointToScreen(p, this);
////			pop.show(this, p.x, p.y);
//			pop.setInvoker(dlgPop);
//			pop.setLocation(p.x, p.y);
//			pop.setVisible(true);			
//			dlgPop.pack();
//			
//			dlgPop.setVisible(true);
			
		} catch (CancelationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// No hace falta un refresh, lo hace mediante eventos.
		// refresh();
		mapContext.invalidate();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param lpo
	 *            DOCUMENT ME!
	 * @param lpd
	 *            DOCUMENT ME!
	 * @param ls
	 *            DOCUMENT ME!
	 */
	public void parentChanged(FLayers lpo, FLayers lpd, FLayer ls) {
		lpo.removeLayer(ls);
		if (lpo.getLayersCount() == 0)
			lpo.getParentLayer().removeLayer(lpo);
		Integer drop_pos = (Integer) ls.getProperty("DROP_POS");
		ls.getExtendedProperties().remove("DROP_POS");		
		boolean bUp = (Boolean) ls.getProperty("GO_UP");
		ls.getExtendedProperties().remove("GO_UP");

		int correct = 1;
		if (bUp) {
			correct = 0;
		}
		
		lpd.addLayer(lpd.getLayersCount() - drop_pos - correct, ls);

		PluginServices.getMainFrame().enableControls();
		/*
		 * if (lpo.getLayersCount()==0){ lpo.getParentLayer().removeLayer(lpo);
		 * }
		 */
		mapContext.invalidate();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.
	 * ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent
	 * )
	 */
	public void componentMoved(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.
	 * ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
		System.out.println("Cambiando tamaño.");

		int i = 0;
		DefaultMutableTreeNode n;
		Enumeration enumeration = m_Root.children();

		while (enumeration.hasMoreElements()) {
			n = (DefaultMutableTreeNode) enumeration.nextElement();

			if (n.getUserObject() instanceof TocItemBranch) {
				ITocItem item = (ITocItem) n.getUserObject();
				Dimension szAnt = item.getSize();
				item.setSize(new Dimension(this.getWidth() - 40, szAnt.height));
			}

		}

		// m_Tree.setSize(this.getSize());
		System.out.println("Ancho del tree=" + m_Tree.getWidth() + " "
				+ m_Tree.getComponentCount());
		System.out.println("Ancho del TOC=" + this.getWidth());

		// m_Tree.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent
	 * )
	 */
	public void componentShown(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.layers.LayerListener#legendChanged(com.iver.cit
	 * .gvsig.fmap.rendering.LegendChangedEvent)
	 */
	public void legendChanged(LegendChangedEvent e) {
		System.out.println("Refrescando TOC");
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.layers.LayerCollectionListener#layerAdded(com
	 * .iver.cit.gvsig.fmap.layers.LayerCollectionEvent)
	 */
	public void layerAdded(LayerCollectionEvent e) {
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.layers.LayerCollectionListener#layerMoved(com
	 * .iver.cit.gvsig.fmap.layers.LayerPositionEvent)
	 */
	public void layerMoved(LayerPositionEvent e) {
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.layers.LayerCollectionListener#layerRemoved(com
	 * .iver.cit.gvsig.fmap.layers.LayerCollectionEvent)
	 */
	public void layerRemoved(LayerCollectionEvent e) {
		m_ItemsExpanded.remove(e.getAffectedLayer().getName());
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.layers.LayerCollectionListener#layerAdding(com
	 * .iver.cit.gvsig.fmap.layers.LayerCollectionEvent)
	 */
	public void layerAdding(LayerCollectionEvent e) throws CancelationException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.layers.LayerCollectionListener#layerMoving(com
	 * .iver.cit.gvsig.fmap.layers.LayerPositionEvent)
	 */
	public void layerMoving(LayerPositionEvent e) throws CancelationException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.layers.LayerCollectionListener#layerRemoving(
	 * com.iver.cit.gvsig.fmap.layers.LayerCollectionEvent)
	 */
	public void layerRemoving(LayerCollectionEvent e)
			throws CancelationException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.layers.LayerCollectionListener#activationChanged
	 * (com.iver.cit.gvsig.fmap.layers.LayerCollectionEvent)
	 */
	public void activationChanged(LayerCollectionEvent e)
			throws CancelationException {
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.layers.LayerCollectionListener#visibilityChanged
	 * (com.iver.cit.gvsig.fmap.layers.LayerCollectionEvent)
	 */
	public void visibilityChanged(LayerCollectionEvent e)
			throws CancelationException {
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event
	 * .TreeExpansionEvent)
	 */
	public void treeCollapsed(TreeExpansionEvent event) {
		TreePath path = event.getPath();
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) path
				.getLastPathComponent();

		if (n.getUserObject() instanceof ITocItem) {
			ITocItem item = (ITocItem) n.getUserObject();
			Boolean b = Boolean.FALSE;

//			System.out.println("Collapsed: " + item.getLabel());
			m_ItemsExpanded.put(item.getLabel(), b);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.TreeExpansionListener#treeExpanded(javax.swing.event
	 * .TreeExpansionEvent)
	 */
	public void treeExpanded(TreeExpansionEvent event) {
		TreePath path = event.getPath();
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) path
				.getLastPathComponent();

		if (n.getUserObject() instanceof ITocItem) {
			ITocItem item = (ITocItem) n.getUserObject();
			Boolean b = Boolean.TRUE;

//			System.out.println("Expanded: " + item.getLabel());
			m_ItemsExpanded.put(item.getLabel(), b);
		}
	}

	/**
	 * Obtiene el JScrollPane que contiene el TOC
	 * 
	 * @return JScrollPane que contiene el TOC
	 */
	/*
	 * public JScrollPane getJScrollPane() { return this.m_Scroller; }
	 */

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	/*
	 * public DnDJTree getTree() { return m_Tree; }
	 */

	/**
	 * Clase Listener que reacciona al pulsar sobre el checkbox de un nodo y
	 * crea un popupmenu al pulsar el botón derecho.
	 */
	class NodeSelectionListener extends MouseAdapter implements ActionListener {

		JTree tree;
		JDialog dlg;
		JColorChooser colorChooser;
		FPopupMenu popmenu = null;
		DefaultMutableTreeNode activeNode;
		TOC thisToc = null;

		/**
		 * Crea un nuevo NodeSelectionListener.
		 * 
		 * @param tree
		 *            DOCUMENT ME!
		 */
		NodeSelectionListener(JTree tree, TOC toc) {
			this.tree = tree;
			thisToc = toc;
		}

		public FLayer getLastClickedLayer() {

			if ((activeNode != null)
					&& (activeNode.getUserObject() instanceof TocItemBranch)) {
				TocItemBranch tib = (TocItemBranch) activeNode.getUserObject();
				return tib.getLayer();
			} else {
				return null;
			}
		}

		public void setLastClickedLayer(FLayer lyr) {
			if (lyr == null) {
				setActiveNode(null);
			} else {
				setActiveNode(new DefaultMutableTreeNode(lyr));
			}
		}

		/**
		 * Convenience method. Active node will be internally set via this
		 * method.
		 * 
		 * @param n
		 */
		private void setActiveNode(DefaultMutableTreeNode n) {
			notifyActiveLayerChange(activeNode, n);
			activeNode = n;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * @param e
		 *            DOCUMENT ME!
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			int row = tree.getRowForLocation(x, y);
			TreePath path = tree.getPathForRow(row);
			LayerCollection layers = mapContext.getLayers();

			// System.out.println(e.getSource());
			if (path != null) {
				if (e.getClickCount() == 1) {
					// this fixes a bug when double-clicking. JTree by default
					// expands the tree when double-clicking, so we capture a
					// different node in the second click than in the first
					setActiveNode((DefaultMutableTreeNode) path
							.getLastPathComponent());
				}

				// System.out.println("Evento de ratón originado por " +
				// e.getSource().getClass().toString());
				if ((activeNode != null)
						&& (activeNode.getUserObject() instanceof TocItemBranch)) {
					// double click with left button ON A BRANCH/NODE (layer)
					if ((e.getClickCount() >= 2)
							&& (e.getButton() == MouseEvent.BUTTON1)) {
						e.consume();
						PluginServices.getMDIManager().setWaitCursor();
						try {
							TocItemBranch layer_node = (TocItemBranch) activeNode
									.getUserObject();

							// ================== notify layer action
							// ================
							if ((layer_node != null)
									&& (layer_node.getLayer() != null)) {
								notifyLayerActionEvent(layer_node.getLayer());
							}
							// ========================================================

							IContextMenuAction action = layer_node
									.getDoubleClickAction();
							if (action != null) {
								/*
								 * if there is an action associated with the
								 * double-clicked element it will be fired for
								 * it and FOR ALL OTHER COMPATIBLES THAT HAVE
								 * BEEN ACTIVATED.
								 */
								ArrayList<FLayer> targetLayers = new ArrayList<FLayer>();

								TocItemBranch owner = (TocItemBranch) activeNode
										.getUserObject();

								FLayer masterLayer = owner.getLayer();
								targetLayers.add(masterLayer);
								FLayer[] actives = mapContext.getLayers()
										.getActives();
								for (int i = 0; i < actives.length; i++) {
									if (actives[i].getClass().equals(
											masterLayer.getClass())) {
										if ((actives[i] instanceof FLyrVect)
												&& actives[i].isAvailable()) {
											FLyrVect vectorLayer = (FLyrVect) actives[i];
											FLyrVect vectorMaster = (FLyrVect) masterLayer;
											if (vectorLayer.getShapeType() == vectorMaster
													.getShapeType()) {
												targetLayers.add(vectorLayer);
											} else {
												vectorLayer.setActive(false);
											}
										}
										// TODO for the rest of layer types
										// (i.e. FLyrRaster)
									} else {
										actives[i].setActive(false);
									}
								}
								action.execute(layer_node,
										targetLayers.toArray(new FLayer[0]));
							}
						} catch (Exception ex) {
							NotificationManager.addError(ex);
						} finally {
							PluginServices.getMDIManager().restoreCursor();
						}
						return;
					}

					TocItemBranch elTema = (TocItemBranch) activeNode
							.getUserObject();
					FLayer lyr = elTema.getLayer();
					lyr.getMapContext().beginAtomicEvent();

					if (((e.getModifiers() & InputEvent.SHIFT_MASK) != 0)
							&& (e.getButton() == MouseEvent.BUTTON1)) {
						FLayer[] activeLayers = layers.getActives();
						if (activeLayers.length > 0) {
							selectInterval(layers, lyr);
						} else {
							updateActive(lyr, !lyr.isActive());
						}

					} else {
						if (!((e.getModifiers() & InputEvent.CTRL_MASK) != 0)
								&& (e.getButton() == MouseEvent.BUTTON1)) {
							layers.setAllActives(false);
						}
						if (e.getButton() == MouseEvent.BUTTON1) {
							// lyr.setActive(true);
							updateActive(lyr, !lyr.isActive());
						}
					}
					// Si pertenece a un grupo, lo ponemos activo también.
					// FLayer parentLayer = lyr.getParentLayer();

					/*
					 * if (parentLayer != null) { parentLayer.setActive(true); }
					 */
					Point layerNodeLocation = tree.getUI()
							.getPathBounds(tree, path).getLocation();

					// Rectángulo que representa el checkbox
					Rectangle checkBoxBounds = m_TocRenderer
							.getCheckBoxBounds();
					checkBoxBounds.translate((int) layerNodeLocation.getX(),
							(int) layerNodeLocation.getY());

					if (checkBoxBounds.contains(e.getPoint())) {
						updateVisible(lyr);
					}

					// }
					if (e.getButton() == MouseEvent.BUTTON3) {
						// Boton derecho sobre un nodo del arbol
						// if ((e.getModifiers() & InputEvent.META_MASK) != 0) {
						popmenu = new FPopupMenu(thisToc, mapContext,
								activeNode);
						tree.add(popmenu);

						popmenu.show(e.getComponent(), e.getX(), e.getY());

					}

					lyr.getMapContext().endAtomicEvent();
				}

				if ((activeNode != null)
						&& (activeNode.getUserObject() instanceof TocItemLeaf)) {
					// double click with left button ON A LEAF (ISymbol)
					if ((e.getClickCount() >= 2)
							&& (e.getButton() == MouseEvent.BUTTON1)) {
						e.consume();

						PluginServices.getMDIManager().setWaitCursor();
						try {
							TocItemLeaf leaf = (TocItemLeaf) activeNode
									.getUserObject();
							TocItemBranch owner = (TocItemBranch) ((DefaultMutableTreeNode) activeNode
									.getParent()).getUserObject();
							FLayer masterLayer = owner.getLayer();

							// ======= trigger legend action event
							// =======================
							if (masterLayer instanceof FLyrVect) {
								ILegend le = ((FLyrVect) masterLayer)
										.getLegend();
								notifyLegendActionEvent(leaf.getSymbol(), le);
							}
							// ===========================================================

							IContextMenuAction action = leaf
									.getDoubleClickAction();
							if (action != null) {
								/*
								 * if there is an action associated with the
								 * double-clicked element it will be fired for
								 * it and FOR ALL OTHER COMPATIBLES THAT HAVE
								 * BEEN ACTIVATED.
								 */
								ArrayList<FLayer> targetLayers = new ArrayList<FLayer>();

								targetLayers.add(masterLayer);
								FLayer[] actives = mapContext.getLayers()
										.getActives();

								if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
									masterLayer.setActive(true);
								} else {
									for (int i = 0; i < actives.length; i++) {
										actives[i].setActive(false);
									}
									masterLayer.setActive(true);
								}

								actives = mapContext.getLayers().getActives();

								for (int i = 0; i < actives.length; i++) {
									if (actives[i].getClass().equals(
											masterLayer.getClass())) {
										if (actives[i] instanceof FLyrVect) {
											FLyrVect vectorLayer = (FLyrVect) actives[i];
											FLyrVect vectorMaster = (FLyrVect) masterLayer;
											int masterLayerShapetypeOF_THE_LEGEND = ((IVectorLegend) vectorMaster
													.getLegend())
													.getShapeType();
											int anotherVectorLayerShapetypeOF_THE_LEGEND = ((IVectorLegend) vectorLayer
													.getLegend())
													.getShapeType();
											if (masterLayerShapetypeOF_THE_LEGEND == anotherVectorLayerShapetypeOF_THE_LEGEND) {
												targetLayers.add(vectorLayer);
											} else {
												vectorLayer.setActive(false);
											}
										}
										// TODO for the rest of layer types
										// (i.e. FLyrRaster)
									} else {
										actives[i].setActive(false);
									}
								}
								action.execute(leaf,
										targetLayers.toArray(new FLayer[0]));
							}
						} catch (Exception ex) {
							NotificationManager.addError(ex);
						} finally {
							PluginServices.getMDIManager().restoreCursor();
						}
						return;
					}

					// Boton derecho sobre un Simbolo
					// TocItemLeaf auxLeaf = (TocItemLeaf) node.getUserObject();
					// FSymbol theSym = auxLeaf.getSymbol();
					if ((e.getModifiers() & InputEvent.META_MASK) != 0) {

						TocItemBranch owner = (TocItemBranch) ((DefaultMutableTreeNode) activeNode
								.getParent()).getUserObject();

						FLayer masterLayer = owner.getLayer();
						FLayer[] actives = mapContext.getLayers().getActives();

						if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
							masterLayer.setActive(true);
						} else {
							for (int i = 0; i < actives.length; i++) {
								actives[i].setActive(false);
							}
							masterLayer.setActive(true);
						}

						popmenu = new FPopupMenu(thisToc, mapContext,
								activeNode);
						tree.add(popmenu);
						popmenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}

				((DefaultTreeModel) tree.getModel()).nodeChanged(activeNode);

				if (row == 0) {
					tree.revalidate();
					tree.repaint();
				}

				if (PluginServices.getMainFrame() != null) {
					PluginServices.getMainFrame().enableControls();
				}
			} else {

				thisToc.setActiveLayer(null);

				if (e.getButton() == MouseEvent.BUTTON3) {
					popmenu = new FPopupMenu(thisToc, mapContext, null);
					tree.add(popmenu);
					popmenu.show(e.getComponent(), e.getX(), e.getY());
				}

			}
		}

		private void selectInterval(LayerCollection layers, FLayer lyr) {
			FLayer[] activeLayers = layers.getActives();
			// if (activeLayers[0].getParentLayer() instanceof FLayers &&
			// activeLayers[0].getParentLayer().getParentLayer()!=null) {
			// selectInterval((LayerCollection)activeLayers[0].getParentLayer(),lyr);
			// }
			for (int j = 0; j < layers.getLayersCount(); j++) {
				FLayer layerAux = layers.getLayer(j);
				// Si se cumple esta condición es porque la primera capa que nos
				// encontramos en el TOC es la que estaba activa
				if (activeLayers[0].equals(layerAux)) {
					boolean isSelected = false;
					for (int i = 0; i < layers.getLayersCount(); i++) {
						FLayer layer = layers.getLayer(i);
						if (!isSelected) {
							isSelected = layer.isActive();
						} else {
							updateActive(layer, true);
							if (lyr.equals(layer)) {
								isSelected = false;
							}
						}
					}
					break;
				} else
				// Si se cumple esta condición es porque la primera capa que
				// nos encontramos en el TOC es la que acabamos de
				// seleccionar
				if (lyr.equals(layerAux)) {
					boolean isSelected = false;
					for (int i = layers.getLayersCount() - 1; i >= 0; i--) {
						FLayer layer = layers.getLayer(i);
						if (!isSelected) {
							isSelected = layer.isActive();
						} else {
							updateActive(layer, true);
							if (lyr.equals(layer)) {
								isSelected = false;
							}
						}
					}
					break;
				}
			}

		}

		/**
		 * DOCUMENT ME!
		 * 
		 * @param lyr
		 *            DOCUMENT ME!
		 * @param active
		 *            DOCUMENT ME!
		 */
		private void updateActive(FLayer lyr, boolean active) {
			lyr.setActive(active);
			updateActiveChild(lyr);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * @param lyr
		 *            DOCUMENT ME!
		 */
		private void updateActiveChild(FLayer lyr) {
			if (lyr instanceof FLayers) { // Es la raiz de una rama o
				// cualquier nodo intermedio.

				FLayers layergroup = (FLayers) lyr;

				for (int i = 0; i < layergroup.getLayersCount(); i++) {
					layergroup.getLayer(i).setActive(lyr.isActive());
					updateActiveChild(layergroup.getLayer(i));
				}
			}
		}

		/**
		 * Actualiza la visibilidad de la capas.
		 * 
		 * @param lyr
		 *            Capa sobre la que se está clickando.
		 */
		private void updateVisible(FLayer lyr) {
			if (lyr.isAvailable()) {
				lyr.setVisible(!lyr.visibleRequired());
				updateVisibleChild(lyr);
				updateVisibleParent(lyr);
				// refresh view treak
				MapContext mc = lyr.getMapContext();
				mc.callLegendChanged();
			}
		}

		/**
		 * Actualiza de forma recursiva la visibilidad de los hijos de la capa
		 * que se pasa como parámetro.
		 * 
		 * @param lyr
		 *            Capa a actualizar.
		 */
		private void updateVisibleChild(FLayer lyr) {
			if (lyr instanceof FLayers) { // Es la raiz de una rama o
											// cualquier nodo intermedio.

				FLayers layergroup = (FLayers) lyr;

				for (int i = 0; i < layergroup.getLayersCount(); i++) {
					layergroup.getLayer(i).setVisible(lyr.visibleRequired());
					updateVisibleChild(layergroup.getLayer(i));
				}
			}
		}

		/**
		 * Actualiza de forma recursiva la visibilidad del padre de la capa que
		 * se pasa como parámetro.
		 * 
		 * @param lyr
		 *            Capa a actualizar.
		 */
		private void updateVisibleParent(FLayer lyr) {
			FLayers parent = lyr.getParentLayer();

			if (parent != null) {
				boolean parentVisible = false;

				for (int i = 0; i < parent.getLayersCount(); i++) {
					if (parent.getLayer(i).visibleRequired()) {
						parentVisible = true;
					}
				}

				parent.setVisible(parentVisible);
				updateVisibleParent(parent);
			}
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * @param arg0
		 *            DOCUMENT ME!
		 */
		public void actionPerformed(ActionEvent arg0) {
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * @param arg0
		 *            DOCUMENT ME!
		 */
		@Override
		public void mouseReleased(MouseEvent arg0) {
			super.mouseReleased(arg0);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * @param arg0
		 *            DOCUMENT ME!
		 */
		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			super.mouseEntered(arg0);

			// FJP: COMENTO ESTO.
			// LO CORRECTO CREO QUE ES IMPLEMENTAR CORRECTAMENTE
			// LOS METODOS DE DRAG AND DROP

			/*
			 * if (m_Root.getChildCount()==0){ m_Tree.dropRoot(m_Root); }
			 */
		}
	}

	@Override
	public String getName() {
		return getFactory().getName();
	}

	// ====================================================
	// ====================================================
	// ====================================================

	public FLayer[] getSelectedLayers() {

		FLayer[] resp = null;
		if (mapContext == null) {
			resp = new FLayer[0];
		} else {
			ArrayList lis = EventUtil
					.getAllLayers(mapContext.getLayers(), true);
			resp = (FLayer[]) lis.toArray(new FLayer[0]);
		}
		return resp;
	}

	public void hideAll() {
		int n = m_Tree.getRowCount();
		while (n >= 0) {
			m_Tree.collapseRow(n);
			n--;
		}
	}

	public void show(FLayer lyr, boolean detailed) {

		makeTocVisibleAreaIncludeLayer(lyr);
		if (detailed) {
			TreePath tp = EventUtil.getPath(lyr, m_Root.getUserObject());
			if (tp != null) {
				m_Tree.expandPath(tp);
			}
		}
	}

	public void showAll() {
		setExpandedNodes(m_Root);
	}

	public void addTocOrderListener(ITocOrderListener l) {
		m_Tree.addOrderListener(l);
	}

	public void removeTocOrderListener(ITocOrderListener l) {
		m_Tree.removeOrderListener(l);
	}

	// =================================
	// =================================
	// =================================

	public void addActiveLayerChangeListener(
			ActiveLayerChangeEventListener alcel) {
		if (!activeLayerChangeListeners.contains(alcel)) {
			activeLayerChangeListeners.add(alcel);
		}
	}

	public void addLegendActionListener(LegendActionEventListener lel) {
		if (!legendActionListeners.contains(lel)) {
			legendActionListeners.add(lel);
		}
	}

	public void clearSelection() {
		m_Tree.clearSelection();
	}

	public FLayer getActiveLayer() {
		return nodeSelectionListener.getLastClickedLayer();
	}

	public TOCFactory getFactory() {
		return tocFactory;
	}

	public DynObject getParameters() {
		DynObject resp = tocFactory.createParameters();
		resp.setDynValue(TOCFactory.PARAMETER_MAPCONTEXT_KEY, getMapContext());
		return resp;
	}

	public void hideAllLegends() {
		ArrayList lis = EventUtil.getAllLayers(mapContext.getLayers(), false);
		FLayer lyr = null;
		for (int n = 0; n < lis.size(); n++) {
			lyr = (FLayer) lis.get(n);
			if (!(lyr instanceof FLayers)) {
				hideLegend(lyr);
			}
		}
	}

	public void hideLegend(FLayer lyr) {
		TreePath tp = EventUtil.getPath(lyr, m_Root.getUserObject());
		if (tp != null) {
			m_Tree.collapsePath(tp);
		}
	}

	public void invokeAction(FLayer lyr, String action) {

		List<TOCAction> l = null;

		try {
			l = TOCLocator.getInstance().getTOCManager().getActions(this);
		} catch (ServiceException e) {
			NotificationManager.addError(e);
			return;
		}
		TOCAction ta = null;
		Object name = null;

		ActionEvent ae = new ActionEvent(lyr, -1, "");

		Iterator<TOCAction> iter = l.iterator();
		while (iter.hasNext()) {
			ta = iter.next();
			name = ta.getValue(Action.NAME);
			if ((name != null) && (name instanceof String)
					&& (action.compareToIgnoreCase((String) name) == 0)) {
				ta.actionPerformed(ae);
				return;
			}
		}

		logger.warn("Action invoked not found in TOC manager: " + action
				+ ". Nothing done.");
	}

	public void removeActiveLayerChangeListener(
			ActiveLayerChangeEventListener alcel) {
		this.activeLayerChangeListeners.remove(alcel);
	}

	public void removeLegendActionListener(LegendActionEventListener lel) {
		legendActionListeners.remove(lel);
	}

	public void selectLayer(FLayer lyr) {
		TreePath tp = EventUtil.getPath(lyr, m_Root.getUserObject());
		if (tp != null) {
			m_Tree.setSelectionPath(tp);
		}
	}

	public void setActiveLayer(FLayer lyr) {
		nodeSelectionListener.setLastClickedLayer(lyr);
	}

	public void showAllLegends() {
		ArrayList lis = EventUtil.getAllLayers(mapContext.getLayers(), false);
		FLayer lyr = null;
		for (int n = 0; n < lis.size(); n++) {
			lyr = (FLayer) lis.get(n);
			if (!(lyr instanceof FLayers)) {
				showLegend(lyr);
			}
		}
	}

	public void showLegend(FLayer lyr) {
		TreePath tp = EventUtil.getPath(lyr, m_Root.getUserObject());
		if (tp != null) {
			m_Tree.expandPath(tp);
		}
	}

	public void unselectLayer(FLayer lyr) {
		TreePath tp = EventUtil.getPath(lyr, m_Root.getUserObject());
		if (tp != null) {
			m_Tree.removeSelectionPath(tp);
		}
	}

	public Manager getManager() {
		return TOCLocator.getInstance().getTOCManager();
	}

	// ================================

	private void notifyLegendActionEvent(ISymbol sym, ILegend leg) {

		LegendActionEvent lae = new LegendActionEvent(this, sym, leg);
		LegendActionEventListener lael = null;
		for (int i = 0; i < legendActionListeners.size(); i++) {
			lael = (LegendActionEventListener) legendActionListeners.get(i);
			lael.legendAction(lae);
		}
	}

	private void notifyActiveLayerChange(DefaultMutableTreeNode old_n,
			DefaultMutableTreeNode new_n) {

		FLayer old_lyr = null;
		if ((old_n != null) && (old_n.getUserObject() instanceof TocItemBranch)) {
			old_lyr = ((TocItemBranch) old_n.getUserObject()).getLayer();
		}

		FLayer new_lyr = null;
		if ((new_n != null) && (new_n.getUserObject() instanceof TocItemBranch)) {
			new_lyr = ((TocItemBranch) new_n.getUserObject()).getLayer();
		}

		ActiveLayerChangeEvent alce = new ActiveLayerChangeEvent(this, old_lyr,
				new_lyr);
		ActiveLayerChangeEventListener alcel = null;
		for (int i = 0; i < activeLayerChangeListeners.size(); i++) {
			alcel = (ActiveLayerChangeEventListener) activeLayerChangeListeners
					.get(i);
			alcel.activeLayerChanged(alce);
		}

	}

	public void addLayerActionListener(LayerActionEventListener lel) {
		if (!layerActionListeners.contains(lel)) {
			layerActionListeners.add(lel);
		}
	}

	public void removeLayerActionListener(LayerActionEventListener lel) {
		layerActionListeners.remove(lel);
	}

	private void notifyLayerActionEvent(FLayer lyr) {

		LayerActionEvent lae = new LayerActionEvent(this, lyr);
		LayerActionEventListener lael = null;
		for (int i = 0; i < layerActionListeners.size(); i++) {
			lael = (LayerActionEventListener) layerActionListeners.get(i);
			lael.layerActionPerformed(lae);
		}
	}

}
