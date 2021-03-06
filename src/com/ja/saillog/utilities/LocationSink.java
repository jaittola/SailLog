package com.ja.saillog.utilities;

import com.ja.saillog.quantity.quantity.Speed;

public interface LocationSink {
    /**
     * A new location update was received.
     * @param latitude
     * @param longitude
     * @param speed
     * @param bearing
     * @param accuracy The accuracy of the position fix (in meters).
     * @param time The timestamp of the location fix. Milliseconds since 1.1.1970.
     */
	public void updateLocation(double latitude,
							   double longitude,
							   Speed speed,
							   double bearing,
							   double accuracy,
							   long time);
	public void setLocationAvailable(boolean isAvailable);
}
