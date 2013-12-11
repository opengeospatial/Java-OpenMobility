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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.augtech.geoapi.feature.NameImpl;
import com.augtech.geoapi.feature.SimpleFeatureImpl;
import com.augtech.geoapi.feature.type.AttributeTypeImpl;
import com.augtech.geoapi.feature.type.GeometryDescriptorImpl;
import com.augtech.geoapi.feature.type.GeometryTypeImpl;
import com.augtech.geoapi.feature.type.SimpleFeatureTypeImpl;
import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.augtech.geoapi.geopackage.table.FeatureField;
import com.augtech.geoapi.geopackage.table.FeaturesTable;
import com.augtech.geoapi.geopackage.table.FeaturesTable.GeometryInfo;
import com.augtech.geoapi.geopackage.table.GpkgContents;
import com.augtech.geoapi.geopackage.table.GpkgDataColumnConstraint;
import com.augtech.geoapi.geopackage.table.GpkgDataColumnConstraint.DataColumnConstraint;
import com.augtech.geoapi.geopackage.table.GpkgDataColumns;
import com.augtech.geoapi.geopackage.table.GpkgExtensions;
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
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ByteOrderValues;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

public class GeoPackage {
	protected ISQLDatabase sqlDB = null;
	protected File dbFile = null;
	private GpkgTable lastFeatTable = null;
	
	public Logger log = Logger.getAnonymousLogger();
	private Map<String, GpkgTable> sysTables = new HashMap<String,  GpkgTable>();
	private Map<String, GpkgView> sysViews = new HashMap<String, GpkgView>();
	//private Map<String, GpkgTable> userTables = new HashMap<String, GpkgTable>();
	
	/** The name to create (if required) and test for use as a FeatureID within the GeoPackage */
	public static String FEATURE_ID_FIELD_NAME = "feature_id";
	
	/** The OGC GeoPackage specification these statements relate to */
	public static final String SPEC_VERSION = "OGC 12-128r9 - 0.9.7 - v8";
	/** The Sqlite registered application_id for a GeoPackage */
	public static final int GPKG_APPLICATION_ID = Integer.decode("0x47503130");
	
