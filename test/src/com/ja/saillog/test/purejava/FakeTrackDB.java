package com.ja.saillog.test.purejava;

import java.io.IOException;

import com.ja.saillog.database.TrackDBInterface;
import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.utilities.ExportFile;


public class FakeTrackDB implements TrackDBInterface{

    @Override
    public void insertPosition(double latitude, double longitude,
                               double bearing, double speed,
                               Distance distanceFromPrevious, double accuracy) {
        mLatitude = latitude;
        mLongitude = longitude;
        mBearing = bearing;
        mSpeed = speed;
        mDistanceFromPrevious = distanceFromPrevious;
        mAccuracy = accuracy;
    }

    @Override
    public void insertEvent(int engineStatus, int sailPlan) {
        mEngineStatus = engineStatus;
        mSailPlan = sailPlan;
    }

    @Override
    public TripStats getTripStats() {
        return new TripStats(mTotalDistance,
                             mEngineTime, mSailingTime, 
                             mAverageSpeed);
    }

    @Override
    public void exportDbAsKML(ExportFile exportFile) throws IOException {
    }

    @Override
    public void exportDbAsSQLite(ExportFile exportFile) throws IOException {
    }

    @Override
    public void close() {
    }


    public double mLatitude = -91;
    public double mLongitude = -181;
    public double mBearing = -1;
    public double mSpeed = -1;
    public Distance mDistanceFromPrevious = null;
    public double mAccuracy = -1;

    public int mEngineStatus = -1;
    public int mSailPlan = -1;

    public Distance mTotalDistance = QuantityFactory.meters(24);
    public double mEngineTime = 0.5;
    public double mSailingTime = 3.7;
    public double mAverageSpeed = 5.1;
}
