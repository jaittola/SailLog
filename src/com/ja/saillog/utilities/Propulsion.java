
package com.ja.saillog.utilities;

import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import android.util.Pair;


public class Propulsion {
    public Propulsion() {
    }
    
    public Propulsion(long numericalSailPlan,
                      boolean engineStatus) {
        setPropulsion(numericalSailPlan, engineStatus);
    }

    public Propulsion(Bundle bundle) {
        setPropulsion(bundle.getLong(sailPlanBundleKey),
                      bundle.getBoolean(engineStatusBundleKey));
    }
    
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(engineStatusBundleKey, engineStatus);
        bundle.putLong(sailPlanBundleKey, sailPlan);
        
        return bundle;
    }
    
    
    /**
     * Compares this propulsion object to another. 
     */
    public boolean equals(Propulsion other) {
        if (this.sailPlan == other.sailPlan &&
            this.engineStatus == other.engineStatus) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns a sail id that must be used when updating the sail plan.
     *
     * @param name must be a valid string that represents a sail.
     *
     * Max 30 sails supported.
     */
    public static int addSail(String name) {
        if (sailNames.size() >= (maxSails - 1)) {
            return -1;  // Hmm?
        }

        sailNames.add(name);
        return sailNames.size() - 1; // The position in the list.
    }

    /**
     * Wipe the sail configuration. Useful for tests at least.
     */
    public static void clearSails() {
        sailNames.clear();
    }

    /**
     * Configure the generic description strings about the sail plan.
     */
    public static void configure(String sailingS,
                                 String motorsailingS,
                                 String motoringS,
                                 String driftingS,
                                 String upStringS,
                                 String downStringS) {
        sailing = sailingS;
        motorsailing = motorsailingS;
        motoring = motoringS;
        drifting = driftingS;
        upString = upStringS;
        downString = downStringS;
    }

    /**
     * A sail has been hoisted or taken down.
     */
    public void setSail(int sailId, boolean isUp) {
        if (sailId >= sailNames.size()) {
            return;
        }

        if (up == isUp) {
            sailPlan |= (1 << sailId);
        }
        else if (down == isUp) {
            sailPlan &= (0xffff ^ (1 << sailId));
        }
    }
    
    /**
     * Set engine status.
     */
    public void setEngine(boolean onOff) {
        engineStatus = onOff;
    }

    /**
     * Get engine status.
     */
    public boolean getEngine() {
        return engineStatus;
    }
    
    /**
     * Get a numerical representation of the current sail plan.
     *
     * This value will typically be stored in the database.
     */
    public long getSailPlan() {
        return sailPlan;
    }

    /**
     * Set the current sail plan. This method can be used to turn a saved
     * sail plan into string format.
     *
     * @param numericalSailPlan is typically stored in database.
     */
    public void setSailPlan(long numericalSailPlan) {
        this.sailPlan = numericalSailPlan;
    }

    /**
     * Set the current sail plan and engine status. 
     * 
     * This method can be used to turn a saved
     * sail plan into string format.
     */
    public void setPropulsion(long numericalSailPlan,
                              boolean engineOnOff) {
        this.sailPlan = numericalSailPlan;
        this.engineStatus = engineOnOff;
    }
    
    /**
     * Get a string representation of the current sail plan.
     */
    public List<Pair<String, String>> currentSails() {
        LinkedList<Pair<String, String>> sails =
            new LinkedList<Pair<String, String>>();

        int idx = 0;
        for (String sail: sailNames) {
            sails.add(new Pair<String, String>(sail,
                                               (0 != (sailPlan & (1 << idx)) ?
                                                upString : downString)));
            ++idx;
        }

        return sails;
    }

    /**
     * Returns a generic description of the current sail plan.
     *
     * Alternatives:
     * "sailing", "motorsailing", "motoring".
     */
    public String generalDescription() {
        if (engineStatus) {
            if (0 != sailPlan) {
                return motorsailing;
            }
            return motoring;
        }
        if (0 != sailPlan) {
            return sailing;
        }
        return drifting;
    }

    /**
     * Constants to be used for up/down status of sail.
     */
    public static final boolean up = true;
    public static final boolean down = false;

    /**
     * Constants for engine on/off.
     */
    public static final boolean engineOn = true;
    public static final boolean engineOff = false;

    private long sailPlan = 0;
    private boolean engineStatus = engineOff;

    private static LinkedList<String> sailNames = new LinkedList<String>();
    private static int maxSails = 30;

    private static String sailing = "sailing";
    private static String motorsailing = "motorsailing";
    private static String motoring = "motoring";
    private static String drifting = "drifting";
    private static String upString = "up";
    private static String downString = "down";
    
    // Public for test access to these values.
    public static final String engineStatusBundleKey = "engineStatus";
    public static final String sailPlanBundleKey = "sailPlan"; 
}
