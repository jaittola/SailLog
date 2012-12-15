package com.ja.saillog.utilities;

import com.ja.saillog.database.TrackDBInterface;

import android.location.Location;

public class DBLocationSink implements LocationSink {

    public DBLocationSink(TrackDBInterface db) {
        this.db = db;

        filter = new LocationFilter();
    }

    public void setDb(TrackDBInterface db) {
        this.db = db;
    }

    public void updateLocation(double latitude, double longitude, double speed,
                               double bearing, double accuracy, long time) {

        double distance = calculateDistance(filter.getPreviousLatitude(), filter.getPreviousLongitude(),
                                            latitude, longitude);

        if (true == filter.canUpdate(latitude, longitude, speed, bearing, time, distance) &&
            null != db) {
            db.insertPosition(latitude, longitude, bearing, speed, distance, accuracy);
        }
    }

    public void insertEvent(int engineStatus, int sailPlan) {
        if (null != db) {
            db.insertEvent(engineStatus, sailPlan);
        }
    }

    public void setLocationAvailable(boolean isAvailable) {
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (Double.isNaN(lat1) ||
            Double.isNaN(lon1) ||
            Double.isNaN(lat2)||
            Double.isNaN(lon2)) {
            return 0.0;
        }

        float [] results = new float[1];
        Location.distanceBetween(lat1,  lon1, lat2, lon2, results);
        return results[0];
    }

    private TrackDBInterface db;
    private LocationFilter filter;
}
