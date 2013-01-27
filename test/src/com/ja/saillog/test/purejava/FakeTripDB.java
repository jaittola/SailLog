package com.ja.saillog.test.purejava;

import java.util.Random;

import com.ja.saillog.database.TripDBInterface;


public class FakeTripDB implements TripDBInterface {

    public FakeTripDB() {
        rand = new Random();
    }

    @Override
    public TripInfo insertTrip(String tripName,
                               String startLocation,
                               String endLocation) {
        insertedTrip = new TripInfo();
        insertedTrip.tripId = rand.nextLong();
        insertedTrip.tripName = tripName;
        insertedTrip.startLocation = startLocation;
        insertedTrip.endLocation = endLocation;

        return insertedTrip;
    }

    @Override
    public void updateTrip(TripInfo ti) {
        updatedTrip = ti;
    }

    @Override
    public void deleteTrip(long tripId) {
        deletedTripId = tripId;
    }

    @Override
    public TripInfo getActiveTrip() {
        if (null == selectedTripId) {
                return null;
        }
        return new TripInfo(selectedTripId,
                            selectedTripName(),
                            "FromSelected",
                            "ToSelected",
                            "theSelectedTripFile.db",
                            TripInfo.selected);
    }

    @Override
    public void close() {
        isClosed = true;
    }

    @Override
    public TripInfo getTripById(long tripId) {
        if (tripId == selectedTripId) {
            return getActiveTrip();
        }
        return aTrip;
    }

    public void setupTrips() {
        aTrip = new TripInfo(1, "MyTestingTrip",
                             "FromLocationTesting",
                             "ToLocationTesting",
                             "aFileNameForTesting.db",
                             TripInfo.notSelected);
        selectedTripId = defaultSelectedTripId;
    }

    public String selectedTripName() {
        return "MySelectedTrip" + selectedTripNameIdentifier +
                        selectedTripId;
    }

    @Override
    public void selectTrip(long tripId) {
        selectedTripId = tripId;
    }

    public Long selectedTripId = null;
    public String selectedTripNameIdentifier = "";
    public TripInfo aTrip;
    public TripInfo updatedTrip;
    public TripInfo insertedTrip;
    public Long deletedTripId;

    private Random rand;

    public boolean isClosed = false;
    
    public static final long defaultSelectedTripId = 42;
 }
