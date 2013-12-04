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

/** Static Trigger Definitions from Annex D that can be run as SQL statements
 * purely for the creation of the triggers.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class GpkgTriggers {

	public static final String CREATE_TRIGGER_GPKG_TILE_MATRIX_ZOOM_LEVEL_INSERT = 
			"CREATE TRIGGER 'gpkg_tile_matrix_zoom_level_insert' "+
					"BEFORE INSERT ON 'gpkg_tile_matrix' "+
					"FOR EACH ROW BEGIN "+
					"SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: zoom_level cannot be less than 0') "+
					"WHERE (NEW.zoom_level < 0); "+
					"END";

	public static final String CREATE_TRIGGER_GPKG_TILE_MATRIX_ZOOM_LEVEL_UPDATE = 
			"CREATE TRIGGER 'gpkg_tile_matrix_zoom_level_update' "+
					"BEFORE UPDATE of zoom_level ON 'gpkg_tile_matrix' "+
					"FOR EACH ROW BEGIN "+
					"SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: zoom_level cannot be less than 0') "+
					"WHERE (NEW.zoom_level < 0); "+
					"END";

	public static final String CREATE_TRIGGER_GPKG_TILE_MATRIX_MATRIX_WIDTH_INSERT = 
			"CREATE TRIGGER 'gpkg_tile_matrix_matrix_width_insert' "+
					"BEFORE INSERT ON 'gpkg_tile_matrix' "+
					"FOR EACH ROW BEGIN "+
					"SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: matrix_width cannot be less than 1') "+
					"WHERE (NEW.matrix_width < 1); "+
					"END";

	public static final String CREATE_TRIGGER_GPKG_TILE_MATRIX_MATRIX_WIDTH_UPDATE = 
			"CREATE TRIGGER 'gpkg_tile_matrix_matrix_width_update' "+
					"BEFORE UPDATE OF matrix_width ON 'gpkg_tile_matrix' "+
					"FOR EACH ROW BEGIN "+
					"SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: matrix_width cannot be less than 1') "+
					"WHERE (NEW.matrix_width < 1); "+
					"END";

	public static final String CREATE_TRIGGER_GPKG_TILE_MATRIX_MATRIX_HEIGHT_INSERT = 
			"CREATE TRIGGER 'gpkg_tile_matrix_matrix_height_insert' "+
					"BEFORE INSERT ON 'gpkg_tile_matrix' "+
					"FOR EACH ROW BEGIN "+
					"SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: matrix_height cannot be less than 1') "+
					"WHERE (NEW.matrix_height < 1); "+
					"END";

	public static final String CREATE_TRIGGER_GPKG_TILE_MATRIX_MATRIX_HEIGHT_UPDATE = 
			"CREATE TRIGGER 'gpkg_tile_matrix_matrix_height_update' "+
					"BEFORE UPDATE OF matrix_height ON 'gpkg_tile_matrix' "+
					"FOR EACH ROW BEGIN "+
					"SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: matrix_height cannot be less than 1') "+
					"WHERE (NEW.matrix_height < 1); "+
					"END";

	public static final String CREATE_TRIGGER_GPKG_TILE_MATRIX_PIXEL_X_SIZE_INSERT = 
			"CREATE TRIGGER 'gpkg_tile_matrix_pixel_x_size_insert' "+
					"BEFORE INSERT ON 'gpkg_tile_matrix' "+
					"FOR EACH ROW BEGIN "+
					"SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: pixel_x_size must be greater than 0') "+
					"WHERE NOT (NEW.pixel_x_size > 0); "+
					"END";

	public static final String CREATE_TRIGGER_GPKG_TILE_MATRIX_PIXEL_X_SIZE_UPDATE = 
			"CREATE TRIGGER 'gpkg_tile_matrix_pixel_x_size_update' "+
					"BEFORE UPDATE OF pixel_x_size ON 'gpkg_tile_matrix' "+
					"FOR EACH ROW BEGIN "+
					"SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: pixel_x_size must be greater than 0') "+
					"WHERE NOT (NEW.pixel_x_size > 0); "+
					"END";

	public static final String CREATE_TRIGGER_GPKG_TILE_MATRIX_PIXEL_Y_SIZE_INSERT = 
			"CREATE TRIGGER 'gpkg_tile_matrix_pixel_y_size_insert' "+
					"BEFORE INSERT ON 'gpkg_tile_matrix' "+
					"FOR EACH ROW BEGIN "+
					"SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: pixel_y_size must be greater than 0') "+
					"WHERE NOT (NEW.pixel_y_size > 0); "+
					"END";

	public static final String CREATE_TRIGGER_GPKG_TILE_MATRIX_PIXEL_Y_SIZE_UPDATE = 
			"CREATE TRIGGER 'gpkg_tile_matrix_pixel_y_size_update' "+
					"BEFORE UPDATE OF pixel_y_size ON 'gpkg_tile_matrix' "+
					"FOR EACH ROW BEGIN "+
					"SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: pixel_y_size must be greater than 0') "+
					"WHERE NOT (NEW.pixel_y_size > 0); "+
					"END";
	
	/** An array of all GeoPackage trigger definitions from Annex D of the Spec */
	public static final String[] ALL_TRIGGERS = new String[] {
		CREATE_TRIGGER_GPKG_TILE_MATRIX_ZOOM_LEVEL_INSERT,
		CREATE_TRIGGER_GPKG_TILE_MATRIX_ZOOM_LEVEL_UPDATE,
		CREATE_TRIGGER_GPKG_TILE_MATRIX_MATRIX_WIDTH_INSERT,
		CREATE_TRIGGER_GPKG_TILE_MATRIX_MATRIX_WIDTH_UPDATE,
		CREATE_TRIGGER_GPKG_TILE_MATRIX_MATRIX_HEIGHT_INSERT,
		CREATE_TRIGGER_GPKG_TILE_MATRIX_MATRIX_HEIGHT_UPDATE,
		CREATE_TRIGGER_GPKG_TILE_MATRIX_PIXEL_X_SIZE_INSERT,
		CREATE_TRIGGER_GPKG_TILE_MATRIX_PIXEL_X_SIZE_UPDATE,
		CREATE_TRIGGER_GPKG_TILE_MATRIX_PIXEL_Y_SIZE_INSERT,
		CREATE_TRIGGER_GPKG_TILE_MATRIX_PIXEL_Y_SIZE_UPDATE
	};
	
}
