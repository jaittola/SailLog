package com.ja.saillog.quantity.unit;

import java.util.Locale;

public class HourMinSec extends TimeUnit {

    public HourMinSec(long unitId) {
        super(1, "", unitId);
    }

    public String applyConversionWithUnit(double baseUnitValue) {
        long[] hms = hourMinSec(baseUnitValue);

        return String.format("%s%s%s",
                             writeVal(hms[0], "h", conditional, withSpace),
                             writeVal(hms[1], "min", conditional, withSpace),
                             writeVal(hms[2], "s", unconditional, noSpace));
    }

    public String applyConversionWithoutUnit(double baseUnitValue) {
        // Do not use this.
        return "";
    }

    private long[] hourMinSec(double seconds) {
        long[] result = new long[3];

        // seconds
        result[2] = (long) (seconds % 60.0);

        double minutes = seconds / 60.0;
        result[1] = (long) (minutes % 60.0);

        result[0] = (long) (minutes / 60.0);

        return result;
    }

    private String writeVal(long val,
                            String unit,
                            boolean isConditional,
                            boolean appendSpace) {
        if (0 != val ||
            unconditional == isConditional) {
            return String.format(Locale.getDefault(),
                                 "%d %s%s",
                                 val,
                                 unit,
                                 (withSpace == appendSpace ? " ": ""));
        }
        
        return "";
    }

    private static final boolean conditional = true;
    private static final boolean unconditional = false;
    private static final boolean withSpace = true;
    private static final boolean noSpace = false;
}
