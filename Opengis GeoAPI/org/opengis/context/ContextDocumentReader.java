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

import java.io.InputStream;
import java.util.Set;

import org.xml.sax.helpers.XMLFilterImpl;

import com.augtech.geoapi.context.xml.ContextFilterImpl;
/** An interface to read a context document through an XML Parser,
 * such as SAX
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface ContextDocumentReader {

	/** Parse the OWS Context document
	 * 
	 * @throws Exception
	 */
	public void parse() throws Exception;
	/** Once a document has been parsed to an {@link Content} document
	 * it will passed back to the reader through this method. 
	 * 
	 * @param contextDoc
	 */
	public void setDocument(Context contextDoc);
	/** Enables the {@link ContextFilterImpl} to get the underlying
	 * {@link InputStream}
	 * 
	 * @return
	 */
	public InputStream getInputStream();
	/** Enables the SAX Context document parser implementation to 
	 * get a secondary parser to deal with GML Geometries
	 * 
	 * @return
	 */
	public XMLFilterImpl getGeometryFilter();
	
	/** Get a set of namespaces that defines any extension elements within the
	 * context document to be populated on to relevant classes.
	 * 
	 * @return
	 */
	public Set<String> getExtensionNameSpaces();
	
	/** Retrieve the {@link Context} document from this reader
	 * 
	 * @return
	 */
	public Context getContextDoc();
}
