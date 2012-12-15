package com.ja.saillog.quantity.unit;

public abstract class Unit {
    public Unit(double conversionFactorFromBaseUnit, String unitText) {
        this.conversionFactorFromBaseUnit = conversionFactorFromBaseUnit;
        this.unitText = unitText;
    }
    
    public String applyConversionWithUnit(double baseUnitValue) {
        return String.format("%.1f %s", 
                             applyConversion(baseUnitValue),
                             unitText);
    }
    
    public String applyConversionWithoutUnit(double baseUnitValue) {
        return String.format("%.1f",
                             applyConversion(baseUnitValue));
    }
    
    public double applyConversion(double baseUnitValue) {
        return baseUnitValue * conversionFactorFromBaseUnit;
    }
    
    protected double conversionFactorFromBaseUnit;
    protected String unitText;
}
