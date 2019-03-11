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
package com.iver.cit.gvsig.fmap.operations.strategies;

import com.hardcode.gdbms.driver.exceptions.InitializeDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.layerOperations.SingleLayer;
import com.iver.utiles.swing.threads.CancellableMonitorable;


/**
 * Simple strategy intended to just traverse the features
 */
public class SimpleStrategy extends DefaultStrategy {

	/**
	 * Creates a new SimpleStrategy.
	 *
	 * @param capa
	 */
	public SimpleStrategy(FLayer capa) {
        super(capa);
	}

	public void process(FeatureVisitor visitor, CancellableMonitorable cancel) throws ReadDriverException, VisitorException {
		try {
//			logger.info("visitor.start()");

			if (visitor.start(capa)) {
				ReadableVectorial va = ((SingleLayer) capa).getSource();
				va.start();
				for (int i = 0; i < va.getShapeCount(); i++) {
					if(cancel != null){
						cancel.reportStep();
					}
					if(verifyCancelation(cancel, va, visitor))
						return;
				    IGeometry geom = va.getShape(i);
				    if (geom == null) {
						continue;
					}

					visitor.visit(geom, i);
				}
				va.stop();
//				logger.info("visitor.stop()");
				visitor.stop(capa);
			}
		} catch (ExpansionFileReadException e) {
			throw new ReadDriverException(getCapa().getName(),e);
		} catch (InitializeDriverException e) {
			throw new ReadDriverException(getCapa().getName(),e);
		}
	}

}
