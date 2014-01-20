package com.augtech.geopackager;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;

import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.augtech.geoapi.geopackage.GpkgTEST;
import com.augtech.geoapi.geopackage.geometry.StandardGeometryDecoder;
import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;


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
		boolean overwrite = true;
		boolean testGML = true;
		
		if (testGML) {

			// Use internal GML file
			File dbFile = new File("C:\\Client Projects\\OGC OWS-10\\WinPackage.gpkg");
			tests = new GpkgTEST(new JDatabase(dbFile), overwrite);

		} else {

			// Use a.n.other DB
			File dbFile = new File("C:\\Client Projects\\OGC OWS-10\\haiti-vectors-split.gpkg");
			tests = new GpkgTEST(new JDatabase(dbFile), overwrite);

		}

		
		if (tests!=null && tests.getGeoPackage()!=null) {
			try {

				if(overwrite && testGML) {
					//tests.loadGMLToCollection(true);
					//tests.createFeatureTablesFromCollection(true);
					
					int insert = tests.insertFeaturesFromCollection(true);
					log.log(Level.INFO, "Insert 1: "+insert+" feature(s) inserted!");

				}
				
				List<SimpleFeature> feats = null;
				BoundingBox bbox = null;

				if (testGML) {
				
					// Get all features from a table
					feats = tests.getGeoPackage().getFeatures("surface_water_sewer", "", new StandardGeometryDecoder());
					log.log(Level.INFO, "Query 1: "+feats.size()+" feature(s) read!");
					
					// Get all features within a bounding box
					bbox = new BoundingBoxImpl(
							-2.16, -2.14, 52.25, 52.27, 
							new CoordinateReferenceSystemImpl("4326") );
					feats = tests.getGeoPackage().getFeatures("surface_water_sewer", bbox);
					log.log(Level.INFO, "Query 2: "+feats.size()+" feature(s) read via bbox query!");
					
					bbox = new BoundingBoxImpl(
							-2.16, -2.14, 52.25, 52.27,
							new CoordinateReferenceSystemImpl("4326") );
					feats = tests.getGeoPackage().getFeatures("foul_sewer", bbox);
					log.log(Level.INFO, "Query 3: "+feats.size()+" feature(s) read via bbox query!");
				
				} else {
					
					// test Port-au-Prince, Haiti (from Compusult)
					feats = tests.getGeoPackage().getFeatures("linear_features", 
								"id=1", new StandardGeometryDecoder() );
					
					bbox = new BoundingBoxImpl(
							-72.335822, -72.633677, 18.4617532, 18.8551624, 
							new CoordinateReferenceSystemImpl("4326") );
					
					feats = tests.getGeoPackage().getFeatures("linear_features", bbox);
					log.log(Level.INFO, "Query 1: "+feats.size()+" feature(s) read via bbox query!");
				
					return;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
