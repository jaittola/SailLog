package com.ja.saillog.test.purejava;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.ja.saillog.LocationFormatter;

public class TestLocationFormatter extends TestCase {
	public void testLatitudeFormatting() {
		// Normal values, north hemisphere.
		Assert.assertEquals("N 60¡ 0.0'", LocationFormatter.formatLatitude(60.0));
		Assert.assertEquals("N 20¡ 12.0'", LocationFormatter.formatLatitude(20.2));

		// Normal values, south hemisphere.
		Assert.assertEquals("S 50¡ 0.0'", LocationFormatter.formatLatitude(-50.0));
		Assert.assertEquals("S 10¡ 10.0'", LocationFormatter.formatLatitude(-10.1666666));
		
		// Out of range.
		Assert.assertEquals("", LocationFormatter.formatLatitude(91));
		Assert.assertEquals("", LocationFormatter.formatLatitude(-91));

		// Boundaries.
		Assert.assertEquals("N 0¡ 0.0'", LocationFormatter.formatLatitude(0.0));
		Assert.assertEquals("N 90¡ 0.0'", LocationFormatter.formatLatitude(90.0));		
		Assert.assertEquals("S 90¡ 0.0'", LocationFormatter.formatLatitude(-90.0));
	}
	
	public void testLongitudeFormatting() {
		// Normal values, east hemisphere.
		Assert.assertEquals("E 25¡ 0.0'", LocationFormatter.formatLongitude(25.0));
		Assert.assertEquals("E 10¡ 24.0'", LocationFormatter.formatLongitude(10.4));

		// Normal values, west hemisphere.
		Assert.assertEquals("W 71¡ 0.0'", LocationFormatter.formatLongitude(-71.0));
		Assert.assertEquals("W 100¡ 10.0'", LocationFormatter.formatLongitude(-100.1666666));
		
		// Out of range.
		Assert.assertEquals("", LocationFormatter.formatLongitude(181));
		Assert.assertEquals("", LocationFormatter.formatLongitude(-181));

		// Boundaries.
		Assert.assertEquals("E 0¡ 0.0'", LocationFormatter.formatLongitude(0.0));
		Assert.assertEquals("E 180¡ 0.0'", LocationFormatter.formatLongitude(180.0));		
		Assert.assertEquals("W 180¡ 0.0'", LocationFormatter.formatLongitude(-180.0));
	}
}

