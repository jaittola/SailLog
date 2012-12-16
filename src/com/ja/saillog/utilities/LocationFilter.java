package com.ja.saillog.utilities;

import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.quantity.quantity.Speed;

public class LocationFilter {
    /*
     * Returns true if the location or the time of the fix is different enough
     * to warrant inserting it into the database.
     */
    public boolean canUpdate(double latitude, double longitude,
                             Speed speed, double bearing,
                             long time, Distance distanceToPrevious) {

        // Allow updating on several conditions:
        // * if we had no previous fix, always allow update.
        if (Double.NaN == pLatitude ||
            // * if the time of the update is much more recent than the previous one,
            isOld(time, pTime) ||
            // * if the new location is far enough form the previous one
            fixIsFaraway(distanceToPrevious) ||
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
     * Get the previous latitude.
     */
    double getPreviousLatitude() {
        return pLatitude;
    }

    /**
     * Get the previous longitude.
     */
    public double getPreviousLongitude() {
        return pLongitude;
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
     * A fix is considered far away if it is more than 100 meters from
     * the previous position.
     */
    private boolean fixIsFaraway(Distance distance) {
        if (QuantityFactory.meters(distance).num() > 100) {
            return true;
        }

        return false;
    }

    /**
     * Speed is different if it is more than 5% different from the previous one.
     * With very small speeds, a larger change is needed.
     */
    private boolean speedIsDifferent(Speed speed, Speed prevSpeed) {
        double speedMs = QuantityFactory.metersPerSecond(speed).num();
        
        double prevSpeedMs = 0.0;
        if (null != prevSpeed) {
            prevSpeedMs = QuantityFactory.metersPerSecond(prevSpeed).num();
        }
        
        double speedDiff = Math.abs(speedMs - prevSpeedMs);

        // With small speeds, do not calculate the fraction
        // to avoid division by small numbers (or by 0).
        double smallSpeedLimit = 0.5;
        double smallSpeedUpdateLimit = 0.2;
        if (prevSpeedMs < smallSpeedLimit) {
            if (speedDiff < smallSpeedUpdateLimit) {
                return false;
            }
            return true;
        }

        if ((speedDiff / prevSpeedMs) > 0.05) {
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

    private void keepFix(double latitude, double longitude,
                         Speed speed, double bearing,
                         long time) {
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
    private Speed pSpeed = null;

    //! Previously saved bearing
    private double pBearing = Double.NaN;

    //! Time when the location was saved previously.
    private long pTime = -1;

}
