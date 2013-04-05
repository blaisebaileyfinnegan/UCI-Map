package edu.uci.mvu1.uci.map;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.SearchManager.OnDismissListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.support.v4.app.NavUtils;
import android.text.InputType;

public class MainActivity extends MapActivity implements LocationListener {
	public static DatabaseTable db;
	
	private EditText textBox;
	
	private BroadcastReceiver broadcastReceiver;
	
	private MapController mapController;
	
	private LocationManager locationManager;
	private android.location.Location location;
	
	private static final GeoPoint CAMPUS_CENTER = new GeoPoint(33645832, -117842419);
	private static final int CAMPUS_RADIUS = 8000;
	
	private boolean satelliteMode = false;
	
	private CustomMapView mapView;
	
	private List<Overlay> mapOverlays;
	private MyLocationOverlay locationOverlay;
	private MapItemizedOverlay destinationOverlay;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        textBox = (EditText) findViewById(R.id.search);
        textBox.setInputType(InputType.TYPE_NULL);
        textBox.setCursorVisible(false);
        textBox.setFocusable(false);
        
        db = new DatabaseTable(this);
        
        mapView = (CustomMapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        mapController = mapView.getController();
		mapController.zoomToSpan(CAMPUS_RADIUS, CAMPUS_RADIUS);
        
        IntentFilter filter = new IntentFilter();
        filter.addAction("edu.uci.mvu1.uci.map.SEARCH_BROADCAST");
        broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String query = intent.getStringExtra("query");
				textBox.setText(query);
			}
        	
        };
        registerReceiver( broadcastReceiver, filter);

        startLocationServices();

		Drawable drawable = this.getResources().getDrawable(R.drawable.pin);
		destinationOverlay = new MapItemizedOverlay(drawable, this);
				
        handleIntent(this.getIntent());
    }
    
    private void handleIntent(Intent intent)
    {
    	if (Intent.ACTION_VIEW.equals(intent.getAction()))
    	{
    		Uri data = intent.getData();
    		
    		String[] PROJECTION = new String[] {
    				DatabaseTable.COL_TITLE,
    				DatabaseTable.COL_LATITUDE,
    				DatabaseTable.COL_LONGITUDE
    		};
    		Cursor c = this.managedQuery(data, PROJECTION, null, null, null);
    		showSearchResult(c);
    	}
    	else
		{
			mapController.animateTo(CAMPUS_CENTER);
		}
    }
    
	public void showSearchResult(Cursor cursor) {
		cursor.moveToFirst();

		int nameIndex = cursor.getColumnIndexOrThrow(DatabaseTable.COL_TITLE);
		int latitudeIndex = cursor.getColumnIndexOrThrow(DatabaseTable.COL_LATITUDE);
		int longitudeIndex = cursor.getColumnIndexOrThrow(DatabaseTable.COL_LONGITUDE);

		Location destination = new Location(cursor.getString(nameIndex), null, cursor.getDouble(latitudeIndex),
				cursor.getDouble(longitudeIndex));

		animateToDestination(destination);
	}
	
	public void animateToDestination(Location location) {
		OverlayItem item = new OverlayItem(location.getGeoPoint(), "Building", "Yo");
		
		destinationOverlay.addOverlay(item);
		mapOverlays.add(destinationOverlay);
		
		mapController.animateTo(location.getGeoPoint());
	}
    
    private void startLocationServices() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        
        this.location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null)
        {
        	location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        
        if (location == null)
        {
        	location = new android.location.Location("MIDDLE_OF_NOWHERE");
        }
        
        boolean gpsEnabled = true;
        boolean wifiEnabled = true;
        
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        	gpsEnabled = false;
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        	wifiEnabled = false;
        
        if (!gpsEnabled || !wifiEnabled)
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	String text = "";
        	if (!gpsEnabled)
        		text+="\n - GPS";
        	if (!wifiEnabled)
        		text+="\n - WiFi";
        	builder.setMessage("UCI Map may not be fully functional because the following is disabled:\n" + text + 
        			"\n\nYou may either continue using the app with less features or click below to go to the " +
        			"settings and enable location services.")
        			.setCancelable(false)
        			.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							enableLocationSettings();
						}
					})
        			.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
        	AlertDialog alert = builder.create();
        	alert.show();
        }
        
        mapOverlays = mapView.getOverlays();
        mapOverlays.clear();
        locationOverlay = new MyLocationOverlay(this, mapView);
        mapOverlays.add(locationOverlay);
    }
    
    protected void onResume() 
    {
        locationOverlay.enableMyLocation();
        super.onResume();
    }
    
    protected void onPause()
    {
    	locationOverlay.disableMyLocation();
    	super.onPause();
    }
    
   
    
    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }
    
    public void satelliteMode(View view) {
    	ImageButton button = (ImageButton) this.findViewById(R.id.satellite);
    	if (!satelliteMode)
    	{
    		mapView.setSatellite(true);
    		satelliteMode = true;
    		button.setSelected(true);
    	}
    	else
    	{
    		mapView.setSatellite(false);
    		satelliteMode = false;
    		button.setSelected(false);
    	}
    }
    
    public void zoomToMyLocation(View view) {
    	GeoPoint point = new GeoPoint( (int)(this.location.getLatitude()*Math.pow(10, 6)), (int) (this.location.getLongitude()*Math.pow(10, 6)));
    	mapController.animateTo(point);
    }

	public void displaySearchDialog(View view) {
    	onSearchRequested();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    @Override
    protected void onDestroy()
    {
    	unregisterReceiver(broadcastReceiver);
    	super.onDestroy();
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onLocationChanged(android.location.Location location) {
		this.location = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

    
}
