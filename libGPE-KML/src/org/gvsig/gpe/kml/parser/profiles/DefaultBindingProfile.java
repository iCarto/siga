package org.gvsig.gpe.kml.parser.profiles;


import org.gvsig.gpe.kml.parser.v21.coordinates.CoordinatesTypeIterator;
import org.gvsig.gpe.kml.parser.v21.coordinates.LatLonAltBoxIterator;
import org.gvsig.gpe.kml.parser.v21.features.DocumentBinding;
import org.gvsig.gpe.kml.parser.v21.features.ElementBinding;
import org.gvsig.gpe.kml.parser.v21.features.FeatureBinding;
import org.gvsig.gpe.kml.parser.v21.features.FolderBinding;
import org.gvsig.gpe.kml.parser.v21.features.LookAtBinding;
import org.gvsig.gpe.kml.parser.v21.features.MetadataBinding;
import org.gvsig.gpe.kml.parser.v21.features.PlaceMarketBinding;
import org.gvsig.gpe.kml.parser.v21.features.StyleBinding;
import org.gvsig.gpe.kml.parser.v21.features.StyleMapBinding;
import org.gvsig.gpe.kml.parser.v21.geometries.DoubleBinding;
import org.gvsig.gpe.kml.parser.v21.geometries.GeometryBinding;
import org.gvsig.gpe.kml.parser.v21.geometries.InnerBoundaryIsBinding;
import org.gvsig.gpe.kml.parser.v21.geometries.LineStringTypeBinding;
import org.gvsig.gpe.kml.parser.v21.geometries.LinearRingBinding;
import org.gvsig.gpe.kml.parser.v21.geometries.MultiGeometryBinding;
import org.gvsig.gpe.kml.parser.v21.geometries.OuterBoundaryIsBinding;
import org.gvsig.gpe.kml.parser.v21.geometries.PointTypeBinding;
import org.gvsig.gpe.kml.parser.v21.geometries.PolygonTypeBinding;
import org.gvsig.gpe.kml.parser.v21.geometries.RegionBinding;
import org.gvsig.gpe.kml.parser.v21.header.HeaderBinding;


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
/* CVS MESSAGES:
 *
 * $Id$
 * $Log$
 *
 */
/**
 * @author Jorge Piera LLodr� (jorge.piera@iver.es)
 */
public class DefaultBindingProfile implements IBindingProfile{

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getCoordinatesTypeBinding()
	 */
	public CoordinatesTypeIterator getCoordinatesTypeBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getDocumentBinding()
	 */
	public DocumentBinding getDocumentBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getDoubleBinding()
	 */
	public DoubleBinding getDoubleBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getElementBinding()
	 */
	public ElementBinding getElementBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getFeatureBinding()
	 */
	public FeatureBinding getFeatureBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getFolderBinding()
	 */
	public FolderBinding getFolderBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getGeometryBinding()
	 */
	public GeometryBinding getGeometryBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getHeaderBinding()
	 */
	public HeaderBinding getHeaderBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getInnerBoundaryIsBinding()
	 */
	public InnerBoundaryIsBinding getInnerBoundaryIsBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getLatLonAltBoxBinding()
	 */
	public LatLonAltBoxIterator getLatLonAltBoxBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getLineStringTypeBinding()
	 */
	public LineStringTypeBinding getLineStringTypeBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getLinearRingBinding()
	 */
	public LinearRingBinding getLinearRingBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getLookAtBinding()
	 */
	public LookAtBinding getLookAtBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getMetadataBinding()
	 */
	public MetadataBinding getMetadataBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getMultiGeometryBinding()
	 */
	public MultiGeometryBinding getMultiGeometryBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getOuterBoundaryIsBinding()
	 */
	public OuterBoundaryIsBinding getOuterBoundaryIsBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getPlaceMarketBinding()
	 */
	public PlaceMarketBinding getPlaceMarketBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getPointTypeBinding()
	 */
	public PointTypeBinding getPointTypeBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getPolygonTypeBinding()
	 */
	public PolygonTypeBinding getPolygonTypeBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getRegionBinding()
	 */
	public RegionBinding getRegionBinding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.kml.parser.bindings.profiles.IBindingProfile#getStyleBinding()
	 */
	public StyleBinding getStyleBinding() {
		return null;
	}

	public StyleMapBinding getStyleMapBinding() {
		return null;
	}
}
