package com.ja.saillog.quantity.unit;

public abstract class Unit {
    public Unit(double conversionFactorFromBaseUnit, String unitText, long unitId) {
        this.conversionFactorFromBaseUnit = conversionFactorFromBaseUnit;
        this.unitText = unitText;
        this.unitId = unitId;
    }
    
    public String applyConversionWithUnit(double baseUnitValue) {
        return String.format(floatConversion + " %s", 
                             applyConversion(baseUnitValue),
                             unitText);
    }
    
    public String applyConversionWithoutUnit(double baseUnitValue) {
        return String.format(floatConversion,
                             applyConversion(baseUnitValue));
    }
    
    public double applyConversion(double baseUnitValue) {
        return baseUnitValue * conversionFactorFromBaseUnit;
    }
    
    public double applyReverseConversion(double value) {
        return value / conversionFactorFromBaseUnit;
    }
    
    public long getUnitId() {
        return unitId;
    }
    
    protected double conversionFactorFromBaseUnit;
    protected String unitText;
    protected long unitId;
    private static final String floatConversion = "%.1f";
}
