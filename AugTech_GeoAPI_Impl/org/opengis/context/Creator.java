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
package org.opengis.context;

import java.util.Map;

/** This datatype class provides place to encode information 
 * related to the creator of the con text document. It includes 
 * the creator application and any relevant properties or settings 
 * for the application. 
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface Creator {
	/** The lcal name of this class in the XML document */
	public static final String TAG = "generator";
	/** The name, reference and version of the creator application used to 
	 * create the context document 
	 * 
	 * @return
	 */
	public CreatorApplication getCreatorApplication();
	/** Properties of the display in use when the context document was 
	 * created (for display based applications only).
	 * 
	 * @return
	 */
	public CreatorDisplay getCreatorDisplay();
	/** Any encoding should allow the user to extend the Creator information to include custom items 
	 * 
	 * @return
	 */
	public Map<String, String> getExtensions();

	
}
