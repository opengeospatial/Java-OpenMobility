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
/** A ContextValue is a value from the context document that
 * can have a specific encoding, compared to just String, and 
 * could be another primitive type, such as Double or Integer.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public interface ContextValue {

	/** Get the String value of the object.
	 * This method should never return <code>Null</code>
	 * 
	 * @return The string value of the object or ''
	 */
	public String getString();
	/** Get the encoding - Text, HTML or XHTML
	 * This method should never return <code>Null</code>
	 * 
	 * @return 
	 */
	public String getEncoding();
	/** Get the java class (i.e. String.class, Integer.class etc)
	 * This method should never return <code>Null</code>
	 * 
	 * @return
	 */
	public String getType();
}
