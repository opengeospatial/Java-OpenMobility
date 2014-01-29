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

public interface Folder {
	/** The lcal name of this class in the XML document */
	public static final String TAG = "folder";
	/** A not empty character string that defines the folder name in which the resource is placed
	 * 
	 * @return The name of the folder or <code>Null</code> if none was specified
	 */
	public String getName();
	/** An optional human readable title for the folder
	 * 
	 * @return
	 */
	public String getTitle();
	
}
