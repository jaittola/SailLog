package com.ja.saillog.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.Date;

import com.ja.saillog.quantity.quantity.Distance;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.quantity.quantity.Speed;
import com.ja.saillog.utilities.ExportFile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

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
                         "UPDATE trip_stats SET distance = distance + ?");

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

    public void insertEvent(int engineStatus, int sailPlan) {

        SQLiteDatabase db = getWritableDatabase();

        SQLiteStatement insertEvStm =
            getStatement(db,
                         "INSERT INTO event (position_id, engine, sailplan) " +
                         "SELECT COALESCE(MAX(position_id), 0), ?, ? " +
                         "FROM position");
        SQLiteStatement updateStatsStm = 
            getStatement(db,
                         "UPDATE trip_stats SET engine_time = engine_time + ?," +
                         "sailing_time = sailing_time + ?");

        double timeSincePrevious = 0;
        if (null != previousEventInsertTime) {
            timeSincePrevious = Math.round((float) (new Date().getTime() - 
                                                    previousEventInsertTime.getTime()) 
                                                    / 1000.0);
        }
        
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
                               "estimated_avg_speed FROM trip_stats " +
                               "LIMIT 1", null);
        try {
            if (true == c.moveToNext()) {
                ts = new TripStats(QuantityFactory.meters(c.getDouble(0)),
                                   c.getDouble(1),
                                   c.getDouble(2),
                                   c.getDouble(3));
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

        SQLiteDatabase db = getReadableDatabase();
        String [] selectionArgs = {};
        Cursor c = db.rawQuery("SELECT position_id, longitude, latitude " +
                               "FROM position ORDER BY position_id",
                               selectionArgs);

        if (0 >= c.getCount()) {
            return;
        }

        try {
            PrintWriter pw = new PrintWriter(exportFile.file(), "utf-8");

            // Start of XML.
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
            pw.println("<Document>");
            pw.println("<Style id=\"red\">");
            pw.println("<LineStyle><color>ffffff00</color><width>4</width></LineStyle></Style>");
            pw.println("<Placemark>");
            pw.println("<styleUrl>#red</styleUrl>");
            pw.println("<LineString>");
            pw.println("<coordinates>");

            while (true == c.moveToNext()) {
                pw.println(String.format("%f,%f", c.getDouble(1), c.getDouble(2)));
            }

            pw.println("</coordinates>");
            pw.println("</LineString>");
            pw.println("</Placemark>");
            pw.println("</Document>");
            pw.println("</kml>");

            pw.close();
        } finally {
            c.close();
        }
    }

    private Date previousEventInsertTime;
    private int previousSailPlan = -1;
    private int previousEngineStatus = -1;
}
