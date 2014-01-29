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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgTEST;
import com.augtech.geoapi.geopackage.GpkgTEST.ITestFeedback;
/** The main Activity for running test cases
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class MainActivity extends Activity implements ITestFeedback {

	static final String LOG_TAG = "GeoPackage Client";
	GpkgTEST testing = null;
	File testDir = getDirectory("GeoPackageTest");
	TextView statusText = null;
	MainActivity thisActivity = null;
	
	private boolean overWrite = false;
	static int currentTestCase = GpkgTEST.TEST_NZ_DEM;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		thisActivity = this;
		
		GeoPackage.MODE_STRICT = true;
		
		String testDB = null;
		
		switch(currentTestCase) {
		case GpkgTEST.TEST_HAITI:
			overWrite = false;
			testDB = "haiti-vectors-split.gpkg";
			break;
		case GpkgTEST.TEST_GML:
		case GpkgTEST.TEST_TILES:
			testDB = "gml_test.gpkg";
			break;
		case GpkgTEST.TEST_NZ:
			overWrite = false;
			testDB = "NZ-Dataset.gpkg";
			break;
		case GpkgTEST.TEST_NZ_DEM:
			overWrite = false;
			testDB = "asc-2.gpkg";
			break;
		}
		
		AndroidSQLDatabase gpkgDB = new AndroidSQLDatabase(this, new File(testDir, testDB));
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

			try {

				testing.runQeryTestCase(currentTestCase, thisActivity);

			} catch (Exception e) {
				e.printStackTrace();
				statusText.setText("Error: "+e.getMessage());
			}
			
		}
	};
	@Override
	public void testComplete(String msg) {
		statusText.setText( msg );
	}
	
	private View.OnClickListener testLoadClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			try {

				// Insert the features into the GeoPackage
				int numLoaded = testing.insertFeaturesFromCollection(true);
				statusText.setText(""+numLoaded+" features inserted");
				
				/* Create a table to hold tiles. 
				 * This is the same name that the image FeatureType will have*/
				testing.createTilesTable( "historic", 1024 );

				// Load a full tree of tiles
				List<File >files = getFileListingNoSort(getDirectory("Awila/tiles/567/17"), true, true);
				int i=0;
				for (File file : files) {
					if (file.isDirectory()) {
						testing.loadTilesToCollection( "historic", file );
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
