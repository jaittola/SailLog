package com.ja.saillog;

public interface DBFactoryInterface {
    /*!
     * Return the trip and leg database object.
     */
    TripDB tripDB();
    
    /*!
     * Return a database for the current leg.
     */
    TrackDBInterface trackDB();
}
