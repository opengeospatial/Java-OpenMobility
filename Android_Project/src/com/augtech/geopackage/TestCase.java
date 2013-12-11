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
package com.augtech.geopackage;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;

import android.content.Context;
import android.util.Log;

import com.augtech.geoapi.feature.FeatureCollection;
import com.augtech.geoapi.feature.NameImpl;
import com.augtech.geoapi.feature.loader.GML2_1;
import com.augtech.geoapi.feature.loader.RasterTile;
import com.augtech.geoapi.feature.type.AttributeTypeImpl;
import com.augtech.geoapi.feature.type.GeometryDescriptorImpl;
import com.augtech.geoapi.feature.type.GeometryTypeImpl;
import com.augtech.geoapi.feature.type.SimpleFeatureTypeImpl;
import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgTable;
import com.augtech.geoapi.geopackage.table.FeaturesTable;
import com.augtech.geoapi.geopackage.table.TilesTable;
import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;
import com.vividsolutions.jts.geom.Geometry;

/** An Android specific test layer that utilises some of the Aug-Tech GeoAPI facilities 
 * for loading data and then testing the Java GeoPackage implementation.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class TestCase {
	static final String LOG_TAG = "GeoPackage Tests";
	GeoPackage geoPackage = null;
	File gpkgFile = null;
	FeatureCollection featureCollection = null;
	FeatureCollection imageCollection = null;
	
	/**
	 * 
	 * @param appContext
	 * @param gpkgFile
	 * @param overwrite
	 */
	public TestCase(Context appContext, File gpkgFile, boolean overwrite) {
		
		this.gpkgFile = gpkgFile;

		AndroidSQLDatabase aSQL = new AndroidSQLDatabase(appContext, gpkgFile);
		//GeoPackage.MODE_STRICT = false;
		Log.i(LOG_TAG, "Connecting to GeoPackage...");
		
		try {
			geoPackage = new GeoPackage(aSQL, overwrite);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		// Quick test to get the current contents
		if (geoPackage!=null) {
			int numExist = geoPackage.getUserTables(GpkgTable.TABLE_TYPE_FEATURES).size();
			Log.i(LOG_TAG, ""+numExist+" feature tables in the GeoPackage");
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
	/** Did the GeoPackage file load?
	 * 
	 * @return
	 */
	public boolean isGpkgLoaded() {
		return this.geoPackage !=null;
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
		
		Log.i(LOG_TAG, "Loading Images...");
		
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
		
		if (imageCollection==null) imageCollection = new FeatureCollection(fTypes);
		int numMerged = imageCollection.mergeAll( loader );
		
		Log.i(LOG_TAG, "Loaded "+numMerged+" images");
		
		return numMerged > 0;
	}
	
	/** Load a GML File to the internal feature collection.
	 * 
	 * @param fileName
	 * @param overwrite True to overwrite the existing FeatureCollection, False to append 
	 * @return True if >0 features are loaded to the featureCollection
	 * @throws Exception
	 */
	public boolean loadGMLToCollection(File gmlFile, boolean overwrite) throws Exception {
		if (!gmlFile.exists()) return false;
		Log.i(LOG_TAG, "Loading GML...");
		
		FileInputStream gmlIS = new FileInputStream( gmlFile );
		GML2_1 gmlLoader = new GML2_1(null);
		
		int numFeat = gmlLoader.loadFeatures(gmlIS);

		Map<Name, SimpleFeatureType> fTypes = gmlLoader.getLoadedTypes();
		if (overwrite) {
			featureCollection = new FeatureCollection( fTypes );
		} else {
			if (featureCollection == null) {
				featureCollection = new FeatureCollection( fTypes );
			}
			// In case the new file has types not previously seen
			if (featureCollection.size()>0) {
				featureCollection.getDefinedTypes().putAll(fTypes);
			}
		}
		int merged = featureCollection.mergeAll(gmlLoader);
		
		// The merged figure can be less as duplicate IDs will not be added.
		if (numFeat!=merged) Log.i(LOG_TAG, ""+(numFeat-merged)+" features not loaded to FC");

		Log.i(LOG_TAG, "Loaded "+merged+" features");
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
		
		Log.i(LOG_TAG, "Inserting images to GeoPackage...");
		int numIns = 0;
		
		if (allTiles) {
			
			numIns = geoPackage.insertTiles( imageCollection );
			
		} else {
			
			long rowID = geoPackage.insertTile( imageCollection.get(0) );
			if (rowID>-1) numIns = 1;
			
		}
	
		Log.i(LOG_TAG, "Inserted "+numIns+" images.");
		
		return numIns;
	}
	/**
	 * 
	 * @param allFeatures
	 * @return
	 * @throws Exception
	 */
	public int insertFeaturesFromCollection(boolean allFeatures) throws Exception {
		if (featureCollection==null) return -1;
		int numIns = 0;
		Log.i(LOG_TAG, "Inserting features to GeoPackage...");
		
		if (allFeatures) {
			
			numIns = geoPackage.insertFeatures( featureCollection );
			
		} else {
			
			long rowID = geoPackage.insertFeature( featureCollection.get(0) );
			if (rowID>-1) numIns = 1;
			
		}
		
		Log.i(LOG_TAG, "Inserted "+numIns+" features.");
		return numIns;
	}
	/**
	 * 
	 * @param allTypes
	 * @return
	 * @throws Exception
	 */
	public boolean createFeatureTablesFromCollection(boolean allTypes) throws Exception {
		if (featureCollection==null) return false;
		boolean processed = false;
		Log.i(LOG_TAG, "Creating feature tables...");
		
		int numCreated = 0;

		if (allTypes) {
			
			for (SimpleFeatureType sft : featureCollection.getDefinedTypes().values()) {
				
				FeaturesTable ft = geoPackage.createFeaturesTable(sft, featureCollection.getBounds() );
				
				if (ft==null) return false;
				numCreated++;
			}
			
		} else {
			
			SimpleFeatureType single = null;
			for (SimpleFeatureType sft : featureCollection.getDefinedTypes().values()) {
				single = sft;
				break;
			}

			geoPackage.createFeaturesTable(single, featureCollection.getBounds() );
			
			numCreated++;
		}
		
		Log.i(LOG_TAG, "Created "+numCreated+" feature tables (types)");
		
		return processed;
	}
}
