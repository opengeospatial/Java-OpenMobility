/*
 * GeoPackage.java
 * 
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

package com.augtech.geoapi.geopackage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.augtech.geoapi.feature.NameImpl;
import com.augtech.geoapi.feature.SimpleFeatureImpl;
import com.augtech.geoapi.feature.type.AttributeTypeImpl;
import com.augtech.geoapi.feature.type.GeometryDescriptorImpl;
import com.augtech.geoapi.feature.type.GeometryTypeImpl;
import com.augtech.geoapi.feature.type.SimpleFeatureTypeImpl;
import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.augtech.geoapi.geopackage.geometry.GeometryDecoder;
import com.augtech.geoapi.geopackage.geometry.OGCWKBWriter;
import com.augtech.geoapi.geopackage.geometry.StandardGeometryDecoder;
import com.augtech.geoapi.geopackage.table.FeatureField;
import com.augtech.geoapi.geopackage.table.FeaturesTable;
import com.augtech.geoapi.geopackage.table.FeaturesTable.GeometryInfo;
import com.augtech.geoapi.geopackage.table.GpkgContents;
import com.augtech.geoapi.geopackage.table.GpkgDataColumnConstraint;
import com.augtech.geoapi.geopackage.table.GpkgDataColumnConstraint.DataColumnConstraint;
import com.augtech.geoapi.geopackage.table.GpkgDataColumns;
import com.augtech.geoapi.geopackage.table.GpkgExtensions;
import com.augtech.geoapi.geopackage.table.GpkgExtensions.Extension;
import com.augtech.geoapi.geopackage.table.GpkgGeometryColumns;
import com.augtech.geoapi.geopackage.table.GpkgMetaData;
import com.augtech.geoapi.geopackage.table.GpkgMetaDataReference;
import com.augtech.geoapi.geopackage.table.GpkgSpatialRefSys;
import com.augtech.geoapi.geopackage.table.GpkgTileMatrix;
import com.augtech.geoapi.geopackage.table.GpkgTileMatrixSet;
import com.augtech.geoapi.geopackage.table.GpkgTriggers;
import com.augtech.geoapi.geopackage.table.TilesTable;
import com.augtech.geoapi.geopackage.table.TilesTable.TileMatrixInfo;
import com.augtech.geoapi.geopackage.views.GpkgView;
import com.augtech.geoapi.geopackage.views.STGeometryColumns;
import com.augtech.geoapi.geopackage.views.STSpatialRefSys;
import com.augtech.geoapi.geopackage.views.SpatialRefSys;
import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ByteOrderValues;

public class GeoPackage {
	protected ISQLDatabase sqlDB = null;
	protected File dbFile = null;
	
	public enum JavaType {
		INTEGER,
		STRING,
		BOOLEAN,
		FLOAT,
		DOUBLE,
		BYTE_ARR,
		UNKNOWN
	}
	/** A map of possible SQL Field types to {@link JavaType} enum values.
	 * Field type names are all lowercase */
	public Map<String, JavaType> sqlTypeMap = new HashMap<String, JavaType>();
	
	public Logger log = Logger.getAnonymousLogger();
	private Map<String, GpkgTable> sysTables = new HashMap<String,  GpkgTable>();
	private Map<String, GpkgView> sysViews = new HashMap<String, GpkgView>();
	private Map<String, GpkgTable> userTables = new HashMap<String, GpkgTable>();
	
	/** The name to create (if required) and test for use as a FeatureID within the GeoPackage */
	public static String FEATURE_ID_FIELD_NAME = "feature_id";
	/** For each new FeaturesTable, create an R*Tree index if the SQLite library supports it?
	 * Default is True. If the library does not support R*Tree ({@link ISQLDatabase#hasRTreeEnabled()}
	 * then indexes cannot be created. */
	public static final boolean CREATE_RTREE_FOR_FEATURES = true;
	/** The OGC GeoPackage specification these statements relate to */
	public static final String SPEC_VERSION = "OGC 12-128r9 - 0.9.7 - v8";
	/** The maximum currently supported GeoPacakge version */
	public static final int MAX_GPKG_VERSION = 0;
	/** The Sqlite registered application_id for a GeoPackage */
	public static final int GPKG_APPLICATION_ID = Integer.decode("0x47503130");
	
	/** If True, reading of GeoPackage headers, pragmas and Geometry encodings will
	 * be validated against the specification and exceptions thrown if not valid.
	 * If False, checks will be performed, but exceptions won't be thrown unless
	 * data cannot be understood. Typical examples are the application_id pragma and Geometry.*/
	public static boolean MODE_STRICT = true;
		
	/** The Geometry version to write in to the Geometry columns. Default is 0 
	 * for Version 1.0 */
	public static int GPKG_GEOM_HEADER_VERSION = 0;
	
	/** If {@code True} insert StandardGeoPackageBinary geometries into the GeoPackage.
	 *  If {@code False} then the Geometry header is set to ExtendedGeoPackageBinary 
	 *  (which this implementation does not yet implement - clause 3.1.2, Annex K of spec).
	 *  Default is {@code True} */
	public static boolean GPKG_GEOMETRY_STANDARD = true;
	/** Encode new Geometry in Little Endian order? Default is {@code False} */
	public static boolean GPKG_GEOMETRY_LITTLE_ENDIAN = false;
	
	public static final int Z_M_VALUES_PROHIBIT = 0;
	public static final int Z_M_VALUES_MANDATORY = 1;
	public static final int Z_M_VALUES_OPTIONAL = 2;
	
	/** An array of extensions applicable to this GeoPackage */
	protected Extension[] gpkgExtensions = null;
	/** The maximum number of records to fetch in one go through the cursor. Default is 1000.
	 * Increasing this number <i>may</i> result in slightly faster queries on large recordsets,
	 * but <i>could</i> also result in memory exceptions or missing records (especially on mobile
	 * devices with limited memory. (Tested on Android at 1000) */
	public static int MAX_RECORDS_PER_CURSOR = 1000;
	
	/** Connect to, or create a new GeoPackage with the supplied name and version.<p>
	 * If the supplied name already exists then the database is checked to see if it
	 * is a valid GeoPackage. If the supplied file does not exist, a new empty GeoPackage
	 * is created with the supplied name.
	 * 
	 * @param fileName The name of the GeoPackage to create or connect to. The .gpkg extension is added 
	 * if not supplied.
	 * @param overwrite Overwrite the existing GeoPackage?
	 * @throws Exception If an existing GeoPackage fails the validity check.
	 * @see #isGPKGValid()
	 */
	public GeoPackage(ISQLDatabase sqlDB, boolean overwrite) {
		
		if (!sqlDB.getDatabaseFile().toString().endsWith(".gpkg"))
			throw new IllegalArgumentException("Invalid file extension for database - Must be .gpkg");

		
		this.sqlDB = sqlDB;
		this.dbFile = sqlDB.getDatabaseFile();

		if (overwrite) {
			if (dbFile.exists() && !dbFile.delete()) 
				throw new IllegalArgumentException("Unable to overwrite GeoPackage file");
		}

		// Load table definitions
		sysTables.put(GpkgSpatialRefSys.TABLE_NAME, new GpkgSpatialRefSys());
		sysTables.put(GpkgContents.TABLE_NAME, new GpkgContents() );
		sysTables.put(GpkgDataColumnConstraint.TABLE_NAME, new GpkgDataColumnConstraint());
		sysTables.put(GpkgDataColumns.TABLE_NAME, new GpkgDataColumns());
		sysTables.put(GpkgExtensions.TABLE_NAME, new GpkgExtensions());
		sysTables.put(GpkgGeometryColumns.TABLE_NAME, new GpkgGeometryColumns());
		sysTables.put(GpkgMetaData.TABLE_NAME, new GpkgMetaData());
		sysTables.put(GpkgMetaDataReference.TABLE_NAME, new GpkgMetaDataReference());
		sysTables.put(GpkgTileMatrix.TABLE_NAME, new GpkgTileMatrix());
		sysTables.put(GpkgTileMatrixSet.TABLE_NAME, new GpkgTileMatrixSet());
		
		sysViews.put(SpatialRefSys.VIEW_NAME, new SpatialRefSys());
		sysViews.put(STGeometryColumns.VIEW_NAME, new STGeometryColumns());
		sysViews.put(STSpatialRefSys.VIEW_NAME, new STSpatialRefSys());
		//sysViews.put(GeometryColumns.VIEW_NAME, new GeometryColumns()); // Requires function definition

		// Look-ups for sql to Java
		sqlTypeMap.put("integer", JavaType.INTEGER);
		sqlTypeMap.put("tinyint", JavaType.INTEGER);
		sqlTypeMap.put("text", JavaType.STRING);
		sqlTypeMap.put("date", JavaType.STRING);
		sqlTypeMap.put("datetime", JavaType.STRING);
		sqlTypeMap.put("string", JavaType.STRING);
		sqlTypeMap.put("boolean", JavaType.BOOLEAN);
		sqlTypeMap.put("float", JavaType.FLOAT);
		sqlTypeMap.put("double", JavaType.DOUBLE);
		sqlTypeMap.put("real", JavaType.DOUBLE);
		sqlTypeMap.put("long", JavaType.DOUBLE);
		sqlTypeMap.put("geometry", JavaType.BYTE_ARR);
		sqlTypeMap.put("blob", JavaType.BYTE_ARR);
		sqlTypeMap.put("none", JavaType.BYTE_ARR);
		
		/* If the file alread exists, check it is a valid geopackage */
		if (dbFile.exists()) {

			if (!isGPKGValid()) 
				throw new IllegalArgumentException("GeoPackage "+dbFile.getName()+" failed integrity checks - Check the source.");
			
		} else {
			
			log.log(Level.INFO, "Database file does not exist. Creating new GeoPackage "+dbFile.getName());
			
			// Create the DB file
			this.sqlDB.createDatabase();

			for (GpkgTable tab : sysTables.values()) 
				tab.create(this);
			for (GpkgView view : sysViews.values()) 
				view.create(this);

			// Our standard triggers
			for (String stmt : GpkgTriggers.ALL_STANDARD_TRIGGERS) sqlDB.execSQL( stmt );
			
			for (String stmt : GpkgSpatialRefSys.INSERT_DEFAULT_SPATIAL_REF_SYS) 
				sqlDB.execSQL( stmt );
			
			// Try setting the application_id pragma through Sqlite implementation
			if ( !setGpkgAppPragma() ) setGpkgAppHeader();
			
		}
		
		log.log(Level.INFO, "Connected to GeoPackage "+dbFile.getName());
	}
	/** Get the name of the database file associated with this GeoPackage
	 * 
	 * @return
	 */
	public String getDatabaseFileName() {
		return this.dbFile.toString();
	}
	/** Close the underlying SQLite DB instance associated with this GeoPackge
	 * 
	 */
	public void close() {
		this.sqlDB.close();
	}
	/** Check for the {@link #GPKG_APPLICATION_ID} in the database Pragma application_id
	 * field.
	 * 
	 * @return True if its set
	 */
	private boolean isGpkgAppPragmaSet() {
		boolean isGPKG = false;

		ICursor c = sqlDB.doRawQuery("pragma application_id");
		if (c.moveToFirst()) {
			isGPKG = c.getInt(0)==GPKG_APPLICATION_ID;
		}
		c.close();
		
		return isGPKG;
	}
	/** Set the GeoPackage application ID pragma.
	 * 
	 * @return True if set successfully.
	 */
	private boolean setGpkgAppPragma() {
		
		if (!sqlDB.isOpen()) sqlDB.getDatabase(true);
		sqlDB.doRawQuery("pragma application_id="+GPKG_APPLICATION_ID);
		
		return isGpkgAppPragmaSet();
	}
	/** Manually test whether the SQLite header contains the {@link #GPKG_APPLICATION_ID}
	 * This is used as no current version of Android supports a version of Sqlite that supports the
	 * pragma 'application_id', therefore we write to the header manually.
	 * 
	 * @return True if its set.
	 */
	private boolean isGpkgAppHeaderSet() {
		if (sqlDB.isOpen()) sqlDB.close();
		
		boolean isSet = false;
		try {
			RandomAccessFile raf = new RandomAccessFile(dbFile, "r");
			raf.seek(68);
			int n68 = raf.readInt();
			isSet = n68==GPKG_APPLICATION_ID;
			raf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return isSet;
	}
	/** Manually set the SQLite file header to include the {@link #GPKG_APPLICATION_ID}.
	 * This is used as no current version of Android supports a version of Sqlite that supports the
	 * pragma 'application_id', therefore we write to the header manually.
	 * 
	 * @return True if set, false if there was an error.
	 */
	private boolean setGpkgAppHeader() {
		if (sqlDB.isOpen()) sqlDB.close();
		
		/*  */
		try {
			RandomAccessFile raf = new RandomAccessFile(dbFile, "rw");
			raf.seek(68);
			raf.writeInt( GPKG_APPLICATION_ID );
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	/** Check that the GeoPackage is valid according to tests outlined in the specification,
	 * namely that the application_id is correct, a database integrity returns 'ok' and there
	 * are no foreign key issues.<p>
	 * This check is performed automatically when connecting to a GeoPackage, but should 
	 * be performed before passing a GeoPackage to another client application or service. 
	 * 
	 * @return True if the checks pass.
	 */
	public boolean isGPKGValid() {
		boolean isGPKG = false;
		boolean integrity = false;
		boolean foreignKey = false;
		
		isGPKG = isGpkgAppPragmaSet();
		if ( !isGPKG && MODE_STRICT ) isGPKG = isGpkgAppHeaderSet();
		
		sqlDB.getDatabase(false);
		
		ICursor c = sqlDB.doRawQuery("PRAGMA integrity_check");
		if (c.moveToFirst()) {
			integrity = c.getString(0).equals("ok");
		}
		c.close();
		c = sqlDB.doRawQuery("PRAGMA foreign_key_check");
		foreignKey = c.moveToFirst();
		c.close();
		
		// Check all system tables are in the database
		boolean tabsExist = true;
		for (GpkgTable gt : sysTables.values()) {
			if (!gt.isTableInDB(this)) {
				tabsExist = false;
				continue;
			}
		}
		
		return (isGPKG || MODE_STRICT==false) && integrity && !foreignKey && tabsExist;
		
	}
	/** Get the database associated with this GeoPackage
	 * 
	 * @return
	 */
	public ISQLDatabase getDatabase() {
		return this.sqlDB;
	}

	/** Get all tiles in the table, at the specified zoom, in order to cover the supplied
	 * bounding box.
	 * 
	 * @param tableName The table to query
	 * @param bbox The extents of the area to cover.
	 * @param zoomLevel What tile level, or zoom, should the query get
	 * @return
	 * @throws Exception
	 */
	public List<SimpleFeature> getTiles(String tableName, BoundingBox bbox, int zoomLevel) throws Exception {
		log.log(Level.INFO, "BBOX query for images in "+tableName);
		
		List<SimpleFeature> allFeats = new ArrayList<SimpleFeature>();
		
		GpkgTable tilesTable = getUserTable( tableName, GpkgTable.TABLE_TYPE_TILES );
		
		// Is BBOX valid against the table or tile_matrix_set?
		if ( !checkBBOXAgainstLast(tilesTable, bbox, false, false)) return allFeats;
		
		// Tile matrix data for this table
		GpkgRecords tmRecs = getSystemTable(GpkgTileMatrix.TABLE_NAME).query(
				this, "table_name='"+tableName+"' AND zoom_level="+zoomLevel);
		if (tmRecs.getFieldInt(0, "zoom_level")!=zoomLevel)
			throw new Exception("Zoom level "+zoomLevel+" is not defined for this tile pyramid");
		
		int tmWidth = tmRecs.getFieldInt(0, "tile_width");
		int tmHeight = tmRecs.getFieldInt(0, "tile_height");
		double pixX = tmRecs.getFieldDouble(0, "pixel_x_size");
		double pixY = tmRecs.getFieldDouble(0, "pixel_y_size");
		
		// Construct a temporary matrix_set bbox (for convenience)
		GpkgRecords tms = getSystemTable(GpkgTileMatrixSet.TABLE_NAME).query(this, "table_name='"+tilesTable.tableName+"'");
		BoundingBox tmsBox = new BoundingBoxImpl(
				tms.getFieldDouble(0, "min_x"), 
				tms.getFieldDouble(0, "max_x"), 
				tms.getFieldDouble(0, "min_y"), 
				tms.getFieldDouble(0, "max_y"));
		
		/* TODO Get all tiles in the table at the specified zoom and check the bounds?,
		 * or something else...
		 */
		
		
		/* Calculate the min and max rows and columns.
		 * This mechanism works for 3857 (slippy tiles) but serious doubt it does for 
		 * anything else, therefore have to test with other projections and create a generic
		 * mechanism for creating a where clause from a bounding box */
		int minX =  (int) Math.round( (bbox.getMinX() - tmsBox.getMinX() ) / (tmWidth * pixX) );
		int maxX =  (int) Math.round( (bbox.getMaxX() - tmsBox.getMinX() ) / (tmWidth * pixX) );
		int minY =  (int) Math.round( (tmsBox.getMaxY() - bbox.getMaxY() ) / (tmHeight * pixY) );
		int maxY =  (int) Math.round( (tmsBox.getMaxY() - bbox.getMinY() ) / (tmHeight * pixY) );
		
		String strWhere = String.format(
				"zoom_level=%s AND tile_column >= %s AND tile_column <= %s AND tile_row >=%s AND tile_row <=%s", 
				zoomLevel, minX, maxX, minY, maxY);

		return getTiles(tableName, strWhere);
		
	}
	/** Query the GeoPackage for one or more tiles based on a where clause.
	 * The SimpleFeature's that are returned have a {@linkplain FeatureType} name
	 * matching the tableName and a {@link GeometryDescriptor} mathing that defined
	 * in gpkg_contents for the table.<p>
	 * The feature id (accessible via {@link SimpleFeature#getID()}) is the of the form 
	 * <code>TableName-RecordID-zoom-row_ref-col_ref (or tableName-id-zoom-x-y) </code><p>
	 * The image data is stored as a byte[] on an attribute named 'the_image' and the bounds
	 * of the tile are stored as a {@link BoundingBox} on an attribute named 'the_geom'.
	 * 
	 * @param tableName The {@linkplain TilesTable#getTableName()} to query
	 * @param whereClause The SQL where clause, excluding the word 'where'
	 * @return A List of {@linkplain SimpleFeature}'s 
	 * @throws Exception
	 */
	public List<SimpleFeature> getTiles(String tableName, String whereClause) throws Exception {
		log.log(Level.INFO, "WHERE query for images in "+tableName);
		
		List<SimpleFeature> allFeats = new ArrayList<SimpleFeature>();
		
		GpkgTable tilesTable = getUserTable( tableName, GpkgTable.TABLE_TYPE_TILES );
		
		// Get the records matching our query
		GpkgRecords featRecords = tilesTable.query(this, whereClause);
		if (featRecords.size()==0) return allFeats;
		
		
		// Build the geometry descriptor for this 'image' SimpleFeatureType.
		CoordinateReferenceSystem thisCRS = tilesTable.getBounds().getCoordinateReferenceSystem();
		GeometryType gType = new GeometryTypeImpl(
				new NameImpl("Envelope"),
				Geometry.class,
				new CoordinateReferenceSystemImpl( thisCRS.getName().getCode() ) );

		// We only have two attributes - The raster data and a bounding box for the tile
		ArrayList<AttributeType> attrs = new ArrayList<AttributeType>();
		attrs.add(new AttributeTypeImpl(new NameImpl("the_image"), Byte[].class ) );
		attrs.add(new AttributeTypeImpl(new NameImpl("the_geom"), Geometry.class) );
		attrs.trimToSize();
		
		// Construct the feature type
		SimpleFeatureType featureType = new SimpleFeatureTypeImpl(
				new NameImpl( tableName ),
				attrs,
				new GeometryDescriptorImpl(gType, new NameImpl("the_geom"))
				);
		
		List<Object> attrValues = null;
		TileMatrixInfo tmi = ((TilesTable)tilesTable).getTileMatrixInfo();
		
		// Now go through each record building the feature with it's attribute values
		for (int rIdx=0; rIdx < featRecords.size(); rIdx++) {
			
			// Create new list so previous values are not over-written 
			attrValues = new ArrayList<Object>();
			attrValues.add( featRecords.getFieldBlob(rIdx, "tile_data") );
			
			// Construct bounding box for tile
			BoundingBox bbox = tmi.getTileBounds(
					featRecords.getFieldInt(rIdx, "tile_column"),
					featRecords.getFieldInt(rIdx, "tile_row"),
					featRecords.getFieldInt(rIdx, "zoom_level")
					);
			attrValues.add( bbox );
			
			// The ID for this tile
			String fid = String.format("%s-%s-%s-%s-%s",
					tableName,
					featRecords.getFieldInt(rIdx, "id"),
					featRecords.getFieldInt(rIdx, "tile_column"),
					featRecords.getFieldInt(rIdx, "tile_row"),
					featRecords.getFieldInt(rIdx, "zoom_level")
					);
			
			// Create the feature and add to list of all features
			allFeats.add( new SimpleFeatureImpl(fid, attrValues, featureType ) );
		}
		
		return allFeats;
	}
	/** Check if this feature is in the GeoPackage.<p>
	 * The query is based on {@link SimpleFeatureType#getTypeName()} = tableName and 
	 * {@link SimpleFeature#getID()} = Table.featureFieldName
	 * 
	 * @param simpleFeature The feature to test.
	 * @return True if found
	 */
	public boolean isFeatureInGeoPackage(SimpleFeature simpleFeature) {
		String tableName = simpleFeature.getType().getTypeName();
		FeaturesTable featTable = (FeaturesTable)getUserTable( tableName, GpkgTable.TABLE_TYPE_FEATURES );
		
		return featTable.isFeatureInTable(simpleFeature);

	}
	/** Get a list of all SimpleFeature's within, or intersecting with, the supplied BoundingBox.<p>
	 * This version always performs an intersection test and does not check the bbox is within or 
	 * intersecting with the table extents. A StandardGeometryDecoder is used for reading feature
	 * data.
	 * 
	 * @param tableName The table name in this GeoPackage to query.
	 * @param bbox The {@link BoundingBox} to find features in, or intersecting with.
	 * @return A list of {@linkplain SimpleFeature}'s
	 * @throws Exception If the SRS of the supplied {@link BoundingBox} does not match the SRS of
	 * the table being queried.
	 */
	public List<SimpleFeature> getFeatures(String tableName, BoundingBox bbox) throws Exception {
		return getFeatures(tableName, bbox, true, true, new StandardGeometryDecoder() );
	}
	/** Get a list of {@link SimpleFeature} from the GeoPackage by specifying a where clause
	 * (for example {@code featureId='pipe.1234'} or {@code id=1234} )
	 * 
	 * @param tableName The table that holds the feature (probably the localName of<br>
	 * {@link SimpleFeatureType#getName()}
	 * @param whereClause The 'Where' clause, less the where. Passing Null will return 
	 * all records from the table, which is discouraged.
	 * @param geomDecoder The type of {@linkplain GeometryDecoder} to use.
	 * @return A list of SimpleFeature's or an empty list if none were found in the specified table
	 * matching the the filter
	 * 
	 * @throws Exception
	 */
	public List<SimpleFeature> getFeatures(String tableName, String whereClause, GeometryDecoder geomDecoder) 
			throws Exception {
		
		FeaturesTable featTable = (FeaturesTable)getUserTable( tableName, GpkgTable.TABLE_TYPE_FEATURES );
		
		String stmt = "SELECT * FROM ["+tableName+"]";
		if (whereClause!=null && !whereClause.equals("")) stmt+=" WHERE "+whereClause;
		
		return getFeatures(stmt, featTable, geomDecoder);
		
	}
	/** Get a list of all SimpleFeature's within, or intersecting with, the supplied BoundingBox.
	 * 
	 * @param tableName The table name in this GeoPackage to query.
	 * @param bbox The {@link BoundingBox} to find features in, or intersecting with.
	 * @param includeIntersect Should feature's intersecting with the supplied box be returned?
	 * @param testExtents Should the bbox be tested against the data extents in gpkg_contents before
	 * issuing the query? If <code>False</code> a short test on the extents is performed. (In case table
	 * extents are null) 
	 * @param geomDecoder The {@link GeometryDecoder} to use for reading feature geometries.
	 * @return A list of {@linkplain SimpleFeature}'s
	 * @throws Exception If the SRS of the supplied {@link BoundingBox} does not match the SRS of
	 * the table being queried.
	 */
	public List<SimpleFeature> getFeatures(String tableName, BoundingBox bbox, boolean includeIntersect, 
			boolean testExtents, GeometryDecoder geomDecoder) throws Exception {
		log.log(Level.INFO, "BBOX query for features in "+tableName);
		
		List<SimpleFeature> allFeats = new ArrayList<SimpleFeature>();

		FeaturesTable featTable = (FeaturesTable)getUserTable( tableName, GpkgTable.TABLE_TYPE_FEATURES );
		
		// Is BBOX valid against the table?
		if ( !checkBBOXAgainstLast(featTable, bbox, includeIntersect, testExtents)) return allFeats;
		
		GeometryInfo gi = featTable.getGeometryInfo();
		
		StringBuffer sqlStmt = new StringBuffer();
		String pk = featTable.getPrimaryKey(this);
		
		// If this GeoPackage is RTREE enabled, use the spatial index
		if (sqlDB.hasRTreeEnabled() && gi.hasSpatialIndex()) {

			String idxTable = "[rtree_"+tableName+"_"+gi.getColumnName()+"]";

			sqlStmt.append("SELECT [").append(tableName).append("].* FROM [").append(tableName).append("], ");
			sqlStmt.append(idxTable).append(" WHERE [");
			sqlStmt.append(tableName).append("].").append(pk).append("=");
			sqlStmt.append(idxTable).append(".id");
			sqlStmt.append(" AND MinX>=").append( bbox.getMinX() );
			sqlStmt.append(" AND MaxX<=").append( bbox.getMaxX() );
			sqlStmt.append(" AND MinY>=").append( bbox.getMinY() );
			sqlStmt.append(" AND MaxY<=").append( bbox.getMaxY() );
			
			return getFeatures(sqlStmt.toString(), featTable, geomDecoder);
			
		}

		/* Query all records in the feature table and check the header envelope
		 * for matchin/ intersecting bounds. If the envelope is null, then the full
		 * geometry is read and checked */
		
		sqlStmt.append("SELECT * FROM [").append(tableName).append("] WHERE id IN(");
		
		// Query only for feature geometry and test that before getting all attributes
		int totalRecs = featTable.getCount(this);
		long startTime = System.currentTimeMillis();
		
		int lastPK = 0, recCount = 0, hitCount = 0;
		boolean hit = false;
		Envelope headerEnv = null;
		Envelope query = new Envelope(bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox.getMaxY());
		
		
		/* Deprecated getCount() on Cursor to save the cursor iterating
		 * whole ResultSet on underlying Cursor implementation */
		
		// While we have less records than total for table..
		while (recCount < totalRecs) {
			
			String sql = String.format("SELECT %s,%s FROM [%s] WHERE %s > %s ORDER BY %s LIMIT %s",
					pk, gi.getColumnName(), tableName, pk, lastPK, pk, MAX_RECORDS_PER_CURSOR);
			ICursor cPage = getDatabase().doRawQuery( sql );

			// Go through these x number of records
			boolean hasRecords = false;
			while (cPage.moveToNext()) {
				
				hasRecords = true;
				// Decode the geometry and test
				headerEnv = geomDecoder.setGeometryData( cPage.getBlob(1) ).getEnvelope();
				
				// No bbox from header, so decode the whole geometry (a lot slower)
				if (headerEnv.isNull() && !geomDecoder.isEmptyGeom()) {
					headerEnv = geomDecoder.getGeometry().getEnvelopeInternal();
				}
	
				// Test bounds
				hit = (includeIntersect ? query.intersects( headerEnv ) : false) ||  query.contains( headerEnv ) || headerEnv.contains( query );
				if (hit) {
					sqlStmt.append(cPage.getInt(0)).append(",");
					hitCount++;
				}
				
				// Store the last key we saw for the next page query
				lastPK = cPage.getInt(0);
				recCount++;
			}
		
			cPage.close();
			if (hasRecords==false) break;
		}

		log.log(Level.INFO, recCount+" geometries checked in "+(System.currentTimeMillis()-startTime)/1000+" secs");
		
		
		// Didn't find anything
		if (hitCount==0) return allFeats;
		
		sqlStmt.setLength(sqlStmt.length()-1);// How many id's can the DB handle??
		sqlStmt.append(");");

		return getFeatures(sqlStmt.toString(), featTable, geomDecoder );
		
	}
	
	/** Get a list of {@link SimpleFeature} from the GeoPackage by specifying a full SQL statement.
	 * 
	 * @param sqlStatement
	 * @param featTable
	 * @param geomDecoder The type of {@linkplain GeometryDecoder} to use.
	 * @return A list of SimpleFeature's or an empty list if none were found in the specified table
	 * matching the the filter
	 * @throws Exception
	 */
	protected List<SimpleFeature> getFeatures(String sqlStatement, FeaturesTable featTable, GeometryDecoder geomDecoder)
			throws Exception {
		
		List<SimpleFeature> allFeats = new ArrayList<SimpleFeature>();

		// Get the record count by extracting any 'where' clause
		String partSql = "SELECT COUNT(*) FROM ["+featTable.tableName+"] ";
		int whereIdx = sqlStatement.toLowerCase().indexOf("where");
		if ( whereIdx > 0) {
			partSql += sqlStatement.substring( whereIdx );
		}
		ICursor rc = getDatabase().doRawQuery(partSql);
		rc.moveToFirst();
		int totalRecs = rc.getInt(0);
		rc.close();
		
		if (totalRecs==0) return allFeats;
		
		GeometryInfo geomInfo = featTable.getGeometryInfo();
		
		// Currently we only support EPSG definitions, but may have to change...
		if (!geomInfo.getOrganization().toLowerCase().equals("epsg"))
			throw new Exception("Only EPSG SRID's are currently supported.");

		/* Build the geometry descriptor for this SimpleFeatureType. Internally we're going to use
		 * Proj.4 for any projection/ transformation, therefore not (currently) concerned with the
		 * definition supplied in the GeoPackage */
		GeometryType gType = new GeometryTypeImpl(
				new NameImpl(geomInfo.getGeometryTypeName()),
				Geometry.class,
				new CoordinateReferenceSystemImpl(""+geomInfo.getSrsID()) );
		GeometryDescriptor gDescr = new GeometryDescriptorImpl( gType, new NameImpl( geomInfo.getColumnName() ) );
		
		// Get the table field information to build the Attribute Types with
		ArrayList<AttributeType> attrTypes = new ArrayList<AttributeType>();
		FeatureField ff = null;
		Class<?> binding = null;
		AttributeTypeImpl attType = null;
		String desc = "";
		String featureFieldName = null;
		
		for (GpkgField gf : featTable.getFields() ) {
			
			ff = (FeatureField)gf;

			/* If this is the feature id (and text) we'll add as a FeatureID, 
			 * not as an attribute later */
			if (ff.isFeatureID()) {
				
				featureFieldName = ff.getFieldName();
				continue;
				
			} else {
				
				binding = ff.getFieldName().equals(geomInfo.getColumnName()) ? Geometry.class : decodeType( ff.getFieldType() );
				desc = ff.getTitle()==null || ff.getTitle().equals("") ? ff.getDescription() : ff.getTitle();
				
				attType = new AttributeTypeImpl( new NameImpl(ff.getFieldName()), binding );
				attType.setDescription(	desc );
				attrTypes.add(attType);
				
			}
			
		}
		attrTypes.trimToSize();
		
		/* Theoretically a features table can have multiple FeatureTypes 
		 * within it, although all the geometry types (and SRID) as well as the attribute
		 * set must be the same - we will therefore assume this can be described as a single
		 * FeatureType. */
		/* TODO: This is a bad assumption as all features can be 'GeometryCollection', which
		 * could be any base type */
		SimpleFeatureTypeImpl featureType = new SimpleFeatureTypeImpl( 
				new NameImpl( featTable.getTableName() ), 
				attrTypes,
				gDescr);
		featureType.setDescription(featTable.getDescription());

		/* Query the table in 'pages' of LIMIT number */

		long startTime = System.currentTimeMillis();
		String pk = featTable.getPrimaryKey(this);
		
		int lastPK = 0, recCount = 0;
		sqlStatement = sqlStatement.endsWith(";") ? sqlStatement.substring(0, sqlStatement.length()-1) : sqlStatement;
		sqlStatement = whereIdx>0 ? sqlStatement+" AND " : sqlStatement+" WHERE ";
		ArrayList<Object> attrValues = new ArrayList<Object>();
		Object value = null;
		String fid;
		GpkgRecords featRecords = null;
		String sql = ""; 
		String fieldName = null;
		
		// While we have less records than total for table..
		while (recCount < totalRecs) {

			sql = String.format(sqlStatement+"%s > %s ORDER BY %s LIMIT %s",
					pk, lastPK, pk, MAX_RECORDS_PER_CURSOR);
			featRecords = featTable.rawQuery(this, sql );

			if (featRecords.size()==0) break;

			// Now go through each record building the feature with it's attribute values
			for (int rIdx=0; rIdx < featRecords.size(); rIdx++) {

				// Create new list so previous values are not overridden 
				attrValues = new ArrayList<Object>();
				fid = null;
				
				/* For each type definition, get the value, ensuring the 
				 * correct order is maintained on the value list*/
				for (int typeIdx=0; typeIdx < attrTypes.size(); typeIdx++) {
					
					fieldName = attrTypes.get( typeIdx ).getName().getLocalPart();
					value = featRecords.get(rIdx).get( featRecords.getFieldIdx(fieldName) );
					
					// If defined as the feature's ID, store for feature creation
					if ( fieldName.equals(featureFieldName) ) {
						
						fid = String.valueOf( value );
						continue; // Add as ID, not an attribute
						
					} else if (fieldName.equals(geomInfo.getColumnName())) {
						
						// If geometry column, decode to actual Geometry
						value = geomDecoder.setGeometryData( (byte[])value ).getGeometry();
						
					}
					
					attrValues.add(value);
					
				}
				attrValues.trimToSize();
				
				// Get or create a feature id?
				if (fid==null || fid.equals("null")) fid = featTable.getTableName()+"."+recCount;

				// Create the feature and add to list of all features
				allFeats.add( new SimpleFeatureImpl(fid, attrValues, featureType ) );

				// Store the last key we saw for the next page query
				lastPK = featRecords.getFieldInt(rIdx, pk );
				recCount++;
			}
		}

		featRecords = null;
		geomDecoder.clear();
		
		log.log(Level.INFO, recCount+" features built in "+(System.currentTimeMillis()-startTime)/1000+" secs");
		
		return allFeats;
		
	}

	/** Convenience method to check the passed bounding box (for a query) CRS matches
	 * that on the {@link #lastFeatTable} and the bbox is within/ intersects with the 
	 * table boundingbox
	 * 
	 * @param checkTable The table to check the query box against
	 * @param queryBBox The query Bounding box
	 * @param includeIntersect If vector/ feature data, should we test for intersection as
	 * well as contains?
	 * @param shortTest If True only the CRS's are tested to make sure they match. If False, the 
	 * table and/ or tile matrix set extents are tested as well.
	 * @return True if checks pass
	 */
	private boolean checkBBOXAgainstLast(GpkgTable checkTable, BoundingBox queryBBox, boolean includeIntersect, boolean shortTest) {
		
		// Check the SRS's are the same (Projection beyond scope of implementation)
		BoundingBox tableBbox = checkTable.getBounds();
		String qCode = queryBBox.getCoordinateReferenceSystem().getName().getCode();
		String qCodeS = queryBBox.getCoordinateReferenceSystem().getName().getCodeSpace();
		
		String tCode = tableBbox.getCoordinateReferenceSystem().getName().getCode();
		String tCodeS = tableBbox.getCoordinateReferenceSystem().getName().getCodeSpace();
		
		if (!qCode.equalsIgnoreCase(tCode) || !qCodeS.equalsIgnoreCase(tCodeS)) {
			log.log(Level.WARNING, "Passed bounding box SRS does not match table SRS");
			return false;
		}
		
		if (shortTest) return true;
		
		
		/* If GpkgContents has null bounds for this table do full query,
		 * otherwise test the table bounds */
		boolean queryTable = false;
		
		if (!tableBbox.isEmpty()) {
			
			if (checkTable instanceof TilesTable) {
				// If tiles, bbox must be inside table extents
				queryTable = queryBBox.intersects( tableBbox ) || tableBbox.contains( queryBBox );
			} else {
				// If features, inside or intersects
				queryTable = (includeIntersect ? queryBBox.intersects( tableBbox ) : false) || 
						 queryBBox.contains( tableBbox ) ||
						 tableBbox.contains(queryBBox);
			}
			
		} else {
			
			if (checkTable instanceof TilesTable) {
				// If a tiles table and no bounds in contents, check the tile_matrix_set definitions
					GpkgRecords tms = null;
					try {
						tms = getSystemTable(GpkgTileMatrixSet.TABLE_NAME).query(this, "table_name='"+checkTable.tableName+"'");
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
					// Construct a bbox to test against
					CoordinateReferenceSystem crs = new CoordinateReferenceSystemImpl(""+tms.getFieldInt(0, "srs_id"));
					BoundingBox tmsBox = new BoundingBoxImpl(
							tms.getFieldDouble(0, "min_x"), 
							tms.getFieldDouble(0, "max_x"), 
							tms.getFieldDouble(0, "min_y"), 
							tms.getFieldDouble(0, "max_y"),
							crs);
					queryTable = queryBBox.intersects( tmsBox ) || tmsBox.contains( queryBBox );

			}
		}
		
		return queryTable;
	}
	
	/** Get a specific GeoPackage system table
	 * 
	 * @param tableName
	 * @return
	 */
	public GpkgTable getSystemTable(String tableName) {
		return sysTables.get(tableName);
	}
	/** Get one of the user defined tables by name. If the table has not
	 * been loaded then it is created and cached.
	 * 
	 * @param tableName The name of the table.
	 * @param tableType Either {@link GpkgTable#TABLE_TYPE_FEATURES} || {@link GpkgTable#TABLE_TYPE_TILES}
	 * @return An instance of the table.
	 * @throws IllegalArgumentException if the table type is not one of the above, or the 
	 * table does not exist in the GeoPackage.
	 */
	public GpkgTable getUserTable(String tableName, String tableType) {
		GpkgTable gpkgTable = userTables.get(tableName);
		
		if (gpkgTable==null) {
			if (tableType.equals(GpkgTable.TABLE_TYPE_FEATURES) ) {
				gpkgTable = new FeaturesTable(this, tableName);
			} else if (tableType.equals(GpkgTable.TABLE_TYPE_TILES) ) {
				gpkgTable = new TilesTable(this, tableName);
			} else {
				throw new IllegalArgumentException("Incompatible user table type: "+tableType);
			}
			
			if (!gpkgTable.isTableInGpkg(this))
				throw new IllegalArgumentException("Table "+tableName+" does not exist in the GeoPackage");
			
			userTables.put(tableName, gpkgTable);
		}
		
		return gpkgTable;
	}
	/** Get a list of all user tables within the current GeoPackage.<p>
	 * Note that the results of this query are not cached in the same way that system tables are and
	 * the table data is not populated until a relevant method/ query (on the table) is 
	 * called. This allows for quicker/ lower cost checks on the number and/ or names of tables in 
	 * the GeoPackage.
	 * 
	 * @param tableType Either {@link GpkgTable#TABLE_TYPE_FEATURES} or {@link GpkgTable#TABLE_TYPE_TILES}
	 * @return A new list of tables or an empty list if none were found or the wrong tableType was specified.
	 */
	public List<GpkgTable> getDefinedUserTables(String tableType) {
		ArrayList<GpkgTable> ret = new ArrayList<GpkgTable>();
		
		if (!tableType.equals(GpkgTable.TABLE_TYPE_FEATURES) && !tableType.equals(GpkgTable.TABLE_TYPE_TILES))
			return ret;
		
		ICursor tables = null;
		try {
			tables = sysTables.get(GpkgContents.TABLE_NAME).query(this, new String[]{"table_name"},
					"data_type='"+tableType+"'");
		} catch (Exception e) {
			e.printStackTrace();
			return ret;
		}
		
		GpkgTable tab = null;
		
		while(tables.moveToNext()) {
			
			if (tableType.equals(GpkgTable.TABLE_TYPE_FEATURES)) {
				tab = new FeaturesTable(this, tables.getString(0));
			} else {
				tab = new TilesTable(this, tables.getString(0));
			}
			
			ret.add(tab);
		}
		tables.close();
		
		ret.trimToSize();
		return ret;
	}

	/** Insert a collection of tiles in to the GeoPackage
	 * 
	 * @param features
	 * @return The number of tiles inserted
	 * @throws Exception
	 */
	public int insertTiles(Collection<SimpleFeature> features) throws Exception {
		int numInserted = 0;
		long rec = -1;
		
		for (SimpleFeature sf : features) {
			rec = insertTile(sf);
			if( rec>-1 ) numInserted++;
			
		}
		
		return numInserted;
	}
	/** Insert a tile into the GeoPackage from a SimpleFeature.<p>
	 * The tile reference is taken from the feature ID in the form of zoom/xRef/yRef
	 * with or without leading information. The zoom/x/y should be the last three parts
	 * of the ID, which can include a file extension.<p>
	 * The first instance of a byte[] on the feature's attribute will be taken as the image
	 * data.
	 * 
	 * @param feature The {@link SimpleFeature} with details as above
	 * @return The row id of the newly inserted record if successful
	 * 
	 * @throws Exception If the table does not exist in the GeoPackage or the supplied
	 * tile reference is not valid for the table or the attributes and/ or reference cannot 
	 * be decoded.
	 */
	public long insertTile(SimpleFeature feature) throws Exception {
		
		byte[] tileData = null;
		// Cycle feature attrs to get the image data (assumes first byte[] is image)
		for (int i=0; i<feature.getAttributeCount(); i++) {
			if (feature.getAttribute(i) instanceof byte[]) {
				tileData = (byte[]) feature.getAttribute(i);
				break;
			}
		}
		if (tileData==null) {
			throw new Exception("Could not find image data");
		}
		
		//id=49/1/12/2023/1347.PNG.tile
		String[] idParts = feature.getID().split("/");
		if (idParts.length<3) {
			throw new Exception("Could not decode tile reference from ID");
		}
		int x=0, y=0, z=0;
		try {
			z = Integer.valueOf(idParts[idParts.length-3]);
			x = Integer.valueOf(idParts[idParts.length-2]);
			String sY = idParts[idParts.length-1];
			y = Integer.valueOf(sY.substring(0, sY.indexOf(".")));
		} catch (Exception e) {
			throw new Exception("Could not decode tile reference from ID");
		}
		
		return insertTile(feature.getType().getName().getLocalPart(), tileData, x, y, z);
		
	}
	/** Get a single tile by its zoom level column and row from this GeoPackage
	 * 
	 * @param tableName The name of the table to query
	 * @param x_col X reference (the column)
	 * @param y_row Y reference (the row)
	 * @param zoom The zoom level from the tile_matrix (generally between 0-18)
	 * @return A byte[] or Null if no matching record is found
	 *  
	 * @throws Exception
	 */
	public byte[] getTile(String tableName, int x_col, int y_row, int zoom) throws Exception {
		
		GpkgRecords recs = new TilesTable(this, tableName).query(this, 
				String.format("zoom_level=%s AND tile_column=%s AND tile_row=%s", zoom, x_col, y_row)
				);
		
		return recs.getFieldBlob(0, "tile_data");
	}
	/** Insert a single raster tile into the GeoPackage
	 * 
	 * @param tableName The tile table name
	 * @param tile The tile image data
	 * @param tileColumn The column ID (x)
	 * @param tileRow The row ID (y)
	 * @param zoom The zoom level for the tile
	 * @return The row id of the newly inserted record if successful
	 * 
	 * @throws Exception If the table does not exist in the GeoPackage or the supplied
	 * tile reference is not valid for the table.
	 */
	public long insertTile(String tableName, byte[] tile, int tileColumn, int tileRow, int zoom) throws Exception {
		
		TilesTable tilesTable = (TilesTable)getUserTable( tableName, GpkgTable.TABLE_TYPE_TILES );

		// Is this data jpeg or png (only permissible types)
		String pngHdr = new String( new byte[]{tile[0], tile[1], tile[2], tile[3]} );
		String jpgHdr = Integer.toHexString(tile[0] & 0xFF)+Integer.toHexString(tile[1] & 0xFF);
		if (!pngHdr.toLowerCase().contains("png") && !jpgHdr.equalsIgnoreCase("ffd8")) {
			throw new Exception("Tile image is neither PNG or JPG");
		}

		// Check the tile reference is valid for the tile-matrix
		GpkgRecords matrix = getSystemTable(GpkgTileMatrix.TABLE_NAME).query(this, 
				"table_name='"+tableName+"' AND zoom_level="+zoom);
		int w = matrix.getFieldInt(0, "matrix_width");
		int h = matrix.getFieldInt(0, "matrix_height");
		if (tileColumn > w || tileColumn < 1 || tileRow > h || tileRow < 1 || w==-1 || h==-1) {
			throw new Exception("Supplied tile reference is outside the scope of the tile matrix for "+tableName);
		}

		Map<String, Object> values = new HashMap<String, Object>();
		values.put("zoom_level", zoom);
		values.put("tile_column", tileColumn);
		values.put("tile_row", tileRow);
		values.put("tile_data", tile);
		
		return tilesTable.insert(this, values);
	}
	/** Create a {@linkplain FeaturesTable} from a {@link SimpleFeatureType}
	 * 
	 * @param featureType The SimpleFeatureType to use.
	 * @param tableExtents The extents for this table
	 * @return The new FeaturesTable
	 * @throws Exception If the supplied data is invalid or constraints are not 
	 * met (i.e No matching SRS definition in the gpkg_spatial_ref_sys table)
	 */
	public FeaturesTable createFeaturesTable(SimpleFeatureType featureType, BoundingBox tableExtents) throws Exception {
		FeaturesTable ft = new FeaturesTable( this, featureType.getTypeName());
		
		ft.create( featureType, tableExtents );
		
		return ft;
	}
	/** Add all {@link SimpleFeature}'s on the supplied collection into the GeoPackage as a batch.
	 * If there are multiple feature types within the collection they are
	 * automatically split to their corresponding tables.
	 * The table name to insert into is taken from the local part of
	 * the {@link FeatureType#getName()}.
	 * 
	 * @param features
	 * @return The number of records inserted
	 * @throws Exception
	 */
	public int insertFeatures(Collection<SimpleFeature> features) throws Exception {
		
		/* Features within the collection could be different types, so split
		 * in to seperate lists for batch insertion */
		Map<Name, List<SimpleFeature>> typeList = new HashMap<Name, List<SimpleFeature>>();
		for (SimpleFeature sf : features) {
			Name tName = sf.getType().getName();
			List<SimpleFeature> thisType = typeList.get(tName);
			
			if (thisType==null) {
				thisType = new ArrayList<SimpleFeature>();
				typeList.put(tName, thisType);
			}
			thisType.add(sf);
			
		}
		
		int numInserted = 0;
		FeaturesTable featTable = null;
		
		// For each set of feature's in our individual lists..
		for (Map.Entry<Name, List<SimpleFeature>> e : typeList.entrySet()) {
			
			featTable = (FeaturesTable)getUserTable( 
					e.getKey().getLocalPart(), GpkgTable.TABLE_TYPE_FEATURES );
			List<Map<String, Object>> insertVals = new ArrayList<Map<String, Object>>();
			
			Collection<GpkgField> tabFields = featTable.getFields();
			
			
			// Get and check dimensional output
			int mOpt = featTable.getGeometryInfo().getMOption();
			int zOpt = featTable.getGeometryInfo().getZOption();
			int dimension = 2;
			if (	mOpt==Z_M_VALUES_MANDATORY || zOpt==Z_M_VALUES_MANDATORY 
					|| mOpt==Z_M_VALUES_OPTIONAL || zOpt==Z_M_VALUES_OPTIONAL) {
				dimension = 3;
			}
			if (mOpt==Z_M_VALUES_MANDATORY && zOpt==Z_M_VALUES_MANDATORY)
				throw new IllegalArgumentException("4 dimensional output is not supported");
			
			
			// Build values for each feature of this type..
			for (SimpleFeature sf : e.getValue()) {
				insertVals.add( buildInsertValues(sf, tabFields, dimension) );
			}
			
			// Do the update on the table
			numInserted += featTable.insert(this, insertVals);

		}
		
		return numInserted;
	}
	/** Insert a single {@link SimpleFeature} into the GeoPackage.
	 * The table name to insert into is taken from the local part of
	 * the {@link FeatureType#getName()}.
	 * 
	 * @param feature The SimpleFeature to insert.
	 * @return The RowID of the new record or -1 if not inserted
	 * @throws Exception
	 * @see {@link #insertFeatures(Collection)} for batch processing many features
	 */
	public long insertFeature(SimpleFeature feature) throws Exception {
		SimpleFeatureType type = feature.getType();
		
		FeaturesTable featTable = (FeaturesTable)getUserTable( 
				type.getName().getLocalPart(), GpkgTable.TABLE_TYPE_FEATURES );

		// Get and check dimensional output
		int mOpt = featTable.getGeometryInfo().getMOption();
		int zOpt = featTable.getGeometryInfo().getZOption();
		int dimension = 2;
		if (	mOpt==Z_M_VALUES_MANDATORY || zOpt==Z_M_VALUES_MANDATORY 
				|| mOpt==Z_M_VALUES_OPTIONAL || zOpt==Z_M_VALUES_OPTIONAL) {
			dimension = 3;
		}
		if (mOpt==Z_M_VALUES_MANDATORY && zOpt==Z_M_VALUES_MANDATORY)
			throw new IllegalArgumentException("4 dimensional output is not supported");
		
		Map<String, Object> values = buildInsertValues(feature, featTable.getFields(), dimension);
		
		return featTable.insert(this, values);
	}
	/** Create a Map of field name to field value for inserting into a table.
	 * 
	 * @param feature The {@link SimpleFeature}
	 * @param tabFields The GeoPackage table fields to use for building the map.
	 * @param geomDimension 2 or 3 for the Geomaetry ordinates/
	 * @return A Map 
	 * @throws IOException
	 */
	private Map<String, Object> buildInsertValues(SimpleFeature feature, 
			Collection<GpkgField> tabFields, int geomDimension) throws IOException {
		
		// Construct values
		SimpleFeatureType type = feature.getType();
		Map<String, Object> values = new HashMap<String, Object>();
		Object value = null;
		FeatureField field = null;
		boolean passConstraint = true;
		
		// For each field defined in the table...
		for (GpkgField f : tabFields) {
			
			if (f.isPrimaryKey()) continue; // We can't update the PK!
		
			field = (FeatureField)f;
			
			// If defined as feature id, use getID, else find the attribute
			if ( field.isFeatureID() ) {
				value = feature.getID();
			} else {
				int idx = type.indexOf( field.getFieldName() );
				//This field isn't defined on the feature type, so can't insert value
				if (idx==-1 || idx > type.getAttributeCount()) continue; 
				
				value = feature.getAttribute(idx);
			}

			passConstraint = true;
			
			// Check constraint if not a blob
			if (field.getMimeType()==null && field.getConstraint()!=null) {
				passConstraint = field.getConstraint().isValueValid( value );
			}
			
			if(passConstraint) {
				if (value instanceof Geometry) {
					values.put(field.getFieldName(), encodeGeometry( (Geometry)value, geomDimension ) );
				} else {
					values.put(field.getFieldName(), value);
				}
			} else {
				if (MODE_STRICT) {
					throw new IllegalArgumentException("Field "+field.getFieldName()+" did not pass constraint check");
				}
				log.log(Level.WARNING, "Field "+field.getFieldName()+" did not pass constraint check; Inserting Null");
				values.put(field.getFieldName(), null);
			}
			
		}
		
		return values;
	}
	/** Encode a JTS {@link Geometry} to standard GeoPackage geometry blob
	 * 
	 * @param geom The Geometry to encode
	 * @param outputDimension How many dimensions to write (2 or 3). JTS does not support 4
	 * @return
	 * @throws IOException
	 */
	private byte[] encodeGeometry(Geometry geom, int outputDimension) throws IOException {
		if (geom==null) throw new IOException("Null Geometry passed");
		
		if (outputDimension < 2 || outputDimension > 3)
			throw new IllegalArgumentException("Output dimension must be 2 or 3");
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		// 'Magic' and Version
		output.write( "GP".getBytes() );
		output.write( GPKG_GEOM_HEADER_VERSION );
		
		// Header flags
		int endianOrder = ByteOrderValues.BIG_ENDIAN;
		byte flags = 0;
		if (GPKG_GEOMETRY_LITTLE_ENDIAN) {
			flags = (byte) (flags | (1 << 0));
			endianOrder = ByteOrderValues.LITTLE_ENDIAN;
		}
		if (!geom.getEnvelopeInternal().isNull()) {
			/* JTS Envelope geoms are only ever XY, not XYZ or XYZM
			 * therefore we only ever set the 2nd bit to 1 */
			flags = (byte) (flags | (1 << 1));
		}
		if ( geom.isEmpty() ) {
			// Set envelope bit to 0
			flags = (byte) (flags | (1 << 0));
			// Flag the geometry is empty
			flags = (byte) (flags | (1 << 4));
		}
		if (GPKG_GEOMETRY_STANDARD==false) {
			// ExtendedGeoPackageBinary encoding
			flags = (byte) (flags | (1 << 5));
		}
		// Bits 7 and 8 are currently reserved and un-used
		output.write(flags);
		
		// SRS
		byte[] buffer = new byte[4];
		ByteOrderValues.putInt(geom.getSRID(), buffer, endianOrder);
		output.write(buffer);
		
		Envelope envelope = geom.getEnvelopeInternal();
		/* Geom envelope - JTS only supports 2 dimensional envelopes. If Geom is
		 * empty then we don't encode an envelope */
		if (!envelope.isNull() && !geom.isEmpty()) {
			buffer = new byte[8];
			// Min X
			ByteOrderValues.putDouble(envelope.getMinX(), buffer, endianOrder);
			output.write(buffer);
			// Max X
			ByteOrderValues.putDouble(envelope.getMaxX(), buffer, endianOrder);
			output.write(buffer);
			// Min Y
			ByteOrderValues.putDouble(envelope.getMinY(), buffer, endianOrder);
			output.write(buffer);
			// Max Y
			ByteOrderValues.putDouble(envelope.getMaxY(), buffer, endianOrder);
			output.write(buffer);
		}
		
		// Write the geometry
		output.write( new OGCWKBWriter( outputDimension ).write(geom) );
		
		buffer = output.toByteArray();
		output.close();
		
		return buffer;
	}
	/**
	 * 
	 * @param metaData
	 */
	public void addMetaData(GpkgMetaData metaData) {
		//TODO Implement GpkgMetaData
	}
	/**
	 * 
	 * @param metaDataReference
	 */
	public void addMetaDataReference(GpkgMetaDataReference metaDataReference) {
		//TODO Implement GpkgMetaDataReference
	}
	/** Add a new constraint to the GeoPackage that can be referenced, using the same constraint_name,
	 * from gpkg_data_columns.<p>
	 * 
	 * The constraint must be created before a record that uses it is inserted into gpkg_data_columns, therefore
	 * constraint names specified on {@link AttributeType}'s via the user-data must be added through this 
	 * method prior to passing the attribute definitions to 
	 * {@link #createFeatureTable(String, String, List, List, BoundingBox, String, boolean)} 
	 * with dataColumns set to True.<p>
	 * 
	 * Any existing constraint(s) in the GeoPackage with the same name are updated (delete-insert).<p>
	 * 
	 * @param tableName The name of the table to apply this constraint to.
	 * @param columnNames An array of column names to apply this constrain to, WRT the tableName
	 * @param dcConstraint {@link DataColumnConstraint}
	 * 
	 */
	public long addDataColumnConstraint(String tableName, String[] columnNames, DataColumnConstraint dcConstraint) {
		if (dcConstraint==null || dcConstraint.isValid()==false) return -1L;
		
		GpkgDataColumnConstraint dcc = new GpkgDataColumnConstraint();
		DataColumnConstraint existingDCC = dcc.getConstraint(this, dcConstraint.constraintName);
		
		if (existingDCC!=null) {
			if (existingDCC.constraintType.equals(GpkgDataColumnConstraint.TYPE_ENUM)) {
				/* Do we want to delete everything and re-insert, or check and update?
				 * Currently delete everything and re-insert */
				dcc.delete(this, "constraint_name='"+dcConstraint.constraintName+"'");
			} else {
				dcc.delete(this, "constraint_name='"+dcConstraint.constraintName+"'");
			}
		}

		// Insert the constraint
		long newRec = dcc.insert(this, dcConstraint.toMap());
		
		// Didn't insert/ update so don't update feature table
		if (newRec ==-1) return -1L;
		
		// Update GpkgDataColumns for the specified columns
		Map<String, Object> vals = null;
		GpkgTable sys = getSystemTable(GpkgDataColumns.TABLE_NAME);
		for (String col : columnNames) {
			vals = new HashMap<String, Object>();
			vals.put("constraint_name", dcConstraint.constraintName);
			sys.update(this, vals, "table_name='"+tableName+"' AND column_name='"+col+"';");
		}

		return newRec;
	}


	/** Check that the supplied geometry type name is valid for a GeoPackage
	 * 
	 * @param geomDescriptor The GeometryDescriptor to check from.
	 * @return True if its valid
	 */
	public boolean isGeomTypeValid(GeometryDescriptor geomDescriptor) {
		String geomType = geomDescriptor.getType().getName().getLocalPart().toLowerCase();

		if (geomType.equals("geometry")) {
			return true;
		} else	if (geomType.equals("point")) {
			return true;
		} else if (geomType.equals("linestring")) {
			return true;
		} else if (geomType.equals("polygon")) {
			return true;
		} else if (geomType.equals("multipoint")) {
			return true;
		} else if (geomType.equals("multilinestring")) {
			return true;
		} else if (geomType.equals("multipolygon")) {
			return true;
		} else if (geomType.equals("geomcollection")) {
			return true;
		} else {
			return false;
		}
	}
	/** Encode basic Java types to those permissible in a GeoPackage
	 * 
	 * @param object The object value to decode
	 * @return A String usable for a table definition data type. Defaults to TEXT for
	 * any unknown class or Object
	 */
	public static String encodeType(Class<?> clazz) {
		String name = clazz.getSimpleName().toLowerCase();

		if (name.equals("integer") || name.equals("int")) {
			return "INTEGER";
		} else if (name.equals("string")) {
			return "TEXT";
		} else if (name.equals("boolean") || name.equals("byte")) {
			return "BOOL";
		} else if (name.equals("double") || name.equals("float")) {
			return "REAL";
		} else if (name.equals("long")) {
			return "INTEGER";
		} else if (name.equals("geometry") || name.equals("byte[]")) {
			return "BLOB";
		}
		
		return "TEXT";
	}
	/** Decode SQLite data types to Java classes
	 * 
	 * @param sqlType
	 * @return
	 */
	public Class<?> decodeType(String sqlType) {
		
		JavaType jType = sqlTypeMap.get(sqlType.toLowerCase());
		if (jType==null || jType==JavaType.UNKNOWN) 
			throw new IllegalArgumentException("Unknown SQL data type '"+sqlType+"'");
		
		switch (jType) {
		case INTEGER:
			return Integer.class;
		case STRING:
			return String.class;
		case BOOLEAN:
			return Boolean.class;
		case FLOAT:
			return Float.class;
		case DOUBLE:
			return Double.class;
		case BYTE_ARR:
			return Byte[].class;
		}

		return String.class;
	}



}
