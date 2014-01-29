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

import org.opengis.context.Creator;
import org.opengis.context.CreatorApplication;
import org.opengis.context.CreatorDisplay;
/**
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class CreatorImpl implements Creator {
	CreatorApplication application;
	CreatorDisplay display;
	Map<String, String> extensions = new HashMap<String, String>();
	
	/**
	 * 
	 * @param application
	 * @param display
	 */
	public CreatorImpl(CreatorApplication application, CreatorDisplay display) {
		
	}
	/**
	 * 
	 * @param application
	 * @param display
	 * @param extensions
	 */
	public CreatorImpl(CreatorApplication application, CreatorDisplay display, Map<String, String> extensions) {
		this.application = application;
		this.display = display;
		if (extensions!=null) this.extensions = extensions;
	}

	@Override
	public CreatorApplication getCreatorApplication() {
		return application;
	}

	@Override
	public CreatorDisplay getCreatorDisplay() {
		return display;
	}

	@Override
	public Map<String, String> getExtensions() {
		return extensions;
	}

}
