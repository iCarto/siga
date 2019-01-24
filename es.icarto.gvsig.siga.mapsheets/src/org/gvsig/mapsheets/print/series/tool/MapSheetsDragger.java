package org.gvsig.mapsheets.print.series.tool;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGrid;
import org.gvsig.mapsheets.print.series.fmap.MapSheetGridGraphic;
import org.gvsig.mapsheets.print.series.fmap.RemoveSelectedException;
import org.gvsig.mapsheets.print.series.tool.action.ActionAdd;
import org.gvsig.mapsheets.print.series.tool.action.ActionDelete;
import org.gvsig.mapsheets.print.series.tool.action.ActionMove;
import org.gvsig.mapsheets.print.series.tool.action.ActionOnGrid;
import org.gvsig.mapsheets.print.series.utils.MapSheetsUtils;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.AtomicEvent;
import com.iver.cit.gvsig.fmap.AtomicEventListener;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.SelectionSupport;
import com.iver.cit.gvsig.fmap.tools.BehaviorException;
import com.iver.cit.gvsig.fmap.tools.Behavior.DraggerBehavior;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.utiles.swing.threads.DefaultCancellableMonitorable;

/**
 * Behavior associated with the adjustment tool deals with
 * mouse/keyboard events, and keeps undo-able actions heap.
 * 
 * @author jldominguez
 *
 */
