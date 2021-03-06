package edu.uci.mvu1.uci.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.maps.MapView;

public class CustomMapView extends MapView {
	private long lastTouchTime = -1;

	public CustomMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		if (ev.getAction() == MotionEvent.ACTION_DOWN) {

			long thisTime = System.currentTimeMillis();
			if (thisTime - lastTouchTime < 250) {
				this.getController().zoomInFixing((int) ev.getX(), (int) ev.getY());
				lastTouchTime = -1;
			} 
			else 
			{
				lastTouchTime = thisTime;
			}
		}

		return super.onInterceptTouchEvent(ev);
	}
}
