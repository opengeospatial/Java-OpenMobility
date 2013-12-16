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

import java.util.HashMap;
import java.util.Map;

import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;
import com.augtech.geoapi.geopackage.GpkgTable;

/**
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class GpkgExtensions extends GpkgTable {
	public static String TABLE_NAME = "gpkg_extensions";
	/**
	 * 
	 */
	public GpkgExtensions() {
		super(	TABLE_NAME, 
				new GpkgField[]{
					new GpkgField("table_name", "TEXT"),
					new GpkgField("column_name", "TEXT"),
					new GpkgField("extension_name", "TEXT", "NOT NULL"),
					new GpkgField("definition", "TEXT", "NOT NULL"),
					new GpkgField("scope", "TEXT", "NOT NULL")
				},
				new String[]{"CONSTRAINT ge_tce UNIQUE (table_name, column_name, extension_name)"}
			);
		
	}
	/** Insert a specific extension in to the GeoPackage. The #Extension will be validated
	 * prior to insert.
	 * 
	 * @param geoPackage The GeoPackage to insert into
	 * @param extension 
	 * @return The row id of the new record or -1 if the Extension is not valid or the insert
	 * failed.
	 */
	public long insert(GeoPackage geoPackage, Extension extension) {
		
		if (!extension.isValid()) return -1;
		
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("table_name", extension.tableName);
		values.put("column_name", extension.columnName);
		values.put("extension_name", extension.extensionName);
		values.put("definition", extension.definition);
		values.put("scope", extension.scope);
		
		return super.insert(geoPackage, values);
	}
	/** A class to hold gpkg_extension information
	 * 
	 *
	 */
	public static class Extension {
		/** Name of the table that requires the extension. When NULL, the extension is required for 
		 * the entire GeoPackage. SHALL NOT be NULL when the column_name is not NULL.*/
		public String tableName = "";
		/** Name of the column that requires the extension. 
		 * When NULL, the extension is required for the entire table. */
		public String columnName = "";
		/** The case sensitive name of the extension that is required, 
		 * in the form <author>_<extension name>. */
		public String extensionName = "";
		/** Definition of the extension in the form specified by the template in 
		 * Annex I or reference thereto.*/
		public String definition = "";
		/** Indicates scope of extension effects on readers / writers: "read-write" or 
		 * "write-only" in lowercase. */
		public String scope = "";
		
		public Extension() {
			
		}
		
		/** Is this extension valid for GeoPackage insert?
		 * 
		 * @return
		 */
		public boolean isValid() {
			// TODO: Validate the Extension
			return true;
		}
	}
}
//public static final String CREATE_TABLE_GPKG_EXTENSIONS = "CREATE TABLE gpkg_extensions ( "+
//		"table_name TEXT, "+
//		"column_name TEXT, "+
//		"extension_name TEXT NOT NULL, "+
//		"definition TEXT NOT NULL, "+
//		"scope TEXT NOT NULL, "+
//		"CONSTRAINT ge_tce UNIQUE (table_name, column_name, extension_name))";
