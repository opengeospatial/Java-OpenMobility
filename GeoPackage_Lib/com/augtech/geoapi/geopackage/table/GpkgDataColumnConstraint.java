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

import java.util.HashMap;
import java.util.Map;

import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;
import com.augtech.geoapi.geopackage.GpkgRecords;
import com.augtech.geoapi.geopackage.GpkgTable;

/** Contains data to specify restrictions on basic data type column values.
 * The constraint_name column is referenced by constraint_name column in the 
 * gpkg_data_columns table defined in clause 2.3.2.1.1
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class GpkgDataColumnConstraint extends GpkgTable {
	public static String TABLE_NAME = "gpkg_data_column_constraints";
	
	public static final String TYPE_RANGE = "range";
	public static final String TYPE_ENUM = "enum";
	public static final String TYPE_GLOB = "glob";
	
	/** The unique name of this constraint. Default is Null */
	//protected String constraintName = null;
	/** The type of constraint. Defaults to TYPE_ENUM */
	//protected String type = TYPE_ENUM;
	/** Specified case sensitive value for  {@link #TYPE_ENUM} or {@link #TYPE_GLOB} or NULL for {@link #TYPE_RANGE} */
	//protected String value = null;
	/** Minimum value for {@link #TYPE_RANGE} or NULL for {@link #TYPE_ENUM} or {@link #TYPE_GLOB} */
	//protected double min = 0;
	/** False if min value is exclusive, or True if min value is inclusive. Default is false */
	//protected boolean minIsInclusive = false;
	/** Maximum value for {@link #TYPE_RANGE} or NULL for {@link #TYPE_ENUM} or {@link #TYPE_GLOB} */
	//protected double max = 0;
	/** False if max value is exclusive, or True if max value is inclusive. Default is false */
	//protected boolean maxIsInclusive = false;
	/**
	 * 
	 */
	public GpkgDataColumnConstraint() {
		super(	TABLE_NAME, 
				new GpkgField[]{
					new GpkgField("constraint_name", "TEXT", "NOT NULL"),
					new GpkgField("constraint_type", "TEXT", "NOT NULL", "'"+TYPE_ENUM+"'"),
					new GpkgField("value", "TEXT"),
					new GpkgField("min", "NUMERIC"),
					new GpkgField("minIsInclusive", "BOOLEAN", null, null),
					new GpkgField("max", "NUMERIC"),
					new GpkgField("maxIsInclusive", "BOOLEAN", null, null),
					new GpkgField("description", "TEXT")
				},
				new String[]{"CONSTRAINT gdcc_ntv UNIQUE (constraint_name, constraint_type, value)"}
			);
		
	}
	/** Get a constructed DataColumnConstraint from the GeoPackage based on its name.
	 * 
	 * @param geoPackage
	 * @param name The Constraint name (i.e the constraint_name column entry)
	 * @return A new DataColumnConstraint or null if a matching name could not be found.
	 */
	public DataColumnConstraint getConstraint(GeoPackage geoPackage, String name) {
		
		if (name==null || name.equals("")) return null;
		
		GpkgRecords records = null;
		try {
			records = query(geoPackage, "constraint_name='"+name+"'");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (getFields().size()==0) return null;
		
		return new DataColumnConstraint( records );

	}
	/** A class that stores a single record from the gpkg_data_column_constraints
	 * table and provides convenience methods for testing values against the constraint
	 * and whether the constraint is valid in terms of inserting in to the GeoPackage.
	 *
	 */
	public class DataColumnConstraint {
		public String constraintName = "";
		public String constraintType = TYPE_ENUM;
		public String[] values = null;
		public float min = Float.NaN;
		public boolean minIsInclusive;
		public float max = Float.NaN;
		public boolean maxIsInclusive;
		public String[] descriptions = null;
		
		/** Build a new constraint from GpkgRecords. Only record 0 is used for type
		 * range, otherwise all records are used to build the values list.
		 * 
		 * @param fieldSet The Map<String, GpkgField> to build the constraint from
		 */
		public DataColumnConstraint(GpkgRecords records) {
			this.constraintName = records.getFieldString(0, "constraint_name");
			this.constraintType = records.getFieldString(0, "constraint_type");

			if (constraintType.equals(TYPE_RANGE)) {
				
				this.min = records.getFieldFloat(0, "min");
				this.max = records.getFieldFloat(0, "max");
				this.minIsInclusive = records.getFieldBool(0, "minIsInclusive");
				this.maxIsInclusive = records.getFieldBool(0, "maxIsInclusive");
				
			} else if (constraintType.equals(TYPE_GLOB)) {
				
				this.values = new String[]{records.getFieldString(0, "value")};
				
			} else {
				
				this.values = new String[records.size()];
				this.descriptions = new String[records.size()];
				for (int i=0;i<records.size(); i++) {
					this.values[i] = records.getFieldString(i, "value");
					this.descriptions[i] = records.getFieldString(i, "Description");
				}
				
			}
		}
		
		/** Return this constraint as a Map<String, Object> suitable for 
		 * inserting into the {@link GpkgDataColumnConstraint} table.
		 * 
		 * @return
		 */
		public Map<String, Object> toMap() {
			
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("constraint_name", this.constraintName);
			values.put("constraint_type", this.constraintType);
			values.put("values", this.values);
			values.put("min", constraintType.equals("TYPE_RANGE") ? this.min : null );
			values.put("minIsInclusive", this.minIsInclusive);
			values.put("max", constraintType.equals("TYPE_RANGE") ? this.max : null );
			values.put("maxIsInclusive", this.maxIsInclusive);
			values.put("descriptions", this.descriptions);
			
			return values;
		}
		/** Test whether the passed value is valid for this constraint.
		 * 
		 * @param value The attribte/ object value to test.
		 * 
		 * @return True if valid for insertion in to the GeoPackage
		 */
		public boolean isValueValid(Object value) {

			if (constraintType.equals(TYPE_ENUM)) {
				
				for (int i=0; i<values.length-1; i++) {
					if (value.toString().equals(values[i])) return true;
				}
				return false;
				
			} else if (constraintType.equals(TYPE_RANGE)) {
				
				double val = Double.parseDouble(value.toString());
				
				return (val > min || (minIsInclusive ? val==min : false)) &&
						(val < max || (maxIsInclusive ? val==max : false)); 
				
			} else if (constraintType.equals(TYPE_GLOB)) {
				
				return value.toString().matches(values[0]);
				
			}
			
			return false;
		}
		/** Check whether this constraint is valid for insertion to the GeoPackage<p>
		 * <li> constraintName and constraintType != <code>null</code>
		 * <li> Where constraintType = {@link #TYPE_ENUM} or {@link #TYPE_GLOB} or {@link #TYPE_RANGE}
		 * <li> For {@link #TYPE_ENUM} or {@link #TYPE_GLOB} value != <code>null</code> and min/ max != 
		 * <code>Float.NaN</code>
		 * <p>
		 * @return True if passed
		 */
		public boolean isValid() {
			
			boolean passed = constraintType!=null && constraintName!=null;
			
			if (constraintType.equals(TYPE_ENUM)) {
				
				return passed && values.length>0 && min==Float.NaN && max==Float.NaN;
				
			} else if (constraintType.equals(TYPE_GLOB)) {

				return passed && values!=null && min==Float.NaN && max==Float.NaN;
				
			} else if (constraintType.equals(TYPE_RANGE)) {

				return passed && values==null || values.length==0;
				
			} else {
				return false;
			}
			
			
		}
	}
}
//public static final String CREATE_TABLE_GPKG_DATA_COLUMN_CONSTRAINTS = "CREATE TABLE gpkg_data_column_constraints ( "+
//		"constraint_name TEXT NOT NULL, "+
//		"constraint_type TEXT NOT NULL, "+ /* 'range' | 'enum' | 'glob' */
//		"value TEXT, "+
//		"min NUMERIC, "+
//		"minIsInclusive BOOLEAN, "+ /* 0 = false, 1 = true */
//		"max NUMERIC, "+
//		"maxIsInclusive BOOLEAN, "+  /* 0 = false, 1 = true */
//		"Description TEXT, "+
//		"CONSTRAINT gdcc_ntv UNIQUE (constraint_name, constraint_type, value) )";
