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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.augtech.geoapi.feature.NameImpl;
import com.augtech.geoapi.feature.type.AttributeTypeImpl;
import com.augtech.geoapi.feature.type.GeometryDescriptorImpl;
import com.augtech.geoapi.feature.type.GeometryTypeImpl;
import com.augtech.geoapi.feature.type.SimpleFeatureTypeImpl;
import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.augtech.geoapi.geopackage.DateUtil;
import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;
import com.augtech.geoapi.geopackage.GpkgRecords;
import com.augtech.geoapi.geopackage.GpkgTable;
import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;
import com.vividsolutions.jts.geom.Geometry;


public class TilesTable extends GpkgTable {
	GeoPackage geoPackage = null;
	private TileMatrixInfo tileMatrixInfo = null;
	
	/**
	 * 
	 * @param geoPackage
	 * @param tableName
	 */
	public TilesTable(GeoPackage geoPackage, String tableName) {
		super(tableName, null, null);
		super.tableType = GpkgTable.TABLE_TYPE_TILES;
		this.geoPackage = geoPackage;
	}

	/** Create a new user Tiles table in the GeoPackage suitable for 'Slippy' map tiles
	 * (aka those used by Google, Bing, Yahoo, OpenStreetMap etc)
	 * 
	 * @param tilePixelsXY The size (in pixels) of the images to be stored
	 * @return True if the tiles table, and associated meta-data tables, are created successfully. False if not.
	 * @throws Exception
	 * @see {@link #create(BoundingBox, int, int, double, double)}
	 * 
	 */
	public boolean create(int tilePixelsXY) throws Exception {

		BoundingBox bbox = new BoundingBoxImpl(-20037508.34, 20037508.34, -20037508.34, 20037508.34,
				new CoordinateReferenceSystemImpl("3857"));

		return create(bbox, 18, tilePixelsXY, 0.597165, 0.597165);
		
	}
	/** Create a new user Tiles table in the GeoPackage. Note that the definition parameters
	 * are for the total area/ size etc that the tiles <i>could</i> cover, not the area/ size that
	 * the data <i>will</i> cover.
	 * 
	 * @param bbox The {@linkplain BoundingBox} for the total region these tiles <i>can</i> cover.
	 * @param maxZoom The maximum (furthers inward) zoom level (usually 18)
	 * @param tilePixelsXY The size (in pixels) of the images to be stored
	 * @param pixelDistAtMaxZoomX The real-world X/Width distance in terrain units (in the correct spatial reference system for 
	 * the data) for one pixel at the most detailed zoom level.
	 * @param pixelDistAtMaxZoomY The real-world Y/Height distance in terrain units (in the correct spatial reference system for 
	 * the data) for one pixel at the most detailed zoom level.
	 * 
	 * @return True if the tiles table, and associated meta-data tables, are created successfully. False if not.
	 * @throws Exception
	 * @see {@link #create(int)}
	 */
	public boolean create(BoundingBox bbox, int maxZoom, int tilePixelsXY, double pixelDistAtMaxZoomX, double pixelDistAtMaxZoomY) 
			throws Exception {
	
		if (isTableInGpkg(geoPackage)) {
			geoPackage.log.log(Level.INFO, "Table ["+tableName+"] already defined in "+GpkgContents.TABLE_NAME);
			return true;
		}

		// Doesn't exist in Contents, but does in DB, therefore not valid and drop
		if (isTableInDB(geoPackage)) {
			geoPackage.log.log(Level.WARNING, "Replacing table "+tableName);
			geoPackage.getDatabase().execSQL("DROP table ["+tableName+"]");
		}
		
		// Check SRS exists in gpkg_spatial_ref_sys table
		int srsID = Integer.parseInt( bbox.getCoordinateReferenceSystem().getName().getCode() );

		GpkgRecords records = geoPackage.getSystemTable(GpkgSpatialRefSys.TABLE_NAME)
								.query(geoPackage, "srs_id="+srsID);

		if (records.getFieldInt(0, "srs_id")!=srsID) 
			throw new Exception("SRS "+srsID+" does not exist in the gpkg_spatial_ref_sys table");
		
		// Create this table
		String tableDef = "CREATE TABLE ["+tableName+"] ("+
					  "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
					  "zoom_level INTEGER NOT NULL, "+
					  "tile_column INTEGER NOT NULL, "+
					  "tile_row INTEGER NOT NULL, "+
					  "tile_data BLOB NOT NULL, "+
					  "UNIQUE (zoom_level, tile_column, tile_row))";
		
		// Contents entry
		String contents = String.format("INSERT INTO %s (table_name, data_type, identifier, last_change, "+
					"min_x, min_y, max_x, max_y, srs_id) VALUES ('%s', '%s', '%s', '%s', %s, %s, %s, %s, %s);", 
					GpkgContents.TABLE_NAME,
					tableName,
					GpkgTable.TABLE_TYPE_TILES,
					tableName,
					DateUtil.serializeDateTime(System.currentTimeMillis(), true),
					bbox.getMinX(),
					bbox.getMinY(),
					bbox.getMaxX(),
					bbox.getMaxY(),
					srsID );

		
		// tile_matrix_set entry
		String tile_matrix_set = String.format("INSERT INTO %s (table_name, min_x, min_y, max_x, max_y, srs_id) "+
				" VALUES ('%s', %s, %s, %s, %s, %s);", 
				GpkgTileMatrixSet.TABLE_NAME,
				tableName,
				bbox.getMinX(),
				bbox.getMinY(),
				bbox.getMaxX(),
				bbox.getMaxY(),
				srsID );

		String[] statements = new String[]{
			tableDef,
			contents,
			tile_matrix_set
		};
		
		// Insert the tile matrix
		boolean success = geoPackage.getDatabase().execSQLWithRollback(statements);
		boolean tmSuccess = false;
		if (success) {
			GpkgTileMatrix tm = new GpkgTileMatrix();
			tmSuccess = tm.insertTimes2TileMatrix(geoPackage, tableName, maxZoom, 
					tilePixelsXY, pixelDistAtMaxZoomX, pixelDistAtMaxZoomY);
		}
		
		// This will also read back the core details.
		getTileMatrixInfo();
		
		return success && tmSuccess;
	}
	/** Issue a raw query on this table using a where clause
	 * 
	 * @param strWhere The where clause excluding the 'where'
	 * @return
	 * @throws Exception
	 */
	public GpkgRecords query(String strWhere) throws Exception {
		getTileMatrixInfo();
		return super.query(geoPackage, strWhere);
	}

