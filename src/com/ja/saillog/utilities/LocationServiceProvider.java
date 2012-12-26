package com.ja.saillog.utilities;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

public class LocationServiceProvider {

    protected LocationServiceProvider(Activity activity) {
        if (null == locationManager) {
            locationManager = (LocationManager)
                activity.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void requestUpdates(LocationListener listener) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                               locationMinimumInterval,
                                               locationMinimumDistance,
                                               listener);
    }

    public void stopUpdates(LocationListener listener) {
        locationManager.removeUpdates(listener);
    }

    // Below is the static part of the class, which 
    // permits the tests to insert their own providers
    // to replace this class.
    public static LocationServiceProvider get(Activity activity) {
        setDefaultProvider(activity);
        return provider;
    }

    private static void setDefaultProvider(Activity activity) {
        if (null == provider) {
            provider = new LocationServiceProvider(activity);
        }
    }

    public static void setProvider(LocationServiceProvider prov) {
        provider = prov;
    }

    private LocationManager locationManager;

    private static LocationServiceProvider provider;

    private static final int locationMinimumInterval = 2000; // ms
    private static final int locationMinimumDistance = 0; // meters.
}
