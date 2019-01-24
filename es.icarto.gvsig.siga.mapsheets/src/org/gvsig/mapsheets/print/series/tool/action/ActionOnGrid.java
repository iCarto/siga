package org.gvsig.mapsheets.print.series.tool.action;

/**
 * Interface for undo-able actions performed on the grid.
 *  
 * @author jldominguez
 *
 */
public interface ActionOnGrid {
	
	public static final int GRID_ACTION_ADD = 0;
	public static final int GRID_ACTION_DELETE = 0;
	public static final int GRID_ACTION_MOVE = 0;
	
	public int getType();
	public boolean undo();

}
