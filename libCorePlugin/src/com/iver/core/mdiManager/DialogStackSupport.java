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

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JDialog;
import com.iver.andami.ui.mdiFrame.MDIFrame;


public class DialogStackSupport{

    /** Pila de diálogos modales mostrados */
    private ArrayList<JDialog> dialogStack = new ArrayList<JDialog>(0);

    private ArrayList<Cursor> dialogCursors = new ArrayList<Cursor>(0);

    public DialogStackSupport(MDIFrame frame){
    }

    public void pushDialog(JDialog dlg){
    	dialogStack.add(dlg);
    }

    public JDialog popDialog(){
    	int dialogStackSize=dialogStack.size();
    	if (dialogStackSize<1)
    		return null;
    	return dialogStack.remove(dialogStackSize - 1);
    }


	public void setWaitCursor() {
        dialogCursors.clear();
		for (Iterator<JDialog> iter = dialogStack.iterator(); iter.hasNext();) {
			JDialog dlg = iter.next();
            dialogCursors.add(dlg.getCursor());
			dlg.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 	        dlg.getGlassPane().setVisible(true);
 	   }
	}


	public void restoreCursor() {
		for (Iterator<JDialog> iter = dialogStack.iterator(); iter.hasNext();) {
			JDialog dlg = iter.next();

            // TODO: RECUPERAR EL CURSOR
            dlg.setCursor(null);
		    dlg.getGlassPane().setVisible(false);
		}
	}
}
