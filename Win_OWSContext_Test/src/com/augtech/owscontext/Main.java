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
package com.augtech.owscontext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.augtech.geoapi.context.ContextImpl;
import com.augtech.geoapi.context.xml.ContextDocumentReaderImpl;
import com.augtech.geoapi.feature.loader.Utils;
import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.ISQLDatabase;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		File testDir = new File("C:\\Client Projects\\OGC OWS-10\\OWS Context");
		File testFile = new File(testDir, "awila_sewers.xml");
		File testDB = new File(testDir, "simple_sewer_features.gpkg");
		String docEncoding = "UTF-8";
		boolean overwriteGpkg = false;
		
		
		ContextDocumentReaderImpl owsXML = null;
		String contextXML = null;
		try {
			contextXML = Utils.getStreamToString( new FileInputStream(testFile), docEncoding );
		} catch (FileNotFoundException e1) {}
		
		
		ISQLDatabase gDB = new JDatabase(testDB);
		GeoPackage gpkg = new GeoPackage(gDB, overwriteGpkg);
		boolean inserted = gpkg.insertOWSContext(contextXML, "application/xml", true);
		
		try {

			if (!inserted) {
				Logger.getAnonymousLogger().log(Level.INFO, "Context not inserted to Gpkg");
				return;
			}
			
			String[] result = gpkg.getOWSContext();
			InputStream stream = new ByteArrayInputStream(result[1].getBytes() );
			ContextImpl c = null;
			
			if (result[0].equals("application/xml") || result[0].equals("text/xml")) {
				
				owsXML = new ContextDocumentReaderImpl( stream );
				owsXML.parse();
				
				c = (ContextImpl)owsXML.getContextDoc();
				
			} else if (result[0].equals("application/json")) {
				
				// JSON Parser here!
				
			} else {
				Logger.getAnonymousLogger().log(Level.INFO, "Invalid mime-type");
				return;
			}

			StringBuffer sb = new StringBuffer();
			c.print( sb );
			Logger.getAnonymousLogger().log(Level.INFO, sb.toString() );
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}

}
