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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.context.Author;
import org.opengis.context.Context;
import org.opengis.context.ContextURI;
import org.opengis.context.ContextValue;
import org.opengis.context.Creator;
import org.opengis.context.CreatorApplication;
import org.opengis.context.CreatorDisplay;
import org.opengis.context.Resource;

import com.vividsolutions.jts.geom.Geometry;

/** An implementation of the {@link Context} document
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class ContextImpl implements Context {
	List<Resource> feedResources;
	Creator creator;
	List<ContextURI> keywords;
	Map<String, Object> feedValues;
	Map<String, String> extensions = new HashMap<String, String>();
	String charEncoding = "UTF-8";
	/**
	 * 
	 * @param charEncoding
	 * @param feedValues
	 * @param feedResources
	 * @param keywords
	 */
	public ContextImpl(String charEncoding, Map<String, Object> feedValues, 
			List<Resource> feedResources, List<ContextURI> keywords) {
		this(charEncoding, feedValues, feedResources, keywords, null);
	}
	/**
	 * 
	 * @param charEncoding
	 * @param feedValues
	 * @param feedResources
	 * @param keywords
	 */
	public ContextImpl(String charEncoding, Map<String, Object> feedValues, 
			List<Resource> feedResources, 
			List<ContextURI> keywords, Map<String, String> extensions) {
		this.feedValues = feedValues;
		this.feedResources = feedResources;
		this.keywords = keywords;
		creator = new CreatorImpl(
				(CreatorApplication)feedValues.get(CreatorApplication.TAG), 
				(CreatorDisplay)feedValues.get(CreatorDisplay.TAG));
		
		if(extensions!=null) this.extensions = extensions;
		if (charEncoding!=null) this.charEncoding = charEncoding;
	}
	public void setExtensions(Map<String, String> extensions) {
		this.extensions = extensions;
	}
	@Override
	public URI getSpecReference() {
		return (URI) feedValues.get("specReference");
	}

	@Override
	public String getLanguage() {
		return String.valueOf( feedValues.get("language") );
	}

	@Override
	public URI getId() {
		return (URI)feedValues.get("id");
	}

	@Override
	public ContextValue getTitle() {
		return (ContextValue)feedValues.get("title");
	}

	@Override
	public ContextValue getAbstract() {
		return (ContextValue)feedValues.get("subtitle");
	}

	@Override
	public String getUpdateDate() {
		return ((ContextValue)feedValues.get("updated")).getString();
	}

	@Override
	public Author getAuthor() {
		return (Author)feedValues.get(Author.TAG);
	}

	@Override
	public String getPublisher() {
		ContextValue cv = (ContextValue)feedValues.get("publisher");
		return cv!=null ? cv.getString() : null;
	}

	@Override
	public Creator getCcreator() {
		return creator;
	}

	@Override
	public ContextValue getRights() {
		return (ContextValue)feedValues.get("rights");
	}

	@Override
	public Geometry getAreaOfInterest() {
		return (Geometry)feedValues.get("where");
	}

	@Override
	public String getTimeIntervalOfInterest() {
		return String.valueOf( feedValues.get("date") );
	}

	@Override
	public List<ContextURI> getKeywords() {
		return keywords;
	}

	@Override
	public List<Resource> getResources() {
		return feedResources;
	}

	@Override
	public ContextURI getMetadata() {
		return (ContextURI) feedValues.get("metaData");
	}
	@Override
	public int getKeywordCount() {
		return keywords==null ? 0 : keywords.size();
	}

	@Override
	public int getResourceCount() {
		return feedResources==null ? 0 : feedResources.size();
	}
	
	@Override
	public Map<String, String> getExtensions() {
		return extensions;
	}

	/** Print the output of Context document to a StringBuffer.
	 * 
	 * @param sb
	 */
	public void print(StringBuffer sb) {
		sb.append("\nContext Values\n");
		sb.append("----------------\n");
		for (Map.Entry<String, Object> e : feedValues.entrySet()) {
			sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
		}
		
		int i=1;
		sb.append("\nContext Keywords (").append(getKeywordCount()).append(")\n");
		sb.append("----------------\n");
		for (ContextURI c : keywords) {
			sb.append("\t").append(i++).append(" ").append(c.toString()).append("\n");
		}
		
		sb.append("\n").append(feedResources.size()).append(" Resources...\n");
		
		for (Resource r : feedResources) {
			((ResourceImpl)r).print(sb);
		}


		
	}
	@Override
	public String toString() {
		return String.format("ContextImpl [Title=%s, id=%s]", 
				feedValues.get("title"), feedValues.get("id"));
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((feedResources == null) ? 0 : feedResources.hashCode());
		result = prime * result
				+ ((feedValues == null) ? 0 : feedValues.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ContextImpl))
			return false;
		ContextImpl other = (ContextImpl) obj;
		if (feedResources == null) {
			if (other.feedResources != null)
				return false;
		} else if (!feedResources.equals(other.feedResources))
			return false;
		if (feedValues == null) {
			if (other.feedValues != null)
				return false;
		} else if (!feedValues.equals(other.feedValues))
			return false;
		return true;
	}
	@Override
	public String getCharEncoding() {
		return charEncoding;
	}

	
}
