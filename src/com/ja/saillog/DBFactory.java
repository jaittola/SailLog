package com.ja.saillog;

import android.content.Context;

public class DBFactory implements DBFactoryInterface {

    public DBFactory(Context context) {
        this.context = context;
    }
    
    public TripDB tripDB() {
        // TODO Auto-generated method stub
        return null;
    }

    public TrackDBInterface trackDB() {
        return realTrackDB();
    }
    
    public TrackDB realTrackDB() {
        if (null == tdb) {
            tdb = new TrackDB(context, "SLDB.db");
        }
        
        return tdb;
    }
    
    private Context context;
    TrackDB tdb;
}
