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

/** A ContextURI is for the creation/ storage of URI's within the
 * Context document that also include mime-type, title, keyword and
 * optional length information as well as a URI. For URI's that cannot
 * contain this information then a standaard URI is used.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface ContextURI {

	/** The Mime type of the meta data (i.e. application/xml)
	 * 
	 * @return
	 */
	public String getMimeType();
	/** A ContextURI cannot contain a mime-type and a keyword
	 * therefore keyword's can be stored under the mime-type.
	 * This method is provided for consistiency with the 
	 * specification naming conventions.
	 * 
	 * @return
	 */
	public String getKeyword();
	/** 
	 * 
	 * @return
	 */
	public URI getURI();
	/** Any title associated with the meta data
	 * 
	 * @return
	 */
	public String getTitle();
	
	/** Get the length in bytes of the content specified
	 * by the URI.
	 * 
	 * @return The length or -1 if unknown
	 */
	public int getLength();
	
}