	/**
	 * @return the BoundingBox from GpkgContents
	 */
	@Override
	public BoundingBox getBounds() {
		try {
			super.getContents(geoPackage);
		} catch (Exception e) {
			e.printStackTrace();
		}


		return super.getBounds();
	}
	/** Get a constructed SimpleFeatureType based on all the available
	 * details for this table.
	 * 
	 * @return
	 */
	public SimpleFeatureType getSchema() {
		
		// Build the geometry descriptor for this 'image' SimpleFeatureType.
		CoordinateReferenceSystem thisCRS = getBounds().getCoordinateReferenceSystem();
		GeometryType gType = new GeometryTypeImpl(
				new NameImpl("Envelope"),
				Geometry.class,
				new CoordinateReferenceSystemImpl( thisCRS.getName().getCode() ) );

		// We only have two attributes - The raster data and a bounding box for the tile
		ArrayList<AttributeType> attrs = new ArrayList<AttributeType>();
		attrs.add(new AttributeTypeImpl(new NameImpl("the_image"), Byte[].class ) );
		attrs.add(new AttributeTypeImpl(new NameImpl("the_geom"), Geometry.class) );
		attrs.add(new AttributeTypeImpl(new NameImpl("tile_column"), Integer.class) );
		attrs.add(new AttributeTypeImpl(new NameImpl("tile_row"), Integer.class) );
		attrs.add(new AttributeTypeImpl(new NameImpl("zoom_level"), Integer.class) );
		attrs.trimToSize();
		
		// Construct the feature type
		SimpleFeatureType featureType = new SimpleFeatureTypeImpl(
				new NameImpl( tableName ),
				attrs,
				new GeometryDescriptorImpl(gType, new NameImpl("the_geom"))
				);
		
		return featureType;
	}
	/**
	 * @return the lastChange
	 */
	@Override
	public Date getLastChange() {
		try {
			super.getContents(geoPackage);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return super.getLastChange();
	}
	/** Get the tile matrix information for this table. This is cached on the table
	 * for future use
	 * 
	 * @return
	 * @throws Exception
	 */
	public TileMatrixInfo getTileMatrixInfo() throws Exception {
		if (tileMatrixInfo!=null) return tileMatrixInfo;
		
		// Get the core details
		super.getContents(geoPackage);
		
		// Tile Matrix column details
		GpkgRecords gRecords = geoPackage.getSystemTable(GpkgTileMatrix.TABLE_NAME)
				.query(geoPackage, "table_name='"+tableName+"';");
		
		if (gRecords==null)
			throw new Exception("No tile matrix definition for "+tableName);
		
		Map<Integer, Collection<GpkgField>> tm = new HashMap<Integer, Collection<GpkgField>>();
		for (int i=0; i<gRecords.size(); i++) {
			
			tm.put(	gRecords.getFieldInt(i, "zoom_level"), gRecords.getFields(i) );
		}
		
		// Get bounds from tile_matrix_set
		gRecords = geoPackage.getSystemTable(GpkgTileMatrixSet.TABLE_NAME)
						.query(geoPackage, "table_name='"+tableName+"';");
		
		BoundingBox tmBox = new BoundingBoxImpl(
				gRecords.getFieldDouble(0,"min_x"),
				gRecords.getFieldDouble(0,"max_x"),
				gRecords.getFieldDouble(0,"min_y"),
				gRecords.getFieldDouble(0,"max_y"),
				new CoordinateReferenceSystemImpl(""+gRecords.getFieldInt(0, "srs_id"))
				);

		
		tileMatrixInfo = new TileMatrixInfo(tm, tmBox);
		
		return tileMatrixInfo;
	}
	/** An object to hold Tile Matrix information about this table
	 * 
	 *
	 */
	public class TileMatrixInfo {
		private Map<Integer, Collection<GpkgField>> matFields = new HashMap<Integer, Collection<GpkgField>>();
		int maxZoom = 1;
		BoundingBox bbox = null;
		
		/** Create a new TileMatrixInfo.
		 * 
		 * @param matFields A Map of TileMatrix fields by zoom level
		 * @param bbox The extents of the TileMatrixSet
		 */
		public TileMatrixInfo(Map<Integer, Collection<GpkgField>> matFields, BoundingBox bbox) {
			this.matFields = matFields;
			int z = 0;
			for (int t=0; t < matFields.keySet().size(); t++) if (t>z) z=t;
			this.bbox = bbox;
		}
		/** Get the highest zoom level defined for this matrix (the most detailed)
		 * 
		 * @return
		 */
		public int getMaxZoom() {
			return this.maxZoom;
		}
		/** Get a single pixel size for a tile at a specified zoom level
		 * 
		 * @param zoom The required zoom
		 * @return double[] as X and Y pixel size or Double.NaN, Double.NaN if the zoom level does not exist
		 */
		public double[] getPixelSize(int zoom) {
			double[] ret = new double[]{Double.NaN, Double.NaN};
			
			for (GpkgField gf : matFields.get(zoom)) {
				if (gf.getFieldName().equals("pixel_x_size")) {
					ret[0] = Double.valueOf(String.valueOf(gf.getValue()) );
				} else if (gf.getFieldName().equals("pixel_y_size")) {
					ret[1] = Double.valueOf(String.valueOf(gf.getValue()) );
				}
			}
			
			return ret;
		}
		/** Get the tile size in pixels for a single tile at a specified zoom level
		 * 
		 * @param zoom The required zoom
		 * @return int[] as X and Y number of pixels or -1,-1 if the zoom level does not exist
		 */
		public int[] getTileSize(int zoom) {
			int[] ret = new int[]{-1,-1};
			
			for (GpkgField gf : matFields.get(zoom)) {
				if (gf.getFieldName().equals("tile_width")) {
					ret[0] = Integer.valueOf(String.valueOf(gf.getValue()) );
				} else if (gf.getFieldName().equals("tile_height")) {
					ret[1] = Integer.valueOf(String.valueOf(gf.getValue()) );
				}
			}
			
			return ret;
		}
		/** Get the size of the matrix at the specified zoom
		 * 
		 * @param zoom The required zoom
		 * @return int[] as width and height in tiles or -1,-1 if the zoom level does not exist
		 */
		public int[] getMatrixSize(int zoom) {
			int[] ret = new int[2];
			
			for (GpkgField gf : matFields.get(zoom)) {
				if (gf.getFieldName().equals("matrix_width")) {
					ret[0] = Integer.valueOf(String.valueOf(gf.getValue()) );
				} else if (gf.getFieldName().equals("matrix_height")) {
					ret[1] = Integer.valueOf(String.valueOf(gf.getValue()) );
				}
			}
			
			return ret;
		}
		/** Get the {@linkplain BoundingBox} of a single tile
		 * 
		 * @param x X tile reference (column)
		 * @param y Y tile reference (row)
		 * @param zoom Zoom level
		 * @return A new Bounding box in the tile matrix CoordinateReferenceSystem or an
		 * empty BoundingBox if the zoom, x or y references are outside the bounds of this matrix
		 */
		public BoundingBox getTileBounds(int x, int y, int zoom) {
			
			int[] mXY = getMatrixSize( zoom );
			if (x>mXY[0] || x<1 || y>mXY[1] || y<1)
				return new BoundingBoxImpl(bbox.getCoordinateReferenceSystem());
			
			double[] pXY = getPixelSize( zoom );
			int[] tXY = getTileSize( zoom );
			
			return new BoundingBoxImpl(
					bbox.getMinX()+( pXY[0]*tXY[0]*x ),
					bbox.getMinX()+( pXY[0]*tXY[0]*(x+1) ),
					bbox.getMaxY()-( pXY[1]*tXY[1]*y ),
					bbox.getMaxY()-( pXY[1]*tXY[1]*(y+1) ),
					bbox.getCoordinateReferenceSystem()
					);
		}
	}
}
