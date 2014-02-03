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
public class GpkgContents extends GpkgTable {
	public static String TABLE_NAME = "gpkg_contents";
	/**
	 * 
	 */
	public GpkgContents() {
		super(	TABLE_NAME, 
				new GpkgField[]{
					new GpkgField("table_name", "TEXT", "PRIMARY KEY"),
					new GpkgField("data_type", "TEXT", "NOT NULL"),
					new GpkgField("identifier", "TEXT", "UNIQUE"),
					new GpkgField("description", "TEXT", null, "''"),
					new GpkgField("last_change", "DATETIME", "NOT NULL", "(strftime('%Y-%m-%dT%H:%M:%fZ','now'))"),
					new GpkgField("min_x", "DOUBLE"),
					new GpkgField("min_y", "DOUBLE"),
					new GpkgField("max_x", "DOUBLE"),
					new GpkgField("max_y", "DOUBLE"),
					new GpkgField("srs_id", "INTEGER")
				},
				new String[]{"CONSTRAINT fk_gc_r_srs_id FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys(srs_id)"}
			);
	}
}
//public static final String CREATE_TABLE_GPKG_CONTENTS = "CREATE TABLE gpkg_contents ( "+
//"table_name TEXT NOT NULL PRIMARY KEY, "+
//"data_type TEXT NOT NULL, "+
//"identifier TEXT UNIQUE, "+
//"description TEXT DEFAULT '', "+
//"last_change DATETIME NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ',CURRENT_TIMESTAMP)), "+
//"min_x DOUBLE, "+
//"min_y DOUBLE, "+
//"max_x DOUBLE, "+
//"max_y DOUBLE, "+
//"srs_id INTEGER, "+
//"CONSTRAINT fk_gc_r_srs_id FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys(srs_id) )";
//