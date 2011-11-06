package com.ja.saillog;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

public class LocationTracker implements LocationListener {
	
	private static final String TAG = "SailLogLocationTracker";
	private static final int locationMinimumInterval = 2000; // ms
	
	private List<LocationSink> locationSinks;
	
	public LocationTracker(Activity activity, List<LocationSink> sinks) {
		locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		locationSinks = sinks;
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
		for (LocationSink sink: locationSinks) {
			sink.updateLocation(location.getLatitude(),
								location.getLongitude(),
							    location.getSpeed(),
							    location.getBearing());
		}
		// TODO, also save to the log.
	}
	
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// This isn't correct
		boolean locationAvailable = (LocationProvider.OUT_OF_SERVICE != status);	
		
		for (LocationSink sink: locationSinks) {
			sink.setLocationAvailable(locationAvailable);
		}
	}
	
	public void onProviderEnabled(String provider) { }
	
	public void onProviderDisabled(String provider) { }
	
	private LocationManager locationManager;
}
