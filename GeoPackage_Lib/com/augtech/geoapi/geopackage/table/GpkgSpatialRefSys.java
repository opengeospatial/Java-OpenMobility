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

import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;
import com.augtech.geoapi.geopackage.GpkgTable;

/**
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class GpkgSpatialRefSys extends GpkgTable {
	/** The internal DB table name */
	public static String TABLE_NAME = "gpkg_spatial_ref_sys";
	/** A String defining a full SQL Insert statement for use with {@linkplain String#format(String, Object...)} */
	static final String STMT_INSERT = "INSERT INTO gpkg_spatial_ref_sys (srs_name, srs_id, organization, organization_coordsys_id, definition, description) "+
							"VALUES ('%s', %s, '%s', %s, '%s', '%s');";
	
	/** Insert for default spatial reference systems including WGS84, British National Grid and Spherical Mercator*/
	public static final String[] INSERT_DEFAULT_SPATIAL_REF_SYS = new String[] {
		String.format(STMT_INSERT, "Undefined Geographic", 0, "NONE", 0, "undefined", null),
		String.format(STMT_INSERT, "Undefined Cartesian", -1, "NONE", -1, "undefined", null),
		String.format(STMT_INSERT, "WGS84", 4326, "EPSG", 4326, "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs  <>", "WGS84"),
		String.format(STMT_INSERT, "British National Grid", 27700, "EPSG", 27700, "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +ellps=airy +datum=OSGB36 +units=m +no_defs  <>", "OSGB 1936 / British National Grid"),
		String.format(STMT_INSERT, "Pseudo Mercator", 3857, "EPSG", 3857, "+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs <>", "WGS 84 - Spherical Mercator")
	};
	
	/**
	 * 
	 */
	public GpkgSpatialRefSys() {
		super(	TABLE_NAME, 
				new GpkgField[]{
					new GpkgField("srs_name", "TEXT", "NOT NULL"),
					new GpkgField("srs_id", "INTEGER", "PRIMARY KEY"),
					new GpkgField("organization", "TEXT", "NOT NULL"),
					new GpkgField("organization_coordsys_id", "INTEGER", "NOT NULL"),
					new GpkgField("definition", "TEXT", "NOT NULL"),
					new GpkgField("description", "TEXT")
				},
				null
			);
		
	}
	/** Insert a new SRS definition into the GeoPackage
	 * 
	 * @param geoPackage
	 * @param name
	 * @param srsID
	 * @param organization
	 * @param definition
	 * @param description
	 */
	public void insertSpatialRefSys(GeoPackage geoPackage, String name, int srsID, String organization, String definition, String description) {

		String sql = String.format(STMT_INSERT, name, srsID, organization, srsID, definition, description);
		
		geoPackage.getDatabase().execSQL(sql);
		
	}
}
//public static final String CREATE_TABLE_SPATIAL_REF_SYS = "CREATE TABLE gpkg_spatial_ref_sys ( "+
//		"srs_name TEXT NOT NULL, "+
//		"srs_id INTEGER NOT NULL PRIMARY KEY, "+
//		"organization TEXT NOT NULL, "+
//		"organization_coordsys_id INTEGER NOT NULL, "+
//		"definition  TEXT NOT NULL, "+
//		"description TEXT	);";