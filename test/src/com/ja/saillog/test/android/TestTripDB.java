package com.ja.saillog.test.android;

import junit.framework.Assert;
import android.database.sqlite.SQLiteDatabase;

import com.ja.saillog.TripDB;
import com.ja.saillog.TripDB.TripDbInfo;

public class TestTripDB extends TestDbBase {

    protected void setUp() throws Exception {
        super.setUp();
        
        expectedTables = new String[] { 
                "trip",
        };

        dbif = new TripDB(mContext, "SLDB-Trip-test.db");    
    }

    public void testTempTripInsertAndFetch() {
    	String tripName = "Test trip";
    	
    	dbif.insertTrip(tripName);
        TripDbInfo tdi = dbif.fetchTripId(tripName);

        Assert.assertTrue("Expecting trip id greater than zero, got " + tdi.tripId,
        			      tdi.tripId > 0);
        Assert.assertEquals(tripName, tdi.tripName);
        Assert.assertNotNull(tdi.dbFileName);
    }   		
        		

    public void testNonExistentTrip() {
        TripDbInfo tdi = dbif.fetchTripId("Does not exist");

        Assert.assertNull(tdi);
    }
  
    @Override
    protected SQLiteDatabase getWritableDatabase() {
        return dbif.getWritableDatabase();
    }

    protected TripDB dbif;
}
