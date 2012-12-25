package com.ja.saillog.test.purejava;

import com.ja.saillog.quantity.quantity.QuantityFactory;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestHourMinSec extends TestCase {
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

    public void testHourMinSec() {
        double val = 2 * 60 * 60 + 200;
        Assert.assertEquals("2 h 3 min 20 s",
                            QuantityFactory.hourMinSec(val).withUnit());
    }
    
}
