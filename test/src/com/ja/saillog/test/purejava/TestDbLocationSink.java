package com.ja.saillog.test.purejava;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.ja.saillog.DBLocationSink;
import com.ja.saillog.DBInterface;

public class TestDbLocationSink extends TestCase {
	
	public class FakeDbIf implements DBInterface {

		@Override
		public void insertPosition(int tripId, double latitude,
				double longitude, double bearing, double speed) {
			mTripId = tripId;
			mLatitude = latitude;
			mLongitude = longitude;
			mBearing = bearing;
			mSpeed = speed;
		}

		@Override
		public void insertEvent(int tripId, int engine, int sailplan) {	    
	    }

		public int mTripId = -1;
		public double mLatitude = -91;
		public double mLongitude = -181;
		public double mBearing = -1;
		public double mSpeed = -1;
	}
	
	public void testDbLocationSink() {
		FakeDbIf fdi = new FakeDbIf();
		DBLocationSink sink = new DBLocationSink(fdi);
		
		sink.updateLocation(20, 60, 2, 3, 0);
		
		Assert.assertEquals(1, fdi.mTripId);
		Assert.assertEquals(20.0, fdi.mLatitude);
		Assert.assertEquals(60.0, fdi.mLongitude);
		Assert.assertEquals(3.0, fdi.mBearing);
		Assert.assertEquals(2.0, fdi.mSpeed);
	}
}
