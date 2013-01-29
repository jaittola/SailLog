package com.ja.saillog.quantity.quantity;

import com.ja.saillog.quantity.unit.Unit;

public class Speed extends Quantity {
    public Speed(double rawValue, Unit unit) {
        super(rawValue, unit);
    }

    public Speed(Speed value, Unit newUnit) {
        super(value, newUnit);
    }
}
