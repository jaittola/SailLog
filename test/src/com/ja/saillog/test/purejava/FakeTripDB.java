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
        return selectedTi;
    }

    @Override
    public void close() {
    }

    @Override
    public TripInfo getTripById(long tripId) {
        // NOTE: This is broken. TODO.
        return selectedTi;
    }

    public void setupTrip() {
        selectedTi = new TripInfo(1, "MyTestingTrip",
                                  "aFileNameForTesting.db");
    }

    public TripInfo selectedTi;
}
