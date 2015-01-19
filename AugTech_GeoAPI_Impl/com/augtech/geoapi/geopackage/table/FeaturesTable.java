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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;

import com.augtech.geoapi.feature.NameImpl;
import com.augtech.geoapi.feature.type.AttributeTypeImpl;
import com.augtech.geoapi.feature.type.GeometryDescriptorImpl;
import com.augtech.geoapi.feature.type.GeometryTypeImpl;
import com.augtech.geoapi.feature.type.SimpleFeatureTypeImpl;
import com.augtech.geoapi.geopackage.DateUtil;
import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;
import com.augtech.geoapi.geopackage.GpkgRecords;
import com.augtech.geoapi.geopackage.GpkgTable;
import com.augtech.geoapi.geopackage.ICursor;
import com.augtech.geoapi.geopackage.ISQLDatabase;
import com.augtech.geoapi.geopackage.geometry.StandardGeometryDecoder;
import com.augtech.geoapi.geopackage.table.GpkgDataColumnConstraint.DataColumnConstraint;
import com.augtech.geoapi.geopackage.table.GpkgExtensions.Extension;
import com.augtech.geoapi.referncing.CoordinateReferenceSystemImpl;
import com.vividsolutions.jts.geom.Geometry;

/** An extension to the standard {@link GpkgTable} that provides specific functionality
 * relating to a vector feature table within the GeoPackage as well as an enclosed
 * class for {@link GeometryInfo} that relates to this table.
 * 
 *
 */
public class FeaturesTable extends GpkgTable {
	
	/** The feature ID to use in the database and on this table */
	String featureFieldName = GeoPackage.FEATURE_ID_FIELD_NAME;
	GeoPackage geoPackage = null;
	GeometryInfo geometryInfo = null;
	
	/** Contrcut a new FeaturesTable.<p>
	 * Note that the table will neither be created, or populated from, the GeoPackage
	 * until one of the relevant methods are called, such as {@link #create(SimpleFeatureType, BoundingBox)}.<p>
	 *  The tables feature id field (if required) will be set to 'feature_id'.
	 * 
	 * @param geoPackage The GeoPackage this table relates to
	 * @param tableName The name of the table. Any spaces will be replaced by '_'
	 * @see {@link #create(SimpleFeatureType, BoundingBox, GeometryDescriptor)}
	 * @see {@link #getBounds()} or {@link #getFields()} or {@link #getGeometryInfo()} to populate from the
	 * GeoPackage
	 */
	public FeaturesTable(GeoPackage geoPackage, String tableName) {
		this(geoPackage, tableName, GeoPackage.FEATURE_ID_FIELD_NAME);
	}
	/** Contrcut a new FeaturesTable.<p>
	 * Note that the table will neither be created, or populated from, the GeoPackage
	 * until one of the relevant methods are called, such as {@link #create(SimpleFeatureType, BoundingBox)}.<p>
	 * 
	 * @param geoPackage The GeoPackage this table relates to
	 * @param tableName The name of the table. Any spaces will be replaced by '_'
	 * @param featureFieldID The name of the field to use as a featureID when constructing
	 * {@link SimpleFeature}'s
	 * @see {@link #create(SimpleFeatureType, BoundingBox, GeometryDescriptor)}
	 * @see {@link #getBounds()} or {@link #getFields()} or {@link #getGeometryInfo()} to populate from the
	 * GeoPackage
	 */
	public FeaturesTable(GeoPackage geoPackage, String tableName, String featureFieldID) {
		super(tableName, null, null);
		super.tableType = GpkgTable.TABLE_TYPE_FEATURES;
		this.geoPackage = geoPackage;
		this.featureFieldName = featureFieldID;
	}


