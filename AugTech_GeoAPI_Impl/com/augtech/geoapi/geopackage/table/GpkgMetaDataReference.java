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
package com.augtech.geoapi.geopackage.table;

import com.augtech.geoapi.geopackage.GpkgField;
import com.augtech.geoapi.geopackage.GpkgTable;

/**
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class GpkgMetaDataReference extends GpkgTable {
	/** The internal DB table name */
	public static String TABLE_NAME = "gpkg_metadata_reference";
	
	public static final String SCOPE_GEOPACKAGE  = "geopackage";
	public static final String SCOPE_TABLE  = "table";
	public static final String SCOPE_COLUMN  = "column";
	public static final String SCOPE_ROW  = "row";
	public static final String SCOPE_ROW_COLUMN  = "row/col";
	
	/**Lowercase metadata reference scope Defaults to SCOPE_GEOPACKAGE*/
	//String reference_scope = SCOPE_GEOPACKAGE;
	/** Name of the table to which this metadata reference applies, 
	 * or NULL for reference_scope of SCOPE_GEOPACKAGE. Defaults to Null*/
	//String table_name = null;
	/** Name of the column to which this metadata reference applies;
	 *  NULL for reference_scope of SCOPE_GEOPACKAGE, SCOPE_TABLE or SCOPE_ROW, or the name of a 
	 *  column in the table_name table for reference_scope of SCOPE_COLUMN or SCOPE_ROW_COLUMN */
	//String column_name = null;
	/** NULL for reference_scope of SCOPE_GEOPACKAGE, SCOPE_TABLE or SCOPE_COLUMN, or the rowID of a row 
	 * record in the table_name table for reference_scope of SCOPE_ROW or SCOPE_ROW_COLUMN */
	//String row_id_value = null;
	/** timestamp value in ISO 8601format as defined by the strftime function '%Y-%m-%dT%H:%M:%fZ'
	 *  format string applied to the current time */
	//String timestamp;
	/**gpkg_metadata table id column value for the metadata to which this gpkg_metadata_reference applies*/
	//int md_file_id;
	/** gpkg_metadata table id column value for the hierarchical parent gpkg_metadata for the gpkg_metadata 
	 * to which this gpkg_metadata_reference applies, or NULL if md_file_id forms the root of a metadata hierarchy */
	//int md_parent_id;
	
	/**
	 * 
	 */
	public GpkgMetaDataReference() {
		super(	TABLE_NAME, 
				new GpkgField[]{
					new GpkgField("reference_scope", "TEXT", "NOT NULL"),
					new GpkgField("table_name", "TEXT"),
					new GpkgField("column_name", "TEXT"),
					new GpkgField("row_id_value", "INTEGER"),
					new GpkgField("timestamp", "DATETIME", "NOT NULL", "(strftime('%Y-%m-%dT%H:%M:%fZ','now'))"),
					new GpkgField("md_file_id", "INTEGER", "NOT NULL"),
					new GpkgField("md_parent_id", "INTEGER")
				},
				new String[]{
					"CONSTRAINT crmr_mfi_fk FOREIGN KEY (md_file_id) REFERENCES gpkg_metadata(id)",
					"CONSTRAINT crmr_mpi_fk FOREIGN KEY (md_parent_id) REFERENCES gpkg_metadata(id)"}
			);
		
	}
}

//public static final String CREATE_TABLE_GPKG_METADATA_REFERENCE = "CREATE TABLE gpkg_metadata_reference ( "+
//		"reference_scope TEXT NOT NULL, "+
//		"table_name TEXT, "+
//		"column_name TEXT, "+
//		"row_id_value INTEGER, "+
//		"timestamp DATETIME NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ',CURRENT_TIMESTAMP)), "+
//		"md_file_id INTEGER NOT NULL, "+
//		"md_parent_id INTEGER, "+
//		"CONSTRAINT crmr_mfi_fk FOREIGN KEY (md_file_id) REFERENCES gpkg_metadata(id), "+
//		"CONSTRAINT crmr_mpi_fk FOREIGN KEY (md_parent_id) REFERENCES gpkg_metadata(id) )";