package com.ja.saillog;

public class DBLocationSink implements LocationSink {

	public DBLocationSink(DBFactoryInterface factory) {
	    this.factory = factory;
	    
		filter = new LocationFilter();
	}
	
	public void updateLocation(double latitude, double longitude, double speed,
			double bearing, long time) {    
	    if (true == filter.canUpdate(latitude, longitude, speed, bearing, time)) {
	        factory.trackDB().insertPosition(latitude, longitude, bearing, speed);
	    }
	}

	public void insertEvent(int engineStatus, int sailPlan) {
	    factory.trackDB().insertEvent(engineStatus, sailPlan);
	}
	
	public void setLocationAvailable(boolean isAvailable) {
	}

	private DBFactoryInterface factory;
	private LocationFilter filter;
}
