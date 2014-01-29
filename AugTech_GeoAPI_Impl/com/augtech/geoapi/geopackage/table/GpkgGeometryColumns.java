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
public class GpkgGeometryColumns extends GpkgTable {
	public static String TABLE_NAME = "gpkg_geometry_columns";
	/**
	 * 
	 */
	public GpkgGeometryColumns() {
		super(	TABLE_NAME, 
				new GpkgField[]{
					new GpkgField("table_name", "TEXT", "NOT NULL"),
					new GpkgField("column_name", "TEXT", "NOT NULL"),
					new GpkgField("geometry_type_name", "TEXT", "NOT NULL"),
					new GpkgField("srs_id", "INTEGER", "NOT NULL"),
					new GpkgField("z", "TINYINT", "NOT NULL"),
					new GpkgField("m", "TINYINT", "NOT NULL")
				},
				new String[]{
					"CONSTRAINT pk_geom_cols PRIMARY KEY (table_name, column_name)",
					"CONSTRAINT uk_gc_table_name UNIQUE (table_name)",
					"CONSTRAINT fk_gc_tn FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name)",
					"CONSTRAINT fk_gc_srs FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys (srs_id)"
				}
			);
		
	}

}
//public static final String CREATE_TABLE_GPKG_GEOMETRY_COLUMNS = "CREATE TABLE gpkg_geometry_columns ( "+
//		"table_name TEXT NOT NULL, "+
//		"column_name TEXT NOT NULL, "+
//		"geometry_type_name TEXT NOT NULL, "+
//		"srs_id INTEGER NOT NULL, "+
//		"z TINYINT NOT NULL, "+
//		"m TINYINT NOT NULL, "+
//		"CONSTRAINT pk_geom_cols PRIMARY KEY (table_name, column_name), "+
//		"CONSTRAINT uk_gc_table_name UNIQUE (table_name), "+
//		"CONSTRAINT fk_gc_tn FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name), "+
//		"CONSTRAINT fk_gc_srs FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys (srs_id))";