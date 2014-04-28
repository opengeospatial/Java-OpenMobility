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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import org.opengis.context.ContextFilter;

/** A FilterInputStream that enables the buffering of in-line content
 * whilst the Context document is being parsed, enabling HTML, GML etc
 * to be stored and retrieved verbatim. This is our implementation
 * to create a Content Buffer as required by
 *  {@link ContextFilter#getContentBuffer(String)}
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class ContentBuffer extends FilterInputStream {
	StringBuffer buffer = new StringBuffer();
	
	/** Create a new instance of the InputStreamRecorder
	 * to store contents of the input stream as its represented,
	 * compared to being treated as XML tags.
	 * 
	 * @param inputStream
	 */
	public ContentBuffer(InputStream inputStream) {
		super(inputStream );
	}
	/** Does the content buffer have any content?
	 * 
	 * @return
	 */
	public boolean hasContent() {
		return buffer.length() > 0;
	}
	/** Get the total contents of the buffer
	 * 
	 * @return
	 */
	public String getBuffer() {
		return buffer.toString();
	}
	/** Get the contents of the buffer up to and including the 
	 * supplied closing tag name.<p>
	 * Once the string is retrieved the buffer is cleared up to the same
	 * point.
	 * 
	 * @param qName  The qualified tag name to retrieve <i>up to</i>
	 * 
	 * @return
	 */
	public String getBuffer(String qName) {

		/* Ensure we look for a closing tag */
		qName = "</"+qName;
		int idx = buffer.indexOf(qName);
		if (idx==-1 && qName.contains(":")) {
			String[] t = qName.split(":");
			if (t==null || t.length<2 ) return "";
			String loc = "</"+t[1];
			idx = buffer.indexOf(loc);
			idx+=loc.length()+1;
		} else {
			idx+=qName.length()+1;
		}

		if (idx < buffer.length()) {
			String s = buffer.substring(0, idx);
			buffer.delete(0, idx);
			return s;
		} else {
			return "";
		}
	}
	/** Clear the current buffer up to the passed
	 * tag name. The passed tag name will appear in the
	 * results of {@link #getBuffer()}
	 * 
	 * @param qName The qualified tag name to clear <i>up to</i>
	 */
	public void resetContentBuffer(String qName) {
		int l = buffer.indexOf("<"+qName)-1;
		if (l>0 && l<buffer.length())
			buffer.delete(0, l );
	}
	@Override
	public int read() throws IOException {
		int i = super.read();
		buffer.append(i);
		return i;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int i = super.read(b, off, len);
		byte[] t = new byte[len];
		System.arraycopy(b, off, t, 0, len);
		buffer.append(new String(t) );
		return i;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int i = super.read(b);
		buffer.append(new String(b) );
		return i;
	}
	
}
