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
import java.util.Map;

/** Content is used to describe various elements of the Context document,
 * such as StyleSet, Resource and Context and can be of either html, text 
 * or xhtml encoding.<p>
 * 
 * Implementations shall support the encoding of all of the 
 * mandatory and optional parameters of the Content Class and 
 * meet all of the tabulated constraints and notes.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface Content {
	/** The lcal name of this class in the XML document */
	public static final String TAG = "content";
	/** Type of the inline content 
	 * 
	 * @return
	 */
	public String getType();
	/** Referenced Content.<p>
	 * 
	 * URL and content elements are mutually exclusive, and the 
	 * provision of one is mandatory. One and only one of URL 
	 * or content should be populated in a specific content 
	 * element (i.e a content element must either have content or reference content). 
	 * 
	 * @return
	 */
	public URI getURI();
	/** Actual content in the content element 
	 * 
	 * URL and content elements are mutually exclusive, and the 
	 * provision of one is mandatory. One and only one of URL 
	 * or content should be populated in a specific content 
	 * element (i.e a content element must either have content or reference content). 
	 * 
	 * @return
	 */
	public String getContent();
	/** Application specific content 
	 * 
	 * @return
	 */
	public Map<String, String> getExtensions();

	
}
