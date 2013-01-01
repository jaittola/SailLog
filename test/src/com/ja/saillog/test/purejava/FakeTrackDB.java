package com.ja.saillog.test.purejava;

import java.io.IOException;
import java.util.Date;

import com.ja.saillog.database.TrackDBInterface;
import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.quantity.quantity.Speed;
import com.ja.saillog.utilities.ExportFile;


public class FakeTrackDB implements TrackDBInterface {

    @Override
    public void insertPosition(double latitude, double longitude,
                               double bearing, Speed speed,
                               Distance distanceFromPrevious, double accuracy) {
        mLatitude = latitude;
        mLongitude = longitude;
        mBearing = bearing;
        mSpeed = speed;
        mDistanceFromPrevious = distanceFromPrevious;
        mAccuracy = accuracy;
    }

    @Override
    public void insertEvent(int engineStatus, long sailPlan) {
        mEngineStatus = engineStatus;
        mSailPlan = sailPlan;
    }

    @Override
    public void setPreviousEventTimeForTesting(Date timestamp) {        
    }

    @Override
    public TripStats getTripStats() {
        return new TripStats(mTotalDistance,
                             mEngineTime, mSailingTime, 
                             mAverageSpeed,
                             mFirstEntry,
                             mLastEntry);
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
    public Speed mSpeed = null;
    public Distance mDistanceFromPrevious = null;
    public double mAccuracy = -1;

    public int mEngineStatus = -1;
    public long mSailPlan = -1;

    public Distance mTotalDistance = QuantityFactory.meters(24);
    public double mEngineTime = 61.0;
    public double mSailingTime = 121.0;
    public double mAverageSpeed = 5.1;
    
    public Date mFirstEntry = new Date(new Date().getTime() - 20000);
    public Date mLastEntry = new Date();
 }
