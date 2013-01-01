package com.ja.saillog.quantity.quantity;

import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.Speed;
import com.ja.saillog.quantity.quantity.Coordinate;
import com.ja.saillog.quantity.unit.UnitFactory;

public abstract class QuantityFactory {
    // Distances
    public static Distance meters(double value) {
        return new Distance(value, UnitFactory.meters);
    }

    public static Distance meters(Distance value) {
        if (value.hasSameUnit(UnitFactory.meters)) {
            return value;
        }
        return new Distance(value, UnitFactory.meters);
    }

    public static Distance nauticalMiles(double value) {
        return new Distance(value, UnitFactory.nauticalMiles);
    }

    public static Distance nauticalMiles(Distance value) {
        if (value.hasSameUnit(UnitFactory.nauticalMiles)) {
            return value;
        }
        return new Distance(value, UnitFactory.nauticalMiles);
    }

    // Speeds
    public static Speed metersPerSecond(double value) {
        return new Speed(value, UnitFactory.metersPerSecond);
    }

    public static Speed metersPerSecond(Speed value) {
        if (value.hasSameUnit(UnitFactory.metersPerSecond)) {
            return value;
        }
        return new Speed(value, UnitFactory.metersPerSecond);
    }

    public static Speed knots(double value) {
        return new Speed(value, UnitFactory.knots);
    }

    public static Speed knots(Speed value) {
        if (value.hasSameUnit(UnitFactory.knots)) {
            return value;
        }
        return new Speed(value, UnitFactory.knots);
    }

    // Times
    // HourMinSec has no conversion to anything at the moment.
    public static Time hourMinSec(double value) {
        return new Time(value, UnitFactory.hourMinSec);
    }

    // Coordinates.
    // No conversions for coordinates either.
    public static Coordinate dmsLatitude(double value) {
        return new Coordinate(value, UnitFactory.dmsLatitude);
    }

    public static Coordinate dmsLongitude(double value) {
        return new Coordinate(value, UnitFactory.dmsLongitude);
    }
}
