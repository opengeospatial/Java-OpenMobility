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
package com.augtech.geoapi.feature;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

import com.augtech.geoapi.geopackage.GeoPackage;

/** An abstract class for loading and creating {@link SimpleFeature} from local storage, once 
 * they have been downloaded from the server. This class processes saved data in to a 
 * list of{@link SimpleFeature} to pass onto the Dataset FeatureCollection.
 * <br>It is the responsibility of the implementor to handle
 * reading of features and of additional query type (getFeatureInfo) requests via the 
 * the {@link #loadFeatures(InputStream)} method.<p>
 * Sub-class should use either a default blank constructor, or one with a Map
 * to ensure they are instantiated correctly.
 */
public abstract class FeatureLoader extends ArrayList<SimpleFeature> {
	private static final long serialVersionUID = 4264941226040642321L;
	protected String nextID = "";
	protected GeoPackage geoPackage = null;
	protected int gpkgChunkSize = 50000;
	protected boolean loadToGpkg = false;
	protected int featuresInserted = 0;
	private int featCounter = 0;

	/** Construct a new FeatureLoader that supports the supplied {@link FeatureType}'s 
	 * and loads the data directly to the supplied GeoPackage<p>
	 * FeatureType's are generally required where the underlying data being loaded
	 * does not contain enough information to create a
	 * proper type, for example where one ultimately wants 10 attributes, but the
	 * data may only contain 2, or the data is an image.
	 * 
	 * @param gpkg The GeoPackage to load feature's into. Note that tables are <i>not</i>
	 * created automatically during load. If Null
	 * the data is stored on this loader as per normal. The Default chunk size
	 * is 25,000
	 * @see #setLoadToGpkgOnly(int)
	 */
	public FeatureLoader(GeoPackage gpkg) {
		this.geoPackage = gpkg;
		setLoadToGpkgOnly(25000);
	}
	/** Is a {@link SimpleFeatureType} required in order to load feature's 
	 * through {@link #loadFeatures(File, SimpleFeatureType)} ?
	 *  
	 * @return True if it is
	 */
	public abstract boolean isTypeRequiredForLoad();
	/** Processes an input file of data into this list backing this loader, or 
	 * the GeoPackage supplied in the constructor (if supplied).<p>
	 * 
	 * @param inFile The File to read features from
	 * @param useType A {@link SimpleFeatureType} to use when constructor the feature's
	 * from the file, or <code>Null</code> to use the types defined within the file (i.e. 
	 * GML based data).
	 * @return The number of feature's loaded or inserted into the GeoPackage
	 * @see #setLoadToGpkgOnly(int)
	 */
	public abstract int loadFeatures(File inFile, SimpleFeatureType useType) throws Exception;
	
	@Override
	public void add(int index, SimpleFeature element) {
		super.add(index, element);
		featCounter++;
		loadToGpkg();
	}

	@Override
	public boolean add(SimpleFeature e) {
		boolean ret = super.add(e);
		featCounter++;
		loadToGpkg();
		return ret;
	}

	@Override
	public boolean addAll(Collection<? extends SimpleFeature> c) {
		boolean ret = super.addAll(c);
		featCounter +=this.size();
		loadToGpkg();
		return ret;
	}

	@Override
	public boolean addAll(int index, Collection<? extends SimpleFeature> c) {
		boolean ret= super.addAll(index, c);
		featCounter +=this.size();
		loadToGpkg();
		return ret;
	}
	/**
	 * 
	 * @return
	 * @throws IllegalDataException
	 */
	private boolean loadToGpkg() {
		if (!loadToGpkg || geoPackage==null || featCounter < gpkgChunkSize) return false;

		try {
			featuresInserted += geoPackage.insertFeatures( this );
			this.clear();
			System.gc();
			featCounter = 0;
			
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		
		Logger.getAnonymousLogger().log(Level.INFO, "Inserted "+featuresInserted+" to GeoPackage");
		return true;

	}
	/** Commit any final feature's stored on the loader to the GeoPackage.
	 * This must be called at the end of {@link #loadFeatures(InputStream)}
	 * to ensure all features are inserted in to the GeoPackage.<p>
	 * Has no effect if no GeoPackage
	 * 
	 * @return The total number of features inserted in to the GeoPackage,
	 * or the number of features in the backing list if no GeoPackage has been supplied.
	 */
	public final int commit() {
		if (!loadToGpkg || geoPackage==null) return this.size();
		
		try {
			featuresInserted += geoPackage.insertFeatures( this );
			this.clear();
			System.gc();
			featCounter = 0;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return featuresInserted;
	}
	/** Load the features directly in to a GeoPackage instead of this feature loader's
	 * backing list. The backing list is cleared after processing in to the GeoPackage
	 * to preserve memory.
	 * 
	 * @param chunkSize The number of features to process before inserting into
	 * the GeoPackage. Defaults to 50,000 if < 0 is supplied
	 */
	public final void setLoadToGpkgOnly(int chunkSize) {
		loadToGpkg = true;
		gpkgChunkSize = chunkSize < 1 ? 50000 : chunkSize;
	}
	/** Get the {@link SimpleFeatureType}'s that were loaded
	 * 
	 * @return A Map of the loaded types, or null if nothing is/ was loaded
	 */
	public Map<Name, SimpleFeatureType> getLoadedTypes() {
		if (this.size()==0) return null;
		
		Map<Name, SimpleFeatureType> fTypes = new HashMap<Name, SimpleFeatureType>();
		
		// Get types loaded
		for (SimpleFeature sf : this) {
			fTypes.put(sf.getFeatureType().getName(), sf.getFeatureType() );
		}
		return fTypes;
	}

	/** Does this feature loader require an ID to be set manually
	 * when loading between tiles? For example, no unique feature ID is
	 * provided as an output of the service. Use setInitialID() if required.
	 * 
	 * @return True if an ID needs to be set
	 */
	public abstract boolean isInitialIDRequired();
	/** If the server does not respond with unique IDs for features,
	 * this can be used to set the first ID for the next tile that is
	 * loaded via loadFeatures().
	 * 
	 * @param id
	 */
	public final void setInitialID(String id) {
		nextID = id;
	}

}
