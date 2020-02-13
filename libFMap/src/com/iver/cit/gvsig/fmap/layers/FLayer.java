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
package com.iver.cit.gvsig.fmap.layers;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.ImageIcon;

import org.cresques.cts.ICoordTrans;
import org.cresques.geo.Projected;
import org.gvsig.exceptions.BaseException;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.exceptions.layers.ReloadLayerException;
import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.layers.layerOperations.ComposedLayer;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;

/**
 * <p>Definition of the basic functionality that all kind of <i>FMap</i> layers should implement.</p>
 *
 * <p>This kind of layers store their data, are drawable, projectable (with a projection), can be a node of a tree of layers, and
 *  could be editable and have a cache with previous draws. They also can be visible or not, and active or not.</p>
 *
 * <p>A layer can also store information about errors produced working with it, and have a name (kind of layer) and
 *  a brief summary explaining what's for.</p>
 *
 * <p>Each particular implementation can add new properties, and limit or expand the functionality.</p>
 *
 * @see Projected
 */
public interface FLayer extends Projected {
	/**
	 * <p>Returns an entity that represents this layer.</p>
	 *
	 * <p>This XML entity has elements that represent and store information about this layer.</p>
	 *
	 * @return an XML entity with information of this layer
	 * @throws com.iver.cit.gvsig.fmap.layers.XMLException if there is an error obtaining the object.
	 *
	 * @see #setXMLEntity(XMLEntity)
	 */
	XMLEntity getXMLEntity() throws XMLException;

	/**
	 * <p>Inserts information to this layer from an XML entity.</p>
	 *
	 * <p>This XML entity has elements that represent and store information about this layer.</p>
	 *
	 * @param xml an <code>XMLEntity</code> with the information
	 *
	 * @throws com.iver.cit.gvsig.fmap.layers.XMLException if there is an error setting the object.
	 *
	 * @see #getXMLEntity()
	 */
	void setXMLEntity(XMLEntity xml) throws XMLException;

	/**
	 * <p>Changes the status of this layer to active or inactive.</p>
	 * <p>One layer is active if is selected in TOC.</p>
	 *
	 * @param selected the boolean to be set
	 *
	 * @see #isActive()
	 */
	void setActive(boolean selected);

	/**
	 * <p>Returns if this layer is active or not in TOC.</p>
	 * <p>One layer is active if is selected in TOC.</p>
	 *
	 * @return <code>true</code> if this layer is active; <code>false</code> otherwise
	 *
	 * @see #setActive(boolean)
	 */
	boolean isActive();

	/**
	 * Sets a name to this layer.
	 *
	 * @param name the string that is to be this layer's name
	 *
	 * @see #getName()
	 */
	void setName(String name);

	/**
	 * Returns the name of this layer.
	 *
	 * @return an string with this layer's name
	 *
	 * @see #setName(String)
	 */
	String getName();

	/**
	 * <p>Executes the initialization operations of this layer. This method is invoked
	 * only one time during the life of this layer and just before visualize it.</p>
	 *
	 * @throws com.iver.cit.gvsig.fmap.drivers.DriverIOException if fails loading the layer.
	 *
	 * @see #reload()
	 */
	void load() throws LoadLayerException;
	/**
	 * <p>Changes the status of this layer to visible or not.</p>
	 * <p>One layer is visible if it's check box associated is selected. This means
	 *  that layer will tried to be painted. If the data associated isn't available,
	 *  then this property will change to <code>false</code>.</p>
	 *
	 * @param visibility the boolean to be set
	 *
	 * @see #isVisible()
	 * @see #visibleRequired()
	 * @see #isAvailable()
	 */
	void setVisible(boolean visibility);

	/**
	 * <p>Returns if this layer is visible and available.</p>
	 * <p>One layer is visible if it's check box associated is selected. This means
	 *  that layer will tried to be painted.</p>
	 * <p>One layer is available if the source of data is on-line.</p>
	 * <p>It's probably that one layer selected hadn't available it's data, for example
	 *  in a remote service.</p>
	 *
	 * @return <code>true</code> if this layer is visible and available; <code>false</code> otherwise
	 *
	 * @see #isAvailable()
	 * @see #setAvailable(boolean)
	 * @see #visibleRequired()
	 */
	boolean isVisible();

	/**
	 * Returns the parent {@link FLayers FLayers} node of this layer.
	 *
	 * @return the parent of this layer, or <code>null</code> if hasn't parent
	 *
	 * @see #setParentLayer(FLayers)
	 */
	public FLayers getParentLayer();

