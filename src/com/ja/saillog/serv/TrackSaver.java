package com.ja.saillog.serv;

import com.ja.saillog.database.DBProvider;
import com.ja.saillog.database.TrackDBInterface;
import com.ja.saillog.database.TripDBInterface;
import com.ja.saillog.database.TripDBInterface.TripInfo;
import com.ja.saillog.utilities.DBLocationSink;
import com.ja.saillog.utilities.LocationServiceProvider;
import com.ja.saillog.utilities.LocationSinkAdapter;
import com.ja.saillog.utilities.Propulsion;

import android.content.Context;

public class TrackSaver {
    public TrackSaver(Context context) {
        this.context = context;
        sink = new DBLocationSink();
        sinkAdapter = new LocationSinkAdapter(sink);
    }
       
    public boolean startSaving() {
        // If already running, do not restart.
        if (null != trackDB) {
            return true;
        }
        
        TripDBInterface tdi = DBProvider.getTripDB(context);
        TripInfo ti = tdi.getActiveTrip();
        tdi.close();

        if (null == ti) {
            return false;
        }

        trackDB = DBProvider.getTrackDB(context, ti.dbFileName);
        sink.setDb(trackDB);
        
        LocationServiceProvider.get(context).requestUpdates(sinkAdapter);
                
        return true;
    }
   
    public void stopSaving() {
        LocationServiceProvider.get(context).stopUpdates(sinkAdapter);
        
        sink.setDb(null);

        if (null != trackDB) {
            trackDB.close();
        }
        trackDB = null;
    }
    
    public boolean isSaving() {
        return null != trackDB;
    }
    
    public void changePropulsion(Propulsion propulsion) {
        if (null != trackDB) {
            trackDB.insertEvent(propulsion);
        }
    }
 
    protected Context context;
    protected TrackDBInterface trackDB;
    protected DBLocationSink sink;
    protected LocationSinkAdapter sinkAdapter;
}
