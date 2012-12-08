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

    	TripInfo tdi = dbif.insertTrip(tripName);
        TripInfo tdi2 = dbif.getTripById(tdi.tripId);

        Assert.assertTrue("Expecting trip id greater than zero, got " + tdi.tripId,
                          tdi.tripId > 0);
        Assert.assertEquals(tripName, tdi.tripName);
        Assert.assertNotNull(tdi.dbFileName);

        Assert.assertEquals(tdi.tripName, tdi2.tripName);
        Assert.assertEquals(tdi.tripId, tdi2.tripId);
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
        TripInfo [] trips = new TripInfo[3];

        for (int i = 0; i < tripNames.length; ++i) {
        	tripNames[i] = tripNameBase + i;
        	trips[i] = dbif.insertTrip(tripNames[i]);
        }

        selectTripAndVerify(trips[1]);
        selectTripAndVerify(trips[0]);
    }

    void selectTripAndVerify(TripInfo tdi) {
        dbif.selectTrip(tdi.tripId);
        TripInfo selectedTdi = dbif.getSelectedTrip();
        Assert.assertEquals(tdi.tripId, selectedTdi.tripId);

        verifySelectionCount(1);
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
    	TripInfo tdi = dbif.insertTrip(tripName);

        dbif.selectTrip(tdi.tripId);
    	dbif.unselectTrips();

    	TripInfo selectedTdi = dbif.getSelectedTrip();
    	Assert.assertEquals(selectedTdi, null);
        verifySelectionCount(0);
    }

    public void testNonExistentTrip() {
        TripInfo tdi = dbif.getTripById(3939393);
        Assert.assertNull(tdi);
    }

    public void testDeleteTrip() {
        TripInfo ti = dbif.insertTrip("MyTestTrip");

        dbif.deleteTrip(ti.tripId);

        TripInfo ti2 = dbif.getTripById(ti.tripId);
        Assert.assertNull(ti2);
    }

    public void testDeleteSelectedTrip() {
        // Deleting the selected trip is not permitted.
        TripInfo ti = dbif.insertTrip("MyTestTripDeleteNotSelected");

        dbif.selectTrip(ti.tripId);

        dbif.deleteTrip(ti.tripId);

        TripInfo ti2 = dbif.getTripById(ti.tripId);
        Assert.assertNotNull(ti2);
    }

    public void testUpdateTrip() {
        TripInfo ti = dbif.insertTrip("MyTestUpdateTrip");

        TripInfo ti2 = new TripInfo(ti.tripId, "New Name", "");
        dbif.updateTrip(ti2);

        TripInfo ti3 = dbif.getTripById(ti.tripId);
        Assert.assertEquals(ti2.tripName, ti3.tripName);
    }

    @Override
    protected SQLiteDatabase getWritableDatabase() {
        return dbif.getWritableDatabase();
    }

    protected TripDB dbif;
}
