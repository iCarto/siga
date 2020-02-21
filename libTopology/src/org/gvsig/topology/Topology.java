/*
 * Created on 07-sep-2007
 *
 * gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
/* CVS MESSAGES:
 *
 * $Id: 
 * $Log: 
 */
package org.gvsig.topology;

import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.topology.topologyrules.MustBeLargerThanClusterTolerance;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.edition.AfterFieldEditEvent;
import com.iver.cit.gvsig.fmap.edition.AfterRowEditEvent;
import com.iver.cit.gvsig.fmap.edition.BeforeFieldEditEvent;
import com.iver.cit.gvsig.fmap.edition.BeforeRowEditEvent;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.IEditionListener;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.layers.CancelationException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerCollectionEvent;
import com.iver.cit.gvsig.fmap.layers.LayerCollectionListener;
import com.iver.cit.gvsig.fmap.layers.LayerEvent;
import com.iver.cit.gvsig.fmap.layers.LayerListener;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.utiles.IPersistence;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.CancellableProgressTask;

/**
 * 
 * This class represents a Topology, as a group of vectorial layers and topology
 * rules that checks geometries and their spatial relationships of these layers.
 * 
 * It extends FLayers to reuse all already existent code related with groups of
 * layers (TOC related, etc.)
 * 
 * TODO Study if we must syncronize writing methods (validate, markAsException,
 * addError, ruleId++, etc)
 * 
 * 
 * @author azabala
 * 
 */
