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
		String.format(STMT_INSERT, "GCS_WGS_1984", 4326, "EPSG", 4326, "GEOGCS[\"WGS 84\", DATUM[\"WGS_1984\", SPHEROID[\"WGS 84\",6378137,298.257223563, AUTHORITY[\"EPSG\",\"7030\"]]"+
				", AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\" 8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]], AUTHORITY[\"EPSG\",\"4326\"]]", null),
		String.format(STMT_INSERT, "British National Grid", 27700, "EPSG", 27700, "PROJCS[\"OSGB 1936 / British National Grid\",GEOGCS[\"OSGB 1936\",DATUM[\"OSGB_1936\","+
				"SPHEROID[\"Airy 1830\",6377563.396,299.3249646,AUTHORITY[\"EPSG\",\"7001\"]],AUTHORITY[\"EPSG\",\"6277\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]]"+
				",AUTHORITY[\"EPSG\",\"4277\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",49],PARAMETER[\"central_meridian\",-2],PARAMETER[\"scale_factor\",0.9996012717],"+
				"PARAMETER[\"false_easting\",400000],PARAMETER[\"false_northing\",-100000],AUTHORITY[\"EPSG\",\"27700\"],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH]]", "OSGB 1936 / British National Grid"),
		String.format(STMT_INSERT, "Pseudo-Mercator", 3857, "EPSG", 3857, "PROJCS[\"WGS 84 / Pseudo-Mercator\",GEOGCS[\"Popular Visualisation CRS\",DATUM[\"Popular_Visualisation_Datum\","+
				"SPHEROID[\"Popular Visualisation Sphere\",6378137,0,AUTHORITY[\"EPSG\",\"7059\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\"EPSG\",\"6055\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,"+
				"AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4055\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"central_meridian\",0],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],"+
				"PARAMETER[\"false_northing\",0],AUTHORITY[\"EPSG\",\"3785\"],AXIS[\"X\",EAST],AXIS[\"Y\",NORTH]]", "WGS 84 - Spherical Mercator")
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
	/** Insert a new SRS definition into the GeoPackage if it doesn't already
	 * exist.
	 * 
	 * @param geoPackage
	 * @param name
	 * @param srsID
	 * @param organization
	 * @param definition
	 * @param description
	 */
	public void insertSpatialRefSys(GeoPackage geoPackage, String name, int srsID, String organization, String definition, String description) {
		if (geoPackage.isSRSLoaded(""+srsID)) return;
		
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