	/** Create a 'Features' table to hold vector features and insert the table details into 
	 * gpkg_contents. The SRS is taken from the {@link BoundingBox}.<p>
	 * The geometry that the user inserts for each feature must match the supplied SRS.<p>
	 * The last last_change field will be set to the time of creation.<p>
	 * Attribute information for into the gpkg_data_columns table
	 * 'full_name', 'mime_type' and 'constraint' are taken from {@link AttributeType#getUserData()} 
	 * to populate their respective fields in the gpkg_data_columns table. Remaining values are taken from
	 * {@link AttributeType#getName()} and {@link AttributeType#getDescription()}. Note that constraints 
	 * must be added to the database via {@link #addDataColumnConstraint(DataColumnConstraint)} prior
	 * to specifying and passing them through this method.<p>
	 * Table field data-types are determined from {@link AttributeType#getBinding()}
	 * 
	 * @param featureType The {@link SimpleFeatureType} that defines the table and its contents<br>
	 * The name of the table is taken from the local part of {@link SimpleFeatureType#getName()} with 
	 * spaces replaced by '_'.<br>
	 * Feature table description is taken from a user-data value name of 'Description'.
	 * @param bbox The {@link BoundingBox} ) provides an informative bounding box 
	 * (not necessarily minimum bounding box) of the content.
	 * 
	 * @return Returns {@code True} if the table(s) are created or the table already exists in
	 * the GeoPackage. False if any of the inserts fail.
	 * @throws Exception If the supplied data is invalid or constraints are not met (i.e No matching SRS 
	 * definition in the gpkg_spatial_ref_sys table)
	 */
	public boolean create(SimpleFeatureType featureType, BoundingBox bbox) throws Exception {
		
		if (isTableInGpkg(geoPackage)) {
			geoPackage.log.log(Level.INFO, "Table "+tableName+" already defined in "+GpkgContents.TABLE_NAME);
			return true;
		}
		
		// Doesn't exist in Contents, but does in DB, therefore not valid and drop
		if (isTableInDB(geoPackage)) {
			geoPackage.log.log(Level.WARNING, "Replacing table "+tableName);
			geoPackage.getDatabase().execSQL("DROP table ["+tableName+"]");
		}
		
		// Get and test Geometry type is valid
		GeometryDescriptor geomDescriptor = featureType.getGeometryDescriptor();
		
		if (!geoPackage.isGeomTypeValid(geomDescriptor) ) {
			String err = String.format("Invalid geometry type for table %s : %s", 
							tableName, 
							geomDescriptor.getType().getName().getLocalPart().toLowerCase() );
			throw new Exception(err);
		}
		
		String raw = null;
		
		// Check SRS exists in gpkg_spatial_ref_sys table
		String code = geomDescriptor.getCoordinateReferenceSystem().getName().getCode();
		int srsID = -1;
		try {
			srsID = Integer.valueOf(code);
		} catch (NumberFormatException ignore) {
			// Try from the bounding box
			code = bbox.getCoordinateReferenceSystem().getName().getCode();
			try {
				srsID = Integer.valueOf(code);
			} catch (NumberFormatException ignore2) {}

		}
		if (!geoPackage.isSRSLoaded( code ))
			throw new Exception("SRS "+code+" does not exist in the gpkg_spatial_ref_sys table");
		
		/* Checks passed, build queries for insertion...*/
		
		/* TODO Replace all system table field names with something more generic, or 
		 * constants on class?... */
		
		// Construct 'fields' text
		String geomName = geomDescriptor.getLocalName();
		List<String> dataColumnDefs = new ArrayList<String>();
		StringBuilder fields = new StringBuilder();
		
		/* Always add a feature_id column to table and gpkg_data_columns
		 * to enable WFS (and similar) IDs to be re-created when reading back 
		 * out of the table */
		fields.append(", [").append(featureFieldName).append("] TEXT");
		dataColumnDefs.add(
				"INSERT INTO gpkg_data_columns (table_name, column_name, name, title) "+
				" VALUES ('"+tableName+"','"+featureFieldName+"','FeatureID', 'FeatureID');");
		
		/* We always add full descriptions into GpkgDataColumns for each
		 * attribute even though its optional */
		for (int i=0;i<featureType.getAttributeCount();i++) {
			
			AttributeType aType = featureType.getType(i);
			Name atName = aType.getName();
			
			// Don't add Geometry to table def, but do add it into gpkg_data_columns
			if (atName.equals(geomDescriptor.getName()) || aType instanceof GeometryType) {
				dataColumnDefs.add(String.format(
						"INSERT INTO gpkg_data_columns (table_name, column_name, name, title) "+
						" VALUES ('%s','%s','%s','Feature Geometry')",
						tableName,
						geomName,
						geomName
						) );
				
				continue;
			}
			
			// The insertion text
			String fName = atName.getLocalPart();
			fName = fName.toLowerCase().equals("id") ? "__id" : fName;
			fields.append(", [").append( fName ).append("] ").append(geoPackage.encodeType( aType.getBinding()) );
			
			// Data columns definitions...
			//table_name, column_name, name, title, description, mime_type, constraint_name
			String mime = (String) aType.getUserData().get("mime_type");
			String constraint = (String) aType.getUserData().get("constraint_name");

			dataColumnDefs.add(String.format(
					"INSERT INTO gpkg_data_columns (table_name, column_name, name, title, description, mime_type, constraint_name) "+
					" VALUES ('%s','%s','%s','%s','%s',%s,%s)",
					tableName,
					aType.getName().getLocalPart(),
					aType.getName().toString(),
					aType.getUserData().get("full_name"),
					aType.getDescription()!=null ? aType.getDescription().toString() : null, 
					mime!=null ? "'"+mime+"'" : null,
					constraint!=null ? "'"+constraint+"'" : null
					) );
		}
		
		if (geomName.equals(""))
			throw new Exception("Unable to decode geometry attribute.");
		
		Object description = featureType.getUserData().get("Description");
		description = description==null ? "" : description.toString();
		
		// Create table
		String tableDef = String.format(
				"CREATE TABLE [%s] (id INTEGER PRIMARY KEY AUTOINCREMENT, [%s] GEOMETRY %s);",
				tableName, geomDescriptor.getLocalName(), fields.toString() );

		// Geometry columns
		raw = 	"INSERT INTO gpkg_geometry_columns (table_name, column_name, geometry_type_name, srs_id, z, m) "+
				" VALUES ('%s','%s','%s',%s,%s,%s);";
		String geomDef = String.format(raw, 
				tableName, 
				geomDescriptor.getLocalName(), 
				geomDescriptor.getType().getName().getLocalPart().toLowerCase(), 
				srsID, 
				GeoPackage.Z_M_VALUES_OPTIONAL, 
				GeoPackage.Z_M_VALUES_OPTIONAL);
		
		// If checks past, insert definition to gpkg_contents
		raw = "INSERT INTO gpkg_contents (table_name,data_type,identifier,description,last_change,"+
		"min_x,min_y,max_x,max_y,srs_id) VALUES ('%s','%s','%s','%s','%s',%s,%s,%s,%s,%s);";
		String contentsDef = String.format(raw, 
				tableName,
				GpkgTable.TABLE_TYPE_FEATURES,
				tableName,
				description,
				DateUtil.serializeDateTime(System.currentTimeMillis(), true),
				bbox.getMinX(),
				bbox.getMinY(),
				bbox.getMaxX(),
				bbox.getMaxY(),
				srsID );

		/* Create spatial index? 
		 * TODO: This is un-tested and the functions in the trigger definitions are not yet
		 * implemented */
		boolean doSpatialIndex = GeoPackage.CREATE_RTREE_FOR_FEATURES && geoPackage.getDatabase().hasRTreeEnabled();
		String[] rTreeDefs = new String[2 + GpkgTriggers.SPATIAL_TRIGGERS.length];
		if (doSpatialIndex) {
			String column = geomDescriptor.getLocalName();
			
			rTreeDefs[0] = String.format("CREATE VIRTUAL TABLE rtree_%s_%s USING "+
					"rtree(id, minx, maxx, miny, maxy)", tableName, geomDescriptor.getLocalName() );
			rTreeDefs[1] = String.format("INSERT INTO %s (table_name, column_name, extension_name, definition, scope) VALUES "+
					"('%s', '%s', 'gpkg_rtree_index', 'GeoPackage 1.0 Specification Annex M', 'write-only');",
					GpkgExtensions.TABLE_NAME, tableName, column);
			
			// For each defined trigger
			for (int i=0; i < GpkgTriggers.SPATIAL_TRIGGERS.length; i++) {
				rTreeDefs[i + 2] = MessageFormat.format(GpkgTriggers.SPATIAL_TRIGGERS[i], tableName, column, "id");
			}

		}
		
		// Execute the commands as a single transaction to allow for rollback...
		int offset = 4;
		String[] statements = new String[dataColumnDefs.size() + offset];
		statements[0] = tableDef;
		statements[1] = geomDef;
		statements[2] = contentsDef;
		statements[3] = String.format("CREATE UNIQUE INDEX '%s_fid' ON [%s] ([%s] ASC);",
								tableName, tableName, featureFieldName);
		for (int s=offset; s<dataColumnDefs.size()+offset; s++) {
			statements[s] = dataColumnDefs.get(s-offset);
		}
		if (doSpatialIndex) {
			// Execute together, or separate??
			String[] tmp = new String[statements.length+rTreeDefs.length];
			System.arraycopy(statements, 0, tmp, 0, statements.length);
			System.arraycopy(rTreeDefs, 0, tmp, statements.length, rTreeDefs.length);
			statements =  tmp;
		}
		
		
		boolean success = geoPackage.getDatabase().execSQLWithRollback(statements);
		
		// Get the information back from DB
		getContents();
		
		return success;
	}
	/** Get a constructed SimpleFeatureType based on all the available
	 * details for this table.
	 * 
	 * @return
	 * @throws Exception 
	 */
	public SimpleFeatureType getSchema() throws Exception {

		/* Build the geometry descriptor for this SimpleFeatureType. Internally we're going to use
		 * Proj.4 for any projection/ transformation, therefore not (currently) concerned with the
		 * definition supplied in the GeoPackage */
		GeometryInfo geomInfo = getGeometryInfo();
		GeometryType gType = new GeometryTypeImpl(
				new NameImpl(geomInfo.getGeometryTypeName()),
				Geometry.class, //TODO: This should really decode to point, line, polygon for shape writing to work?
				new CoordinateReferenceSystemImpl(""+geomInfo.getSrsID()) );
		GeometryDescriptor gDescr = new GeometryDescriptorImpl( gType, new NameImpl( geomInfo.getColumnName() ) );
		
		
		// Get the field information to build the Attribute Types with
		ArrayList<AttributeType> attrTypes = new ArrayList<AttributeType>();
		FeatureField ff = null;
		Class<?> binding = null;
		AttributeTypeImpl attType = null;
		String desc = "";
		String featureFieldName = null;
		
		for (GpkgField gf : getFields() ) {
			
			ff = (FeatureField)gf;

			/* If this is the feature id (and text) we'll add as a FeatureID, 
			 * not as an attribute later */
			if (ff.isFeatureID()) {
				
				featureFieldName = ff.getFieldName();
				continue;
				
			} else {
				
				binding = ff.getFieldName().equals(geomInfo.getColumnName()) ? Geometry.class : geoPackage.decodeType( ff.getFieldType() );
				desc = ff.getTitle()==null || ff.getTitle().equals("") ? ff.getDescription() : ff.getTitle();
				
				attType = new AttributeTypeImpl( new NameImpl(ff.getFieldName()), binding );
				attType.setDescription(	desc );
				attrTypes.add(attType);
				
			}
			
		}
		attrTypes.trimToSize();
		
		SimpleFeatureTypeImpl featureType = new SimpleFeatureTypeImpl( 
				new NameImpl( getTableName() ), 
				attrTypes,
				gDescr);
		featureType.setDescription(getDescription());
		
		return featureType;
	}
	/** Get a list of {@link SimpleFeature} from the GeoPackage by specifying a where clause
	 * (for example {@code featureId='pipe.1234'} or {@code id=1234} ).
	 * This method calls the {@link GeoPackage#getFeatures(String, String, com.augtech.geoapi.geopackage.geometry.GeometryDecoder)}
	 * method with a {@link StandardGeometryDecoder}.
	 * 
	 * @param strWhere The where clause.
	 * @return A List of {@link SimpleFeature}'s matching the where clause.
	 */ 
	public List<SimpleFeature> getFeatures(String strWhere) throws Exception {
		return geoPackage.getFeatures(this.tableName, strWhere, new StandardGeometryDecoder());
	}
	/** Issue a raw query on this table using a where clause
	 * 
	 * @param strWhere The where clause excluding the 'where'
	 * @return
	 * @throws Exception
	 */
	public GpkgRecords query(String strWhere) throws Exception {
		getContents();
		return super.query(geoPackage, strWhere);
	}

