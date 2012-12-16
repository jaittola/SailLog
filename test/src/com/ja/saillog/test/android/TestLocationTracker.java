package com.ja.saillog.test.android;

import java.util.LinkedList;

import junit.framework.Assert;
import android.location.Location;
import android.test.AndroidTestCase;

import com.ja.saillog.quantity.quantity.Speed;
import com.ja.saillog.utilities.LocationSink;
import com.ja.saillog.utilities.LocationTracker;

public class TestLocationTracker extends AndroidTestCase {
	private class FakeSink implements LocationSink {
		
		public FakeSink() {
			lastLatitude = Double.NaN;
			lastLongitude = Double.NaN;
			lastSpeed = null;
			lastBearing = Double.NaN;
			lastAccuracy = Double.NaN;
		}
		
		@Override
		public void updateLocation(double latitude, double longitude,
								   Speed speed, double bearing, 
								   double accuracy, long time) {
			lastLatitude = latitude;
			lastLongitude = longitude;
			lastSpeed = speed;
			lastBearing = bearing;
			lastAccuracy = accuracy;
		}

		@Override
		public void setLocationAvailable(boolean isAvailable) {
			// TODO Auto-generated method stub
		}

		public double lastLatitude;
		public double lastLongitude;
		public Speed lastSpeed;
		public double lastBearing;
		public double lastAccuracy;
	}
	
	@Override
	public void setUp() {

		sinks = new LinkedList<LocationSink>();
		sinks.add(new FakeSink());
		sinks.add(new FakeSink());
		
		lt = new LocationTracker(null);
		lt.setSinks(sinks);
		
		location = new Location("FakeGPS");
		location.setLatitude(1);
		location.setLongitude(2);
		location.setBearing(3);
		location.setSpeed(4);
		location.setAccuracy(42);
	}
	
	public void testLocationData() {
		lt.onLocationChanged(location);
		
		for (LocationSink s: sinks) {
			FakeSink sink = (FakeSink) s;
			Assert.assertEquals(location.getLatitude(), sink.lastLatitude);
			Assert.assertEquals(location.getLongitude(), sink.lastLongitude);
			Assert.assertEquals(location.getBearing(), sink.lastBearing, 0.1);
			Assert.assertEquals(location.getSpeed(), sink.lastSpeed.num(), 0.1);
			Assert.assertEquals(location.getAccuracy(), sink.lastAccuracy, 0.1);
		}
	}
	
	LinkedList<LocationSink> sinks;
	
	LocationTracker lt;
	
	Location location;
}
