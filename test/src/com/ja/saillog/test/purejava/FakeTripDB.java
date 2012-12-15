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
    public TripInfo getSelectedTrip() {
        return selectedTrip;
    }

    @Override
    public void close() {
    }

    @Override
    public TripInfo getTripById(long tripId) {
        return aTrip;
    }

    public void setupTrips() {
        aTrip = new TripInfo(1, "MyTestingTrip",
                             "FromLocationTesting", 
                             "ToLocationTesting",
                             "aFileNameForTesting.db");
        selectedTrip = new TripInfo(42, "MySelectedTrip",
                                    "FromSelected", 
                                    "ToSelected",
                                    "theSelectedTripFile.db");
    }

    @Override
    public void selectTrip(long tripId) {
        if (tripId == aTrip.tripId) {
            selectedTrip = aTrip;
        }
        else {
            selectedTrip.tripId = tripId;
        }
    }

    public TripInfo aTrip;
    public TripInfo selectedTrip;
    public TripInfo updatedTrip;
    public TripInfo insertedTrip;
    public Long deletedTripId;

    private Random rand;
 }
