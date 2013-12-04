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
package com.augtech.geoapi.geopackage.table;

import com.augtech.geoapi.geopackage.GpkgField;
import com.augtech.geoapi.geopackage.table.GpkgDataColumnConstraint.DataColumnConstraint;
/** A FeatureField is an extension of a {@linkplain GpkgField} that adds
 * methods for getting and setting Feature Table specific information from the 
 * GpkgDataColumns table.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class FeatureField extends GpkgField {
	boolean featureID = false;
	String name = "";
	String title = "";
	String description = "";
	String mimeType = "";
	DataColumnConstraint constraint = null;
	
	/** Copy an existing FeatureField changing its field data-type
	 * 
	 * @param toCopy
	 * @param fieldType The new field data-type
	 */
	public FeatureField(FeatureField toCopy, String fieldType) {
		super(toCopy.getFieldName(), fieldType);

		name = toCopy.name;
		title = toCopy.title;
		description = toCopy.description;
		mimeType = toCopy.mimeType;
		constraint = toCopy.constraint;
		primaryKey = toCopy.primaryKey;
		featureID = toCopy.featureID;
	}
	/** Create a new FeatureField.
	 * 
	 * @param fieldName
	 * @param fieldType
	 */
	public FeatureField(String fieldName, String fieldType) {
		super(fieldName, fieldType);
	}

	/** Get the additional name for this field (not the column_name)
	 * 
	 * @return the defined name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}


	/**
	 * @return the constraint
	 */
	public DataColumnConstraint getConstraint() {
		return constraint;
	}


	/**
	 * @return the primaryKey
	 */
	public boolean isPrimaryKey() {
		return primaryKey;
	}


	/**
	 * @return the featureID
	 */
	public boolean isFeatureID() {
		return featureID;
	}

}
