package com.ja.saillog;

import android.content.Context;

/*!
 * Provides a factory for Database objects.
 *
 * Needed for injecting fake Database objects to tests.
 */
public class DBProvider {
    protected TripDBInterface getTripDBInstance(Context context) {
        return new TripDB(context);
    }

    protected TrackDBInterface getTrackDBInstance(Context context, String databaseName) {
        return new TrackDB(context, databaseName);
    }

    public static TripDBInterface getTripDB(Context context) {
        setDefaultProvider();
        return provider.getTripDBInstance(context);
    }

    public static TrackDBInterface getTrackDB(Context context, String databaseName) {
        setDefaultProvider();
        return provider.getTrackDBInstance(context, databaseName);
    }

    public static void setProvider(DBProvider p) {
        provider = p;
    }

    private static void setDefaultProvider() {
        if (null == provider) {
            provider = new DBProvider();
        }
    }

    static DBProvider provider;
}
