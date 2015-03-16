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

import java.net.URL;
import java.util.Map;

/** This datatype class defines a portrayal style for a 
 * resource content or serviceContent element. It can be 
 * specified at a resource level or at a service offering 
 * level (when it only applies to the offering.<p>
 * 
 * Note: StyleSet cannot be derived from the service request, and it 
 * is a list of relevant styles which could be applied if the 
 * service supports dynamic styling, for example if the WMS 
 * is an FPS and supports SLD. The intention is that the client 
 * could visualize the layer using the getMap Call (which would have a 
 * defined styling) but could also offer the user a selection of 
 * alternative styles for the layer. These would be derived from the 
 * style list parameter. 
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface StyleSet {
	/** The name of this class in the XML document */
	public static final String TAG = "styleSet";
	/** Unique name of the styleSet within a given offering 
	 * 
	 * @return
	 */
	public String getName();
	/** A Human Readable Title for the OWS Context Document 
	 * 
	 * @return
	 */
	public String getTitle();
	/** Description of the Style 
	 * 
	 * @return
	 */
	public Content getAbstract();
	/** Whether this Styleset is the one to be used as default (initial display) 
	 * 
	 * @return
	 */
	public boolean getDefault();
	/** URL of a legend image for the style 
	 * 
	 * @return
	 */
	public URL getLegendUrl();
	/** The in-line content (an SLD or CSS) or an external reference to the style definition
	 * 
	 * @return
	 */
	public Content getContent();
	/** Any encoding should allow the user to extend the style definition to include custom items 
	 * 
	 * @return
	 */
	public Map<String, String> getExtensions();
	
}
