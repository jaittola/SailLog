package com.ja.saillog.test.purejava;

import junit.framework.TestCase;

import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.quantity.quantity.Speed;
import com.ja.saillog.utilities.LocationFilter;

public class TestLocationFilter extends TestCase {

    // The time difference after which a new location update always occurs.
    // 5 min at the moment.
    private static final long timeDifferent = ((5 * 60 * 1000) + 1);
    // Time not different enough for a new update.
    private static final long timeAlmostSame = 1 * 60 * 1000;

    private static final double firstLatitude = 60.0;
    private static final double firstLongitude = 25.0;
    private static final Speed firstSpeed = QuantityFactory.metersPerSecond(2.0);
    private static final double firstBearing = 30;
    private static final long firstLocationTime = 6000000;
    private static final Distance zeroDistance = QuantityFactory.meters(0.0);

    private LocationFilter filter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        filter = new LocationFilter();
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    firstBearing, firstLocationTime, zeroDistance));
    }

    public void testDifferentFix() {
        assertTrue(filter.canUpdate(firstLatitude + 1, firstLongitude + 1, firstSpeed,
                                    firstBearing + 20, firstLocationTime + 1, zeroDistance));
    }

    public void testSameLocation() {
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                     firstBearing, firstLocationTime + timeAlmostSame, 
                                     zeroDistance));
    }

    public void testSameLocationDifferentTime() {
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    firstBearing, firstLocationTime + timeDifferent, 
                                    zeroDistance));
    }

    public void testDistanceGoodEnough() {
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    firstBearing, firstLocationTime, 
                                    QuantityFactory.meters(101)));
    }

    public void testDistanceBorderNotEnough() {
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                     firstBearing, firstLocationTime, 
                                     QuantityFactory.meters(99.999)));
    }

    public void testDistanceSamePlace() {
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                     firstBearing, firstLocationTime, 
                                     zeroDistance));
    }

    public void testCloseDifferentBearing() {
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    firstBearing + 5, firstLocationTime + timeAlmostSame, 
                                    zeroDistance));
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                     firstBearing + 2, firstLocationTime + timeAlmostSame, 
                                     zeroDistance));
    }

    public void testBearingsAround360_1() {
        // Successive bearing changes around the 360 degree discontinuity.
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    359, firstLocationTime + timeAlmostSame, zeroDistance));
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                     2, firstLocationTime + timeAlmostSame, zeroDistance));
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    5, firstLocationTime + timeAlmostSame, zeroDistance));
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    0, firstLocationTime + timeAlmostSame, zeroDistance));
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    355, firstLocationTime + timeAlmostSame, zeroDistance));
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    0, firstLocationTime + timeAlmostSame, zeroDistance));
    }

    public void testDifferentSpeeds() {
        // Successive speed changes.
        Speed speed2 = QuantityFactory.metersPerSecond(1.06 * firstSpeed.num());
        Speed speed3 = QuantityFactory.metersPerSecond(0.94 * speed2.num());
        Speed speed4 = QuantityFactory.metersPerSecond(0.98 * speed3.num());

        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, speed2,
                                    firstBearing, firstLocationTime + timeAlmostSame, 
                                    zeroDistance));
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, speed3,
                                    firstBearing, firstLocationTime + timeAlmostSame, 
                                    zeroDistance));
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, speed4,
                                     firstBearing, firstLocationTime + timeAlmostSame, 
                                     zeroDistance));
    }

    public void testSmallSpeeds() {
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, QuantityFactory.metersPerSecond(0),
                                    firstBearing, firstLocationTime, zeroDistance));
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, QuantityFactory.metersPerSecond(0.1),
                                     firstBearing, firstLocationTime, zeroDistance));
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, QuantityFactory.metersPerSecond(0.31),
                                    firstBearing, firstLocationTime, zeroDistance));
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, QuantityFactory.metersPerSecond(0.50),
                                     firstBearing, firstLocationTime, zeroDistance));
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, QuantityFactory.metersPerSecond(0.31),
                                     firstBearing, firstLocationTime, zeroDistance));
    }
}
