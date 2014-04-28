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

/** Definition of the operation either to get the information or 
 * to get the capabilities. Note that service specific extension 
 * requirements may mandate more than one owc:operation.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface Operation {
	/** The lcal name of this class in the XML document */
	public static final String TAG = "operation";
	/** Code identifying the type of Operation.<p>
	 * 
	 * Typically the OGC Service request type, e.g. "GetCapabilities" or "GetMap".
	 * 
	 * @return
	 */
	public String getCode();
	/** Name of operation method request
	 * 
	 * @return
	 */
	public String getMethod();
	/** MIMEType of the return result 
	 * 
	 * @return
	 */
	public String getType();
	/** Service Request URL.<p> 
	 * 
	 * Full request URL for an http:get, and request URL http:post.
	 * 
	 * @return
	 */
	public String getURI();
	/** Optional request body content.<p>
	 * 
	 * Note not necessarily XML (defined by MIMEType).
	 * 
	 * @return
	 */
	public Content getRequest();
	/** result of the operation (optional) 
	 * 
	 * @return
	 */
	public Content getResult();
	/** Application specific content 
	 * 
	 * @return
	 */
	Map<String, String> getExtensions();
}
