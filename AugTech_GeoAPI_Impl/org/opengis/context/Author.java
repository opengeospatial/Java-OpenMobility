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

/** Details about the author of the context document or context offering
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface Author {
	/** The lcal name of this class in the XML document */
	public static final String TAG = "author";
	/** Name
	 * 
	 * @return
	 */
	public String getName();
	/** Email
	 * 
	 * @return
	 */
	public String getEmail();
	/** URI
	 * 
	 * @return
	 */
	public URI getURI();
}
