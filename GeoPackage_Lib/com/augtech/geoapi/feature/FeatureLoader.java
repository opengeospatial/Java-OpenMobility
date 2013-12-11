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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

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
	protected Map<Name, SimpleFeatureType> definedFeatureTypes;
	protected String nextID = "";

	/**
	 * 
	 * @param featureTypes
	 */
	public FeatureLoader(Map<Name, SimpleFeatureType> featureTypes) {
		this.definedFeatureTypes = featureTypes;
	}
	
	/** Processes an InputStream of data into this container.<p>
	 * This process should handle uncompressing the stream if required.
	 * 
	 * @param inStream The InputStream to read features from
	 */
	public abstract int loadFeatures(InputStream inStream) throws Exception;

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
	/** Get the first SimpleFeatureType from the Map of defined types provided
	 * in the constructor.
	 * 
	 * @return SimpleFeatureType or Null if none exist
	 */
	public final SimpleFeatureType getDefaultType() {
		if (definedFeatureTypes==null) return null;
		
		for (SimpleFeatureType sft : definedFeatureTypes.values()) {
			return sft;
		}
		return null;
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
