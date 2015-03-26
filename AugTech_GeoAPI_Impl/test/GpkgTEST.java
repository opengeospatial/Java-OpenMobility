/*
 * Copyright 2014, Augmented Technologies Ltd.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.augtech.geoapi.feature.FeatureCollection;
import com.augtech.geoapi.feature.NameImpl;
import com.augtech.geoapi.feature.loader.GML2_1;
import com.augtech.geoapi.feature.loader.RasterTile;
import com.augtech.geoapi.feature.type.AttributeTypeImpl;
import com.augtech.geoapi.feature.type.GeometryDescriptorImpl;
import com.augtech.geoapi.feature.type.GeometryTypeImpl;
import com.augtech.geoapi.feature.type.SimpleFeatureTypeImpl;
import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.augtech.geoapi.geopackage.geometry.StandardGeometryDecoder;
import com.augtech.geoapi.geopackage.table.FeaturesTable;
import com.augtech.geoapi.geopackage.table.TilesTable;
import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;

import com.vividsolutions.jts.geom.Geometry;

/** A test layer that utilises some of the Aug-Tech GeoAPI facilities
 * for loading data and then testing the Java GeoPackage implementation.
 *
 * @author Augmented Technologies Ltd 2014.
 *
 */
public class GpkgTEST {
	GeoPackage geoPackage = null;
	FeatureCollection featureCollection = null;
	FeatureCollection imageCollection = null;
	ISQLDatabase gpkgDatabase = null;
	Logger log = Logger.getAnonymousLogger();
	static String TEST_GML_FILE = "test_gml";

	public static final int TEST_HAITI = 0;
	public static final int TEST_GML = 1;
	public static final int TEST_NZ = 2;
	public static final int TEST_TILES = 3;
	public static final int TEST_NZ_DEM = 4;
	public static final int TEST_SIGMA = 5;

