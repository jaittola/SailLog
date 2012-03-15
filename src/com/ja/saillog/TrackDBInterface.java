package com.ja.saillog;

/**
 * An Interface for implementing mocks of the actual
 * position and event database interface.
 */
public interface TrackDBInterface {
	public void insertPosition(double latitude, double longitude, 
							   double bearing, double speed);
	
	public void insertEvent(int engineStatus, int sailPlan);
}
