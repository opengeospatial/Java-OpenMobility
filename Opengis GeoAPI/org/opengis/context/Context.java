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

import com.vividsolutions.jts.geom.Geometry;

/** This class is the overall container class for the context document
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface Context {
	/** The lcal name of this class in the XML document */
	public static final String TAG = "feed";
	
	/** Get the character encoding for the context document (i.e. UTF-8)
	 * 
	 * @return
	 */
	public String getCharEncoding();
	/** Specification Reference identifying that this is an owc Context document 
	 * 
	 * @return
	 */
	public URI getSpecReference();
	/** Language used in the owc Context document 
	 * 
	 * @return
	 */
	public String getLanguage();
	/** Unique Identifier assigned to the OWS Context Document 
	 * 
	 * @return
	 */
	public URI getId();
	/** A Human Readable Title for the OWS Context Document 
	 * 
	 * @return
	 */
	public ContextValue getTitle(); 
	/** Description of the Context Document Purpose/Content 
	 * 
	 * @return
	 */
	public ContextValue getAbstract(); 
	/** Date when the Context Document was updated 
	 * 
	 * @return
	 */
	public String getUpdateDate(); 
	/** Identifier for the author of the document 
	 * 
	 * @return
	 */
	public Author getAuthor(); 
	/** Identifier for the publisher of the document 
	 * 
	 * @return
	 */
	public String getPublisher(); 
	/** The tool/application used to create the context document and its properties
	 * 
	 * @return
	 */
	public Creator getCcreator();
	/** Rights which apply to the context document<p>
	 * The rights described apply to the Context Document itself not to any of its contents.
	 * 
	 * @return
	 */
	public ContextValue getRights();
	/** Geographic area of interest of the users of the context document<p>
	 * 
	 * These properties define the geographic area of interest and date/time 
	 * interval of interest to the context user. They do not define the bounding 
	 * extent (either in geographic area or time) of the referenced resources. 
	 * The intention is not to provide the overall bounds or clipping extent but 
	 * simply to indicate to a Context Document client the expected view of the 
	 * information in area and time.<p>
	 * 
	 * The Coordinate Reference System shall be unambiguous. It should either be 
	 * implicit in the choice of encoding of an area of interest or explicitly defined.
	 *  
	 * @return
	 */
	public Geometry getAreaOfInterest();
	/** A date/time interval relevant to the context document<p>
	 * 
	 * These properties define the geographic area of interest and date/time 
	 * interval of interest to the context user. They do not define the bounding 
	 * extent (either in geographic area or time) of the referenced resources. 
	 * The intention is not to provide the overall bounds or clipping extent but 
	 * simply to indicate to a Context Document client the expected view of the 
	 * information in area and time.
	 * 
	 * @return
	 */
	public String getTimeIntervalOfInterest();
	/** Keyword related to this context document. Shall support an optional 
	 * codelist parameter. 
	 * 
	 * @return
	 */
	public List<ContextURI> getKeywords();
	/** The number of keywords/ categories for this Context feed
	 * 
	 * @return
	 */
	public int getKeywordCount();
	/** A list of the resources and their access parameters and configuration
	 * for this Context document<p>
	 * 
	 * Resources are ordered. Clients would normally interpret this in terms of 
	 * display order. How the encoding defines the order of layers in relation 
	 * to display shall be defined in the encoding specification.
	 * 
	 * @return
	 */
	public List<Resource> getResources();
	/** The number of resources (Entries) in this Context feed
	 * 
	 * @return
	 */
	public int getResourceCount();
	/** Additional metadata describing the context document itself. The format 
	 * recommendation is ISO19115 complaint metadata. The metadata standard used 
	 * should be specified
	 * 
	 * @return
	 */
	public ContextURI getMetadata();
	/** Any encoding should allow the user to extend the context content to include custom items.<p> 
	 * This is application specific
	 * 
	 * @return
	 */
	public Map<String, String> getExtensions();

	
	
}
