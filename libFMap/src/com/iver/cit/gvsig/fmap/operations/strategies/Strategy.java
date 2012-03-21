/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
package com.iver.cit.gvsig.fmap.operations.strategies;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.BitSet;

import javax.print.attribute.PrintRequestAttributeSet;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.utiles.swing.threads.Cancellable;
import com.iver.utiles.swing.threads.CancellableMonitorable;


/**
 * Interfaz estrategia.
 */
public interface Strategy {
	/**
	 * Dibuja la capa vectorial asociada al Strategy en la imagen que se pasa
	 * como par�metro.
	 *
	 * @param image
	 * @param g
	 * @param viewPort
	 * @param cancel
	 * @throws ReadDriverException TODO
	 */
	void draw(BufferedImage image, Graphics2D g, ViewPort viewPort,
		Cancellable cancel) throws ReadDriverException;

	/**
	 * Dibujar� esta Shape en el Graphics con el s�mbolo que se pasa como
	 * par�metro y despues de aplicarle la transformaci�n que se pasa tambi�n
	 * como par�metro. Dibujar� la geometria en caso de que la IGeometry
	 * intersecte o est� contenida en el rect�ngulo que se pasa como par�metro
	 *
	 * @param g
	 * @param viewPort
	 * @param cancel
	 * @throws ReadDriverException TODO
	 */
	void print(Graphics2D g, ViewPort viewPort, Cancellable cancel, PrintRequestAttributeSet properties)
		throws ReadDriverException;


	/**
	 * Similar to next one, but allows cancelations.
	 * @param visitor
	 * @param subset
	 * @param cancel
	 * @throws ReadDriverException TODO
	 * @throws ExpansionFileReadException
	 * @throws VisitorException TODO
	 */
	void process(FeatureVisitor visitor, BitSet subset, CancellableMonitorable cancel)
	throws ReadDriverException, ExpansionFileReadException, VisitorException;
	/**
	 * Recorre las features de la capa vectorial invocando el m�todo visit del
	 * visitador que se pasa como par�metro, que es el que realizar� la
	 * operaci�n relacionada con la geometry
	 *
	 * @param visitor
	 * @param subset
	 * @throws ReadDriverException TODO
	 * @throws ExpansionFileReadException
	 * @throws VisitorException TODO
	 */
	void process(FeatureVisitor visitor, BitSet subset)
		throws ReadDriverException, ExpansionFileReadException, VisitorException;

	/**
	 * Similar to process(FeatureVisitor) but allows cancelations.
	 * @param visitor
	 * @param cancel
	 * @throws ReadDriverException TODO
	 * @throws VisitorException TODO
	 */
	void process(FeatureVisitor visitor, CancellableMonitorable cancel) throws ReadDriverException, VisitorException;
	/**
	 * Recorre las features de la capa vectorial invocando el m�todo visit del
	 * visitador que se pasa como par�metro, que es el que realizar� la
	 * operaci�n relacionada con la geometry
	 *
	 * @param visitor
	 * @throws ReadDriverException TODO
	 * @throws VisitorException TODO
	 */
	void process(FeatureVisitor visitor) throws ReadDriverException, VisitorException;


	/**
	 * Similar to process(FeatureVisitor, Rectangle2D) allowing cancelations.
	 * @param visitor
	 * @param rectangle
	 * @param cancel
	 * @throws ReadDriverException TODO
	 * @throws ExpansionFileReadException
	 * @throws VisitorException TODO
	 */
	void process(FeatureVisitor visitor,
			Rectangle2D rectangle, CancellableMonitorable cancel) throws ReadDriverException, ExpansionFileReadException, VisitorException;
	/**
	 * Iterates features of a vectorial layer whose geometry would be within
	 * Rectangle2D specified, and it calls visit method of visitor.
	 * @param visitor
	 * @param rectangle
	 * @throws ReadDriverException TODO
	 * @throws ExpansionFileReadException
	 * @throws VisitorException TODO
	 */
	void process(FeatureVisitor visitor,
			Rectangle2D rectangle) throws ReadDriverException, ExpansionFileReadException, VisitorException;



	/**
	 * Realiza una query por punto en coordenadas del mundo real de la capa
	 * vectorial asociada a la estrategia
	 *
	 * @param p
	 * @param tolerance
	 *
	 * @return BitSet con los �ndices de los registros de la repuesta a la
	 * 		   consulta.
	 * @throws ReadDriverException TODO
	 * @throws VisitorException TODO
	 */
	FBitSet queryByPoint(Point2D p, double tolerance) throws ReadDriverException, VisitorException;

	/**
	 * Similar to previous method, but allowing cancelations.
	 * @param p
	 * @param tolerance
	 * @param cancel
	 * @return
	 * @throws ReadDriverException TODO
	 * @throws VisitorException TODO
	 */
	FBitSet queryByPoint(Point2D p, double tolerance, CancellableMonitorable cancel) throws ReadDriverException, VisitorException;

	/**
	 * Realiza una query por rect�ngulo en coordenadas del mundo real de la
	 * capa vectorial asociada a la estrategia
	 *
	 * @param rect Rect�ngulo sobre el que hacer la consulta.
	 *
	 * @return BitSet con los �ndices de los registros.
	 * @throws ReadDriverException TODO
	 * @throws VisitorException TODO
	 */
	FBitSet queryByRect(Rectangle2D rect) throws ReadDriverException, VisitorException;

	/**
	 * Allowing cancelations.
	 * @param rect
	 * @param cancel
	 * @return
	 * @throws ReadDriverException TODO
	 * @throws VisitorException TODO
	 */
	FBitSet queryByRect(Rectangle2D rect, CancellableMonitorable cancel)
		throws ReadDriverException, VisitorException;

	/**
	 * Realiza una query por IGeometry en coordenadas del mundo real de la capa
	 * vectorial asociada a la estrategia
	 *
	 * @param g
	 * @param relationship
	 *
	 * @return BitSet con los �ndices de los registros.
	 * @throws ReadDriverException TODO
	 * @throws VisitorException TODO
	 */
	FBitSet queryByShape(IGeometry g, int relationship)
		throws ReadDriverException, VisitorException;

	/**
	 * Allowing cancelations
	 * @param g
	 * @param relationship
	 * @param cancel
	 * @return
	 * @throws ReadDriverException TODO
	 * @throws VisitorException TODO
	 */
	FBitSet queryByShape(IGeometry g, int relationship, CancellableMonitorable cancel)
	throws ReadDriverException, VisitorException;

	/**
	 * Obtiene el rect�ngulo m�nimo que contiene todas las features
	 * seleccionadas o null si no hay ninguna seleccionada
	 *
	 * @return Devuelve el extent de la consulta.
	 */
	Rectangle2D getSelectionBounds();

	/**
	 * Crea un �ndice sobre la capa vectorial de la estrategia
	 */
	void createIndex();


}
