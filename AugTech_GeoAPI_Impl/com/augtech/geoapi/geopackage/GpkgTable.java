/*
 * Copyright 2013, Augmented Technologies Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.augtech.geoapi.geopackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.geometry.BoundingBox;

import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.augtech.geoapi.geopackage.GeoPackage.JavaType;
import com.augtech.geoapi.geopackage.table.FeatureField;
import com.augtech.geoapi.geopackage.table.FeaturesTable;
import com.augtech.geoapi.geopackage.table.GpkgContents;
import com.augtech.geoapi.geopackage.table.GpkgDataColumnConstraint;
import com.augtech.geoapi.geopackage.table.GpkgExtensions;
import com.augtech.geoapi.geopackage.table.GpkgExtensions.Extension;
import com.augtech.geoapi.geopackage.table.TilesTable;
import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;

/** An abstract class to create, query and update a table within the GeoPackage.
 * All system tables extend this class, as do Feature and Tile tables. Implementors
 * should extend this class for their own tables within the GeoPackage to enable standard
 * and consistent functionality.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public abstract class GpkgTable {

	/** The data_type of a user defined table in gpkg_contents */
	public static final String TABLE_TYPE_FEATURES = "features";
	/** The data_type of a user defined table in gpkg_contents */
	public static final String TABLE_TYPE_TILES = "tiles";
	/** An internal system table type (not used in GeoPackage) */
	public static final String TABLE_TYPE_SYSTEM = "system";
	/** This table is user-defined and not used as a part of the standard GeoPackage structure */
	public static final String TABLE_TYPE_USER = "user";
	
	Map<String, GpkgField> fields = new HashMap<String, GpkgField>();
	protected String tableName = "";
	protected String tableType = TABLE_TYPE_SYSTEM;
	String[] constraints = null;
	private StringBuffer sb = new StringBuffer();
	private boolean hasContentInfo = false, hasExtensionInfo = false;
	
	// gpkg_contents info
	protected String identifier = "";
	protected String description = "";
	private Date lastChange = null;
	private BoundingBox bbox = new BoundingBoxImpl("EPSG:4326");
	/** A List of extensions applicable to this table.
	 * Swapped to sse arraylist to save getting a count on the cursor which cycles
	 * whole ResultSet on underlying Cursor implementation */
	protected List<Extension> gpkgExtensions = null;
	
	/** Create a new instance of a GpkgTable (this will not create the table in 
	 * the GeoPackage, only a class for handling a table).
	 * 
	 * @param tableName The name of the table. Spaces will be replaced with '_' during query, insert,
	 * update and delete operations
	 * @param fields An array of fields that can be referenced. If <code>Null</code>
	 * then the fields will not be available immediately. One of the query methods will populate
	 * the field data. <code>Null</code> Is typically used when creating a {@linkplain FeatureTable} or 
	 * {@link TilesTable}.
	 * @param tableConstraints Any constraints that apply to the passed fields. These are SQL constraints, 
	 * <i>not</i> {@linkplain GpkgDataColumnConstraint}'s.
	 * @see #create(GeoPackage)
	 */
	public GpkgTable(String tableName, GpkgField[] fields, String[] tableConstraints) {
		this.tableName = tableName;
		this.constraints = tableConstraints;
		
		if (fields==null) return;// Feature tables may not have fields at initialisation
		
		for (GpkgField gf : fields) { 
			this.fields.put(gf.getFieldName(), gf);
		}

	}
	
	/** Create this table in the GeoPackage. Only use-able for system tables.
	 * 
	 * @return True if created, False if it already exists or it is either
	 * a FeaturesTable or TilesTable
	 * @see {@linkplain FeatureTable#create(GeoPackage)}
	 */
	protected boolean create(GeoPackage geoPackage) {

		if (this.tableType.equals(TABLE_TYPE_SYSTEM)==false) return false;
		
		if (isTableInDB(geoPackage) || isTableInGpkg(geoPackage) ) return false;
		
		sb.setLength(0);
		
		sb.append("Create TABLE [").append(tableName).append("] (");

		for (GpkgField f : fields.values()) {
			sb.append("[").append(f.getFieldName()).append("] ").append(f.getFieldType());
			
			if (f.getFieldOptions()!=null && !f.getFieldOptions().equals("")) {
				sb.append(" ").append(f.getFieldOptions());
			}
			
			if (f.getDefaultValue()!=null && !f.getDefaultValue().equals("")) {
				sb.append(" DEFAULT ").append(f.getDefaultValue());
			}
			sb.append(", ");
		}
		
		// Trim last comma
		sb.setLength(sb.length()-2);
		
		// Add constraints
		if (constraints!=null) {
			for (String con : constraints) {
				sb.append(", ").append(con);
			}
		}
		
		sb.append(");");
		
		geoPackage.getDatabase().execSQL( sb.toString() );
		
		return true;
	}
	/** Build the field definitions for this table as well as extended information
	 * from gpkg_contents. The information is only populated if it hasn't been done
	 * already.
	 * 
	 * @param geoPackage The GeoPackage to get the information from
	 * @throws Exception 
	 */
	protected void getContents(GeoPackage geoPackage) throws Exception {
		
		// Not valid for system tables
		if (this.tableType.equals(TABLE_TYPE_SYSTEM)) return;
		
		if (this.fields.size()==0) {
			
			// Get the table definition creation statement so we can find out the column data-types
			ICursor columnCursor = geoPackage.getDatabase().doRawQuery("PRAGMA table_info('"+tableName+"');");
			
			if (columnCursor==null)
				throw new Exception("Could not get "+tableName+" column information");
			
			// Build our field definitions for this table
			while (columnCursor.moveToNext()) {
				
				// Defined field name
				String fieldName = columnCursor.getString(columnCursor.getColumnIndex("name"));
	
				/* Fields from a user defined 'FeatureTable' can have extended information from
				 * GpkgDataColumns, therefore to save extensive casting/ testing when processing a 
				 * FeatureTable we create a FeatureField here instead. */
				GpkgField gf = null;
				if (this instanceof FeaturesTable) {
					gf = new FeatureField(fieldName, columnCursor.getString(columnCursor.getColumnIndex("type")) );
				} else {
					gf = new GpkgField(fieldName, columnCursor.getString(columnCursor.getColumnIndex("type")) );
				}
				
				gf.primaryKey =  columnCursor.getInt(columnCursor.getColumnIndex("pk"))==1;
						
				this.fields.put(fieldName, gf);
			}
			columnCursor.close();
			
		}
		
		// Table details and bounds from GpkgContents
		if (hasContentInfo==false) {
			
			GpkgRecords contents = geoPackage.getSystemTable(GpkgContents.TABLE_NAME)
												.query(geoPackage, "table_name='"+tableName+"'");
			if (contents==null || contents.size()==0) 
				throw new Exception("Table "+tableName+" not defined in "+GpkgContents.TABLE_NAME);
			
			this.identifier = contents.getFieldString(0, "identifier");
			this.description = contents.getFieldString(0, "description");
			String lc = contents.getFieldString(0, "last_change");
			if (!lc.equals("")) {
				this.lastChange = DateUtil.deserializeDateTime( contents.getFieldString(0, "last_change") );
			}
			
			bbox = new BoundingBoxImpl(
						contents.getFieldDouble(0,"min_x"),
						contents.getFieldDouble(0,"max_x"),
						contents.getFieldDouble(0,"min_y"),
						contents.getFieldDouble(0,"max_y"),
						new CoordinateReferenceSystemImpl(""+contents.getFieldInt(0, "srs_id"))
						);
			hasContentInfo = true;
		}
		
		getExtensionInfo(geoPackage);

	}
	/** Get any GpkgExtension information for this table.
	 * 
	 * @return An array of Extension's relating to this table, or <code>Null</code>
	 * if there isn't any
	 */
	public List<Extension> getExtensionInfo(GeoPackage geoPackage) {
		
		if (gpkgExtensions==null && !hasExtensionInfo) {
			GpkgTable extTable = geoPackage.getSystemTable(GpkgExtensions.TABLE_NAME);
			if (!extTable.isTableInDB(geoPackage)) {
				hasExtensionInfo=true;
				return null;
			}
			
			ICursor exCursor = extTable.query(geoPackage, null, "table_name='"+tableName+"'");


			if (exCursor!=null) {
				this.gpkgExtensions = new ArrayList<Extension>();
				while (exCursor.moveToNext()) {
					Extension ext = new Extension();
					ext.tableName = tableName;
					ext.columnName = exCursor.getString( exCursor.getColumnIndex("column_name") );
					ext.extensionName = exCursor.getString( exCursor.getColumnIndex("extension_name") );
					ext.definition = exCursor.getString( exCursor.getColumnIndex("definition") );
					ext.scope = exCursor.getString( exCursor.getColumnIndex("scope") );
					
					this.gpkgExtensions.add( ext );
				}
				exCursor.close();
			}
			hasExtensionInfo = this.gpkgExtensions.size() > 0;
		}

		return this.gpkgExtensions;
	}
	/** Get the defined primary key field name for this table
	 * 
	 * @param geoPackage
	 * @return The name of the pk field or 'rowid' if not explicitly defined
	 * @throws Exception
	 */
	public String getPrimaryKey(GeoPackage geoPackage) throws Exception {
		getContents(geoPackage);
		
		for (GpkgField gf: this.fields.values()) {
			if (gf.isPrimaryKey()) return gf.getFieldName();
		}
		
		return "rowid";
	}
	/**Get the type of table; One of 
	 * {@link #TABLE_TYPE_FEATURES}, {@link #TABLE_TYPE_TILES} or {@link #TABLE_TYPE_SYSTEM}
	 * 
	 * @return the tableType
	 */
	public String getTableType() {
		return tableType;
	}
	/** Get the number of records within this table. Note that the table
	 * is queried every time as records could be inserted or deleted by other
	 * methods.
	 * 
	 * @param geoPackage
	 * @return The count of records or -1 if the table does not exist.
	 */
	public int getCount(GeoPackage geoPackage) {
		if (isTableInDB(geoPackage)==false) return -1;

		// Count on primary key is quicker than * on large tables
		String pk = "*";
		try {
			pk = getPrimaryKey(geoPackage);
		} catch (Exception e1) {}
		
		String sql = String.format("Select count(%s) from [%s]", pk, tableName);

		ICursor c = geoPackage.getDatabase().doRawQuery( sql );
		if (c==null || !c.moveToFirst()) {
			try {
				c.close();
			} catch (Exception e){}
			return -1;
		}
		
		int count = c.getInt(0);
		c.close();
		return count;
	}
	/** Insert a set of record values into this table as a batch
	 * 
	 * @param geoPackage
	 * @param allValues
	 * @return The number of records successfully inserted
	 */
	public long insert(GeoPackage geoPackage, List<Map<String, Object>> allValues) {
		return geoPackage.getDatabase().doInsert("["+tableName+"]", allValues);
	}
	/** Insert a record into the table
	 * 
	 * @param geoPackage
	 * @param values
	 * @return The row ID of the newly inserted row
	 */
	public long insert(GeoPackage geoPackage, Map<String, Object> values) {
		return geoPackage.getDatabase().doInsert("["+tableName+"]", values);
	}

	/** Issue a raw query on this table for a {@linkplain ICursor}
	 * 
	 * @param geoPackage The GeoPackage to query
	 * @param columns An array of the columns to get
	 * @param strWhere A valid where clause, without the 'where'. If <code>Null</code>
	 * all records will be returned (which is not advised!)
	 * @return
	 */
	public ICursor query(GeoPackage geoPackage, String[] columns, String strWhere) {
		
		return geoPackage.getDatabase().doQuery("["+tableName+"]", columns, strWhere);
		
	}
	/** Get a list of GpkgRecords from this table matching the where clause. It
	 * is suggested that sub-classes override this method to provide suitable
	 * where clauses. This method requires a valid set of GpkgField's defined for 
	 * the table.
	 * 
	 * @param geoPackage The GeoPackage to query
	 * @param strWhere A valid where clause, without the 'where'. If <code>Null</code>
	 * all records will be returned (which is not advised!)
	 * @return A list containing a map of {@link GpkgField}. Each member of the list
	 * is one record. If the query could not be executed then an empty list is returned.
	 * @throws Exception 
	 */
	public GpkgRecords query(GeoPackage geoPackage, String strWhere) throws Exception {
		
		String stmt = "SELECT * FROM ["+tableName+"]";
		if (strWhere!=null && !strWhere.equals("")) stmt+=" WHERE "+strWhere;
		
		return rawQuery(geoPackage, stmt);
	}
	/** Get a list of GpkgRecords from this table matching using the full SQL statement. It
	 * is suggested that sub-classes override this method to provide suitable
	 * where clauses. This method requires a valid set of GpkgField's defined for 
	 * the table.
	 * 
	 * @param geoPackage The GeoPackage to query
	 * @param sqlStmt A valid SQL statement. (No checks are performed on this)
	 * @return A list containing a map of {@link GpkgField}. Each member of the list
	 * is one record. If the query could not be executed then an empty list is returned.
	 * @throws Exception 
	 */
	public GpkgRecords rawQuery(GeoPackage geoPackage, String sqlStmt) throws Exception {

		// Populate field info (only applicable for non-system tables)
		getContents(geoPackage);

		ICursor cur = geoPackage.getDatabase().doRawQuery(sqlStmt);

		
		Map<String, Integer> fieldIdx = new HashMap<String, Integer>();
		List<GpkgField> fieldList = new ArrayList<GpkgField>();
		GpkgRecords records = null;
		ArrayList<Object> thisRec = null;
		String fieldType = "";
		JavaType jType = null;
		List<JavaType> jTypeList = new ArrayList<JavaType>();
		
		// Get details of each column
		int colIdx = 0;
		for (String col : cur.getColumnNames()) {
			
			// Add field to record-set index
			fieldIdx.put(col, colIdx);
			GpkgField gf = fields.get(col).clone();
			fieldList.add( gf );
			
			// A simple list of the mapped Java type
			jTypeList.add( geoPackage.sqlTypeMap.get( gf.getFieldType().toLowerCase() ) );
			colIdx++;
		}
		
		// The result set
		records = new GpkgRecords(fieldIdx, fieldList);
		
		// For each record in cursor..
		while (cur.moveToNext()) {
			thisRec = new ArrayList<Object>();

			// For each column in cursor
			for (int idx=0;idx < cur.getColumnCount(); idx++) {
				
				jType = jTypeList.get(idx);
				
				if (jType==null || jType==JavaType.UNKNOWN) 
					throw new IllegalArgumentException("Unknown SQL data type '"+fieldType+"'");
				
				switch (jType) {
				case INTEGER:
					thisRec.add( cur.getInt(idx) );
					break;
				case STRING:
					thisRec.add( cur.getString(idx) );
					break;
				case BOOLEAN:
					thisRec.add( cur.getBoolean(idx) );
					break;
				case FLOAT:
					thisRec.add( cur.getFloat(idx) );
					break;
				case DOUBLE:
					thisRec.add( cur.getDouble(idx) );
					break;
				case BYTE_ARR:
					thisRec.add( cur.getBlob(idx) );
					break;
				}
				
			} // Next column
			
			thisRec.trimToSize();
			records.add( thisRec );
		}
		
		cur.close();

		return records;
	}
	/** Does this table exist in gpkg_contents?
	 * 
	 * @param geoPackage The GeoPackage to look in
	 * @return True if table exists in gpkg_contents
	 */
	public boolean isTableInGpkg(GeoPackage geoPackage) {
		boolean tExists = false;
		if (this.tableType.equals(TABLE_TYPE_SYSTEM)) return false;
		
		ICursor c = geoPackage.getDatabase().doQuery(
				"gpkg_contents", 
				new String[]{"table_name"},
				"table_name='"+tableName+"'");
		
		tExists =  c.moveToFirst();
		c.close();
		
		return tExists;
	}
	/** Check that a table exists in the GeoPackage database. This is different to 
	 * checking whether the table definition exists in the gpkg_contents table.
	 * 
	 * @param geoPackage The GeoPackage to look in
	 * @return True if the table is in SQLITE_MASTER
	 * @see #isTableInGpkg(String, String)
	 */
	public boolean isTableInDB(GeoPackage geoPackage) {
		// Does the table already exist?
		boolean tExists = false;
		ICursor c = geoPackage.getDatabase().doQuery("SQLITE_MASTER", new String[]{"tbl_name"},"tbl_name='"+tableName+"'");
		if( c.moveToFirst() ) tExists = c.getString(0).equals(tableName);
		c.close();
		return tExists;
	}
	/** Get the internal GeoPackage table name
	 * 
	 * @return
	 */
	public String getTableName() {
		return this.tableName;
	}
	/** Get a field definition for this table
	 * This will not be populated on non-system tables until 
	 * {@link #getContents(GeoPackage)} has been called.
	 * 
	 * @param fieldName The name of the field to get
	 * @return
	 */
	public GpkgField getField(String fieldName) {
		return fields.get(fieldName);
	}
	/** Get an array of the field names defined for this table
	 * This will not be populated on non-system tables until 
	 * {@link #getContents(GeoPackage)} has been called.
	 * 
	 * @return
	 */
	public String[] getFieldNames() {
		String[] ret = new String[fields.size()];
		int i=0;
		for (GpkgField f : fields.values()) {
			ret[i] = f.getFieldName();
			i++;
		}
		return ret;
	}
	/** Get a list of the field definitions for this table.
	 * This list will not be populated on non-system tables until 
	 * {@link #getContents(GeoPackage)} has been called.
	 * 
	 * @return
	 */
	public Collection<GpkgField> getFields() {
		return this.fields.values();
	}
	/** Add a new field on to this table definition. This does not add it
	 * in to the database, only the list of defined fields.
	 * @param field
	 */
	protected void addField(GpkgField field) {
		this.fields.put(field.fieldName, field);
	}
	/** Update a table in the GeoPackage with the supplied values, based on the 
	 * passed where clause.
	 * 
	 * @param geoPackage The GeoPackage to update
	 * @param values A map of the field names and new values
	 * @param strWhere A valid where clause, excluding the word 'where'
	 * 
	 * @return The number of records updated.
	 */
	public int update(GeoPackage geoPackage, Map<String, Object> values, String strWhere) {
		return geoPackage.getDatabase().doUpdate("["+tableName+"]", values, strWhere);
	}
	/** Delete a record from this table
	 * 
	 * @param geoPackage The GeoPackage to delete from
	 * @param strWhere The where clause without 'where'. Set to NULL to clear all records.
	 * @return The number of rows affected if a where clause is passed in, 0 otherwise
	 */
	public int delete(GeoPackage geoPackage, String strWhere) {
		return geoPackage.getDatabase().doDelete("["+tableName+"]", strWhere);
	}
	/** Get the Identifier from gpkg_contents
	 * This will not be populated on non-system tables until 
	 * {@link #getContents(GeoPackage)} has been called.
	 * 
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}


	/** Get the description from gpkg_contents.
	 * This will not be populated on non-system tables until 
	 * {@link #getContents(GeoPackage)} has been called.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}


	/** Get the date of the last change from gpkg_contents.
	 * This will not be populated on non-system tables until 
	 * {@link #getContents(GeoPackage)} has been called.
	 * 
	 * @return the lastChange
	 */
	public Date getLastChange() {
		return lastChange;
	}
	/** Get the bounding box of this table (if in gpkg_contents).
	 * This will not be populated on non-system tables until 
	 * {@link #getContents(GeoPackage)} has been called.
	 * 
	 * @return the BoundingBox from GpkgContents
	 */
	public BoundingBox getBounds() {
		return this.bbox;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(constraints);
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result
				+ ((tableName == null) ? 0 : tableName.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GpkgTable))
			return false;
		GpkgTable other = (GpkgTable) obj;
		if (!Arrays.equals(constraints, other.constraints))
			return false;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}
	

}
