package com.ja.saillog.test.android;

import java.io.IOException;

import junit.framework.Assert;

import android.test.AndroidTestCase;

import com.ja.saillog.database.TrackDB;
import com.ja.saillog.utilities.ExportFile;

public class TestKMLExport extends AndroidTestCase {
    // This is a manually runnable test case for now, because
    // the test data needs to be supplied to the device.
    // Alternatively we could generate the test data.
    // Hence private.
    private void testKMLExport1() {
        String inputDbFile = "/mnt/sdcard/sle-koe-2.db";
        
        TrackDB tdb = new TrackDB(mContext, inputDbFile);
        ExportFile ef = new ExportFile("kml");
        
        try {
            tdb.exportDbAsKML(ef);
        }
        catch (IOException ex) {
            Assert.fail("Got IOException: " + ex.toString());
        }
        finally {
            tdb.close();
        }
    }
}
