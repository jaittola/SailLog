package com.ja.saillog.test.android;

import junit.framework.Assert;
import android.database.sqlite.SQLiteDatabase;

import com.ja.saillog.TripDB;

public class TestTripDB extends TestDbBase {

    protected void setUp() throws Exception {
        super.setUp();
        
        expectedTables = new String[] { 
                "trip",
                "trip_leg",
        };

        dbif = new TripDB(mContext, "SLDB-Trip-test.db");    
    }

    public void testTempTripInsertAndFetch() {
        dbif.insertTrip("Test trip");
        int id = dbif.fetchTripId("Test trip");

        Assert.assertTrue("Expecting trip id greater than zero, got " + id, id > 0);
    }

    public void testNonExistentTrip() {
        int id = dbif.fetchTripId("Does not exist");

        Assert.assertTrue("Expecting trip id smaller than zero, got" + id, id < 0);
    }
  
    @Override
    protected SQLiteDatabase getWritableDatabase() {
        return dbif.getWritableDatabase();
    }

    protected TripDB dbif;
}
