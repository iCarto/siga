/*
 * Created on 26-oct-2005
 *
 * gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
package com.iver.cit.gvsig.fmap.drivers;

import java.awt.geom.Rectangle2D;
import java.sql.Types;

public class DBLayerDefinition extends LayerDefinition {
	private IConnection conn;
	private String catalogName;
	private String schema;
	private String tableName;
	// private String[] fieldNames; // GeometryField not included
	private String fieldID; // Estos 2 campos en PostGIS los sabremos
	private String fieldGeometry; // (gid y the_geom), pero por si acaso se cambian
	private String whereClause;
	private Rectangle2D workingArea;
	private String SRID_EPSG;
	private String classToInstantiate;

	// private int idFieldGeometry = -1;
	private int idFieldID = -1;
	private int dimension = 2;
	private String host;
	private int port;
	private String database;
	private String user;
	private String password;
	private String connectionname;

	public String getCatalogName() {
		return catalogName;
	}
	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}
	public String getFieldGeometry() {
		return fieldGeometry;
	}
	public void setFieldGeometry(String fieldGeometry) {
		this.fieldGeometry = fieldGeometry;
	}
	public String getFieldID() {
		return fieldID;
	}
	/**
	 * @param fieldID
	 */
	 public void setFieldID(String fieldID) {
		 this.fieldID = fieldID;
		 // No se usa lo de a�adir el id por otro lado.
		 // Se hace en MyFinishAction
		 /* boolean bFound = false;
    	for (int i=0; i< fieldsDesc.length; i++)
    	{
    		if (fieldsDesc[i].getFieldName().compareToIgnoreCase(fieldID) == 0)
    			bFound = true;
    	}
        if (bFound != true)
        {
        	int length = fieldsDesc.length;
        	FieldDescription[] aux = new FieldDescription[length+1];
        	System.arraycopy(fieldsDesc, 0, aux, 0, length);
        	FieldDescription f = new FieldDescription();
        	aux[length] = field
        } */
	 }

	 /**
	  * Check if a field is in a array of FieldDescription fields
	  * @param fields Array to search on.
	  * @param fieldName Name of the field we want to search.
	  * @return -1 if the field exist or the index if not
	  */
	 private int contains(FieldDescription[] fields, String fieldName){
		 for (int i=0; i < fields.length; i++)
		 {
			 FieldDescription f = fields[i];
			 if (f.getFieldName().equalsIgnoreCase(fieldName))
			 {
				 return i;
			 }
		 }
		 return -1;
	 }

	 /** This method creates a new Field ID & add it to FieldDescription array.
	  *
	  * It will try to create a field named gid. If there is a field so named in
	  * FieldDescription array, it will try to create gid1, etc.
	  * As it will check if exists a field with that name in fieldsDesc,
	  * fieldsDesc must be filled before calling this method.
	  * @return true if a new field for ID was created sucessfully
	  */
	 public void setNewFieldID() {

		 //setting FieldID
		 int index = 0;
		 String fieldName = "gid";
		 while(contains(fieldsDesc, fieldName) != -1){
			 index++;
			 fieldName = "gid"+index;
		 }

		 setFieldID(fieldName);

		 //adding it to fieldsDesc
		 int numFieldsAnt = fieldsDesc.length;
		 FieldDescription[] newFields = new FieldDescription[fieldsDesc.length + 1];
		 for (int i=0; i < numFieldsAnt; i++) {
			 newFields[i] = fieldsDesc[i];
		 }
		 newFields[numFieldsAnt] = new FieldDescription();
		 newFields[numFieldsAnt].setFieldDecimalCount(0);
		 newFields[numFieldsAnt].setFieldType(Types.INTEGER);
		 newFields[numFieldsAnt].setFieldLength(7);
		 newFields[numFieldsAnt].setFieldName(fieldName);
		 this.setFieldsDesc(newFields);
	 }

	 /**
	  * @return GeometryField is not included in this list
	  */
	 public String[] getFieldNames() {
		 String[] fieldNames = new String[fieldsDesc.length];
		 for (int i=0; i< fieldsDesc.length; i++)
		 {
			 fieldNames[i] = fieldsDesc[i].getFieldName();
		 }
		 return fieldNames;
	 }
	 /**
	  * Geometry field not included in this list
	  * @param fieldNames
	  */
	 public void setFieldNames(String[] fieldNames) {
		 idFieldID = -1;
		 // idFieldGeometry = -1;
		 if (fieldsDesc == null)
			 fieldsDesc = new FieldDescription[fieldNames.length];
		 if (fieldsDesc.length != fieldNames.length)
			 fieldsDesc = new FieldDescription[fieldNames.length];
		 for (int i=0; i < fieldNames.length; i++)
		 {
			 fieldsDesc[i] = new FieldDescription();
			 fieldsDesc[i].setFieldName(fieldNames[i]);
		 }
	 }
	 /**
	  * @deprecated Better use getCompoundTableName to deal with schemas.
	  * This method should be used only by drivers when it is necessary to distinguish between table and schema.
	  * @return
	  */
	 public String getTableName() {
		 return tableName;
	 }
	 public void setTableName(String tableName) {
		 this.tableName = tableName;
	 }
	 /**
	  * The returned string must contain "WHERE " clause
	  * @return
	  */
	 public String getWhereClause() {
		 return whereClause;
	 }
	 public void setWhereClause(String whereClause) {
		 this.whereClause = whereClause;
	 }
	 public Rectangle2D getWorkingArea() {
		 return workingArea;
	 }
	 public void setWorkingArea(Rectangle2D workingArea) {
		 this.workingArea = workingArea;
	 }

	 /**
	  * @param fieldName
	  * @return index of field (0 based). -1 if not found
	  */
	 public int getIdField(String fieldName)
	 {
		 int result = -1;
		 for (int i=0; i < fieldsDesc.length; i++)
		 {
			 if (fieldName.equalsIgnoreCase(fieldsDesc[i].getFieldName()))
			 {
				 result = i;
				 break;
			 }
		 }
		 return result;
	 }

	 /**
	  * 0 based index of the ID field
	  * @return Returns the idFieldID.
	  */
	 public int getIdFieldID() {
		 if (idFieldID == -1)
			 idFieldID = getIdField(fieldID);
		 return idFieldID;
	 }
	 /**
	  * @return Returns the sRID_EPSG.
	  */
	 public String getSRID_EPSG() {
		 return SRID_EPSG;
	 }
	 /**
	  * @param srid_epsg The sRID_EPSG to set.
	  */
	 public void setSRID_EPSG(String srid_epsg) {
		 SRID_EPSG = srid_epsg;
	 }
	 /**
	  * @return Returns the classToInstantiate.
	  */
	 public String getClassToInstantiate() {
		 return classToInstantiate;
	 }
	 /**
	  * @param classToInstantiate The classToInstantiate to set.
	  */
	 public void setClassToInstantiate(String classToInstantiate) {
		 this.classToInstantiate = classToInstantiate;
	 }

	 public int getFieldIdByName(String name)
	 {
		 int resul = -1;
		 for (int i=0; i<fieldsDesc.length; i++)
		 {
			 if (fieldsDesc[i].getFieldName().equalsIgnoreCase(name))
			 {
				 resul = i;
				 break;
			 }
		 }
		 return resul;
	 }
	 public IConnection getConnection() {
		 return conn;
	 }

	 public void setConnection(IConnection connection) {
		 conn = connection;
	 }
	 public int getDimension() {
		 return dimension;
	 }

	 public void setDimension(int dim) {
		 dimension = dim;
	 }
	 public String getHost() {
		 return host;
	 }
	 public void setHost(String host){
		 this.host=host;
	 }
	 public int getPort() {
		 return port;
	 }
	 public void setPort(int port){
		 this.port=port;
	 }
	 public String getDataBase() {
		 return database;
	 }
	 public void setDataBase(String database){
		 this.database=database;
	 }
	 public String getUser() {
		 return user;
	 }
	 public void setUser(String user){
		 this.user=user;
	 }
	 public String getPassword() {
		 return password;
	 }
	 public void setPassword(String password){
		 this.password=password;
	 }
	 public void setConnectionName(String stringProperty) {
		 this.connectionname=stringProperty;
	 }
	 public String getConnectionName() {
		 return connectionname;
	 }
	 public void setSchema(String schema) {
		 this.schema = schema;
	 }
	 public String getSchema() {
		 return schema;
	 }
	 /**
	  * @return "schema"."tableName" or only "tableName" if schema is not defined. schema and tableName are quoted
	  */
	 public String getComposedTableName() {
		 String quote = conn.getIdentifierQuoteString();

		 String compoundTable = "";
		 if (getSchema()!=null && !getSchema().equals("")){
			 compoundTable = quote + getSchema() + quote + ".";
		 }
		 compoundTable = compoundTable + quote + getTableName() + quote;
		 return compoundTable;
	 }
	 public void setIdFieldID(int idFieldID) {
		 this.idFieldID = idFieldID;
	 }
}
