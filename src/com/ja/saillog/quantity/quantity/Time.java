package com.ja.saillog.quantity.quantity;

import com.ja.saillog.quantity.unit.Unit;

public class Time extends Quantity {

    public Time(double value, Unit unit) {
        super(value, unit);
    }

    public Time(Quantity value, Unit newUnit) {
        super(value, newUnit);
    }

}
