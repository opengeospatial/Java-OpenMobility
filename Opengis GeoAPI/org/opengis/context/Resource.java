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

/** This class describes the resource elements which supply the 
 * information layers used in the Context Document. Resource, 
 * while it can be used bare, is typically classified by owc:MediaCode 
 * indicating the type of information encoded in the resource. 
 * The resource offers multiple ways to pass a configured resource, in-line content, 
 * by absolute URI and by service Content which needs to be interpreted by an 
 * intelligent client. While there may be limitations in the individual encoding, 
 * each of these types shall be semantically equivalent, i.e. alternative 
 * representations of the same information (they can for example have different 
 * resolutions but should not show fundamentally different information).
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface Resource {
	/** The name of this class in the XML document */
	public static final String TAG = "entry";
	/** Unique Identifier assigned to the owc Resource. Used to reference a resource from other resources 
	 * 
	 * @return
	 */
	public URI getId();
	/** A Human Readable Title for the owc Resource. 
	 * 
	 * @return
	 */
	public ContextValue getTitle();
	/** Description of the Context Document Purpose/Content 
	 * 
	 * @return
	 */
	public Content getAbstract();
	/** Date when the resource definition was updated 
	 * 
	 * @return
	 */
	public String getUpdateDate();
	/** Identifier for the author of the resource definition 
	 * 
	 * @return
	 */
	public Author getAuthor();
	/** Identifier for the publisher of the resource definition 
	 * 
	 * @return
	 */
	public String getPublisher();
	/** Rights which apply to the resource definition.<p>
	 * The semantics of rights is not defined here and needs to be defined in 
	 * extension packages.
	 * 
	 * @return
	 */
	public ContextValue getRights();
	/** The geographic extent of the resource.<p>
	 * 
	 * The geospatial extent indicates to a client that data 
	 * intersects with this area needs to be retrieved and, if relevant portrayed. 
	 * There is no specific requirement to hard clip the data to this boundary.
	 * 
	 * @return
	 */
	public Geometry getGeospatialExtent();
	/** The temporal extent of the content of the resource<p>
	 * 
	 * The temporal extent indicates to a client that data that 
	 * intersects with this time interval needs to be retrieved, and, 
	 * if relevant, portrayed. There is no specific requirement to 
	 * hard clip the data to this boundary.
	 * 
	 * @return
	 */
	public String getTemporalExtent();
	/** A reference to a description of the Context resource in alternative format.
	 * 
	 * @return
	 */
	public ContextURI getContentDescription();
	/** A URI identifying a preview of the resource 
	 * 
	 * @return
	 */
	public ContextURI getPreview();
	/** A URI identifying a service which will return an immediately exploitable 
	 * result by simply requesting based on the URI. The expectation is that 
	 * the return type of this call will be a well-known format 
	 * 
	 * @return
	 */
	public ContextURI getContentByRef();
	/** A list of all service or inline content offerings for the resource targeted at OGC compliant clients 
	 * 
	 * @return
	 */
	public List<Offering> getOfferings();
	
	/** The number of offerings in this resource entry
	 * 
	 * @return
	 */
	public int getOfferingCount();
	/** This flag indicates the state of the resource within the context document. 
	 * It can be interpreted by the caller as required (this may be defined in a 
	 * profile or in the specific service extensions) 
	 * 
	 * @return
	 */
	public boolean isActive();
	/** Keyword related to this resource definition. Shall support an optional codelist parameter. 
	 * 
	 * @return
	 */
	public List<ContextURI> getKeywords();
	/** The number of keywords/ categories in this resource entry
	 * 
	 * @return
	 */
	public int getKeywordCount();
	/** Minimum scale for the display of the layer.<p>
	 * 
	 * The scale denominator is defined with respect to a "standardized rendering pixel size" 
	 * of 0.28 mm × 0.28 mm (millimeters). The definition is the same used in WMS 1.3.0 
	 * [OGC 06-042] and in Symbology Encoding Implementation Specification 1.1.0 [05-077r4]. 
	 * Frequently, the true pixel size is unknown and 0.28 mm is a common actual size for 
	 * current displays.
	 * 
	 * @return
	 */
	public double getMinScaleDenominator();
	/** Maximum scale for the display of the layer.<p>
	 * 
	 * The scale denominator is defined with respect to a "standardized rendering pixel size"
	 * of 0.28 mm × 0.28 mm (millimeters). The definition is the same used in WMS 1.3.0 
	 * [OGC 06-042] and in Symbology Encoding Implementation Specification 1.1.0 [05-077r4]. 
	 * Frequently, the true pixel size is unknown and 0.28 mm is a common actual size for 
	 * current displays.
	 * 
	 * @return
	 */
	public double getMaxScaleDenominator();
	/** Metadata about the resource itself 
	 * 
	 * @return
	 */
	public ContextURI getMetadata();
	/** Definition of the folder structure in which the resource is placed. 
	 * 
	 * @return
	 */
	public Folder getFolder();
	/** Any encoding should allow the user to extend the resource content to include custom items 
	 * This is application specific
	 * @return
	 */
	Map<String, String> getExtensions();


}
