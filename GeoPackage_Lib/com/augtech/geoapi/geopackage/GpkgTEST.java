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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;

import com.augtech.geoapi.feature.FeatureCollection;
import com.augtech.geoapi.feature.NameImpl;
import com.augtech.geoapi.feature.loader.GML2_1;
import com.augtech.geoapi.feature.loader.RasterTile;
import com.augtech.geoapi.feature.type.AttributeTypeImpl;
import com.augtech.geoapi.feature.type.GeometryDescriptorImpl;
import com.augtech.geoapi.feature.type.GeometryTypeImpl;
import com.augtech.geoapi.feature.type.SimpleFeatureTypeImpl;
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
			
			int numExist = geoPackage.getDefinedUserTables(GpkgTable.TABLE_TYPE_FEATURES).size();
			log.log(Level.INFO, ""+numExist+" feature tables in the GeoPackage");
			
			numExist = geoPackage.getDefinedUserTables(GpkgTable.TABLE_TYPE_TILES).size();
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
		attrs.add(new AttributeTypeImpl(new NameImpl("the_geom"), Geometry.class) );

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
		Map<Name, SimpleFeatureType> fTypes = new HashMap<Name, SimpleFeatureType>();
		fTypes.put(fTypeName, sft);
		
		// Create a loader for the tiles
		RasterTile loader = new RasterTile( fTypes );
		int numLoaded = 0;
		try {
			// Load each file..
			for (File f : path.listFiles()) {
				loader.setInitialID( f.toString() );
				numLoaded = loader.loadFeatures( new FileInputStream(f) );
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		if (imageCollection==null) imageCollection = new FeatureCollection();

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
		
		InputStream gmlFile = this.getClass().getResourceAsStream(TEST_GML_FILE);
		if (gmlFile==null) return false;
		
		log.log(Level.INFO, "Loading GML...");

		GML2_1 gmlLoader = new GML2_1(null);
		
		int numFeat = gmlLoader.loadFeatures( gmlFile );

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
		
		boolean processed = false;
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

			geoPackage.createFeaturesTable(single, featureCollection.getBounds() );
			
			numCreated++;
		}
		
		log.log(Level.INFO, "Created "+numCreated+" feature tables (types)");
		
		return processed;
	}
}
