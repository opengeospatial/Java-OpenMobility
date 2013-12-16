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
	public static final String[] ALL_STANDARD_TRIGGERS = new String[] {
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
	
	/** Formatted String to create one of the Spatial index triggers. {0}=Table Name, {1}=Column Name, {2}=Index Column name 
	 * Conditions: Insertion of non-empty geometry Actions   : Insert record into rtree */
	public static final String CREATE_TRIGGER_SPATIAL_INSERT = "CREATE TRIGGER rtree_{0}_{1}_insert AFTER INSERT ON {0}" +
			" WHEN (new.{1} NOT NULL AND NOT ST_IsEmpty(NEW.{1}))" +
			" BEGIN INSERT OR REPLACE INTO rtree_{0}_{1} VALUES (" +
			" NEW.{2}, ST_MinX(NEW.{1}), ST_MaxX(NEW.{1}), ST_MinY(NEW.{1}), ST_MaxY(NEW.{1})"+
			" ); END;";
	
	/** Formatted String to create one of the Spatial index triggers. {0}=Table Name, {1}=Column Name, {2}=Index Column name
	 * Conditions: Update of geometry column to non-empty geometry 
	 * No row ID change Actions   : Update record in rtree */
	public static final String CREATE_TRIGGER_SPATIAL_UPDATE1 = "CREATE TRIGGER rtree_{0}_{1}_update1 AFTER UPDATE OF {1} ON {0}" +
			" WHEN OLD.{2} = NEW.{2} AND (NEW.{1} NOTNULL AND NOT ST_IsEmpty(NEW.{1}))" +
			" BEGIN INSERT OR REPLACE INTO rtree_{0}_{1} VALUES (" +
			" NEW.{2}, ST_MinX(NEW.{1}), ST_MaxX(NEW.{1}), ST_MinY(NEW.{1}), ST_MaxY(NEW.{1})"+
			" ); END;";
	
	/** Formatted String to create one of the Spatial index triggers. {0}=Table Name, {1}=Column Name, {2}=Index Column name
	 * Conditions: Update of geometry column to empty geometry
	 *  No row ID change  Actions   : Remove record from rtree */
	public static final String CREATE_TRIGGER_SPATIAL_UPDATE2 = "CREATE TRIGGER rtree_{0}_{1}_update2 AFTER UPDATE OF {1} ON {0}" +
			" WHEN OLD.{2} = NEW.{2} AND (NEW.{1} ISNULL OR ST_IsEmpty(NEW.{1}))" +
			" BEGIN DELETE FROM rtree_{0}_{1} WHERE id = OLD.{2}; END;";
	
	/** Formatted String to create one of the Spatial index triggers. {0}=Table Name, {1}=Column Name, {2}=Index Column name
	 * Conditions: Update of any column
               Row ID change
               Non-empty geometry
   		Actions   : Remove record from rtree for old rowid
               Insert record into rtree for new rowid  */
	public static final String CREATE_TRIGGER_SPATIAL_UPDATE3 = "CREATE TRIGGER rtree_{0}_{1}_update3 AFTER UPDATE OF {1} ON {0}" +
			" WHEN OLD.{2} != NEW.{2} AND (NEW.{2} NOTNULL AND NOT ST_IsEmpty(NEW.{2}))" +
			" BEGIN DELETE FROM rtree_{0}_{1} WHERE id = OLD.{2};" +
			" INSERT OR REPLACE INTO rtree_{0}_{1} VALUES (" +
			" NEW.{2}, ST_MinX(NEW.{1}), ST_MaxX(NEW.{1}), ST_MinY(NEW.{1}), ST_MaxY(NEW.{1})"+
			" ); END;";
	
	/** Formatted String to create one of the Spatial index triggers. {0}=Table Name, {1}=Column Name, {2}=Index Column name
	 * Conditions: Update of any column
               Row ID change 
               Empty geometry
   		Actions   : Remove record from rtree for old and new rowid */
	public static final String CREATE_TRIGGER_SPATIAL_UPDATE4 = "CREATE TRIGGER rtree_{0}_{1}_update4 AFTER UPDATE ON {0}" +
			" WHEN OLD.{2} != NEW.{2} AND (NEW.{1} ISNULL OR ST_IsEmpty(NEW.{1}))" +
			" BEGIN DELETE FROM rtree_{0}_{1} WHERE id IN (OLD.{2}, NEW.{2}); END;";
	
	/** Formatted String to create one of the Spatial index triggers. {0}=Table Name, {1}=Column Name, {2}=Index Column name
	 * Conditions: Row deleted  Actions   : Remove record from rtree for old rowid */
	public static final String CREATE_TRIGGER_SPATIAL_DELETE = "CREATE TRIGGER rtree_{0}_{1}_delete AFTER DELETE ON {0}" +
			" WHEN old.{1} NOT NULL" +
			" BEGIN DELETE FROM rtree_{0}_{1} WHERE id = OLD.{2}; END;";

	public static final String[] SPATIAL_TRIGGERS = new String[]{
		CREATE_TRIGGER_SPATIAL_INSERT,
		CREATE_TRIGGER_SPATIAL_UPDATE1,
		CREATE_TRIGGER_SPATIAL_UPDATE2,
		CREATE_TRIGGER_SPATIAL_UPDATE3,
		CREATE_TRIGGER_SPATIAL_UPDATE4,
		CREATE_TRIGGER_SPATIAL_DELETE
	};

}
