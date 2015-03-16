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

import java.net.URI;

/** This datatype class provides place to encode information related to the 
 * creator context when the document was produced. It includes the creator 
 * application and relevant properties or settings for the application. 
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface CreatorApplication {
	/** The lcal name of this class in the XML document */
	public static final String TAG = "generator";
	/** Title or name of the application (for display purposes)
	 * 
	 * @return
	 */
	public String getTitle();
	/** URI describing the creator application.
	 * 
	 * @return
	 */
	public URI getUri();
	/** Version of the application. 
	 * 
	 * @return
	 */
	public String getVersion();

	
}