	/**
	 * <p>Returns a reference to the model of this layer, or null if this layer has no model.</p>
	 *
	 * @return the model of this layer
	 */
	public MapContext getMapContext();

	/**
	 * Inserts the parent {@link FLayers FLayers} node of the layer.
	 *
	 * @param root a <code>FLayers</code> object
	 *
	 * @see #getParentLayer()
	 */
	public void setParentLayer(FLayers root);
	/**
	 * Returns the full extension of the layer node.
	 *
	 * @return location and dimension of this layer node
	 *
	 * @throws com.iver.cit.gvsig.fmap.DriverException if fails the driver used in this method.
	 */
	Rectangle2D getFullExtent() throws ReadDriverException, ExpansionFileReadException;

	/**
	 * Draws the layer using a buffer.
	 *
	 * @param image an image used to accelerate the screen draw
	 * @param g for rendering 2-dimensional shapes, text and images on the Java(tm) platform
	 * @param viewPort information for drawing this layer
	 * @param cancel an object thread that implements the <code>Cancellable</code> interface, and will allow to cancel the draw
	 * @param scale value that represents the scale
	 *
	 * @throws com.iver.cit.gvsig.fmap.DriverException if fails the driver used in this method.
	 *
	 * @see #print(Graphics2D, ViewPort, Cancellable, double, PrintRequestAttributeSet)
	 */
	void draw(BufferedImage image, Graphics2D g, ViewPort viewPort,
			Cancellable cancel,double scale) throws ReadDriverException;

	/**
	 * Prints this layer according to some properties requested.
	 *
	 * @param g for rendering 2-dimensional shapes, text and images on the Java(tm) platform
	 * @param viewPort the information for drawing the layers
	 * @param cancel an object thread that implements the {@link Cancellable Cancellable} interface, and will allow to cancel the draw
	 * @param scale the scale of the view. Must be between {@linkplain FLayer#getMinScale()} and {@linkplain FLayer#getMaxScale()}.
	 * @param properties a set with the settings to be applied to a whole print job and to all the docs in the print job
	 *
	 * @throws com.iver.cit.gvsig.fmap.DriverException if fails the driver used in this method.
	 *
	 * @see #draw(BufferedImage, Graphics2D, ViewPort, Cancellable, double)
	 */
	void print(Graphics2D g, ViewPort viewPort, Cancellable cancel, double scale, PrintRequestAttributeSet properties)
	throws ReadDriverException;

	/**
	 * Inserts the transformation coordinates.
	 *
	 * @param ct transformation coordinates
	 *
	 * @see #getCoordTrans()
	 */
	void setCoordTrans(ICoordTrans ct);

	/**
	 * Returns the transformation coordinates.
	 *
	 * @return transformation coordinates
	 *
	 * @see #setCoordTrans(ICoordTrans)
	 */
	ICoordTrans getCoordTrans();

	/**
	 * Adds a <code>LayerListener</code> to the listener list.
	 *
	 * @param o a layer listener
	 *
	 * @return <code>true</code> if hasn't been any problem during the insertion of the listener
	 *
	 * @see #getLayerListeners()
	 * @see #removeLayerListener(LayerListener)
	 */
	public boolean addLayerListener(LayerListener o);
	/**
	 * Returns all <code>LayerListener</code>s of this layer as an array.
	 *
	 * @return an array with all layer listeners associated to this layer
	 *
	 * @see #addLayerListener(LayerListener)
	 * @see #removeLayerListener(LayerListener)
	 */
	public LayerListener[] getLayerListeners();
	/**
	 * Removes the <code>LayerListener</code> argument from this layer.
	 *
	 * @param o a layer listener
	 *
	 * @return <code>true</code> if hasn't been any problem doing this process
	 *
	 * @see #addLayerListener(LayerListener)
	 * @see #getLayerListeners()
	 */
	public boolean removeLayerListener(LayerListener o);
	/**
	 * <p>Returns if the value of <code>scale</code> argument
	 *  is within the maximum and minimum scale of this layer.</p>
	 *
	 * @param scale the scale > 0
	 *
	 * @return <code>true</code> if the <code>scale</code> argument is within the range of scales of this layer; <code>false</code> otherwise
	 *
	 * @see #setMinScale(double)
	 * @see #setMaxScale(double)
	 */
	public boolean isWithinScale(double scale);


	/**
	 * Returns the minimum scale visible. Lower scales won't be drawn.
	 *
	 * @return the minimum scale > 0, -1 if not defined
	 *
	 * @see #setMinScale(double)
	 */
	public double getMinScale();

