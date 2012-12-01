package com.ja.saillog.test.purejava;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.ja.saillog.DBLocationSink;
import com.ja.saillog.TrackDBInterface;

public class TestDbLocationSink extends TestCase {
	
	public class FakeDB implements TrackDBInterface {

		@Override
		public void insertPosition(double latitude,
				double longitude, double bearing, double speed) {
			mLatitude = latitude;
			mLongitude = longitude;
			mBearing = bearing;
			mSpeed = speed;
		}

		@Override
		public void insertEvent(int engineStatus, int sailPlan) {	   
		    mEngineStatus = engineStatus;
		    mSailPlan = sailPlan;
	    }

		public double mLatitude = -91;
		public double mLongitude = -181;
		public double mBearing = -1;
		public double mSpeed = -1;
		
		public int mEngineStatus = -1;
		public int mSailPlan = -1;
	}
	
	protected void setUp() throws Exception {
	    super.setUp();
	    
	    db = new FakeDB();
	    sink = new DBLocationSink(db);
	}
	
	public void testDbLocationSink() {		
		sink.updateLocation(20, 60, 2, 3, 0);
		
		Assert.assertEquals(20.0, db.mLatitude);
		Assert.assertEquals(60.0, db.mLongitude);
		Assert.assertEquals(3.0, db.mBearing);
		Assert.assertEquals(2.0, db.mSpeed);
	}
	
	private FakeDB db;
	private DBLocationSink sink;
}
