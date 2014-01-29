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
package com.augtech.geoapi.context;

import java.util.HashMap;
import java.util.Map;

import org.opengis.context.CreatorDisplay;

/**
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class CreatorDisplayImpl implements CreatorDisplay {
	private Map<String, String> cdValues = new HashMap<String, String>();
	private Map<String, String> extensions = new HashMap<String, String>();
	/**
	 * 
	 * @param cdValues
	 */
	public CreatorDisplayImpl(Map<String, String> cdValues) {
		this(cdValues, null);
	}
	public CreatorDisplayImpl(Map<String, String> cdValues, Map<String, String> extensions) {
		this.cdValues = cdValues;
		if(extensions!=null) this.extensions = extensions;
	}
	
	@Override
	public int getPixelWidth() {
		return Integer.valueOf(cdValues.get("pixelWidth"));
	}

	@Override
	public int getPixelheight() {
		return Integer.valueOf(cdValues.get("pixelHeight"));
	}

	@Override
	public double getMMPerPixel() {
		return Double.valueOf(cdValues.get("mmPerPixel"));
	}

	@Override
	public Map<String, String> getExtensions() {
		return extensions;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("CreatorDisplayImpl [Width=%s, Height=%s, mmPerPixel=%s]",
				cdValues.get("pixelWidth"), cdValues.get("pixelHeight"), cdValues.get("mmPerPixel") );
	}



}