	/**
	 * Returns the maximum scale visible. Higher scales won't be drawn.
	 *
	 * @return the maximum scale > 0, -1 if not defined
	 *
	 * @see #setMaxScale(double)
	 */
	public double getMaxScale();
	/**
	 * Sets the minimum scale visible. Lower scales won't be drawn.
	 *
	 * @param minScale the scale > 0, -1 if not defined
	 *
	 * @see #getMinScale()
	 */
	public void setMinScale(double minScale);
	/**
	 * Sets the maximum scale visible. Higher scales won't be drawn.
	 *
	 * @param maxScale the scale > 0, -1 if not defined
	 *
	 * @see #getMaxScale()
	 */
	public void setMaxScale(double maxScale);
	/**
	 * <p>Changes the status of this layer to editable or not.</p>
	 * <p>One layer is editable if user can modify its information with graphical tools.</p>
	 *
	 * @param b the boolean to be set
	 *
	 * @throws com.iver.cit.gvsig.fmap.edition.EditionException if fails enabling for edition this kind of layer.
	 *
	 * @see #isEditing()
	 */
	public void setEditing(boolean b) throws StartEditionLayerException;
	/**
	 * <p>Returns if this layer is editable.</p>
	 * <p>One layer is editable if user can modify its information with graphical tools.</p>
	 *
	 * @return <code>true</code> if this layer is editable; <code>false</code> otherwise
	 *
	 * @see #setEditing(boolean)
	 */
	public boolean isEditing();

	/**
	 * Returns the image icon that will be shown in the TOC next to this layer.
	 *
	 * @return a reference to the image icon, or <code>null</code> if there isn't any
	 */
	public ImageIcon getTocImageIcon();

	/**
	 * <p>Returns if this layer appears in the TOC.</p>
	 * <p>If doesn't appears, remains in the view and in the project.</p>
	 *
	 * @return <code>true</code> if this layer appears in the TOC; <code>false</code> otherwise
	 */
	boolean isInTOC();
	/**
	 * <p>Sets that this layer appears or not in the TOC.</p>
	 *
	 * @param b <code>true</code> if appears in the TOC; <code>false</code> otherwise
	 */
	void setInTOC(boolean b);
	/**
	 * Returns the status of this layer.
	 *
	 * @return the status stored in a <code>FLayerStatus</code> object
	 *
	 * @see #setFLayerStatus(FLayerStatus)
	 */
	public FLayerStatus getFLayerStatus();
	/**
	 * Sets the status of this layer.
	 *
	 * @param status information of the status for this layer
	 *
	 * @see #getFLayerStatus()
	 */
	public void setFLayerStatus(FLayerStatus status);
	/*
	 * This stuff is to save error's info that causes
	 * unavailable status.
	 * */
	/**
	 * <p>Returns if this layer hasn't got errors.</p>
	 *
	 * @return <code>true</code> if this layer hasn't got errors; <code>false</code> otherwise
	 */
	public boolean isOk();
	/**
	 * Returns the number of errors which causes this layer to be in unavailable status.
	 *
	 * @return number of errors >= 0
	 *
	 * @see #getError(int)
	 * @see #getErrors()
	 * @see #addError(DriverException)
	 */
	public int getNumErrors();

	/**
	 * Returns the specified error.
	 *
	 * @param i index of the error >= 0 && < <code>getNumErrors</code>
	 *
	 * @return a singular error
	 *
	 * @see #getNumErrors()
	 * @see #getErrors()
	 */
	public BaseException getError(int i);

	/**
	 * Adds an error reason that describes this layer's wrong status.
	 *
	 * @param error a <code>DriverException</code> with the information of the error
	 *
	 * @see #getNumErrors()
	 * @see #getError(int)
	 * @see #getErrors()
	 */
	public void addError(BaseException exception);

	/**
	 * Returns a list with all layer errors.
	 *
	 * @return an <code>ArrayList</code> with the errors
	 *
	 * @see #getError(int)
	 * @see #getNumErrors()
	 */
	public List getErrors();
	/**
	 * <p>Changes the status of availability of this layer.</p>
	 * <p>One layer is available if the source of data is on-line.</p>
	 *
	 * @param the boolean to be set
	 *
	 * @see #isAvailable()
	 */
	public void setAvailable(boolean available);
	/**
	 * <p>Returns the status of availability of this layer.</p>
	 * <p>One layer is available if the source of data is on-line.</p>
	 *
	 * @return <code>true</code> if the source of data is on-line; <code>false</code> otherwise
	 *
	 * @see #setAvailable(boolean)
	 * @see #isVisible()
	 */
	public boolean isAvailable();
	/**
	 * <p>Tries recover a layer of a possible error.</p>
	 * <p>If it has any problem during the load, marks the availability
	 *  to false and throws an exception.</p>
	 *
	 * @throws DriverIOException if it's thrown a <code>DriverException</code> or an <code>IOException</code> during the load of this layer.
	 *
	 * @see #load()
	 */
	public void reload() throws ReloadLayerException;

