package com.ja.saillog.test.android;

import junit.framework.Assert;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.ja.saillog.TripDB;
import com.ja.saillog.TripDBInterface.TripInfo;

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
        TripInfo tdi = dbif.getTrip(tripName);

        Assert.assertTrue("Expecting trip id greater than zero, got " + tdi.tripId,
        			      tdi.tripId > 0);
        Assert.assertEquals(tripName, tdi.tripName);
        Assert.assertNotNull(tdi.dbFileName);
    }   		
        		
    public void testTripList() {
        String tripNameBase = "Test trip ";
        
        for (int i = 0; i < 3; ++i) {
            String tripName = tripNameBase + i;
            dbif.insertTrip(tripName);
            try {
            	Thread.sleep(1001);  // Bad but we need different timestamps.
            }
            catch (Exception ex) {
            	Assert.fail("Caught exception while sleeping");
            }
        }
        
        Cursor c = dbif.listTrips();
        
        // Verify that we get the trips back in the reverse order (they should
        // be sorted by the activate time in reverse).
        for (int i = 2; i >= 0; --i) {
            Assert.assertTrue(c.moveToNext());
            
            String expectedTripName = tripNameBase + i;
            Assert.assertEquals(c.getString(c.getColumnIndex("trip_name")), expectedTripName);
        }
        
        c.close();
    }
    
    public void testTripSelection() {
    	String [] tripNames = new String[3];
        String tripNameBase = "Test trip selection ";
        
        for (int i = 0; i < tripNames.length; ++i) {
        	tripNames[i] = tripNameBase + i;
        	dbif.insertTrip(tripNames[i]);
        }
    	
        TripInfo tdi = dbif.getTrip(tripNames[1]);
        TripInfo tdi0 = dbif.getTrip(tripNames[0]);
        
        selectTripAndVerify(tdi);
        selectTripAndVerify(tdi0);
        
        verifySelectionCount(1);
    }
    
    void selectTripAndVerify(TripInfo tdi) {
        dbif.selectTrip(tdi.tripId);
        TripInfo selectedTdi = dbif.getSelectedTrip();
        Assert.assertEquals(tdi.tripId, selectedTdi.tripId);        
    }
    
    void verifySelectionCount(long expectedSelections) {
        SQLiteStatement stm = dbif.getReadableDatabase().compileStatement("SELECT COUNT(*) FROM trip " +
                "WHERE selected = 1");
        long foundSelections = stm.simpleQueryForLong();
        stm.close();
        
        Assert.assertEquals(expectedSelections, foundSelections);
    }
    
    public void testTripUnselection() {
    	String tripName = "TripUnSelectionName";
    	dbif.insertTrip(tripName);
    	TripInfo tdi = dbif.getTrip(tripName);
    	
        dbif.selectTrip(tdi.tripId);
    	dbif.unselectTrips();
    	
    	TripInfo selectedTdi = dbif.getSelectedTrip();
    	Assert.assertEquals(selectedTdi, null);
        verifySelectionCount(0);
    }
    
    public void testNonExistentTrip() {
        TripInfo tdi = dbif.getTrip("Does not exist");

        Assert.assertNull(tdi);
    }
  
    @Override
    protected SQLiteDatabase getWritableDatabase() {
        return dbif.getWritableDatabase();
    }

    protected TripDB dbif;
}
