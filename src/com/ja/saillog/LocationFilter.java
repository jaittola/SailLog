package com.ja.saillog;

public class LocationFilter {
    /*
     * Returns true if the location or the time of the fix is different enough
     * to warrant inserting it into the database.
     */
    public boolean canUpdate(double latitude, double longitude, double speed, double bearing, long time) {

        // Allow updating on several conditions:
        // * if we had no previous fix, always allow update.
        if (Double.NaN == pLatitude ||
                // * if the time of the update is much more recent than the previous one, 
            isOld(time, pTime) ||
                // * if the new location is far enough form the previous one
            fixIsFaraway(latitude, longitude, pLatitude, pLongitude) ||
                // * if the current speed differs from the previous one.
            speedIsDifferent(speed, pSpeed) ||
                // * if the bearing is different
            bearingIsDifferent(bearing, pBearing)) {
            // Save the previous ones
            keepFix(latitude, longitude, speed, bearing, time);
            return true;
        }
        
        return false;
    }
    
    /**
     * A fix is old if it is more than 5 minutes old
     */
    private boolean isOld(double current, double old) {
        if ((current - old) >  (5 * 60 * 1000)) {
            return true;
        }
        
        return false;
    }

    /**
     * A fix is considered far away if it is more than about 100 meters 
     * (0.54 arc minutes == 9*10^-4 arc degrees) from the previous position.
     * 
     * The calculation uses the equation of the distance of two points 
     * in a plane. It should be accurate enough for this purpose. And this is incorrect.
     * We should calculate how long the longitude degree is at the mean latitude.
     */
    private boolean fixIsFaraway(double latitude, double longitude, double prevLat, double prevLong) {
        double deltaXSq = Math.pow(latitude - prevLat, 2);
        double deltaYSq = Math.pow(longitude - prevLong, 2);
        double distance = Math.sqrt(deltaXSq + deltaYSq);
 
        if (distance > 0.0009) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Speed is different if it is more than 5% different from the previous one.
     * With very small speeds, a larger change is needed.
     */
    private boolean speedIsDifferent(double speed, double prevSpeed) {
        double speedDiff = Math.abs(speed - prevSpeed);
        
        // With small speeds, do not calculate the fraction
        // to avoid division by small numbers (or by 0).
        if (prevSpeed < 0.5) {
            if (speedDiff < 0.2) {
                return false;
            }
            return true;
        }
        
        if ((speedDiff / prevSpeed) > 0.05) {
            return true;
        }
        
        return false;
    }
    
    private boolean bearingIsDifferent(double bearing, double prevBearing) {

        // Calculate the bearing difference.
        double diff = Math.abs(bearing - prevBearing);
        // Then calculate the other alternative bearing difference. 
        double diff2 = Math.abs(360 - diff);
        
        // If either of these differences is below our limit, accept the update.
        if (diff < 5.0 || diff2 < 5.0) {
            return false;
        }
        
        return true;
    }
    
    private void keepFix(double latitude, double longitude, double speed, double bearing, long time) {
        pLatitude = latitude;
        pLongitude = longitude;
        pSpeed = speed;
        pBearing = bearing;
        pTime = time;
    }
    
    //! Previously saved latitude
    private double pLatitude = Double.NaN;
 
    //! Previously saved longitude
    private double pLongitude = Double.NaN;
 
    //! Previously saved speed
    private double pSpeed = Double.NaN;
 
    //! Previously saved bearing
    private double pBearing = Double.NaN;
 
    //! Time when the location was saved previously.
    private long pTime = -1;
 
}
