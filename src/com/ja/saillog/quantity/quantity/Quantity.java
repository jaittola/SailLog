package com.ja.saillog.quantity.quantity;

import com.ja.saillog.quantity.unit.Unit;

public abstract class Quantity {
    
    /**
     * Initialization.
     *  @param value the numeric value
     *  @param unit the unit in which the value is in.
     */
    protected Quantity(double value, Unit unit) {
        this.unit = unit;
        this.valueInBaseUnit = unit.applyReverseConversion(value);
    }
    
    /**
     * Conversion from another unit.
     * @param quantity the value in another unit.
     * @param newUnit the new unit to use in the new value.
     */
    protected Quantity(Quantity value, Unit newUnit) {
        this.valueInBaseUnit = value.valueInBaseUnit;
        this.unit = newUnit;
    }
    
    public String stringValueWithUnit() {
        return unit.applyConversionWithUnit(valueInBaseUnit);
    }
    
    public String stringValueWithoutUnit() {
        return unit.applyConversionWithoutUnit(valueInBaseUnit);
    }
    
    /**
     * @return true if the unit of this quantity is the same as otherUnit.
     */
    public boolean hasSameUnit(Unit otherUnit) {
        if (otherUnit.getUnitId() == unit.getUnitId()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * @return the numerical value of this quantity in the 
     * unit contained in this class.
     */
    public double num() {
        return unit.applyConversion(valueInBaseUnit);
    }

    protected Unit unit = null;
    
    protected double valueInBaseUnit = Double.NaN;
}
