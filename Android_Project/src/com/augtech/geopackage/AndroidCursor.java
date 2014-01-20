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

import android.database.Cursor;

import com.augtech.geoapi.geopackage.ICursor;

/** An implementation of the ICursor suitable for use with the standard
 * Android SQLite implementation(s).
 * 
 *
 */
public class AndroidCursor implements ICursor {
	Cursor cur = null;
	
	public AndroidCursor(Cursor cursor) {
		cur = cursor;
	}
	
	@Override
	public boolean moveToFirst() {
		return cur.moveToFirst();
	}

	@Override
	public boolean moveToNext() {
		return cur.moveToNext();
	}
	
	@Override
	public void close() {
		cur.close();
	}

	@Override
	public boolean moveToLast() {
		return cur.moveToLast();
	}

	@Override
	public String getString(int columnIndex) {
		return cur.getString(columnIndex);
	}

	@Override
	public int getInt(int columnIndex) {
		return cur.getInt(columnIndex);
	}

	@Override
	public int getColumnCount() {
		return cur.getColumnCount();
	}

	@Override
	public double getDouble(int columnIndex) {
		return cur.getDouble(columnIndex);
	}

	@Override
	public boolean getBoolean(int columnIndex) {
		return cur.getInt(columnIndex)==0 ? false : true;
	}

	@Override
	public byte[] getBlob(int columnIndex) {
		return cur.getBlob(columnIndex);
	}

	@Override
	public float getFloat(int columnIndex) {
		return cur.getFloat(columnIndex);
	}

	@Override
	public int getColumnIndex(String columnName) {
		return cur.getColumnIndex(columnName);
	}

	@Override
	public String getColumnName(int columnIndex) {
		return cur.getColumnName(columnIndex);
	}

	@Override
	public String[] getColumnNames() {
		return cur.getColumnNames();
	}

}
