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
public class GpkgDataColumns extends GpkgTable {
	public static String TABLE_NAME = "gpkg_data_columns";
	/**
	 * 
	 */
	public GpkgDataColumns() {
		super(	TABLE_NAME, 
				new GpkgField[]{
					new GpkgField("table_name", "TEXT", "NOT NULL"),
					new GpkgField("column_name", "TEXT", "NOT NULL"),
					new GpkgField("name", "TEXT"),
					new GpkgField("title", "TEXT"),
					new GpkgField("description", "TEXT"),
					new GpkgField("mime_type", "TEXT"),
					new GpkgField("constraint_name", "TEXT"),
				},
				new String[]{
					"CONSTRAINT pk_gdc PRIMARY KEY (table_name, column_name)",
					"CONSTRAINT fk_gdc_tn FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name)"}
			);
		
	}
}

//public static final String CREATE_TABLE_GPKG_DATA_COLUMNS = "CREATE TABLE gpkg_data_columns ( "+
//		"table_name TEXT NOT NULL, "+
//		"column_name TEXT NOT NULL, "+
//		"name TEXT, "+
//		"title TEXT, "+
//		"description TEXT, "+
//		"mime_type TEXT, "+
//		"constraint_name TEXT, "+
//		"CONSTRAINT pk_gdc PRIMARY KEY (table_name, column_name), "+
//		"CONSTRAINT fk_gdc_tn FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name))";
