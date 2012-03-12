package com.ja.saillog;

public class DBLocationSink implements LocationSink {

	public DBLocationSink(DBInterface db) {
		dbif = db;
		filter = new LocationFilter();
	}
	
	void setActiveTripId(int id) {
		activeTripId = id;
	}
	
	public void updateLocation(double latitude, double longitude, double speed,
			double bearing, long time) {
	    if (true == filter.canUpdate(latitude, longitude, speed, bearing, time)) {
	        dbif.insertPosition(activeTripId, latitude, longitude, bearing, speed);
	    }
	}

	public void setLocationAvailable(boolean isAvailable) {
	}

	private DBInterface dbif;
	private LocationFilter filter;
	private int activeTripId = 1;
}
