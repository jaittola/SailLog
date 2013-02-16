package com.ja.saillog.test.android;

import java.io.IOException;

import junit.framework.Assert;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ja.saillog.database.KMLExporter;
import com.ja.saillog.database.TrackDB;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.utilities.ExportFile;
import com.ja.saillog.utilities.Propulsion;

public class TestKMLExport extends TestDbBase {
    @Override
    protected void setUp() throws Exception {
        
        super.setUp();
        
        // Eventually this configuration should be stored in the 
        // database. But as we do not yet have a configurable
        // sail plan, we can have it hard-coded like this.
        
        Propulsion.addSail("Main");
        Propulsion.addSail("Jib");
        Propulsion.addSail("Spinnaker");
        
        db = new TrackDB(mContext, "SLDB-test-export.db");
    }
    
    @Override
    protected void tearDown() throws Exception {
        Propulsion.clearSails();
        
        super.tearDown();
    }
    
    private class TrackDataItem {
        public TrackDataItem(Double lat, 
                             Double lon, 
                             Integer sails, 
                             Integer engine) {
            this.lat = lat;
            this.lon = lon;
            this.sails = sails;
            this.engine = engine;
        }
        
        public Double lat;
        public Double lon;
        public Integer sails;
        public Integer engine;
    }
    
    private TrackDataItem[] trackDataItems = {
            new TrackDataItem(60.0, 25.0, 0, 1),
            new TrackDataItem(60.1, 25.1, null, null),
            new TrackDataItem(60.2, 25.2, 1, 0),
            new TrackDataItem(60.3, 25.3, null, null),
            new TrackDataItem(60.4, 25.4, 0, 0),
    };
    
    private void checkSame(Integer value, Cursor c, int cursorColumn) {
        if (null == value) {
            Assert.assertTrue(c.isNull(cursorColumn));
        }
        else {
            Assert.assertEquals(value.intValue(), c.getInt(cursorColumn));
        }
    }
    
    public void testTrackLineExportQuery() {
        insertTrackAndEvents();
        
        KMLExporter xp = new KMLExporter();
        Cursor cursor = xp.getPositionQuery(db.getReadableDatabase());
        
        try {
            for (int i = 0; i < trackDataItems.length; ++i) {
                Assert.assertTrue(cursor.moveToNext());
                Assert.assertEquals(trackDataItems[i].lat, cursor.getDouble(1));
                Assert.assertEquals(trackDataItems[i].lon, cursor.getDouble(2));
                
                checkSame(trackDataItems[i].sails, cursor, 5);
                checkSame(trackDataItems[i].engine, cursor, 6);
            }
        } finally {
            cursor.close();
        }
    }
        
    private void testExportGeneratedData() {
        ExportFile ef = new ExportFile("kml");
        
        try {
            new KMLExporter().export(db.getReadableDatabase(), ef);
        } catch (IOException ex) {
            Assert.fail("Caught IOException: " + ex.toString());
        }
    }
    
    // Note that  this data is completely nonsense. The distance is off 
    // and so on.
    protected void insertTrackAndEvents() {
        
        for (TrackDataItem tdi: trackDataItems) {
            db.insertPosition(tdi.lat.doubleValue(), 
                              tdi.lon.doubleValue(), 
                              0,
                              QuantityFactory.knots(0), 
                              QuantityFactory.meters(0), 1.0);
            if (null != tdi.sails && null != tdi.engine) {
                db.insertEvent(tdi.sails.intValue(),
                               tdi.engine.longValue());
            }
        }
    }
    
    // This is a manually runnable test case for now, because
    // the test data needs to be supplied to the device.
    // Alternatively we could generate the test data.
    // Hence private.
    private void testKMLExport1() {
        String inputDbFile = "/mnt/sdcard/sle-koe-2.db";
        
        TrackDB tdb = new TrackDB(mContext, inputDbFile);
        ExportFile compressedEf = new ExportFile("kml", "kmz");
        ExportFile plainEf = new ExportFile("kml");
        
        try {
            tdb.exportDbAsKML(compressedEf);
            tdb.exportDbAsKML(plainEf);
        }
        catch (IOException ex) {
            Assert.fail("Got IOException: " + ex.toString());
        }
        finally {
            tdb.close();
        }
    }
    
    protected SQLiteDatabase getWritableDatabase() {
        return db.getWritableDatabase();
    }
    
    protected TrackDB db = null;
}
