package com.ja.saillog;

public class DBLocationSink implements LocationSink {

	public DBLocationSink(TrackDBInterface db) {
		dbif = db;
		filter = new LocationFilter();
	}
	
	void setActiveTripId(int id) {
		activeTripId = id;
	}
	
	public void updateLocation(double latitude, double longitude, double speed,
			double bearing, long time) {
	    if (true == filter.canUpdate(latitude, longitude, speed, bearing, time)) {
	        dbif.insertPosition(latitude, longitude, bearing, speed);
	    }
	}

	public void insertEvent(int engineStatus, int sailPlan) {
	    dbif.insertEvent(engineStatus, sailPlan);
	}
	
	public void setLocationAvailable(boolean isAvailable) {
	}

	private TrackDBInterface dbif;
	private LocationFilter filter;
	
	// TODO, trip ids to be added.
	private int activeTripId = 1;
}
