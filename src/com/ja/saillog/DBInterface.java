package com.ja.saillog;

/**
 * An Interface for implementing mocks of the actual
 * Database interface.
 */
public interface DBInterface {
	public void insertPosition(int tripId, double latitude, double longitude, 
							   double bearing, double speed);
	
	public void insertEvent(int tripId, int engine, int sailplan);
}