	@Override
	public GpkgField getField(String fieldName) {
		getContents();
		return super.getField(fieldName);
	}
	/** Get extended information for this FeatureTable
	 * 
	 * @throws Exception
	 */
	private void getContents() {
		// Probably already been built
		if (super.getFields().size()>0 && geometryInfo!=null) return;
		
		// Standard info from gpkg_contents and table definition
		try {
			super.getContents(geoPackage);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		/* Get extended information about each field for this table from GpkgDataColumns.
		 * There may or may not be column definitions in GpkgDataColumns */
		Map<String, FeatureField> dataColumns = new HashMap<String, FeatureField>();
		ICursor dcCursor = geoPackage.getSystemTable(GpkgDataColumns.TABLE_NAME)
				.query(geoPackage, null, "table_name='"+tableName+"'");
		
		if (dcCursor!=null) {
			while (dcCursor.moveToNext()) {
				
				String fName = dcCursor.getString(dcCursor.getColumnIndex("column_name"));
				FeatureField fField = new FeatureField(fName, "TEXT");
				
				fField.featureID = fName.equals(GeoPackage.FEATURE_ID_FIELD_NAME);
				fField.name = dcCursor.getString(dcCursor.getColumnIndex("name"));
				fField.title = dcCursor.getString(dcCursor.getColumnIndex("title"));
				fField.description = dcCursor.getString(dcCursor.getColumnIndex("description"));
				fField.mimeType = dcCursor.getString(dcCursor.getColumnIndex("mime_type"));
				String conName =  dcCursor.getString(dcCursor.getColumnIndex("constraint_name"));
				fField.constraint = new GpkgDataColumnConstraint().getConstraint(geoPackage, conName);
				
				dataColumns.put(fName, fField);
			}
			dcCursor.close();
		}
		
		
		/* Go through the table fields ( from getContents() ) and update with the additional info
		 * from GpkgDataColumns */
		for (GpkgField gf : super.getFields()) {
			
			FeatureField fField = dataColumns.get( gf.getFieldName() );
			if (fField==null) continue;
			
			// Already defined, so copy updating the field data-type from the table_info
			fField = new FeatureField(fField, gf.getFieldType() );
			
			// Update the base field with the new extended one.
			super.addField(fField);
			
		}
		
		// Get the geometry info on to this table
		try {
			getGeometryInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	/** Is the passed feature in this table?
	 * The query is based on {@link SimpleFeature#getID()} = featureFieldName.
	 * 
	 * @param simpleFeature The feature to check
	 * @return True if in the table
	 */
	public boolean isFeatureInTable(SimpleFeature simpleFeature) {
		ICursor ic = this.query(geoPackage, 
				new String[]{"id",featureFieldName}, featureFieldName+"='"+simpleFeature.getID()+"';");
		boolean inDb = ic.moveToFirst();
		ic.close();
		
		return inDb;
	}

	/** Get all geometry information for this table
	 * 
	 * @return
	 * @see #getBounds() for GpkgContents defined bounding box.
	 * @throws Exception
	 */
	public GeometryInfo getGeometryInfo() throws Exception {
		if (geometryInfo!=null) return geometryInfo;
		
		geometryInfo = new GeometryInfo();
		
		// Geometry column details
		GpkgRecords gRecord = geoPackage.getSystemTable(GpkgGeometryColumns.TABLE_NAME)
				.query(geoPackage, "table_name='"+tableName+"';");
		
		if (gRecord==null)
			throw new Exception("No geometry field definition for "+tableName);
		
		geometryInfo.columnName = gRecord.getFieldString(0, "column_name");
		geometryInfo.geometryTypeName = gRecord.getFieldString(0, "geometry_type_name");
		geometryInfo.srsID = gRecord.getFieldInt(0, "srs_id");
		int z = gRecord.getFieldInt(0, "z");
		if (z!=-1) geometryInfo.z = z;
		int m = gRecord.getFieldInt(0, "m");
		if (m!=-1) geometryInfo.m = m;
		
		// Check and get the SRID is defined in GeoPackage
		GpkgRecords sRecord = geoPackage.getSystemTable(GpkgSpatialRefSys.TABLE_NAME)
				.query(geoPackage, "srs_id="+geometryInfo.srsID);
		if (sRecord==null || sRecord.get(0)==null)
			throw new Exception("SRS "+geometryInfo.srsID+" not defined in GeoPackage");
		
		geometryInfo.organization = sRecord.getFieldString(0, "organization");
		geometryInfo.definition = sRecord.getFieldString(0, "definition");
		
		// Check extensions for spatial index
		List<Extension> ext = getExtensionInfo(geoPackage);
		if (ext!=null) {
			for (Extension e : ext) {
				if (	e.columnName.equals(geometryInfo.columnName) && 
						e.extensionName.equals("gpkg_rtree_index")) {
					geometryInfo.spatialIndex = true;
					break;
				}
			}
		}
		
		return geometryInfo;
	}

	@Override
	public Collection<GpkgField> getFields() {
		getContents();
		return super.getFields();
	}
	/**
	 * @return the BoundingBox from GpkgContents
	 */
	@Override
	public BoundingBox getBounds() {
		getContents();
		return super.getBounds();
	}
	/** Get the name of the Feature ID field.
	 * If one doesn't exist the name of the primary ket is returned.
	 * 
	 * @return
	 */
	public String getFeatureIDField() {
		getContents();
		String featureFieldName = "id";
		for (GpkgField gf : this.getFields() ) {
			if ( ((FeatureField)gf).isFeatureID() ) {
				featureFieldName = gf.getFieldName();
				break;
			}
		}
		return featureFieldName;
	}
	/**
	 * @return the lastChange
	 */
	@Override
	public Date getLastChange() {
		getContents();
		return super.getLastChange();
	}

	/** A class for storing FeaturesTable geometry information
	 * 
	 *
	 */
	public class GeometryInfo {
		protected String columnName = "";
		protected String geometryTypeName = "";
		protected String organization = "EPSG";
		protected String definition = "";
		protected int srsID = -1;
		protected int z = GeoPackage.Z_M_VALUES_OPTIONAL;
		protected int m = GeoPackage.Z_M_VALUES_OPTIONAL;
		protected boolean spatialIndex = false;
		
		public GeometryInfo() {
		}

		/** Get the feature table's geometry column name
		 * 
		 * @return the columnName
		 */
		public String getColumnName() {
			return columnName;
		}

		/** Get the type of geometry from Table 42 or Table 43 in Annex E 
		 * of the specification.
		 * 
		 * @return the geometryTypeName
		 */
		public String getGeometryTypeName() {
			return geometryTypeName;
		}

		/** Get the CRS organization name.
		 * 
		 * @return the organization
		 */
		public String getOrganization() {
			return organization;
		}

		/** Get the WKT representation of the projection definition
		 * 
		 * @return the definition
		 */
		public String getDefinition() {
			return definition;
		}

		/**
		 * @return the srsID
		 */
		public int getSrsID() {
			return srsID;
		}

		/** Get whether the Height dimension is prohibited, mandatory or optional.
		 * 
		 * @return the z
		 */
		public int getZOption() {
			return z;
		}

		/** Get whether the Measure dimension is prohibited, mandatory or optional.
		 * 
		 * @return the m
		 */
		public int getMOption() {
			return m;
		}

		/** Does this Geometry have an R*Tree spatial index?
		 * Note this doesn't necessarily mean that the index can be used
		 * 
		 * @return the spatialIndex
		 * @see {@link ISQLDatabase#hasRTreeEnabled()}
		 */
		public boolean hasSpatialIndex() {
			return spatialIndex;
		}
		
		
	}
	
	
	//public static final String CREATE_TABLE_GPKG_DATA_COLUMN_CONSTRAINTS = "CREATE TABLE gpkg_data_column_constraints ( "+
//	"constraint_name TEXT NOT NULL, "+
//	"constraint_type TEXT NOT NULL, "+ /* 'range' | 'enum' | 'glob' */
//	"value TEXT, "+
//	"min NUMERIC, "+
//	"minIsInclusive BOOLEAN, "+ /* 0 = false, 1 = true */
//	"max NUMERIC, "+
//	"maxIsInclusive BOOLEAN, "+  /* 0 = false, 1 = true */
//	"Description TEXT, "+
//	"CONSTRAINT gdcc_ntv UNIQUE (constraint_name, constraint_type, value) )";
	
}
