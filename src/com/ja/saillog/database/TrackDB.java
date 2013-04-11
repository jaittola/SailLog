package com.ja.saillog.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.quantity.quantity.Speed;
import com.ja.saillog.utilities.ExportFile;
import com.ja.saillog.utilities.Propulsion;

public class TrackDB extends SailLogDBBase implements TrackDBInterface {

    public TrackDB(Context context, String databaseName) {
        super(context, databaseName);

        createDbStatements = new String[] {
            "set_vacuum",
            "drop_pos",
            "create_pos",
            "drop_event",
            "create_event",
            "drop_trip_stats",
            "create_trip_stats",
            "insert_trip_stats_entry",
        };
    }

    public void insertPosition(double latitude, double longitude,
                               double bearing, Speed speed,
                               Distance distanceFromPrevious, double accuracy) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement insertPosStm =
            getStatement(db,
                         "INSERT INTO position (latitude, longitude, " +
                         "speed, bearing, accuracy) " +
                         "VALUES (?, ?, ?, ?, ?)");
        SQLiteStatement updateStatsStm =
            getStatement(db,
                         "UPDATE trip_stats SET distance = distance + ?" +
                         tstampUpdateClause);

        try {
            db.beginTransaction();

            insertPosStm.bindDouble(1, latitude);
            insertPosStm.bindDouble(2, longitude);
            insertPosStm.bindDouble(3, QuantityFactory.metersPerSecond(speed).num());
            insertPosStm.bindDouble(4, bearing);
            insertPosStm.bindDouble(5, accuracy);
            insertPosStm.executeInsert();

            updateStatsStm.bindDouble(1, QuantityFactory.meters(distanceFromPrevious).num());
            updateStatsStm.executeInsert();

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void insertEvent(Propulsion propulsion) {

        SQLiteDatabase db = getWritableDatabase();

        SQLiteStatement insertEvStm =
            getStatement(db,
                         "INSERT INTO event (position_id, engine, sailplan) " +
                         "SELECT COALESCE(MAX(position_id), 0), ?, ? " +
                         "FROM position");
        SQLiteStatement updateStatsStm =
            getStatement(db,
                         "UPDATE trip_stats SET engine_time = engine_time + ?," +
                         "sailing_time = sailing_time + ?" +
                         tstampUpdateClause);

        double timeSincePrevious = 0;
        if (null != previousEventInsertTime) {
            timeSincePrevious = Math.round((float) (new Date().getTime() -
                                                    previousEventInsertTime.getTime())
                                                    / 1000.0);
        }

        long sailPlan = propulsion.getSailPlan();
        long engineStatus = (propulsion.getEngine() ? 1 : 0);
        
        double sailTimeToAdd = (0 != sailPlan ? timeSincePrevious : 0);
        double engineTimeToAdd = (0 != engineStatus ? timeSincePrevious : 0);

        try {
            db.beginTransaction();

            if (engineStatus != previousEngineStatus ||
                sailPlan != previousSailPlan) {
                
                insertEvStm.bindLong(1, engineStatus);
                insertEvStm.bindLong(2, sailPlan);
                insertEvStm.executeInsert();
            }

            updateStatsStm.bindDouble(1, engineTimeToAdd);
            updateStatsStm.bindDouble(2, sailTimeToAdd);
            updateStatsStm.executeInsert();

            db.setTransactionSuccessful();

            previousEventInsertTime = new Date();
            previousEngineStatus = engineStatus;
            previousSailPlan = sailPlan;
        }
        finally {
            db.endTransaction();
        }
    }

    public void setPreviousEventTimeForTesting(Date timestamp) {
        previousEventInsertTime = timestamp;
    }

    public TripStats getTripStats() {
        TripStats ts = null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT distance, engine_time, sailing_time, " +
                               "estimated_avg_speed, " +
                               "strftime('%s', first_entry) as first_entryf, " +
                               "strftime('%s', last_entry) as last_entryf " +
                               "FROM trip_stats " +
                               "LIMIT 1", null);
        try {
            if (true == c.moveToNext()) {
                ts = new TripStats(QuantityFactory.meters(c.getDouble(0)),
                                   c.getDouble(1),
                                   c.getDouble(2),
                                   c.getDouble(3),
                                   getDate(c, 4),
                                   getDate(c, 5));
            }
        }
        finally {
            c.close();
        }

        return ts;
    }

    /**
     * Create a copy of the database on another file. The idea is to export
     * this file on the MMC memory so that it can be transferred from
     * the device.
     */
    public void exportDbAsSQLite(ExportFile exportFile) throws IOException {

        File sourceFile = new File(getReadableDatabase().getPath());

        FileChannel source = null;
        FileChannel target = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            target = new FileOutputStream(exportFile.file()).getChannel();
            target.transferFrom(source, 0, source.size());
        } finally {
            if (null != source) {
                source.close();
            }
            if (null != target) {
                target.close();
            }
        }
    }

    public void exportDbAsKML(ExportFile exportFile) throws IOException {
        new KMLExporter().export(getReadableDatabase(), exportFile);
    }

    private Date getDate(Cursor c, int column) {
        if (true == c.isNull(column)) {
            return null;
        }

        return new Date(c.getLong(column) * 1000);
    }


    private String tstampUpdateClause =
        ", first_entry = coalesce(first_entry, datetime('now'))" +
        ", last_entry = datetime('now')";

    private Date previousEventInsertTime;
    private long previousSailPlan = -1;
    private long previousEngineStatus = -1;
}
