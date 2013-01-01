package com.ja.saillog.quantity.quantity;

import com.ja.saillog.quantity.unit.Unit;

public class Coordinate extends Quantity {
    public Coordinate(double rawValue, Unit unit) {
        super(rawValue, unit);
    }
    
    public Coordinate(Coordinate value, Unit newUnit) {
        super(value, newUnit);
    }
}
