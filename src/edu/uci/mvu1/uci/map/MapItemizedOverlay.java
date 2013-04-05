package edu.uci.mvu1.uci.map;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapItemizedOverlay extends ItemizedOverlay {
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    Context mContext;
 
    public MapItemizedOverlay(Drawable marker) {
        super(boundCenterBottom(marker));
    }
 
    public MapItemizedOverlay(Drawable marker, Context context) {
        super(boundCenterBottom(marker));
        mContext = context;
    }
    
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when)
	{
		return super.draw(canvas, mapView, shadow, when);
	}
 
    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }
 
    @Override
    protected OverlayItem createItem(int i) {
        return mOverlays.get(i);
    }
 
    @Override
    public int size() {
        return mOverlays.size();
    }

}
