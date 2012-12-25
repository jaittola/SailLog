package com.ja.saillog.quantity.unit;

public class HourMinSec extends TimeUnit {

    public HourMinSec(long unitId) {
        super(1, "", unitId);
    }

    public String applyConversionWithUnit(double baseUnitValue) {
        long[] hms = hourMinSec(baseUnitValue);
        
        StringBuffer resultBuff = 
                new StringBuffer(writeVal(hms[0], "h", conditional))
                .append(writeVal(hms[1], "min", conditional))
                .append(writeVal(hms[2], "s", unconditional));
        
        return resultBuff.toString().trim();
    }
    
    public String applyConversionWithoutUnit(double baseUnitValue) {
        // Do not use this.
        return "";
    }
    
    private long[] hourMinSec(double value) {
        long[] result = new long[3];
        
        // seconds
        result[2] = (long) (value % 60.0);
        
        double minutes = value / 60.0;
        result[1] = (long) (minutes % 60.0);
        
        result[0] = ((long) minutes) / 60;
        
        return result;
    }
    
    private StringBuffer writeVal(long val, String unit, boolean isConditional) {
        StringBuffer resultBuffer = new StringBuffer();
        
        if (unconditional == isConditional ||
            0 != val) {
            resultBuffer = resultBuffer.append(" " + val + " " + unit);
        }
        
        return resultBuffer;
    }
    
    private static final boolean conditional = true;
    private static final boolean unconditional = false;
}
