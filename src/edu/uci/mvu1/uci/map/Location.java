package edu.uci.mvu1.uci.map;

import java.util.ArrayList;
import java.util.Vector;

import com.google.android.maps.GeoPoint;

public class Location {
	
	private String name;
	private ArrayList<String> searchTerms;
	private GeoPoint geoPoint;
	
	Location(String name, ArrayList<String> searchTerms, GeoPoint geoPoint)
	{
		this.name = name;
		this.searchTerms = searchTerms;
		this.geoPoint = geoPoint;
	}
	
	Location(String name, ArrayList<String> searchTerms, double latitude, double longitude)
	{
		this.name = name;
		this.searchTerms = searchTerms;
		
		int latitudeE6 = (int) (latitude*Math.pow(10, 6));
		int longitudeE6 = (int) (longitude*Math.pow(10, 6));
		geoPoint = new GeoPoint(latitudeE6, longitudeE6);
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<String> getSearchTerms() {
		return searchTerms;
	}
	
	public GeoPoint getGeoPoint() {
		return geoPoint;
	}
	
}
