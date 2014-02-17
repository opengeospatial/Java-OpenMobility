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
package com.augtech.geoapi.context;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.context.Author;
import org.opengis.context.Content;
import org.opengis.context.ContextURI;
import org.opengis.context.ContextValue;
import org.opengis.context.Folder;
import org.opengis.context.Offering;
import org.opengis.context.Operation;
import org.opengis.context.Resource;
import org.opengis.context.StyleSet;

import com.vividsolutions.jts.geom.Geometry;
/** A Resource (Entry) implementation for storing all Entry details
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class ResourceImpl implements Resource {
	Map<String, Object> resourceValues;
	List<Offering> offerings;
	List<ContextURI> keywords;
	Map<String, String> extensions = new HashMap<String, String>();
	
	/**
	 * 
	 * @param resourceValues
	 * @param offerings
	 * @param keywords
	 */
	public ResourceImpl(Map<String, Object> resourceValues, List<Offering> offerings,
			List<ContextURI> keywords, Map<String, String> extensions) {
		this.resourceValues = resourceValues;
		this.offerings = offerings;
		this.keywords = keywords;
		if (extensions!=null) this.extensions = extensions;
	}
	
	@Override
	public URI getId() {
		Object o = resourceValues.get("id");
		if (o instanceof String && o!=null) {
			try {
				return new URI(String.valueOf(o));
			} catch (URISyntaxException ignore) { }
		} else if (o instanceof URI) {
			return (URI)o; 
		}
		
		return null;

	}

	@Override
	public ContextValue getTitle() {
		return (ContextValue) resourceValues.get("title");
	}

	@Override
	public Content getAbstract() {
		return (Content) resourceValues.get("content");
	}

	@Override
	public String getUpdateDate() {
		return String.valueOf( resourceValues.get("updated") );
	}

	@Override
	public Author getAuthor() {
		return (Author) resourceValues.get(Author.TAG);
	}

	@Override
	public String getPublisher() {
		ContextValue cv = (ContextValue)resourceValues.get("publisher");
		return cv!=null ? cv.getString() : null;
	}

	@Override
	public ContextValue getRights() {
		return (ContextValue) resourceValues.get("rights");
	}

	@Override
	public Geometry getGeospatialExtent() {
		return (Geometry) resourceValues.get("where");
	}

	@Override
	public String getTemporalExtent() {
		return String.valueOf( resourceValues.get("date") );
	}

	@Override
	public ContextURI getContentDescription() {
		return (ContextURI) resourceValues.get("description");
	}

	@Override
	public ContextURI getPreview() {
		return (ContextURI) resourceValues.get("preview");
	}

	@Override
	public ContextURI getContentByRef() {
		return (ContextURI) resourceValues.get("contentByRef");
	}

	@Override
	public List<Offering> getOfferings() {
		return offerings;
	}

	@Override
	public boolean isActive() {
		return resourceValues.get("active")==null ? false : 
			Boolean.valueOf(String.valueOf(resourceValues.get("active")));
	}

	@Override
	public List<ContextURI> getKeywords() {
		return keywords;
	}

	@Override
	public double getMinScaleDenominator() {
		return resourceValues.get("minScaleDenominator")==null ? 0d : 
			(Double) resourceValues.get("minScaleDenominator");
	}

	@Override
	public double getMaxScaleDenominator() {
		return resourceValues.get("maxScaleDenominator")==null ? 0d : 
			(Double) resourceValues.get("maxScaleDenominator");
	}

	@Override
	public ContextURI getMetadata() {
		return (ContextURI) resourceValues.get("metaData");
	}

	@Override
	public Folder getFolder() {
		return (Folder) resourceValues.get(Folder.TAG);
	}

	@Override
	public Map<String, String> getExtensions() {
		return extensions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((keywords == null) ? 0 : keywords.hashCode());
		result = prime * result
				+ ((offerings == null) ? 0 : offerings.hashCode());
		result = prime * result
				+ ((resourceValues == null) ? 0 : resourceValues.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ResourceImpl))
			return false;
		ResourceImpl other = (ResourceImpl) obj;
		if (keywords == null) {
			if (other.keywords != null)
				return false;
		} else if (!keywords.equals(other.keywords))
			return false;
		if (offerings == null) {
			if (other.offerings != null)
				return false;
		} else if (!offerings.equals(other.offerings))
			return false;
		if (resourceValues == null) {
			if (other.resourceValues != null)
				return false;
		} else if (!resourceValues.equals(other.resourceValues))
			return false;
		return true;
	}
	/** Print the output of this resource to a StringBuffer.
	 * 
	 * @param sb
	 */
	public void print(StringBuffer sb) {
		sb.append("Resource Values:\n");
		sb.append("---------------\n");
		for (Map.Entry<String, Object> e : resourceValues.entrySet()) {
			sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
		}
		int i = 1;
		
		sb.append("\n");
		sb.append("Resource Offerings:\n");
		sb.append("------------------\n");
		for (Offering of : offerings) {
			int j = 1, k=1;
			sb.append(i++).append(": ").append(of.getCode()).append("\n");
			sb.append("\tOperations (").append(of.getOperationCount()).append("):\n");
			for (Operation o : of.getOperations() ) {
				sb.append("\t\t").append(j++).append(": ").append(o.toString()).append("\n");
			}
			sb.append("\n\tContents (").append(of.getContentsCount()).append("):\n");
			for (Content c : of.getContents() ) {
				sb.append("\t\t").append(k++).append(": ").append(c.toString()).append("\n");
			}
			StyleSet ss = of.getStyleset();
			if (ss!=null) {
				sb.append("\n\tStyleSet\n\t");
				sb.append(ss.toString());
			}
			sb.append("\n");
		}
		
		i = 1;
		sb.append("Resource Keywords:\n");
		sb.append("-----------------\n");
		for (ContextURI c : keywords) {
			sb.append("\t").append(i++).append(": ").append(c.toString()).append("\n");
		}
		sb.append("\n");
	}

	@Override
	public int getOfferingCount() {
		return offerings==null ? 0 : offerings.size();
	}

	@Override
	public int getKeywordCount() {
		return keywords==null ? 0 : keywords.size();
	}
}
