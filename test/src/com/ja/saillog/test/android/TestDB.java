package com.ja.saillog.test.android;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.test.AndroidTestCase;

import com.ja.saillog.DB;
import com.ja.saillog.ExportFile;

public class TestDB extends AndroidTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        dbif = new DB(mContext, "SLDB-test.db");
    }

    protected void tearDown() throws Exception {
        SQLiteDatabase db = dbif.getWritableDatabase();
        String dbPath = db.getPath();
        db.close();

        new File(dbPath).delete();

        super.tearDown();
    }

    public void testDbSetUp() {
        SQLiteDatabase db = dbif.getWritableDatabase();

        String[] expectedTables = { 
                "trip",
                "position"};
        checkTablesExist(db, expectedTables);
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

    public void testPositionInsert() {
        dbif.insertTrip("Position test trip");
        int tripId = dbif.fetchTripId("Position test trip");
        Assert.assertTrue(tripId > 0);

        dbif.insertPosition(tripId, 60.1, 24.9, 350, 4);

        String selectionArgs[] = { Integer.toString(tripId) };

        SQLiteDatabase db = dbif.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT position_id, pos_time, latitude, " + 
                "longitude, speed, bearing FROM position WHERE " + 
                "trip_id = ?",
                selectionArgs);
        Assert.assertTrue(c.moveToFirst());
        for (int i = 0; i < c.getColumnCount(); ++i) {
            Assert.assertFalse(c.isNull(i));
        }
        Assert.assertEquals(tripId, c.getInt(0));
        // We skip the timestamp.
        Assert.assertEquals(60.1, c.getDouble(2));
        Assert.assertEquals(24.9, c.getDouble(3));
        Assert.assertEquals(4.0, c.getDouble(4), 0.01);
        Assert.assertEquals(350.0, c.getDouble(5), 0.01);

        c.close();
    }

    public void testDbExport() {
        File dbCopy = null;

        try {
            dbif.insertTrip("Something to export");

            ExportFile ef = null;

            try {
                ef = new ExportFile("db");
                dbif.exportDbAsSQLite(ef);
            } catch (IOException iox) {
                Assert.assertTrue("Got io exception: " + iox.toString(), false);
            }

            dbCopy = new File(ef.fileName());
            File origDb = new File(dbif.getReadableDatabase().getPath());

            // Just compare sizes.
            Assert.assertEquals(String.format("The sizes of the original (%s) and db copy (%s) differ. ", 
                                              origDb.getAbsolutePath(), ef.fileName()), 
                                              origDb.length(), dbCopy.length());	    
            Assert.assertTrue("The database copy is empty", dbCopy.length() > 0);
        }
        finally {
            dbCopy.delete();
        }
    }

    private void checkTablesExist(SQLiteDatabase db, String[] requiredTables) {
        for (String table: requiredTables) {
            SQLiteStatement stm = db.compileStatement(String.format("select count(*) from sqlite_master where type = 'table' and name = '%s'", table));
            int tablesFound = (int) stm.simpleQueryForLong();
            Assert.assertEquals(String.format("Table %s not found; ", table), 1, tablesFound);
        }
    }

    private DB dbif;
}
