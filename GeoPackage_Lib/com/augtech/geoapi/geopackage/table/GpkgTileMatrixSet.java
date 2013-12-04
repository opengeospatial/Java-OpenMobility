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
public class GpkgTileMatrixSet extends GpkgTable {
	/** The internal DB table name */
	public static String TABLE_NAME = "gpkg_tile_matrix_set";
	/**
	 * 
	 */
	public GpkgTileMatrixSet() {
		super(	TABLE_NAME, 
				new GpkgField[]{
					new GpkgField("table_name", "TEXT", "NOT NULL PRIMARY KEY"),
					new GpkgField("srs_id", "INTEGER", "NOT NULL"),
					new GpkgField("min_x", "DOUBLE", "NOT NULL"),
					new GpkgField("min_y", "DOUBLE", "NOT NULL"),
					new GpkgField("max_x", "DOUBLE", "NOT NULL"),
					new GpkgField("max_y", "DOUBLE", "NOT NULL")
				},
				new String[]{
					"CONSTRAINT fk_gtms_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name)",
					"CONSTRAINT fk_gtms_srs FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys (srs_id)"
				}
			);
		
	}
}
//public static final String CREATE_TABLE_GPKG_TILE_MATRIX_SET = "CREATE TABLE gpkg_tile_matrix_set ( "+
//		"table_name TEXT NOT NULL PRIMARY KEY, "+
//		"srs_id INTEGER NOT NULL, "+
//		"min_x DOUBLE NOT NULL, "+
//		"min_y DOUBLE NOT NULL, "+
//		"max_x DOUBLE NOT NULL, "+
//		"max_y DOUBLE NOT NULL, 
//		CONSTRAINT fk_gtms_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name), "+
//		"CONSTRAINT fk_gtms_srs FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys (srs_id) )";