package com.ja.saillog.quantity.unit;

public abstract class UnitFactory {

    // Distances
    public static DistanceUnit meters() {
        return new DistanceUnit(1, "m");
    }
    
    public static DistanceUnit nauticalMiles() {
        return new DistanceUnit(0.000539956803, "nm");
    }
    
    // Speeds
    public static SpeedUnit metersPerSecond() {
        return new SpeedUnit(1, "m/s");
    }
    
    public static SpeedUnit knots() {
        return new SpeedUnit(1.94384449, "kn");
    }
}
