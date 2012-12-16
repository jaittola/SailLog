package com.ja.saillog.utilities;

import com.ja.saillog.database.TrackDBInterface;
import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.quantity.quantity.Speed;

import android.location.Location;

public class DBLocationSink implements LocationSink {

    public DBLocationSink(TrackDBInterface db) {
        this.db = db;

        filter = new LocationFilter();
    }

    public void setDb(TrackDBInterface db) {
        this.db = db;
    }

    public void updateLocation(double latitude, double longitude, Speed speed,
                               double bearing, double accuracy, long time) {

        Distance distance = calculateDistance(filter.getPreviousLatitude(), filter.getPreviousLongitude(),
                                              latitude, longitude);

        if (true == filter.canUpdate(latitude, longitude, 
                                     speed, 
                                     bearing, time, distance) &&
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

    public Distance calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (Double.isNaN(lat1) ||
            Double.isNaN(lon1) ||
            Double.isNaN(lat2)||
            Double.isNaN(lon2)) {
            return QuantityFactory.meters(0.0);
        }

        float [] results = new float[1];
        Location.distanceBetween(lat1,  lon1, lat2, lon2, results);
        return QuantityFactory.meters(results[0]);
    }

    private TrackDBInterface db;
    private LocationFilter filter;
}
