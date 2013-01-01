package com.ja.saillog.quantity.unit;

public abstract class UnitFactory {

    // Distances
    public static final DistanceUnit meters = new DistanceUnit(1, "m", 1L);
    public static final DistanceUnit nauticalMiles = new DistanceUnit(0.000539956803, "nm", 2L);

    // Speeds
    public static final SpeedUnit metersPerSecond = new SpeedUnit(1, "m/s", 3L);
    public static final SpeedUnit knots = new SpeedUnit(1.94384449, "kn", 4L);

    // Time, hour / minute / second.
    public static final TimeUnit hourMinSec = new HourMinSec(5L);

    // Coordinates.
    public static final CoordinateUnit dmsLatitude = new DegMinSec("N", "S",
                                                                   6L);
    public static final CoordinateUnit dmsLongitude = new DegMinSec("E", "W",
                                                                    6L);
}
