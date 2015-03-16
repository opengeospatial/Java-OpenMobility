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
 * related to the display area used in the creator application 
 * when the OWS Context document was produced. This set of properties 
 * only applies to creator applications which are using a geographic 
 * display and is supporting information to the exploiter of the OWS 
 * Context document.<p>
 * Note the elements within creator display are 
 * intended as supporting information (metadata) for clients and not 
 * properties which should control the display size of the client opening the document.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface CreatorDisplay {
	/** The lcal name of this class in the XML document */
	public static final String TAG = "display";
	/** Pixel width of the display specified by Area of Interest. 
	 * 
	 * @return
	 */
	public int getPixelWidth();
	/** Pixel height of the display specified by Area of Interest.
	 * 
	 * @return
	 */
	public int getPixelheight();
	/** The number of mm per pixel for the display. If no value is 
	 * available the field should be set to NULL. 
	 * 
	 * @return
	 */
	public double getMMPerPixel();
	/** Any encoding should allow the user to extend the display information to include custom items 
	 * 
	 * @return
	 */
	public Map<String, String> getExtensions();
	
}
