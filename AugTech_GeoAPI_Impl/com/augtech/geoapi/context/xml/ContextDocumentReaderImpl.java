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
package com.augtech.geoapi.context.xml;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.opengis.context.Context;
import org.opengis.context.ContextDocumentReader;
import org.xml.sax.helpers.XMLFilterImpl;

import com.augtech.geoapi.geotools.gml.GMLFilterDocument;
import com.augtech.geoapi.geotools.gml.GMLFilterGeometry;
import com.augtech.geoapi.geotools.gml.GMLHandlerJTS;
import com.vividsolutions.jts.geom.Geometry;
/** An implementation of {@link ContextDocumentReader} to process
 * an OWS Context document into a {@link Context} implementation using
 * a SAX Parser. This class uses an amended version of the GeoTools
 * GML parser to process 'Where' geometries in to JTS Geometry objects
 * for use in the Context class.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class ContextDocumentReaderImpl implements ContextDocumentReader {
	Context contextDoc = null;
	ContextFilterImpl docFilter = null;
	InputStream inStream = null;
	GMLFilterDocument gmlFilter = null;
	Set<String> extensions = new HashSet<String>();
	
	/** Construct a new reader from the supplied InputStream with a set of 
	 * namespaces that define 'extension' tags.
	 * 
	 * @param stream
	 * @param extNameSpaces
	 * @throws Exception
	 */
	public ContextDocumentReaderImpl(InputStream stream, Set<String> extNameSpaces) throws Exception {
		inStream = stream;
		GMLFilterGeometry geomHandler = new GMLFilterGeometry( new GeometryHandler() );
		gmlFilter = new GMLFilterDocument(geomHandler);
		docFilter = new ContextFilterImpl(this);
		if(extNameSpaces!=null) extensions = extNameSpaces;
	}
	/** Construct a new reader from the supplied InputStream
	 * 
	 * @param stream
	 * @throws Exception
	 */
	public ContextDocumentReaderImpl(InputStream stream) throws Exception {
		this(stream, null);
	}
	@Override
	public void parse() throws Exception {
		docFilter.parse();
	}
	
	@Override
	public void setDocument(Context contextDoc) {
		this.contextDoc = contextDoc;
	}
	/** Retrieve the {@link Context} document from this reader
	 * 
	 * @return
	 */
	public Context getContextDoc() {
		return this.contextDoc;
	}
	/**
	 * Handles the string chunks collected in {@link #characters}.
	 */

	@Override
	public InputStream getInputStream() {
		return inStream;
	}

	@Override
	public XMLFilterImpl getGeometryFilter() {
		return gmlFilter;
	}
	private class GeometryHandler extends XMLFilterImpl implements GMLHandlerJTS {

		@Override
		public void geometry(Geometry geometry) {
			docFilter.setGeometry(geometry);
		}
		
	}
	@Override
	public Set<String> getExtensionNameSpaces() {
		return extensions;
	}

}
