package com.ja.saillog.test.android;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ja.saillog.database.TrackDB;
import com.ja.saillog.database.TrackDBInterface.TripStats;
import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.quantity.quantity.Speed;
import com.ja.saillog.utilities.ExportFile;


public class TestTrackDB extends TestDbBase {

    private class PositionContainer {
        public PositionContainer(double latitude, double longitude,
                                 double bearing, Speed speed,
                                 double accuracy, Distance distanceFromPrev) {
            myLat = latitude;
            myLong = longitude;
            myBearing = bearing;
            mySpeed = speed;
            myAccuracy = accuracy;
            myDistanceFromPrev = distanceFromPrev;
        }

        public double myLat;
        public double myLong;
        public double myBearing;
        public Speed mySpeed;
        public double myAccuracy;
        public Distance myDistanceFromPrev;
    }

    private class EventContainer {
        public EventContainer(int engine, int sailplan) {
            myEngine = engine;
            mySailplan = sailplan;
        }

        public int myEngine;
        public int mySailplan;
    }

    protected void setUp() throws Exception {
        super.setUp();

        expectedTables = new String[] {
            "position",
            "event",
            "trip_stats",
        };

        dbif = new TrackDB(mContext, "SLDB-Track-test.db");
    }

    public void testPositionInsert() {
        insertPositionsAndEvents();

        SQLiteDatabase db = dbif.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT position_id, pos_time, latitude, " +
                               "longitude, speed, bearing, accuracy " +
                               "FROM position " +
                               "ORDER BY position_id",
                               null);
        Assert.assertTrue(c.moveToFirst());

        Assert.assertEquals(c.getCount(), posS.length);

