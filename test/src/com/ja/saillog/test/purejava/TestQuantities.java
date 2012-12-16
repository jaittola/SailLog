package com.ja.saillog.test.purejava;

import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.quantity.quantity.Speed;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestQuantities extends TestCase {

    private double msValue = 2.0;
    private double knotsValue = 3.88768898;
    private double metersValue = 1000;
    private double nmValue = 0.539956803;
    
    private Speed ms = null;
    
    @Override
    protected void setUp() throws Exception {
        ms = QuantityFactory.metersPerSecond(msValue);
    }
    
    public void testValueExtractMethods() {
        Assert.assertEquals(ms.num(), msValue);
        Assert.assertEquals(String.format("%.1f m/s", msValue),
                            ms.stringValueWithUnit());
        Assert.assertEquals(String.format("%.1f", msValue),
                            ms.stringValueWithoutUnit());       
    }
    
    public void testSameUnitConversion() {
        Speed ms2 = QuantityFactory.metersPerSecond(ms);
        
        Assert.assertEquals(ms.num(), ms2.num());
        Assert.assertEquals(ms.stringValueWithoutUnit(), 
                            ms2.stringValueWithoutUnit());        
        Assert.assertEquals(ms.stringValueWithUnit(), 
                            ms2.stringValueWithUnit());
    }
    
    public void testSpeedConversion() {
        Speed knots = QuantityFactory.knots(ms);
        Speed ms2 = QuantityFactory.metersPerSecond(knots);
        
        Assert.assertEquals(knotsValue, knots.num());
        Assert.assertEquals(ms.num(), ms2.num());
    }

    public void testNmConversion() {
        Distance m = QuantityFactory.meters(metersValue);
        Distance nm = QuantityFactory.nauticalMiles(m);
        Distance m_2 = QuantityFactory.meters(nm);
        
        Assert.assertEquals(metersValue, m_2.num());
        Assert.assertEquals(nmValue, nm.num());
    }
    
    public void testKnotsToMsConversion() {
        Speed kn = QuantityFactory.knots(2);
        Speed ms = QuantityFactory.metersPerSecond(kn);
        Assert.assertEquals(1.02888889, ms.num(), 0.0001);
    }
}
