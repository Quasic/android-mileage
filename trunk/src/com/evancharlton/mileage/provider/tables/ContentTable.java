package com.evancharlton.mileage.provider.tables;

import java.util.HashMap;
import java.util.Set;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.evancharlton.mileage.dao.Dao;

public abstract class ContentTable {
	protected static String TABLE_NAME = "content_table";

	abstract public String getTableName();

	public static final HashMap<String, String> buildProjectionMap(String[] map) {
		HashMap<String, String> projection = new HashMap<String, String>();
		// just in case
		projection.put(Dao._ID, Dao._ID);
		for (String key : map) {
			projection.put(key, key);
		}
		return projection;
	}

	public String getDefaultSortOrder() {
		return Dao._ID + " desc";
	}

	abstract public void registerUris(UriMatcher uriMatcher);

	abstract public int delete(SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs);

	abstract public String getType(int type);

	abstract public long insert(int type, SQLiteDatabase db, ContentValues initialValues);

	abstract public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder);

	abstract public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs);

	abstract public String init();

	abstract public String create();

	abstract public String upgrade(final int currentVersion);

	protected final class TableBuilder {
		private StringBuilder mBuilder = new StringBuilder();

		public TableBuilder() {
			mBuilder.append("CREATE TABLE ").append(getTableName()).append(" (");
			mBuilder.append(Dao._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT");
		}

		public TableBuilder addDouble(String fieldName) {
			return addField(fieldName, "DOUBLE");
		}

		public TableBuilder addInteger(String fieldName) {
			return addField(fieldName, "INTEGER");
		}

		public TableBuilder addText(String fieldName) {
			return addField(fieldName, "TEXT");
		}

		private TableBuilder addField(String fieldName, String fieldType) {
			mBuilder.append(", ").append(fieldName).append(" ").append(fieldType);
			return this;
		}

		public String build() {
			mBuilder.append(");");
			return mBuilder.toString();
		}

		@Override
		public String toString() {
			return build();
		}
	}

	protected final class InsertBuilder {
		private StringBuilder mBuilder = new StringBuilder();
		private HashMap<String, String> mData = new HashMap<String, String>();

		public InsertBuilder() {
			mBuilder.append("INSERT INTO ").append(getTableName()).append(" (");
		}

		public InsertBuilder add(String field, String value) {
			mData.put(field, value);
			return this;
		}

		public InsertBuilder add(String field, long value) {
			return add(field, String.valueOf(value));
		}

		public String build() {
			Set<String> keySet = mData.keySet();
			final int length = keySet.size();
			String[] values = new String[length];

			int i = 0;
			for (String key : keySet) {
				values[i] = mData.get(key);
				mBuilder.append(key);
				if (i + 1 < length) {
					mBuilder.append(",");
				}
				i++;
			}
			mBuilder.append(") VALUES (");
			for (i = 0; i < length; i++) {
				mBuilder.append("'").append(values[i]).append("'");
				if (i + 1 < length) {
					mBuilder.append(",");
				}
			}

			mBuilder.append(");");
			return mBuilder.toString();
		}

		@Override
		public String toString() {
			return build();
		}
	}
}