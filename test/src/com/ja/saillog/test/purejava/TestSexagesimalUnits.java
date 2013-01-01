package com.ja.saillog.test.purejava;

import com.ja.saillog.quantity.quantity.QuantityFactory;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestSexagesimalUnits extends TestCase {
    public void testSecsOnly() {
        double val = 35.1;
        Assert.assertEquals("35 s",
                            QuantityFactory.hourMinSec(val).withUnit());
    }

    public void testMinSec() {
        double val = 97.8;
        Assert.assertEquals("1 min 37 s",
                            QuantityFactory.hourMinSec(val).withUnit());
    }

    public void testMinSec2() {
        double val = 127;
        Assert.assertEquals("2 min 7 s",
                            QuantityFactory.hourMinSec(val).withUnit());
    }

    public void testHourMinSec() {
        double val = 2 * 3600 + 3 * 60 + 20;
        Assert.assertEquals("2 h 3 min 20 s",
                            QuantityFactory.hourMinSec(val).withUnit());
    }

    public void testDegMinSec1() {
        double lat = 60 + 8.0/60.0 + 57.4/3600.0;
        double lon = 24 + 57/60.0 + 54.7/3600.0;

        Assert.assertEquals("N 60¡ 8' 57.400\"",
                            QuantityFactory.dmsLatitude(lat).withUnit());
        Assert.assertEquals("E 24¡ 57' 54.700\"",
                            QuantityFactory.dmsLongitude(lon).withUnit());

    }

    public void testDegMinSec2() {
        double lat = -20 - 9/60.0 - 20.0/3600.0;
        double lon = -50 - 30/60.0 - 12.7/3600.0;

        Assert.assertEquals("S 20¡ 9' 20.000\"",
                            QuantityFactory.dmsLatitude(lat).withUnit());
        Assert.assertEquals("W 50¡ 30' 12.700\"",
                            QuantityFactory.dmsLongitude(lon).withUnit());
    }
}
