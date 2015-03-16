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
import java.util.List;
import java.util.Map;

/** This datatype class defines the properties of a specific service 
 * binding or inline content for an offering. The service binding is primarily
 * characterized by a series of parameters. The parameters valid for a 
 * specific type of service binding, e.g. WFS are defined outside of the 
 * OWS Context core specification. Each specific service binding is 
 * defined by a URI which references a requirement class.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface Offering {
	/** The name of this class in the XML document */
	public static final String TAG = "offering";
	
	/** Get a unique ID for this offering, preferably one the enables
	 * distinction between service end-points. (i.e. If multiple
	 * offerings point at the same end-point this ID should be the
	 * same). If the Offering has in-line content then the code will
	 * be unique across offerings.
	 * 
	 * @return
	 */
	public int getID();
	/** Is this an OGC service (WFS, WMS, WCS etc), or either
	 * a flat file or in-line content?
	 * 
	 * @return True if an OGC service offering, false if not.
	 */
	public boolean isService();
	/** Code identifying the type of service offering.<p>
	 * 
	 * Operations of a specific service request should be defined 
	 * in a separate extension of this document. Additional, 
	 * custom additions are supported on an ad-hoc basis without 
	 * changing the core service offering type. Any modification 
	 * of the parameter field types or semantics would require a 
	 * new service offering code value.
	 * 
	 * @return
	 */
	public URI getCode();
	/** Operations used to invoke the service.<p>
	 * 
	 * Operations of a specific service request should be defined 
	 * in a separate extension of this document. Additional, 
	 * custom additions are supported on an ad-hoc basis without 
	 * changing the core service offering type. Any modification 
	 * of the parameter field types or semantics would require a 
	 * new service offering code value.
	 * 
	 * @return
	 */
	public List<Operation> getOperations();
	/** The number of Operations in this Offering
	 * 
	 * @return
	 */
	public int getOperationCount();
	/** inline content 
	 * 
	 * @return
	 */
	public List<Content> getContents();
	/** The number of in-line contents in this Offering entry
	 * 
	 * @return
	 */
	public int getContentsCount();
	/** Style sets to style the in-line content
	 * 
	 * @return
	 */
	public StyleSet getStyleset();
	/** Application specific content 
	 * 
	 * @return
	 */
	Map<String, String> getExtensions();
	
}
