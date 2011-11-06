package com.ja.saillog;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class SailLogLocationTracker implements LocationListener {
	
	private static final String TAG = "SailLogLocationTracker";
	private static final int locationMinimumInterval = 2000; // ms
	
	public SailLogLocationTracker(SailLogActivity sl) {
		sink = sl;
		
		locationManager = (LocationManager) sl.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void setEnabled(boolean isEnabled) {
		if (true == isEnabled) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationMinimumInterval, 0, this);
		}
		else {
			locationManager.removeUpdates(this);
		}
	}
	
	public void onLocationChanged(Location location) {
		sink.updateLocation(location.getLatitude(),
							location.getLongitude(),
							location.getSpeed(),
							location.getBearing());
		// TODO, also save to the log.
	}
	
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// This isn't correct
		boolean locationAvailable = (LocationProvider.OUT_OF_SERVICE != status);	
		
		sink.setLocationAvailable(locationAvailable);
	}
	
	public void onProviderEnabled(String provider) { }
	
	public void onProviderDisabled(String provider) { }
	
	private SailLogLocationSink sink;
	private LocationManager locationManager;
}