public class Topology extends FLayers implements ITopologyStatus,
		ITopologyErrorContainer, Cloneable {

	private static Logger logger = Logger.getLogger(Topology.class.getName());

	private final static ImageIcon NOT_VALIDATED_TOC_ICON = new ImageIcon(
			Topology.class.getResource("images/topoicon.png"));

	private final static ImageIcon VALIDATED_TOC_ICON = new ImageIcon(
			Topology.class.getResource("images/topoicon_validated.png"));

	private final static ImageIcon VALIDATED_WITH_ERRORS_TOC_ICON = new ImageIcon(
			Topology.class
					.getResource("images/topoicon_validated_with_errors.png"));

	private final static ImageIcon EMPTY_TOC_ICON = new ImageIcon(
			Topology.class.getResource("images/topoicon_empty.png"));

	private final static ImageIcon VALIDATED_WITH_DIRTY_ZONES_TOC_ICON = new ImageIcon(
			Topology.class
					.getResource("images/topoicon_validated_with_dirty_zones.png"));

	private final static ImageIcon VALIDATING_TOC_ICON = new ImageIcon(
			Topology.class.getResource("images/topoicon_validating.png"));

	private ImageIcon statusIcon = EMPTY_TOC_ICON;

	/**
	 * topology name
	 */
	private String name;
	/**
	 * cluster tolerance of the topology
	 */

	private double clusterTolerance;

	/**
	 * validation status of the topology
	 */
	private byte status = EMPTY;

	/**
	 * If during validation process a topoly exceeds this parameters, validation
	 * will be stopped.
	 * 
	 */
	private int maxNumberOfErrors = -1;

	/**
	 * topology rules of the topology
	 */
	private List<ITopologyRule> rules;

	/**
	 * Each layer of a topology must have a cluster tolerance rule
	 */
	private List<MustBeLargerThanClusterTolerance> clusterToleranceRules;

	/**
	 * Regions of the topology to be validated
	 */
	private List<Rectangle2D> dirtyZones;

	/**
	 * Error container in which topology delegates error container
	 * responsability.
	 */
	private ITopologyErrorContainer errorContainer;

	/**
	 * Collection of error layers created from topology errors in topology error
	 * container.
	 */
	private FLayer errorLayer;

	/**
	 * Registered class to listen for change status events.
	 */
	private List<ITopologyStatusListener> statusListeners;

	/**
	 * Map that relates a FLyrVect of the Topology with its rank (weight when we
	 * are going to snap many coordinates)
	 */
//	private Map<FLyrVect, XYZLayerRank> layerRanks;

	/**
	 * Numerical identifier for rules of a topology
	 */
	private int ruleId = 0;

	/**
	 * Interface for all of these classes interested in listening topology
	 * change status events.
	 * 
	 * @author Alvaro Zabala
	 * 
	 */
	public interface ITopologyStatusListener {
		public void statusChange(TopologyStatusEvent event);
	}

	/**
	 * Topology change status event. It contains old and new topology status.
	 * 
	 * @author Alvaro Zabala
	 * 
	 */
	public class TopologyStatusEvent {
		int newStatus;
		int prevStatus;
	}
	
	public Topology(){
		this(null, null );
	}

	/**
	 * Default constructor for a topology as a FLayers
	 * 
	 * @param fmap
	 * @param parent
	 */
	public Topology(MapContext fmap, FLayers parent, double clusterTolerance,
			int numberOfErrors, ITopologyErrorContainer errorContainer) {
		super.setMapContext(fmap);
		super.setParentLayer(parent);
		this.clusterTolerance = clusterTolerance;
		this.maxNumberOfErrors = numberOfErrors;
		this.errorContainer = errorContainer;
		this.errorContainer.setTopology(this);

		rules = new ArrayList<ITopologyRule>();
		clusterToleranceRules = new ArrayList<MustBeLargerThanClusterTolerance>();
		dirtyZones = new ArrayList<Rectangle2D>();
//		layerRanks = new HashMap<FLyrVect, XYZLayerRank>();

		statusListeners = new ArrayList<ITopologyStatusListener>();
		// This listener updates the icon status returneb by this kind of layer
		statusListeners.add(new ITopologyStatusListener() {
			public void statusChange(TopologyStatusEvent event) {
				switch (event.newStatus) {
				case ITopologyStatus.VALIDATED:
					statusIcon = VALIDATED_TOC_ICON;
					break;

				case ITopologyStatus.NOT_VALIDATED:
					statusIcon = NOT_VALIDATED_TOC_ICON;
					break;

				case ITopologyStatus.VALIDATED_WITH_DIRTY_ZONES:
					statusIcon = VALIDATED_WITH_DIRTY_ZONES_TOC_ICON;
					break;

				case ITopologyStatus.VALIDATED_WITH_ERRORS:
					statusIcon = VALIDATED_WITH_ERRORS_TOC_ICON;
					break;

				case ITopologyStatus.VALIDATING:
					statusIcon = VALIDATING_TOC_ICON;
					break;
				}
			}
		});
	}

	public Topology(MapContext mapContext, FLayers layers) {
		this(mapContext, layers, 0d, 1000, new SimpleTopologyErrorContainer());
	}

	public void addStatusListener(ITopologyStatusListener statusListener) {
		this.statusListeners.add(statusListener);
	}

	/**
	 * Creates a topology from its XML representation
	 * 
	 * @param xmlEntity
	 * @return
	 */
	public static Topology createFromXML(MapContext mapContext,
			XMLEntity xmlEntity) {
		FLayers rootLyr = mapContext.getLayers();
		Topology solution = new Topology(mapContext, rootLyr);

		try {
			solution.setXMLEntity(xmlEntity);
			if (solution.getErrorContainer() == null) {
				solution.setErrorContainer(new SimpleTopologyErrorContainer());
			}
		} catch (XMLException e) {
			logger.error("Error al reconstruir la topologia desde fichero xml",
					e);
		}
		return solution;
	}

	public ImageIcon getTocImageIcon() {
		return statusIcon;
	}

	public Image getTocStatusImage() {
		return statusIcon.getImage();
	}

	/**
	 * Changes the cluster tolerance of the topology
	 * 
	 * 
	 * This operation resets the status of the topology (it clears errors and
	 * dirty zones)
	 * 
	 * @param clusterTolerance
	 */
	public void setClusterTolerance(double clusterTolerance) {
		if (status == VALIDATING)
			return;// maybe we could launch an exception??
		this.clusterTolerance = clusterTolerance;
		resetStatus();
		Iterator<MustBeLargerThanClusterTolerance> rulesIt = this.clusterToleranceRules
				.iterator();
		while (rulesIt.hasNext()) {
			MustBeLargerThanClusterTolerance rule = rulesIt.next();
			rule.setClusterTolerance(clusterTolerance);
		}

	}

	public double getClusterTolerance() {
		return clusterTolerance;
	}

	public void resetStatus() {
		setStatus(NOT_VALIDATED);
		this.clear();
		this.dirtyZones.clear();

	}

	/**
	 * Adds a new topology rule to the topology.
	 * 
	 * The layers referenced by the rule must exist in the topology.
	 * 
	 * The state of the topology changes to "NON_VALIDATED", and a new dirty
	 * zone is added for the scope of the new rule (envelope of one or two
	 * layers).
	 * 
	 * 
	 * @throws TopologyRuleDefinitionException
	 */
	public void addRule(ITopologyRule rule) throws RuleNotAllowedException,
			TopologyRuleDefinitionException {

		if (status == VALIDATING)
			throw new RuleNotAllowedException(
					"No se puede añadir una regla si la topologia está siendo validada");

		Rectangle2D ruleScope = null;
		try {
			if (rule instanceof IOneLyrRule) {
				FLyrVect originLyr = ((IOneLyrRule) rule).getOriginLyr();
				if (getLayer(originLyr.getName()) == null) {
					throw new RuleNotAllowedException(
							"Regla con capa  que no forma parte de la topologia");
				}
				ruleScope = originLyr.getFullExtent();
			}
			if (rule instanceof ITwoLyrRule) {
				FLyrVect destLyr = ((ITwoLyrRule) rule).getDestinationLyr();
				if (getLayer(destLyr.getName()) == null) {
					throw new RuleNotAllowedException(
							"Regla con capa  que no forma parte de la topologia");
				}
				if (ruleScope != null)
					ruleScope.add(destLyr.getFullExtent());
			}
		} catch (ExpansionFileReadException e) {
			e.printStackTrace();
			throw new TopologyRuleDefinitionException(e);
		} catch (ReadDriverException e) {
			e.printStackTrace();
			throw new TopologyRuleDefinitionException(e);
		}

		// before to add the rule we check if it verifies preconditions
		rule.checkPreconditions();
		rule.setTopology(this);
		rules.add(rule);

		// Si se añade una nueva regla, no es posible conservar los errores
		// y las zonas sucias previas
		// if(status == EMPTY)
		// status = NOT_VALIDATED;
		// else if(status == VALIDATED ){
		// status = VALIDATED_WITH_DIRTY_ZONES;
		// addDirtyZone(ruleScope);
		// }else if(status == VALIDATED_WITH_ERRORS){
		// //we dont change the status, but add a new dirty zone
		// addDirtyZone(ruleScope);
		// }
		resetStatus();
		rule.setTopologyErrorContainer(this);
		rule.setId(this.ruleId++);
	}

	/*
	 * Overwrited implementations of FLayers methods
	 */

	public void addLayer(FLayer layer) {
		addLayer(layers.size(), layer);
	}

	public void addLayer(int pos, FLayer layer) {
		if (!(layer instanceof FLyrVect))
			throw new WrongLyrForTopologyException(
					"Intentando añadir capa no vectorial a una topologia");
		if(this.layers.contains(layer))
			return;
		if(layer.getMapContext() != null){
			layer.getMapContext().beginAtomicEvent();
		}
		
		FLayers oldParentLayer  = layer.getParentLayer();
		if(oldParentLayer != null)
			oldParentLayer.removeLayer(layer);
		layer.setParentLayer(null);
		super.addLayer(pos, layer);
		if(layer.getMapContext() != null)
		{
			layer.getMapContext().endAtomicEvent();
			layer.getMapContext().invalidate();
		}
		
//		setRank((FLyrVect) layer, 1, 1);

		int shapeType = -1;
		try {
			shapeType = ((FLyrVect) layer).getShapeType();
			if ((shapeType == FShape.POINT) || (shapeType == FShape.MULTIPOINT)
					|| (shapeType == FShape.TEXT))
				return;
		} catch (ReadDriverException e) {
			e.printStackTrace();
			throw new WrongLyrForTopologyException(
					"Error al intentar verificar el tipo de geometria de la capa",
					e);
		}

		MustBeLargerThanClusterTolerance rule = new MustBeLargerThanClusterTolerance(
				this, (FLyrVect) layer, clusterTolerance);
		rule.setId(this.ruleId++);
		Rectangle2D ruleScope;
		try {
			ruleScope = layer.getFullExtent();
			// before to add the rule we check if it verifies preconditions
			rule.checkPreconditions();

			if (status == EMPTY)
				setStatus(NOT_VALIDATED);
			else if (status == VALIDATED) {
				setStatus(VALIDATED_WITH_DIRTY_ZONES);
				addDirtyZone(ruleScope);
			} else if (status == VALIDATED_WITH_ERRORS) {
				// we dont change the status, but add a new dirty zone
				// addDirtyZone(ruleScope);
				// si habia errores, la reevaluacion haria que se repitiesen
				resetStatus();
//				this.errorContainer.clear();
			}
			rule.setTopologyErrorContainer(this);
			clusterToleranceRules.add(rule);

			// finally, we connect edition with topology
			layer.addLayerListener(new LayerListener() {

				public void activationChanged(LayerEvent e) {
				}

				public void editionChanged(LayerEvent e) {
					FLayer editionLyr = e.getSource();

					if (editionLyr instanceof FLyrVect) {

						final ArrayList<Rectangle2D> dirtyZones = new ArrayList<Rectangle2D>();

						FLyrVect fLyrVect = (FLyrVect) editionLyr;

						ReadableVectorial rv = fLyrVect.getSource();
						if (!(rv instanceof VectorialEditableAdapter))
							return;
						final VectorialEditableAdapter vea = (VectorialEditableAdapter) rv;

						vea.addEditionListener(new IEditionListener() {

							public void afterFieldEditEvent(
									AfterFieldEditEvent e) {
							}

							public void afterRowEditEvent(IRow row,
									AfterRowEditEvent e) {
								// If we include alphanumeric rules to a
								// topology as a
								// integrity rule, we'll add a new dirty zone
								// for this too
								if (e.getChangeType() == EditionEvent.ALPHANUMERIC)
									return;
								int numRow = (int) e.getNumRow();

								IGeometry geom;
								try {
									geom = vea.getShape(numRow);
									if (geom != null){
										dirtyZones.add(geom.getBounds2D());
									}
								} catch (ExpansionFileReadException e1) {
									e1.printStackTrace();
								} catch (ReadDriverException e1) {
									e1.printStackTrace();
								}
							}

							public void beforeFieldEditEvent(
									BeforeFieldEditEvent e) {
							}

							public void beforeRowEditEvent(IRow feat,
									BeforeRowEditEvent e) {
							}

							public void processEvent(EditionEvent e) {
								if (e.getChangeType() == EditionEvent.STOP_EDITION) {
									for (int i = 0; i < dirtyZones.size(); i++) {
										Rectangle2D dirtyZone = dirtyZones
												.get(i);
										Topology.this.addDirtyZone(dirtyZone);
									}// for
								}// if
							}
						});
					}
				}

				public void nameChanged(LayerEvent e) {
				}

				public void visibilityChanged(LayerEvent e) {
				}

				public void drawValueChanged(LayerEvent e) {
					// TODO Auto-generated method stub
					
				}
			});

		} catch (ExpansionFileReadException e) {
			e.printStackTrace();
			throw new WrongLyrForTopologyException(
					"No es posible acceder all FullExtent de la capa", e);
		} catch (ReadDriverException e) {
			e.printStackTrace();
			throw new WrongLyrForTopologyException(
					"No es posible acceder all FullExtent de la capa", e);
		} catch (TopologyRuleDefinitionException e) {
			e.printStackTrace();
			throw new WrongLyrForTopologyException(
					"Regla topologica mal definida", e);
		}
	}

	/**
	 * Sets the rank/importance of a layer in xy and z planes.
	 * 
	 * @param lyr
	 *            layer
	 * 
	 * @param xyRank
	 *            importance of this layer coordinates in xy plane
	 * 
	 * @param zRank
	 *            importante of this layer coordinates in z plane
	 */
//	public void setRank(FLyrVect lyr, int xyRank, int zRank) {
//		XYZLayerRank rank = new XYZLayerRank(lyr.getName(), xyRank, zRank);
//		layerRanks.put(lyr, rank);
//	}

//	public XYZLayerRank getRank(FLyrVect lyr) {
//		return layerRanks.get(lyr);
//	}

	/**
	 * Adds a layer to the topology. If the topology has been validated, changes
	 * topology status to NON-VALIDATED and adds a dirty zone equals to the
	 * layer extent.
	 */
	public void addLayer(FLyrVect layer, int xyRank, int zRank) {
		this.addLayer(layer);
//		setRank(layer, xyRank, zRank);
	}

	/**
	 * Remove a layer from a topology.
	 * 
	 * This task is more complex than removing a layer from a LayerCollection:
	 * -must remove all rules which references to this layer. -must recompute
	 * status and dirty zones. etc.
	 * 
	 * TODO Implement remove layer as a geoprocess.
	 * 
	 */
	public void removeLayer(FLayer lyr) throws CancelationException {
//		super.removeLayer(lyr);//bug detectado por Eustaquio
		callLayerRemoving(LayerCollectionEvent.createLayerRemovingEvent(lyr));

		super.removeLayer(lyr);
		// remove cluster rules related with the layer
		Iterator<MustBeLargerThanClusterTolerance> clusterRulesIt = clusterToleranceRules
				.iterator();
		while (clusterRulesIt.hasNext()) {
			MustBeLargerThanClusterTolerance rule = clusterRulesIt.next();
			if (rule.getOriginLyr().equals(lyr)) {
				clusterRulesIt.remove();
			}
		}// while

		// Remove normal rules related with the layer
		Iterator<ITopologyRule> rulesIt = this.rules.iterator();
		while (rulesIt.hasNext()) {
			ITopologyRule rule = rulesIt.next();
			if (rule instanceof IOneLyrRule) {
				IOneLyrRule oneLyrRule = (IOneLyrRule) rule;
				if (oneLyrRule.getOriginLyr().equals(lyr)) {
					rulesIt.remove();
					continue;
				}
			}

			if (rule instanceof ITwoLyrRule) {
				ITwoLyrRule twoLyrRule = (ITwoLyrRule) rule;
				if (twoLyrRule.getOriginLyr().equals(lyr)) {
					rulesIt.remove();
				}
			}
		}// while

		this.errorContainer.removeErrorsByLayer((FLyrVect) lyr);
//		this.layerRanks.remove(lyr);
		this.updateDirtyZones();
		callLayerRemoved(LayerCollectionEvent.createLayerRemovedEvent(lyr));
	}

	public void removeLayer(int idLyr) {
		FLayer lyr = (FLayer) layers.get(idLyr);
		removeLayer(lyr);
	}

	public void removeRule(ITopologyRule rule) {
		if (rules.contains(rule)) {
			rules.remove(rule);
		} else if (clusterToleranceRules.contains(rule)) {
			clusterToleranceRules.remove(rule);
		}
		this.errorContainer.removeErrorsByRule(rule.getName());

		this.updateDirtyZones();
	}

	private void updateDirtyZones() {
		// this.dirtyZones.clear(); //FIXME REVISAR SI ES NECESARIO BORRAR LAS
		// ZONAS SUCIAS
		int errorNum = errorContainer.getNumberOfErrors();
		for (int i = 0; i < errorNum; i++) {
			TopologyError topologyError = errorContainer.getTopologyError(i);
			Rectangle2D rect = topologyError.getGeometry().getBounds2D();
			addDirtyZone(rect);
		}
	}

	/**
	 * Ranks (in xy plane and z plane) for layers of the topology.
	 * 
	 * The rank of layer marks its weight for computing weihgted average
	 * coordinates.
	 * 
	 * @author azabala
	 * 
	 */
	class XYZLayerRank implements IPersistence {
		int xyRank;
		int zRank;
		String layerName;

		XYZLayerRank(String layerName, int xyRank, int zRank) {
			this.layerName = layerName;
			this.xyRank = xyRank;
			this.zRank = zRank;
		}

		XYZLayerRank() {
		}

		public String getClassName() {
			return this.getClass().getName();
		}

		public XMLEntity getXMLEntity() {
			XMLEntity solution = new XMLEntity();
			solution.putProperty("layerName", layerName);
			solution.putProperty("xyRank", xyRank);
			solution.putProperty("zRank", zRank);
			return solution;
		}

		public void setXMLEntity(XMLEntity xml) {
			if (xml.contains("layerName"))
				layerName = xml.getStringProperty("layerName");

			if (xml.contains("xyRank"))
				xyRank = xml.getIntProperty("xyRank");

			if (xml.contains("zRank"))
				zRank = xml.getIntProperty("zRank");

		}
	}

	public void setStatus(byte newStatus) {
		TopologyStatusEvent newStatusEvent = new TopologyStatusEvent();
		newStatusEvent.prevStatus = this.status;
		newStatusEvent.newStatus = newStatus;
		this.status = newStatus;

		fireStatusChange(newStatusEvent);
	}

	public byte getStatus() {
		return status;
	}

	public List<ITopologyRule> getAllRules() {
		List<ITopologyRule> solution = new ArrayList<ITopologyRule>();
		solution.addAll(this.rules);
		solution.addAll(this.clusterToleranceRules);
		return solution;
	}

	/**
	 * Adds a dirty zone to the topology (usually when a feature of a layer of
	 * the topology has been edited)
	 */
	public void addDirtyZone(Rectangle2D newDirtyZone) {
		if (status == NOT_VALIDATED)
			return;
		Iterator<Rectangle2D> zonesIt = dirtyZones.iterator();
		while (zonesIt.hasNext()) {
			Rectangle2D dirtyZone = zonesIt.next();
			if (dirtyZone.contains(newDirtyZone)) {
				return;// we dont add this dirty zone. Its redundant
			}
			if (newDirtyZone.contains(dirtyZone)) {
				zonesIt.remove();
				dirtyZones.add(newDirtyZone);
				return;
			}

			if (dirtyZone.intersects(newDirtyZone)) {
				dirtyZone.add(newDirtyZone);
				return;
			}
		}// while

		if (status == VALIDATED)
			setStatus(VALIDATED_WITH_DIRTY_ZONES);

		// at this point, we add the new dirty zone
		dirtyZones.add(newDirtyZone);
	}

	public void removeDirtyZone(Rectangle2D newDirtyZone) {
		if (status == NOT_VALIDATED)
			return;// maybe we must launch an inconsistent status exception
		Iterator<Rectangle2D> zonesIt = dirtyZones.iterator();
		while (zonesIt.hasNext()) {
			Rectangle2D dirtyZone = zonesIt.next();
			if (newDirtyZone.contains(dirtyZone)) {
				zonesIt.remove();
				continue;
			}
		}// while

		// at this point, we add the new dirty zone
		dirtyZones.remove(newDirtyZone);
	}

	public Rectangle2D getDirtyZone(int i) {
		return dirtyZones.get(i);
	}

	public int getNumberOfDirtyZones() {
		return dirtyZones.size();
	}

	public void validate() {
		validate(null);
	}

	/**
	 * Validates the topology: it validates each topology rule for the given
	 * dirty zones. After the end of the process,
	 */
	public void validate(CancellableProgressTask progressMonitor) {

		if (progressMonitor != null) {
			progressMonitor.setInitialStep(0);
			int numOfSteps = rules.size() + clusterToleranceRules.size();
			progressMonitor.setFinalStep(numOfSteps);
			progressMonitor.setDeterminatedProcess(true);
			progressMonitor.setNote(Messages.getText("Validating_a_topology"));
			progressMonitor.setStatusMessage(Messages.getText(rules.get(0)
					.getName()));
		}

		if (this.status == EMPTY) {
			// TODO Maybe we must do progressMonitor.setFinished(true)
			// or throw an exception
			return;
		} else if (this.status == VALIDATED) {
			return;
		}

		else if (this.status == NOT_VALIDATED) {
			setStatus(VALIDATING);

			// we make a local copy of dirty zones to avoid to use dirty zones
			// created in
			// the current validation.
			ArrayList<Rectangle2D> dirtyZonesCopy = new ArrayList<Rectangle2D>();
			Collections.copy(dirtyZonesCopy, this.dirtyZones);
			Iterator<MustBeLargerThanClusterTolerance> it = clusterToleranceRules
					.iterator();
			while (it.hasNext()) {
				MustBeLargerThanClusterTolerance rule = it.next();
				if (progressMonitor != null) {
					if (progressMonitor.isCanceled()/*
													 * ||
													 * progressMonitor.isFinished()
													 */) {
						resetStatus();
						return;
					}
					progressMonitor.setNote(Messages.getText("VALIDANDO_REGLA")
							+ " " + Messages.getText(rule.getName()));
					progressMonitor.reportStep();
				}

				if (getNumberOfErrors() >= this.maxNumberOfErrors) {
					if (progressMonitor != null)
						progressMonitor.finished();
					setErrorLyr();
					setStatus(VALIDATED_WITH_ERRORS);
					return;
				}

				rule.checkRule(progressMonitor);
			}

			Iterator<ITopologyRule> rulesIt = this.rules.iterator();
			while (rulesIt.hasNext()) {
				ITopologyRule rule = rulesIt.next();

				if (progressMonitor != null) {
					if (progressMonitor.isCanceled()/*
													 * ||
													 * progressMonitor.isFinished()
													 */) {
						resetStatus();
						return;
					}
					progressMonitor.setNote(Messages.getText("VALIDANDO_REGLA")
							+ " " + Messages.getText(rule.getName()));
					progressMonitor.reportStep();
				}

				if (getNumberOfErrors() >= this.maxNumberOfErrors) {
					if (progressMonitor != null)
						progressMonitor.finished();
					setErrorLyr();
					setStatus(VALIDATED_WITH_ERRORS);
					return;
				}

				if (dirtyZonesCopy.size() == 0) {
					rule.checkRule(progressMonitor);
				} else {
					// A topology is NON_VALIDATED with dirty zones when
					// it has VALIDATED status and we add a new rule.

					// TODO Check to add a new rule to a topology
					// with VALIDATED_WITH_ERROR status
					Iterator<Rectangle2D> dirtyZonesIt = dirtyZonesCopy
							.iterator();
					while (dirtyZonesIt.hasNext()) {
						Rectangle2D rect = dirtyZonesIt.next();
						rule.checkRule(progressMonitor, rect);
					}// while
				}// else
			}// while
			if (this.errorContainer.getNumberOfErrors() > 0) {
				setStatus(VALIDATED_WITH_ERRORS);
				setErrorLyr();
			} else
				setStatus(VALIDATED);
		} else if (this.status == VALIDATED_WITH_ERRORS
				|| this.status == ITopologyStatus.VALIDATED_WITH_DIRTY_ZONES) {
			setStatus(VALIDATING);
			// this topology must have at least one dirty zone
			if (this.dirtyZones.size() < 1) {
				// FIXME Deberiamos lanzar una
				// InconsistentStatusException ??
				return;
			}

			this.errorContainer.clear();

			Iterator<MustBeLargerThanClusterTolerance> it = this.clusterToleranceRules
					.iterator();
			while (it.hasNext()) {
				MustBeLargerThanClusterTolerance rule = it.next();
				if (progressMonitor != null) {
					if (progressMonitor.isCanceled()/*
													 * ||
													 * progressMonitor.isFinished()
													 */) {
						//TODO Si se cancela la validacion de una topologia con 
						//estado VALIDADO CON ERRORES, NO SE DEBE LLAMAR A RESETSTATUS!!!
						//LO QUE HAY QUE HACER ES RESTAURAR EL ESTADO PREVIO
						resetStatus();
						return;
					}
					progressMonitor.setNote(Messages.getText("VALIDANDO_REGLA")
							+ " " + Messages.getText(rule.getName()));
					progressMonitor.reportStep();
				}

				if (getNumberOfErrors() >= this.maxNumberOfErrors) {
					if (progressMonitor != null)
						progressMonitor.finished();
					setErrorLyr();
					setStatus(VALIDATED_WITH_ERRORS);
					return;
				}

				List<Rectangle2D> dirtyZonesCopy = new ArrayList<Rectangle2D>(
						dirtyZones);
				Iterator<Rectangle2D> dirtyZonesIt = dirtyZonesCopy.iterator();
				while (dirtyZonesIt.hasNext()) {
					Rectangle2D dirtyZone = dirtyZonesIt.next();
					rule.checkRule(dirtyZone);
				}
			}// MustBeGreaterThanClusterTolerance

			Iterator<ITopologyRule> rulesIt = this.rules.iterator();
			while (rulesIt.hasNext()) {
				ITopologyRule rule = rulesIt.next();
				if (progressMonitor != null) {
					if (progressMonitor.isCanceled()/*
													 * ||
													 * progressMonitor.isFinished()
													 */) {
						resetStatus();
						return;
					}
					progressMonitor.setNote(Messages.getText("VALIDANDO_REGLA")
							+ " " + Messages.getText(rule.getName()));
					progressMonitor.reportStep();
				}

				if (getNumberOfErrors() >= this.maxNumberOfErrors) {
					if (progressMonitor != null)
						progressMonitor.finished();
					setErrorLyr();
					setStatus(VALIDATED_WITH_ERRORS);
					return;
				}
				List<Rectangle2D> dirtyZonesCopy = new ArrayList<Rectangle2D>(
						dirtyZones);
				Iterator<Rectangle2D> dirtyZonesIt = dirtyZonesCopy.iterator();
				while (dirtyZonesIt.hasNext()) {
					Rectangle2D dirtyZone = dirtyZonesIt.next();
					rule.checkRule(dirtyZone);
				}
			}// while
			if (this.errorContainer.getNumberOfErrors() > 0) {
				setStatus(VALIDATED_WITH_ERRORS);
				setErrorLyr();
			} else
				setStatus(VALIDATED);
		}// if
	}

	private void setErrorLyr() {
		IProjection proj = this.getProjection();
		int idx = 0;
		while (proj == null && idx < this.getLayerCount()) {
			proj = this.getLayer(idx).getProjection();
			idx++;
		}
		errorLayer = this.errorContainer.getAsFMapLayer(this.name + "_error",
				proj);
	}

	public int getLayerCount() {
		return super.getLayersCount();
	}

	public List getLayers() {
		return this.layers;
	}

	public int getRuleCount() {
		return rules.size();
	}

	public FLyrVect getLyr(int lyrIndex) {
		return (FLyrVect) super.getLayer(lyrIndex);
	}

	public ITopologyRule getRule(int ruleIndex) {
		return rules.get(ruleIndex);
	}

	public ITopologyRule getRuleById(int ruleId) {
		for (int i = 0; i < rules.size(); i++) {
			ITopologyRule rule = rules.get(i);
			if (rule.getId() == ruleId) {
				return rule;
			}// if
		}// for

		for (int i = 0; i < clusterToleranceRules.size(); i++) {
			MustBeLargerThanClusterTolerance rule = clusterToleranceRules
					.get(i);
			if (rule.getId() == ruleId) {
				return rule;
			}// if
		}
		return null;
	}

	/**
	 * Looks for all rules which has at least one reference to the given layer
	 * (as origin or destination layer in the rule)
	 * 
	 * @param lyr
	 * @return
	 */
	public List<ITopologyRule> getRulesByLyr(FLyrVect lyr) {
		List<ITopologyRule> solution = new ArrayList<ITopologyRule>();
		Iterator<ITopologyRule> ruleIt = this.rules.iterator();
		while (ruleIt.hasNext()) {
			ITopologyRule rule = ruleIt.next();
			if (rule instanceof IOneLyrRule) {
				IOneLyrRule oneLyrRule = (IOneLyrRule) rule;
				FLyrVect originLyr = oneLyrRule.getOriginLyr();
				if (originLyr.equals(lyr)) {
					solution.add(rule);
					continue;// dont need to check for destination layer
				}
			}// if

			if (rule instanceof ITwoLyrRule) {
				ITwoLyrRule twoLyrRule = (ITwoLyrRule) rule;
				FLyrVect destinationLyr = twoLyrRule.getDestinationLyr();
				if (destinationLyr.equals(lyr)) {
					solution.add(rule);
				}
			}

		}// while

		Iterator<MustBeLargerThanClusterTolerance> clusterIt = clusterToleranceRules
				.iterator();
		while (clusterIt.hasNext()) {
			MustBeLargerThanClusterTolerance rule = clusterIt.next();
			FLyrVect originLyr = rule.getOriginLyr();
			if (originLyr.equals(lyr)) {
				solution.add(rule);
				continue;// dont need to check for destination layer
			}
		}
		return solution;
	}

	/**
	 * Returns if a specified rectangle touch one of the existing dirty zones.
	 * If not, probably is needed to add to the dirty zones collection. If true,
	 * maybe it could modify the dirty zone.
	 */
	public boolean isInDirtyZone(Rectangle2D envelope) {
		Iterator<Rectangle2D> zonesIt = dirtyZones.iterator();
		while (zonesIt.hasNext()) {
			Rectangle2D rect = zonesIt.next();
			if (rect.contains(envelope))
				return true;
		}
		return false;
	}

	/**
	 * Modify the dirty zone of the specified index
	 * 
	 * TODO Make thread safe ?
	 */
	public void updateDirtyZone(int dirtyZoneIndex, Rectangle2D dirtyZone) {
		dirtyZones.remove(dirtyZoneIndex);
		dirtyZones.add(dirtyZoneIndex, dirtyZone);
	}

	public void setMaxNumberOfErrors(int maxNumberOfErrors) {
		this.maxNumberOfErrors = maxNumberOfErrors;
	}

	public int getMaxNumberOfErrors() {
		return maxNumberOfErrors;
	}

	/*
	 * @see org.gvsig.topology.ITopologyErrorContainer#addTopologyError(org.gvsig.topology.TopologyError)
	 */

	public void addTopologyError(TopologyError topologyError) {
		errorContainer.addTopologyError(topologyError);
		Rectangle2D rect = topologyError.getGeometry().getBounds2D();
		addDirtyZone(rect);
	}

	/**
	 * marks topologyErrors as an exception (and removes its bounds of the dirty
	 * zones list)
	 * 
	 * @param topologyError
	 *            error to mark as exceptions
	 */
	public void markAsTopologyException(TopologyError topologyError) {
		errorContainer.markAsTopologyException(topologyError);
		Rectangle2D rect = topologyError.getGeometry().getBounds2D();
		removeDirtyZone(rect);

		if (status == VALIDATED_WITH_DIRTY_ZONES) {
			if (dirtyZones.size() == 0)
				setStatus(VALIDATED);
		} else if (status == VALIDATED_WITH_ERRORS) {
			if (getNumberOfErrors() == getNumberOfExceptions())
				setStatus(VALIDATED);
		}
	}

	public void demoteToError(TopologyError topologyError) {
		errorContainer.demoteToError(topologyError);
		Rectangle2D rect = topologyError.getGeometry().getBounds2D();
		addDirtyZone(rect);
		if (getNumberOfErrors() > getNumberOfExceptions())
			setStatus(VALIDATED_WITH_ERRORS);
	}

	public List<TopologyError> getTopologyErrorsByLyr(FLyrVect layer,
			IProjection desiredProjection, boolean includeExceptions) {

		return errorContainer.getTopologyErrorsByLyr(layer, desiredProjection,
				includeExceptions);
	}

	public List<TopologyError> getTopologyErrorsByRule(String ruleName,
			IProjection desiredProjection, boolean includeExceptions) {

		return errorContainer.getTopologyErrorsByRule(ruleName,
				desiredProjection, includeExceptions);
	}

	public List<TopologyError> getTopologyErrorsByShapeType(int shapeType,
			IProjection desiredProjection, boolean includeExceptions) {
		return errorContainer.getTopologyErrorsByShapeType(shapeType,
				desiredProjection, includeExceptions);
	}

	/**
	 * Get TopologyError filtered byte shapeType, sourte layer of the rule which
	 * was violtated by this error.
	 */
	public List<TopologyError> getTopologyErrors(String ruleName,
			int shapeType, FLyrVect sourceLayer, IProjection desiredProjection,
			boolean includeExceptions) {

		return errorContainer.getTopologyErrors(ruleName, shapeType,
				sourceLayer, desiredProjection, includeExceptions);
	}

	/**
	 * Return an unique identifier for the error saved in this
	 * TopologyErrorContainer.
	 * 
	 * Until a new error would be added to this error container, it will return
	 * always the same error fid.
	 */
	public synchronized String getErrorFid() {
		return errorContainer.getErrorFid();
	}

	public int getNumberOfErrors() {
		return errorContainer.getNumberOfErrors();
	}

	public TopologyError getTopologyError(int index) {
		return errorContainer.getTopologyError(index);
	}

	public void clear() {
		errorContainer.clear();
	}

	public XMLEntity getXMLEntity() throws XMLException {
		boolean errorLayerExists = this.errorLayer != null;
		FLayer errorLayerCopy = null;
		if(errorLayerExists){
			errorLayerCopy = this.errorLayer;
			super.removeLayer(this.errorLayer);
		}
		// Topology is a subclass of FLayers, so the call to super
		// allows to persist layer status, toc information and layers
		XMLEntity xml = super.getXMLEntity();

		// Si no ponemos esto className será FLayerDefault, no??
		xml.putProperty("className", this.getClass().getName());
		xml.putProperty("name", name);
		xml.putProperty("clusterTolerance", clusterTolerance);
		xml.putProperty("status", status);
		xml.putProperty("maxNumberOfErrors", maxNumberOfErrors);

		int numberOfTopologyRules = rules.size();
		xml.putProperty("numberOfTopologyRules", numberOfTopologyRules);
		for (int i = 0; i < numberOfTopologyRules; i++) {
			ITopologyRule rule = rules.get(i);
			xml.addChild(rule.getXMLEntity());
		}

//		int numberOfClusterRules = clusterToleranceRules.size();
//		xml.putProperty("numberOfClusterRules", numberOfClusterRules);
//		for (int i = 0; i < numberOfClusterRules; i++) {
//			MustBeLargerThanClusterTolerance rule = clusterToleranceRules
//					.get(i);
//			xml.addChild(rule.getXMLEntity());
//		}

		int numberOfDirtyZones = dirtyZones.size();
		xml.putProperty("numberOfDirtyZones", numberOfDirtyZones);
		for (int i = 0; i < numberOfDirtyZones; i++) {
			Rectangle2D rect = dirtyZones.get(i);
			if (rect != null) {
				XMLEntity dirtyZoneXML = new XMLEntity();
				dirtyZoneXML.putProperty("extent" + i + "X", rect.getX());
				dirtyZoneXML.putProperty("extent" + i + "Y", rect.getY());
				dirtyZoneXML.putProperty("extent" + i + "W", rect.getWidth());
				dirtyZoneXML.putProperty("extent" + i + "H", rect.getHeight());

				xml.addChild(dirtyZoneXML);
			}// if

		}// for

		XMLEntity errorContainerXML = errorContainer.getXMLEntity();
		xml.addChild(errorContainerXML);
		
		xml.putProperty("errorLayerExists", errorLayerExists);
		xml.putProperty("nextRuleFid", ruleId);
		
		if(errorLayerExists){
			this.errorLayer = errorLayerCopy;
			try {
				if(((FLyrVect)errorLayer).getSource().getShapeCount() > 0)
					super.addLayer(this.layers.size(), this.errorLayer);
			} catch (ReadDriverException e) {
				e.printStackTrace();
			}	
			
		}

//		Collection<XYZLayerRank> ranksVal = layerRanks.values();
//		int numberOfRanks = ranksVal.size();
//		xml.putProperty("numberOfRanks", numberOfRanks);
//		Iterator<XYZLayerRank> xyzRankIterator = layerRanks.values().iterator();
//		while (xyzRankIterator.hasNext()) {
//			XYZLayerRank layerRank = xyzRankIterator.next();
//			XMLEntity entity = layerRank.getXMLEntity();
//			xml.addChild(entity);
//		}
		return xml;
	}

	public void setXMLEntity(XMLEntity xml) throws XMLException {
		super.setXMLEntity(xml);

		int numLayers = this.getLayersCount();
		int numProperties = this.getExtendedProperties().size();

		int childrenCount = numLayers + numProperties;

		if (xml.contains("clusterTolerance")) {
			this.clusterTolerance = xml.getDoubleProperty("clusterTolerance");
		}

		if (xml.contains("name")) {
			this.name = xml.getStringProperty("name");
		}

		if (xml.contains("status")) {
			int newStatus = xml.getIntProperty("status");
			this.setStatus((byte) newStatus);
		}

		if (xml.contains("maxNumberOfErrors")) {
			this.maxNumberOfErrors = xml.getIntProperty("maxNumberOfErrors");
		}
		
		if (xml.contains("nextRuleFid")) {
			this.ruleId = xml.getIntProperty("nextRuleFid");
		}
	
		boolean errorLayerExists = false;
		if (xml.contains("errorLayerExists")) {
			errorLayerExists = xml.getBooleanProperty("errorLayerExists");
		}
		if (xml.contains("numberOfTopologyRules")) {
			int numberOfTopologyRules = xml
					.getIntProperty("numberOfTopologyRules");
			for (int i = 0; i < numberOfTopologyRules; i++) {
				XMLEntity ruleXML = xml.getChild(childrenCount++);
				ITopologyRule rule = TopologyRuleFactory.createFromXML(this,
						ruleXML);
				
				this.rules.add(rule);
			}
		}

//		if (xml.contains("numberOfClusterRules")) {
//			int numberOfClusterRules = xml
//					.getIntProperty("numberOfClusterRules");
//			for (int i = 0; i < numberOfClusterRules; i++) {
//				XMLEntity ruleXML = xml.getChild(childrenCount++);
//				MustBeLargerThanClusterTolerance rule = (MustBeLargerThanClusterTolerance) TopologyRuleFactory
//						.createFromXML(this, ruleXML);
//				this.clusterToleranceRules.add(rule);
//			}
//		}

		if (xml.contains("numberOfDirtyZones")) {
			int numberOfDirtyZones = xml.getIntProperty("numberOfDirtyZones");
			for (int i = 0; i < numberOfDirtyZones; i++) {
				XMLEntity dirtyZoneXml = xml.getChild(childrenCount++);
				double x = dirtyZoneXml.getDoubleProperty("extent" + i + "X");
				double y = dirtyZoneXml.getDoubleProperty("extent" + i + "Y");
				double w = dirtyZoneXml.getDoubleProperty("extent" + i + "W");
				double h = dirtyZoneXml.getDoubleProperty("extent" + i + "H");

				Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w, h);
				dirtyZones.add(rect);
			}
		}

		XMLEntity errorContainerXML = xml.getChild(childrenCount++);
		if (errorContainerXML != null) {
			this.errorContainer = TopologyPersister
					.createErrorContainerFromXML(this, errorContainerXML);
			if(errorLayerExists)
			{
				setErrorLyr();
				updateErrorLyr();
			}	
		}
		
		//finally, we set error container instance for all rules
		for(ITopologyRule r:rules){
			r.setTopologyErrorContainer(errorContainer);
		}
		
		for(ITopologyRule r:clusterToleranceRules){
			r.setTopologyErrorContainer(errorContainer);
		}

//		if (xml.contains("numberOfRanks")) {
//			int numberOfRanks = xml.getIntProperty("numberOfRanks");
//			for (int i = 0; i < numberOfRanks; i++) {
//				XMLEntity xmlRank = xml.getChild(childrenCount++);
//				XYZLayerRank rank = new XYZLayerRank();
//				rank.setXMLEntity(xmlRank);
//			}
//		}
	}

	public int getNumberOfExceptions() {
		return this.errorContainer.getNumberOfExceptions();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ITopologyErrorContainer getErrorContainer() {
		return (ITopologyErrorContainer) this.errorContainer/*.clone()*/;
	}

	public FLayer getErrorLayer() {
		return this.errorLayer;
	}

	public void setErrorContainer(ITopologyErrorContainer errorContainer) {
		this.errorContainer = errorContainer;
		this.errorContainer.setTopology(this);
	}

	private void fireStatusChange(TopologyStatusEvent event) {
		Iterator<ITopologyStatusListener> it = this.statusListeners.iterator();
		while (it.hasNext()) {
			ITopologyStatusListener listener = it.next();
			listener.statusChange(event);
		}
	}

	public void removeErrorsByLayer(FLyrVect layer) {
		this.errorContainer.removeErrorsByLayer(layer);
	}

	public void removeErrorsByRule(String ruleName) {
		this.errorContainer.removeErrorsByRule(ruleName);
	}

	public void setRules(List<ITopologyRule> rules) {
		this.rules = rules;
	}

	public void setDirtyZones(List<Rectangle2D> dirtyZones) {
		this.dirtyZones = dirtyZones;
	}

	public void setStatusListeners(List<ITopologyStatusListener> statusListeners) {
		this.statusListeners = statusListeners;
	}

//	public void setLayerRanks(Map<FLyrVect, XYZLayerRank> layerRanks) {
//		this.layerRanks = layerRanks;
//	}

	public Object clone() {
		Topology newTopology = new Topology(super.getMapContext(), super
				.getParentLayer());
		try {
			Topology.copyProperties(this, newTopology);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newTopology;
	}

	public static void copyProperties(Topology from, Topology to) throws Exception {
		to.setMapContext(from.getMapContext());
		to.setParentLayer(from.getParentLayer());
		to.setClusterTolerance(from.getClusterTolerance());
		
		to.clusterToleranceRules.clear();
		to.layers = new ArrayList<FLayer>();
		List layers = from.layers;
		for (int i = 0; i < layers.size(); i++) {
			FLyrVect lyrVect = (FLyrVect) layers.get(i);
			if(lyrVect != from.errorLayer)//we skip error layer to avoid creating cluster tol rules
				to.addLayer(lyrVect);
		}
		
//		List lyrsCollectionListener = from.getLayerCollectionListeners();
//	    for(int i = 0; i < lyrsCollectionListener.size(); i++){
//	    	LayerCollectionListener listener = (LayerCollectionListener) lyrsCollectionListener.get(i);
//	    	
//	    	if(listener instanceof UpdateTopologyInTocLayerCollectionListener){
//	    		UpdateTopologyInTocLayerCollectionListener updateToc =
//	    			(UpdateTopologyInTocLayerCollectionListener) listener;
//	    		updateToc.setTopology(to);
//	    	}
//	    	to.addLayerCollectionListener((LayerCollectionListener) lyrsCollectionListener.get(i));
//	    }
	    
	    
		to.setTocStatusImage(from.getTocStatusImage());
		to.setName(from.getName());
		to.setStatus(from.getStatus());
		to.setMaxNumberOfErrors(from.getMaxNumberOfErrors());
		
		to.setRules(from.rules);
		
		to.setDirtyZones(from.dirtyZones);
		
//		to.setStatusListeners(from.statusListeners);
		
//		to.setLayerRanks(from.layerRanks);
		
		to.ruleId = from.ruleId;
		
		to.setErrorContainer((ITopologyErrorContainer) from.getErrorContainer().clone());
		
		to.errorLayer = to.errorContainer.getAsFMapLayer(from.errorLayer.getName(), from.errorLayer.getProjection());
		
		to.updateErrorLyr();

		
	}

	public FLyrVect getAsFMapLayer(String name, IProjection proj) {
		return errorContainer.getAsFMapLayer(name, proj);
	}

	/*
	 * Implementation of ITopologyErrorContanier
	 * 
	 */
	public Topology getTopology() {
		return this;
	}

	// FIXME See if Topology must implement ITopologyErrorContainer
	public void setTopology(Topology topology) {
	}

	public void removeError(TopologyError topologyError) {
		this.errorContainer.removeError(topologyError);
		
		Rectangle2D rect = topologyError.getGeometry().getBounds2D();
		removeDirtyZone(rect);
		if (status == VALIDATED_WITH_DIRTY_ZONES) {
			if (dirtyZones.size() == 0)
				setStatus(VALIDATED);
		} else if (status == VALIDATED_WITH_ERRORS) {
			if (getNumberOfErrors() == getNumberOfExceptions() || getNumberOfErrors() == 0)
				setStatus(VALIDATED);
		}
	}
	
	public List<LayerCollectionListener> getLayerCollectionListeners(){
		return layerCollectionListeners;
	}

	public void updateErrorLyr() {
		MapContext mapContext = getMapContext();
		if(mapContext != null){
			mapContext.beginAtomicEvent();
		}
		FLayer previousErrorLyr = super.getLayer(name + "_error");
		if(previousErrorLyr != null)
			super.removeLayer(previousErrorLyr);
	
		try {
			if(((FLyrVect)errorLayer).getSource().getShapeCount() > 0){
				super.addLayer(this.layers.size(), this.errorLayer);
				if(mapContext != null){
					mapContext.getLayers().removeLayer(this);
					mapContext.getLayers().addLayer(this);
				}
			}
		} catch (ReadDriverException e) {
			e.printStackTrace();
		}	
		if(mapContext != null){
			mapContext.endAtomicEvent();
		}
	}
}
