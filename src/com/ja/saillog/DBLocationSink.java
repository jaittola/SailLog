package com.ja.saillog;

public class DBLocationSink implements LocationSink {

	public DBLocationSink(TrackDBInterface db) {
	    this.db = db;
	    
		filter = new LocationFilter();
	}
	
	void setDb(TrackDBInterface db) {
	    this.db = db;
	}
	
	public void updateLocation(double latitude, double longitude, double speed,
			double bearing, long time) {    
	    if (true == filter.canUpdate(latitude, longitude, speed, bearing, time) &&
	        null != db) {
	        db.insertPosition(latitude, longitude, bearing, speed);
	    }
	}

	public void insertEvent(int engineStatus, int sailPlan) {
	    if (null != db) {
	        db.insertEvent(engineStatus, sailPlan);            
        }
	}
	
	public void setLocationAvailable(boolean isAvailable) {
	}

	private TrackDBInterface db;
	private LocationFilter filter;
}
