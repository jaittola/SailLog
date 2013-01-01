package com.ja.saillog.database;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.utilities.ExportFile;
import com.ja.saillog.utilities.SailPlan;
import com.ja.saillog.utilities.StaticStrings;

public abstract class KMLExporter {
    static void export(SQLiteDatabase db, ExportFile exportFile)
        throws IOException {

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
        // - Event filtering (no duplicates on the same location).

        writeTrackLine(db, pw);
        writeEventMarkers(db, pw);

        pw.println("</Document>");
        pw.println("</kml>");

        pw.close();
    }

    private static void writeTrackLine(SQLiteDatabase db, PrintWriter pw) {
        String [] selectionArgs = {};

        String styleName = styleUnknown;
        String nextStyleName = null;
        double currLat = Double.NaN;
        double currLon = Double.NaN;
        int engineStatus = 0;
        SailPlan sailPlan = new SailPlan();

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

                engineStatus = initialConfig.getInt(0);
                sailPlan = getSailPlan(initialConfig, 1);

                styleName = getLineStyle(engineStatus,
                                         sailPlan.getSailPlan());
            }

            if (0 >= positions.getCount()) {
                initialConfig.close();
                positions.close();
                return;
            }

            // Write out a line that shows the route.

            startLineString(pw, styleName, eventName(engineStatus, sailPlan));

            while (true == positions.moveToNext()) {
                // If there is event information, fetch the next line style.
                if (false == positions.isNull(5) &&
                    false == positions.isNull(6)) {
                    engineStatus = positions.getInt(5);
                    sailPlan = getSailPlan(positions, 6);

                    nextStyleName = getLineStyle(engineStatus,
                                                 sailPlan.getSailPlan());
                    if (false == nextStyleName.equals(styleName)) {
                        styleName = nextStyleName;

                        endLineString(pw);
                        startLineString(pw, styleName,
                                        eventName(engineStatus, sailPlan));

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
        Cursor events = db.rawQuery("SELECT e.event_id, " +
                                    "       e.position_id, " +
                                    "       strftime('%s', e.event_time), " +
                                    "       e.engine, " +
                                    "       e.sailplan, " +
                                    "       p.latitude, " +
                                    "       p.longitude " +
                                    "FROM event e " +
                                    "JOIN position p " +
                                    "ON e.position_id = p.position_id " +
                                    "WHERE e.event_id IN " +
                                    "    (SELECT MAX(event_id) " +
                                    "     FROM event " +
                                    "     GROUP BY position_id) " +
                                    "ORDER BY e.event_id",
                                    selectionArgs);

        try {
            while (true == events.moveToNext()) {
                writeEventMarker(pw,
                                 getDate(events, 2),    // timestamp (ms)
                                 events.getInt(3),      // engine status
                                 getSailPlan(events, 4),
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
                                         SailPlan sailplan,
                                         double latitude,
                                         double longitude) {
        pw.println("<Placemark>");
        pw.println("<name>" + eventName(engine, sailplan) + "</name>");
        pw.println("<description><![CDATA[");
        p(pw, date(timestamp));
        p(pw,
          String.format("%s<br>%s",
                        QuantityFactory.dmsLatitude(latitude).withUnit(),
                        QuantityFactory.dmsLongitude(longitude).withUnit()));
        p(pw, StaticStrings.engine() + ": " +
                (0 != engine ?
                 StaticStrings.on(): StaticStrings.off()));

        List<Pair<String, String> > sails = sailplan.currentSails();
        for (Pair<String, String> p: sails) {
            p(pw, p.first + ": " + p.second);
        }

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

    private static String date(Date timestamp) {
        return DateFormat.getDateTimeInstance().format(timestamp);
    }

    private static String eventName(int engine, SailPlan sp) {
        return sp.generalDescription(0 != engine);
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
                                        String styleName,
                                        String name) {
        pw.println("<Placemark>");
        pw.println("<name>" + name + "</name>");
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

    private static String getLineStyle(int engineStatus, long sailPlanNum) {
        if (0 != engineStatus && 0 == sailPlanNum) {
            return styleEngine;
        }
        if (0 != engineStatus && 0 != sailPlanNum) {
            return styleMotorSailing;
        }
        if (0 == engineStatus && 0 != sailPlanNum) {
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

    private static SailPlan getSailPlan(Cursor c, int column) {
        SailPlan sp = new SailPlan();

        if (false == c.isNull(column)) {
            sp.setSailPlan(c.getLong(column));
        }

        return sp;
    }

    private static final String styleUnknown = "unknown";
    private static final String styleEngine = "engine";
    private static final String styleSailing = "sailing";
    private static final String styleMotorSailing = "motorSailing";
}