	/**
	 *
	 * @param appContext
	 * @param gpkgFile
	 * @param overwrite
	 */
	public GpkgTEST(ISQLDatabase database, boolean overwrite) {

		this.gpkgDatabase = database;

		log.log(Level.INFO, "Connecting to GeoPackage...");

		geoPackage = new GeoPackage(gpkgDatabase, overwrite);

		// Quick test to get the current contents
		if (geoPackage!=null) {

			int numExist = geoPackage.getUserTables(GpkgTable.TABLE_TYPE_FEATURES).size();
			log.log(Level.INFO, ""+numExist+" feature tables in the GeoPackage");

			numExist = geoPackage.getUserTables(GpkgTable.TABLE_TYPE_TILES).size();
			log.log(Level.INFO, ""+numExist+" tile tables in the GeoPackage");
		}

	}
	/** Add a new user tiles table suitable for Slippy tiles (OSM tiles)
	 *
	 * @param tableName
	 * @param pxWidthHeight Width and height in pixels
	 * @return
	 */
	public boolean createTilesTable(String tableName, int pxWidthHeight) {

		TilesTable tt = new TilesTable(geoPackage, tableName);
		boolean added = false;
		try {
			added = tt.create( pxWidthHeight );
		} catch (Exception e) {
			e.printStackTrace();
		}

		return added;
	}
	/** Get the GeoPackage currently in use.
	 *
	 * @return
	 */
	public GeoPackage getGeoPackage() {
		return this.geoPackage;
	}
	/**
	 *
	 * @param typeName The name of the feature type on the collection
	 * (should match the tableName)
	 * @param path The directory where the tiles are stored
	 * @return
	 */
	public boolean loadTilesToCollection(String typeName, File path) {
		if (!path.exists()) return false;

		log.log(Level.INFO, "Loading Images...");

		// Build attribute definitions
		ArrayList<AttributeType> attrs = new ArrayList<AttributeType>();
		attrs.add(new AttributeTypeImpl(new NameImpl("the_image"), Byte[].class ) );
		//attrs.add(new AttributeTypeImpl(new NameImpl("the_geom"), Geometry.class) );

		// The geometry
		GeometryType gType = new GeometryTypeImpl(
				new NameImpl("Envelope"),
				Geometry.class,
				new CoordinateReferenceSystemImpl("3857"));

		// Now construct the feature type
		Name fTypeName = new NameImpl( typeName );
		SimpleFeatureType sft = new SimpleFeatureTypeImpl(
				fTypeName,
				attrs,
				new GeometryDescriptorImpl(gType, new NameImpl("the_geom"))
				);


		// Create a loader for the tiles
		RasterTile loader = new RasterTile( null );
		int numLoaded = 0;
		try {
			// Load each file..
			for (File f : path.listFiles()) {
				loader.setInitialID( f.toString() );
				numLoaded = loader.loadFeatures( f, sft );
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (imageCollection==null) imageCollection = new FeatureCollection();

		Map<Name, SimpleFeatureType> fTypes = new HashMap<Name, SimpleFeatureType>();
		fTypes.put(fTypeName, sft);

		List<String> newIDs = imageCollection.mergeAll( fTypes, loader );

		log.log(Level.INFO, "Loaded "+newIDs.size()+" images");

		return newIDs.size() > 0;
	}

	/** Load the test GML File to the internal feature collection.
	 *
	 * @param overwrite True to overwrite the existing FeatureCollection, False to append
	 * @return True if >0 features are loaded to the featureCollection
	 * @throws Exception
	 */
	public boolean loadGMLToCollection(boolean overwrite) throws Exception {

		InputStream gmlRes = this.getClass().getResourceAsStream(TEST_GML_FILE);
		File gmlFile = new File(System.getProperty("java.io.tmpdir"), "tmpGml.gml");
		if (gmlFile.exists()) gmlFile.delete();

		streamCopy(gmlRes, new FileOutputStream(gmlFile));

		if (!gmlFile.exists()) return false;

		log.log(Level.INFO, "Loading GML...");

		GML2_1 gmlLoader = new GML2_1(null);

		int numFeat = gmlLoader.loadFeatures( gmlFile, null );
		gmlFile.delete();

		Map<Name, SimpleFeatureType> fTypes = gmlLoader.getLoadedTypes();
		if (overwrite) {
			featureCollection = new FeatureCollection();
		} else {
			if (featureCollection == null) {
				featureCollection = new FeatureCollection( );
			}
		}
		List<String> newIds = featureCollection.mergeAll(fTypes, gmlLoader);
		int merged = newIds.size();
		// The merged figure can be less as duplicate IDs will not be added.
		if (numFeat!=merged)log.log(Level.INFO, ""+(numFeat-merged)+" features not loaded to FC");

		log.log(Level.INFO, "Loaded "+merged+" features");
		return merged > 0;
	}
	/**
	 *
	 * @param in
	 * @param out
	 * @throws IOException
	 */
    static void streamCopy(InputStream in, OutputStream out) throws IOException{
        byte[] b = new byte[1024];
        int read;
        while ((read = in.read(b)) != -1) {
                out.write(b, 0, read);
        }
        out.close();
        in.close();
    }
	/**
	 *
	 * @param allFeatures
	 * @return
	 * @throws Exception
	 */
	public int insertTilesFromCollection(boolean allTiles) throws Exception {
		if (imageCollection==null) return -1;

		log.log(Level.INFO, "Inserting images to GeoPackage...");
		int numIns = 0;

		if (allTiles) {

			numIns = geoPackage.insertTiles( imageCollection );

		} else {

			long rowID = geoPackage.insertTile( imageCollection.get(0) );
			if (rowID>-1) numIns = 1;

		}

		log.log(Level.INFO, "Inserted "+numIns+" images.");

		return numIns;
	}
	/**
	 *
	 * @param allFeatures
	 * @return
	 * @throws Exception
	 */
	public int insertFeaturesFromCollection(boolean allFeatures) throws Exception {

		if (featureCollection==null) createFeatureTablesFromCollection(true);

		int numIns = 0;
		log.log(Level.INFO, "Inserting features to GeoPackage...");

		if (allFeatures) {

			numIns = geoPackage.insertFeatures( featureCollection );

		} else {

			long rowID = geoPackage.insertFeature( featureCollection.get(0) );
			if (rowID>-1) numIns = 1;

		}

		log.log(Level.INFO, "Inserted "+numIns+" features.");
		return numIns;
	}
	/**
	 *
	 * @param allTypes
	 * @return
	 * @throws Exception
	 */
	public boolean createFeatureTablesFromCollection(boolean allTypes) throws Exception {
		if (featureCollection==null) loadGMLToCollection(true);

		log.log(Level.INFO, "Creating feature tables...");

		int numCreated = 0;

		if (allTypes) {

			for (SimpleFeatureType sft : featureCollection.getCurrentTypes()) {

				FeaturesTable ft = geoPackage.createFeaturesTable(sft, featureCollection.getBounds() );

				if (ft==null) return false;
				numCreated++;
			}

		} else {

			SimpleFeatureType single = null;
			for (SimpleFeatureType sft : featureCollection.getCurrentTypes() ) {
				single = sft;
				break;
			}

			FeaturesTable ft = geoPackage.createFeaturesTable(single, featureCollection.getBounds() );

			if (ft==null) return false;
			numCreated++;
		}

		log.log(Level.INFO, "Created "+numCreated+" feature tables (types)");

		return true;
	}
	/**
	 *
	 * @param testCase
	 * @param fb
	 * @return
	 * @throws Exception
	 */
	public boolean runQeryTestCase(int testCase, ITestFeedback fb) throws Exception {

		if (geoPackage==null) {
			setTestMsg(fb, "No GeoPackage connected");
			return false;
		}

		List<SimpleFeature> feats = null;
		BoundingBox bbox = null;
		CoordinateReferenceSystem crs = null;

		switch (testCase) {

		case TEST_HAITI:// Port-au-Prince, Haiti (from Compusult)

			crs = new CoordinateReferenceSystemImpl("4326");

			feats = geoPackage.getFeatures("linear_features", "id=1", new StandardGeometryDecoder() );
			setTestMsg(fb, feats.size()+" linear_feature read by query");

			bbox = new BoundingBoxImpl(-72.335822, -72.633677, 18.4617532, 18.8551624, crs);
			feats = geoPackage.getFeatures("linear_features", bbox);
			setTestMsg(fb, feats.size()+" linear_features read via bbox query!");

			break;

		case TEST_GML:

			crs = new CoordinateReferenceSystemImpl("4326");
			bbox = new BoundingBoxImpl(-2.14, -2.14, 52.2, 52.3, crs);

			// Get all features from a table
			feats = geoPackage.getFeatures("surface_water_sewer", "", new StandardGeometryDecoder());
			setTestMsg(fb, feats.size()+" surface_water_sewer feature(s) read.");

			// Get all features within a bounding box
			feats = geoPackage.getFeatures("surface_water_sewer", bbox);
			setTestMsg(fb, feats.size()+" surface_water_sewer feature(s) read via bbox query!");

			feats = geoPackage.getFeatures("foul_sewer", bbox);
			setTestMsg(fb, feats.size()+" foul_sewer feature(s) read via bbox query!");

			break;

		case TEST_TILES:

			crs = new CoordinateReferenceSystemImpl("3857");
			bbox = new BoundingBoxImpl(-239247.301401622, -238788.67923184743, 6846158.279178806, 6846616.90134858,crs);

			// Get all tiles from a single table
			feats = geoPackage.getTiles("GoogleMaps", null);
			setTestMsg(fb, feats.size()+" images(s) (all) read from GoogleMaps");

			// Get a single tile we know we have
			feats = geoPackage.getTiles("GoogleMaps", "zoom_level=18 and tile_column=129506 and tile_row=86286");
			setTestMsg(fb, feats.size()+" images(s) read direct by query");

			// Get a set of tiles within/ across a bounding box
			feats = geoPackage.getTiles("GoogleMaps", bbox, 18);
			setTestMsg(fb, feats.size()+" images(s) read by BBOX query");

			break;

		case TEST_NZ:
			crs = new CoordinateReferenceSystemImpl("27200");
			bbox = new BoundingBoxImpl(2560000, 2565000, 6600000, 6605000, crs);

			feats = geoPackage.getFeatures("nzlri-vegetation", bbox);
			setTestMsg(fb, feats.size()+" feature(s) read from [nzlri-vegetation] by BBOX query");

			feats = geoPackage.getFeatures("lcdb-v33-land-cover-datab", bbox);
			setTestMsg(fb, feats.size()+" feature(s) read from [lcdb-v33-land-cover-datab] by BBOX query");

			//bbox = ProjectionUtils.reproject(bbox, "4326");
			crs = new CoordinateReferenceSystemImpl("4326");
			bbox = new BoundingBoxImpl(173.54606, 173.60088, -35.8007, -35.75527, crs);

			feats = geoPackage.getFeatures("nzlri-north-island-edition", bbox);
			setTestMsg(fb, feats.size()+" feature(s) read from [north-island-edition] by BBOX query");

			feats = geoPackage.getFeatures("s-map-a-new-soil-spatial", bbox);
			setTestMsg(fb, feats.size()+" feature(s) read from [new-soil-spatial] by BBOX query");

			break;

		case TEST_NZ_DEM:

			crs = new CoordinateReferenceSystemImpl("27200");
			bbox = new BoundingBoxImpl(2560000, 2565000, 6600000, 6605000, crs);

			feats = geoPackage.getFeatures("TerrainPoint", bbox);
			setTestMsg(fb, feats.size()+" feature(s) read from [terrainpoint] by BBOX query");

			break;

		case TEST_SIGMA:
			crs = new CoordinateReferenceSystemImpl("3857");
			bbox = new BoundingBoxImpl(-20037508.3428, 20037508.3428, -20037471.2051, 20037471.2051, crs);

			String table = "fromosm_tiles";

			// Get all tiles from a single table
			feats = geoPackage.getTiles(table, null);
			setTestMsg(fb, feats.size()+" images(s) (all) read from "+table);

			// Get a single tile we know we have
			feats = geoPackage.getTiles(table, "zoom_level=10 and tile_column=341 and tile_row=479");
			setTestMsg(fb, feats.size()+" images(s) read direct by query from "+table);

			// Get a set of tiles within/ across a bounding box
			int zoom = 10;
			feats = geoPackage.getTiles(table, bbox, zoom);
			setTestMsg(fb, feats.size()+" images(s) read by BBOX query from "+table+" at zoom "+zoom);

			// Get a set of tiles within/ across a bounding box
			zoom = 4;
			feats = geoPackage.getTiles(table, bbox, zoom);
			setTestMsg(fb, feats.size()+" images(s) read by BBOX query from "+table+" at zoom "+zoom);

			// Vector...
			crs = new CoordinateReferenceSystemImpl("4326");
			bbox = new BoundingBoxImpl(-73, -71, 17.5, 19.1, crs);
			table = "geonames";

			// Get all features from a table via bbox
			feats = geoPackage.getFeatures(table, bbox);
			setTestMsg(fb, feats.size()+" feature(s) read by BBOX query from "+table);

			// Get all features within a bounding box from all feature tables
			List<GpkgTable> ts = geoPackage.getUserTables(GpkgTable.TABLE_TYPE_FEATURES);

			for (GpkgTable gt : ts) {
				table = gt.getTableName();
				try {
				feats = geoPackage.getFeatures(table);
				setTestMsg(fb, feats.size()+" feature(s) read via bbox query from "+table);
				} catch (Exception e) {
					setTestMsg(fb, "Error: "+e.getLocalizedMessage()+" reading from "+table);
					e.printStackTrace();
				}
			}

			break;
		}

		return true;

	}
	/**
	 *
	 * @param fb
	 * @param msg
	 */
	private void setTestMsg(ITestFeedback fb, String msg) {
		if (fb!=null) {
			fb.testComplete( msg );
		} else {
			log.log(Level.INFO, msg);
		}
	}
	/** Simple interface to receive a message from load and query tests.
	 *
	 */
	public interface ITestFeedback {
		/** A sets has been completed with the supplied text
		 *
		 * @param msg
		 */
		public void testComplete(String msg);
	}
}
