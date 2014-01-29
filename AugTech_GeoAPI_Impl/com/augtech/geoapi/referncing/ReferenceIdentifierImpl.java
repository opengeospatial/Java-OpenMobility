/*
 * Copyright 2013, Augmented Technologies Ltd.
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
package com.augtech.geoapi.referncing;

import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.ReferenceIdentifier;

public class ReferenceIdentifierImpl implements ReferenceIdentifier {
	String code = "";
	String codeSpace = "";
	String version = "";
	Citation authority = null;
	String projDefinition = "";
	
	public ReferenceIdentifierImpl(String code, String codeSpace, String version) {
		this.code = code;
		codeSpace = codeSpace==null ? "EPSG" : codeSpace;
		this.codeSpace = codeSpace.equals("") ? "EPSG" : codeSpace;
		this.version = version;
	}
	
	/**
	 * @return the projDefinition
	 */
	public String getProjDefinition() {
		return projDefinition;
	}

	/**
	 * @param projDefinition the projDefinition to set
	 */
	public void setProjDefinition(String projDefinition) {
		this.projDefinition = projDefinition;
	}

	@Override
	public Citation getAuthority() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getCodeSpace() {
		return codeSpace;
	}

	@Override
	public String getVersion() {
		return version;
	}

}
