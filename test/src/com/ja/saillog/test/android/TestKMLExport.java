package com.ja.saillog.test.android;

import java.io.IOException;

import junit.framework.Assert;

import android.test.AndroidTestCase;

import com.ja.saillog.database.TrackDB;
import com.ja.saillog.utilities.ExportFile;
import com.ja.saillog.utilities.SailPlan;

public class TestKMLExport extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        // Eventually this configuration should be stored in the 
        // database. But as we do not yet have a configurable
        // sail plan, we can have it hard-coded like this.
        
        SailPlan.addSail("Main");
        SailPlan.addSail("Jib");
        SailPlan.addSail("Spinnaker");
    }
    
    @Override
    protected void tearDown() throws Exception {
        SailPlan.clearSails();
    }
    
    // This is a manually runnable test case for now, because
    // the test data needs to be supplied to the device.
    // Alternatively we could generate the test data.
    // Hence private.
    // TODO TODO
    public void testKMLExport1() {
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
