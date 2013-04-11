package com.ja.saillog.database;

import java.io.IOException;
import java.util.Date;

import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.Speed;
import com.ja.saillog.utilities.ExportFile;
import com.ja.saillog.utilities.Propulsion;

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
                         double estimatedAvgSpeed,
                         Date firstEntry,
                         Date lastEntry) {
            this.distance = distance;
            this.engineTime = engineTime;
            this.sailingTime = sailingTime;
            this.estimatedAvgSpeed = estimatedAvgSpeed;
            this.firstEntry = firstEntry;
            this.lastEntry = lastEntry;
        }

        public Distance distance = null;
        public double engineTime = -1;
        public double sailingTime = -1;
        public double estimatedAvgSpeed = -1;
        public Date firstEntry = null;
        public Date lastEntry = null;
    }

    public void insertPosition(double latitude, double longitude,
                               double bearing, Speed speed,
                               Distance distanceFromPrevious, double accuracy);

    public void insertEvent(Propulsion propulsion);

    /**
     * Set the timestamp of the event that was just inserted to
     * a specific time. To be used for testing purposes only.
     */
    public void setPreviousEventTimeForTesting(Date timestamp);

    public TripStats getTripStats();

    public void exportDbAsKML(ExportFile exportFile) throws IOException;

    public void exportDbAsSQLite(ExportFile exportFile) throws IOException;

    public void close();
}
