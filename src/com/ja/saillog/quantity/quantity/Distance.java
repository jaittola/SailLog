package com.ja.saillog.quantity.quantity;

import com.ja.saillog.quantity.unit.Unit;

public class Distance extends Quantity {

    public Distance(double rawValue, Unit unit) {
        super(rawValue, unit);
    }

    public Distance(Distance value, Unit newUnit) {
        super(value, newUnit);
    }
}
