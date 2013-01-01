package com.ja.saillog.utilities;

import com.ja.saillog.R;

import android.app.Activity;

/**
 * This class is a bit funny but it helps in avoiding the passing
 * of activities to certain parts of the code.
 * 
 * Also using this class we can keep some ugly initialization
 * code away from the UI classes.
 */

public abstract class StaticStrings {
    static public void setup(Activity activity) {
        engineS = activity.getString(R.string.engine);
        onS = activity.getString(R.string.on);
        offS = activity.getString(R.string.off);
        
        SailPlan.configure(activity.getString(R.string.sailing),
                           activity.getString(R.string.motorsailing),
                           activity.getString(R.string.motoring),
                           activity.getString(R.string.no_sail_plan),
                           activity.getString(R.string.sail_up),
                           activity.getString(R.string.sail_down));
    }
    
    static public String engine() {
        return engineS;
    }
    
    static public String on() {
        return onS;
    }
    
    static public String off() {
        return offS;
    }
    
    static private String engineS = "Engine";
    static private String onS = "On";
    static private String offS = "Off";
}
