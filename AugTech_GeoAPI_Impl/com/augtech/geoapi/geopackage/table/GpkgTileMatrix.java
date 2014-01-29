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
public class GpkgTileMatrix extends GpkgTable {
	/** The internal DB table name */
	public static String TABLE_NAME = "gpkg_tile_matrix";
	/** A String defining a full SQL Insert statement for use with {@linkplain String#format(String, Object...)} */
	public static String STMT_INSERT = "INSERT INTO "+TABLE_NAME+" (table_name, zoom_level, matrix_width, matrix_height, tile_width, "+
			"tile_height, pixel_x_size, pixel_y_size) VALUES ('%s', %s, %s, %s, %s, %s, %s, %s);";
	/**
	 * 
	 */
	public GpkgTileMatrix() {
		super( TABLE_NAME,
				new GpkgField[]{
					new GpkgField("table_name", "TEXT", "NOT NULL"),
					new GpkgField("zoom_level", "INTEGER", "NOT NULL"),
					new GpkgField("matrix_width", "INTEGER", "NOT NULL"),
					new GpkgField("matrix_height", "INTEGER", "NOT NULL"),
					new GpkgField("tile_width", "INTEGER", "NOT NULL"),
					new GpkgField("tile_height", "INTEGER", "NOT NULL"),
					new GpkgField("pixel_x_size", "DOUBLE", "NOT NULL"),
					new GpkgField("pixel_y_size", "DOUBLE", "NOT NULL")
				},
				new String[]{
					"CONSTRAINT pk_ttm PRIMARY KEY (table_name, zoom_level)",
					"CONSTRAINT fk_tmm_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name)"
				}
			);
		
	}
	/** Insert a new 'zoom times 2' tile matrix definition into the GeoPackage.
	 * 
	 * @param geoPackage The GeoPackage to insert the records to
	 * @param tableName The table name this matrix relates to
	 * @param maxZoom The maximum (furthers inward) zoom level (usually 18)
	 * @param tilePixelsXY The size (in pixels) of the images to be stored
	 * @param pixelDistAtMaxZoomX The real-world X/Width distance in terrain units (in the correct spatial reference system for 
	 * the data) for one pixel at the most detailed zoom level.
	 * @param pixelDistAtMaxZoomY The real-world Y/Height distance in terrain units (in the correct spatial reference system for 
	 * the data) for one pixel at the most detailed zoom level.
	 * 
	 * @return True if all inserts were executed successfully.
	 */
	public boolean insertTimes2TileMatrix(GeoPackage geoPackage, String tableName, int maxZoom, 
			int tilePixelsXY, double pixelDistAtMaxZoomX, double pixelDistAtMaxZoomY) {
		
		String[] stmts = new String[maxZoom+1];

		for (int zoom=0; zoom < maxZoom+1; zoom++) {
			double matrixWH = Math.pow(2, zoom);
		
			stmts[zoom] = String.format(STMT_INSERT, 
							tableName,
							zoom, 
							matrixWH,
							matrixWH,
							tilePixelsXY,
							tilePixelsXY,
							Math.pow(2, maxZoom-zoom)*pixelDistAtMaxZoomX,
							Math.pow(2, maxZoom-zoom)*pixelDistAtMaxZoomY
							);

		}
		
		return geoPackage.getDatabase().execSQLWithRollback(stmts);
	}

}
//public static final String CREATE_TABLE_GPKG_TILE_MATRIX = "CREATE TABLE gpkg_tile_matrix ( "+
//		"table_name TEXT NOT NULL, "+
//		"zoom_level INTEGER NOT NULL, "+
//		"matrix_width INTEGER NOT NULL, "+
//		"matrix_height INTEGER NOT NULL, "+
//		"tile_width INTEGER NOT NULL, "+
//		"tile_height INTEGER NOT NULL, "+
//		"pixel_x_size DOUBLE NOT NULL, "+
//		"pixel_y_size DOUBLE NOT NULL, "+
//		"CONSTRAINT pk_ttm PRIMARY KEY (table_name, zoom_level), "+
//		"CONSTRAINT fk_tmm_table_name FOREIGN KEY (table_name) "+
//		"  REFERENCES gpkg_contents(table_name))";