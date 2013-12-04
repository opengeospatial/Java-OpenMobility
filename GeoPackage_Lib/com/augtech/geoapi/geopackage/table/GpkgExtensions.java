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
}
//public static final String CREATE_TABLE_GPKG_EXTENSIONS = "CREATE TABLE gpkg_extensions ( "+
//		"table_name TEXT, "+
//		"column_name TEXT, "+
//		"extension_name TEXT NOT NULL, "+
//		"definition TEXT NOT NULL, "+
//		"scope TEXT NOT NULL, "+
//		"CONSTRAINT ge_tce UNIQUE (table_name, column_name, extension_name))";
