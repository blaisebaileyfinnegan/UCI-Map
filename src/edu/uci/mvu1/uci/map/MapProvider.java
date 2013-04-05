package edu.uci.mvu1.uci.map;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class MapProvider extends ContentProvider {
	String TAG = "MapProvider";
	
	public static String AUTHORITY = "edu.uci.mvu1.uci.map.MapProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/database");
	
	public static final String SEARCH_TERMS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.mvu1.uci.map";
	public static final String LOCATION_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.mvu1.uci.map";
	
	private DatabaseTable mDatabase;
	
	private static final int SEARCH_LOCATIONS = 0;
	private static final int GET_LOCATION = 1;
	private static final int SEARCH_SUGGEST = 2;
	private static final int REFRESH_SHORTCUT = 3;
	
	private static final UriMatcher sURIMatcher = buildUriMatcher();
	
	private static UriMatcher buildUriMatcher()
	{
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		matcher.addURI(AUTHORITY, "database", SEARCH_LOCATIONS);
		matcher.addURI(AUTHORITY, "database/#", GET_LOCATION);
		
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
		
		return matcher;
	}
	

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri))
		{
			case SEARCH_LOCATIONS:
				return SEARCH_TERMS_MIME_TYPE;
			case GET_LOCATION:
				return LOCATION_MIME_TYPE;
			case SEARCH_SUGGEST:
				return SearchManager.SUGGEST_MIME_TYPE;
			case REFRESH_SHORTCUT:
				return SearchManager.SHORTCUT_MIME_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onCreate() {
		mDatabase = new DatabaseTable(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		switch (sURIMatcher.match(uri))
		{
			case SEARCH_SUGGEST:
				if (selectionArgs == null) {
					throw new IllegalArgumentException("You specified: " + uri.getLastPathSegment() + " selectionArgs must be provided for this URI: " + uri);
				}
				return getSuggestions(selectionArgs[0]);
			case SEARCH_LOCATIONS:
				if (selectionArgs == null) {
					throw new IllegalArgumentException("You specified: " + uri.getLastPathSegment() + " selectionArgs must be provided for this URI: " + uri);
				}
				return search(selectionArgs[0]);
			case GET_LOCATION:
				return getLocation(uri);
			case REFRESH_SHORTCUT:
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}
	
	private Cursor getSuggestions(String query)
	{
		query = query.toLowerCase();
		String[] columns = new String[] {
				BaseColumns._ID,
				DatabaseTable.COL_TITLE,
				DatabaseTable.COL_SEARCH_TERMS,
				SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID 
		};
		return mDatabase.getLocationMatches(query, columns);
	}
	
	private Cursor search(String query)
	{
		query = query.toLowerCase();
		String[] columns = new String[] {
				BaseColumns._ID,
				DatabaseTable.COL_TITLE,
				DatabaseTable.COL_SEARCH_TERMS,
		};
		
		return mDatabase.getLocationMatches(query, columns);
	}
	
	private Cursor getLocation(Uri uri)
	{
		String rowId = uri.getLastPathSegment();
		String[] columns = new String[] {
			DatabaseTable.COL_TITLE,
			DatabaseTable.COL_LATITUDE,
			DatabaseTable.COL_LONGITUDE
		};
		
		return mDatabase.getLocation(rowId, columns);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();

	}
}
