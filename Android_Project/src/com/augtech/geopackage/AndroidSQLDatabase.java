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
package com.augtech.geopackage;

import java.io.File;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.augtech.geoapi.geopackage.ICursor;
import com.augtech.geoapi.geopackage.ISQLDatabase;
/** An Android specific implementation of {@link ISQLDatabase} to
 * interact with the standard Android SQLite database implementation.<p>
 * Other SQLite implementation should utilise similar classes for the actual
 * query, insert, update, delete and other database methods.
 * 
 *
 */
public class AndroidSQLDatabase extends SQLiteOpenHelper implements ISQLDatabase {
	SQLiteDatabase sqlDB = null;
	File thisFile = null;
	
	public AndroidSQLDatabase(Context context, File dbFile) {
		super(context, dbFile.toString(), null, 1);
		thisFile = dbFile;
	}

	public AndroidSQLDatabase(Context context, File dbFile, int dbVersion) {
		super(context, dbFile.toString(), null, dbVersion);
		thisFile = dbFile;
	}
	
	@Override
	public File getDatabaseFile() {
		return thisFile;
	}
	
	@Override
	public ISQLDatabase createDatabase() {
		this.getReadableDatabase();// This will automatically call the Android onCreate method
		return this;
	}

	@Override
	public ISQLDatabase getDatabase(boolean writeable) {
		if(writeable) {
			if (sqlDB==null) sqlDB = this.getWritableDatabase();
			if (sqlDB.isReadOnly() || !sqlDB.isOpen()) {
				sqlDB = this.getWritableDatabase();
			}
		} else {
			if (sqlDB == null) sqlDB = this.getReadableDatabase();
			if (!sqlDB.isOpen()) sqlDB = this.getReadableDatabase();
		}
		return this;
	}

	@Override
	public ICursor doRawQuery(String sql) {
		getDatabase(false);
		return new AndroidCursor( sqlDB.rawQuery(sql, null) );
	}
	
	
	@Override
	public ICursor doQuery(String table, String[] columns, String strWhere) {
		getDatabase(false);
    	if (strWhere !=null) {
	    	if (strWhere.contains("'")) strWhere.replace("'", "''");
    	}
    	return new AndroidCursor( sqlDB.query(table, columns, strWhere, null, null, null, null) );
    	
	}

	@Override
	public int doUpdate(String table, Map<String, Object> values, String strWhere) {
		getDatabase(true);
		
    	int ret = 0;
		ContentValues cv = new ContentValues();
		for (Map.Entry<String, Object> v : values.entrySet()) cv.put(v.getKey(), v.getValue().toString() );
    	if (strWhere !=null) { 
    		ret = sqlDB.update(table, cv, strWhere, null);
    	}
    	return ret;
	}

	@Override
	public int doDelete(String table, String strWhere) {
		getDatabase(true);
		
    	int ret = 0;
    	if (strWhere !=null) {
    		if (strWhere.equals("")) {
    			ret = sqlDB.delete(table, null, null);
    		} else {
    			ret = sqlDB.delete(table, strWhere, null);
    		}
    	} else {
    		ret = sqlDB.delete(table, null, null);
    	}
    	return ret;
	}
	/**
	 * 
	 * @param values
	 * @return
	 */
	private ContentValues getContentValues(Map<String, Object> values) {
		ContentValues cv = new ContentValues();
		
		for (Map.Entry<String, Object> v : values.entrySet()) {
			Object o = v.getValue();
			
			if (o instanceof String) {
				cv.put(v.getKey(), (String)o );
			} else if (o instanceof Integer) {
				cv.put(v.getKey(), (Integer)o );	
			} else if (o instanceof Boolean) {
				cv.put(v.getKey(), (Boolean)o );
			} else if (o instanceof Double) {
				cv.put(v.getKey(), (Double)o );	
			} else if (o instanceof byte[]) {
				cv.put(v.getKey(), (byte[])o );	
			} else if (o instanceof Byte) {
				cv.put(v.getKey(), (Byte)o );	
			} else if (o instanceof Float) {
				cv.put(v.getKey(), (Float)o );	
			} else if (o instanceof Long) {
				cv.put(v.getKey(), (Long)o );	
			} else if (o instanceof Short) {
				cv.put(v.getKey(), (Short)o );	
			} else {
				cv.put(v.getKey(), String.valueOf(o) );
			}
			
		}
		return cv;
	}
	@Override
	public long doInsert(String table, Map<String, Object> values) {
		getDatabase(true);

		return sqlDB.insert(table, null, getContentValues(values) );
	}
	@Override
	public long doInsert(String table, List<Map<String, Object>> values) {
		getDatabase(true);
		
		int inserted = 0;
		
		sqlDB.beginTransaction();
		for (Map<String, Object> m : values) {
			if (sqlDB.insert(table, null, getContentValues(m) )>-1) inserted++;
		}
		sqlDB.setTransactionSuccessful();
		sqlDB.endTransaction();

		return inserted;
	}
	@Override
	public void execSQL(String sql) {
		getDatabase(true);
		sqlDB.execSQL(sql);
	}
	
	@Override
	public boolean execSQLWithRollback(String[] statements) {
		getDatabase(true);
		try {
			sqlDB.beginTransaction();
			for (String sql : statements) {
				sqlDB.execSQL( sql );	
			}
			sqlDB.setTransactionSuccessful();
			sqlDB.endTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public boolean isOpen() {
		return sqlDB.isOpen();
	}

	@Override
	public void close() {
		sqlDB.close();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		sqlDB = db;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public boolean hasRTreeEnabled() {
		// Future version of Android may?
		return false;
	}


}