	/**
	 * Returns <code>true</code> if this layer has the visible status enabled.
	 *
	 * @return <code>true</code> if visible this layer has the visible status enabled, otherwise <code>false</code>
	 *
	 * @see #isVisible()
	 * @see #setVisible(boolean)
	 */
	boolean visibleRequired();
	/**
	 * Returns an string with the information of this layer.
	 *
	 * @return the string that is to be this component's information
	 */
	public String getInfoString();
	/**
	 * <p>Returns the writing status of this layer.</p>
	 * <p>One layer is writable if there is a writing driver for this layer.</p>
	 *
	 * @return <code>true</code> if there is a writing driver for this layer; <code>false</code> otherwise
	 */
	public boolean isWritable();

	/**
	 * <p>This method can be used to have a fast cloned layer.</p>
	 * <p>The implementations should take care of not recreate this layer. Instead of this,
	 *  is better to use the same source (driver) and <i>deepclone</i> the legend. Exception:
	 *   the labels aren't <i>deepcloned</i> to avoid memory consumption.</p>
	 * <p><i>Note</i>: Labels are memory consuming to speed up layers like PostGIS and so on.</p>
	 *
	 * @return a layer that is a clonation of this layer
	 *
	 * @throws java.lang.Exception any exception produced during the cloning of this layer.
	 */
	public FLayer cloneLayer() throws Exception;



	/**
	 * <p>Returns a reference to an object (property) associated to this layer.</p>
	 *
	 * <p>For example, you can attach a network definition to key "network" and check
	 *  if a layer has a network loaded using <i>getAssociatedObject("network")</i> and
	 *  that it's not null.</p>
	 *
	 * @param key the key associated to the property
	 *
	 * @return <code>null</code> if key is not found
	 *
	 * @see #getExtendedProperties()
	 * @see #setProperty(Object, Object)
	 */
	public Object getProperty(Object key);

	/**
	 * Insets an object as a property to this layer.
	 *
	 * @param key the key associated to the property
	 * @param obj the property
	 *
	 * @see #getProperty(Object)
	 * @see #getExtendedProperties()
	 */
	public void setProperty(Object key, Object obj);
	/**
	 * Returns a hash map with all new properties associated to this layer.
	 *
	 * @return hash table with the added properties
	 *
	 * @see #getProperty(Object)
	 * @see #setProperty(Object, Object)
	 */
	public Map getExtendedProperties();

	/**
	 * <p>Returns a new instance of {@link ComposedLayer ComposedLayer}.</p>
	 *
	 * <p>This allows make a single draw for a group
	 * of layers with the same source.</p>
	 *
	 * <p>If this operation isn't applicable for this
	 * kind of layer, this method returns null.</p>
	 *
	 * <p>By default this operation is not supported.</p>
	 *
	 * @see com.iver.cit.gvsig.fmap.layers.layerOperations.ComposedLayer
	 *
	 * @return a new composed layer or <code>null</code> if not supported
	 */
	public ComposedLayer  newComposedLayer();

	/**
	 * Returns the image icon that will be shown in the TOC next to this layer, according its status.
	 *
	 * @return the image
	 */
	Image getTocStatusImage();

//	Métodos para la utilización de HyperLinks

	/**
	 * Returns information about if the layer allows HyperLink or not
	 * @return boolean true if allows Link, false if not
	 */
	public boolean allowLinks();

	/**
	 * Returns an instance of AbstractLinkProperties that contains the information
	 * of the HyperLink
	 * @return Abstra
	 */
	public AbstractLinkProperties getLinkProperties();

	/**
	 * Provides an array with URIs. Returns one URI by geometry that includes the point
	 * in its own geometry limits with a allowed tolerance.
	 * @param layer, the layer
	 * @param point, the point to check that is contained or not in the geometries in the layer
	 * @param tolerance, the tolerance allowed. Allowed margin of error to detect if the  point
	 * 		is contained in some geometries of the layer
	 * @return
	 */
	public URI[] getLink(Point2D point, double tolerance);



	public long getDrawVersion();

}
