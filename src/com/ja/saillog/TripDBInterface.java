package com.ja.saillog;

public interface TripDBInterface {

    public class TripInfo extends Object {
        public TripInfo() {
            tripId = -1;
        }

        public TripInfo(long tripId, String tripName, String dbFileName) {
            this.tripId = tripId;
            this.tripName = tripName;
            this.dbFileName = dbFileName;
        }

        public long tripId;
        public String tripName;
        public String dbFileName;
    }

    public TripInfo insertTrip(String tripName);

    public void updateTrip(TripInfo ti);

    public void deleteTrip(long tripId);

    public TripInfo getSelectedTrip();

    public void close();

    public TripInfo getTripById(long tripId);

    void selectTrip(long tripId);

}
