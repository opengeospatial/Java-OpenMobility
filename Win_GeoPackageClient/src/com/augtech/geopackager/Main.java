package com.augtech.geopackager;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgTEST;
import com.augtech.geoapi.utils.ProcessASCII;
import com.augtech.nz.SHPLoader;


public class Main {
	Main me = null;
	static GpkgTEST tests = null;
	
	public Main() {
		this.me = this;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Main();

		Logger log = Logger.getAnonymousLogger();

		int done = 0;
		boolean doInsertData = false;
		GeoPackage gpkg = null;

		File testDir = new File("C:\\Client Projects\\OGC OWS-10");
		boolean overWrite = false;
		int currentTestCase = GpkgTEST.TEST_HAITI;

		String testDB = null;
		File loadFile = null;


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
			loadFile = new File(testDir, "nzlri-north-island-edition.shp");
			testDB = "NZ-Dataset.gpkg";
			break;
		case GpkgTEST.TEST_NZ_DEM:
			overWrite = false;
			loadFile = new File(testDir, "1-0003-0005.asc");
			testDB = "asc-2.gpkg";
			break;
		case GpkgTEST.TEST_SIGMA:
			overWrite = false;
			testDB = "world.gpkg";
			break;
		}

		File dbFile = new File(testDir, testDB);
		tests = new GpkgTEST(new JDatabase(dbFile), overWrite);
		gpkg = tests.getGeoPackage();

		if (doInsertData) {
			
			switch(currentTestCase) {
			case GpkgTEST.TEST_NZ_DEM:
				ProcessASCII pa = new ProcessASCII(loadFile, new JDatabase(dbFile), overWrite);
				pa.process("TerrainPoint", 0);
				
				break;
			case GpkgTEST.TEST_NZ:

//				gpkg.setSimplifyOnInsertion(50000, 0.0001);
//
//				try {
//					done = new SHPLoader(gpkg, 4326).loadFeatures( loadFile );
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//				log.log(Level.INFO, "Done: "+done);
				break;
			case GpkgTEST.TEST_GML:
			case GpkgTEST.TEST_TILES:
				if (overWrite) {
					try {
						int insert = tests.insertFeaturesFromCollection(true);
						log.log(Level.INFO, "Insert 1: "+insert+" feature(s) inserted!");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			}
		}


		try {

			tests.runQeryTestCase(currentTestCase, null);

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
