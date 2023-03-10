/*
 * Copyright (c) 2011. iCarto
 *
 * This file is part of extNavTableForms
 *
 * extNavTableForms is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * extNavTableForms is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with extNavTableForms.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package es.icarto.gvsig.navtableforms.ormlite.domainvalues;


/**
 * Interface for all DomainReaders.
 * 
 * @author Jorge L?pez <jlopez@cartolab.es>
 * @author Francisco Puga <fpuga@icarto.es>
 * 
 */
public interface DomainReader {

    public DomainValues getDomainValues();
    
    public void setAddVoidValue(boolean addVoidValue);

}
