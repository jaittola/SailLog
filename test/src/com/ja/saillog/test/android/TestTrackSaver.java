package com.ja.saillog.test.android;

import junit.framework.Assert;

import com.ja.saillog.database.DBProvider;
import com.ja.saillog.database.TrackDBInterface;
import com.ja.saillog.database.TripDBInterface;
import com.ja.saillog.serv.TrackSaver;
import com.ja.saillog.test.purejava.FakeTrackDB;
import com.ja.saillog.test.purejava.FakeTripDB;
import com.ja.saillog.utilities.LocationServiceProvider;
import com.ja.saillog.utilities.Propulsion;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.test.AndroidTestCase;

public class TestTrackSaver extends AndroidTestCase {
      
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        trackDB = new FakeTrackDB();
        tripDB = new FakeTripDB();
        tripDB.setupTrips();
        
        DBProvider.setProvider(new DBProvider() {
            protected TripDBInterface getTripDBInstance(Context context) {
                return tripDB;
            }
            protected TrackDBInterface getTrackDBInstance(Context context,
                                                          String databaseName) {
                return trackDB;
            }            
        });
        
        lsp = new TSSLocationServProvider();
        LocationServiceProvider.setProvider(lsp);
        
        ts = new TrackSaver(getContext());
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        ts.stopSaving();
        
        ts = null;
        trackDB = null;
        tripDB = null;
    }
    
    public void testStartSavingNoTrip() {
        tripDB.selectedTripId = null;

        Assert.assertFalse(ts.isSaving());
        Assert.assertFalse(ts.startSaving());
        Assert.assertFalse(ts.isSaving());
    }
            
    public void testStartSavingWithTrip() {
        Assert.assertTrue(ts.startSaving());
        Assert.assertNotNull(lsp.listener);
        Assert.assertTrue(ts.isSaving());
        
        savePosition(61.0, 25.1, ensureSaved);
    }
    
    public void testStopSaving() {
        Assert.assertTrue(ts.startSaving());
        ts.stopSaving();
        Assert.assertFalse(ts.isSaving());
 
        Assert.assertNull(lsp.listener);
    }
    
    public void testPropulsionSave() {
        Propulsion propulsion = getSamplePropulsion();
        
        Assert.assertTrue(ts.startSaving());
        ts.changePropulsion(propulsion);
        
        Assert.assertEquals(propulsion.getSailPlan(), trackDB.mSailPlan);
        Assert.assertEquals(propulsion.getEngine(), trackDB.mEngineStatus);
    }
    
    public void testPropulsionSaveWithoutTrip() {
        trackDB.mSailPlan = -1;
        trackDB.mEngineStatus = false;
        
        Propulsion propulsion = getSamplePropulsion();
        ts.changePropulsion(propulsion);
        
        Assert.assertEquals(-1, trackDB.mSailPlan);
        Assert.assertFalse(trackDB.mEngineStatus);
    }

    private void XtestPropulsionTimedUpdate() {  // TODO ENABLE
        trackDB.eventsSaved = 0;
        // ts.setPropulsionUpdateInterval(100);        
        Propulsion propulsion = getSamplePropulsion();
        
        Assert.assertTrue(ts.startSaving());
        
        ts.changePropulsion(propulsion);
        Assert.assertTrue("Propulsion should have been updated at least twice, " +
                          "but got " + 
                          trackDB.eventsSaved +
                          " updates",
                          trackDB.eventsSaved >= 2);
    }

    public void testChangeTrip() {
        
    }
    
    public void testStatusQuery() {
        
    }
        
    private void savePosition(double lat, double lon, boolean expectSaved) {
        trackDB.mLatitude = Double.NaN;
        trackDB.mLongitude = Double.NaN;
        
        Location loc = new Location("TestTrackSaver");
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        
        lsp.listener.onLocationChanged(loc);
 
        if (ensureSaved == expectSaved) {
            Assert.assertEquals(lat, trackDB.mLatitude);
            Assert.assertEquals(lon, trackDB.mLongitude);
        }
        else {
            Assert.assertTrue(Double.isNaN(trackDB.mLatitude));
            Assert.assertTrue(Double.isNaN(trackDB.mLongitude));
        }
    }
    
    private Propulsion getSamplePropulsion() {
        Propulsion propulsion = new Propulsion();
        propulsion.setEngine(Propulsion.engineOn);
        propulsion.setSail(1, Propulsion.up);
        
        return propulsion;
    }
    
    private class TSSLocationServProvider extends LocationServiceProvider {
        public void requestUpdates(LocationListener listener) {
            this.listener = listener;
        }

        public void stopUpdates(LocationListener listener) {
            this.listener = null;
        }
        
        public LocationListener listener = null;
    }
    
    private static final boolean ensureSaved = true;
    private static final boolean notSaved = false;
    
    private TrackSaver ts;
    private FakeTripDB tripDB;
    private FakeTrackDB trackDB;
    private TSSLocationServProvider lsp;
}