        try {
            int row = 0;
            do {
                checkColumnsNotNull(c);

                // We skip over the two first columns, which contain the
                // position id and the timestamp.
                int col = 2;
                Assert.assertEquals(posS[row].myLat, c.getDouble(col++));
                Assert.assertEquals(posS[row].myLong, c.getDouble(col++));
                Assert.assertEquals(posS[row].mySpeed.num(),
                                    c.getDouble(col++), 0.01);
                Assert.assertEquals(posS[row].myBearing,
                                    c.getDouble(col++), 0.01);
                Assert.assertEquals(posS[row].myAccuracy,
                                    c.getDouble(col++), 0.01);

                row++;
            } while(c.moveToNext());
        }
        finally {
            c.close();
        }

    }

    public void testInsertEventsWithPositions() {
        insertPositionsAndEvents();

        SQLiteDatabase db = dbif.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT position_id, event_time, engine, " +
                               "sailplan " +
                               "FROM event " +
                               "ORDER BY event_id",
                               null);

        try {
            Assert.assertTrue(c.moveToFirst());
            Assert.assertEquals(c.getCount(), eventS.length);

            for (EventContainer ev: eventS) {
                checkColumnsNotNull(c);

                // Checking skipped for
                //  - event id: it is the autogenerated primary key
                //  - position id: would require comparing against the
                //    position table, but we get the position id as
                //    the last value of the position table.
                //  - event_time: autogenerated timestamp.
                int col = 2;
                Assert.assertEquals(ev.myEngine, c.getLong(col++));
                Assert.assertEquals(ev.mySailplan, c.getLong(col++));
                c.moveToNext();
            }
        }
        finally {
            c.close();
        }
    }

    public void testInsertEventsNoPositions() {
        insertEvents();

        SQLiteDatabase db = dbif.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT(position_id), COUNT(*) " +
                               "FROM event", null);
        try {
            Assert.assertTrue(c.moveToFirst());
            Assert.assertEquals(1, c.getCount());

            Assert.assertEquals(0, c.getLong(0));
            Assert.assertEquals(eventS.length, c.getLong(1));
        }
        finally {
            c.close();
        }
    }

    public void testCumulativeDistanceUpdates() {
        double totalDistance = 0;

        for (PositionContainer pos: posS) {
            dbif.insertPosition(pos.myLat,
                                pos.myLong,
                                pos.myBearing,
                                QuantityFactory.metersPerSecond(pos.mySpeed),
                                pos.myDistanceFromPrev,
                                pos.myAccuracy);

            totalDistance += pos.myDistanceFromPrev.num();

            TripStats ts = dbif.getTripStats();
            Assert.assertEquals(totalDistance,
                                ts.distance.num(), 1.0);
        }
    }

    public void testSailAndEngineStats() {
        double totalSailingTime = 0;
        double totalEngineTime = 0;
        double interval = 1;
        Date eventTimestamp = new Date();

        int[][] eventValues = {
            { 0, 3 },
            { 1, 0 },
            { 1, 1 }
        };

        for (int i = 0; i < eventValues.length; ++i) {
            for (int j = 0; j < 3; ++j) {

                int engineStatus = eventValues[i][0];
                int sailConfiguration = eventValues[i][1];

                dbif.insertEvent(engineStatus, sailConfiguration);
                dbif.setPreviousEventTimeForTesting(eventTimestamp);

                if (0 != sailConfiguration) {
                    totalSailingTime += interval;
                }
                if (0 != engineStatus) {
                    totalEngineTime += interval;
                }

                interval += 1;
                // TODO: Here the time stamps of the events go backwards. 
                // Should probably fix.
                eventTimestamp.setTime(new Date().getTime() -
                                       (long) (interval) * 1000);
            }

            TripStats ts = dbif.getTripStats();
            Assert.assertEquals(totalSailingTime,
                                ts.sailingTime, 1.0);
            Assert.assertEquals(totalEngineTime,
                                ts.engineTime);
        }
        
        SQLiteDatabase db = dbif.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT engine, sailplan " +
                               "FROM event " +
                               "ORDER BY event_id",
                               null);
        
        Assert.assertEquals(eventValues.length, c.getCount());
        
        try {
            Assert.assertTrue(c.moveToFirst());
            for (int i = 0; i < eventValues.length; ++i) {
                int engineStatus = eventValues[i][0];
                int sailConfiguration = eventValues[i][1];
                
                Assert.assertEquals(engineStatus, 
                                    c.getInt(0));
                Assert.assertEquals(sailConfiguration,
                                    c.getInt(1));
                c.moveToNext();
            }
        }
        finally {
            c.close();
        }
    }

    public void testEventStats() {
        TripStats ts = dbif.getTripStats();
        
        Assert.assertNull(ts.firstEntry);
        Assert.assertNull(ts.lastEntry);
        
        insertEvents();
        
        Date now = new Date();
        ts = dbif.getTripStats();
        Assert.assertEquals(ts.firstEntry.getTime(), ts.lastEntry.getTime());
        Assert.assertTrue(now.getTime() - ts.firstEntry.getTime() < 1000);

        doSleep(1001);
        
        insertPositions();

        Date now2 = new Date();
        ts = dbif.getTripStats();
        Assert.assertTrue(ts.lastEntry.getTime() > ts.firstEntry.getTime());
        Assert.assertTrue(now2.getTime() - ts.lastEntry.getTime() < 1000);
    }
    
    public void testDbExport() {
        File dbCopy = null;

        try {
            insertPositionsAndEvents();

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
            Assert.assertEquals(String.format("The sizes of the original (%s) " +
                                              "and db copy (%s) differ. ",
                                              origDb.getAbsolutePath(),
                                              ef.fileName()),
                                origDb.length(), dbCopy.length());
            Assert.assertTrue("The database copy is empty", dbCopy.length() > 0);
        }
        catch (Exception ex) {
            Assert.fail("Got exception + " + ex.toString());
        }
        finally {
            if (null != dbCopy) {
                dbCopy.delete();
            }
        }
    }

    private void insertPositionsAndEvents() {
        insertPositions();
        insertEvents();
    }

    private void insertPositions() {
        for (PositionContainer pos: posS) {
            dbif.insertPosition(pos.myLat,
                                pos.myLong,
                                pos.myBearing,
                                pos.mySpeed,
                                pos.myDistanceFromPrev,
                                pos.myAccuracy);
        }
    }

    private void insertEvents() {
        for (EventContainer ev: eventS) {
            dbif.insertEvent(ev.myEngine,
                             ev.mySailplan);
        }
    }

    protected SQLiteDatabase getWritableDatabase() {
        return dbif.getWritableDatabase();
    }

    private TrackDB dbif;

    // Note, it is assumed in this class that the posS and eventS arrays
    // are of the same length.
    private PositionContainer[] posS = {
        new PositionContainer(60.1, 24.9, 350,
                              QuantityFactory.metersPerSecond(4),
                              10.0, QuantityFactory.meters(0.0)),
        new PositionContainer(60.6, 25.1, 25.0,
                              QuantityFactory.metersPerSecond(4.2),
                              15.0, QuantityFactory.meters(1000)),
        new PositionContainer(60.0, 25.0, 180,
                              QuantityFactory.metersPerSecond(5.2),
                              30.0, QuantityFactory.meters(3000)),
    };

    private EventContainer[] eventS = {
        new EventContainer(1, 0),
        new EventContainer(0, 2),
    };
}
