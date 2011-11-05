package com.ja.saillog;

public interface SailLogLocationSink {
	public void updateLocation(double latitude,
							   double longitude,
							   double speed,
							   double bearing);
	public void setLocationAvailable(boolean isAvailable);
}