	/** If True, reading of GeoPackage headers, pragmas and Geometry encodings will
	 * be validated against the specification and exceptions thrown if not valid.
	 * If False, lazy checks will be performed, but exceptions won't be thrown unless
	 * data cannot be understood. Typical examples are application_id pragma and Geometry header.*/
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
	public GeoPackage(ISQLDatabase sqlDB, boolean overwrite) throws Exception {
		if (!sqlDB.getDatabaseFile().toString().endsWith(".gpkg")) {
			throw new Exception("Invalid file extension for database - Must be .gpkg");
		}
		
		this.sqlDB = sqlDB;
		this.dbFile = sqlDB.getDatabaseFile();

		if (overwrite) dbFile.delete();
		
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
		
		/* If the file alread exists, check it is a valid geopackage */
		if (dbFile.exists()) {

			if (!isGPKGValid()) 
				throw new Exception("GeoPackage "+dbFile.getName()+" failed integrity checks - Check the source.");
		} else {
			// Create the DB file
			this.sqlDB.createDatabase();

			for (GpkgTable tab : sysTables.values()) 
				tab.create(this);
			for (GpkgView view : sysViews.values()) 
				view.create(this);

			// Our standard triggers
			for (String stmt : GpkgTriggers.ALL_TRIGGERS) sqlDB.execSQL( stmt );
			
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
		
		return (isGPKG || MODE_STRICT==false) && integrity && !foreignKey;
		
	}
	/** Get the database associated with this GeoPackage
	 * 
	 * @return
	 */
	public ISQLDatabase getDatabase() {
		return this.sqlDB;
	}

	/**
	 * 
	 * @param tableName
	 * @param bbox
	 * @param zoomLevel What tile level, or zoom, should the query get
	 * @return
	 * @throws Exception
	 */
	public List<SimpleFeature> getTiles(String tableName, BoundingBox bbox, int zoomLevel) throws Exception {
		log.log(Level.INFO, "BBOX query for images in "+tableName);
		
		List<SimpleFeature> allFeats = new ArrayList<SimpleFeature>();
		// Is table cached?
		if (lastFeatTable==null || lastFeatTable.getTableName().equals(tableName)==false) {
			lastFeatTable = new TilesTable(this, tableName);
		}
		
		// Is BBOX valid against the table or tile_matrix_set?
		if ( !checkBBOXAgainstLast(bbox, false, false)) return allFeats;
		
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
		GpkgRecords tms = getSystemTable(GpkgTileMatrixSet.TABLE_NAME).query(this, "table_name='"+lastFeatTable.tableName+"'");
		BoundingBox tmsBox = new BoundingBoxImpl(
				tms.getFieldDouble(0, "min_x"), 
				tms.getFieldDouble(0, "max_x"), 
				tms.getFieldDouble(0, "min_y"), 
				tms.getFieldDouble(0, "max_y"));
		
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
		
		// Is table cached?
		if (lastFeatTable==null || lastFeatTable.getTableName().equals(tableName)==false) {
			lastFeatTable = new TilesTable(this, tableName);
		}
		
		// Get the records matching our query
		GpkgRecords featRecords = lastFeatTable.query(this, whereClause);
		if (featRecords.size()==0) return allFeats;
		
		
		// Build the geometry descriptor for this 'image' SimpleFeatureType.
		CoordinateReferenceSystem thisCRS = lastFeatTable.getBounds().getCoordinateReferenceSystem();
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
		TileMatrixInfo tmi = ((TilesTable)lastFeatTable).getTileMatrixInfo();
		
		// Now go through each record building the feature with it's attribute values
		for (Map<String, GpkgField> record : featRecords) {
			
			// Create new list so previous values are not over-written 
			attrValues = new ArrayList<Object>();
			attrValues.add(record.get("tile_data").value);
			
			// Construct bounding box for tile
			BoundingBox bbox = tmi.getTileBounds(
					(Integer) record.get("tile_column").getValue(), 
					(Integer) record.get("tile_row").getValue(), 
					(Integer) record.get("zoom_level").getValue()
					);
			attrValues.add( bbox );
			
			// The ID for this tile
			String fid = String.format("%s-%s-%s-%s-%s",
					tableName,
					record.get("id").value,
					record.get("zoom_level").value,
					record.get("tile_column").value,
					record.get("tile_row").value );
			
			// Create the feature and add to list of all features
			allFeats.add( new SimpleFeatureImpl(fid, attrValues, featureType ) );
		}
		
		return allFeats;
	}
	/** Convenience method to check the passed bounding box (for a query) CRS matches
	 * that on the {@link #lastFeatTable} and the bbox is within/ intersects with the 
	 * table boundingbox
	 * 
	 * @param bbox The query Bounding box
	 * @param includeIntersect If vector/ feature data, should we test for intersection as
	 * well as contains?
	 * @param shortTest If True only the CRS's are tested to make sure they match. If False, the 
	 * table and/ or tile matrix set extents are tested as well.
	 * @return True if checks pass
	 */
	private boolean checkBBOXAgainstLast(BoundingBox bbox, boolean includeIntersect, boolean shortTest) {
		
		// Check the SRS's are the same (Projection beyond scope of implementation)
		BoundingBox tableBbox = lastFeatTable.getBounds();
		String qCode = bbox.getCoordinateReferenceSystem().getName().getCode();
		String qCodeS = bbox.getCoordinateReferenceSystem().getName().getCodeSpace();
		
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
			
			if (lastFeatTable instanceof TilesTable) {
				// If tiles, bbox must be inside table extents
				queryTable = bbox.intersects( tableBbox ) || tableBbox.contains( bbox );
			} else {
				// If features, inside or intersects
				queryTable = (includeIntersect ? bbox.intersects( tableBbox ) : false) || 
						 bbox.contains( tableBbox ) ||
						 tableBbox.contains(bbox);
			}
			
		} else {
			
			if (lastFeatTable instanceof TilesTable) {
				// If a tiles table and no bounds in contents, check the tile_matrix_set definitions
					GpkgRecords tms = null;
					try {
						tms = getSystemTable(GpkgTileMatrixSet.TABLE_NAME).query(this, "table_name='"+lastFeatTable.tableName+"'");
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
					queryTable = bbox.intersects( tmsBox ) || tmsBox.contains( bbox );

			}
		}
		
		return queryTable;
	}
	/** Get a list of all SimpleFeature's within, or intersecting with, the supplied BoundingBox.<p>
	 * This version always performs an intersection test and does not check the bbox is within or 
	 * intersecting with the table extents
	 * 
	 * @param tableName The table name in this GeoPackage to query.
	 * @param bbox The {@link BoundingBox} to find features in, or intersecting with.
	 * @return A list of {@linkplain SimpleFeature}'s
	 * @throws Exception If the SRS of the supplied {@link BoundingBox} does not match the SRS of
	 * the table being queried.
	 */
	public List<SimpleFeature> getFeatures(String tableName, BoundingBox bbox) throws Exception {
		return getFeatures(tableName, bbox, true, true);
	}
	/** Get a list of all SimpleFeature's within, or intersecting with, the supplied BoundingBox.
	 * 
	 * @param tableName The table name in this GeoPackage to query.
	 * @param bbox The {@link BoundingBox} to find features in, or intersecting with.
	 * @param includeIntersect Should feature's intersecting with the supplied box be returned?
	 * @param testExtents Should the bbox be tested against the data extents in gpkg_contents before
	 * issuing the query? If <code>False</code> a short test on the extents is performed. (In case table
	 * extents are null) 
	 * @return A list of {@linkplain SimpleFeature}'s
	 * @throws Exception If the SRS of the supplied {@link BoundingBox} does not match the SRS of
	 * the table being queried.
	 */
	public List<SimpleFeature> getFeatures(String tableName, BoundingBox bbox, boolean includeIntersect, boolean testExtents) throws Exception {
		log.log(Level.INFO, "BBOX query for features in "+tableName);
		
		List<SimpleFeature> allFeats = new ArrayList<SimpleFeature>();

		// Is table cached?
		if (lastFeatTable==null || lastFeatTable.getTableName().equals(tableName)==false) {
			lastFeatTable = new FeaturesTable(this, tableName);
			if (lastFeatTable.isTableInGpkg(this)==false) return allFeats;
		}
		
		// Is BBOX valid against the table?
		if ( !checkBBOXAgainstLast(bbox, includeIntersect, testExtents)) return allFeats;
		
		// If this GeoPackage is RTREE enabled, use the spatial index
		boolean hasRTree = false;//TODO Not sure how we know yet!
		
		if (hasRTree) {
			// Build the spatial query
			StringBuffer sb = new StringBuffer();
			sb.append("ST_MinX(geom. Geometry)<=").append(bbox.getMaxX());
			sb.append(" AND ST_MaxX(geom. Geometry)>=").append(bbox.getMinX());
			sb.append(" AND ST_MinY(geom. Geometry)<=").append(bbox.getMaxY());
			sb.append(" AND ST_MaYX(geom. Geometry)>=").append(bbox.getMinY());

			return getFeatures(tableName, sb.toString() );
			
		}

		// Query only for feature geometry and test that before getting all attributes
		ICursor allCur = lastFeatTable.query(
				this, 
				new String[]{"id", ((FeaturesTable)lastFeatTable).getGeometryInfo().getColumnName()}, 
				null);
		if (allCur==null) return allFeats;
		
		// Cycle all features and test each geometry against our BoundingBox
		StringBuffer finalIDs = new StringBuffer();
		boolean hit = false;
		Geometry geom = null;
		BoundingBox fEnv = new BoundingBoxImpl("");
		
		while (allCur.moveToNext()) {
			
			// Decode the geometry and test
			geom = decodeGeometry( allCur.getBlob(1) );
			fEnv = new BoundingBoxImpl( geom.getEnvelopeInternal(), ""+geom.getSRID() );

			hit = (includeIntersect ? bbox.intersects( fEnv ) : false) ||  bbox.contains( fEnv ) || fEnv.contains( bbox );
			if (hit) {
				finalIDs.append(allCur.getInt(0)).append(",");
			}

		}
		allCur.close();
		
		// Didn't find anything
		if (finalIDs.length()==0) return allFeats;
		
		finalIDs.setLength(finalIDs.length()-1);
		finalIDs.insert(0, "id IN(").append(");");// How many can this handle??
		
		// Get full feature information for those in the bbox
		allFeats = getFeatures(tableName, finalIDs.toString() );
		
		return allFeats;
	}

	/** Get a list of {@link SimpleFeature} from the GeoPackage by specifying a where clause
	 * (for example {@code featureId='pipe.1234'} or {@code id=1234} )
	 * 
	 * @param tableName The table that holds the feature (probably the localName of<br>
	 * {@link SimpleFeatureType#getName()}
	 * @param whereClause The 'Where' clause, less the where. Passing Null will return 
	 * all records from the table, which is discouraged.
	 * @return A list of SimpleFeature's or an empty list if none were found in the specified table
	 * matching the the filter
	 * 
	 * @throws Exception
	 */
	public List<SimpleFeature> getFeatures(String tableName, String whereClause) throws Exception {
		log.log(Level.INFO, "WHERE query for features in "+tableName);
		
		List<SimpleFeature> allFeats = new ArrayList<SimpleFeature>();
		
		// Is table cached?
		if (lastFeatTable==null || lastFeatTable.getTableName().equals(tableName)==false) {
			lastFeatTable = new FeaturesTable(this, tableName);
			if (lastFeatTable.isTableInGpkg(this)==false) return allFeats;
		}
		
		// Get the records matching our query
		if (whereClause.equals("")) whereClause = null;
		GpkgRecords featRecords = ((FeaturesTable)lastFeatTable).query(whereClause);
		if (featRecords.size()==0) return allFeats;

		GeometryInfo geomInfo = ((FeaturesTable)lastFeatTable).getGeometryInfo();
		
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
		
		for (GpkgField gf : lastFeatTable.getFields() ) {
			
			ff = (FeatureField)gf;

			// If this is the feature id (and text) we'll add as a FeatureID, not an attribute
			if (ff.isFeatureID()) continue;
			
			binding = ff.getFieldName().equals(geomInfo.getColumnName()) ? Geometry.class : decodeType( ff.getFieldType() );
			desc = ff.getTitle()==null || ff.getTitle().equals("") ? ff.getDescription() : ff.getTitle();
			
			attType = new AttributeTypeImpl( new NameImpl(ff.getFieldName()), binding );
			attType.setDescription(	desc );
			attrTypes.add(attType);
			
		}
		attrTypes.trimToSize();
		
		/* Theoretically a features table can have multiple FeatureTypes 
		 * within it, although all the geometry types (and SRID) as well as the attribute
		 * set must be the same - we will therefore assume this can be described as a single
		 * FeatureType. */
		SimpleFeatureTypeImpl featureType = new SimpleFeatureTypeImpl( 
				new NameImpl(tableName), 
				attrTypes,
				gDescr);
		featureType.setDescription(lastFeatTable.getDescription());
		
		ArrayList<Object> attrValues = new ArrayList<Object>();
		int fidCounter = 0;
		FeatureField thisField = null;
		Object value = null;
		String fid;
		
		// Now go through each record building the feature with it's attribute values
		for (Map<String, GpkgField> record : featRecords) {
			
			// Create new list so previous values are not overridden 
			attrValues = new ArrayList<Object>();
			fid = null;
			
			// Build the attributes in the correct order
			for (GpkgField gf : lastFeatTable.getFields() ) {
				thisField = (FeatureField) record.get( gf.getFieldName() );
				
				// If defined as the feature's ID, store for feature creation
				if ( thisField.isFeatureID() ) {
					fid = String.valueOf( thisField.getValue() );
					continue; // Add as ID, not an attribute
				}
				
				value = null;
				
				// If geometry column, decode to actual Geometry
				if (thisField.getFieldName().equals(geomInfo.getColumnName())) {
					value = decodeGeometry( thisField.getValue() );
				} else {
					value = thisField.getValue();
				}
				
				attrValues.add(value);
			}
			attrValues.trimToSize();
			
			// Get or create a feature id?
			if (fid==null || fid.equals("null")) fid = tableName+"."+fidCounter;
			
			// Create the feature and add to list of all features
			allFeats.add( new SimpleFeatureImpl(fid, attrValues, featureType ) );

			fidCounter++;
		}

		return allFeats;
		
	}

	/** Get a specific GeoPackage system table
	 * 
	 * @param tableName
	 * @return
	 */
	public GpkgTable getSystemTable(String tableName) {
		return sysTables.get(tableName);
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
	public List<GpkgTable> getUserTables(String tableType) {
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
		String tableName = feature.getType().getName().getLocalPart();
		
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
		
		return insertTile(tableName, tileData, x, y, z);
		
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
		// Cache table as could be inserting 1000s features
		if (lastFeatTable==null || !lastFeatTable.getTableName().equals(tableName)) {
			lastFeatTable = new TilesTable(this, tableName);
		}
		
		GpkgRecords recs = lastFeatTable.query(this, String.format("zoom_level=%s AND tile_column=%s AND tile_row=%s", zoom, x_col, y_row));
		
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
		
		// Cache table as could be inserting 1000s features
		if (lastFeatTable==null || !lastFeatTable.getTableName().equals(tableName)) {
			lastFeatTable = new TilesTable(this, tableName);
		}

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
		
		return lastFeatTable.insert(this, values);
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
	/**
	 * 
	 * @param features
	 * @return
	 * @throws Exception
	 */
	public int insertFeatures(Collection<SimpleFeature> features) throws Exception {
		
		int numInserted = 0;
		long rec = -1;
		
		for (SimpleFeature sf : features) {
			rec = insertFeature(sf);
			if( rec>-1 ) numInserted++;
			
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
	 */
	public long insertFeature(SimpleFeature feature) throws Exception {
		SimpleFeatureType type = feature.getType();
		String tableName = type.getName().getLocalPart();
		
		// Cache table as could be inserting 1000s features
		if (lastFeatTable==null || !lastFeatTable.getTableName().equals(tableName)) {
			lastFeatTable = new FeaturesTable(this, tableName);
		}

		// Construct values
		Map<String, Object> values = new HashMap<String, Object>();
		Object value = null;
		FeatureField field = null;
		boolean passConstraint = true;
		
		for (GpkgField f : lastFeatTable.getFields()) {
			
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
					values.put(field.getFieldName(), encodeGeometry( (Geometry)value ) );
				} else {
					values.put(field.getFieldName(), value);
				}
			} else {
				if (MODE_STRICT) {
					throw new Exception("Field "+field.getFieldName()+" did not pass constraint check");
				}
				log.log(Level.WARNING, "Field "+field.getFieldName()+" did not pass constraint check; Inserting Null");
				values.put(field.getFieldName(), null);
			}
			
		}
		
		return lastFeatTable.insert(this, values);
	}

	/** Deocde a geometry blob (byte[]) in to a JTS {@link Geometry}
	 * 
	 * @param geomBlob
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private Geometry decodeGeometry(Object inGeom) throws IOException, ParseException {
		if ((inGeom instanceof byte[])==false) throw new IOException("Decode Geometry: Passed object is not byte[]");
		
		ByteArrayInputStream bais = new ByteArrayInputStream( (byte[])inGeom );
		
		// 'Magic' and version
		byte[] buffer = new byte[2];
		bais.read(buffer);
		String magic = new String(buffer);
		if (!magic.equals("GP") && MODE_STRICT) throw new ParseException("Decode Geometry: Geometry header not valid");

		// TODO: Check version number against...?
		int version = bais.read();
		
		// Decode header flags
		int flags = bais.read();
		int byteOrder = (flags & 1)==0 ? ByteOrderValues.BIG_ENDIAN : ByteOrderValues.LITTLE_ENDIAN;
		// Envelope
		int envBytes = 0;
		if ( (flags >> 1 & 1)==1) {
			envBytes = 32; // XY
		} else if ( (flags >> 2 & 1)==1 ) {
			envBytes = 48; // XYZ
		} else if ( (flags >> 3 & 1)==1 ) {
			envBytes = 48; // XYM
		} else if ( (flags >> 4 & 1)==1 ) {
			envBytes = 64; // XYZM
		}
		boolean geomEmpty = (flags & (1 << 4))==1;
		boolean extendedGeom = (flags & (1 << 5))==1;
		// Bits 7 and 8 are reserved and currently 0

		// SRID
		buffer = new byte[4];
		bais.read( buffer );
		int srsID = ByteOrderValues.getInt(buffer, byteOrder);
		
		// Envelope
		int numCoord = envBytes/8;
		buffer = new byte[ envBytes/4 ];
		double[] vals = envBytes > 0 ? new double[numCoord] : null;
		for(int i=0; i < numCoord; i++) {
			bais.read(buffer);
			vals[i] = ByteOrderValues.getDouble(buffer, byteOrder);
		}
		
		/* We construct an Envelope from the header, but are not using as JTS will create an envelope
		 * from the resulting geometry automatically. Note that JTS implementation only supports 2D 
		 * envelopes anyway. */
		Envelope envelope = null;
		if (vals!=null) {
			envelope = new Envelope(vals[0], vals[1], vals[2], vals[3]);
		} else {
			envelope = new Envelope();
		}

		// Create geometry
		Geometry geom = null;
		if (geomEmpty) geom = geomFactory.createPoint(new Coordinate());
		
		// Build the actual geometry
		if (!extendedGeom) {

			geom = wkbReader.read( inputStreamToByte(bais) );

		} else {
			// Extended geometry - Implementers to add own code if required
		}

		geom.setSRID( srsID );
		
		bais.close();

		return geom;
	}
	private GeometryFactory geomFactory = new GeometryFactory();;
	private WKBReader wkbReader = new WKBReader();
	private WKBWriter wkbWriter = new WKBWriter(3);
	/** Encode a JTS {@link Geometry} to standard GeoPackage geometry blob
	 * 
	 * @param geom The Geometry to encode
	 * @return
	 * @throws IOException
	 */
	private byte[] encodeGeometry(Geometry geom) throws IOException {
		if (geom==null) throw new IOException("Null Geometry passed");
		
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
		output.write( wkbWriter.write(geom) );
		
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
	public String encodeType(Class<?> clazz) {
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
		sqlType = sqlType.toLowerCase();
		if (sqlType.equals("integer") ) {
			return Integer.class;
		} else if (sqlType.equals("text")) {
			return String.class;
		} else if (sqlType.equals("string")) {
			return String.class;
		} else if (sqlType.equals("boolean")) {
			return Boolean.class;
		} else if (sqlType.equals("float")) {
			return Float.class;
		} else if (sqlType.equals("double")) {
			return Double.class;
		} else if (sqlType.equals("long")) {
			return Long.class;
		} else if (sqlType.equals("geometry") ) {
			return Byte[].class;
		} else if (sqlType.equals("blob")) {
			return Byte[].class;
		}
		return String.class;
	}
	/** Process an InputStream to a correctly sized byte array 
	 * 
	 * @param is The inputStream
	 * @return a new byte[]
	 * 
	 * @throws IOException
	 */
	public static byte[] inputStreamToByte(InputStream is) throws IOException {
		int length = 0;int initBufferSize = 4096;
		int bufferSize = initBufferSize;
		byte[] buffer = new byte[bufferSize];
		byte[] tmp = new byte[bufferSize];
		int bytesRead;
		while ((bytesRead = is.read(tmp)) != -1) {
			if (length + bytesRead > bufferSize) {
				bufferSize = Math.max(bufferSize << 1, length + bytesRead);
				byte[] newbuffer = new byte[bufferSize];
				System.arraycopy(buffer, 0, newbuffer, 0, length);
				buffer = newbuffer;
			}
			System.arraycopy(tmp, 0, buffer, length, bytesRead);
			length += bytesRead;
		}
		// Trim buffer to correct size
		tmp = new byte[length];
		System.arraycopy(buffer, 0, tmp, 0, length);
		buffer = tmp;

		return buffer;
	}

}
