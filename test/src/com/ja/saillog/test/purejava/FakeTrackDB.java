package com.ja.saillog.test.purejava;

import java.io.IOException;

import com.ja.saillog.ExportFile;
import com.ja.saillog.TrackDBInterface;

public class FakeTrackDB implements TrackDBInterface{

    @Override
    public void insertPosition(double latitude,
                               double longitude, double bearing, double speed) {
        mLatitude = latitude;
        mLongitude = longitude;
        mBearing = bearing;
        mSpeed = speed;
    }

    @Override
    public void insertEvent(int engineStatus, int sailPlan) {
        mEngineStatus = engineStatus;
        mSailPlan = sailPlan;
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

    public int mEngineStatus = -1;
    public int mSailPlan = -1;
}