public class MapSheetsDragger extends DraggerBehavior implements
AtomicEventListener, KeyEventDispatcher  {
	
	private static Logger logger = Logger.getLogger(MapSheetsDragger.class);
	
	public static final String MAP_SERIES_SET_GRID_TOOL_ID = "MAP_SERIES_SET_GRID_TOOL_ID";

	private static final long DOUBLE_CLICK_MILISECS = 200;
	
	private MapSheetGrid theGrid = null;
	// private ArrayList rectsGeoms = new ArrayList();
	
	private static Image img = null;
	private static Cursor cur = null;
	private MapSheetGridGraphic movingGraphic = null; 
	private IGeometry sampleGeom = null; 
	
	private Point2D mouseCoordsInSheetSpace = null;
	private Point2D mouseDownMapCoords = new Point2D.Double();
	
	private boolean ctrlWhenMouseDown = false;
	
	private long lastMouseDown = 0;
	private String prevTool = null;
	private View parentView = null;
	
	static {
		img = Toolkit.getDefaultToolkit().getImage(
				MapSheetsDragger.class.getClassLoader().getResource("images" + File.separator + "movepage.png"));
		cur = Toolkit.getDefaultToolkit().createCustomCursor(img,
				new Point(15, 15), "");
	}
	
	public MapSheetsDragger(View comp) {
		super(new EmptyPanListener());
		parentView = comp;
		
		// comp.getMapControl().getMapContext().a
	
		KeyStroke controlz = KeyStroke.getKeyStroke(KeyEvent.VK_U, 0);
		PluginServices.unRegisterKeyStroke(controlz);
		PluginServices.registerKeyStroke(controlz, this);
		
	}
	

	private ArrayList actionHeap = new ArrayList();
	private ActionOnGrid thisAction = null;
	
	private boolean pushAction(ActionOnGrid a) {
		
		if (a != null) {
			actionHeap.add(0, a);
			return true;
		} else {
			return false;
		}
		
	}
	
	private boolean pushAction() {
		
		if (thisAction != null) {
			actionHeap.add(0, thisAction);
			thisAction = null;
			return true;
		} else {
			return false;
		}
		
	}


	private ActionOnGrid popAction() {
		if (actionHeap.size() > 0) {
			return (ActionOnGrid) actionHeap.remove(0);
		} else {
			return null;
		}
	}
	
	



	public void mousePressed(MouseEvent e) {
		
		if (e.isControlDown()) {
			ctrlWhenMouseDown = true;
		} else {
			ctrlWhenMouseDown = false;
		}

		long curr = System.currentTimeMillis();
		if ((curr - lastMouseDown) < DOUBLE_CLICK_MILISECS) {
			lastMouseDown = 0;
			ctrlWhenMouseDown = false;
			processDoubleClick(e, e.isControlDown());
			return;
		} else {
			lastMouseDown = curr;
		}
		
		Object[] geo_ind = null;
		
		try {
			geo_ind = findSheet(e.getPoint());
		} catch (Exception ex) {
			NotificationManager.addError("While searching sheet in mouse coords. ", ex);
			return;
		}
		
		
		if (geo_ind[0] != null && e.isControlDown()) {
			Integer indi = (Integer) geo_ind[1];
			MapSheetsUtils.reverseSelection(getGrid(), indi.intValue());
			ctrlWhenMouseDown = false;
			getMapControl().repaint();
			// done
			return;
		} else {
			
			Point2D scr_orig = new Point2D.Double(e.getX(), e.getY());
			
			mouseDownMapCoords = getMapControl().getViewPort().toMapPoint(
					(int) scr_orig.getX(),
					(int) scr_orig.getY());

			
			if (geo_ind[0] != null) {
				// geom yes, ctrl no => drag this 
				movingGraphic = (MapSheetGridGraphic) geo_ind[0];
				mouseCoordsInSheetSpace =
					screenCoordsToRectCoords(
							scr_orig,
							movingGraphic.getGeom().getBounds2D());
				// update sample geom
				sampleGeom = movingGraphic.getGeom().cloneGeometry(); 
			} else {
				// ctrl yes => drag selected (geom doesnt matter)
				// will drag all selected
				if (getGrid().getSelectionSupport().getSelection().length() > 0) {
					int frst = getGrid().getSelectionSupport().getSelection().nextSetBit(0);
					IGeometry frst_shp = null;
					try {
						frst_shp = getGrid().getTheMemoryDriver().getShape(frst);
					} catch (ReadDriverException exc) {
						NotificationManager.addError("While getting first sel geom. ", exc);
						ctrlWhenMouseDown = false;
					} 
					mouseCoordsInSheetSpace =
						screenCoordsToRectCoords(scr_orig, frst_shp.getBounds2D());
				} else {
					// nothing because no selection
					ctrlWhenMouseDown = false;
				}
			}
		}

	}
	
	private void processDoubleClick(MouseEvent e, boolean ctrl) {
		
		// IGeometry double_click_geom = findSheet();
		Point p = e.getPoint(); 
		Point2D mapp = getMapControl().getViewPort().toMapPoint(
				(int) p.getX(),
				(int) p.getY());
		
		MapSheetGridGraphic rmved = null;
		MapSheetGridGraphic[] rr = null;
		boolean rmv_done = false;
		ActionOnGrid ade = null;
		
		try {
			rmved = theGrid.removeGraphicContaining(mapp);
			rmv_done = (rmved != null);
			if (rmved != null) {
				// =========== action: remove ===========================
				rr = new MapSheetGridGraphic[1];
				rr[0] = rmved;
				ade = new ActionDelete(rr, theGrid);
				this.pushAction(ade);
				// ======================================================
			}
			// rmved is null if no graphic contains point 
		} catch (RemoveSelectedException ex) {
			
			int user_op = JOptionPane.showConfirmDialog(
					parentView,
					PluginServices.getText(this, "Remove_selected_question") + " (" + ex.getN() + ")",
					PluginServices.getText(this, "Remove"),
					JOptionPane.WARNING_MESSAGE);
			if (user_op == JOptionPane.YES_OPTION) {
				
				rr = theGrid.removeSelected();
				// =============== action: remove several =================
				ade = new ActionDelete(rr, theGrid);
				this.pushAction(ade);
				// ========================================================
			}
			rmv_done = true;
			
		} catch (Exception ex) {
			NotificationManager.addError(ex);
		}
		
		if (!rmv_done) {
			
			// not found: add
			if (sampleGeom != null) {
				
				IGeometry new_rect = copySampleGeometryCentered(mapp);
				try {
					HashMap ats = theGrid.getCommonAtts();
					
					String new_code = getIdFromUser(theGrid); // theGrid.createNewCode();
					if (new_code == null) {
						// cancel
						return;
					}
					
					ats.put(MapSheetGrid.ATT_NAME_CODE,
							ValueFactory.createValue(new_code));
					
					rr = new MapSheetGridGraphic[1];
					rr[0] = theGrid.addSheet(new_rect, ats);
					// ================== action: add ====================
					ade = new ActionAdd(rr, theGrid);
					this.pushAction(ade);
					// ====================================================
				} catch (Exception ex) {
					NotificationManager.addError(ex);
				}
			} else {
				logger.error("sampleGeom is NULL (?)");
			}
		}
		getMapControl().repaint();
		ctrlWhenMouseDown = false;
		
	}

	private String getIdFromUser(MapSheetGrid gri) throws Exception {

		boolean ini = true;
		String resp = "";
		while (ini || !(resp == null || MapSheetsUtils.validCode(gri, resp))) {
			
			if (!ini && resp != null) {
				JOptionPane.showMessageDialog(
						parentView,
						PluginServices.getText(this, "Id_already_exists"),
						PluginServices.getText(this, "Bad_input"),
						JOptionPane.ERROR_MESSAGE);
			}
			
			ini = false;
			resp = JOptionPane.showInputDialog(parentView,
					PluginServices.getText(this, "Enter_new_id"));
		}
		return resp;
	}

	private IGeometry copySampleGeometryCentered(Point2D mapp) {
		
		double currx = sampleGeom.getBounds2D().getCenterX();
		double curry = sampleGeom.getBounds2D().getCenterY();
		double offx = mapp.getX() - currx;
		double offy = mapp.getY() - curry;
		
		AffineTransform at = AffineTransform.getTranslateInstance(offx, offy);
		IGeometry ig = sampleGeom.cloneGeometry();
		ig.transform(at);
		return ig;
	}

	private Point2D screenCoordsToRectCoords(Point2D scr_p, Rectangle2D r) {

		Point2D map_po = getMapControl().getViewPort().toMapPoint(
				(int) scr_p.getX(),
				(int) scr_p.getY());
		return new Point2D.Double(
				map_po.getX() - r.getMinX(),
				map_po.getY() - r.getMinY());
	}
	
	public void mouseDragged(MouseEvent e) throws BehaviorException {
		
		// prevent drag inside double click
		lastMouseDown = 0;
		
		if (ctrlWhenMouseDown) {
			dragSheet(null, e);
			getMapControl().repaint();
		} else {
			if (movingGraphic != null) {
				dragSheet(movingGraphic, e);
				getMapControl().repaint();
			}
		}
		
	}
	
	/**
	 * 
	 * @param ig null means drag selected geoms
	 * @param ev
	 */
	private void dragSheet(MapSheetGridGraphic grfc, MouseEvent ev) {
		
		Point2D map_po = getMapControl().getViewPort().toMapPoint(
				(int) ev.getX(),
				(int) ev.getY());
		
		Point2D new_sheets_xy = new Point2D.Double(
				map_po.getX() - mouseCoordsInSheetSpace.getX(),
				map_po.getY() - mouseCoordsInSheetSpace.getY());
		
		Point2D off_from_start = new Point2D.Double(
				map_po.getX() - mouseDownMapCoords.getX(),
				map_po.getY() - mouseDownMapCoords.getY());
			
		
		MapSheetGridGraphic[] gg = null;
		
		if (grfc != null) {
			relMove(grfc, new_sheets_xy);
			gg = new MapSheetGridGraphic[1];
			gg[0] = grfc;
			// done, last line in this method will update thisAction
		} else {

			Point2D new_sheets_xy_aux = null;
			
			SelectionSupport ss = getGrid().getSelectionSupport();
	    	FBitSet fbs = ss.getSelection();
	    	MapSheetGridGraphic item_grf = null;
	    	
	    	boolean is_first = true;
	    	Rectangle2D first_bb = null;
	    	ArrayList sel_gg = new ArrayList();
	    	
	    	for(int i=fbs.nextSetBit(0); i>=0; i=fbs.nextSetBit(i+1)) {
	    		try {
					item_grf = getGrid().getTheMemoryDriver().getGraphic(i);
	    			if (is_first) {
	    				first_bb = item_grf.getGeom().getBounds2D(); 
						relMove(item_grf, new_sheets_xy);
						
						is_first = false;
	    			} else {
	    				
	    				new_sheets_xy_aux = addRelPos(
	    						new_sheets_xy,
	    						item_grf.getGeom().getBounds2D(),
	    						first_bb);
	    				relMove(item_grf, new_sheets_xy_aux);
	    			}
	    			
	    			// add sel grafs
	    			sel_gg.add(item_grf);
	    			
				} catch (Exception e) {
					NotificationManager.addError("Wile getting geom/graphic grom grid. ", e);
				}
	    	}
	    	gg = (MapSheetGridGraphic[]) sel_gg.toArray(new MapSheetGridGraphic[0]);
		}
		
		// last line updates thisAction
		updateThisActionMove(gg, off_from_start);
		
	}
	
	
	private void updateThisActionMove(MapSheetGridGraphic[] gg, Point2D offs) {
		thisAction = new ActionMove(gg, offs);
	}

	private Point2D addRelPos(
			Point2D new_sheets_xy,
			Rectangle2D t_bb,
			Rectangle2D s_bb) {
		
		Point2D resp = new Point2D.Double(
				new_sheets_xy.getX() + t_bb.getMinX() - s_bb.getMinX(), 
				new_sheets_xy.getY() + t_bb.getMinY() - s_bb.getMinY());
		return resp;
	}

	private void relMove(MapSheetGridGraphic grf, Point2D new_xy) {
		double currx = grf.getGeom().getBounds2D().getMinX();
		double curry = grf.getGeom().getBounds2D().getMinY();
		double offx = new_xy.getX() - currx;
		double offy = new_xy.getY() - curry;
		
		AffineTransform at = AffineTransform.getTranslateInstance(offx, offy);
		grf.getGeom().transform(at);
	}
	
	
	public void mouseReleased(MouseEvent e) {
		pushAction();
		movingGraphic = null;
		ctrlWhenMouseDown = false;
	}
	
	
	public Cursor getCursor() {
		return cur;
	}
	
	public void paintComponent(Graphics g) {
		BufferedImage img = getMapControl().getImage();

		if (img != null) {
			g.drawImage(img, 0, 0, null);
			drawRects(g);
		}
	}

	private void drawRects(Graphics g) {
		
		ArrayList rectsGeoms = theGrid.getGeometries(false);
		
		if (sampleGeom == null && rectsGeoms.size() > 0) {
			sampleGeom = (IGeometry) rectsGeoms.get(0);
		}

		ArrayList tags = theGrid.getCodes();
		int len = 0;

		if (rectsGeoms != null) {
			len = rectsGeoms.size();
			Color old_color = g.getColor();
			Font old_font = g.getFont();
			Font used_font = old_font.deriveFont(1.75f * old_font.getSize());
			
			HashMap selIndexes = MapSheetsUtils.getSelectedIndicesHM(getGrid());

			// ===============================================
			// selected, non-selected
			// ===============================================
			boolean is_sel = false;
			for (int i=0; i<len; i++) {
				
				FPolygon2D poly = (FPolygon2D) ((IGeometry) rectsGeoms.get(i)).getInternalShape();
				poly = (FPolygon2D) poly.cloneFShape();
				AffineTransform at = getMapControl().getViewPort().getAffineTransform(); 
				
				poly.transform(at);
				is_sel = (null != selIndexes.get(new Integer(i)));
				MapSheetsUtils.getFrameSymbol(true, is_sel).draw((Graphics2D) g,
						at, poly, new DefaultCancellableMonitorable()); 
			}
			// ===============================================
			// tags
			// ===============================================
			g.setColor(Color.RED.darker());
			g.setFont(used_font);
			
			for (int i=0; i<len; i++) {
				
				FPolygon2D poly = (FPolygon2D) ((IGeometry) rectsGeoms.get(i)).getInternalShape();
				poly = (FPolygon2D) poly.cloneFShape();
				AffineTransform at = getMapControl().getViewPort().getAffineTransform(); 
				poly.transform(at);
				drawTag((Graphics2D) g, poly.getBounds(), (String) tags.get(i));
			}
			
			g.setColor(old_color);
			g.setFont(old_font);
		}
	}


	private void drawTag(Graphics2D g, Rectangle bb, String str) {

		Point rel_pos = MapSheetsUtils.getPosFor(g, bb, str);
		
		if (rel_pos != null) {
			g.drawString(str, rel_pos.x, rel_pos.y);
		}
		
		
	}

	public MapSheetGrid getGrid() {
		return theGrid;
	}

	public void setGrid(MapSheetGrid gr, boolean clear_undo) {
		// rectsGeoms.clear();
		theGrid = gr;
		if (clear_undo) {
			actionHeap.clear();
		}
	}
	
	private Object[] findSheet(Point p) throws Exception {
		Point2D mapp = getMapControl().getViewPort().toMapPoint((int) p.getX(), (int) p.getY());
		
		Object[] resp = new Object[2];

		int n = theGrid.getTheMemoryDriver().getShapeCount();
		MapSheetGridGraphic item = null;
		for (int i=0; i<n; i++) {
			item = theGrid.getTheMemoryDriver().getGraphic(i);
			if (item.getGeom().contains(mapp)) {
				resp[0] = item;
				resp[1] = new Integer(i);
				break;
			}
		}
		return resp;
	}

	public int getSheetCount() {
		try {
			return theGrid.getTheMemoryDriver().getShapeCount();
		} catch (Exception ex) {
			NotificationManager.addError(ex);
			return 0;
		}
		
	}

	private MapContext mapCtxt = null;
	public void setListener(MapContext mx) {
		
		if (mapCtxt != null) {
			mapCtxt.removeAtomicEventListener(this);
		}
		
		if (mx != null) {
			mapCtxt = mx;
			mx.addAtomicEventListener(this);
		}
	}

	public void atomicEvent(AtomicEvent e) {
		
		LaterTask lt = new LaterTask(theGrid, prevTool);
		SwingUtilities.invokeLater(lt);

	}

	public void setPreviousTool(String t) {
		prevTool = t;
	}
	
	private class LaterTask implements Runnable {
		
		private MapSheetGrid gr = null;
		private String ptool = null;
		
		public LaterTask(MapSheetGrid grid, String prev_tool) {
			gr = grid;
			ptool = prev_tool;
		}

		public void run() {
			
	        try {
	            IWindow w = PluginServices.getMDIManager().getActiveWindow();

	            if (w instanceof View) {
	                View v = (View) w;
	                MapControl mc = v.getMapControl();
	                Rectangle2D curext =
	                	MapSheetsUtils.undetectableChange(mc.getViewPort().getAdjustedExtent());
	                MapContext mx = mc.getMapContext();
		        	MapSheetGrid msg = MapSheetsUtils.getActiveMapSheetGrid(mx);
		        	if (msg != null) {
		        		if (msg != gr) {
		        			// changed to a new grid
		        			setGrid(msg, true);
		        			mc.getViewPort().setExtent(curext);
		        			// mc.getViewPort().drawMap(true);
		        		}
		        	} else {
		        		// selection changed: not a grid, change tool
		        		setListener((MapContext) null);
		        		if (ptool != null) {
		        			mc.setTool(ptool);
		        		}
		        		mc.getViewPort().setExtent(curext);
		        		// mc.drawMap(true);
		        	}
	            } else {
	            	// throw new Exception("Windows is not a view");
	            }
	        } catch (Exception ex) {
	        	NotificationManager.addError("While setting adjust tool.", ex);
	        }			
		}
		
	}

	public ArrayList getGeoms(boolean cloned) {
		return theGrid.getGeometries(cloned);
	}

	public boolean dispatchKeyEvent(KeyEvent e) {
		
		if (e.getID() != KeyEvent.KEY_PRESSED) {
			return false;
		}
		if (conditionsApplyToUndo()) {
			
			ActionOnGrid ac = popAction();
			if (ac != null) {
				ac.undo();
				getMapControl().repaint();
			}
			return true;
		} else {
			// JOptionPane.showMessageDialog(parentView, "NO UNDO");
			return false;
		}
	}

	private boolean conditionsApplyToUndo() {
		
		if (actionHeap.size() > 0) {
			if (theGrid.isActiveAsGrid()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	



	
	
}
