package com.ja.saillog.database;

import java.io.IOException;

import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.Speed;
import com.ja.saillog.utilities.ExportFile;

/**
 * An Interface for implementing mocks of the actual
 * position and event database interface.
 */
public interface TrackDBInterface {
    public class TripStats extends Object {
        public TripStats() {
        }

        public TripStats(Distance distance,
                         double engineTime,
                         double sailingTime,
                         double estimatedAvgSpeed) {
            this.distance = distance;
            this.engineTime = engineTime;
            this.sailingTime = sailingTime;
            this.estimatedAvgSpeed = estimatedAvgSpeed;
        }

        public Distance distance = null;
        public double engineTime = -1;
        public double sailingTime = -1;
        public double estimatedAvgSpeed = -1;
    }

    public void insertPosition(double latitude, double longitude,
                               double bearing, Speed speed,
                               Distance distanceFromPrevious, double accuracy);

    public void insertEvent(int engineStatus, int sailPlan);

    public TripStats getTripStats();

    public void exportDbAsKML(ExportFile exportFile) throws IOException;

    public void exportDbAsSQLite(ExportFile exportFile) throws IOException;

    public void close();
}
