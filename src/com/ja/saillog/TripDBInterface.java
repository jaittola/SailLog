package com.ja.saillog;

import com.ja.saillog.TripDBInterface.TripInfo;

public interface TripDBInterface {

    public class TripInfo extends Object {
        public TripInfo() {}
        
        public TripInfo(int tripId, String tripName, String dbFileName) {
            this.tripId = tripId;
            this.tripName = tripName;
            this.dbFileName = dbFileName;
        }
        
        public long tripId;
        public String tripName; 
        public String dbFileName;
    }

    public abstract TripInfo getTrip(String tripName);

    public abstract TripInfo getSelectedTrip();

    public abstract void close();

    public abstract TripInfo getTripById(long tripId);

}