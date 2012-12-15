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
        sink.updateLocation(60, 20, 2, 3, 15.2, 0);

        Assert.assertEquals(60.0, db.mLatitude);
        Assert.assertEquals(20.0, db.mLongitude);
        Assert.assertEquals(3.0, db.mBearing);
        Assert.assertEquals(2.0, db.mSpeed);
        Assert.assertEquals(15.2, db.mAccuracy);
        Assert.assertEquals(0.0, db.mDistanceFromPrevious);

        sink.updateLocation(61, 20, 2, 3, 15.2, 0);
        Assert.assertEquals(61.0, db.mLatitude);
        Assert.assertEquals(111420.0, db.mDistanceFromPrevious, 1.0);
    }

    private class DistanceData extends Object {
        public DistanceData(double lat1, double lon1,
                            double lat2, double lon2,
                            double expectedDistance) {
            this.lat1 = lat1;
            this.lon1 = lon1;
            this.lat2 = lat2;
            this.lon2 = lon2;
            this.expectedDistance = expectedDistance;
        }

        public double lat1;
        public double lon1;
        public double lat2;
        public double lon2;
        public double expectedDistance;
    };

    public void testDistanceCalculation() {
        DistanceData [] testData = {
            new DistanceData(60.0, 25.0, 60.0, 25.0, 0.0),
            new DistanceData(Double.NaN, 25.0, 60.0, 25.0, 0.0),
            new DistanceData(60.0, Double.NaN, 60.0, 25.0, 0.0),
            new DistanceData(60.0, 25.0, Double.NaN, 25.0, 0.0),
            new DistanceData(60.0, 25.0, 60.0, Double.NaN, 0.0),
            new DistanceData(61.0, 25.0, 60.0, 25.0, 111420),
        };

        for (int i = 0; i < testData.length; ++i) {
            double dist = sink.calculateDistance(testData[i].lat1, testData[i].lon1,
                                                 testData[i].lat2, testData[i].lon2);
            Assert.assertEquals("Row " + i, testData[i].expectedDistance, dist, 1.0);
        }
    }

    private FakeTrackDB db = new FakeTrackDB();
    private DBLocationSink sink;
}
