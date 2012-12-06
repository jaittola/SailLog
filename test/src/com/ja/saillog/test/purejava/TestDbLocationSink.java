package com.ja.saillog.test.purejava;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.ja.saillog.DBLocationSink;

public class TestDbLocationSink extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();

        db = new FakeTrackDB();
        sink = new DBLocationSink(db);
    }

    public void testDbLocationSink() {
        sink.updateLocation(20, 60, 2, 3, 0);

        Assert.assertEquals(20.0, db.mLatitude);
        Assert.assertEquals(60.0, db.mLongitude);
        Assert.assertEquals(3.0, db.mBearing);
        Assert.assertEquals(2.0, db.mSpeed);
    }

    private FakeTrackDB db = new FakeTrackDB();
    private DBLocationSink sink;
}
