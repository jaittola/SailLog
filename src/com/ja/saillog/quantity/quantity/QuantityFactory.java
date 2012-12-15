package com.ja.saillog.quantity.quantity;

import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.Speed;
import com.ja.saillog.quantity.unit.UnitFactory;

public abstract class QuantityFactory {
    // Distances
    public static Distance meters(double value) {
        return new Distance(value, UnitFactory.meters());
    }
    
    public static Distance meters(Distance value) {
        return new Distance(value, UnitFactory.meters());
    }
    
    public static Distance nauticalMiles(double value) {
        return new Distance(value, UnitFactory.nauticalMiles());
    }
    
    public static Distance nauticalMiles(Distance value) {
        return new Distance(value, UnitFactory.nauticalMiles());
    }
    
    // Speeds
    public static Speed metersPerSecond(double value) {
        return new Speed(value, UnitFactory.metersPerSecond());
    }
    
    public static Speed knots(double value) {
        return new Speed(value, UnitFactory.knots());
    }

}
