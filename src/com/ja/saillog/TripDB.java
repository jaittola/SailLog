package com.ja.saillog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.Random;

public class TripDB extends SailLogDBBase implements TripDBInterface {

    public TripDB(Context context) {
        super(context, "TripDB.db");
        setup();
    }

    public TripDB(Context context, String databaseName) {
        super(context, databaseName);
        setup();
    }

    void setup() {
        createDbStatements = new String[] {
            "set_vacuum",
            "drop_trip",
            "create_trip",
        };

        // This is crap.
        rand = new Random();
    }

    @Override
    protected void finalize() throws Throwable {
        closeStatement(insertTripStm); insertTripStm = null;
        closeStatement(unselectTripStm); unselectTripStm = null;
        closeStatement(selectTripStm); selectTripStm = null;
        closeStatement(updateTripStm); updateTripStm = null;
        closeStatement(deleteTripStm); deleteTripStm = null;

        super.finalize();
    }

    public TripInfo insertTrip(String tripName) {
        TripInfo ti = null;

        SQLiteDatabase db = getWritableDatabase();

        // TODO: This needs fixing. Should use the row sequential row number.
        // But it's now easier like this.
        String tripDbFile = String.format("SLDB_%s_%d.db",
                                          tripName.replace(' ', '_'),
                                          rand.nextInt());

        if (null == insertTripStm) {
            insertTripStm = db.compileStatement("INSERT INTO trip " +
                                                "(trip_name, trip_db_filename) "
                                                + "VALUES (?, ?)");
        }

        try {
            db.beginTransaction();
            insertTripStm.bindString(1, tripName);
            insertTripStm.bindString(2, tripDbFile);
            long rowid = insertTripStm.executeInsert();
            db.setTransactionSuccessful();

            // This is SQLite-specific behaviour: the row id that is
            // returned from the insert is the same as the trip id,
            // because our trip ID is an 'integer primary key'.
            ti = new TripInfo(rowid, tripName, tripDbFile);
        } finally {
            db.endTransaction();
        }

        return ti;
    }


    public void updateTrip(TripInfo ti) {
        SQLiteDatabase db = getWritableDatabase();

        if (null == updateTripStm) {
            updateTripStm = db.compileStatement("UPDATE trip " +
                                                "SET trip_name = ? " +
                                                "WHERE trip_id = ?");
        }

        try {
            db.beginTransaction();
            updateTripStm.bindString(1, ti.tripName);
            updateTripStm.bindLong(2, ti.tripId);
            updateTripStm.executeInsert();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteTrip(long tripId) {
        SQLiteDatabase db = getWritableDatabase();

        if (null == deleteTripStm) {
            deleteTripStm = db.compileStatement("DELETE FROM trip " +
                                                "WHERE trip_id = ? " +
                                                "AND selected = 0");
        }

        try {
            db.beginTransaction();
            deleteTripStm.bindLong(1, tripId);
            deleteTripStm.executeInsert();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public TripInfo getTripById(long id) {
        String [] selectionArgs = { Long.toString(id) };

        return queryTripInfo("SELECT trip_id, trip_name, trip_db_filename " +
                             "FROM trip WHERE trip_id = ?",
                             selectionArgs);
    }

    public Cursor listTrips() {
        SQLiteDatabase db = getReadableDatabase();
        String [] selectionArgs = {};
        Cursor c = db.rawQuery("SELECT trip_id as _id, trip_name, selected " +
                               "FROM trip " +
                               "ORDER BY last_activated desc",
                               selectionArgs);
        return c;
    }

    public void selectTrip(long tripId) {
        SQLiteDatabase db = getWritableDatabase();

        if (null == selectTripStm) {
            selectTripStm = db.compileStatement("UPDATE trip " +
                                                "SET selected = 1,  " +
                                                "last_activated = CURRENT_TIMESTAMP " +
                                                "WHERE trip_id = ?");
        }

        ensureUnselectStatement(db);

        try {
            db.beginTransaction();

            unselectTripStm.executeInsert();

            selectTripStm.bindLong(1, tripId);
            selectTripStm.executeInsert();

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void unselectTrips() {
        SQLiteDatabase db = getWritableDatabase();

        ensureUnselectStatement(db);
        try {
            db.beginTransaction();
            unselectTripStm.executeInsert();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public TripInfo getSelectedTrip() {
        String [] selectionArgs = {};

        return queryTripInfo("SELECT trip_id, trip_name, trip_db_filename " +
                             "FROM trip WHERE " +
                             "trip_id = " +
                             "(SELECT MAX(trip_id) FROM trip " +
                             "WHERE selected = 1)",
                             selectionArgs);
    }

    private TripInfo queryTripInfo(String queryString, String[] selectionArgs) {
        TripInfo tdi = null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(queryString, selectionArgs);

        if (true == c.moveToNext()) {
            tdi = new TripInfo(c.getInt(c.getColumnIndex("trip_id")),
                               c.getString(c.getColumnIndex("trip_name")),
                               c.getString(c.getColumnIndex("trip_db_filename")));
        }
        c.close();

        return tdi;
    }

    private void ensureUnselectStatement(SQLiteDatabase db) {
        if (null == unselectTripStm) {
            unselectTripStm = db.compileStatement("UPDATE trip " +
                                                  "SET selected = 0 " +
                                                  "WHERE selected = 1");
        }
    }

    private void closeStatement(SQLiteStatement stm) {
        if (null != stm) {
            stm.close();
        }
    }

    private SQLiteStatement insertTripStm;
    private SQLiteStatement unselectTripStm;
    private SQLiteStatement selectTripStm;
    private SQLiteStatement updateTripStm;
    private SQLiteStatement deleteTripStm;

    Random rand;
}
