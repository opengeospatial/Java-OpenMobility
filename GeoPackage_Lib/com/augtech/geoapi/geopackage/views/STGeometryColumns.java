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
package com.augtech.geoapi.geopackage.views;
/**
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class STGeometryColumns extends GpkgView {
	/** The internal DB View name */
	public static String VIEW_NAME = "st_geometry_columns";
	/**
	 * 
	 */
	public STGeometryColumns() {
		super(	VIEW_NAME, 
				"gpkg_geometry_columns AS g JOIN gpkg_spatial_ref_sys AS s", 
				new String[]{
					"table_name",
					"column_name",
					"\"ST_\" || geometry_type_name",
					"g.srs_id",
					"srs_name"
				},
				"g.srs_id = s.srs_id"
		);

	}

}
//public static final String CREATE_VIEW_ST_GEOMETRY_COLUMNS = "CREATE VIEW st_geometry_columns AS SELECT "+
//		"table_name, "+
//		"column_name, "+
//		"\"ST_\" || geometry_type_name, "+
//		"g.srs_id, "+
//		"srs_name "+
//		"FROM gpkg_geometry_columns as g JOIN gpkg_spatial_ref_sys AS s "+
//		"WHERE g.srs_id = s.srs_id;";