package com.ja.saillog;

public class DBLocationSink implements LocationSink {

	public DBLocationSink(DBInterface db) {
		dbif = db;
	}
	
	void setActiveTripId(int id) {
		activeTripId = id;
	}
	
	public void updateLocation(double latitude, double longitude, double speed,
			double bearing) {
		dbif.insertPosition(activeTripId, latitude, longitude, bearing, speed);
	}

	public void setLocationAvailable(boolean isAvailable) {
	}

	private DBInterface dbif;
	private int activeTripId = 1;
}
