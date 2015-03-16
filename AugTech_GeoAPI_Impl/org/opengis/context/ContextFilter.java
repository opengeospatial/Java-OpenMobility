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
import java.util.Set;

import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

/** An interface for ATOM entry elements to pass information
 * to/ from the parent context document. It is recomended this is 
 * implemented on a {@link Context} document XMLFilterImpl SAX parser.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface ContextFilter extends XMLFilter {

	/** Get the current XML element value
	 * 
	 * @return
	 */
	public String getElementValue();
	/** Pass a new Resource entry value back-up to the 
	 * Context document.
	 * 
	 * @param entry
	 */
	public void addEntry(Resource entry);
	/** Ask the parent to build a Geometry object and place it
	 * in the supplied Map of current Entry values.
	 * 
	 * @param values
	 */
	public void buildWhereGeometry(Map<String, Object> entryValues) throws SAXException;
	/** Get the contents of the current Content buffer up to and
	 * including the last tag name matching qName.
	 * 
	 * @param qName
	 * @return
	 */
	public String getContentBuffer(String qName);
	/** Reset the content buffer up to the first instance
	 * of qName (keeping the qName tag)
	 * 
	 * @param qName
	 */
	public void resetContentBuffer(String qName);
	/** Check whether the Content buffer currently has String
	 * data in it.
	 * @return
	 */
	public boolean hasContentInBuffer();
	
	/** Get a set of namespaces that defines any extension elements within the
	 * context document to be populated on to relevant classes.
	 * 
	 * @return
	 */
	public Set<String> getExtensionNameSpaces();
}
