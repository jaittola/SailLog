package com.ja.saillog.test.purejava;

import com.ja.saillog.TripDBInterface;

public class FakeTripDB implements TripDBInterface {

    public FakeTripDB() {
    }

    @Override
    public TripInfo getTrip(String tripName) {
        return null;
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
        // NOTE: This is broken. TODO.
        return aTrip;
    }

    public void setupTrips() {
        aTrip = new TripInfo(1, "MyTestingTrip",
                             "aFileNameForTesting.db");
        selectedTrip = new TripInfo(42, "MySelectedTrip",
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

    public TripInfo selectedTrip;
    public TripInfo aTrip;
}
