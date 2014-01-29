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
package com.augtech.geoapi.geopackage;

/** A GpkgField provides storage and interaction with a single field defined
 * within the GeoPackage. This super class is predominently aimed at system
 * tables, such as gpkg_contents where information is defined from the GeoPackage
 * specification. This class should be extended for specific purposes or tables,
 * such as Feature or Tile tables.
 *
 */
public class GpkgField implements Cloneable {
	/** The internal field name */
	protected String fieldName = "";
	/** The SQLite data type */
	protected String fieldType = "";
	/** Field creation options */
	protected String fieldOptions = "";
	/** Any notes associated with the field */
	protected String note = "";
	/** Database default value */
	protected String defaultValue = "";
	/** A value associated with this field */
	protected Object value = null;
	/** Is this field the table primary key? */
	protected boolean primaryKey = false;
	/**
	 * 
	 * @param fieldName
	 * @param fieldType
	 */
	public GpkgField(String fieldName, String fieldType) {
		this(fieldName, fieldType, null, null);
	}
	/**
	 * 
	 * @param fieldName
	 * @param fieldType
	 * @param options
	 */
	public GpkgField(String fieldName, String fieldType, String options) {
		this(fieldName, fieldType, options, null);
	}
	/**
	 * 
	 * @param fieldName
	 * @param fieldType
	 * @param options
	 * @param defaultValue
	 */
	public GpkgField(String fieldName, String fieldType, String options, String defaultValue) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.fieldOptions = options==null ? "" : options;
		this.defaultValue = defaultValue==null ? "" : defaultValue;
	}
	/** Add a descriptive note to the field.<br>
	 * This note is not used in the operation of the database, but may provide
	 * useful information such as specification number or use.
	 * 
	 * @param note
	 */
	public void addNote(String note) {
		this.note = note;
	}
	/** Is this field the primary key?
	 * 
	 * @return
	 */
	public boolean isPrimaryKey() {
		return this.primaryKey;
	}
	/** Set a value on this field definition. Does not set in the database.
	 * 
	 * @param value
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	/**
	 * 
	 * @return
	 */
	public Object getValue() {
		return this.value;
	}
	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}
	/**
	 * @return the fieldType
	 */
	public String getFieldType() {
		return fieldType;
	}
	/**
	 * @return the fieldOptions
	 */
	public String getFieldOptions() {
		return fieldOptions;
	}
	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}
	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	
	public GpkgField clone() {
		try {
			return (GpkgField) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result
				+ ((fieldOptions == null) ? 0 : fieldOptions.hashCode());
		result = prime * result
				+ ((fieldType == null) ? 0 : fieldType.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GpkgField))
			return false;
		GpkgField other = (GpkgField) obj;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (fieldOptions == null) {
			if (other.fieldOptions != null)
				return false;
		} else if (!fieldOptions.equals(other.fieldOptions))
			return false;
		if (fieldType == null) {
			if (other.fieldType != null)
				return false;
		} else if (!fieldType.equals(other.fieldType))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("GpkgField [fieldName=%s, fieldType=%s]",
				fieldName, fieldType);
	}
	
}
