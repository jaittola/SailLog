package com.ja.saillog.quantity.unit;

import java.util.Locale;

public class DegMinSec extends CoordinateUnit {

    public DegMinSec(String positiveUnit,
                     String negativeUnit,
                     long unitId) {
        super(1, "", unitId);

        this.positiveUnit = positiveUnit;
        this.negativeUnit = negativeUnit;
    }

    public String applyConversionWithUnit(double baseUnitValue) {
        double results[] = minSec(baseUnitValue);

        String s = String.format(Locale.getDefault(),
                                 "%s %.0f¡ %.0f' %.3f\"",
                                 (0 <= baseUnitValue ?
                                  positiveUnit : negativeUnit),
                                 Math.floor(Math.abs(baseUnitValue)),
                                 Math.floor(Math.abs(results[0])),
                                 Math.abs(results[1]));
        System.err.println("Resulting value is " + s);
        return s;
    }

    public String applyConversionWithoutUnit(double baseUnitValue) {
        return String.format(Locale.getDefault(),
                             "%s %.5f",
                             (0 >= baseUnitValue ?
                              positiveUnit : negativeUnit),
                             baseUnitValue);
    }

    private double[] minSec(double degrees) {
        double[] result = new double[2];

        double minutes = (degrees % 1.0) * 60.0;
        result[0] = minutes;

        // seconds
        double fraction = minutes % 1.0;
        result[1] = fraction * 60.0;

        return result;
    }

    private String positiveUnit;
    private String negativeUnit;
}
