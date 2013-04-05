package edu.uci.mvu1.uci.map;

import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.text.InputType;

public class SearchActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
	private SimpleCursorAdapter mAdapter;
	private EditText textBox;
	
	private ListView lv = null;
	
	static final String[] PROJECTION = new String[] {
		BaseColumns._ID,
		DatabaseTable.COL_TITLE
	};
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        lv = (ListView) findViewById(R.id.searchresults);
        
        textBox = (EditText) findViewById(R.id.search_inception);
        textBox.setInputType(InputType.TYPE_NULL);
        textBox.setCursorVisible(false);
        textBox.setFocusable(false);
        
        String[] from = { DatabaseTable.COL_TITLE, DatabaseTable.COL_SEARCH_TERMS};
        int[] to = { R.id.listitem_name, R.id.listitem_description };
        
		mAdapter = new SimpleCursorAdapter(this, R.layout.list_item, null, from, to);
		lv.setAdapter(mAdapter);
		
		lv.setOnItemClickListener(this);
		
		handleIntent(getIntent());
    
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search, menu);
        return true;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	setIntent(intent);
    	handleIntent(intent);
    }
    
    private void handleIntent(Intent intent)
    {
    	if (Intent.ACTION_SEARCH.equals(intent.getAction()))
    	{
    		String query = intent.getStringExtra(SearchManager.QUERY);
    		//Cursor c = MainActivity.db.getLocationMatches(query, null);
    		Bundle bundle = new Bundle();
    		bundle.putString("query", query);
    		textBox.setText(query);
    		getSupportLoaderManager().restartLoader(0, bundle, this);
    		
    		// This allows us to save the last search query in the EditText of both the search and main activities.
    		Intent dataIntent = new Intent();
    		dataIntent.setAction("edu.uci.mvu1.uci.map.SEARCH_BROADCAST");
    		dataIntent.putExtra("query", query);
    		this.sendBroadcast(dataIntent);
    	}
    	else if (Intent.ACTION_VIEW.equals(intent.getAction()))
    	{
    		// Copy over the intent's data into a new one and send it to MainActivity to be displayed
			Intent location = new Intent(this, MainActivity.class);
			location.setAction(intent.getAction());
			location.setData(intent.getData());
			
			startActivity(location);
			finish();
    	}
    }
    
    public void displaySearchDialog(View view) {
    	onSearchRequested();
    }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, MapProvider.CONTENT_URI, PROJECTION, null, new String[]{args.getString("query")}, null);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
		if (data == null || data.getCount() == 0)
		{
			lv.setEmptyView(findViewById(android.R.id.empty));
		}
		else
		{
			lv.setEmptyView(null);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// Build the Intent used to open MainActivity with a
		// specific location Uri
		Intent location = new Intent(getApplicationContext(), MainActivity.class);
		location.setAction(Intent.ACTION_VIEW);
		Uri data = Uri.withAppendedPath(MapProvider.CONTENT_URI, String.valueOf(id));
		location.setData(data);
		startActivity(location);
	}

    
}
