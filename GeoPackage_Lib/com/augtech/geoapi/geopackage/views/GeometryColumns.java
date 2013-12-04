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
/** TODO This view includes a function that has yet to be created by this implementation,
 * therefore it will cause an error when trying to execute the creation statement.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class GeometryColumns extends GpkgView {
	/** The internal DB View name */
	public static String VIEW_NAME = "geometry_columns";
	/** Create a new GeometryColumns view - This has yet to be done 
	 * correctly, therefore should not be executed at the moment.
	 * 
	 */
	public GeometryColumns() {
		super(	VIEW_NAME, 
				"gpkg_geometry_columns", 
				new String[]{
					"table_name AS f_table_name",
					"column_name AS f_geometry_column",
					"geometry_type_name (geometry_type_name) AS geometry_type",
					"2 + (CASE z WHEN 1 THEN 1 WHEN 2 THEN 1 ELSE 0 END) + (CASE m WHEN 1 THEN 1 WHEN 2 THEN 1 ELSE 0 END) AS coord_dimension",
					"srs_id AS srid"
				}
		);

	}

}
//public static final String CREATE_VIEW_GEOMETRY_COLUMNS = "CREATE VIEW geometry_columns AS SELECT "+
//		"table_name AS f_table_name, "+
//		"column_name AS f_geometry_column, "+
//		"geometry_type_name (geometry_type_name) AS geometry_type, "+
//		"2 + (CASE z WHEN 1 THEN 1 WHEN 2 THEN 1 ELSE 0 END) "+
//		" + (CASE m WHEN 1 THEN 1 WHEN 2 THEN 1 ELSE 0 END)  "+
//		"AS coord_dimension, "+
//		"srs_id AS srid "+
//		"FROM gpkg_geometry_columns;";