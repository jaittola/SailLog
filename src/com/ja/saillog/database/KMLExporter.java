package com.ja.saillog.database;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.DateFormat;

import com.ja.saillog.utilities.ExportFile;

public abstract class KMLExporter {
    static void export(SQLiteDatabase db, ExportFile exportFile) throws IOException {
        PrintWriter pw = new PrintWriter(exportFile.file(), "utf-8");

        // KML output below.
        makeKMLHeading(pw);

        // Grey if there is no sail or engine configuration.
        makeLineStyle(pw, "ff999999", styleUnknown);
        // Engine with red color.
        makeLineStyle(pw, "ffff0000", styleEngine);
        // Sailing with green.
        makeLineStyle(pw, "ff00ff00", styleSailing);
        // Motorsailing with lila.
        makeLineStyle(pw, "ffff00ff", styleMotorSailing);

        // TODO
        // - Trip name
        // - Trip description
        // - Names for the events
        // - Event filtering (no duplicates on the same location).
        // - Names for the trip sections.

        writeTrackLine(db, pw);
        writeEventMarkers(db, pw);

        pw.println("</Document>");
        pw.println("</kml>");

        pw.close();
    }

    private static void writeTrackLine(SQLiteDatabase db, PrintWriter pw) {
        String [] selectionArgs = {};

        String styleName = "unknown";
        String nextStyleName = null;
        double currLat = Double.NaN;
        double currLon = Double.NaN;

        Cursor initialConfig = db.rawQuery("SELECT engine, sailplan " +
                                           "FROM event " +
                                           "WHERE event_id = " +
                                           "  (SELECT MAX(event_id) " +
                                           "   FROM event " +
                                           "   WHERE position_id = 0)",
                                           selectionArgs);

        Cursor positions = db.rawQuery("SELECT p.latitude, p.longitude, " +
                                       "e.event_id, e.position_id, " +
                                       "strftime('%s', e.event_time), " +
                                       "e.engine, e.sailplan " +
                                       "FROM position p " +
                                       "LEFT OUTER JOIN event e " +
                                       "ON p.position_id = e.position_id " +
                                       "ORDER BY p.position_id",
                                       selectionArgs);

        try {
            if (0 < initialConfig.getCount()) {
                initialConfig.moveToNext();

                styleName = getLineStyle(initialConfig.getInt(0),
                                         initialConfig.getInt(1));
            }

            if (0 >= positions.getCount()) {
                initialConfig.close();
                positions.close();
                return;
            }
            // Route
            startLineString(pw, styleName);

            while (true == positions.moveToNext()) {
                // If there is event information, fetch the next line style.
                if (false == positions.isNull(5) &&
                    false == positions.isNull(6)) {
                    nextStyleName = getLineStyle(positions.getInt(5),
                                                 positions.getInt(6));
                    if (false == nextStyleName.equals(styleName)) {
                        styleName = nextStyleName;

                        endLineString(pw);
                        startLineString(pw, styleName);

                        writeCoordinates(pw, currLon, currLat);

                    }
                }
                currLat = positions.getDouble(0);
                currLon = positions.getDouble(1);

                writeCoordinates(pw, currLon, currLat);
            }

            endLineString(pw);
        } finally {
            positions.close();
            initialConfig.close();
        }
    }

    private static void writeEventMarkers(SQLiteDatabase db, PrintWriter pw) {
        String [] selectionArgs = {};
        Cursor events = db.rawQuery("SELECT e.event_id, e.position_id, " +
                                    "strftime('%s', e.event_time), e.engine, " +
                                    "e.sailplan, p.latitude, p.longitude " +
                                    "FROM event e " +
                                    "JOIN position p " +
                                    "ON e.position_id = p.position_id " +
                                    "ORDER BY e.event_id",
                                    selectionArgs);

        try {
            while (true == events.moveToNext()) {
                writeEventMarker(pw,
                                 getDate(events, 2),    // timestamp (ms)
                                 events.getInt(3),      // engine status
                                 events.getInt(4),      // sailplan
                                 events.getDouble(5),   // latitude
                                 events.getDouble(6));  // longitude
            }
        } finally {
            events.close();
        }
    }

    private static void writeEventMarker(PrintWriter pw,
                                         Date timestamp,
                                         int engine,
                                         int sailplan,
                                         double latitude,
                                         double longitude) {
        pw.println("<Placemark>");
        pw.println("<name>" + writeDate(timestamp) + "</name>");
        pw.println("<description><![CDATA[");
        p(pw, "At " + writeDate(timestamp));
        // TODO, coordinate formatting.
        p(pw, String.format("Coordinates: %.2f %.2f", latitude, longitude));
        p(pw, "Engine: " + (0 != engine ? "on" : "off") + ", ");
        // TODO, sail plan formatting.
        p(pw, "Sails: " + (0 != sailplan ? "up" : "down"));
        pw.println("]]></description>");
        pw.println("<Point>");
        pw.println("<coordinates>");

        writeCoordinates(pw, longitude, latitude);

        pw.println("</coordinates>");
        pw.println("</Point>");
        pw.println("</Placemark>");
    }

    private static void p(PrintWriter pw, String text) {
        pw.println("<p>");
        pw.println(text);
        pw.println("</p>");
    }

    private static String writeDate(Date timestamp) {
        return DateFormat.getDateTimeInstance().format(timestamp);
    }

    private static void makeKMLHeading(PrintWriter pw) {
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
        pw.println("<Document>");
    }

    private static void makeLineStyle(PrintWriter pw,
                                      String rgb,
                                      String styleName) {
        pw.println("<Style id=\"" + styleName + "\">");
        pw.println("<LineStyle><color>" +
                   rgb +
                   "</color><width>4</width></LineStyle>");
        pw.println("</Style>");
    }

    private static void startLineString(PrintWriter pw,
                                        String styleName) {
        pw.println("<Placemark>");
        pw.println("<styleUrl>#" + styleName + "</styleUrl>");
        pw.println("<LineString>");
        pw.println("<coordinates>");
    }

    private static void endLineString(PrintWriter pw) {
        pw.println("</coordinates>");
        pw.println("</LineString>");
        pw.println("</Placemark>");
    }

    private static void writeCoordinates(PrintWriter pw,
                                         double lon,
                                         double lat) {
        if (false == Double.isNaN(lon) &&
            false == Double.isNaN(lat)) {
            pw.println(String.format("%f,%f", lon, lat));
        }
    }

    private static String getLineStyle(int engineStatus, int sailPlan) {
        if (0 != engineStatus && 0 == sailPlan) {
            return styleEngine;
        }
        if (0 != engineStatus && 0 != sailPlan) {
            return styleMotorSailing;
        }
        if (0 == engineStatus && 0 != sailPlan) {
            return styleSailing;
        }
        return styleUnknown;
    }

    // This is copypasta and should be moved to some
    // central utility.
    private static Date getDate(Cursor c, int column) {
        if (true == c.isNull(column)) {
            return null;
        }

        return new Date(c.getLong(column) * 1000);
    }

    private static final String styleUnknown = "unknown";
    private static final String styleEngine = "engine";
    private static final String styleSailing = "sailing";
    private static final String styleMotorSailing = "motorSailing";
}
