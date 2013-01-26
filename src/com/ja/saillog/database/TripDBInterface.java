package com.ja.saillog.database;

public interface TripDBInterface {

    public class TripInfo extends Object {
        public TripInfo() {
            tripId = -1;
        }

        public TripInfo(long tripId,
                        String tripName,
                        String startLocation,
                        String endLocation,
                        String dbFileName) {
            this.tripId = tripId;
            this.tripName = tripName;
            this.startLocation = startLocation;
            this.endLocation = endLocation;
            this.dbFileName = dbFileName;
        }

        public boolean isSame(TripInfo other) {
            return other.tripId == tripId;
        }

        public long tripId;
        public String tripName;
        public String startLocation;
        public String endLocation;
        public String dbFileName;
    }

    public TripInfo insertTrip(String tripName,
                               String startLocation,
                               String endLocation);

    public void updateTrip(TripInfo ti);

    public void deleteTrip(long tripId);

    public TripInfo getActiveTrip();

    public void close();

    public TripInfo getTripById(long tripId);

    void selectTrip(long tripId);
}
