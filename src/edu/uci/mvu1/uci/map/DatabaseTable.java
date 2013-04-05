package edu.uci.mvu1.uci.map;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.stream.JsonReader;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseTable {
	private static final String TAG = "MapDatabase";
	
	public static final String COL_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String COL_SEARCH_TERMS = SearchManager.SUGGEST_COLUMN_TEXT_2;
	public static final String COL_LATITUDE = "latitude";
	public static final String COL_LONGITUDE = "longitude";
	
	private static final String DATABASE_NAME = "UCIMap";
	private static final String FTS_VIRTUAL_TABLE = "FTS";
	private static final int DATABASE_VERSION = 8;
	
	private static final String FTS_TABLE_CREATE =
			"CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
			" USING fts3 (" +
			COL_TITLE + ", " +
			COL_SEARCH_TERMS + ", " +
			COL_LATITUDE + ", " +
			COL_LONGITUDE + ")";
	
	private final DatabaseOpenHelper mDatabaseOpenHelper;
	private static final HashMap<String, String> mColumnMap = buildColumnMap();
	
	public DatabaseTable(Context context) 
	{
		mDatabaseOpenHelper = new DatabaseOpenHelper(context);
	}
	
	// rowid and _id is confusing me, so I include this part
	private static HashMap<String, String> buildColumnMap() 
	{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(COL_TITLE, COL_TITLE);
		map.put(COL_SEARCH_TERMS, COL_SEARCH_TERMS);
		map.put(COL_LATITUDE, COL_LATITUDE);
		map.put(COL_LONGITUDE, COL_LONGITUDE);
		map.put(BaseColumns._ID, "rowid as " + BaseColumns._ID);
		map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
		map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid as " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
		
		return map;
	}
	
	public Cursor getLocationMatches(String query, String[] columns) 
	{
		String selection =  FTS_VIRTUAL_TABLE +  " MATCH ?";
		String[] selectionArgs = new String[] {"*" + query + "*"};
		
		return query(selection, selectionArgs, columns);
	}
	
	public Cursor getLocation(String rowId, String[] columns)
	{
		String selection = "rowid = ?";
		String[] selectionArgs = new String[] {rowId};
		
		return query(selection, selectionArgs, columns);
	}
	
	private Cursor query(String selection, String[] selectionArgs, String[] columns)
	{
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(FTS_VIRTUAL_TABLE);
		builder.setProjectionMap(mColumnMap);
		
		Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), columns, selection, selectionArgs, null, null, null);
		
		if (cursor == null)
		{
			return null;
		}
		else if (!cursor.moveToFirst())
		{
			cursor.close();
			return null;
		}
		return cursor;
	}
	
	private static class DatabaseOpenHelper extends SQLiteOpenHelper 
	{
		private final Context mHelperContext;
		private SQLiteDatabase mDatabase;
		
		public DatabaseOpenHelper(Context context) 
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mHelperContext = context;
			
		}
		
		public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version) 
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mHelperContext = context;
			
		}

		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			mDatabase = db;
			mDatabase.execSQL(FTS_TABLE_CREATE);
			load();
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
			onCreate(db);
			
		}
		
		private void load() {
			try {
				loadLocations();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		private void loadLocations() throws IOException {
			final Resources resources = mHelperContext.getResources();
			InputStream inputStream = resources.openRawResource(R.raw.locations);
			JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
			try {
				reader.beginArray();
				while(reader.hasNext())
				{
					reader.beginObject();
					String title = null;
					List<String> searchTerms = new ArrayList<String>();
					List<Double> geo = new ArrayList<Double>();
					
					while(reader.hasNext())
					{
						String name = reader.nextName();
						if (name.equals("title"))
						{
							title = reader.nextString();
						}
						else if (name.equals("searchterms"))
						{
							reader.beginArray();
							while (reader.hasNext()) 
							{
								searchTerms.add(reader.nextString());
							}
							reader.endArray();
						}
						else if (name.equals("geo"))
						{
							reader.beginArray();
							while (reader.hasNext()) 
							{
								geo.add(reader.nextDouble());
							}
							reader.endArray();
						}
						else
						{
							reader.skipValue();
						}
					}
					reader.endObject();
					
					ContentValues initialValues = new ContentValues();
					initialValues.put(COL_TITLE, title);
					String raw = "";
					for (String searchTerm : searchTerms)
					{
						raw += searchTerm + ", ";
					}
					if (raw.endsWith(", "))
						raw = raw.substring(0, raw.length()-2);
					initialValues.put(COL_SEARCH_TERMS, raw);
					initialValues.put(COL_LATITUDE, geo.get(0));
					initialValues.put(COL_LONGITUDE, geo.get(1));
					
					if (mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues) < 0) 
					{
						Log.e(TAG, "Unable to add location: " + title);
					}
					

				}
				reader.endArray();
			} finally {
				reader.close();
			}
			
		}
	}
	

}