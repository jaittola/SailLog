package com.ja.saillog.test.purejava;

import junit.framework.TestCase;

import com.ja.saillog.LocationFilter;

public class TestLocationFilter extends TestCase {
    
    // The time difference after which a new location update always occurs.
    // 5 min at the moment.
    private static final long timeDifferent = ((5 * 60 * 1000) + 1);
    // Time not different enough for a new update.
    private static final long timeAlmostSame = 1 * 60 * 1000;
    
    private static final double firstLatitude = 60.0;
    private static final double firstLongitude = 25.0;
    private static final double firstSpeed = 2.0;
    private static final double firstBearing = 30;
    private static final long firstLocationTime = 6000000;

    // This location is just outside the 100m border
    private static final double slightlyDifferentLatitude = 60.000658;
    private static final double slightlyDifferentLongitude = 25.001456;
    
    private LocationFilter filter;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        filter = new LocationFilter();
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed, 
                                    firstBearing, firstLocationTime));
    }
    
    public void testDifferentFix() {
        assertTrue(filter.canUpdate(firstLatitude + 1, firstLongitude + 1, firstSpeed + 1, 
                                    firstBearing + 20, firstLocationTime + 1));
    }
    
    public void testSameLocation() {
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                     firstBearing, firstLocationTime + timeAlmostSame));
    }
    
    public void testSameLocationDifferentTime() {
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    firstBearing, firstLocationTime + timeDifferent));
    }

    public void testDifferentLocationBorder1() {
        assertTrue(filter.canUpdate(firstLatitude + 0.001, firstLongitude, firstSpeed,
                                    firstBearing, firstLocationTime + timeAlmostSame));
    }
  
    public void testDifferentLocationBorder2() {
        assertFalse(filter.canUpdate(firstLatitude + 0.0008, firstLongitude, firstSpeed,
                                    firstBearing, firstLocationTime + timeAlmostSame));
    }
    
    public void testDifferenLocationBorder3() {
        // This position should fall just outside the 100 meter mark.
        assertTrue(filter.canUpdate(slightlyDifferentLatitude, slightlyDifferentLongitude, firstSpeed,
                                    firstBearing, firstLocationTime + timeAlmostSame));
    }
  
    public void XtestDifferenLocationBorder4() {
        // This shows how the calculation is incorrect. To be fixed.
        // Should use the mean latitude and determine what is the longitude
        // scale at that particular latitude. TODO.        
        assertFalse(filter.canUpdate(firstLatitude, 25.001, firstSpeed,
                                     firstBearing, firstLocationTime + timeAlmostSame));
    }
    
    public void testCloseDifferentBearing() {
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    firstBearing + 5, firstLocationTime + timeAlmostSame));
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                     firstBearing + 2, firstLocationTime + timeAlmostSame));
    }
    
    public void testBearingsAround360_1() {
        // Successive bearing changes around the 360 degree discontinuity.
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    359, firstLocationTime + timeAlmostSame));
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                     2, firstLocationTime + timeAlmostSame));
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                     5, firstLocationTime + timeAlmostSame));
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    0, firstLocationTime + timeAlmostSame));      
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    355, firstLocationTime + timeAlmostSame));      
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, firstSpeed,
                                    0, firstLocationTime + timeAlmostSame));      
    }
    
    public void testDifferentSpeeds() {
        // Successive speed changes.
        double speed2 = 1.06 * firstSpeed;
        double speed3 = 0.94 * speed2;
        double speed4 = 0.98 * speed3;
        
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, speed2, 
                                    firstBearing, firstLocationTime + timeAlmostSame));
        assertTrue(filter.canUpdate(firstLatitude, firstLongitude, speed3, 
                                    firstBearing, firstLocationTime + timeAlmostSame));
        assertFalse(filter.canUpdate(firstLatitude, firstLongitude, speed4, 
                                    firstBearing, firstLocationTime + timeAlmostSame));
    }
}
