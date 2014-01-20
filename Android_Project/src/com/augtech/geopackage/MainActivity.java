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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.augtech.geoapi.feature.NameImpl;
import com.augtech.geoapi.feature.type.AttributeTypeImpl;
import com.augtech.geoapi.feature.type.GeometryDescriptorImpl;
import com.augtech.geoapi.feature.type.GeometryTypeImpl;
import com.augtech.geoapi.feature.type.SimpleFeatureTypeImpl;
import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgTEST;
import com.augtech.geoapi.geopackage.geometry.StandardGeometryDecoder;
import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;
import com.vividsolutions.jts.geom.Geometry;
/** The main Activity for running test cases
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class MainActivity extends Activity {

	static final String LOG_TAG = "GeoPackage Client";
	GpkgTEST testing = null;
	//File testDir = getDirectory("Awila/tiles");
	File testDir = getDirectory("GeoPackageTest");
	TextView statusText = null;
	private boolean overWrite = true;
	String tilesTable = "historic_images";
	Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mContext = this;
		
		GeoPackage.MODE_STRICT = true;
		
		AndroidSQLDatabase gpkgDB = new AndroidSQLDatabase(this, new File(testDir, "gml_test.gpkg"));
		//AndroidSQLDatabase gpkgDB = new AndroidSQLDatabase(this, new File(testDir, "haiti-vectors-split.gpkg"));
		testing = new GpkgTEST(gpkgDB, overWrite);

		statusText = (TextView) findViewById(R.id.statusText);
		Button load = (Button)findViewById(R.id.btn_testLoad);
		Button query = (Button)findViewById(R.id.btn_testQuery);
		load.setOnClickListener(testLoadClick);
		query.setOnClickListener(testQueryClick);
		
		if (testing==null || testing.getGeoPackage()==null) {
			statusText.setText("Failed to load GeoPackage");
			load.setEnabled(false);
			query.setEnabled(false);
		}
		
		if ( !isStorageWriteable() ) {
			statusText.setText("Cannot write to disk");
			load.setEnabled(false);
		}
		
		
	}

	private View.OnClickListener testQueryClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			boolean Haiti = true;
			
			try {
				// If it is loaded, test querying for features/ tiles
				if (testing.getGeoPackage()!=null) {
					
					List<SimpleFeature> feats = null;
					BoundingBox bbox = null;

					// test Port-au-Prince, Haiti
					if (Haiti) {
					
						feats = testing.getGeoPackage().getFeatures("linear_features", 
									"id=1", new StandardGeometryDecoder() );
						
						bbox = new BoundingBoxImpl(
								-72.335822, -72.633677, 18.4617532, 18.8551624, 
								new CoordinateReferenceSystemImpl("4326") );
						
						feats = testing.getGeoPackage().getFeatures("linear_features", bbox);
						statusText.setText(""+feats.size()+" feature(s) read via bbox query!");
					
						return;
					}
					// Get all features from a table
					feats = testing.getGeoPackage().getFeatures("surface_water_sewer", "", new StandardGeometryDecoder());
					statusText.setText(""+feats.size()+" feature(s) read!");
					
					// Get all features within a bounding box
					bbox = new BoundingBoxImpl(
							380587, 395041, 252954, 273645, 
							new CoordinateReferenceSystemImpl("27700") );
					feats = testing.getGeoPackage().getFeatures("surface_water_sewer", bbox);
					statusText.setText(""+feats.size()+" feature(s) read via bbox query!");
					
					bbox = new BoundingBoxImpl(
							380587, 395041, 252954, 273645, 
							new CoordinateReferenceSystemImpl("27700") );
					feats = testing.getGeoPackage().getFeatures("foul_sewer", bbox);
					statusText.setText(""+feats.size()+" feature(s) read via bbox query!");
					
					
//					// Get all tiles from a single table
//					feats = testing.getGeoPackage().getTiles(tilesTable, null);
//					statusText.setText(""+feats.size()+" images(s) read!");
//					
//					// Get a single tile we know we have
//					feats = testing.getGeoPackage().getTiles(tilesTable, "zoom_level=18 and tile_column=129506 and tile_row=86286");
//					statusText.setText(""+feats.size()+" images(s) read!");
//
//					// Get a set of tiles within/ across a bounding box
//					bbox = new BoundingBoxImpl(
//							-239247.301401622, -238788.67923184743, 6846158.279178806, 6846616.90134858, 
//							new CoordinateReferenceSystemImpl("3857"));
//					feats = testing.getGeoPackage().getTiles(tilesTable, bbox, 18);
//					statusText.setText(""+feats.size()+" images(s) read!");
					
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
				statusText.setText("Error: "+e.getMessage());
			}
		}
	};
	
	private View.OnClickListener testLoadClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			try {
				// Load a GML file to use for testing creation and inserts
				testing.loadGMLToCollection( false );
				statusText.setText("GML file loaded");
				
				// Load feature types from GML into the GeoPackage
				boolean typesloaded = testing.createFeatureTablesFromCollection( true );
				statusText.setText("Feature types loaded: "+typesloaded);
				
				// Insert the features into the GeoPackage
				int numLoaded = testing.insertFeaturesFromCollection(true);
				statusText.setText(""+numLoaded+" features inserted");
				
				/* Create a table to hold tiles. 
				 * This is the same name that the image FeatureType will have*/
				testing.createTilesTable( tilesTable, 1024 );

				// Load a full tree of tiles
				List<File >files = getFileListingNoSort(getDirectory("Awila/tiles/567/17"), true, true);
				int i=0;
				for (File file : files) {
					if (file.isDirectory()) {
						testing.loadTilesToCollection( tilesTable, file );
						i++;
					}
				}
				if (i>0) {
					numLoaded = testing.insertTilesFromCollection(true);
					statusText.setText(""+numLoaded+" images inserted");
				}
			} catch (Exception e) {
				e.printStackTrace();
				statusText.setText("Error: "+e.getMessage());
				return;
			}
			
			// Disable button to stop multiple loads
			v.setEnabled(false);
			
		}
	};
	
	
	@Override
	public void onBackPressed() {
		if(testing !=null && testing.getGeoPackage()!=null) testing.getGeoPackage().close();
		System.exit(0);
	}


	/** Do we have write access to the local SD card?
	 * 
	 * @return True if we can read from storage
	 */
	public static boolean isStorageAvailable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    } else {
	    	return false;
	    }
	}
	/** Check can read/write to SD card
	 * 
	 * @return True if we can
	 */
	public static boolean isStorageWriteable() {
	    String state = Environment.getExternalStorageState();
	    return Environment.MEDIA_MOUNTED.equals(state);
	}
	/** Get a directory on extenal storage (SD card etc), ensuring it exists
	 * 
	 * @return a new File representing the chosen directory
	 */
	public static File getDirectory(String directory) {
		if (directory==null) return null;
		String path = Environment.getExternalStorageDirectory().toString();
		path += directory.startsWith("/") ? "" : "/";
		path += directory.endsWith("/") ? directory : directory + "/"; 
		File file = new File(path);
		file.mkdirs();
		return file;
	}
	/**
	 * 
	 * @param aStartingDir
	 * @param incDirs
	 * @param recursive
	 * @return
	 * @throws FileNotFoundException
	 */
	private static List<File> getFileListingNoSort(File aStartingDir, boolean incDirs, boolean recursive) throws FileNotFoundException {
		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = aStartingDir.listFiles();
		List<File> filesDirs = Arrays.asList(filesAndDirs);

		for(File file : filesDirs) {

			if (incDirs) {
				result.add(file); //always add, even if directory
			} else {
				if (!file.isDirectory()) {
					result.add(file);
				}
			}

			if ( file.isDirectory() && recursive) {
				//must be a directory - recursive call!
				List<File> deeperList = getFileListingNoSort(file, incDirs, recursive);
				result.addAll(deeperList);
			}
		}
		return result;
	}
}
